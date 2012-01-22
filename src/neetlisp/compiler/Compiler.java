/*
 * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neetlisp.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import neetlisp.ClassFinder;
import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Isa;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.Name;
import neetlisp.Util;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

// TODO recheck all list operations!!!!
public class Compiler extends ClassLoader implements Opcodes
{
    final Context context;
    private final HashMap<Name, SpecializedCompile> specializes = new HashMap<Name, SpecializedCompile>();
    private boolean debugMode = true;
    
    public Compiler(final Context context)
    {
        super(Compiler.class.getClassLoader());
        this.context = context;
        this.importFns();
        this.cleanDebugFolder();
    }
    
    private void cleanDebugFolder()
    {
        final File folder = new File(System.getProperty("user.home"), "nlsp_dbg");
        folder.mkdir();
        final File[] files = folder.listFiles();
        for(File f : files)
        {
            if(f.isFile() && f.getName().endsWith(".class"))
                f.delete();
        }
    }
    
    public void debugOutClass(byte[] code, final String name)
    {
        if(this.debugMode)
        {
            final File folder = new File(System.getProperty("user.home"), "nlsp_dbg");
            final File out = new File(folder.getAbsolutePath(), name.substring(name.lastIndexOf('/') + 1) + ".class");
            try
            {
                final FileOutputStream fos = new FileOutputStream(out);
                fos.write(code);
                fos.close();
            }
            catch(IOException e)
            {
                //
            }
        }
    }
    
    public void setDebugMode(final boolean enable)
    {
        this.debugMode = enable;
    }
    
    private void importFns()
    {
        try
        {
            final List<Class<?>> classes = ClassFinder.find(ClassLoader.getSystemClassLoader(), "neetlisp.compiler");
            for(final Class<?> c : classes)
            {
                final Class<?>[] inters = c.getInterfaces();
                for(final Class<?> ci : inters)
                {
                    if(ci == SpecializedCompile.class)
                    {
                        final SpecializedCompile sc = (SpecializedCompile)c.newInstance();
                        this.specializes.put(sc.getName(), sc);
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Fn compile(Object obj)
    {
        return this.compile(obj, new Scope(this));
    }

    public Fn compile(Object obj, Scope scope)
    {
        try
        {
            final CFn cfn = new CFn();
            final MethodVisitor mv = cfn.createEval(0);

            this.compileObject(scope, cfn, mv, obj);
            cfn.stdEnd(mv);
            cfn.addInvoke0();
            byte[] code = cfn.finish();

            this.debugOutClass(code, cfn.getFullName());

            final Class<?> myFn = scope.getClassLoader().create(cfn.getFullName().replace('/', '.'), code);
            myFn.getMethod("initStatics", Context.class).invoke(null, this.context);
            return Util.injectClassLoader((Fn)myFn.getDeclaredConstructors()[0].newInstance(this.context), scope.getClassLoader());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object doMacroexpand(NFiniteSeq seq)
    {
        final Fn fn;
        if(Isa.name(seq.car()))
            fn = (Fn)this.context.resolve((Name)seq.car());
        else
            fn = (Fn)seq.car();
        if(!fn.isMacro())
            throw new IllegalArgumentException(fn + " is not a macro");
        return this.doMacroexpand(fn, seq);
    }
    
    public Object doMacroexpand(Fn fn, NFiniteSeq seq)
    {
        final Scope scope = new Scope(this);
        final CFn cfn = new CFn();
        final MethodVisitor mv = cfn.createEval(0);
        final Name name = (Name)seq.car();

        final FnMeta meta = fn.getMetaData();
        NFiniteSeq cdr = (NFiniteSeq)seq.cdr();
        if(!Util.assureArgc(fn, cdr.size()))
            throw new IllegalArgumentException("Incorrect argument count for " + fn);

        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        final String fnName = fn.getClass().getCanonicalName().replace('.', '/');

        cfn.visitFnObject(mv, fn, name);
        mv.visitTypeInsn(CHECKCAST, fnName);
        
        final NIterator it = cdr.getIterator();
        for(int i = 0; i < meta.arguments; i++)
        {
            SpecialCoreQuote.compileObject(scope, cfn, mv, it.next());
            sb.append("Ljava/lang/Object;");
        }
        
        if(meta.varargs)
        {
            sb.append("Ljava/lang/Object;");

            final int len = cdr.size() - meta.arguments;
            CFn.visitIntConst(mv, len);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            for(int i = 0; i < len; i++)
            {
                mv.visitInsn(DUP);
                CFn.visitIntConst(mv, i);
                    SpecialCoreQuote.compileObject(scope, cfn, mv, it.next());
                mv.visitInsn(AASTORE);
            }
            mv.visitMethodInsn(INVOKESTATIC, "neetlisp/seq/NArray", "wrap", "([Ljava/lang/Object;)Lneetlisp/seq/NArray;");
        }
        
        sb.append(")Ljava/lang/Object;");
        mv.visitMethodInsn(INVOKEVIRTUAL, fnName, "eval", sb.toString());
        cfn.stdEnd(mv);
        cfn.addInvoke0();

        try
        {
            byte[] code = cfn.finish();
            
            final Class<?> myFn = scope.getClassLoader().create(cfn.getFullName().replace('/', '.'), code);
            myFn.getMethod("initStatics", Context.class).invoke(null, this.context);
            return Util.injectClassLoader((Fn)myFn.getDeclaredConstructors()[0].newInstance(this.context), scope.getClassLoader()).invoke();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    void compileObject(Scope scope, CFn cfn, MethodVisitor mv, Object obj)
    {
        this.compileObject(scope, cfn, mv, obj, false);
    }
    
    void compileObject(Scope scope, CFn cfn, MethodVisitor mv, Object obj, boolean fromUnquote)
    {
        if(Isa.seq(obj))
        {
            this.compileSeq(scope, cfn, mv, (NFiniteSeq)obj);
        }
        else if(Isa.bool(obj))
        {
            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", Util.booleanValue(obj) ? "TRUE" : "FALSE", "Ljava/lang/Boolean;");
        }
        else if(Isa.name(obj))
        {
            this.compileResolve(scope, cfn, mv, scope.resolveToName((Name)obj), fromUnquote);
        }
        else
        {
            ((Compilable)obj).compile(cfn, mv);
        }
    }
    
    void compileResolve(Scope scope, CFn cfn, MethodVisitor mv, Name name)
    {
        this.compileResolve(scope, cfn, mv, name, false);
    }
    
    void compileResolve(Scope scope, CFn cfn, MethodVisitor mv, Name name, final boolean fromUnquote)
    {
        if(!name.hasNs() && scope.hasLocal(name))
        {
            mv.visitVarInsn(ALOAD, scope.addLocal(name));
        }
        else if(!name.hasNs() && scope.uppersHaveLocal(name))
        {
            int index = scope.addClosureVariable(cfn, name);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, cfn.getFullName(), "cl_" + index, "Ljava/lang/Object;");
        }
        else if(scope.hasClosure(name))
        {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, cfn.getFullName(), "cl_" + scope.getClosure(name), "Ljava/lang/Object;");
        }
        else
        {
            if(fromUnquote)
                name.compile(cfn, mv);
            else
            {
                mv.visitVarInsn(ALOAD, 0);
                name.compile(cfn, mv);
                mv.visitTypeInsn(CHECKCAST, "neetlisp/Name");
                mv.visitMethodInsn(INVOKEVIRTUAL, cfn.getFullName(), "resolve", "(Lneetlisp/Name;)Ljava/lang/Object;");
            }
        }
    }
    
    void compileSeq(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq seq)
    {
        Object car = seq.car();
        Fn fn = null;
        if(Isa.name(car))
            fn = (Fn)this.context.resolve((Name)car);

        if(fn != null)
        {
            final FnMeta meta = fn.getMetaData();
            NFiniteSeq cdr = (NFiniteSeq)seq.cdr();
            if(!Util.assureArgc(fn, cdr.size()))
                throw new IllegalArgumentException("Incorrect argument count for " + fn);
    
            final SpecializedCompile sc = this.specializes.get(meta.name);
            if(sc != null)
            {
                sc.compile(scope, cfn, mv, cdr);
                return;
            }

            if(fn.isSpecialForm())
                throw new IllegalStateException("Can not call special form " + fn + " directly");
            
            final StringBuilder sb = new StringBuilder();
            sb.append('(');
            final String fnName = fn.getClass().getCanonicalName().replace('.', '/');

            if(fn.isMacro())
            {
                Object objs = this.doMacroexpand(fn, seq);
                this.compileObject(scope, cfn, mv, objs);
                return;
            }
            
            cfn.visitFnObject(mv, fn, (Name)car);
            mv.visitTypeInsn(CHECKCAST, fnName);
            
            final NIterator it = cdr.getIterator();
            for(int i = 0; i < meta.arguments; i++)
            {
                this.compileObject(scope, cfn, mv, it.next());
                sb.append("Ljava/lang/Object;");
            }
            
            if(meta.varargs)
            {
                sb.append("Ljava/lang/Object;");
    
                final int len = cdr.size() - meta.arguments;
                if(len == 0)
                {
                    mv.visitFieldInsn(GETSTATIC, "neetlisp/seq/NArray", "EMPTY", "Lneetlisp/seq/NArray;");
                }
                else if(len < 5)
                {
                    final StringBuilder wa = new StringBuilder();
                    wa.append('(');
                    for(int i = 0; i < len; i++)
                    {
                        wa.append("Ljava/lang/Object;");
                        this.compileObject(scope, cfn, mv, it.next());
                    }
                    wa.append(")Lneetlisp/seq/NArray;");
                    mv.visitMethodInsn(INVOKESTATIC, "neetlisp/Util", "wrapArray", wa.toString());
                }
                else
                {
                    CFn.visitIntConst(mv, len);
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                    for(int i = 0; i < len; i++)
                    {
                        mv.visitInsn(DUP);
                        CFn.visitIntConst(mv, i);
                        this.compileObject(scope, cfn, mv, it.next());
                        mv.visitInsn(AASTORE);
                    }
                    mv.visitMethodInsn(INVOKESTATIC, "neetlisp/seq/NArray", "wrap", "([Ljava/lang/Object;)Lneetlisp/seq/NArray;");
                }
            }
            
            sb.append(")Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEVIRTUAL, fnName, "eval", sb.toString());
        }
        else
        {
            this.compileObject(scope, cfn, mv, car);
            mv.visitTypeInsn(CHECKCAST, "neetlisp/Fn");
            NFiniteSeq cdr = (NFiniteSeq)seq.cdr();
            int len = cdr.size();
            CFn.visitIntConst(mv, len);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            final NIterator it = cdr.getIterator();
            for(int i = 0; i < len; i++)
            {
                mv.visitInsn(DUP);
                CFn.visitIntConst(mv, i);
                this.compileObject(scope, cfn, mv, it.next());
                mv.visitInsn(AASTORE);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "neetlisp/Fn", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;");
        }
    }
}

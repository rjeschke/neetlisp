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

import neetlisp.Context;
import neetlisp.FnMeta;
import neetlisp.Isa;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NString;
import neetlisp.Name;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreDefmacro implements SpecializedCompile, Opcodes
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/defmacro");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        final Name varname = Name.fromString("&");
        Object obj;
        final NIterator it = cdr.getIterator();
        
        Name name;
        String doc = null;
        int argc = 0;
        boolean varargs = false;
        
        obj = it.next();
        
        Scope nscope = scope.copy();
        nscope.isFn = true;
        name = (Name)obj;
        if(!name.hasNs())
            name = new Name(scope.compiler.context.getNamespace(), name.getName());
        obj = it.next();
        if(Isa.nstring(obj))
        {
            doc = ((NString)obj).toString();
            obj = it.next();
        }
        if(!Isa.seq(obj))
        {
            throw new IllegalArgumentException("Argument list expected");
        }
        
        name.compile(cfn, mv);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "neetlisp/Name");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.SWAP);

        final NFiniteSeq arg = (NFiniteSeq)obj;
        argc = arg.size();
        final NIterator ita = arg.getIterator();
        final StringBuilder sig = new StringBuilder();
        sig.append('(');
        while(ita.hasNext())
        {
            final Name n = (Name)ita.next();
            if(n.equals(varname))
            {
                varargs = true;
                break;
            }
            if(nscope.hasLocal(n))
                throw new IllegalArgumentException("Duplicate argument " + n);
            nscope.addLocal(n);
            sig.append("Ljava/lang/Object;");
        }
        if(varargs)
        {
            final Name n = (Name)ita.next();
            if(nscope.hasLocal(n))
                throw new IllegalArgumentException("Duplicate argument " + n);
            nscope.addLocal(n);
            argc -= 2;
            sig.append("Ljava/lang/Object;");
        }
        sig.append(")Ljava/lang/Object;");
        
        if(!it.hasNext())
            throw new IllegalArgumentException("Missing fn body");
        
        final FnMeta meta = new FnMeta(name.getFullName(), doc, argc, varargs, FnMeta.FLAG_MACRO);
        
        CFn ncfn = new CFn(meta);
        MethodVisitor nmv = ncfn.createEval(varargs ? argc + 1 : argc);
        boolean first = true;
        while(it.hasNext())
        {
            if(!first)
                nmv.visitInsn(POP);
            scope.compiler.compileObject(nscope, ncfn, nmv, it.next());
            first = false;
        }
        ncfn.stdEnd(nmv);
        
        nmv = ncfn.createInvoke();
        nmv.visitVarInsn(ALOAD, 0);
        nmv.visitVarInsn(ALOAD, 1);
        nmv.visitInsn(ARRAYLENGTH);
        nmv.visitMethodInsn(INVOKEVIRTUAL, ncfn.getFullName(), "assureArguments", "(I)V");
        
        nmv.visitVarInsn(ALOAD, 0);
        for(int n = 0; n < argc; n++)
        {
            nmv.visitVarInsn(ALOAD, 1);
            CFn.visitIntConst(nmv, n);
            nmv.visitInsn(AALOAD);
        }
        if(varargs)
        {
            nmv.visitVarInsn(ALOAD, 1);
            CFn.visitIntConst(nmv, argc);
            nmv.visitMethodInsn(INVOKESTATIC, "neetlisp/seq/NArray", "hardTailseq", "([Ljava/lang/Object;I)Lneetlisp/seq/NArray;");
        }
        nmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ncfn.getFullName(), "eval", sig.toString());
        ncfn.stdEnd(nmv);
        
        byte[] code = ncfn.finish();

        try
        {
            
            scope.compiler.debugOutClass(code, ncfn.getFullName());
    
            final Class<?> myFn = scope.compiler.context.getClassLoader().create(ncfn.getFullName().replace('/', '.'), code);
            myFn.getMethod("initStatics", Context.class).invoke(null, scope.compiler.context);
            
            nscope.remove();

            mv.visitTypeInsn(Opcodes.NEW, ncfn.getFullName());
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, cfn.getFullName(), "context", "Lneetlisp/Context;");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ncfn.getFullName(), "<init>", "(Lneetlisp/Context;)V");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cfn.getFullName(), "add", "(Lneetlisp/Name;Ljava/lang/Object;)V");
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        
    }
}

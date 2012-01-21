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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Isa;
import neetlisp.Name;
import neetlisp.asm.ClassWriter;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class CFn implements Opcodes
{
    private final static AtomicLong ufncounter = new AtomicLong();
    private final static AtomicLong utfncounter = new AtomicLong();
    private final HashMap<Object, String> constants = new HashMap<Object, String>();
    private long ucounter = 0;
    private FnMeta fnMeta;
    private final String fullName;
    private final String className;
    private ClassWriter cw;
    private MethodVisitor clinit;
    
    public CFn()
    {
        this.fnMeta = null;
        this.className = uniqueTempClassname();
        this.fullName = "nlsp/utfn/" + this.className;
        this.initClassWriter();
    }

    public CFn(final FnMeta fnMeta)
    {
        this.fnMeta = fnMeta;
        this.className = uniqueClassname();
        this.fullName = "nlsp/ufn/" + this.className;
        this.initClassWriter();
    }
    
    public String getFullName()
    {
        return this.fullName;
    }
    
    public String addConstant(Object obj)
    {
        String name = this.constants.get(obj);
        if(name != null)
            return name;
        name = "C" + this.ucounter++;
        this.constants.put(obj, name);
        this.cw.visitField(ACC_STATIC | ACC_PRIVATE | ACC_FINAL, name, "Ljava/lang/Object;", null, null);
        return name;
    }
    
    public void addClosureField(int index)
    {
        this.cw.visitField(ACC_PRIVATE, "cl_" + index, "Ljava/lang/Object;", null, null);
    }
    
    public boolean hasConstant(Object obj)
    {
        return this.constants.get(obj) != null;
    }
    
    public MethodVisitor getClinit()
    {
        return this.clinit;
    }
    
    public void visitLoadConst(MethodVisitor mv, String name)
    {
        mv.visitFieldInsn(GETSTATIC, this.fullName, name, "Ljava/lang/Object;");
    }
    
    public static void visitIntConst(MethodVisitor mv, int val)
    {
        switch(val)
        {
        case -1:
            mv.visitInsn(ICONST_M1);
            break;
        case 0:
            mv.visitInsn(ICONST_0);
            break;
        case 1:
            mv.visitInsn(ICONST_1);
            break;
        case 2:
            mv.visitInsn(ICONST_2);
            break;
        case 3:
            mv.visitInsn(ICONST_3);
            break;
        case 4:
            mv.visitInsn(ICONST_4);
            break;
        case 5:
            mv.visitInsn(ICONST_5);
            break;
        default:
            mv.visitLdcInsn(Integer.valueOf(val));
            break;
        }
    }

    public void visitFnObject(MethodVisitor mv, Fn fn)
    {
        this.visitFnObject(mv, fn, null);
    }
    
    public void visitFnObject(MethodVisitor mv, Fn fn, Name n)
    {
        String name = this.constants.get(fn);
        if(name == null)
        {
            this.clinit.visitVarInsn(ALOAD, 0);
            name = "C" + this.ucounter++;
            this.constants.put(fn, name);
            this.cw.visitField(ACC_STATIC | ACC_PRIVATE | ACC_FINAL, name, "Ljava/lang/Object;", null, null);
            if(n != null)
                n.compile(this, this.clinit);
            else
                fn.getMetaData().name.compile(this, this.clinit);
            this.clinit.visitTypeInsn(CHECKCAST, "neetlisp/Name");
            this.clinit.visitMethodInsn(INVOKEVIRTUAL, "neetlisp/Context", "safeResolve", "(Lneetlisp/Name;)Ljava/lang/Object;");
            this.clinit.visitFieldInsn(Opcodes.PUTSTATIC, this.fullName, name, "Ljava/lang/Object;");
        }
        this.visitLoadConst(mv, name);
    }
    
    public MethodVisitor createEval(int args)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        for(int i = 0; i < args; i++)
            sb.append("Ljava/lang/Object;");
        sb.append(")Ljava/lang/Object;");
        return this.cw.visitMethod(ACC_PUBLIC,
                "eval",
                sb.toString(),
                null,
                null);
    }
    
    public MethodVisitor createsetClosure(int args)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        for(int i = 0; i < args; i++)
            sb.append("Ljava/lang/Object;");
        sb.append(")V");
        return this.cw.visitMethod(ACC_PUBLIC,
                "setClosure",
                sb.toString(),
                null,
                null);
    }
    
    public MethodVisitor createInvoke()
    {
        return this.cw.visitMethod(ACC_PUBLIC,
                "invoke",
                "([Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null);
    }
    
    public void stdEnd(MethodVisitor mv)
    {
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    public void addInvoke0()
    {
        MethodVisitor mv = this.createInvoke();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.fullName, "eval", "()Ljava/lang/Object;");
        this.stdEnd(mv);
    }
    
    public byte[] finish()
    {
        this.clinit.visitInsn(RETURN);
        this.clinit.visitMaxs(0, 0);
        this.clinit.visitEnd();
        return this.cw.toByteArray();
    }
    
    private void initClassWriter()
    {
        this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, this.fullName, null, "neetlisp/Fn", null);

        if(this.fnMeta != null)
        {
            this.cw.visitField(ACC_STATIC | ACC_PRIVATE | ACC_FINAL, "CMETA", "Lneetlisp/FnMeta;", null, null);
        }
        
        MethodVisitor mv = this.cw.visitMethod(ACC_PUBLIC,
                "<init>",
                "(Lneetlisp/Context;)V",
                null,
                null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        if(this.fnMeta != null)
            mv.visitFieldInsn(GETSTATIC, this.fullName, "CMETA", "Lneetlisp/FnMeta;");
        else
            mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKESPECIAL, "neetlisp/Fn", "<init>", "(Lneetlisp/Context;Lneetlisp/FnMeta;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        this.clinit = this.cw.visitMethod(ACC_STATIC | ACC_PUBLIC,
                "initStatics",
                "(Lneetlisp/Context;)V",
                null,
                null);
        if(this.fnMeta != null)
        {
            this.clinit.visitTypeInsn(NEW, "neetlisp/FnMeta");
            this.clinit.visitInsn(DUP);
            if(this.fnMeta.name == null)
                this.clinit.visitInsn(ACONST_NULL);
            else
                this.clinit.visitLdcInsn(this.fnMeta.name.getFullName());
            if(this.fnMeta.doc != null && !Isa.nil(this.fnMeta.doc))
                this.clinit.visitLdcInsn(this.fnMeta.doc);
            else
                this.clinit.visitInsn(ACONST_NULL);
            CFn.visitIntConst(this.clinit, this.fnMeta.arguments);
            this.clinit.visitInsn(this.fnMeta.varargs ? ICONST_1 : ICONST_0);
            CFn.visitIntConst(this.clinit, this.fnMeta.flags);
            this.clinit.visitMethodInsn(INVOKESPECIAL, "neetlisp/FnMeta", "<init>", "(Ljava/lang/String;Ljava/lang/String;IZI)V");
            this.clinit.visitFieldInsn(PUTSTATIC, this.fullName, "CMETA", "Lneetlisp/FnMeta;");
        }
    }
    
    public static String uniqueClassname()
    {
        return "Nfn_" + ufncounter.incrementAndGet();
    }
    
    public static String uniqueTempClassname()
    {
        return "Ntfn_" + utfncounter.incrementAndGet();
    }
}

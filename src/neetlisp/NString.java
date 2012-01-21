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
package neetlisp;

import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;
import neetlisp.compiler.CFn;
import neetlisp.compiler.Compilable;

public class NString implements Compilable
{
    public final static NString EMPTY = new NString("");
    final String string;
    
    public NString(final String string)
    {
        this.string = string;
    }
    
    @Override
    public String toString()
    {
        return this.string;
    }
    
    @Override
    public int hashCode()
    {
        return this.string.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof NString) ? ((NString)obj).string.equals(this.string) : false;
    }

    @Override
    public void compile(CFn cfn, MethodVisitor mv)
    {
        final String name;
        if(!cfn.hasConstant(this))
        {
            name = cfn.addConstant(this);
            final MethodVisitor cmv = cfn.getClinit();
            cmv.visitTypeInsn(Opcodes.NEW, "neetlisp/NString");
            cmv.visitInsn(Opcodes.DUP);
            cmv.visitLdcInsn(this.string);
            cmv.visitMethodInsn(Opcodes.INVOKESPECIAL, "neetlisp/NString", "<init>", "(Ljava/lang/String;)V");
            cmv.visitFieldInsn(Opcodes.PUTSTATIC, cfn.getFullName(), name, "Ljava/lang/Object;");
        }
        else
        {
            name = cfn.addConstant(this);
        }
        cfn.visitLoadConst(mv, name);
    }
}

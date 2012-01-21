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

import neetlisp.Isa;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.Name;
import neetlisp.Util;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreQuote implements SpecializedCompile
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/quote");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        compileObject(scope, cfn, mv, cdr.getIterator().next());
    }
    
    public static void compileObject(Scope scope, CFn cfn, MethodVisitor mv, Object obj)
    {
        if(Isa.seq(obj))
        {
            final NFiniteSeq seq = (NFiniteSeq)obj;
            final NIterator it = seq.getIterator();
            final int len = seq.size();
            CFn.visitIntConst(mv, len);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            for(int i = 0; i < len; i++)
            {
                mv.visitInsn(Opcodes.DUP);
                CFn.visitIntConst(mv, i);
                compileObject(scope, cfn, mv, it.next());
                mv.visitInsn(Opcodes.AASTORE);
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/seq/NArray", "wrap", "([Ljava/lang/Object;)Lneetlisp/seq/NArray;");
        }
        else
        {
            if(Isa.bool(obj))
                mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", Util.booleanValue(obj) ? "TRUE" : "FALSE", "Ljava/lang/Boolean;");
            else
                ((Compilable)obj).compile(cfn, mv);
        }
    }
}

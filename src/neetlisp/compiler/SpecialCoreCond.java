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
import neetlisp.asm.Label;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreCond implements SpecializedCompile
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/cond");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        final NIterator it = cdr.getIterator();
        
        final Label end = new Label();
        Label next = null;
        boolean hasElse = false;
        while(it.hasNext())
        {
            final Object cond = it.next();
            if(!it.hasNext())
                throw new IllegalArgumentException("cond needs an even number of elements");
            final Object expr = it.next();
            // Skip constant FALSE conditions
            if(Isa.nil(cond) || cond == Boolean.FALSE)
                continue;
            if(next != null)
            {
                mv.visitJumpInsn(Opcodes.GOTO, end);
                mv.visitLabel(next);
            }
            next = new Label();
            // Constant TRUE expression -> exit cond
            if(Isa.keyword(cond)
                    || Isa.nstring(cond)
                    || Isa.number(cond)
                    || cond == Boolean.TRUE)
            {
                hasElse = true;
            }
            else
            {
                scope.compiler.compileObject(scope, cfn, mv, cond);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/Util", "booleanValue", "(Ljava/lang/Object;)Z");
                mv.visitJumpInsn(Opcodes.IFEQ, next);
            }
            
            scope.compiler.compileObject(scope, cfn, mv, expr);
            if(hasElse)
                break;
            
        }
        if(!hasElse)
        {
            if(next != null)
            {
                mv.visitJumpInsn(Opcodes.GOTO, end);
                mv.visitLabel(next);    
            }
            mv.visitFieldInsn(Opcodes.GETSTATIC, "neetlisp/Nil", "NIL", "Lneetlisp/Nil;");
        }
        mv.visitLabel(end);
    }
}

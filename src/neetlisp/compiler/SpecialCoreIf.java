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

public class SpecialCoreIf implements SpecializedCompile
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/if");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        final NIterator it = cdr.getIterator();
        final Object cond = it.next();
        if(Isa.nil(cond) || cond == Boolean.FALSE)
        {
            // just else
            it.next();
            scope.compiler.compileObject(scope, cfn, mv, it.next());
        }
        else if(Isa.keyword(cond)
                || Isa.nstring(cond)
                || Isa.number(cond)
                || cond == Boolean.TRUE)
        {
            // just then
            scope.compiler.compileObject(scope, cfn, mv, it.next());
        }
        else
        {
            // both
            scope.compiler.compileObject(scope, cfn, mv, cond);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/Util", "booleanValue", "(Ljava/lang/Object;)Z");
            final Label l0 = new Label();
            final Label l1 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);
            scope.compiler.compileObject(scope, cfn, mv, it.next());
            mv.visitJumpInsn(Opcodes.GOTO, l1);
            mv.visitLabel(l0);
            scope.compiler.compileObject(scope, cfn, mv, it.next());
            mv.visitLabel(l1);
        }
    }
}


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

import neetlisp.Fn;
import neetlisp.NFiniteSeq;
import neetlisp.Name;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreDef implements SpecializedCompile
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/def");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        Name name = (Name)cdr.car();
        if(!name.hasNs())
        {
            name = new Name(scope.compiler.context.getNamespace(), name.getName());
        }
        name.compile(cfn, mv);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "neetlisp/Name");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.SWAP);
        final Scope nscope = scope.copy();
        nscope.lastName = name;
        final Fn fn = scope.compiler.compile(cdr.get(1), nscope);
        final String fnName = fn.getClass().getCanonicalName().replace('.', '/');
        nscope.remove();
        
        mv.visitTypeInsn(Opcodes.NEW, fnName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, cfn.getFullName(), "context", "Lneetlisp/Context;");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, fnName, "<init>", "(Lneetlisp/Context;)V");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, fnName, "eval", "()Ljava/lang/Object;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cfn.getFullName(), "add", "(Lneetlisp/Name;Ljava/lang/Object;)V");
    }
}

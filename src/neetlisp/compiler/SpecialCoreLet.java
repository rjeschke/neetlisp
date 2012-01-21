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

import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.Name;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreLet implements SpecializedCompile
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/let");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        final NIterator it = cdr.getIterator();
        final NFiniteSeq args = (NFiniteSeq)it.next();
        if(args.size() < 2 || (args.size() & 1) != 0)
            throw new IllegalArgumentException("Let binding needs at least 2 elements and an even count of elements");
        
        final Scope nscope = Scope.forLet(scope);
        final NIterator ita = args.getIterator();
        while(ita.hasNext())
        {
            final Name name = (Name)ita.next();
            if(nscope.hasLocal(name))
                throw new IllegalArgumentException("Duplicate argument to let " + name);
            int index = nscope.addLocal(name);
            nscope.lastName = name;
            scope.compiler.compileObject(nscope, cfn, mv, ita.next());
            mv.visitVarInsn(Opcodes.ASTORE, index);
        }
        
        boolean first = true;
        while(it.hasNext())
        {
            if(!first)
                mv.visitInsn(Opcodes.POP);
            scope.compiler.compileObject(nscope, cfn, mv, it.next());
            first = false;
        }
        
        nscope.remove();
    }
}

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

public class SpecialCoreDo implements SpecializedCompile, Opcodes
{
    @Override
    public Name getName()
    {
        return Name.fromString("core/do");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        final NIterator it = cdr.getIterator();
        boolean first = true;
        while(it.hasNext())
        {
            if(!first)
                mv.visitInsn(POP);
            scope.compiler.compileObject(scope, cfn, mv, it.next());
            first = false;
        }
    }
}

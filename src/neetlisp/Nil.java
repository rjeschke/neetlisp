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

public class Nil implements Compilable
{
    public final static Nil NIL = new Nil();
    
    private Nil()
    {
        // we are nil
    }
    
    @Override
    public String toString()
    {
        return "nil";
    }

    @Override
    public void compile(CFn cfn, MethodVisitor mv)
    {
        mv.visitFieldInsn(Opcodes.GETSTATIC, "neetlisp/Nil", "NIL", "Lneetlisp/Nil;");
    }
}

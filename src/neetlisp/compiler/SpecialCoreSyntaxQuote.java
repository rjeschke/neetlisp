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
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;

public class SpecialCoreSyntaxQuote implements SpecializedCompile, Opcodes
{
    final static Name unquote = Name.fromString("core/unquote");
    final static Name unquote_splice = Name.fromString("core/unquote-splice");
    
    @Override
    public Name getName()
    {
        return Name.fromString("core/syntax-quote");
    }
    
    @Override
    public void compile(Scope scope, CFn cfn, MethodVisitor mv, NFiniteSeq cdr)
    {
        compileObject(scope, cfn, mv, cdr.getIterator().next(), false);
    }
    
    public boolean compileObject(Scope scope, CFn cfn, MethodVisitor mv, Object obj, boolean inList)
    {
        if(Isa.seq(obj))
        {
            final NFiniteSeq seq = (NFiniteSeq)obj;
            
            if(seq.car().equals(unquote))
            {
                if(seq.size() != 2)
                    throw new IllegalStateException("Argument mismatch: Expected 2, got " + seq.size() + " for " + scope.compiler.context.resolve(unquote));
                scope.compiler.compileObject(scope, cfn, mv, seq.get(1), true);
                return true;
            }
            if(seq.car().equals(unquote_splice))
            {
                if(seq.size() != 2)
                    throw new IllegalStateException("Argument mismatch: Expected 2, got " + seq.size() + " for " + scope.compiler.context.resolve(unquote_splice));
                if(!inList)
                    throw new IllegalStateException("unquote-splice outside of seq");
                scope.compiler.compileObject(scope, cfn, mv, seq.get(1), true);
                return false;
            }
            int len = seq.size();
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            final NIterator it = seq.getIterator();
            for(int i = 0; i < len; i++)
            {
                mv.visitInsn(DUP);
                if(compileObject(scope, cfn, mv, it.next(), true))
                {
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z");
                    mv.visitInsn(POP);
                }
                else
                    mv.visitMethodInsn(INVOKESTATIC, "neetlisp/Util", "appendToList", "(Ljava/util/ArrayList;Lneetlisp/NSeq;)V");
            }
            mv.visitMethodInsn(INVOKESTATIC, "neetlisp/seq/NList", "wrap", "(Ljava/util/ArrayList;)Lneetlisp/seq/NList;");
        }
        else
        {
            if(Isa.name(obj))
                scope.resolveToName((Name)obj).compile(cfn, mv);
            else
            {
                scope.compiler.compileObject(scope, cfn, mv, obj);
            }
        }
        return true;
    }
}


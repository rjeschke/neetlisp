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

import java.util.concurrent.ConcurrentHashMap;

import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;
import neetlisp.compiler.CFn;
import neetlisp.compiler.Compilable;

public class Keyword implements Compilable
{
    private final static ConcurrentHashMap<String, Keyword> KEYWORDS = new ConcurrentHashMap<String, Keyword>();
    public final String value;
    private final int hashcode;
    
    private Keyword(final String value)
    {
        this.value = value;
        this.hashcode = value.hashCode();
    }
    
    protected static void flushKeywords()
    {
        KEYWORDS.clear();
    }
    
    public static Keyword create(final String value)
    {
        Keyword k = KEYWORDS.get(value);
        if(k == null)
        {
            k = new Keyword(value);
            KEYWORDS.put(value, k);
        }
        return k;
    }
    
    @Override
    public int hashCode()
    {
        return this.hashcode;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj == this;
    }
    
    @Override
    public String toString()
    {
        return ":" + this.value;
    }

    @Override
    public void compile(CFn cfn, MethodVisitor mv)
    {
        final String name;
        if(!cfn.hasConstant(this))
        {
            name = cfn.addConstant(this);
            final MethodVisitor cmv = cfn.getClinit();
            cmv.visitLdcInsn(this.value);
            cmv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/Keyword", "create", "(Ljava/lang/String;)Lneetlisp/Keyword;");
            cmv.visitFieldInsn(Opcodes.PUTSTATIC, cfn.getFullName(), name, "Ljava/lang/Object;");
        }
        else
        {
            name = cfn.addConstant(this);
        }
        cfn.visitLoadConst(mv, name);
    }
}

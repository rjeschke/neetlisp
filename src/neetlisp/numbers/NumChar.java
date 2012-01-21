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
package neetlisp.numbers;

import neetlisp.Number;
import neetlisp.Numbers;
import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;
import neetlisp.compiler.CFn;

public class NumChar implements Number
{
    public final char value;
    private final static NumChar[] CACHE = new NumChar[256];
    
    private NumChar(char value)
    {
        this.value = value;
    }
    
    public static Number create(char value)
    {
        if(value < 256)
            return CACHE[value];
        return new NumChar(value);
    }
    
    static
    {
        for(int i = 0; i < 256; i++)
            CACHE[i] = new NumChar((char)i);
    }
    
    @Override
    public Number add(Number a)
    {
        if(a instanceof NumChar)
            return NumChar.create((char)(this.value + ((NumChar)a).value));

        if(this.getSize() > a.getSize())
            return this.add(this.box(a));
        
        return a.box(this).add(a);
    }

    @Override
    public Number sub(Number a)
    {
        if(a instanceof NumChar)
            return NumChar.create((char)(this.value - ((NumChar)a).value));

        if(this.getSize() > a.getSize())
            return this.sub(this.box(a));
        
        return a.box(this).sub(a);
    }

    @Override
    public Number mul(Number a)
    {
        if(a instanceof NumChar)
            return NumChar.create((char)(this.value * ((NumChar)a).value));

        if(this.getSize() > a.getSize())
            return this.mul(this.box(a));
        
        return a.box(this).mul(a);
    }

    @Override
    public Number div(Number a)
    {
        if(a instanceof NumChar)
            return NumChar.create((char)(this.value / ((NumChar)a).value));

        if(this.getSize() > a.getSize())
            return this.div(this.box(a));
        
        return a.box(this).div(a);
    }

    @Override
    public String toString()
    {
        return String.format("\\%c", this.value);
    }

    @Override
    public Character javaBox()
    {
        return Character.valueOf(this.value);
    }

    public static Number toThis(Number n)
    {
        if(n instanceof NumChar)
            return n;
        
        if(n instanceof NumDouble)
            return NumChar.create((char)((NumDouble)n).value);

        if(n instanceof NumFloat)
            return NumChar.create((char)((NumFloat)n).value);

        if(n instanceof NumLong)
            return NumChar.create((char)((NumLong)n).value);
        
        if(n instanceof NumInt)
            return NumChar.create((char)((NumInt)n).value);
        
        return null;
    }

    @Override
    public Number box(Number n)
    {
        return toThis(n);
    }
    
    @Override
    public int getSize()
    {
        return Numbers.CHAR_SIZE;
    }

    @Override
    public Number negate()
    {
        return create((char)(-this.value));
    }

    @Override
    public int compareTo(Number other)
    {
        if(other instanceof NumChar)
        {
            final int oi = ((NumChar)other).value;
            return this.value < oi ? -1 : this.value > oi ? 1 : 0;
        }
        
        if(this.getSize() > other.getSize())
            return this.compareTo(this.box(other));
        
        return other.box(this).compareTo(other);
    }
    
    @Override
    public int hashCode()
    {
        return this.value;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof NumChar) ? ((NumChar)obj).value == this.value : false;
    }

    @Override
    public void compile(CFn cfn, MethodVisitor mv)
    {
        final String name;
        if(!cfn.hasConstant(this))
        {
            name = cfn.addConstant(this);
            final MethodVisitor cmv = cfn.getClinit();
            CFn.visitIntConst(cmv, this.value);
            cmv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/numbers/NumChar", "create", "(C)Lneetlisp/Number;");
            cmv.visitFieldInsn(Opcodes.PUTSTATIC, cfn.getFullName(), name, "Ljava/lang/Object;");
        }
        else
        {
            name = cfn.addConstant(this);
        }
        cfn.visitLoadConst(mv, name);
    }

    @Override
    public Number recip()
    {
        return NumChar.create((char)(1 / this.value));
    }
}

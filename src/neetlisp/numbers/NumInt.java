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

public class NumInt implements Number
{
    public final int value;
    private final static NumInt[] CACHE = new NumInt[128 + 256];
    
    private NumInt(int value)
    {
        this.value = value;
    }
    
    public static Number create(int value)
    {
        if(value > -129 && value < 256)
            return CACHE[value + 128];
        return new NumInt(value);
    }
    
    static
    {
        for(int i = -128; i < 256; i++)
            CACHE[i + 128] = new NumInt(i);
    }
    
    @Override
    public Number add(Number a)
    {
        if(a instanceof NumInt)
            return NumInt.create(this.value + ((NumInt)a).value);

        if(this.getSize() > a.getSize())
            return this.add(this.box(a));
        
        return a.box(this).add(a);
    }

    @Override
    public Number sub(Number a)
    {
        if(a instanceof NumInt)
            return NumInt.create(this.value - ((NumInt)a).value);

        if(this.getSize() > a.getSize())
            return this.sub(this.box(a));
        
        return a.box(this).sub(a);
    }

    @Override
    public Number mul(Number a)
    {
        if(a instanceof NumInt)
            return NumInt.create(this.value * ((NumInt)a).value);

        if(this.getSize() > a.getSize())
            return this.mul(this.box(a));
        
        return a.box(this).mul(a);
    }

    @Override
    public Number div(Number a)
    {
        if(a instanceof NumInt)
            return NumInt.create(this.value / ((NumInt)a).value);

        if(this.getSize() > a.getSize())
            return this.div(this.box(a));
        
        return a.box(this).div(a);
    }

    @Override
    public String toString()
    {
        return Integer.toString(this.value);
    }

    @Override
    public Integer javaBox()
    {
        return Integer.valueOf(this.value);
    }

    public static Number toThis(Number n)
    {
        if(n instanceof NumInt)
            return n;
        
        if(n instanceof NumDouble)
            return NumInt.create((int)((NumDouble)n).value);

        if(n instanceof NumFloat)
            return NumInt.create((int)((NumFloat)n).value);

        if(n instanceof NumLong)
            return NumInt.create((int)((NumLong)n).value);
        
        if(n instanceof NumChar)
            return NumInt.create(((NumChar)n).value);
        
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
        return Numbers.INT_SIZE;
    }

    @Override
    public Number negate()
    {
        return create(-this.value);
    }

    @Override
    public int compareTo(Number other)
    {
        if(other instanceof NumInt)
        {
            final int oi = ((NumInt)other).value;
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
        return (obj instanceof NumInt) ? ((NumInt)obj).value == this.value : false;
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
            cmv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/numbers/NumInt", "create", "(I)Lneetlisp/Number;");
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
        return NumInt.create(1 / this.value);
    }
}

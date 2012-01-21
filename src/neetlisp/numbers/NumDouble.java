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

public class NumDouble implements Number
{
    public final double value;
    
    public NumDouble(double value)
    {
        this.value = value;
    }
    
    public static Number create(double value)
    {
        return new NumDouble(value);
    }
    
    @Override
    public Number add(Number a)
    {
        if(a instanceof NumDouble)
            return new NumDouble(this.value + ((NumDouble)a).value);

        if(this.getSize() > a.getSize())
            return this.add(this.box(a));
        
        return a.box(this).add(a);
    }

    @Override
    public Number sub(Number a)
    {
        if(a instanceof NumDouble)
            return new NumDouble(this.value - ((NumDouble)a).value);

        if(this.getSize() > a.getSize())
            return this.sub(this.box(a));
        
        return a.box(this).sub(a);
    }

    @Override
    public Number mul(Number a)
    {
        if(a instanceof NumDouble)
            return new NumDouble(this.value * ((NumDouble)a).value);

        if(this.getSize() > a.getSize())
            return this.mul(this.box(a));
        
        return a.box(this).mul(a);
    }

    @Override
    public Number div(Number a)
    {
        if(a instanceof NumDouble)
            return new NumDouble(this.value / ((NumDouble)a).value);

        if(this.getSize() > a.getSize())
            return this.div(this.box(a));
        
        return a.box(this).div(a);
    }

    @Override
    public String toString()
    {
        return Double.toString(this.value);
    }

    @Override
    public Double javaBox()
    {
        return Double.valueOf(this.value);
    }

    public static Number toThis(Number n)
    {
        if(n instanceof NumDouble)
            return n;

        if(n instanceof NumInt)
            return new NumDouble(((NumInt)n).value);
        
        if(n instanceof NumFloat)
            return new NumDouble(((NumFloat)n).value);

        if(n instanceof NumLong)
            return new NumDouble(((NumLong)n).value);
        
        if(n instanceof NumChar)
            return new NumDouble(((NumChar)n).value);
        
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
        return Numbers.DOUBLE_SIZE;
    }

    @Override
    public Number negate()
    {
        return create(-this.value);
    }

    @Override
    public int compareTo(Number other)
    {
        if(other instanceof NumDouble)
            return Double.compare(this.value, ((NumDouble)other).value);
        
        if(this.getSize() > other.getSize())
            return this.compareTo(this.box(other));
        
        return other.box(this).compareTo(other);
    }
    
    @Override
    public int hashCode()
    {
        final long l = Double.doubleToRawLongBits(this.value);
        return (int)l + (int)(l >> 32L) * 31;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof NumDouble) ? ((NumDouble)obj).value == this.value : false;
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
            cmv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/numbers/NumDouble", "create", "(D)Lneetlisp/Number;");
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
        return NumDouble.create(1.0 / this.value);
    }
}

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

import neetlisp.numbers.NumChar;
import neetlisp.numbers.NumDouble;
import neetlisp.numbers.NumFloat;
import neetlisp.numbers.NumInt;
import neetlisp.numbers.NumLong;

public class Numbers
{
    public final static int CHAR_SIZE = 1;
    public final static int INT_SIZE = 2;
    public final static int LONG_SIZE = 3;
    public final static int FLOAT_SIZE = 4;
    public final static int DOUBLE_SIZE = 5;
    
    public static Number tryAuto(Object v)
    {
        if(Isa.number(v))
            return (Number)v;
        
        if(v instanceof Byte)
            return create((Byte)v);
        
        if(v instanceof Short)
            return create((Short)v);
        
        if(v instanceof Character)
            return create((Character)v);
        
        if(v instanceof Integer)
            return create((Integer)v);
        
        if(v instanceof Long)
            return create((Long)v);
        
        if(v instanceof Float)
            return create((Float)v);
        
        if(v instanceof Double)
            return create((Double)v);
        
        throw new IllegalArgumentException(v.getClass() + " can not be cast to Number.");
    }
    
    public static Number create(Integer v)
    {
        return NumInt.create(v);
    }

    public static Number create(int v)
    {
        return NumInt.create(v);
    }

    public static Number create(Character v)
    {
        return NumChar.create(v);
    }
    
    public static Number create(char v)
    {
        return NumChar.create(v);
    }
    
    public static Number create(Long v)
    {
        return NumLong.create(v);
    }
    
    public static Number create(long v)
    {
        return NumLong.create(v);
    }
    
    public static Number create(Double v)
    {
        return NumDouble.create(v);
    }

    public static Number create(double v)
    {
        return NumDouble.create(v);
    }

    public static Number create(Float v)
    {
        return NumFloat.create(v);
    }

    public static Number create(float v)
    {
        return NumFloat.create(v);
    }
}

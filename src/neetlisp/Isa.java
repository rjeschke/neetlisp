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

public class Isa
{
    public static boolean number(Object o)
    {
        return o instanceof Number;
    }

    public static boolean keyword(Object o)
    {
        return o instanceof Keyword;
    }
    
    public static boolean eof(Object o)
    {
        return o instanceof Parser.Eof;
    }
    
    public static boolean name(Object o)
    {
        return o instanceof Name;
    }
    
    public static boolean nstring(Object o)
    {
        return o instanceof NString;
    }
    
    public static boolean fseq(Object o)
    {
        return o instanceof NFiniteSeq;
    }

    public static boolean iseq(Object o)
    {
        return o instanceof NInfiniteSeq;
    }
    
    public static boolean seq(Object o)
    {
        return o instanceof NSeq;
    }
    
    public static boolean fn(Object o)
    {
        return o instanceof Fn;
    }

    public static boolean nil(Object o)
    {
        return o instanceof Nil;
    }
    
    public static boolean bool(Object o)
    {
        return o instanceof Boolean;
    }
}

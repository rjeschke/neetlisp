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
package neetlisp.fns.core;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Isa;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.NString;
import neetlisp.numbers.NumChar;
import neetlisp.seq.NArray;

public class FnStr extends Fn
{
    public FnStr(final Context context)
    {
        super(context, new FnMeta("core/str", 
                "(& xs)\n" +
                " Returns a string consisting of the concatenations of all xs, nil results\n" +
                " in an empty string", 
                0, true, 0));
    }
    
    public Object eval(Object seq)
    {
        StringBuilder sb = new StringBuilder();
        for(final NIterator it = ((NSeq)seq).getIterator(); it.hasNext();)
        {
            final Object o = it.next();
            if(!Isa.nil(o))
            {
                if(o instanceof NumChar)
                {
                    sb.append(((NumChar)o).value);
                }
                else
                    sb.append(o);
            }
        }
        return sb.length() > 0 ? new NString(sb.toString()) : NString.EMPTY;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        return this.eval(NArray.safeSeq(objects));
    }
}

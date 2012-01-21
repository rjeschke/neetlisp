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
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Number;
import neetlisp.Numbers;
import neetlisp.seq.NArray;

public class FnAdd extends Fn
{
    public FnAdd(final Context context)
    {
        super(context, new FnMeta("core/+", 
                "(& xs)\n" +
                " Returns the sum of all xs, (+) returns 0", 
                0, true, 0));
    }
    
    public Object eval(Object seq)
    {
        Number num = Numbers.create(0);
        for(final NIterator it = ((NSeq)seq).getIterator(); it.hasNext();)
            num = num.add((Number)it.next());
        return num;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        return this.eval(NArray.safeSeq(objects));
    }
}

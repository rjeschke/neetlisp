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
import neetlisp.seq.NArray;

public class FnNumGe extends Fn
{
    public FnNumGe(final Context context)
    {
        super(context, new FnMeta("core/>=", 
                "(num & nums)\n" +
                " Returns true if num & nums are in monotonically non-increasing order", 
                1, true, 0));
    }
    
    public Object eval(Object first, Object others)
    {
        boolean ok = true;
        Number num = (Number)first;
        for(final NIterator it = ((NSeq)others).getIterator(); it.hasNext();)
        {
            final Number next = (Number)it.next();
            if(num.compareTo(next) < 0)
            {
                ok = false;
                break;
            }
            num = next;
        }
        return ok;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], NArray.hardTailseq(objects, 1));
    }
}

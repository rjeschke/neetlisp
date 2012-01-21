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
import neetlisp.seq.NArray;

public class FnEquals extends Fn
{
    public FnEquals(final Context context)
    {
        super(context, new FnMeta("core/==", 
                "(x & xs)\n" +
                " Returns true if x is equal to all xs", 
                1, true, 0));
    }
    
    public Object eval(Object first, Object others)
    {
        for(final NIterator it = ((NSeq)others).getIterator(); it.hasNext();)
        {
            if(!first.equals(it.next()))
                return false;
        }
        return true;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], NArray.hardTailseq(objects, 1));
    }
}

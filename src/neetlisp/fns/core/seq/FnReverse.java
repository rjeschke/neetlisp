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
package neetlisp.fns.core.seq;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Util;
import neetlisp.seq.NArray;

public class FnReverse extends Fn
{
    public FnReverse(final Context context)
    {
        super(context, new FnMeta("core/reverse", "A doc string", 1, false, 0));
    }
    
    public Object eval(Object seq)
    {
        final NFiniteSeq s = Util.finiteSeq((NSeq)seq);
        final Object[] ret = new Object[s.size()];
        final NIterator it = s.getIterator();
        for(int i = ret.length - 1; i >= 0; i--)
            ret[i] = it.next();
        return new NArray(ret);
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0]);
    }
}

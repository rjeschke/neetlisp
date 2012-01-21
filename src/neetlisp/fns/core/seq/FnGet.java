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
import neetlisp.Isa;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Nil;
import neetlisp.Number;
import neetlisp.numbers.NumInt;
import neetlisp.seq.NArray;

public class FnGet extends Fn
{
    public FnGet(final Context context)
    {
        super(context, new FnMeta("core/get", "A doc string", 2, true, 0));
    }
    
    public Object eval(Object seq, Object index, Object other)
    {
        NFiniteSeq nfs = (NFiniteSeq)other;
        Object notFound = nfs.isEmpty() ? Nil.NIL : nfs.car();
        if(Isa.fseq(seq))
        {
            final int i = ((NumInt)(NumInt.toThis((Number)index))).value;
            NFiniteSeq s = (NFiniteSeq)seq;
            return i < s.size() ? s.get(i) : notFound;
        }
        if(Isa.seq(seq))
        {
            final int n = ((NumInt)(NumInt.toThis((Number)index))).value;
            NIterator it = ((NSeq)seq).getIterator();
            Object ret = notFound;
            for(int i = 0; i < n; i++)
            {
                if(!it.hasNext())
                    break;
                ret = it.next();
            }
            return ret;
        }
        return Nil.NIL;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1], NArray.hardTailseq(objects, 2));
    }
}

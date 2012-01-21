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
import neetlisp.Nil;
import neetlisp.Util;
import neetlisp.seq.NArray;

public class FnReduce extends Fn
{
    public FnReduce(final Context context)
    {
        super(context, new FnMeta("core/reduce", "A doc string", 2, true, 0));
    }
    
    public Object eval(Object f, Object s, Object s2)
    {
        final Fn fn = (Fn)f;
        if(!Util.assureArgc(fn, 2))
            throw new IllegalArgumentException("Incompatible function for reduce: " + fn);
        
        final NFiniteSeq args = (NFiniteSeq)s2;
        
        if(args.size() > 1)
            throw new IllegalArgumentException("Too many arguments to: fn reduce");
        
        Object val;
        NFiniteSeq seq;
        NIterator it;
        
        if(args.size() == 0)
        {
            val = Nil.NIL;
            seq = (NFiniteSeq)s;
            it = seq.getIterator();
            if(!it.hasNext())
                return val;
            val = it.next();
        }
        else
        {
            val = s;
            seq = (NFiniteSeq)(args.getIterator().next());
            it = seq.getIterator();
        }
        
        while(it.hasNext())
            val = fn.invoke(val, it.next());
        
        return val;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1], NArray.hardTailseq(objects, 2));
    }
}

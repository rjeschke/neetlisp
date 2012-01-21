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
import neetlisp.Number;
import neetlisp.numbers.NumInt;
import neetlisp.seq.NAbstractLazySeq;
import neetlisp.seq.NArray;

public class FnRange extends Fn
{
    public FnRange(final Context context)
    {
        super(context, new FnMeta("core/range", "A doc string", 0, true, 0));
    }
    
    public Object eval(Object args)
    {
        final NFiniteSeq seq = (NFiniteSeq)args;
        if(seq.size() > 3)
            throw new IllegalArgumentException("Too many arguments to 'core/range");
        
        Number minimum = NumInt.create(0);
        Number maximum = null;
        Number increment = NumInt.create(1);
        
        switch(seq.size())
        {
        case 1:
            maximum = (Number)seq.get(0);
            break;
        case 2:
            minimum = (Number)seq.get(0);
            maximum = (Number)seq.get(1);
            break;
        case 3:
            minimum = (Number)seq.get(0);
            maximum = (Number)seq.get(1);
            increment = (Number)seq.get(2);
            break;
        }
        
        return new LzRange(new LzRangeState(minimum, maximum, increment));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        return this.eval(NArray.safeSeq(objects));
    }
    
    private static class LzRangeState
    {
        Number current;
        final Number maximum;
        final Number increment;

        public LzRangeState(Number minimum, Number maximum, Number increment)
        {
            this.current = minimum;
            this.maximum = maximum;
            this.increment = increment;
        }
    }
    
    private static class LzRange extends NAbstractLazySeq
    {
        public LzRange(LzRangeState state)
        {
            super(state);
        }
        
        @Override
        protected Object next(Object state)
        {
            LzRangeState s = (LzRangeState)state;
            if(s.maximum != null && s.current.compareTo(s.maximum) >= 0)
                return null;
            Number ret = s.current;
            s.current = s.current.add(s.increment);
            return ret;
        }
    }
}

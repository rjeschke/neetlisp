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
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Number;
import neetlisp.numbers.NumInt;
import neetlisp.seq.NAbstractLazySeq;

public class FnTake extends Fn
{
    public FnTake(final Context context)
    {
        super(context, new FnMeta("core/take", "A doc string", 2, false, 0));
    }
    
    public Object eval(Object n, Object s)
    {
        return new LzTake(new LzTakeState(((NSeq)s).getIterator(), ((NumInt)NumInt.toThis((Number)n)).value));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1]);
    }
    
    private final static class LzTakeState
    {
        final int length;
        int done;
        final NIterator it;
        
        public LzTakeState(final NIterator it, final int length)
        {
            this.it = it;
            this.done = 0;
            this.length = length;
        }
    }
    
    private final static class LzTake extends NAbstractLazySeq
    {
        public LzTake(Object state)
        {
            super(state);
        }
        
        @Override
        protected Object next(Object state)
        {
            final LzTakeState s = (LzTakeState)state;
            
            if(s.done >= s.length || !s.it.hasNext())
                return null;
            s.done++;
            return s.it.next();
        }
        
    }
}

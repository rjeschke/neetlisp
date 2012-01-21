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
import neetlisp.seq.NAbstractLazySeq;

public class FnIterate extends Fn
{
    public FnIterate(final Context context)
    {
        super(context, new FnMeta("core/iterate", "A doc string", 2, false, 0));
    }
    
    public Object eval(Object f, Object a)
    {
        return new LzIterate(new IterateState((Fn)f, a));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1]);
    }
    
    private static class IterateState
    {
        final Fn fn;
        Object value;

        public IterateState(Fn fn, Object value)
        {
            this.fn = fn;
            this.value = value;
        }
    }
    
    private static class LzIterate extends NAbstractLazySeq
    {
        public LzIterate(IterateState state)
        {
            super(state);
        }
        
        @Override
        protected Object next(Object state)
        {
            IterateState s = (IterateState)state;
            final Object ret = s.value;
            s.value = s.fn.invoke(s.value);
            return ret;
        }
    }
}

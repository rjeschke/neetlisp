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
import neetlisp.Util;
import neetlisp.seq.NAbstractLazySeq;

public class FnMap extends Fn
{
    public FnMap(final Context context)
    {
        super(context, new FnMeta("core/map", "A doc string", 2, false, 0));
    }
    
    public Object eval(Object f, Object s)
    {
        final Fn fn = (Fn)f;
        if(!Util.assureArgc(fn, 1))
            throw new IllegalArgumentException("Incompatible function for map: " + fn);

        return new LzMap(new MapState(fn, ((NSeq)s).getIterator()));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1]);
    }
    
    private final static class MapState
    {
        final Fn fn;
        final NIterator it;
        
        public MapState(final Fn fn, final NIterator it)
        {
            this.fn = fn;
            this.it = it;
        }
    }
    
    private final static class LzMap extends NAbstractLazySeq
    {
        public LzMap(Object state)
        {
            super(state);
        }

        @Override
        protected Object next(Object state)
        {
            final MapState s = (MapState)state;
            if(s.it.hasNext())
                return s.fn.invoke(s.it.next());
            return null;
        }
    }
}

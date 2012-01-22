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

import java.util.concurrent.atomic.AtomicLong;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Name;
import neetlisp.RndLCG;

public class FnGensym extends Fn
{
    public FnGensym(final Context context)
    {
        super(context, new FnMeta("core/gensym", 
                "()\n" +
                " Returns a unique name", 
                0, false, 0));
    }

    private final static AtomicLong counter = new AtomicLong();
    private final static RndLCG rnd = new RndLCG();

    public synchronized Object eval()
    {
        return new Name(null, String.format("G=%d=%08X=S", counter.incrementAndGet(), rnd.getInt()));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval();
    }
}

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

import java.util.ArrayList;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.seq.NList;

public class FnTakeAll extends Fn
{
    public FnTakeAll(final Context context)
    {
        super(context, new FnMeta("core/take-all", "A doc string", 1, false, 0));
    }
    
    public Object eval(Object s)
    {
        final NSeq seq = (NSeq)s;
        final ArrayList<Object> ret = new ArrayList<Object>();
        
        for(final NIterator it = seq.getIterator(); it.hasNext(); )
            ret.add(it.next());

        return new NList(ret);
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0]);
    }
}

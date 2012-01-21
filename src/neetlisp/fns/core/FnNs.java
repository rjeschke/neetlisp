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
import neetlisp.Name;
import neetlisp.Nil;

public class FnNs extends Fn
{
    public FnNs(final Context context)
    {
        super(context, new FnMeta("core/ns", 
                "(name)\n" +
                " Change current namespace to name", 
                1, false, 0));
    }
    
    public Object eval(Object n)
    {
        final Name name = (Name)n;
        if(name.hasNs())
            throw new IllegalArgumentException("Can not set a namespace to a namespace");
        this.context.setNamespace(name.getName());
        return Nil.NIL;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0]);
    }
}

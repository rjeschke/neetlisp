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
import neetlisp.Isa;
import neetlisp.Name;
import neetlisp.Nil;
import neetlisp.Util;

public class FnDoc extends Fn
{
    public FnDoc(final Context context)
    {
        super(context, new FnMeta("core/doc", 
                "(name)\n" +
                " Displays the doc-string for 'name", 
                1, false, 0));
    }
    
    public Object eval(Object n)
    {
        final Name name = (Name)n;
        final Object obj = this.context.resolve(name);
        
        if(Isa.nil(obj) || !Isa.fn(obj))
            System.out.println("doc: Can not find any fn like " + name);
        else
        {
            final Fn fn = (Fn)obj;
            System.out.println("doc: " + fn);
            System.out.println("---");
            System.out.println(Util.safeString(fn.getMetaData().doc));
        }

        return Nil.NIL;
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0]);
    }
}

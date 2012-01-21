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

public class FnDef extends Fn
{
    public FnDef(final Context context)
    {
        super(context, new FnMeta("core/def", 
                "(name val)\n" +
                " Evals val and assigns it to name", 
                2, false, FnMeta.FLAG_SPECIAL_FORM));
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        throw new IllegalStateException("Can not invoke special form function " + this.meta.name);
    }
}

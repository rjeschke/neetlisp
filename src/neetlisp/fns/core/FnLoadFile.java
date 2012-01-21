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

import java.io.IOException;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Isa;
import neetlisp.Name;
import neetlisp.Tokenizer;

public class FnLoadFile extends Fn
{
    public FnLoadFile(final Context context)
    {
        super(context, new FnMeta("core/load-file", 
                "(path)\n" +
                " Loads and evaluates a file, path may be a name or a string", 
                1, false, 0));
    }
    
    public Object eval(Object o)
    {
        final String fname;
        if(Isa.name(o))
        {
            final Name n = (Name)o;
            if(n.hasNs())
                throw new IllegalArgumentException("Name for load-file must not contain a namespace");
            fname = ((Name)o).getName().replace('.', '/');
        }
        else
            fname = o.toString();
        final String ns = this.context.getNamespace();
        try
        {
            return this.context.evalParse(new Tokenizer(this.context.loadSource(fname)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            this.context.setNamespace(ns);
        }
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0]);
    }
}

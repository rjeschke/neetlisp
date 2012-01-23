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
package neetlisp.fns.reader;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Nil;
import neetlisp.Number;
import neetlisp.numbers.NumChar;

public class FnReaderMacro extends Fn
{
    public FnReaderMacro(Context context)
    {
        super(context, new FnMeta("core.read/reader-macro", 
                "(reader-macro character func) -> define a new reader-macro for\n" +
                "  character 'character. 'func is a fn taking two arguments:\n" +
                "  -> fn (parser next-character). reader-macros must exit using\n" +
                "  one of the 'rm-resp-* response fns. next-character is an int", 2, false, 0));
    }

    public Object eval(Object c, Object fn)
    {
        final NumChar ch = (NumChar)(NumChar.toThis((Number)c));
        final Fn f = (Fn)fn;
        this.context.addReaderMacro(ch.value, f);
        return Nil.NIL;
    }
    
    @Override
    public Object invoke(Object... objects)
    {
        this.assureArguments(objects.length);
        return this.eval(objects[0], objects[1]);
    }
}

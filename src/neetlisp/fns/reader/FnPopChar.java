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

import java.util.Stack;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.FnMeta;
import neetlisp.Util;
import neetlisp.numbers.NumInt;

public class FnPopChar extends Fn
{
    public FnPopChar(Context context)
    {
        super(context, new FnMeta("core.read/pop-char", 
                "...", 
                0, false, 0));
    }

    public Object eval()
    {
        final Stack<Integer> stack = Util.getCharStack();
        return NumInt.create(stack.isEmpty() ? -1 : stack.pop());
    }
    
    @Override
    public Object invoke(Object ... objects)
    {
        this.assureArguments(objects.length);
        return this.eval();
    }
}

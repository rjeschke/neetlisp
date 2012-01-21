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
package neetlisp;

public abstract class Fn
{
    protected final Context context;
    protected final FnMeta meta;
    ClassLoader loader;
    
    public Fn(Context context, FnMeta meta)
    {
        this.context = context;
        this.meta = meta;
    }
    
    public FnMeta getMetaData()
    {
        return this.meta;
    }
    
    public boolean isMacro()
    {
        return this.meta != null && (this.meta.flags & FnMeta.FLAG_MACRO) != 0;
    }
    
    public boolean isSpecialForm()
    {
        return this.meta != null && (this.meta.flags & FnMeta.FLAG_SPECIAL_FORM) != 0;
    }
    
    public Object resolve(Name name)
    {
        return this.context.safeResolve(name);
    }
    
    public void add(Name name, Object value)
    {
        this.context.addBinding(name, value);
    }
    
    public void assureArguments(int argc)
    {
        if(!Util.assureArgc(this, argc))
            throw new IllegalArgumentException("Incorrect argument count for " + this);
    }
    
    @Override
    public String toString()
    {
        return this.meta != null ? this.meta.toString() : "fn()";
    }
    
    public abstract Object invoke(Object ... objects);
}

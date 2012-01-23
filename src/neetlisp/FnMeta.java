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

public class FnMeta implements Comparable<FnMeta>
{
    public final static int FLAG_SPECIAL_FORM = 1;
    public final static int FLAG_MACRO = 2;

    public final Name name;
    public final String doc;
    public final int arguments;
    public final boolean varargs;
    public final int flags;
    
    public FnMeta(final String name, final String doc, final int arguments, final boolean varargs, int flags)
    {
        this.name = Name.fromString(name);
        this.doc = doc == null ? "nil" : doc;
        this.arguments = arguments;
        this.varargs = varargs;
        this.flags = flags;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        if((this.flags & FLAG_MACRO) != 0)
            sb.append("macro");
        else
            sb.append("fn");
        if(this.name != null)
        {
            sb.append(' ');
            sb.append(this.name);
        }
        sb.append(" (");
        if(this.arguments > 0)
            sb.append(this.arguments);
        if(this.varargs)
        {
            if(this.arguments > 0)
                sb.append(' ');
            sb.append('&');
        }
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof FnMeta)
        {
            final FnMeta f = (FnMeta)obj;
            return this.name.equals(f.name) 
                && this.arguments == f.arguments 
                && this.varargs == f.varargs
                && this.flags == f.flags
                && this.doc.equals(f.doc);
        }
        return false;
    }

    @Override
    public int compareTo(FnMeta o)
    {
        if(this.name.getNamespace().length() == o.name.getNamespace().length())
            return this.name.getFullName().compareTo(o.name.getFullName());
        return this.name.getNamespace().length() - o.name.getNamespace().length();
    }
}

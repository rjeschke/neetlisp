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

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import neetlisp.seq.NArray;
import neetlisp.seq.NList;

public class Util
{
    private final static AtomicLong uniquify = new AtomicLong(0);
    final static Name fnDo = new Name("core", "do");
    
    public static NString wrapString(Object obj)
    {
        if(Isa.nstring(obj))
            return (NString)obj;
        
        return new NString(obj.toString());
    }
    
    public static long getUniqueID()
    {
        return uniquify.getAndIncrement();
    }
    
    public static boolean booleanValue(Object obj)
    {
        return !(obj == null || Isa.nil(obj) || (obj.equals(Boolean.FALSE)));
    }

    public static boolean assureArgc(Fn fn, int argc)
    {
        final FnMeta meta = fn.getMetaData();
        return (meta.arguments == argc) || (meta.arguments < argc && meta.varargs);
    }
    
    public static String seqToString(NIterator it)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        while(it.hasNext())
        {
            if(sb.length() > 1)
                sb.append(' ');
            sb.append(it.next());
        }
        sb.append(')');
        return sb.toString();
    }
    
    public static Object wrapDo(final Object first, final NFiniteSeq rest)
    {
        if(first != null && !rest.isEmpty())
        {
            final Object[] sexp = new Object[2 + rest.size()];
            sexp[0] = fnDo;
            sexp[1] = first;
            final NIterator it = rest.getIterator();
            int i = 2;
            while(it.hasNext())
                sexp[i++] = it.next();
            return new NArray(sexp);
        }
        return first;
    }
    
    public static Object denull(Object value)
    {
        return value == null ? Nil.NIL : value;
    }
    
    public static NString safeString(Object value)
    {
        if(value == null || Isa.nil(value))
            return NString.EMPTY;
        if(value instanceof NString)
            return (NString)value;
        return new NString(value.toString());
    }
    
    public static Fn injectClassLoader(final Fn fn, final ClassLoader l)
    {
        fn.loader = l;
        return fn;
    }
    
    public static NFiniteSeq finiteSeq(NSeq s)
    {
        if(Isa.fseq(s))
            return (NFiniteSeq)s;
        final ArrayList<Object> ret = new ArrayList<Object>();
        for(NIterator it = s.getIterator(); it.hasNext(); )
            ret.add(it.next());
        return new NList(ret);
    }
    
    public static void appendToList(final ArrayList<Object> list, NSeq seq)
    {
        final NIterator it = seq.getIterator();
        while(it.hasNext())
            list.add(it.next());
    }
    
    public static File resolveHome(final String string)
    {
        if(string.startsWith("~"))
            return new File(System.getProperty("user.home"), string.substring(1));
        return new File(string);
    }
}

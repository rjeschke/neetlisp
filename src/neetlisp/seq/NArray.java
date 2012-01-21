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
package neetlisp.seq;

import java.util.Arrays;

import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Nil;
import neetlisp.Util;

public class NArray implements NFiniteSeq
{
    public final static NArray EMPTY = new NArray();
    
    private final Object[] list;
    private final int startIndex;
    private final int size;
    
    private NArray()
    {
        this(new Object[0], 0, 0);
    }
    
    public NArray(Object[] list)
    {
        this(list, 0, list.length);
    }
    
    public NArray(Object[] list, final int start)
    {
        this(list, start, list.length - start);
    }
    
    public NArray(Object[] list, final int start, final int size)
    {
        this.list = list;
        this.startIndex = Math.min(start, list.length);
        this.size = Math.max(0, size);
    }

    public static NArray wrap(Object[] values)
    {
        return values.length > 0 ? new NArray(values, 0, values.length) : EMPTY;
    }
    
    public static NArray safeSeq(Object[] list)
    {
        return list.length > 0 ? new NArray(Arrays.copyOf(list, list.length)) : EMPTY;
    }
    
    public static NArray hardTailseq(Object[] list, int start)
    {
        if(start >= list.length)
            return EMPTY;
        
        if(start == 0)
            return new NArray(Arrays.copyOf(list, list.length));
        
        final int len = list.length - start;
        final Object[] l = new Object[len];
        System.arraycopy(list, start, l, 0, len);
        return new NArray(l);
    }
    
    @Override
    public Object car()
    {
        return this.isEmpty() ? Nil.NIL : this.list[this.startIndex];
    }

    @Override
    public Object cdr()
    {
        return this.size < 1 ? EMPTY : this.tailseq(1);
    }

    @Override
    public NIterator getIterator()
    {
        return new Iterator(this);
    }

    @Override
    public int size()
    {
        return this.size;
    }

    @Override
    public boolean isEmpty()
    {
        return this.size == 0;
    }

    @Override
    public Object get(int index)
    {
        return index < 0 || index >= this.size() ? Nil.NIL : this.list[this.startIndex + index];
    }

    @Override
    public NSeq headseq(int length)
    {
        return new NArray(this.list, this.startIndex, Math.min(this.size, length));
    }

    @Override
    public NSeq tailseq(int start)
    {
        return new NArray(this.list, this.startIndex + start, this.size - start);
    }
    
    @Override
    public String toString()
    {
        return Util.seqToString(this.getIterator());
    }
    
    private static class Iterator implements NIterator
    {
        private int position = 0;
        private final NArray list;
        
        public Iterator(NArray list)
        {
            this.list = list;
        }
        
        @Override
        public boolean hasNext()
        {
            return this.position < this.list.size();
        }

        @Override
        public Object next()
        {
            return this.position < this.list.size() ? this.list.get(this.position++) : Nil.NIL;
        }
    }
}

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

import java.util.ArrayList;

import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Nil;
import neetlisp.Util;

public class NList implements NFiniteSeq
{
    public final static NList EMPTY = new NList();
    
    private final ArrayList<Object> list;
    private final int startIndex;
    private final int size;
    
    private NList()
    {
        this(new ArrayList<Object>(), 0, 0);
    }
    
    public NList(ArrayList<Object> list)
    {
        this(list, 0, list.size());
    }
    
    public NList(ArrayList<Object> list, final int start)
    {
        this(list, start, list.size() - start);
    }
    
    public NList(ArrayList<Object> list, final int start, final int size)
    {
        this.list = list;
        this.startIndex = Math.min(start, list.size());
        this.size = Math.max(0, size);
    }
    
    public static NList wrap(ArrayList<Object> list)
    {
        return list.isEmpty() ? EMPTY : new NList(list, 0, list.size());
    }
    
    @Override
    public Object car()
    {
        return this.isEmpty() ? Nil.NIL : this.list.get(this.startIndex);
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
        return index < 0 || index >= this.size() ? Nil.NIL : this.list.get(this.startIndex + index);
    }

    @Override
    public NSeq headseq(int length)
    {
        return new NList(this.list, this.startIndex, Math.min(this.size, length));
    }

    @Override
    public NSeq tailseq(int start)
    {
        return new NList(this.list, this.startIndex + start, this.size - start);
    }

    @Override
    public String toString()
    {
        return Util.seqToString(this.getIterator());
    }
    
    private static class Iterator implements NIterator
    {
        private int position = 0;
        private final NList list;
        
        public Iterator(NList list)
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

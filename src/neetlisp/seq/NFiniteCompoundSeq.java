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

import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Nil;
import neetlisp.Util;

public class NFiniteCompoundSeq implements NFiniteSeq
{
    final Object first;
    final NFiniteSeq rest;
    
    public NFiniteCompoundSeq(final Object first, final NFiniteSeq rest)
    {
        this.first = first;
        this.rest = rest;
    }
    
    @Override
    public Object car()
    {
        return this.first;
    }

    @Override
    public Object cdr()
    {
        return this.rest;
    }

    @Override
    public int size()
    {
        return this.rest.size() + 1;
    }

    @Override
    public NIterator getIterator()
    {
        return new Iterator(this.first, this.rest);
    }

    @Override
    public String toString()
    {
        return Util.seqToString(this.getIterator());
    }
    
    @Override
    public boolean isEmpty()
    {
        return this.first == null;
    }
    
    private static class Iterator implements NIterator
    {
        private boolean start = true; 
        private final Object first;
        private final NIterator rest;
        
        public Iterator(Object first, NFiniteSeq seq)
        {
            this.first = first;
            this.rest = seq.getIterator();
        }
        
        @Override
        public boolean hasNext()
        {
            return this.start || this.rest.hasNext();
        }

        @Override
        public Object next()
        {
            if(this.start)
            {
                this.start = false;
                return this.first;
            }
            return this.rest.next();
        }
    }

    @Override
    public Object get(int index)
    {
        return Nil.NIL;
    }

    @Override
    public NSeq headseq(int length)
    {
        if(length >= this.size())
            return this;
        final Object[] objs = new Object[Math.min(length, this.size())];
        final NIterator it = this.getIterator();
        for(int i = 0; i < objs.length; i++)
            objs[i] = it.next();
        return new NArray(objs);
    }

    @Override
    public NSeq tailseq(int start)
    {
        if(start <= 0)
            return this;
        final Object[] objs = new Object[Math.max(0, this.size() - start)];
        final NIterator it = this.getIterator();
        for(int n = 0; n < start; n++)
            it.next();
        for(int i = 0; i < objs.length; i++)
            objs[i] = it.next();
        return new NArray(objs);
    }
}

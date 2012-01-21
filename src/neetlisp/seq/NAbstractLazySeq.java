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

import java.util.List;
import java.util.Vector;

import neetlisp.NInfiniteSeq;
import neetlisp.NIterator;
import neetlisp.NSeq;
import neetlisp.Nil;
import neetlisp.Util;

public abstract class NAbstractLazySeq implements NInfiniteSeq
{
    Vector<Object> backend = new Vector<Object>();
    final Object state;
    
    public NAbstractLazySeq(Object state)
    {
        this.state = state;
    }
    
    synchronized void assureSize(int sz)
    {
        final List<Object> list = this.backend;
        final Object state = this.state;
        while(list.size() < sz)
        {
            final Object obj = this.next(state);
            if(obj == null)
                break;
            list.add(obj);
        }
    }
    
    @Override
    public Object car()
    {
        this.assureSize(1);
        final List<Object> list = this.backend;
        return list.size() > 0 ? list.get(0) : Nil.NIL;
    }

    @Override
    public Object cdr()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NSeq headseq(int length)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NSeq tailseq(int start)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NIterator getIterator()
    {
        return new DefaultIterator(this);
    }

    @Override
    public Object get(int index)
    {
        this.assureSize(index + 1);
        final List<Object> list = this.backend;
        return index < list.size() ? list.get(index) : Nil.NIL;
    }

    protected abstract Object next(Object state);
    
    public static class DefaultIterator implements NIterator
    {
        int pos = 0;
        private final NAbstractLazySeq seq;
        
        public DefaultIterator(NAbstractLazySeq seq)
        {
            this.seq = seq;
        }
        
        @Override
        public boolean hasNext()
        {
            this.seq.assureSize(this.pos + 1);
            return this.pos < this.seq.backend.size();
        }

        @Override
        public Object next()
        {
            final List<Object> list = this.seq.backend;
            this.seq.assureSize(this.pos + 1);
            return this.pos < list.size() ? list.get(this.pos++) : Nil.NIL;
        }
    }
    
    @Override
    public String toString()
    {
        return Util.seqToString(this.getIterator());
    }
}

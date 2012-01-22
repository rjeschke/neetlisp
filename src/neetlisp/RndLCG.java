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

public class RndLCG
{
    private final static long A = 6364136223846793005L;
    private final static long C = 1442695040888963407L;
    private long value;
    
    public RndLCG(final long seed)
    {
        this.value = seed;
    }
    
    public RndLCG()
    {
        this(System.nanoTime());
    }
    
    public synchronized int getInt()
    {
        this.value = this.value * A + C;
        return (int)(this.value >> 32);
    }
    
    public float getFloatUnipolar()
    {
        return (this.getInt() / 4294967296.f) + 0.5f;
    }

    public float getFloatBipolar()
    {
        return this.getInt() / 2147483648.f;
    }
    
    public double getDoubleUnipolar()
    {
        return (this.getInt() / 4294967296.0) + 0.5;
    }
    
    public double getDoubleBipolar()
    {
        return this.getInt() / 2147483648.0;
    }
}
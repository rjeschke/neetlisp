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

public class ReaderMacroResponse
{
    public final Type type;
    public final Object object;
    
    private ReaderMacroResponse(Type type, Object object)
    {
        this.type = type;
        this.object = object;
    }
    
    public static ReaderMacroResponse none()
    {
        return new ReaderMacroResponse(Type.NONE, null);
    }
    
    public static ReaderMacroResponse object(final Object value)
    {
        return new ReaderMacroResponse(Type.OBJECT, value);
    }
    
    @Override
    public String toString()
    {
        return "rm-resp: " + this.type + ":" + Util.denull(this.object);
    }
    
    public enum Type
    {
        NONE,
        OBJECT
    }
}

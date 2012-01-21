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
package neetlisp.compiler;

import java.util.ArrayList;
import java.util.HashMap;

import neetlisp.Name;
import neetlisp.NlClassLoader;

public class Scope
{
    private Scope parent;
    private Scope child;
    final Compiler compiler;
    final NlClassLoader tempClassLoader;
    public Name lastName = null;
    private final HashMap<Name, Integer> locals = new HashMap<Name, Integer>();
    private final HashMap<Name, Integer> closures = new HashMap<Name, Integer>();
    final ArrayList<Name> closureNames = new ArrayList<Name>();
    CFn scfn;
    boolean isFn = false;
    
    public Scope(Compiler compiler)
    {
        this.tempClassLoader = new NlClassLoader(compiler.context.getClassLoader());
        this.compiler = compiler;
    }
    
    public Scope(Scope p)
    {
        this.compiler = p.compiler;
        this.tempClassLoader = p.tempClassLoader;
        p.child = this;
        this.parent = p;
    }

    public static Scope forLet(Scope p)
    {
        Scope s = new Scope(p);
        s.locals.putAll(p.locals);
        s.closures.putAll(p.closures);
        s.closureNames.addAll(p.closureNames);
        return s;
    }
    
    public boolean hasLocal(final Name name)
    {
        return this.locals.containsKey(name);
    }
    
    public boolean hasClosureVariables()
    {
        return !this.closures.isEmpty();
    }
    
    public boolean hasClosure(final Name name)
    {
        return this.closures.containsKey(name);
    }
    
    public boolean uppersHaveLocal(final Name name)
    {
        Scope p = this.parent;
        while(p != null)
        {
            if(p.hasLocal(name) || p.hasClosure(name))
                return true;
            p = p.parent;
        }
        return false;
    }
    
    public int addClosureVariable(CFn cfn, final Name name)
    {
        return this.addClosureVariable(cfn, name, true);
    }
    
    private int addClosureVariable(CFn cfn, final Name name, boolean recur)
    {
        int idx;
        if(!this.closures.containsKey(name) && cfn != null)
        {
            idx = this.closureNames.size();
            this.closureNames.add(name);
            this.closures.put(name, idx);
            if(this.isFn)
                cfn.addClosureField(idx);
        }
        else
            idx = this.closures.get(name);
        if(recur)
        {
            Scope p = this.parent;
            while(p != null)
            {
                if(p.hasLocal(name) || p.hasClosure(name))
                    break;
                p.addClosureVariable(p.scfn, name, false);
                p = p.parent;
            }
        }
        return idx;
    }
    
    public int addLocal(final Name name)
    {
        final Integer i = this.locals.get(name);
        if(i == null)
        {
            int ret = this.locals.size() + 1;
            this.locals.put(name, ret);
            return ret;
        }
        return i;
    }

    public int getClosure(final Name name)
    {
        return this.closures.get(name);
    }
    
    public Name resolveToName(Name name)
    {
        if(this.hasLocal(name) || this.hasClosure(name))
            return name;
        return this.compiler.context.resolveName(name);
    }
    
    public void remove()
    {
        final Scope p = this.parent;
        if(this.parent != null)
            this.parent = this.child;
        if(this.child != null)
            this.child = p;
    }
    
    public Scope copy()
    {
        return new Scope(this);
    }
    
    public NlClassLoader getClassLoader()
    {
        return this.tempClassLoader;
    }
}

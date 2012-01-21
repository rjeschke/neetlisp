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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import neetlisp.compiler.Compiler;

public class Context
{
    final static String[] AUTOLOADS = {"reader_macros", "core_macros", "core_redefs", "core"}; 
    final HashMap<String, Namespace> nss;
    final HashMap<Keyword, ArrayList<String>> load_paths = new HashMap<Keyword, ArrayList<String>>();
    final Fn[] readCharacterHandler;
    String namespace = "user";
    Parser parser; 
    private final NlClassLoader classLoader;
    private final Compiler compiler;
    
    public Context()
    {
        long t0 = System.nanoTime();
        this.compiler = new Compiler(this);
        this.classLoader = new NlClassLoader(Context.class.getClassLoader());
        this.nss = new HashMap<String, Namespace>();
        this.readCharacterHandler = new Fn[65536];
        this.importFns();

        try
        {
            for(final String s : AUTOLOADS)
            {
                final String ns = this.namespace;
                this.evalParse(new Tokenizer(this.loadSource("/neetlisp/nlsp/" + s)));
                this.namespace = ns;
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        
        this.runTests();
        
        t0 = System.nanoTime() - t0;
        System.err.println("Startup took " + (float)(t0 * 1e-6) + "ms");
        
        /*
        List<FnMeta> metas = this.getAllFnMetas();
        for(final FnMeta fn : metas)
        {
            System.out.println(fn.name.getFullName());
            System.out.println("---");
            System.out.println(fn);
            System.out.println(fn.doc);
            System.out.println();
        }
        */
    }
    
    public void runTests()
    {
        final String[] checks = {"sanity_checks"};
        try
        {
            for(final String s : checks)
            {
                final String ns = this.namespace;
                this.evalParse(new Tokenizer(this.loadSource("/neetlisp/nlsp/tests/" + s)));
                this.namespace = ns;
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public NlClassLoader getClassLoader()
    {
        return this.classLoader;
    }
    
    public Compiler getCompiler()
    {
        return this.compiler;
    }
    
    public void addReaderMacro(char ch, Fn fn)
    {
        this.readCharacterHandler[ch] = fn;
    }
    
    private void importFns()
    {
        try
        {
            final List<Class<?>> classes = ClassFinder.find(ClassLoader.getSystemClassLoader(), "neetlisp.fns");
            for(final Class<?> c : classes)
            {
                if(c.getSuperclass() == Fn.class)
                {
                    final Fn fn = (Fn)c.getConstructors()[0].newInstance(this);
                    final FnMeta meta = fn.getMetaData();
                    this.addBinding(meta.name, fn);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    Fn getReadCharacterHandler(char ch)
    {
        return this.readCharacterHandler[ch];
    }
    
    public void addSearchPath(final Keyword key, final String path)
    {
        ArrayList<String> paths = this.load_paths.get(key);
        if(paths == null)
        {
            paths = new ArrayList<String>();
            this.load_paths.put(key, paths);
        }
        if(!paths.contains(path))
            paths.add(path);
    }
    
    public Object evalParse(Tokenizer tok) throws IOException
    {
        this.parser = new Parser(this, tok);
        Object value = null, expr = null;
        while(!Isa.eof(expr))
        {
            expr = this.parser.parse();
            if(Isa.eof(expr))
                break;
            value = this.compiler.compile(expr).invoke();
        }
        
        tok.close();
        return value;
    }
    
    public String getNamespace()
    {
        return this.namespace;
    }
    
    public void setNamespace(final String ns)
    {
        this.namespace = ns;
    }
    
    public Object safeResolve(final Name name)
    {
        final Object obj = this.resolve(name);
        if(obj == null)
            throw new IllegalArgumentException("Can not resolve " + name);
        return obj;
    }
    
    public Object resolve(final Name name)
    {   
        Namespace ns;
        Object obj;
        
        if(name.hasNs())
        {
            ns = this.nss.get(name.getNamespace());
            if(ns == null)
                return null;
            obj = ns.resolveName(name.getName());
            if(obj == null)
                return null;
            
            return obj;
        }
        
        ns = this.nss.get(this.namespace);
        if(ns != null)
        {
            obj = ns.resolveName(name.getName());
            if(obj != null)
                return obj;
        }
        
        ns = this.nss.get("core");
        if(ns != null)
        {
            obj = ns.resolveName(name.getName());
            if(obj != null)
                return obj;
        }

        for(Namespace n : this.nss.values())
        {
            if(n.getName().equals("core"))
                continue;
            
            obj = n.resolveName(name.getName());
            if(obj != null)
                return obj;
        }
        
        return null;
    }

    public Name resolveName(final Name name)
    {   
        Namespace ns;
        
        if(name.hasNs())
            return name;
        
        ns = this.nss.get(this.namespace);
        if(ns != null)
        {
            if(ns.resolveName(name.getName()) != null)
                return new Name(this.namespace, name.getName());
        }
        
        ns = this.nss.get("core");
        if(ns != null)
        {
            if(ns.resolveName(name.getName()) != null)
                return new Name("core", name.getName());
        }
        
        for(Namespace n : this.nss.values())
        {
            if(n.getName().equals("core"))
                continue;
            
            if(n.resolveName(name.getName()) != null)
                return new Name(n.name, name.getName());
        }
        
        return name;
    }
    
    public void addBinding(final Name name, final Object value)
    {
        if(!name.hasNs())
            throw new IllegalStateException("Trying to add a local binding to global scope.");
        Namespace ns = this.nss.get(name.getNamespace());
        if(ns == null)
        {
            ns = new Namespace(name.getNamespace());
            this.nss.put(name.getNamespace(), ns);
        }
        ns.globals.put(name.getName(), value);
    }
    
    public Reader loadSource(final String name) throws IOException
    {
        final File f = Util.resolveHome(name.indexOf('.') == -1 ? name + ".nlsp" : name);
        InputStream in;
        
        if(f.exists())
            return new InputStreamReader(new FileInputStream(f), "UTF-8");

        ArrayList<String> paths = this.load_paths.get(Keyword.create("nlsp"));
        
        if(paths != null)
        {
            for(final String s : paths)
            {
                final File file = new File(Util.resolveHome(s).getAbsolutePath(), f.toString());
                if(file.exists())
                    return new InputStreamReader(new FileInputStream(file), "UTF-8");
            }
        }
        
        if((in = this.getClass().getResourceAsStream(f.toString())) != null)
            return new InputStreamReader(in, "UTF-8");
        
        if((in = this.getClass().getResourceAsStream("/" + f.toString())) != null)
            return new InputStreamReader(in, "UTF-8");
        
        throw new FileNotFoundException(name);
    }
    
    public List<FnMeta> getAllFnMetas()
    {
        HashSet<FnMeta> metas = new HashSet<FnMeta>();
        for(final Namespace ns : this.nss.values())
        {
            for(final Object o : ns.globals.values())
            {
                if(Isa.fn(o))
                {
                    final FnMeta fm = ((Fn)o).meta;
                    if(fm != null)
                        metas.add(fm);
                }
            }
        }
        ArrayList<FnMeta> ret = new ArrayList<FnMeta>(metas);
        Collections.sort(ret);
        return ret;
    }
}


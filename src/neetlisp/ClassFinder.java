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
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFinder
{
    public static List<Class<?>> find(final ClassLoader cl, final String pkgName)
    {
        try
        {
            URL url = cl.getResource(pkgName.replace('.', '/'));
            if(url.getProtocol().equals("jar"))
                return getJars(url, pkgName.replace('.', '/'));
            return getFiles(url.getPath(), pkgName);
        }
        catch(ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private static List<Class<?>> getFiles(final String path, String basePackage) throws ClassNotFoundException
    {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        final File dir = new File(path).getAbsoluteFile();
        final File[] files = dir.listFiles();
        for(File f : files)
        {
            final String fn = f.getName();
            if(f.isDirectory())
            {
                classes.addAll(getFiles(new File(dir, fn).getAbsolutePath(), basePackage + "." + fn));
            }
            else if(f.isFile())
            {
                if(fn.endsWith(".class"))
                {
                    classes.add(Class.forName(basePackage + "." + fn.substring(0, fn.length() - 6)));
                }
            }
        }
        return classes;
    }
    
    private static List<Class<?>> getJars(URL furl, String pkgname)
    {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        try
        {
            final URL url = new URL(furl.getPath());
            final String f = URLDecoder.decode(url.getFile(), System.getProperty("file.encoding"));
            final File dir = new File(f.substring(0, f.lastIndexOf('!')));
            final JarFile jar = new JarFile(dir);
            final Enumeration<JarEntry> j = jar.entries();
            while(j.hasMoreElements())
            {
                final JarEntry je = j.nextElement();
                if(!je.isDirectory())
                {
                    if(je.getName().startsWith(pkgname) && je.getName().endsWith(".class"))
                    {
                        String cn = je.getName();
                        cn = cn.substring(0, cn.length() - 6).replace('/', '.');
                        classes.add(Class.forName(cn));
                    }
                }
            }
            jar.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return classes;
    }
}

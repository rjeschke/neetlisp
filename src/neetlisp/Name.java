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

import neetlisp.asm.MethodVisitor;
import neetlisp.asm.Opcodes;
import neetlisp.compiler.CFn;
import neetlisp.compiler.Compilable;

public class Name implements Compilable
{
    private int hashcode = 0;
    private final String name;
    private final String namespace;
    private final String fullName;
    
    public Name(final String namespace, final String name)
    {
        if(name == null || name.length() == 0)
            throw new IllegalArgumentException("Invalid name: " + name);
        if(namespace != null && namespace.length() == 0)
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        this.namespace = namespace;
        this.name = name;
        if(namespace != null)
            this.fullName = this.namespace + "/" + this.name;
        else
            this.fullName = this.name;
    }
    
    public static Name fromString(final String str)
    {
        if(str == null)
            return null;
        final int idx = str.indexOf('/');
        if(idx == -1)
            return new Name(null, str);
        final String ns = str.substring(0, idx);
        final String na = str.substring(idx + 1);
        if(na.length() == 0)
            return new Name(null, str);
        return new Name(ns, na);
    }
    
    public boolean hasNs()
    {
        return this.namespace != null;
    }
    
    public String getNamespace()
    {
        return this.namespace;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getFullName()
    {
        return this.fullName;
    }
    
    @Override
    public String toString()
    {
        if(this.hasNs())
            return "'" + this.namespace + "/" + this.name;
        return "'" + this.name;
    }
    
    @Override
    public int hashCode()
    {
        if(this.hashcode == 0)
        {
            this.hashcode = this.fullName.hashCode();
        }
        return this.hashcode;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Name))
            return false;
        final Name n = (Name)obj;
        return n.fullName.equals(this.fullName);
    }

    @Override
    public void compile(CFn cfn, MethodVisitor mv)
    {
        final String name;
        if(!cfn.hasConstant(this))
        {
            name = cfn.addConstant(this);
            final MethodVisitor cmv = cfn.getClinit();
            cmv.visitLdcInsn(this.fullName);
            cmv.visitMethodInsn(Opcodes.INVOKESTATIC, "neetlisp/Name", "fromString", "(Ljava/lang/String;)Lneetlisp/Name;");
            cmv.visitFieldInsn(Opcodes.PUTSTATIC, cfn.getFullName(), name, "Ljava/lang/Object;");
        }
        else
        {
            name = cfn.addConstant(this);
        }
        cfn.visitLoadConst(mv, name);
    }
}

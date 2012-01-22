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
package neetlisp.swank;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;

import neetlisp.Context;
import neetlisp.Isa;
import neetlisp.Keyword;
import neetlisp.NFiniteSeq;
import neetlisp.NIterator;
import neetlisp.Name;
import neetlisp.Nil;
import neetlisp.Parser;
import neetlisp.Tokenizer;
import neetlisp.Util;
import neetlisp.seq.NArray;

public class Swank implements Runnable
{
    static NFiniteSeq fromSlime(final NFiniteSeq seq)
    {
        final Object[] ret = new Object[seq.size()];
        final NIterator it = seq.getIterator();
        for(int i = 0; i < ret.length; i++)
        {
            Object obj = it.next();
            if(Isa.name(obj))
            {
                Name n = (Name)obj;
                if(n.getFullName().startsWith("swank:"))
                    obj = Keyword.create(n.getFullName());
                else if(n.getFullName().toLowerCase().equals("t"))
                    obj = Boolean.TRUE;
            }
            else if(Isa.seq(obj))
            {
                obj = fromSlime((NFiniteSeq)obj);
            }
            ret[i] = obj;
        }
        return new NArray(ret);
    }
    
    static String toSlime(final NFiniteSeq seq)
    {
        final StringBuilder sb = new StringBuilder();
        final NIterator it = seq.getIterator();
        sb.append('(');
        while(it.hasNext())
        {
            if(sb.length() > 1)
                sb.append(' ');
            final Object obj = it.next();
            if(Isa.keyword(obj))
            {
                final String s = obj.toString();
                if(s.startsWith(":swank:"))
                    sb.append(s.substring(1));
                else
                    sb.append(s);
            }
            else if(Isa.bool(obj))
            {
                sb.append(Util.booleanValue(obj) ? "t" : "f");
            }
            else if(Isa.nstring(obj))
            {
                sb.append('"');
                sb.append(obj);
                sb.append('"');
            }
            else if(Isa.seq(obj))
            {
                sb.append(toSlime((NFiniteSeq)obj));
            }
            else
            {
                sb.append(obj);
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        ServerSocket server = new ServerSocket(4005);
        System.out.println("Neetlisp SWANK started, listening on 4005, hardcore alpha version.");
        while(true)
        {
            Socket client = server.accept();
            System.out.println("Connected: " + client);
            final Thread t = new Thread(new Swank(client));
            t.setDaemon(true);
            t.start();
        }
    }

    final Socket client;
    final Context context;
    
    public Swank(Socket client)
    {
        this.client = client;
        this.context = new Context();
    }

    private void send(String mesg)
    {
        this.send(mesg, true);
    }
    
    private void send(String mesg, boolean eval)
    {
        try
        {
            final String toSend = eval ? toSlime((NFiniteSeq)new Parser(this.context, new Tokenizer(new StringReader(mesg))).parse()) : mesg;
            final byte[] bytes = toSend.getBytes("UTF-8");
            final String rpc = String.format("%06X%s", bytes.length, toSend);
            this.client.getOutputStream().write(rpc.getBytes("UTF-8"));
            this.client.getOutputStream().flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }
    
    final static String connect_reply = 
        "(:return "+
        "(:ok " +
        "(:pid %s :style :spawn :lisp-implementation " +
        "(:type \"Neetlisp\" :name \"neetlisp\" :version \"0.5.0\") " +
        ":package " +
        "(:name \"user\" :prompt \"user\") " +
        ":version \"20100404\")) " +
        "%d)";
    
    
    final static String create_repl = 
        "(:return "+
        "(:ok " +
        "(\"user\" \"user\")) " +
        " %d)";
             
    final static String write_string = 
        "(:write-string \"%s\" :repl-result) ";

    final static String reply = 
        "(:return " +
        "(:ok nil) " +
        "%d) ";
     
     private void dispatch(NFiniteSeq seq, int counter)
    {
        final Keyword rpc = (Keyword)seq.get(0);
        if(rpc.equals(Keyword.create("swank:connection-info")))
        {
            final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            this.send(String.format(connect_reply, pid.substring(0, pid.indexOf('@')), counter));
        }
        else if(rpc.equals(Keyword.create("swank:create-repl")))
        {
            this.send(String.format(create_repl, counter));
        }
        else if(rpc.equals(Keyword.create("swank:listener-eval")))
        {
            try
            {
                final String str = seq.get(1).toString().trim();
                final Object obj = (str.length() > 0) ? this.context.evalParse(new Tokenizer(new StringReader(str))) : Nil.NIL;
                this.send(String.format(write_string, obj.toString()));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                this.send(String.format(write_string, e.toString()));
            }
            this.send(String.format(reply, counter));
        }
    }
    
    @Override
    public void run()
    {
        InputStream ins;
        int counter = 1;
        try
        {
            this.client.setKeepAlive(true);
            this.client.setSoTimeout(0);
            ins = this.client.getInputStream();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            return;
        }
        
        while(true)
        {
            try
            {
                byte[] sizeBuf = new byte[6];
                if(ins.read(sizeBuf) < 6)
                {
                    this.client.getOutputStream().write(new byte[0]);
                    this.client.getOutputStream().flush();
                    this.client.close();
                    break;
                }
                final int msgLen = Integer.parseInt(new String(sizeBuf, "UTF-8"), 16);
                byte[] message = new byte[msgLen];
                ins.read(message);
                final NFiniteSeq seq = fromSlime((NFiniteSeq)new Parser(this.context, new Tokenizer(new StringReader(new String(message, "UTF-8")))).parse());
                System.out.println("<< " + seq);
                this.dispatch((NFiniteSeq)seq.get(1), counter++);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
        System.out.println("Died.");
    }

}

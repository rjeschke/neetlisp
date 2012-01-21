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

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

public class Tokenizer
{
    private Reader ins;
    private int chCur = ' ';
    private LinkedList<Integer> ungets = new LinkedList<Integer>();
    private Token current = Token.NONE;
    private StringBuilder sb = new StringBuilder();
    private String string;
    private Number number;
    private Context context = null;
    private Fn readerMacro = null;
    
    public Tokenizer(final Reader ins)
    {
        this.ins = ins;
    }
    
    void close() throws IOException
    {
        this.ins.close();
    }
    
    void setContext(final Context c)
    {
        this.context = c;
    }

    public int getCurrentChar()
    {
        return this.chCur;
    }
    
    public Token getToken()
    {
        return this.current;
    }
    
    public String getString()
    {
        return this.string;
    }
    
    public Number getNumber()
    {
        return this.number;
    }
    
    public Fn getReaderMacro()
    {
        return this.readerMacro;
    }
    
    private void readString() throws IOException
    {
        boolean done = false;
        this.sb.setLength(0);
        this.sb.append((char)this.chCur);
        while(!done)
        {
            this.read();
            switch(this.chCur)
            {
            case -1:
                throw new IllegalStateException("Open string.");
            case '\\':
                this.read();
                if(this.chCur == -1)
                    throw new IllegalStateException("Open string.");
                switch(this.chCur)
                {
                case '\\':
                    this.sb.append('\\');
                    break;
                case 't':
                    this.sb.append('\t');
                    break;
                case 'n':
                    this.sb.append('\n');
                    break;
                case 'r':
                    this.sb.append('\r');
                    break;
                default:
                    this.sb.append((char)this.chCur);
                    break;
                }
                break;
            case '"':
                this.read();
                done = true;
                break;
            default:
                this.sb.append((char)this.chCur);
                break;
            }
        }
        this.string = this.sb.toString();
    }
    
    private void readName() throws IOException
    {
        boolean done = false;
        this.sb.setLength(0);
        if(this.chCur == -1 || Character.isWhitespace(this.chCur))
            throw new IllegalStateException("Name is empty.");
        this.sb.append((char)this.chCur);
        while(!done)
        {
            this.read();
            switch(this.chCur)
            {
            case -1:
            case ' ':
            case '\t':
            case '\n':
            case '\r':
            case '(':
            case ')':
                done = true;
                break;
            default:
                this.sb.append((char)this.chCur);
                break;
            }
        }
        this.string = this.sb.toString();
    }
    
    private void readNumber() throws IOException
    {
        boolean done = false;
        this.sb.setLength(0);
        if(this.chCur != '+')
            this.sb.append((char)this.chCur);
        
        while(!done)
        {
            this.read();
            switch(this.chCur)
            {
            case -1:
            case ' ':
            case '\t':
            case '\n':
            case '\r':
            case '(':
            case ')':
                done = true;
                break;
            default:
                this.sb.append((char)this.chCur);
                break;
            }
        }
        
        if(this.sb.indexOf(".") != -1 || this.sb.indexOf("e") != -1 || this.sb.indexOf("E") != -1)
            this.number = Numbers.create(Double.parseDouble(this.sb.toString()));
        else
        {
            try
            {
                final long l = Long.parseLong(this.sb.toString());
                if(l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE)
                    this.number = Numbers.create((int)l);
                else
                    this.number = Numbers.create(l);
            }
            catch(NumberFormatException e)
            {
                this.number = Numbers.create(Double.parseDouble(this.sb.toString()));
            }
        }
    }
    
    
    
    private void ungetc(int ch)
    {
        this.ungets.addLast(this.chCur);
        this.chCur = ch;
    }
    
    public int read() throws IOException
    {
        if(this.ungets.size() > 0)
            return this.chCur = this.ungets.removeFirst();
        return this.chCur = this.ins.read();
    }
    
    public Token next() throws IOException
    {
        Fn fn;
        int tmp;
        for(;;)
        {
            switch(this.chCur)
            {
            case -1:
                return this.current = Token.EOF;
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                this.read();
                break;
            case ';':
                {
                    for(;;)
                    {
                        this.read();
                        if(this.chCur == '\n' || this.chCur == -1)
                            break;
                    }
                }
                break;
            case '(':
                this.read();
                return this.current = Token.BRACE_OPEN;
            case ')':
                this.read();
                return this.current = Token.BRACE_CLOSE;
            case '\"':
                this.read();
                this.readString();
                return this.current = Token.STRING;
            case '+':
            case '-':
                tmp = this.chCur;
                this.read();
                if(Character.isDigit(this.chCur))
                {
                    this.ungetc(tmp);
                    this.readNumber();
                    return this.current = Token.NUMBER;
                }
                this.ungetc(tmp);
                this.readName();
                return this.current = Token.NAME;
            case ':':
                this.read();
                this.readName();
                return this.current = Token.KEYWORD;
            default:
                fn = this.context.getReadCharacterHandler((char)this.chCur);
                if(fn != null)
                {
                    this.read();
                    this.readerMacro = fn;
                    return Token.READER;
                }
                if(Character.isDigit(this.chCur))
                {
                    this.readNumber();
                    return this.current = Token.NUMBER;
                }
                this.readName();
                return this.current = Token.NAME;
            }
        }
    }
    
    public enum Token
    {
        NONE,
        EOF,
        BRACE_OPEN,
        BRACE_CLOSE,
        NAME,
        STRING,
        NUMBER,
        ILLEGAL,
        READER, 
        KEYWORD
    }
}

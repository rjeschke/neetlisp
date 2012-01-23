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
import java.util.ArrayList;

import neetlisp.Tokenizer.Token;
import neetlisp.seq.NList;

public class Parser
{
    private final Tokenizer tok;
    private final Context context;
    
    public Parser(final Context context, final Tokenizer tok)
    {
        this.tok = tok;
        this.context = context;
        tok.setContext(this.context);
    }

    Tokenizer getTokenizer()
    {
        return  this.tok;
    }
    
    public int readCharacter() throws IOException
    {
        return this.tok.read();
    }
    
    public void ungetCharacter(int ch)
    {
        this.tok.ungetc(ch);
    }
    
    private NFiniteSeq parseSeq() throws IOException
    {
        final ArrayList<Object> list = new ArrayList<Object>();
        ReaderMacroResponse resp;
        
        for(;;)
        {
            switch(this.tok.next())
            {
            default:
            case NONE:
            case ILLEGAL:
                throw new IllegalStateException("Syntax error.");
            case EOF:
                return new NList(list);
            case BRACE_OPEN:
                {
                    final NFiniteSeq s = this.parseSeq();
                    if(this.tok.getToken() != Token.BRACE_CLOSE)
                        throw new IllegalStateException("Unmatched par.");
                    list.add(s);
                }
                break;
            case BRACE_CLOSE:
                return new NList(list);
            case NAME:
                if(this.tok.getString().equals("nil"))
                    list.add(Nil.NIL);
                else if(this.tok.getString().equals("false"))
                    list.add(Boolean.FALSE);
                else if(this.tok.getString().equals("true"))
                    list.add(Boolean.TRUE);
                else
                    list.add(Name.fromString(this.tok.getString()));
                break;
            case NUMBER:
                list.add(this.tok.getNumber());
                break;
            case STRING:
                list.add(new NString(this.tok.getString()));
                break;
            case KEYWORD:
                list.add(Keyword.create(this.tok.getString()));
                break;
            case READER:
                resp = (ReaderMacroResponse)this.tok.getReaderMacro().invoke(this, Numbers.create(this.tok.getCurrentChar()));
                switch(resp.type)
                {
                case NONE:
                    break;
                case OBJECT:
                    list.add(resp.object);
                    break;
                }
                break;
            }
        }
    }
    
    private Object parseValue() throws IOException
    {
        ReaderMacroResponse resp;
        for(;;)
        {
            switch(this.tok.next())
            {
            default:
            case NONE:
            case ILLEGAL:
                throw new IllegalStateException("Syntax error.");
            case EOF:
                return Eof.EOF;
            case BRACE_OPEN:
                {
                    final NFiniteSeq s = this.parseSeq();
                    if(this.tok.getToken() != Token.BRACE_CLOSE)
                        throw new IllegalStateException("Unmatched par.");
                    return s;
                }
            case BRACE_CLOSE:
                throw new IllegalStateException("Unmatched par.");
            case NAME:
                if(this.tok.getString().equals("nil"))
                    return Nil.NIL;
                else if(this.tok.getString().equals("false"))
                    return Boolean.FALSE;
                else if(this.tok.getString().equals("true"))
                    return Boolean.TRUE;
                return Name.fromString(this.tok.getString());
            case NUMBER:
                return this.tok.getNumber();
            case STRING:
                return new NString(this.tok.getString());
            case KEYWORD:
                return Keyword.create(this.tok.getString());
            case READER:
                resp = (ReaderMacroResponse)this.tok.getReaderMacro().invoke(this, Numbers.create(this.tok.getCurrentChar()));
                switch(resp.type)
                {
                case NONE:
                    break;
                case OBJECT:
                    return resp.object;
                }
                break;
            }
        }
    }
    
    public Object parse() throws IOException
    {
        return this.parseValue();
    }
    
    public static class Eof
    {
        public final static Eof EOF = new Eof();
        
        private Eof()
        {
            //
        }
        
        @Override
        public String toString()
        {
            return "eof";
        }
    }
}

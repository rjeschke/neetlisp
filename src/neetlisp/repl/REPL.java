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
package neetlisp.repl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import neetlisp.Context;
import neetlisp.Fn;
import neetlisp.Isa;
import neetlisp.Nil;
import neetlisp.Parser;
import neetlisp.Tokenizer;

public class REPL extends JFrame implements WindowListener
{
    private static final long serialVersionUID = -1563767059287075550L;
    JTextArea output;
    JTextPane input;
    private Context context = new Context();
    private ArrayList<String> history = new ArrayList<String>();
    int historyPos = -1;
    int fontSize = 16;

    public REPL()
    {
        super("neetlisp REPL (bc)");
        this.setSize(800, 600);
        this.getContentPane().setLayout(new BorderLayout());
        
        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDoubleBuffered(true);
        split.setResizeWeight(0.8);
        final JScrollPane upper = new JScrollPane(this.output = new JTextArea(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        final JScrollPane lower = new JScrollPane(this.input = new LispPane(),
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        final Font font = new Font(Font.MONOSPACED, Font.PLAIN, this.fontSize);
        this.output.setEditable(false);
        this.output.setBackground(Color.BLACK);
        this.output.setForeground(Color.GREEN);
        this.output.setTabSize(2);
        this.output.setFont(font);
        this.input.setFont(font);
        this.input.setBackground(Color.BLACK);
        this.input.setForeground(Color.GREEN);
        this.input.setCaretColor(Color.GREEN);
        
        split.add(upper, JSplitPane.TOP);
        split.add(lower, JSplitPane.BOTTOM);

        final Keymap km = JTextComponent.addKeymap("editKeys", this.input.getKeymap());
        final int cmd = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();        
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new ClearAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, cmd), new ClearOutputAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, cmd), new SubmitAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, cmd), new HistoryUpAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, cmd), new HistoryDownAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_L, cmd), new LambdaAction());
        
        if(System.getProperty("os.name").toLowerCase().startsWith("mac"))
        {
            km.addActionForKeyStroke(KeyStroke.getKeyStroke(93, cmd), new FontIncAction());
            km.addActionForKeyStroke(KeyStroke.getKeyStroke(47, cmd), new FontDecAction());
        }
        else
        {
            km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, cmd), new FontIncAction());
            km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, cmd), new FontDecAction());
        }
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, cmd), new FontIncAction());
        km.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, cmd), new FontDecAction());
        
        this.input.setKeymap(km);

        this.output.setLineWrap(true);
        
        this.getContentPane().add(split, BorderLayout.CENTER);
        
        this.addWindowListener(this);

        try
        {
            System.setOut(new PrintStream(new TextAreaOutputStream(), true, "UTF-8"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addToHistory(final String str)
    {
        int idx = this.history.indexOf(str);
        if(idx != -1)
        {
            this.history.remove(idx);
            this.history.add(str);
        }
        else
        {
            this.history.add(str);
        }
        this.historyPos = -1;
    }
    
    private String prepareText(String text)
    {
        String[] toks = text.replaceAll("\r\n", "\n").split("\n");
        StringBuilder ret = new StringBuilder();
        ret.append(this.context.getNamespace());
        for(int i = 0; i < toks.length; i++)
        {
            ret.append("=> ");
            ret.append(toks[i]);
            ret.append('\n');
        }
        return ret.toString();
    }
    
    public void submit()
    {
        final String text = this.input.getText().trim();

        if(text.length() == 0)
        {
            this.input.setText("");
            return;
        }
        
        try
        {
            System.out.print(this.prepareText(text));
            System.out.flush();
            final Parser parser = new Parser(this.context, new Tokenizer(new StringReader(text)));
            Object obj, ret = Nil.NIL;
            for(;;)
            {
                long t0, t1, t2;
                t0 = System.nanoTime();
                obj = parser.parse();
                t0 = System.nanoTime() - t0;
                
                if(Isa.eof(obj))
                    break;
                
                t1 = System.nanoTime();
                final Fn fn = this.context.getCompiler().compile(obj);
                t1 = System.nanoTime() - t1;
                
                t2 = System.nanoTime();
                ret = fn.invoke();
                t2 = System.nanoTime() - t2;
                
                System.err.println("Parse: " + (float)(t0 * 1e-6) + "ms, compile: " + (float)(t1 * 1e-6) + "ms, eval: " + (float)(t2 * 1e-6) + "ms");
            }
            System.out.println("<= " + ret);
            System.out.flush();
            this.addToHistory(this.input.getText());
            this.input.setText("");
        }
        catch (Exception e)
        {
            System.out.println("**Exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.flush();
        this.output.setCaretPosition(this.output.getText().length());
    }
    
    public void historyMove(int delta)
    {
        if(delta > 0)
        {
            if(this.historyPos != -1 && this.historyPos < this.history.size())
            {
                this.historyPos++;
                this.input.setText(this.historyPos < this.history.size() ? this.history.get(this.historyPos) : "");
            }
        }
        else
        {
            if(this.historyPos == -1)
            {
                this.historyPos = this.history.size() - 1;
                this.input.setText(this.historyPos < this.history.size() && this.historyPos >= 0 ? this.history.get(this.historyPos) : "");
            }
            else
            {
                if(this.historyPos > 0)
                {
                    this.historyPos--;
                    this.input.setText(this.historyPos < this.history.size() ? this.history.get(this.historyPos) : "");
                }
            }
        }
    }
    
    class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = -4332314437684815878L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.historyPos = -1;
            REPL.this.input.setText("");
        }
    }
    
    class ClearOutputAction extends AbstractAction
    {
        private static final long serialVersionUID = 7296126370461188147L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.output.setText("");
            System.out.println(";; neetlisp REPL - you may start eval'ing ... again");
        }
    }
    
    class SubmitAction extends AbstractAction
    {
        private static final long serialVersionUID = -2450989430774284046L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.submit();
        }
    }
    
    class LambdaAction extends AbstractAction
    {
        private static final long serialVersionUID = -2450989430774284046L;
        
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            try
            {
                REPL.this.input.getDocument().insertString(REPL.this.input.getCaretPosition(), "λ", null);
            }
            catch (BadLocationException e)
            {
                // ignore
            }
        }
    }
    
    class HistoryUpAction extends AbstractAction
    {
        private static final long serialVersionUID = -4973991586803487159L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.historyMove(-1);
        }
    }
    
    class HistoryDownAction extends AbstractAction
    {
        private static final long serialVersionUID = 2177699911805895999L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.historyMove(1);
        }
    }
    
    class FontIncAction extends AbstractAction
    {
        private static final long serialVersionUID = -3070333214357487278L;

        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.fontSize = Math.min(24, REPL.this.fontSize + 1);
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, REPL.this.fontSize);
            REPL.this.input.setFont(font);
            REPL.this.output.setFont(font);
        }
    }

    class FontDecAction extends AbstractAction
    {
        private static final long serialVersionUID = -3070333214357487278L;
        
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            REPL.this.fontSize = Math.max(8, REPL.this.fontSize - 1);
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, REPL.this.fontSize);
            REPL.this.input.setFont(font);
            REPL.this.output.setFont(font);
        }
    }
    
    public static void main(String[] args)
    {
        REPL repl = new REPL();
        repl.setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent arg0)
    {
        this.input.grabFocus();
        System.out.println(";; neetlisp REPL - you may start eval'ing");
    }

    @Override
    public void windowClosing(WindowEvent arg0)
    {
        this.dispose();
    }

    @Override public void windowActivated(WindowEvent arg0) { /* */ }
    @Override public void windowClosed(WindowEvent arg0) { /* */ }
    @Override public void windowDeactivated(WindowEvent arg0) { /* */ }
    @Override public void windowDeiconified(WindowEvent arg0) { /* */ }
    @Override public void windowIconified(WindowEvent arg0) { /* */ }
    
    public class TextAreaOutputStream extends OutputStream
    {
        private byte[] buffer = new byte[65536];
        private int contains = 0;

        public TextAreaOutputStream()
        {
            //
        }

        private void assureSize(int sz)
        {
            if(this.buffer.length < sz)
                this.buffer = Arrays.copyOf(this.buffer, this.buffer.length * 2);
        }
        
        @Override
        public void write(int i) throws IOException
        {
            this.assureSize(this.contains + 1);
            this.buffer[this.contains++] = (byte)i;
        }

        @Override
        public void write(byte[] bytes, int offs, int len) throws IOException
        {
            this.assureSize(this.contains + len);
            System.arraycopy(bytes, offs, this.buffer, this.contains, len);
            this.contains += len;
        }

        @Override
        public void flush() throws IOException 
        {
            if(this.contains > 0)
            {
                final String output = new String(this.buffer, 0, this.contains, "UTF-8");
                this.contains = 0;
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        REPL.this.output.append(output);
                    }
                });
            }
        }
    }
}

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

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class LispPane extends JTextPane implements DocumentListener
{
    private static final long serialVersionUID = -4373307505569284743L;
    private AttributeSet badBrace;
    private AttributeSet normal;
    private AttributeSet[] rainbow;
    
    private final static Color[] COLORS = new Color[] {
        Color.RED, Color.LIGHT_GRAY, Color.ORANGE, new Color(0x5050ff), Color.PINK, Color.CYAN, Color.WHITE, Color.YELLOW
    };
    
    public LispPane()
    {
        super();
        this.getDocument().addDocumentListener(this);
        this.createAttributes();
    }
    
    private void createAttributes()
    {
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        
        this.badBrace = sc.addAttribute(
                sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.WHITE), 
                StyleConstants.Background, Color.RED);
        
        this.normal = sc.addAttribute(
                sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, Color.BLACK), 
                StyleConstants.Foreground, Color.GREEN);
        
        this.rainbow = new AttributeSet[COLORS.length];
        
        for(int i = 0; i < COLORS.length; i++)
            this.rainbow[i] = sc.addAttribute(
                    sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, Color.BLACK), 
                    StyleConstants.Foreground, COLORS[i]);
    }

    void rainbowBraces()
    {
        final StyledDocument doc = this.getStyledDocument();
        String text = "";
        final int len = doc.getLength();
        try
        {
          text = doc.getText(0, len);
        }
        catch (BadLocationException eaten) 
        { 
            // *munch*
        }

        doc.setCharacterAttributes(0, len, this.normal, true);
        int bc = 0;
        for(int i = 0; i < text.length(); i++)
        {
            final char ch = text.charAt(i);
            if(ch == '(')
            {
                doc.setCharacterAttributes(i, 1, this.rainbow[bc % COLORS.length], true);
                bc++;
            }
            else if(ch == ')')
            {
                if(bc == 0)
                    doc.setCharacterAttributes(i, 1, this.badBrace, true);
                else
                {
                    bc--;
                    doc.setCharacterAttributes(i, 1, this.rainbow[bc % COLORS.length], true);
                }
            }
            else if(ch == '\"')
            {
                i++;
                while(i < text.length() && text.charAt(i) != '\"')
                    i++;
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        // empty
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                LispPane.this.rainbowBraces();
            }
        });
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                LispPane.this.rainbowBraces();
            }
        });
    }
}
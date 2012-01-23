package neetlisp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DocGen
{
    public static class Entry implements Comparable<Entry>
    {
        final Name name;
        final FnMeta meta;
        
        public Entry(Name name, FnMeta meta)
        {
            this.name = name;
            this.meta = meta;
        }

        @Override
        public int compareTo(Entry o)
        {
            if(this.name.getNamespace().length() == o.name.getNamespace().length())
                return this.name.getFullName().compareTo(o.name.getFullName());
            return this.name.getNamespace().length() - o.name.getNamespace().length();
        }
    }
    
    public static String encode(String str)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
        {
            switch(str.charAt(i))
            {
            case '>':
                sb.append("&gt;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            default:
                sb.append(str.charAt(i));
                break;
            }
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException, IOException
    {
        ArrayList<Entry> all = new ArrayList<DocGen.Entry>();
        HashMap<FnMeta, ArrayList<String>> mapped = new HashMap<FnMeta, ArrayList<String>>();
        
        final Context context = new Context();
        
        for(final Namespace ns : context.nss.values())
        {
            for(final java.util.Map.Entry<String, Object> e : ns.globals.entrySet())
            {
                final String n = e.getKey();
                final Object o = e.getValue();
                if(Isa.fn(o))
                {
                    final FnMeta fm = ((Fn)o).meta;
                    if(fm != null)
                    {
                        final Name fna = new Name(ns.name, n); 
                        all.add(new Entry(fna, fm));
                        ArrayList<String> aa = mapped.get(fm);
                        if(aa == null)
                        {
                            aa = new ArrayList<String>();
                            mapped.put(fm, aa);
                        }
                        aa.add(fna.toString());
                    }
                }
            }
        }

        Collections.sort(all);

        final OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream("../../api.html"), "UTF-8");
        
        w.write("<!DOCTYPE html>\n");
        w.write("<html>\n");
        w.write("<head>\n");
        w.write("<meta charset='utf-8'>\n");
        w.write("<title>neetlisp API</title>\n");
        w.write("</head>\n");
      
        w.write("<body>\n");
        w.write("<table style='width:100ex;'><tr><td>\n");
        w.write("<a name='overview'><h2>Overview:</h2></a>\n");
        w.write("<table width='100%'>\n");
        int x = 0;
        for(Entry e : all)
        {
            if(x == 0)
                w.write("<tr>");
            w.write("<td>");
            StringBuilder link = new StringBuilder();
            link.append("<a href=\"#");
            link.append(URLEncoder.encode(e.meta.name.getFullName(), "UTF-8"));
            link.append("\">");
            link.append(encode(e.name.toString()));
            link.append("</a>");
            w.write(link.toString());
            w.write("</td>");
            x++;
            if(x == 4)
            {
                x = 0;
                w.write("</tr>\n");
            }
        }
        if(x != 0)
        {
            for(int u = x; u < 4; u++)
            {
                w.write("<td>&nbsp;</td>");
            }
            w.write("</tr>\n");
        }
        w.write("</table>\n");

        w.write("<h2>Detail:</h2>\n");
        
        ArrayList<FnMeta> sorted = new ArrayList<FnMeta>(mapped.keySet());
        Collections.sort(sorted);
        
        for(ArrayList<String> as : mapped.values())
        {
            Collections.sort(as);
        }
        
        for(FnMeta fn : sorted)
        {
            w.write("<b><a name=\"");
            w.write(URLEncoder.encode(fn.name.getFullName(), "UTF-8"));
            w.write("\">");
            final ArrayList<String> aliases = mapped.get(fn);
            w.write(encode(aliases.get(0)));
            for(int i = 1; i < aliases.size(); i++)
            {
                w.write(", ");
                w.write(encode(aliases.get(i)));
            }
            w.write("</a></b></br>\n");
            w.write("<pre>");
            w.write(encode(fn.toString()));
            w.write("\n");
            w.write("-----\n");
            w.write(encode(fn.doc));
            w.write("</pre>");
            w.write("<small><a href='#overview'>Overview</a></small>\n");
            w.write("<hr>\n");
        }

        w.write("<small>Copyright &copy; 2012 René Jeschke</br>\n");
        w.write("Project link: <a href='https://github.com/rjeschke/neetlisp'>https://github.com/rjeschke/neetlisp</a></br></small>");
        w.write("</td></tr></table>\n");
        w.write("</body>\n");
        w.write("</html>\n");
        
        w.close();
    }
}

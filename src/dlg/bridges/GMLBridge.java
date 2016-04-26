/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import dlg.util.Label;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class GMLBridge implements DLGWriter {
        
    public DLG load(BufferedReader br) throws Exception {
        int root = -1;
        int nvertexes = 0;
        List<String> vertexLabels = new ArrayList<>();
        List<Integer> sources = new ArrayList<>();
        List<Integer> targets = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        if (!br.ready()) return null;
        
        String token = readNextToken(br);
        if (!token.equals("graph")) throw new Exception("graph not found");
        token = readNextToken(br);
        if (!token.equals("[")) throw new Exception("[ not found");
        do {
            token = readNextToken(br);
            if (token.equals("comment")) {
                readNextToken(br);
            } else if (token.equals("directed")) {
                token = readNextToken(br);
                if (!token.equals("1")) throw new Exception("only directed graphs are supportedd");
            } else if (token.equals("id")) {
                readNextToken(br);
            } else if (token.equals("label")) {
                readNextToken(br);
            } else if (token.equals("root")) {
                token = readNextToken(br);
                root = Integer.parseInt(token);
            } else if (token.equals("]")) {
                break;
            } else if (token.equals("node")) {
                token = readNextToken(br);
                if (!token.equals("[")) throw new Exception("[ not found");
                token = readNextToken(br);
                if (!token.equals("id")) throw new Exception("id not found");
                token = readNextToken(br);
                if (!token.equals("" + nvertexes)) throw new Exception("vertex ids are not consecutive, or do not start on 0");
                nvertexes++;
                token = readNextToken(br);
                if (!token.equals("label")) throw new Exception("label not found");
                token = readNextToken(br);
                vertexLabels.add(token);
                token = readNextToken(br);
                if (!token.equals("]")) throw new Exception("] not found");
            } else if (token.equals("edge")) {
                token = readNextToken(br);
                if (!token.equals("[")) throw new Exception("[ not found");
                token = readNextToken(br);
                if (!token.equals("source")) throw new Exception("source not found");
                token = readNextToken(br);
                sources.add(Integer.parseInt(token));
                token = readNextToken(br);
                if (!token.equals("target")) throw new Exception("target not found");
                token = readNextToken(br);
                targets.add(Integer.parseInt(token));
                token = readNextToken(br);
                if (!token.equals("label")) throw new Exception("label not found");
                token = readNextToken(br);
                labels.add(token);
                token = readNextToken(br);
                if (!token.equals("]")) throw new Exception("] not found");
            }
        }while(true);
        
        DLG g = null;
        if (root==-1) {
            g = new DLG(nvertexes);
        } else {
            g = new TreeDLG(nvertexes);
            ((TreeDLG)g).setRoot(root);
        }
        for(int i = 0;i<nvertexes;i++) {
            g.setVertex(i, new Label(vertexLabels.get(i)));
        }
        for(int i = 0;i<sources.size();i++) {
            g.setEdge(sources.get(i), targets.get(i), new Label(labels.get(i)));
        }
        
        return g;
    }

    
    static String readNextToken(BufferedReader br) throws Exception {
        String token = "";
        if (!br.ready()) return null;
        char c = (char)br.read();
        while (c==' ' || c=='\r' || c=='\n') {
            if (!br.ready()) return null;
            c = (char)br.read();
        }
        if (c=='\"') {
            c = (char)br.read();
            while (c!='\"') {
                if (!br.ready()) return null;
                token += c;
                c = (char)br.read();
            }
        } else {
            while (c!=' ' && c!='\r' && c!='\n') {
                if (!br.ready()) return token;
                token += c;
                c = (char)br.read();
            }
        }
        return token;
    }
    

    public void save(DLG g, Writer w) throws Exception {
        save(g, w, null, null);
    }
    
    
    public void save(DLG g, Writer w, Integer id, String label) throws Exception {
        w.write("graph [\n");
        w.write("  directed 1\n");
        if (id!=null) w.write("  id " + id + "\n");
        if (label!=null) w.write("  label \"" + label + "\"\n");
        if (g instanceof TreeDLG) {
            w.write("  root " + ((TreeDLG) g).getRoot() + "\n");
        }
        for(int i = 0;i<g.getNVertices();i++) {
            w.write("  node [\n");
            w.write("    id " + i + "\n");
            w.write("    label \""+ g.getVertex(i) + "\"\n");
            w.write("  ]\n");
        }
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                if (g.getEdge(i, j)!=null) {
                    w.write("  edge [\n");
                    w.write("    source " + i + "\n");
                    w.write("    target "+ j + "\n");
                    w.write("    label \"" + g.getEdge(i, j) + "\"\n");
                    w.write("  ]\n");
                }
            }
        }
        w.write("]\n");
    }    
}

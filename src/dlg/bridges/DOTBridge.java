/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import java.io.Writer;

/**
 *
 * @author santi
 */
public class DOTBridge {
    public void save(DLG g, Writer w) throws Exception {
        save(g, w, "graphname");
    }
    
    
    public void save(DLG g, Writer w, String label) throws Exception {
        w.write("digraph \""+label+"\" {\n");
        for(int i = 0;i<g.getNVertices();i++) {
            w.write("  v"+i+" [label=\""+g.getVertex(i)+"\"];\n");
        }
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                if (g.getEdge(i, j)!=null) {
                    w.write("  v"+i+" -> v"+j+" [label=\""+g.getEdge(i,j)+"\"];\n");
                }
            }
        }
        w.write("}\n");
    }    
}

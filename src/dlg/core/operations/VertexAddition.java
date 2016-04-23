/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class VertexAddition {
    public static DLG addVertex(DLG g, Label l) {
        int n = g.getNVertices();
        DLG g2 = (g instanceof TreeDLG ? new TreeDLG(n+1) : new DLG(n+1));
        for(int i = 0;i<n;i++) {
            g2.setVertex(i, g.getVertex(i));
            for(int j = 0;j<n;j++) {
                g2.setEdge(i, j, g.getEdge(i, j));
            }
        }
        g2.setVertex(n, l);
        if (g2 instanceof TreeDLG) {
            for(int i = 0;i<n-1;i++) {
                if (((TreeDLG)g2).getParent(i)==-1) {
                    ((TreeDLG)g2).setRoot(i);
                    break;
                }
            }            
        }        
        return g2;
    }


    public static DLG addVertexFrom(DLG g, Label l, Label edgeLabel, int sourceVertex) {
        DLG g2 = addVertex(g, l);
        g2.setEdge(sourceVertex, g.getNVertices(), edgeLabel);
        return g2;
    }


    public static DLG addVertexTo(DLG g, Label l, Label edgeLabel, int targetVertex) {
        DLG g2 = addVertex(g, l);
        g2.setEdge(g.getNVertices(), targetVertex, edgeLabel);
        return g2;
    }

}

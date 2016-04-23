/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core;

import java.util.List;

/**
 *
 * @author santi
 */
public class TreeDLG extends DLG {
    int m_root;    
    
    
    public TreeDLG(int n) {
        super(n);
        m_root = 0;
    }
    
    
    public TreeDLG(TreeDLG t) {
        super(t);
        m_root = t.m_root;
    }
    
    
    public TreeDLG(DLG g, int root) {
        super(g);
        m_root = root;
    }


    public TreeDLG(DLG g) {
        super(g);
        for(int i = 0;i<getNVertices();i++) {
            if (getParent(i)==-1) {
                m_root = i;
                break;
            }
        }
    }
    
    
    public int getRoot() {
        return m_root;
    }
    
    
    public void setRoot(int root) {
        m_root = root;
    }
    
    public int[] getChildren(int v) {
        return getCondensedOutgoingEdges()[v];
    }
    
    
    public int getParent(int v) {
        int []tmp = getCondensedIncomingEdges()[v];
        if (tmp!=null && tmp.length>0) return tmp[0];
        return -1;
    }
    
    
    public boolean isParent(int parent, int child) {
        int c_parent = getParent(child);
        if (parent == c_parent) return true;
        if (c_parent == -1) return false;
        return isParent(parent, c_parent);
    }


    public TreeDLG subgraph(List<Integer> vertices) {
        TreeDLG g = new TreeDLG(vertices.size());
        for(int i = 0;i<vertices.size();i++) {
            g.setVertex(i, getVertex(vertices.get(i)));
            for(int j = 0;j<vertices.size();j++) {
                g.setEdge(i, j, getEdge(vertices.get(i), vertices.get(j)));
            }
        }
        for(int i = 0;i<vertices.size();i++) {
            if (g.getParent(i) == -1) g.setRoot(i);
        }
        return g;
    }
}

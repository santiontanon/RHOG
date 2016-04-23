/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class GraphFilter {

    public static DLG removeVertex(DLG g, int v) {
        int n = g.getNVertices();
        DLG g2 = (g instanceof TreeDLG ? new TreeDLG(n-1) : new DLG(n-1));
        for(int i = 0;i<n-1;i++) {
            if (i<v) {
                g2.setVertex(i, g.getVertex(i));
                for(int j = 0;j<n-1;j++) {
                    if (j<v) {
                        g2.setEdge(i, j, g.getEdge(i, j));
                    } else {
                        g2.setEdge(i, j, g.getEdge(i, j+1));
                    }
                }
            } else {
                g2.setVertex(i, g.getVertex(i+1));
                for(int j = 0;j<n-1;j++) {
                    if (j<v) {
                        g2.setEdge(i, j, g.getEdge(i+1, j));
                    } else {
                        g2.setEdge(i, j, g.getEdge(i+1, j+1));
                    }
                }
            }
        }
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
    
    
    public static DLG removeVertexWithLabel(DLG g, Label label) {
        List<Label> labels = new ArrayList<>();
        labels.add(label);
        return removeVertexWithLabels(g, labels);
    }
    
    public static DLG removeVertexWithLabels(DLG g, List<Label> labels) {
        List<Integer> verticesToKeep = new ArrayList<>();
        for(int i = 0;i<g.getNVertices();i++) {
            if (!labels.contains(g.getVertex(i))) verticesToKeep.add(i);
        }
                
        DLG g2 = new DLG(verticesToKeep.size());
        for(int i = 0;i<g2.getNVertices();i++) {
            int mapped_i = verticesToKeep.get(i);
            g2.setVertex(i, g.getVertex(mapped_i));
            for(int j = 0;j<g2.getNVertices();j++) {
                int mapped_j = verticesToKeep.get(j);
                g2.setEdge(i, j, g.getEdge(mapped_i, mapped_j));
            }
        }
        return g2;
    }
    
    
    // removes all edges with the specified labels. Any nodes that became isolated from
    // the parents of these edges, are also removed
    public static DLG removeEdgesAndDescendantsWithLabel(DLG g, Label label) {
        List<Label> labels = new ArrayList<>();
        labels.add(label);
        return removeEdgesAndDescendantsWithLabels(g, labels);
    }
    
    public static DLG removeEdgesAndDescendantsWithLabels(DLG g, List<Label> labels) {
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                if (labels.contains(g.getEdge(i, j))) {
                    DLG tmp_g = removeEdgeAndDescendants(g, i, j);
                    return removeEdgesAndDescendantsWithLabels(tmp_g, labels);
                }
            }
        }
        
        return g;
    }
    
    
    // removes an edge. Any vertices that became isolated from
    // the parents of these edges, are also removed
    public static DLG removeEdgeAndDescendants(DLG g, int v1, int v2) {
        List<Integer> verticesToKeep = new ArrayList<>();
        List<Integer> open = new ArrayList<>();
        open.add(v1);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            verticesToKeep.add(current);
            
            for(int i = 0;i<g.getNVertices();i++) {
                if ((g.getEdge(current, i)!=null || g.getEdge(i, current)!=null) && 
                    !verticesToKeep.contains(i) && !open.contains(i)) {
                    if ((current!=v1 || i!=v2) && 
                        (i!=v1 || current!=v2)) {
                        open.add(i);
                    }
                }
            }
        }
        
        // make sure the resulting graph has the vertices in the same order 
        // (this is not needed, but useful for debugging, and this filter should only be called
        //  for things loaded from disk, anyway, so efficiency is not as important)
        Collections.sort(verticesToKeep); 
        
        DLG g2 = new DLG(verticesToKeep.size());
        for(int i = 0;i<g2.getNVertices();i++) {
            int mapped_i = verticesToKeep.get(i);
            g2.setVertex(i, g.getVertex(mapped_i));
            for(int j = 0;j<g2.getNVertices();j++) {
                int mapped_j = verticesToKeep.get(j);
                if ((mapped_i!=v1 || mapped_j!=v2) && 
                    (mapped_j!=v1 || mapped_i!=v2)) {
                    g2.setEdge(i, j, g.getEdge(mapped_i, mapped_j));
                }                
            }
        }
        return g2;
    }    


    // removes a vertex. Any vertices that became isolated from
    // the parents of this vertex, are also removed
    public static DLG removeVertexAndDescendants(DLG g, int v) {
        List<Integer> verticesToRemove = g.getReachableVertices(v);
        List<Integer> verticesToKeep = new ArrayList();
        for(int i = 0;i<g.getNVertices();i++) 
            if (!verticesToRemove.contains(i)) verticesToKeep.add(i);
                
        DLG g2 = new DLG(verticesToKeep.size());
        for(int i = 0;i<g2.getNVertices();i++) {
            int mapped_i = verticesToKeep.get(i);
            g2.setVertex(i, g.getVertex(mapped_i));
            for(int j = 0;j<g2.getNVertices();j++) {
                int mapped_j = verticesToKeep.get(j);
                g2.setEdge(i, j, g.getEdge(mapped_i, mapped_j));
            }
        }
        return g2;
    }
    
    
    // removes a vertex. Any vertices that became isolated from
    // the parents of this vertex, are also removed
    public static TreeDLG removeVertexAndDescendants(TreeDLG g, int v) {
        List<Integer> verticesToRemove = g.getReachableVertices(v);
        List<Integer> verticesToKeep = new ArrayList();
        for(int i = 0;i<g.getNVertices();i++) 
            if (!verticesToRemove.contains(i)) verticesToKeep.add(i);
                
        TreeDLG g2 = new TreeDLG(verticesToKeep.size());
        for(int i = 0;i<g2.getNVertices();i++) {
            int mapped_i = verticesToKeep.get(i);
            g2.setVertex(i, g.getVertex(mapped_i));
            for(int j = 0;j<g2.getNVertices();j++) {
                int mapped_j = verticesToKeep.get(j);
                g2.setEdge(i, j, g.getEdge(mapped_i, mapped_j));
            }
        }
        if (verticesToKeep.contains(g.getRoot())) {
            g2.setRoot(verticesToKeep.indexOf(g.getRoot()));
        } else {
            for(int i = 0;i<g2.getNVertices();i++) {
                if (g2.getParent(i)==-1) {
                    g2.setRoot(i);
                    break;
                }
            }
        }
        return g2;
    }
    
    
    // this method removes a vertex, but if the removed vertex had incoming and outgoing edges
    // with matching labels, the corresponging parent/child vertices will be linked
    public static DLG removeVertexLinkingNeighbors(DLG g, int v) {
        List<Integer> verticesToKeep = new ArrayList<>();
        for(int i = 0;i<g.getNVertices();i++) {
            if (i!=v) verticesToKeep.add(i);
        }        
        DLG g2;
        if (g instanceof TreeDLG) {
            g2 = new TreeDLG(verticesToKeep.size());
        } else {
            g2 = new DLG(verticesToKeep.size());
        }
        
        for(int i = 0;i<g2.getNVertices();i++) {
            int mapped_i = verticesToKeep.get(i);
            g2.setVertex(i, g.getVertex(mapped_i));
            for(int j = 0;j<g2.getNVertices();j++) {
                int mapped_j = verticesToKeep.get(j);
                g2.setEdge(i, j, g.getEdge(mapped_i, mapped_j));
            }
        }
        
        // link neighbors:
        for(int v1:g.getCondensedIncomingEdges()[v]) {
            for(int v2:g.getCondensedOutgoingEdges()[v]) {
                if (g.getEdge(v1, v).equals(g.getEdge(v, v2))) {
                    g2.setEdge(verticesToKeep.indexOf(v1), 
                               verticesToKeep.indexOf(v2), g.getEdge(v1, v));
                }
            }
        }
        if (g2 instanceof TreeDLG) {
            for(int i = 0;i<g2.getNVertices();i++) {
                if (((TreeDLG)g2).getParent(i)==-1) {
                    ((TreeDLG)g2).setRoot(i);
                    break;
                }
            }            
        }        
        return g2;
    }
}

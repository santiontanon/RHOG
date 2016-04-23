/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core;

import dlg.bridges.TGFBridge;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class DLG {
    int n_vertices = 0;
    Label vertices[] = null;   // size: n_vertices
    Label edges[][] = null;    // size: n_vertices * n_Vertices
    
    // This one just contains entries for the actual edges. E.g., if a specific vertex i, only has one outgoing edge, then coundensedEdges[i].length = 1. If there are no edges, it will be null.
    int condensedOutgoingEdges[][] = null;   // size: n_vertices * variable
    int condensedIncomingEdges[][] = null;   // size: n_vertices * variable
    
    public DLG(int n) {
        n_vertices = n;
        vertices = new Label[n];
        edges = new Label[n][n];
    }
    
    public DLG(DLG g) {
        n_vertices = g.n_vertices;
        vertices = new Label[n_vertices];
        edges = new Label[n_vertices][n_vertices];
        
        for(int i = 0;i<n_vertices;i++) {
            vertices[i] = g.vertices[i];
            for(int j = 0;j<n_vertices;j++) {
                edges[i][j] = g.edges[i][j];
            }
        }
    }
    
    public int getNVertices() {
        return n_vertices;
    }
    
    public Label []getVertices() {
        return vertices;
    }
    
    public Label getVertex(int i) {
        return vertices[i];
    }
    
    public void setVertex(int i, Label l) {
        vertices[i] = l;
    }
    
    public Label [][]getEdges() {
        return edges;
    }
    
    public Label getEdge(int i, int j) {
        return edges[i][j];
    }
    
    public void setEdge(int i, int j, Label l) {
        edges[i][j] = l;
        condensedOutgoingEdges = condensedIncomingEdges = null; // this might have become invalid
    }
    
    
    public int [][]getCondensedOutgoingEdges() {
        if (condensedOutgoingEdges == null) computeCondensedOutgoingEdges();
        return condensedOutgoingEdges;
    }
    
    public int [][]getCondensedIncomingEdges() {
        if (condensedIncomingEdges == null) computeCondensedIncomingEdges();
        return condensedIncomingEdges;
    }    
    
    public void computeCondensedOutgoingEdges() {
        condensedOutgoingEdges = new int [n_vertices][];
        for(int i = 0;i<n_vertices;i++) {
            int n = 0;
            for(int j = 0;j<n_vertices;j++) {
                if (edges[i][j]!=null) n++;
            }
            condensedOutgoingEdges[i] = new int[n];
            for(int j = 0, k = 0;j<n_vertices;j++) {
                if (edges[i][j]!=null) {
                    condensedOutgoingEdges[i][k] = j;
                    k++;
                }
            }
        }
    }
    
    public void computeCondensedIncomingEdges() {
        condensedIncomingEdges = new int [n_vertices][];
        for(int i = 0;i<n_vertices;i++) {
            int n = 0;
            for(int j = 0;j<n_vertices;j++) {
                if (edges[j][i]!=null) n++;
            }
            condensedIncomingEdges[i] = new int[n];
            for(int j = 0, k = 0;j<n_vertices;j++) {
                if (edges[j][i]!=null) {
                    condensedIncomingEdges[i][k] = j;
                    k++;
                }
            }
        }
    }    
    
    public List<Label> getAllVertexLabels() {
        List<Label> l = new ArrayList<>();
        
        for(Label label:getVertices()) {
            if (!l.contains(label)) l.add(label);
        }
        
        return l;
    }

    public List<Label> getAllEdgeLabels() {
        List<Label> l = new ArrayList<>();
        
        for(int i = 0;i<getNVertices();i++) {
            for(int j = 0;j<getNVertices();j++) {
                Label label = getEdge(i,j);
                if (label!=null && !l.contains(label)) l.add(label);            
            }
        }
        
        return l;
    }
    
    
    public List<Integer> getReachableVertices(int v) {
        List<Integer> l = new ArrayList<>();
        List<Integer> open = new ArrayList<>();
        open.add(v);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            if (!l.contains(current)) l.add(current);
            for(int child:getCondensedOutgoingEdges()[current]) {
                if (!l.contains(child) && !open.contains(child)) open.add(child);
            }
        }
        
        return l;
    }
    
    
    public DLG subgraph(List<Integer> vertices) {
        DLG g = new DLG(vertices.size());
        for(int i = 0;i<vertices.size();i++) {
            g.setVertex(i, getVertex(vertices.get(i)));
            for(int j = 0;j<vertices.size();j++) {
                g.setEdge(i, j, getEdge(vertices.get(i), vertices.get(j)));
            }
        }
        return g;
    }

    
    public String toString() {
        try {
            TGFBridge importer = new TGFBridge();
            StringWriter sw = new StringWriter();
            importer.save(this, sw);
            return sw.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
            
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.subsumption;

import dlg.core.DLG;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;
import java.util.Arrays;

/**
 *
 * @author santi
 */
public class FlatTransSubsumption extends Subsumption {
    public static int DEBUG = 0;
    
    boolean objectIdentity = false;
    
    public FlatTransSubsumption(boolean oi) {
        objectIdentity = oi;
    }
    
    public boolean getObjectIdentity() {
        return objectIdentity;
    }

    
    void sortVertices(int vertexOrder[], List<Integer> candidates[])
    {
        // bubble sort:
        boolean change;
        do{
            change = false;
            for(int i = 0;i<vertexOrder.length-1;i++) {
                if (candidates[vertexOrder[i]].size()>candidates[vertexOrder[i+1]].size()) {
                    change = true;
                    int tmp = vertexOrder[i];
                    vertexOrder[i] = vertexOrder[i+1];
                    vertexOrder[i+1] = tmp;
                }
            }
        }while(change);
        
        if (DEBUG>=1) {
            System.out.println("sortVertices: " + Arrays.toString(vertexOrder));
            System.out.print("  candidates:");
            for(int i = 0;i<vertexOrder.length;i++) {
                System.out.print(candidates[vertexOrder[i]].size() + ", ");
            }
            System.out.println("");
        }
    } 

    
    public int[] subsumes(DLG g1, DLG g2, int []mapping) {
        List<Integer> candidates[] = new List[g1.getNVertices()];
        int vertexOrder[] = new int[g1.getNVertices()];
        
        // find candidates:
        for(int i1 = 0;i1<g1.getNVertices();i1++) {
            vertexOrder[i1] = i1;
            candidates[i1] = new ArrayList<Integer>();
            Label l1 = g1.getVertex(i1);
            if (mapping==null || mapping[i1]==-1) {
                for(int i2 = 0;i2<g2.getNVertices();i2++) {
                    // label of the vertex must match:
                    if (!g2.getVertex(i2).equals(l1)) continue;

                    // g2 must have edges coming out with the same labels as the node in g1:
                    boolean allFound = true;
                    for(int j1:g1.getCondensedOutgoingEdges()[i1]) {
                        Label l1j = g1.getEdge(i1, j1);
                        boolean found = false;
                        for(int j2:g2.getCondensedOutgoingEdges()[i2]) {
                            if (g2.getEdge(i2, j2).equals(l1j)) {
                                found = true;
                                break;
                            }
                        }             
                        if (!found) {
                            allFound = false;
                            break;
                        }
                    }
                    if (!allFound) continue;

                    candidates[i1].add(i2);
                }
            } else {
                // if an input mapping is provided, consider only those posibilities:
                if (!g2.getVertex(mapping[i1]).equals(l1)) return null;
                candidates[i1].add(mapping[i1]);
            }
            if (candidates[i1].isEmpty()) return null;
        }
        
        // sort the variables:
        sortVertices(vertexOrder, candidates);

        // find a mapping:
        int []m = new int[g1.getNVertices()];
        for(int i = 0;i<g1.getNVertices();i++) m[i] = -1;
    
        if (objectIdentity) {
            boolean []used = new boolean[g2.getNVertices()];
            for(int i = 0;i<g2.getNVertices();i++) used[i] = false;
            if (subsumesInternalObjectIdentity(0, m, used, candidates, g1, g2, vertexOrder)) return m;
        } else {
            if (subsumesInternal(0, m, candidates, g1, g2, vertexOrder)) return m;
        }
        
        return null;
    }
    
    
    boolean subsumesInternal(int vertex_index, int []m, List<Integer> candidates[], DLG g1, DLG g2, int []vertexOrder) 
    {
        if (vertex_index >= g1.getNVertices()) return true;
        int vertex = vertexOrder[vertex_index];
        if (m[vertex]>=0) {
            return subsumesInternal(vertex_index+1, m, candidates, g1, g2, vertexOrder);
        }
        
        for(int mapping:candidates[vertex]) {
            m[vertex] = mapping;
            if (checkEdgeConsistency(vertex, m, g1, g2) &&
                subsumesInternal(vertex_index+1, m, candidates, g1, g2, vertexOrder)) return true;
            m[vertex] = -1;
        }
        
        return false;
    }
    
    
    boolean checkEdgeConsistency(int i1, int []m, DLG g1, DLG g2) 
    {
        // outgoing:
        for(int j1:g1.getCondensedOutgoingEdges()[i1]) {
            int i2 = m[i1];
            int j2 = m[j1];            
            if (i2>=0 && j2>=0) {
                if (!pathExistsThroughLabel(g2, i2, j2, g1.getEdge(i1, j1))) return false;
            }
        }
        
        // incoming:
        for(int j1:g1.getCondensedIncomingEdges()[i1]) {
            int i2 = m[i1];
            int j2 = m[j1];            
            if (i2>=0 && j2>=0) {
                if (!pathExistsThroughLabel(g2, j2, i2, g1.getEdge(j1, i1))) return false;
            }
        }

        return true;
    }    
    
    
    public boolean pathExistsThroughLabel(DLG g, int v1, int v2, Label l) {
        int n = g.getNVertices();
        boolean closed[] = new boolean[n];
        ArrayList<Integer> open = new ArrayList<>();
        closed[v1] = true;
        open.add(v1);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            for(int next:g.getCondensedOutgoingEdges()[current]) {
                if (g.getEdge(current, next).equals(l)) {
                    if (next==v2) return true;
                    if (!closed[next]) {
                        closed[next] = true;
                        open.add(next);
                    }
                }
            }
        }
        
        return false;
    }
    
    
    boolean subsumesInternalObjectIdentity(int vertex_index, int []m, boolean used[], List<Integer> candidates[], DLG g1, DLG g2, int []vertexOrder) 
    {
        if (vertex_index >= g1.getNVertices()) return true;
        int vertex = vertexOrder[vertex_index];
        if (m[vertex]>=0) {
            return subsumesInternalObjectIdentity(vertex_index+1, m, used, candidates, g1, g2, vertexOrder);
        }
        
        for(int mapping:candidates[vertex]) {
            if (!used[mapping]) {
                if (DEBUG>=1) System.out.println("----\nConsidering mapping m[" + vertex + "] = " + mapping);
                if (DEBUG>=1) System.out.println("used: " + Arrays.toString(used));
                
                m[vertex] = mapping;
                // find all the new edges that have been created:
                int nedges = 0;
                for(int w:g1.getCondensedOutgoingEdges()[vertex]) if (m[w]!=-1) nedges++;
                for(int w:g1.getCondensedIncomingEdges()[vertex]) if (m[w]!=-1 && w!=vertex) nedges++;
                int edgesv1[] = new int[nedges];
                int edgesv2[] = new int[nedges];
                int i = 0;
                for(int w:g1.getCondensedOutgoingEdges()[vertex]) {
                    if (m[w]!=-1) {
                        if (DEBUG>=2) System.out.println("  new edge (out): " + vertex + " -> " + w);
                        edgesv1[i] = vertex;
                        edgesv2[i] = w;
                        i++;
                    }
                }
                for(int w:g1.getCondensedIncomingEdges()[vertex]) {
                    // self-loops are only considered as outgoing (that's why we have the extra check)
                    if (m[w]!=-1 && w!=vertex) {
                        if (DEBUG>=2) System.out.println("  new edge (in): " + w + " -> " + vertex);
                        edgesv1[i] = w;
                        edgesv2[i] = vertex;
                        i++;
                    }
                }
                
                if (DEBUG>=1) System.out.println("edges to check: " + edgesv1.length);
                
                // recursively iterate over all the possibilities for each of those paths 
                // (if for any vertex no path can be found, then backtrack)
                used[mapping] = true;
                if (subsumesInternalObjectIdentityCheckPaths(0, edgesv1, edgesv2, vertex_index, m, used, candidates, g1, g2, vertexOrder)) {
                    return true;
                }
                used[mapping] = false;
                m[vertex] = -1;
            }
        }
        
        return false;
    }    
    
    
    boolean subsumesInternalObjectIdentityCheckPaths(int nextEdge, int edgesv1[], int edgesv2[], int vertex_index, int []m, boolean used[], List<Integer> candidates[], DLG g1, DLG g2, int []vertexOrder) 
    {
        if (nextEdge>=edgesv1.length) {
            return subsumesInternalObjectIdentity(vertex_index+1, m, used, candidates, g1, g2, vertexOrder);
        } else {
            if (DEBUG>=1) System.out.println("  checking edge: " + edgesv1[nextEdge] + " -> " + edgesv2[nextEdge]);
            
            int v1 = edgesv1[nextEdge];
            int v2 = edgesv2[nextEdge];
            Label l = g1.getEdge(v1, v2);
            List<List<Integer>> paths = allPathsThroughLabel(g2, m[v1], m[v2], l, used);
            
            for(List<Integer> path:paths) {
                for(int w:path) used[w] = true;
                if (DEBUG>=1) System.out.println("    checking path: " + path);
                if (subsumesInternalObjectIdentityCheckPaths(nextEdge+1, edgesv1, edgesv2, vertex_index, m, used, candidates, g1, g2, vertexOrder)) return true;
                if (DEBUG>=1) System.out.println("    checking path failed");
                for(int w:path) used[w] = false;
            }
        }
        return false;
    }
    
    
    public List<List<Integer>> allPathsThroughLabel(DLG g, int v1, int v2, Label l, boolean used[]) {
        int n = g.getNVertices();
        int closed[] = new int[n];
        ArrayList<Integer> open = new ArrayList<>();
        ArrayList<List<Integer>> allPaths = new ArrayList<>();
        
        for(int i = 0;i<n;i++) closed[i] = -1;
        closed[v1] = v1;
        open.add(v1);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            for(int next:g.getCondensedOutgoingEdges()[current]) {
                if (g.getEdge(current, next).equals(l)) {
                    if (next==v2) {
                        List<Integer> path = new ArrayList<>();
//                        path.add(v2);
                        int tmp = current;
                        while(tmp!=v1) {
                            path.add(0, tmp);
                            tmp = closed[tmp];
                        }
//                        path.add(0,v1);
                        allPaths.add(path);
                    } else {
                        if (closed[next]==-1 && !used[next]) {
                            closed[next] = current;
                            open.add(next);
                        }
                    }
                }
            }
        }
        
        return allPaths;
    }    
            
}



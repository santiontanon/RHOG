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
public class FlatSubsumption extends Subsumption {
    
    public static int DEBUG = 0;
        
    boolean objectIdentity = false;
    
    public FlatSubsumption(boolean oi) 
    {
        objectIdentity = oi;
    }
    
    public boolean getObjectIdentity() 
    {
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
    
    
    public int[] subsumes(DLG g1, DLG g2, int []mapping) 
    {
        List<Integer> candidates[] = new List[g1.getNVertices()];
        int vertexOrder[] = new int[g1.getNVertices()];
        boolean candidateForAnyVertex[] = new boolean[g2.getNVertices()];
        int ncandidatesForAnyVertex = 0;
        boolean []used = new boolean[g2.getNVertices()];
        for(int i2 = 0;i2<g2.getNVertices();i2++) {
            candidateForAnyVertex[i2] = false;
            used[i2] = false;
        }
        
        // find candidates:
        for(int i1 = 0;i1<g1.getNVertices();i1++) {
            vertexOrder[i1] = i1;
            candidates[i1] = new ArrayList<Integer>();
            Label l1 = g1.getVertex(i1);
            if (mapping==null || mapping[i1]==-1) {
                for(int i2 = 0;i2<g2.getNVertices();i2++) {
                    if (used[i2]) continue;
                    
                    // label of the vertex must match:
                    if (!g2.getVertex(i2).equals(l1)) continue;

                    // under object identity, the more general must haveless or equal connections:
                    if (objectIdentity) {
                        if (g1.getCondensedIncomingEdges()[i1].length>g2.getCondensedIncomingEdges()[i2].length) continue;
                        if (g1.getCondensedOutgoingEdges()[i1].length>g2.getCondensedOutgoingEdges()[i2].length) continue;
                    }
                    
                    // g2 must have edges coming out with the same labels as the node in g1:
                    boolean allFound = true;
                    for(int j1:g1.getCondensedOutgoingEdges()[i1]) {
                        Label l1j = g1.getEdge(i1, j1);
                        boolean found = false;
                        for(int j2:g2.getCondensedOutgoingEdges()[i2]) {
                            if (g2.getEdge(i2, j2).equals(l1j) && 
                                g2.getVertex(j2).equals(g1.getVertex(j1))) {
                                if (objectIdentity) {
                                    if (g1.getCondensedIncomingEdges()[j1].length>g2.getCondensedIncomingEdges()[j2].length) continue;
                                    if (g1.getCondensedOutgoingEdges()[j1].length>g2.getCondensedOutgoingEdges()[j2].length) continue;
                                }
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

                    // g2 must have edges coming in with the same labels as the node in g1:
                    allFound = true;
                    for(int j1:g1.getCondensedIncomingEdges()[i1]) {
                        Label l1j = g1.getEdge(j1, i1);
                        boolean found = false;
                        for(int j2:g2.getCondensedIncomingEdges()[i2]) {
                            if (g2.getEdge(j2, i2).equals(l1j) && 
                                g2.getVertex(i2).equals(g1.getVertex(i1))) {
                                if (objectIdentity) {
                                    if (g1.getCondensedIncomingEdges()[i1].length>g2.getCondensedIncomingEdges()[i2].length) continue;
                                    if (g1.getCondensedOutgoingEdges()[i1].length>g2.getCondensedOutgoingEdges()[i2].length) continue;
                                }
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
                    if (!candidateForAnyVertex[i2]) ncandidatesForAnyVertex++;
                    candidateForAnyVertex[i2] = true;
                }
            } else {
                // if an input mapping is provided, consider only those posibilities:
                if (!g2.getVertex(mapping[i1]).equals(l1)) return null;
                candidates[i1].add(mapping[i1]);
            }
            if (candidates[i1].isEmpty()) return null;
            if (objectIdentity && ncandidatesForAnyVertex<i1+1) return null;
            if (objectIdentity && candidates[i1].size()==1) {
                if (used[candidates[i1].get(0)]) return null;
                used[candidates[i1].get(0)] = true;
            }
        }
                
        // inference (propagate the constraints for those variables that only have one candidate):
        List<Integer> open = new ArrayList<>();
        for(int i = 0;i<g1.getNVertices();i++) {
            if (candidates[i].size()==1) open.add(i);
        }
//        int totalBefore = 0;
//        for(int i = 0;i<g1.getNVertices();i++) totalBefore+=candidates[i].size();
//        System.out.println("open: " + open.size());        
        while(!open.isEmpty()) {
            int i1 = open.remove((int)0);
            int i2 = candidates[i1].get(0);
            
            if (objectIdentity) {
                // If i2 is the only option for i1, then i2 cannot be a candidate for any other vertex:
                for(int i = 0;i<g1.getNVertices();i++) {
                    if (i!=i1) {
                        if (candidates[i].remove((Integer)i2)) {
                            if (candidates[i].isEmpty()) return null;
                            if (candidates[i].size()==1) open.add(i); 
                        }
                    }
                }
            }
            
            for(int j1:g1.getCondensedOutgoingEdges()[i1]) {
                List<Integer> toDelete = new ArrayList<>();
                for(int j2:candidates[j1]) {
                    if (g2.getEdge(i2, j2) == null ||
                        !g1.getEdge(i1, j1).equals(g2.getEdge(i2, j2))) toDelete.add(j2);
                }
                if (!toDelete.isEmpty()) {
                    candidates[j1].removeAll(toDelete);
                    if (candidates[j1].isEmpty()) {
//                        System.out.println("null because of inference!");
                        return null;
                    }
                    if (candidates[j1].size()==1) open.add(j1);
                }                
            }
            for(int j1:g1.getCondensedIncomingEdges()[i1]) {
                List<Integer> toDelete = new ArrayList<>();
                for(int j2:candidates[j1]) {
                    if (g2.getEdge(j2, i2) == null ||
                        !g1.getEdge(j1, i1).equals(g2.getEdge(j2, i2))) toDelete.add(j2);
                }
                if (!toDelete.isEmpty()) {
                    candidates[j1].removeAll(toDelete);
                    if (candidates[j1].isEmpty()) {
//                        System.out.println("null because of inference!");
                        return null;
                    }
                    if (candidates[j1].size()==1) open.add(j1);
                }                
            }
        }
//        int totalAfter = 0;
//        for(int i = 0;i<g1.getNVertices();i++) totalAfter+=candidates[i].size();
//        if (totalBefore != totalAfter) System.out.println("Inference result: " + totalBefore + " -> " + totalAfter + " (" + (totalBefore - totalAfter) + ")");
        
        // sort the variables:
        sortVertices(vertexOrder, candidates);        

        // find a mapping:
        int []m = new int[g1.getNVertices()];
        for(int i = 0;i<g2.getNVertices();i++) used[i] = false;
        for(int i = 0;i<g1.getNVertices();i++) {
            if (candidates[i].size()==1) {
                m[i] = candidates[i].get(0);
                if (objectIdentity && used[m[i]]) return null;
                used[m[i]] = true;
            } else {
                m[i] = -1;                
            }
        }
        
        if (objectIdentity) {
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
    
    
    boolean subsumesInternalObjectIdentity(int vertex_index, int []m, boolean used[], List<Integer> candidates[], DLG g1, DLG g2, int []vertexOrder) 
    {
        if (vertex_index >= g1.getNVertices()) return true;
        int vertex = vertexOrder[vertex_index];
        if (m[vertex]>=0) {
            used[m[vertex]] = true;
            return subsumesInternalObjectIdentity(vertex_index+1, m, used, candidates, g1, g2, vertexOrder);
        }
        
        for(int mapping:candidates[vertex]) {
            if (!used[mapping]) {
                m[vertex] = mapping;
                used[mapping] = true;
                if (//checkSolvabilityOI(vertex_index+1, m, used, candidates, vertexOrder) &&
                    checkEdgeConsistency(vertex, m, g1, g2) &&
                    subsumesInternalObjectIdentity(vertex_index+1, m, used, candidates, g1, g2, vertexOrder)) return true;
                used[mapping] = false;
                m[vertex] = -1;
            }
        }
        
        return false;
    }    

    /*
    boolean checkSolvabilityOI(int vertex_index, int []m, boolean used[], List<Integer> candidates[], int []vertexOrder) {
        for(int i = vertex_index; i<candidates.length; i++) {
            boolean anyAvailable = false;
            if (m[vertexOrder[i]]>=0) continue;
            for(int mapping:candidates[vertexOrder[i]]) {
                if (!used[mapping]) {
                    anyAvailable = true;
                    break;
                }
            }
            if (!anyAvailable) return false;
        }
        return true;
    }
    */
    
    boolean checkEdgeConsistency(int i1, int []m, DLG g1, DLG g2) 
    {
        // outgoing:
        for(int j1:g1.getCondensedOutgoingEdges()[i1]) {
            int i2 = m[i1];
            int j2 = m[j1];            
            if (i2>=0 && j2>=0) {
                if (!g1.getEdge(i1, j1).equals(g2.getEdge(i2, j2))) return false;
            }
        }
        
        // incoming:
        for(int j1:g1.getCondensedIncomingEdges()[i1]) {
            int i2 = m[i1];
            int j2 = m[j1];            
            if (i2>=0 && j2>=0) {
                if (!g1.getEdge(j1, i1).equals(g2.getEdge(j2, i2))) return false;
            }
        }

        return true;
    }    
}



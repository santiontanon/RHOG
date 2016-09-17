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
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author santi
 */
public class FlatSubsumption extends Subsumption {
    
    public class Trail {
        HashMap<Integer, List<Integer>> candidatesTrail = new HashMap<>();
        List<Integer> mappingTrail = new ArrayList<>();
    }
    
    
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
        
        // sort the variables:
        sortVertices(vertexOrder, candidates);        

        // find a mapping:
        int []m = new int[g1.getNVertices()];
        for(int i = 0;i<g2.getNVertices();i++) used[i] = false;
        for(int i = 0;i<g1.getNVertices();i++) m[i] = -1;
        for(int i = 0;i<g1.getNVertices();i++) {
            if (candidates[i].size()==1) {
                m[i] = candidates[i].get(0);
                if (objectIdentity && used[m[i]]) return null;
                if (!checkEdgeConsistency(i, m, g1, g2)) return null;
                used[m[i]] = true;
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

                Trail trail = inference(vertex_index, m, used, candidates, g1, g2, vertexOrder);
                
                if (trail!=null &&
                    checkEdgeConsistency(vertex, m, g1, g2) &&
                    subsumesInternalObjectIdentity(vertex_index+1, m, used, candidates, g1, g2, vertexOrder)) return true;
                if (trail!=null) restoreTrail(trail, candidates, m, used);
                
                used[mapping] = false;
                m[vertex] = -1;
            }
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
    
    
    public Trail inference(int vertex_index, int []m, boolean used[], List<Integer> candidates[], DLG g1, DLG g2, int []vertexOrder) {
        Trail trail = new Trail();
        List<Integer> open = new ArrayList<>();
        boolean backtrack = false;
        
        open.add(vertexOrder[vertex_index]);
        
        if (DEBUG>=1) {
            System.out.println("FlatSubsumption.inference(start): " + vertexOrder[vertex_index]);
        }
        
        while(!open.isEmpty()) {
            int vertex = open.remove(0);
            int image = -1;
                        
            if (m[vertex]>=0) {
                image = m[vertex];
            } else {
                assert candidates[vertex].size()==1;
                image = candidates[vertex].get(0);
                if (used[image]) {
                    if (DEBUG>=1) System.out.println("FlatSubsumption.inference: " + image + " already used! backtrack!");
                    backtrack = true;
                } else {
                    m[vertex] = image;
                    used[image] = true;
                    trail.mappingTrail.add(vertex);
                    if (!checkEdgeConsistency(vertex, m, g1, g2)) {
                        if (DEBUG>=1) System.out.println("FlatSubsumption.inference: " + image + " edge consistency failed! backtrack!");
                        backtrack = true;
                    }
                }
            }
            
            if (DEBUG>=1) System.out.println("FlatSubsumption.inference: " + vertex + " -> " + image);

            if (!backtrack && objectIdentity) {
                List<Integer> trail_v = null;
                // If i2 is the only option for i1, then i2 cannot be a candidate for any other vertex:
                for(int i = vertex_index+1;i<g1.getNVertices();i++) {
                    if (m[vertexOrder[i]]==-1) {
                        if (candidates[vertexOrder[i]].remove((Integer)image)) {
                            if (trail_v==null) trail_v = new ArrayList<>();
                            trail_v.add(vertexOrder[i]);
                            if (candidates[vertexOrder[i]].isEmpty()) {
                                if (DEBUG>=1) System.out.println("FlatSubsumption.inference: candidates for " + vertexOrder[i] + " empty! (OI elimination)");
                                backtrack = true;
                                break;
                            }
                            if (candidates[vertexOrder[i]].size()==1) open.add(vertexOrder[i]);
                        }
                    }
                }
                if (trail_v!=null) trail.candidatesTrail.put(image, trail_v);
            }

            if (!backtrack) {
                for(int j1:g1.getCondensedOutgoingEdges()[vertex]) {
                    if (m[j1]==-1) {
                        List<Integer> toDelete = new ArrayList<>();
                        for(int j2:candidates[j1]) {
                            if (g2.getEdge(image, j2) == null ||
                                !g1.getEdge(vertex, j1).equals(g2.getEdge(image, j2))) toDelete.add(j2);
                        }
                        if (!toDelete.isEmpty()) {
                            for(int v:toDelete) {
                                List<Integer> trail_v = trail.candidatesTrail.get(v);
                                if (trail_v==null) {
                                    trail_v = new ArrayList<>();
                                    trail.candidatesTrail.put(v, trail_v);
                                }
                                trail_v.add(j1);
                            }
                            candidates[j1].removeAll(toDelete);
                            if (candidates[j1].isEmpty()) {
                                if (DEBUG>=1) System.out.println("FlatSubsumption.inference: candidats for " + j1 + " empty! (outgoing elimination)");
                                backtrack = true;
                                break;
                            }
                            if (candidates[j1].size()==1) open.add(j1);
                        }                
                    }
                }
            }
            
            if (!backtrack) {        
                for(int j1:g1.getCondensedIncomingEdges()[vertex]) {
                    if (m[j1]==-1) {
                        List<Integer> toDelete = new ArrayList<>();
                        for(int j2:candidates[j1]) {
                            if (g2.getEdge(j2, image) == null ||
                                !g1.getEdge(j1, vertex).equals(g2.getEdge(j2, image))) toDelete.add(j2);
                        }
                        if (!toDelete.isEmpty()) {
                            for(int v:toDelete) {
                                List<Integer> trail_v = trail.candidatesTrail.get(v);
                                if (trail_v==null) {
                                    trail_v = new ArrayList<>();
                                    trail.candidatesTrail.put(v, trail_v);
                                }
                                trail_v.add(j1);
                            }
                            candidates[j1].removeAll(toDelete);
                            if (candidates[j1].isEmpty()) {
                                if (DEBUG>=1) System.out.println("FlatSubsumption.inference: candidats for " + j1 + " empty! (incoming elimination)");
                                backtrack = true;
                                break;
                            }
                            if (candidates[j1].size()==1) open.add(j1);
                        }       
                    }
                }  
            }
            
            if (backtrack) break;
        }
        
        if (backtrack) {
            if (DEBUG>=1) System.out.println("FlatSubsumption.inference: backtrack!");
            restoreTrail(trail, candidates, m, used);
            return null;
        }        

        if (DEBUG>=1) System.out.println("FlatSubsumption.inference: ok");
        return trail;
    }
    
    
    public void restoreTrail(Trail trail, List<Integer> candidates[], int m[], boolean used[]) {
        for(Entry<Integer, List<Integer>> entry:trail.candidatesTrail.entrySet()) {
            for(int v:entry.getValue()) {
                candidates[v].add(entry.getKey());
            }
        }
        
        for(int v:trail.mappingTrail) {
            used[m[v]] = false;
            m[v] = -1;
        }
    }
}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.subsumption;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.TreeDLG;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class TreePOTransSubsumption extends Subsumption {
    public static int DEBUG = 0;
    
    boolean objectIdentity = false;
    PartialOrder partialOrder = null;
    
    public TreePOTransSubsumption(boolean oi, PartialOrder po) {
        objectIdentity = oi;
        partialOrder = po;
    }

    public boolean getObjectIdentity() {
        return objectIdentity;
    }
    
    public int[] subsumes(DLG g1, DLG g2, int input_mapping[]) {
        TreeDLG t1 = (TreeDLG)g1;
        TreeDLG t2 = (TreeDLG)g2;
        
        if (g1.getNVertices()==0) return new int[0];

        int []m = new int[g1.getNVertices()];
        boolean []used = new boolean[g2.getNVertices()];
        List<Integer> candidates = new ArrayList<>();
        for(int i = 0;i<g1.getNVertices();i++) m[i] = -1;
        for(int i = 0;i<g2.getNVertices();i++) used[i] = false;
        if (input_mapping!=null && input_mapping[t1.getRoot()]!=-1) {
            candidates.add(input_mapping[t1.getRoot()]);
        } else {
            for(int i = 0;i<g2.getNVertices();i++) candidates.add(i);
        }

        if (subsumesInternal(t1.getRoot(), candidates, m, used, t1, t2, input_mapping)) {
            if (DEBUG>=1) System.out.println("TreeTransFlatSubsumption.subsumes: success with mapping " + Arrays.toString(m));
            return m;
        }
    
        return null;
    }
    
    
    public boolean subsumesInternal(int vertex, List<Integer> candidates, int m[], boolean used[], TreeDLG t1, TreeDLG t2, int input_mapping[]) {
        for(int candidate:candidates) {
            if (objectIdentity) {
                // check that the whole path is unused:
                int vertexParent = t1.getParent(vertex);
                if (vertexParent == -1) {
                    if (used[candidate]) continue;
                } else {
                    int v = candidate;
                    boolean anyUsed = false;
                    while(v != m[vertexParent]) {
                        if (used[v]) {
                            anyUsed = true;
                            break;
                        }
                        v = t2.getParent(v);
                    }
                    if (anyUsed) continue;
                }
                
            }
            
            // label of the vertex must be subsumed:
            Label l1 = t1.getVertex(vertex);
            if (!partialOrder.subsumes(l1, t2.getVertex(candidate))) continue;

            if (DEBUG>=2) System.out.println("----\nConsidering mapping m[" + vertex + "] = " + candidate);
            if (DEBUG>=2) System.out.println("used: " + Arrays.toString(used));
                
            // g2 must have edges coming out with the same labels as the node in g1:
            if (DEBUG>=2) System.out.println("Checking labels for children " + Arrays.toString(t1.getChildren(vertex)));
            boolean allFound = true;
            for(int j1:t1.getChildren(vertex)) {
                Label l1j = t1.getEdge(vertex, j1);
                boolean found = false;
                for(int j2:t2.getChildren(candidate)) {
                    if (partialOrder.subsumes(l1j, t2.getEdge(candidate, j2))) {
                        if (DEBUG>=2) System.out.println(vertex + "--(" + l1j + ")-->" + j1 + "  could be mapped to  " + candidate + "--(" + t2.getEdge(candidate, j2) + ")-->" + j2);
                        found = true;
                        break;
                    }
                }             
                if (!found) {
                    allFound = false;
                    break;
                }
            }
            if (DEBUG>=2) System.out.println("allFound (for " + candidate + ") = " + allFound);
            if (!allFound) continue;

            
            boolean success = true;
            m[vertex] = candidate;
            // mark all the path as used:
            int vertexParent = t1.getParent(vertex);
            if (vertexParent==-1) {
                used[candidate] = true;
            } else {
                int v = candidate;
                while(v != m[vertexParent]) {
                    if (DEBUG>=2) System.out.println("  used[" + v + "] = true");
                    used[v] = true;
                    v = t2.getParent(v);
                }
            }
            for(int child:t1.getChildren(vertex)) {
                List<Integer> childCandidates = new ArrayList<>();
                Label el = t1.getEdge(vertex, child);
                for(int childCandidate:t2.getChildren(candidate)) {
                    if (partialOrder.subsumes(el, t2.getEdge(candidate, childCandidate))) {
                        if (input_mapping!=null && input_mapping[child]!=-1) {
                            if (childCandidate==input_mapping[child]) childCandidates.add(childCandidate);
                        } else {
                            childCandidates.add(childCandidate);
                            addDescendantsThroughLabel(childCandidates, t2, childCandidate, el);
                        }
                    }
                }
                if (!subsumesInternal(child, childCandidates, m, used, t1, t2, input_mapping)) {
                    success = false;
                    break;
                }            
            }
            if (success) return true;
            m[vertex] = -1;
            // mark all the path as used:
            if (vertexParent==-1) {
                used[candidate] = false;
            } else {
                int v = candidate;
                while(v != m[vertexParent]) {
                    used[v] = false;
                    v = t2.getParent(v);
                }
            }
        }
        
        return false;
    }
    
    
    void addDescendantsThroughLabel(List<Integer> childCandidates, TreeDLG t, int v, Label el)
    {
        for(int child:t.getChildren(v)) {
            if (partialOrder.subsumes(el, t.getEdge(v, child))) {
                childCandidates.add(child);
                addDescendantsThroughLabel(childCandidates, t, child, el);
            }
        }
    }
    
}



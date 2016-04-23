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
public class TreePOSubsumption extends Subsumption {
    public static int DEBUG = 0;
    
    boolean objectIdentity = false;
    PartialOrder partialOrder = null;
    
    public TreePOSubsumption(boolean oi, PartialOrder po) {
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
            if (DEBUG>=1) System.out.println("TreePOSubsumption.subsumes: success with mapping " + Arrays.toString(m));
            return m;
        }
    
        return null;
    }
    
    
    public boolean subsumesInternal(int vertex, List<Integer> candidates, int m[], boolean used[], TreeDLG t1, TreeDLG t2, int input_mapping[]) {
        boolean used_tmp[] = new boolean[used.length];
        for(int i = 0;i<used.length;i++) used_tmp[i] = used[i];
        for(int candidate:candidates) {
            if (objectIdentity && used[candidate]) continue;
            
            // label of the vertex must subsume that of the candidate:
            Label l1 = t1.getVertex(vertex);
            if (!partialOrder.subsumes(l1, t2.getVertex(candidate))) continue;
            if (DEBUG>=2) System.out.println("Considering mapping m[" + vertex + "] = " + candidate);
                
            // g2 must have edges coming out with the same labels as the node in g1:
            if (DEBUG>=2) System.out.println("Checking labels for children " + Arrays.toString(t1.getChildren(vertex)));
            boolean allFound = true;
            for(int j1:t1.getChildren(vertex)) {
                Label l1j = t1.getEdge(vertex, j1);
                boolean found = false;
                for(int j2:t2.getChildren(candidate)) {
                    if (DEBUG>=2) System.out.println("  Subsumption test between: '"+l1j+"' and '"+t2.getEdge(candidate, j2)+"'");
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
            used[candidate] = true;
            for(int child:t1.getChildren(vertex)) {
                List<Integer> childCandidates = new ArrayList<>();
                for(int childCandidate:t2.getChildren(candidate)) {

                    if (partialOrder.subsumes(t1.getEdge(vertex, child), 
                                              t2.getEdge(candidate, childCandidate))) {
                        if (input_mapping!=null && input_mapping[child]!=-1) {
                            if (childCandidate==input_mapping[child]) childCandidates.add(childCandidate);
                        } else {
                            childCandidates.add(childCandidate);
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
            // we need to restore the whole vector, since we don't know what got changed during the recursive calls:
            for(int i = 0;i<used.length;i++) used[i] = used_tmp[i];
        }
        
        return false;
    }
}



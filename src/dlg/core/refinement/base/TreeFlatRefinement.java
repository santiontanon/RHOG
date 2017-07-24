/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.base;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import dlg.core.operations.GraphFilter;
import dlg.core.refinement.IncrementalRefinementOperator;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class TreeFlatRefinement extends RefinementOperatorUsingFlatLabels implements IncrementalRefinementOperator {

    // internal state of downward incremental refinement:
    TreeDLG dw_currentDLG = null;
    int dw_incremental_stage = 0;
    int dw_stage_0_next_label = 0;
    int dw_stage_1_next_vertex = 0;
    int dw_stage_1_next_vlabel = 0;
    int dw_stage_1_next_elabel = 0;
    int dw_stage_2_next_vlabel = 0;
    int dw_stage_2_next_elabel = 0;
    
    // internal state of upward incremental refinement:
    TreeDLG uw_currentDLG = null;
    int uw_incremental_stage = 0;
    int uw_stage_1_next_vertex = 0;
    
    
    public DLG getTop()
    {
        return new TreeDLG(0);
    }
    
    
    public TreeFlatRefinement(List<Label> rl, List<Label> el) {
        super(rl, el);
    }
    
    
    public List<TreeDLG> downwardRefinements(DLG g) {
        List<TreeDLG> refinements = new ArrayList<>();

        setDLGForDownwardRefinement(g);
        while(true) {
            TreeDLG g2 = (TreeDLG)getNextDownwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }

    
    public void setDLGForDownwardRefinement(DLG g) {
        dw_currentDLG = (TreeDLG)g;
        dw_incremental_stage = 0;
        dw_stage_0_next_label = 0;
        dw_stage_1_next_vertex = 0;
        dw_stage_1_next_vlabel = 0;
        dw_stage_1_next_elabel = 0;
        dw_stage_2_next_vlabel = 0;
        dw_stage_2_next_elabel = 0;
    }

    
    public DLG getNextDownwardRefinement() {
        int n = dw_currentDLG.getNVertices();
        switch(dw_incremental_stage) {
            case 0: // add a vertex to an empty graph:
                {
                    if (n>0 || dw_stage_0_next_label>=vertexLabels.size()) {
                        dw_incremental_stage = 1;
                        dw_stage_1_next_vertex = 0;
                        dw_stage_1_next_vlabel = 0;
                        dw_stage_1_next_elabel = 0;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexLabels.get(dw_stage_0_next_label);
                    dw_stage_0_next_label++;
                    TreeDLG g2 = new TreeDLG(1);
                    g2.setVertex(0,l);
                    g2.setRoot(0);
//                    System.out.println("0");
                    return g2;
                }

            case 1: // add new leaves
                {
                    if (dw_stage_1_next_vertex>=n || edgeLabels.isEmpty()) {
                        dw_incremental_stage = 2;
                        dw_stage_2_next_vlabel = 0;
                        dw_stage_2_next_elabel = 0;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexLabels.get(dw_stage_1_next_vlabel);
                    Label l2 = edgeLabels.get(dw_stage_1_next_elabel);

                    TreeDLG g2 = new TreeDLG(n+1);            
                    g2.setRoot(((TreeDLG)dw_currentDLG).getRoot());
                    for(int j = 0;j<n;j++) g2.setVertex(j, dw_currentDLG.getVertex(j));
                    g2.setVertex(n, l);

                    for(int j = 0;j<n;j++) {
                        for(int k = 0;k<n;k++) {
                            g2.setEdge(j, k, dw_currentDLG.getEdge(j, k));
                        }
                    }
                    g2.setEdge(dw_stage_1_next_vertex, n, l2);
                    
//                    System.out.println("1 - " + dw_stage_1_next_elabel + " - " + dw_stage_1_next_vlabel + " - " + dw_stage_1_next_vertex + " (el: " + edgeLabels.size() + ")");

                    dw_stage_1_next_elabel++;
                    if (dw_stage_1_next_elabel>=edgeLabels.size()) {
                        dw_stage_1_next_elabel = 0;
                        dw_stage_1_next_vlabel++;
                    }
                    if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                        dw_stage_1_next_vlabel = 0;
                        dw_stage_1_next_vertex++;
                    }
                    
                    return g2;
                }
                
            case 2: // add a new root:
                {          
                    if (dw_stage_2_next_vlabel>=vertexLabels.size() ||
                        edgeLabels.isEmpty()) return null;

                    Label l = vertexLabels.get(dw_stage_2_next_vlabel);
                    Label l2 = edgeLabels.get(dw_stage_2_next_elabel);
                    TreeDLG g2 = new TreeDLG(n+1);
                    g2.setVertex(0, l);
                    g2.setRoot(0);
                    if (n>0) {
                        g2.setEdge(0,dw_currentDLG.getRoot()+1, l2);
                    }
                    for(int i = 0;i<n;i++) {
                        g2.setVertex(i+1, dw_currentDLG.getVertex(i));
                        for(int j = 0;j<n;j++) {
                            g2.setEdge(i+1,j+1, dw_currentDLG.getEdge(i,j));
                        }
                    }
                    
                    dw_stage_2_next_elabel++;
                    if (dw_stage_2_next_elabel>=edgeLabels.size()) {
                        dw_stage_2_next_elabel = 0;
                        dw_stage_2_next_vlabel++;
                    }
                    
                    return g2;
                }
        }        
        return null;
    }

    public List<? extends DLG> upwardRefinements(DLG g) throws Exception {
        List<TreeDLG> refinements = new ArrayList<>();

        setDLGForUpwardRefinement(g);
        while(true) {
            TreeDLG g2 = (TreeDLG)getNextUpwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }

    public void setDLGForUpwardRefinement(DLG g) throws Exception {
        uw_currentDLG = (TreeDLG)g;
        uw_incremental_stage = 0;
        uw_stage_1_next_vertex = 0;
    }

    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case 0: // remove the root if it only has 1 or 0 children:
                {
                    if (uw_currentDLG.getNVertices()==0) return null;
                    int root = uw_currentDLG.getRoot();
                    if (uw_currentDLG.getChildren(root).length>1) {
                        uw_incremental_stage = 1;
                        uw_stage_1_next_vertex = 0;
                        return getNextUpwardRefinement();
                    }

                    // remove root
                    DLG g2 = GraphFilter.removeVertex(uw_currentDLG, root);
                    uw_incremental_stage = 1;
                    return g2;
                }
            case 1: // remove a leaf
            {
                if (n<=1 || uw_stage_1_next_vertex>=n) return null;

                if (uw_currentDLG.getChildren(uw_stage_1_next_vertex).length>0) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }
                
                // remove leaf:
                DLG g2 = GraphFilter.removeVertex(uw_currentDLG, uw_stage_1_next_vertex);

                uw_stage_1_next_vertex++;                
                return g2;
            }
        }
        return null;
    }
    
}

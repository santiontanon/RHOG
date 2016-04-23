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
import dlg.core.refinement.RefinementOperator;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 * 
 * This refinement operator is an extended version of "TreeFlatRefinement":
 * It adds a new set of refinements: add vertices "in-the-middle-of" an edge, 
 * in order to make the refinement operator complete when paired with trans-subsumption.
 * 
 */
public class TreeFlatTransRefinement extends RefinementOperatorUsingFlatLabels implements IncrementalRefinementOperator {

    // since trans refinement basically generates all the refinements of flat refinement
    // plus some additional ones, we use the flat refinement operator as a starting point:
    TreeFlatRefinement m_flat_rho = null;
    
    // internal state of incremental downward refinement:
    TreeDLG dw_currentDLG = null;
    int dw_incremental_stage = 0;
    int dw_stage_1_next_vertex = 0;
    int dw_stage_1_next_vlabel = 0;
    
    // internal state for incremental upward refinement:
    TreeDLG uw_currentDLG = null;
    int uw_incremental_stage = 0;
    int uw_stage_1_next_vertex = 0;
    
    public DLG getTop()
    {
        return new TreeDLG(0);
    }
    
    
    public TreeFlatTransRefinement(List<Label> rl, List<Label> el) {
        super(rl, el);
        
        m_flat_rho = new TreeFlatRefinement(rl, el);
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
        m_flat_rho.setDLGForDownwardRefinement(g);
        dw_currentDLG = (TreeDLG)g;
        dw_incremental_stage = 0;
        dw_stage_1_next_vertex = 0;
        dw_stage_1_next_vlabel = 0;
    }

    
    public DLG getNextDownwardRefinement() {
        int n = dw_currentDLG.getNVertices();
        switch(dw_incremental_stage) {
            case 0: // use flat refinement:
                {
                    DLG g2 = m_flat_rho.getNextDownwardRefinement();
                    if (g2==null) {
                        dw_incremental_stage = 1;
                        return getNextDownwardRefinement();
                    }
                    return g2;
                }
                
            case 1: // add vertices in between edges:
                {
                    if (dw_stage_1_next_vertex>=n ||
                        vertexLabels.isEmpty()) return null;
                
                    int parent = dw_currentDLG.getParent(dw_stage_1_next_vertex);
                    if (parent==-1) {
                        dw_stage_1_next_vertex++;
                        dw_stage_1_next_vlabel = 0;
                        return getNextDownwardRefinement();
                    }
                    Label vl = vertexLabels.get(dw_stage_1_next_vlabel);
                    Label el = dw_currentDLG.getEdge(parent, dw_stage_1_next_vertex);
                    
                    {
                        int parentOfParent = dw_currentDLG.getParent(parent);
                        if (parentOfParent!=-1 &&
                            el.equals(dw_currentDLG.getEdge(parentOfParent, parent)) &&
                            vl.equals(dw_currentDLG.getVertex(parent))) {
                            // this refinement will be redundant, since it'll be equivalent to splitting the edge of the parent:
                            dw_stage_1_next_vlabel++;
                            if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                                dw_stage_1_next_vlabel++;
                                if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                                    dw_stage_1_next_vlabel = 0;
                                    dw_stage_1_next_vertex++;
                                }
                            }
                            return getNextDownwardRefinement();
                        }
                    }                    
                    
                    if (dw_currentDLG.getChildren(dw_stage_1_next_vertex).length==0 &&
                         vl.equals(dw_currentDLG.getVertex(dw_stage_1_next_vertex))) {
                        // this refinement will be redundant, since it'll be equivalent to just adding a new leaf/root:
                        dw_stage_1_next_vlabel++;
                        if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                            dw_stage_1_next_vlabel++;
                            if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                                dw_stage_1_next_vlabel = 0;
                                dw_stage_1_next_vertex++;
                            }
                        }
                        return getNextDownwardRefinement();
                    }
                    
                    TreeDLG g2 = new TreeDLG(n+1);
                    for(int i = 0;i<n;i++) {
                        g2.setVertex(i, dw_currentDLG.getVertex(i));
                        for(int j = 0;j<n;j++) {
                            g2.setEdge(i,j, dw_currentDLG.getEdge(i,j));
                        }
                    }
                    g2.setEdge(parent, dw_stage_1_next_vertex, null);
                    g2.setVertex(n, vl);
                    g2.setEdge(parent, n, el);
                    g2.setEdge(n, dw_stage_1_next_vertex, el);

                    dw_stage_1_next_vlabel++;
                    if (dw_stage_1_next_vlabel>=vertexLabels.size()) {
                        dw_stage_1_next_vlabel = 0;
                        dw_stage_1_next_vertex++;
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
        m_flat_rho.setDLGForUpwardRefinement(g);
        uw_currentDLG = (TreeDLG)g;
        uw_incremental_stage = 0;
        uw_stage_1_next_vertex = 0;
    }

    
    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case 0: // use flat refinement:
                {
                    DLG g2 = m_flat_rho.getNextUpwardRefinement();
                    if (g2==null) {
                        uw_incremental_stage = 1;
                        uw_stage_1_next_vertex = 0;
                        return getNextUpwardRefinement();
                    }
                    return g2;
                }
              
            case 1: // shorten edges:
            {
                if (uw_stage_1_next_vertex>=n) return null;
                
                int parent = uw_currentDLG.getParent(uw_stage_1_next_vertex);
                if (parent == -1 || 
                    uw_currentDLG.getChildren(uw_stage_1_next_vertex).length!=1) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }
                int child = uw_currentDLG.getChildren(uw_stage_1_next_vertex)[0];
                if (!uw_currentDLG.getEdge(parent, uw_stage_1_next_vertex).equals(
                        uw_currentDLG.getEdge(uw_stage_1_next_vertex, child))) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }
                
                {
                    int parentOfParent = uw_currentDLG.getParent(parent);
                    if (parentOfParent!=-1 &&
                        uw_currentDLG.getEdge(parentOfParent, parent).equals(
                            uw_currentDLG.getEdge(parent, uw_stage_1_next_vertex)) &&
                        uw_currentDLG.getVertex(parent).equals(uw_currentDLG.getVertex(parentOfParent))) {
                        // this will be a redundant refinement, since it's equivalent to removing the parent
                        uw_stage_1_next_vertex++;
                        return getNextUpwardRefinement();
                    }
                }
                
                DLG g2 = GraphFilter.removeVertexLinkingNeighbors(uw_currentDLG, uw_stage_1_next_vertex);
                uw_stage_1_next_vertex++;
                return g2;                                    
            }
            
        }
        return null;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.base;

import dlg.core.DLG;
import dlg.core.PartialOrder;
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
 * This refinement operator is an extended version of "PORefinement":
 * It adds a new set of refinements: add vertices "in-the-middle-of" an edge, 
 * in order to make the refinement operator complete when paired with trans-subsumption.
 * 
 */
public class POTransRefinement extends RefinementOperator implements IncrementalRefinementOperator {

    List<Label> vertexTopLabels = new ArrayList<>();
    List<Label> edgeTopLabels = new ArrayList<>();
    PartialOrder partialOrder = null;
    
    // since trans refinement basically generates all the refinements of flat refinement
    // plus some additional ones, we use the flat refinement operator as a starting point:
    PORefinement m_po_rho = null;
    
    // internal state of incremental downward refinement:
    DLG dw_currentDLG = null;
    int dw_incremental_stage = 0;
    int dw_stage_1_next_vertex1 = 0;
    int dw_stage_1_next_vertex2 = 0;
    int dw_stage_1_next_vlabel = 0;
    
    // internal state for incremental upward refinement:
    DLG uw_currentDLG = null;
    int uw_incremental_stage = 0;
    int uw_stage_1_next_vertex = 0;
    
    public DLG getTop()
    {
        return new DLG(0);
    }
    

    public POTransRefinement( PartialOrder po) {
        vertexTopLabels.add(po.getTop());
        edgeTopLabels.add(po.getTop());
        partialOrder = po;
        
        m_po_rho = new PORefinement(po);
    }

    
    public POTransRefinement(List<Label> rl, List<Label> el, PartialOrder po) {
        vertexTopLabels.addAll(rl);
        edgeTopLabels.addAll(el);
        partialOrder = po;
        
        m_po_rho = new PORefinement(rl, el, po);
    }
    
    
    public List<DLG> downwardRefinements(DLG g) {
        List<DLG> refinements = new ArrayList<>();

        setDLGForDownwardRefinement(g);
        while(true) {
            DLG g2 = getNextDownwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }

    
    public void setDLGForDownwardRefinement(DLG g) {
        m_po_rho.setDLGForDownwardRefinement(g);
        dw_currentDLG = g;
        dw_incremental_stage = 0;
        dw_stage_1_next_vertex1 = 0;
        dw_stage_1_next_vertex2 = 0;
        dw_stage_1_next_vlabel = 0;
    }

    
    public DLG getNextDownwardRefinement() {
        int n = dw_currentDLG.getNVertices();
        switch(dw_incremental_stage) {
            case 0: // use po refinement:
                {
                    DLG g2 = m_po_rho.getNextDownwardRefinement();
                    if (g2==null) {
                        dw_incremental_stage = 1;
                        return getNextDownwardRefinement();
                    }
                    return g2;
                }
                
            case 1: // add vertices in between edges:
                {
                    if (dw_stage_1_next_vertex1>=n ||
                        vertexTopLabels.isEmpty()) return null;

                    Label el = dw_currentDLG.getEdge(dw_stage_1_next_vertex1, 
                                                    dw_stage_1_next_vertex2);
                    if (el==null) {
                        dw_stage_1_next_vertex2++;
                        if (dw_stage_1_next_vertex2>=n) {
                            dw_stage_1_next_vertex1++;
                            dw_stage_1_next_vertex2 = 0;
                        }
                        dw_stage_1_next_vlabel = 0;
                        return getNextDownwardRefinement();
                    }
                    Label vl = vertexTopLabels.get(dw_stage_1_next_vlabel);
                    
                    {
                        int parent = -1;
                        int parents[] = dw_currentDLG.getCondensedIncomingEdges()[dw_stage_1_next_vertex1];
                        if (parents.length==1) parent = parents[0];
                        if (parent!=-1 &&
                            el.equals(dw_currentDLG.getEdge(parent, dw_stage_1_next_vertex1)) &&
                            vl.equals(dw_currentDLG.getVertex(dw_stage_1_next_vertex1))) {
                            // this refinement will be redundant, since it'll be equivalent to splitting the edge of the parent:
                            dw_stage_1_next_vlabel++;
                            if (dw_stage_1_next_vlabel>=vertexTopLabels.size()) {
                                dw_stage_1_next_vlabel = 0;
                                dw_stage_1_next_vertex2++;
                                if (dw_stage_1_next_vertex2>=n) {
                                    dw_stage_1_next_vertex1++;
                                    dw_stage_1_next_vertex2 = 0;
                                }
                            }
                            return getNextDownwardRefinement();
                        }
                    }
                    if (dw_currentDLG.getCondensedOutgoingEdges()[dw_stage_1_next_vertex2].length==0 &&
                        dw_currentDLG.getCondensedIncomingEdges()[dw_stage_1_next_vertex2].length==1 &&
                        vl.equals(dw_currentDLG.getVertex(dw_stage_1_next_vertex2))) {
                       // this refinement will be redundant, since it'll be equivalent to just adding a new leaf:
                       dw_stage_1_next_vlabel++;
                       if (dw_stage_1_next_vlabel>=vertexTopLabels.size()) {
                           dw_stage_1_next_vlabel = 0;
                           dw_stage_1_next_vertex2++;
                           if (dw_stage_1_next_vertex2>=n) {
                               dw_stage_1_next_vertex1++;
                               dw_stage_1_next_vertex2 = 0;
                           }
                       }
                       return getNextDownwardRefinement();
                    }
                    
                    DLG g2 = new DLG(n+1);
                    for(int i = 0;i<n;i++) {
                        g2.setVertex(i, dw_currentDLG.getVertex(i));
                        for(int j = 0;j<n;j++) {
                            g2.setEdge(i,j, dw_currentDLG.getEdge(i,j));
                        }
                    }
                    g2.setEdge(dw_stage_1_next_vertex1, dw_stage_1_next_vertex2, null);
                    g2.setVertex(n, vl);
                    g2.setEdge(dw_stage_1_next_vertex1, n, el);
                    g2.setEdge(n, dw_stage_1_next_vertex2, el);

                    dw_stage_1_next_vlabel++;
                    if (dw_stage_1_next_vlabel>=vertexTopLabels.size()) {
                        dw_stage_1_next_vlabel = 0;
                        dw_stage_1_next_vertex2++;
                        if (dw_stage_1_next_vertex2>=n) {
                            dw_stage_1_next_vertex1++;
                            dw_stage_1_next_vertex2 = 0;
                        }
                    }

                    return g2;
                }
        }        
        return null;
    }


    public List<? extends DLG> upwardRefinements(DLG g) throws Exception {
        List<DLG> refinements = new ArrayList<>();

        setDLGForUpwardRefinement(g);
        while(true) {
            DLG g2 = getNextUpwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }

    
    public void setDLGForUpwardRefinement(DLG g) throws Exception {
        m_po_rho.setDLGForUpwardRefinement(g);
        uw_currentDLG = g;
        uw_incremental_stage = 0;
        uw_stage_1_next_vertex = 0;
    }

    
    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case 0: // shorten edges:
            {
                if (uw_stage_1_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();                    
                }
                
                // we can only eliminate vertices with a top label:
                Label vl = uw_currentDLG.getVertex(uw_stage_1_next_vertex);
                if (!vertexTopLabels.contains(vl)) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }                
                int parent = -1;
                int child = -1;
                if (uw_currentDLG.getCondensedIncomingEdges()[uw_stage_1_next_vertex].length==1) {
                    parent = uw_currentDLG.getCondensedIncomingEdges()[uw_stage_1_next_vertex][0];
                }
                if (uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_1_next_vertex].length==1) {
                    child = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_1_next_vertex][0];
                }
                    
                if (parent == -1 || child == -1) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }
                               
                if (!uw_currentDLG.getEdge(parent, uw_stage_1_next_vertex).equals(
                        uw_currentDLG.getEdge(uw_stage_1_next_vertex, child))) {
                    uw_stage_1_next_vertex++;
                    return getNextUpwardRefinement();
                }

                {
                    int parentOfParent = -1;
                    if (uw_currentDLG.getCondensedIncomingEdges()[parent].length==1) {
                        parentOfParent = uw_currentDLG.getCondensedIncomingEdges()[parent][0];
                    }
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
            case 1: // use po refinement:
                {
                    DLG g2 = m_po_rho.getNextUpwardRefinement();
                    if (g2==null) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }
                    return g2;
                }
            default: return null;
        }
    }
    
}

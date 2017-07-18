/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.base;

import dlg.core.DLG;
import dlg.core.operations.Connectivity;
import dlg.core.operations.GraphFilter;
import dlg.core.refinement.IncrementalRefinementOperator;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;
import dlg.util.Pair;

/**
 *
 * @author santi
 */
public class FlatRefinement extends RefinementOperatorUsingFlatLabels implements IncrementalRefinementOperator {

    public static int DEBUG = 0;
    
    // order in which the refinements occur (which might have a strong impact on performance):
    public final int DW_STAGE_EMPTY_GRAPH = 2;
    public final int DW_STAGE_ADD_VERTEX = 1;
    public final int DW_STAGE_ADD_EDGE = 0;
    
    // order in which the refinements occur (which might have a strong impact on performance):
    public final int UW_STAGE_REMOVE_NON_BRIDGE_EDGE = 1;
    public final int UW_STAGE_REMOVE_LEAF = 0;
    
    // internal state of incremental downward refinement:
    DLG dw_currentDLG = null;
    int dw_incremental_stage = 0;
    int dw_stage_EG_next_label = 0;
    int dw_stage_AV_next_vertex = 0;
    int dw_stage_AV_next_vlabel = 0;
    int dw_stage_AV_next_elabel = 0;
    boolean dw_stage_AV_incoming = true;
    int dw_stage_AE_next_vertex1 = 0;
    int dw_stage_AE_next_vertex2 = 0;
    int dw_stage_AE_next_elabel = 0;
    
    
    // internal state of incremental upward refinement:
    DLG uw_currentDLG = null;
    List<Pair<Integer,Integer>> uw_stage_0_bridges = null;
    int uw_incremental_stage = 0;
    int uw_stage_0_next_vertex1 = 0;
    int uw_stage_0_next_vertex2 = 0;
    int uw_stage_1_next_vertex = 0;

    
    public DLG getTop()
    {
        return new DLG(0);
    }
    
    
    public FlatRefinement(List<Label> rl, List<Label> el) {
        super(rl, el);
    }
    
    
    public List<DLG> upwardRefinements(DLG g) throws Exception {
        List<DLG> refinements = new ArrayList<>();

        setDLGForUpwardRefinement(g);
        while(true) {
            DLG g2 = getNextUpwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }
    
       
    public List<DLG> downwardRefinements(DLG g) throws Exception {
        List<DLG> refinements = new ArrayList<>();

        setDLGForDownwardRefinement(g);
        while(true) {
            DLG g2 = getNextDownwardRefinement();
            if (g2==null) return refinements;
            refinements.add(g2);
        }
    }

    
    public void setDLGForDownwardRefinement(DLG g) {
        dw_currentDLG = g;
        dw_incremental_stage = 0;
        dw_stage_EG_next_label = 0;
        dw_stage_AV_next_vertex = 0;
        dw_stage_AV_next_vlabel = 0;
        dw_stage_AV_next_elabel = 0;
        dw_stage_AV_incoming = true;
        dw_stage_AE_next_vertex1 = 0;
        dw_stage_AE_next_vertex1 = 0;
        dw_stage_AE_next_elabel = 0;
    }

    
    public DLG getNextDownwardRefinement() {
        int n = dw_currentDLG.getNVertices();
        switch(dw_incremental_stage) {
            case DW_STAGE_EMPTY_GRAPH: // add a vertex to an empty graph:
                {
                    if (n>0 || dw_stage_EG_next_label>=vertexLabels.size()) {
                        dw_incremental_stage++;
                        dw_stage_AV_incoming = true;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexLabels.get(dw_stage_EG_next_label);
                    dw_stage_EG_next_label++;
                    DLG g2 = new DLG(1);
                    g2.setVertex(0,l);
                    if (DEBUG>=1) System.out.println("generated refinement by DW_STAGE_EMPTY_GRAPH");
                    return g2;
                }

            case DW_STAGE_ADD_VERTEX: // add vertices
                {
                    if (dw_stage_AV_next_vertex>=n || edgeLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexLabels.get(dw_stage_AV_next_vlabel);
                    Label l2 = edgeLabels.get(dw_stage_AV_next_elabel);

                    DLG g2 = new DLG(n+1);            
                    for(int j = 0;j<n;j++) g2.setVertex(j, dw_currentDLG.getVertex(j));
                    g2.setVertex(n, l);

                    for(int j = 0;j<n;j++) {
                        for(int k = 0;k<n;k++) {
                            g2.setEdge(j, k, dw_currentDLG.getEdge(j, k));
                        }
                    }
                    if (dw_stage_AV_incoming) {
                        g2.setEdge(dw_stage_AV_next_vertex, n, l2);
                    } else {
                        g2.setEdge(n, dw_stage_AV_next_vertex, l2);
                    }
                    
                    if (dw_stage_AV_incoming) {
                        dw_stage_AV_incoming = false;
                    } else {            
                        dw_stage_AV_incoming = true;
                        dw_stage_AV_next_elabel++;
                        if (dw_stage_AV_next_elabel>=edgeLabels.size()) {
                            dw_stage_AV_next_elabel = 0;
                            dw_stage_AV_next_vlabel++;
                        }
                        if (dw_stage_AV_next_vlabel>=vertexLabels.size()) {
                            dw_stage_AV_next_vlabel = 0;
                            dw_stage_AV_next_vertex++;
                        }
                    }
                    
                    return g2;
                }
                
            case DW_STAGE_ADD_EDGE: // add edges:
                {          
                    if (dw_stage_AE_next_vertex1>=n || edgeLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }

                    if (dw_currentDLG.getEdge(dw_stage_AE_next_vertex1, 
                                              dw_stage_AE_next_vertex2)==null) {
                        Label l = edgeLabels.get(dw_stage_AE_next_elabel);
                        DLG g2 = new DLG(dw_currentDLG);
                        g2.setEdge(dw_stage_AE_next_vertex1, dw_stage_AE_next_vertex2, l);
                        
                        dw_stage_AE_next_elabel++;
                        if (dw_stage_AE_next_elabel>=edgeLabels.size()) {
                            dw_stage_AE_next_elabel = 0;
                            dw_stage_AE_next_vertex2++;
                        }
                        if (dw_stage_AE_next_vertex2>=n) {
                            dw_stage_AE_next_vertex2 = 0;
                            dw_stage_AE_next_vertex1++;
                        }
                        
                        return g2;
                    } else {
                        dw_stage_AE_next_elabel = 0;
                        dw_stage_AE_next_vertex2++;
                        if (dw_stage_AE_next_vertex2>=n) {
                            dw_stage_AE_next_vertex2 = 0;
                            dw_stage_AE_next_vertex1++;
                        }
                        return getNextDownwardRefinement();
                    }
                }
            default: return null;
        }        
    }
    
    
    public void setDLGForUpwardRefinement(DLG g) throws Exception {
        uw_currentDLG = g;
        
        // 1) identify edges that when removed do not disconnect the graph
        uw_stage_0_bridges = Connectivity.getBridges(g);
        uw_incremental_stage = 0;
        uw_stage_0_next_vertex1 = 0;
        uw_stage_0_next_vertex2 = 0;
        uw_stage_1_next_vertex = 0;
        
        if (DEBUG>=1) System.out.println("setDLGForUpwardRefinement: bridges found: " + uw_stage_0_bridges);
    }    
    

    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case UW_STAGE_REMOVE_NON_BRIDGE_EDGE: // remove a non-bridge edge:
                {
                    while(uw_stage_0_next_vertex1<n) {
                        Label e = uw_currentDLG.getEdge(uw_stage_0_next_vertex1, uw_stage_0_next_vertex2);
                        boolean isBridge = false;
                        if (e!=null) {
                            for(Pair<Integer,Integer> b:uw_stage_0_bridges) {
                                if ((b.m_a == uw_stage_0_next_vertex1 &&
                                     b.m_b == uw_stage_0_next_vertex2) ||
                                    (b.m_a == uw_stage_0_next_vertex2 &&
                                     b.m_b == uw_stage_0_next_vertex1)) {
                                    isBridge = true;
                                    break;
                                }
                            }
                        }

                        if (DEBUG>=1) System.out.println("getNextUpwardRefinement stage 0: " + uw_stage_0_next_vertex1 + "-" + uw_stage_0_next_vertex2 + 
                                                         "(" + e + "), is bridge: " + isBridge);

                        if (e==null || isBridge) {
                            uw_stage_0_next_vertex2++;
                            if (uw_stage_0_next_vertex2>=n) {
                                uw_stage_0_next_vertex1++;
                                uw_stage_0_next_vertex2 = 0;
                            }
                            continue;
                        }

                        DLG g2 = new DLG(uw_currentDLG);
                        g2.setEdge(uw_stage_0_next_vertex1, uw_stage_0_next_vertex2, null);
                        if (DEBUG>=1) System.out.println("getNextUpwardRefinement: from stage 0");

                        uw_stage_0_next_vertex2++;
                        if (uw_stage_0_next_vertex2>=n) {
                            uw_stage_0_next_vertex1++;
                            uw_stage_0_next_vertex2 = 0;
                        }
                        return g2;
                    }
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();
                }
            case UW_STAGE_REMOVE_LEAF:
                {
                    if (uw_stage_1_next_vertex >= n) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }

                    int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_1_next_vertex].length + 
                                 uw_currentDLG.getCondensedIncomingEdges()[uw_stage_1_next_vertex].length;
                    
                    if (nedges>1) {
                        uw_stage_1_next_vertex++;
                        return getNextUpwardRefinement();
                    }
                
                    // remove leaf:
                    DLG g2 = GraphFilter.removeVertex(uw_currentDLG, uw_stage_1_next_vertex);
                    if (DEBUG>=1) System.out.println("getNextUpwardRefinement: from stage 1 (remove leaf " + uw_stage_1_next_vertex + ")");

                    uw_stage_1_next_vertex++;                
                    return g2;                    
                }
            default: return null;
        }
    }
    
}

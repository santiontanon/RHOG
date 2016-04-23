/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.filtered;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.operations.Connectivity;
import dlg.core.operations.GraphFilter;
import dlg.core.refinement.IncrementalRefinementOperator;
import dlg.core.refinement.RefinementOperator;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;
import dlg.util.Pair;
import java.util.HashMap;

/**
 *
 * @author santi
 */
public class PORefinementFiltered extends RefinementOperator implements IncrementalRefinementOperator {

    public static int DEBUG = 0;
    
    public final int DW_STAGE_EMPTY_GRAPH = 0;
    public final int DW_STAGE_ADD_VERTEX = 4;
    public final int DW_STAGE_ADD_EDGE = 3;
    public final int DW_STAGE_SPECIALIZE_VERTEX = 2;
    public final int DW_STAGE_SPECIALIZE_EDGE = 1;
    
    List<Label> vertexTopLabels = new ArrayList<>();
    List<Label> edgeTopLabels = new ArrayList<>();
    PartialOrder partialOrder = null;
    
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
    int dw_stage_SV_next_vertex = 0;
    int dw_stage_SV_next_label = 0;
    int dw_stage_SE_next_vertex1 = 0;
    int dw_stage_SE_next_vertex2 = 0;
    int dw_stage_SE_next_label = 0;
    
    
    // internal state of incremental upward refinement:
    DLG uw_currentDLG = null;
    List<Pair<Integer,Integer>> uw_stage_RNBE_bridges = null;
    int uw_incremental_stage = 0;
    int uw_stage_RNBE_next_vertex1 = 0;
    int uw_stage_RNBE_next_vertex2 = 0;
    int uw_stage_RTL_next_vertex = 0;
    int uw_stage_GETL_next_vertex1 = 0;
    int uw_stage_GETL_next_vertex2 = 0;
    int uw_stage_GETL_next_label = 0;
    int uw_stage_GVL_next_vertex = 0;
    int uw_stage_GVL_next_label = 0;
    int uw_stage_GENTL_next_vertex1 = 0;
    int uw_stage_GENTL_next_vertex2 = 0;
    int uw_stage_GENTL_next_label = 0;    
    int uw_stage_GVNL_next_vertex = 0;
    int uw_stage_GVNL_next_label = 0;
    
    // extracted from sample DLGs to filter the number of refinements:
    HashMap<Label, List<Label>> outgoingEdgeTopLabels = new HashMap<>();
    HashMap<Label, List<Label>> incomingEdgeTopLabels = new HashMap<>();
    
    public DLG getTop()
    {
        return new DLG(0);
    }
    
 
    public PORefinementFiltered(PartialOrder po, List<DLG> baseDLGs) {
        vertexTopLabels.add(po.getTop());
        edgeTopLabels.add(po.getTop());
        partialOrder = po;
        
        inferLabels(baseDLGs);
    }

    
    public PORefinementFiltered(List<Label> vl, List<Label> el, PartialOrder po, List<DLG> baseDLGs) {
        if (vl==null) {
            vertexTopLabels.add(po.getTop());
        } else {
            vertexTopLabels.addAll(vl);
        }
        if (el==null) {
            edgeTopLabels.add(po.getTop());
        } else {
            edgeTopLabels.addAll(el);
        }
        partialOrder = po;

        inferLabels(baseDLGs);
    }

    
    public void inferLabels(List<DLG> baseDLGs) {
        for(DLG g: baseDLGs) {
            for(int v = 0;v<g.getNVertices();v++) {
                Label vl = g.getVertex(v);
                List<Label> vTopLabels = outgoingEdgeTopLabels.get(vl);
                if (vTopLabels==null) {
                    vTopLabels = new ArrayList<>();
                    outgoingEdgeTopLabels.put(vl, vTopLabels);
                }                
                for(int v2:g.getCondensedOutgoingEdges()[v]) {
                    Label ve = g.getEdge(v, v2);
                    updateTopLabelList(ve, vTopLabels);
                }
                
                vTopLabels = incomingEdgeTopLabels.get(vl);
                if (vTopLabels==null) {
                    vTopLabels = new ArrayList<>();
                    incomingEdgeTopLabels.put(vl, vTopLabels);
                }                
                for(int v2:g.getCondensedIncomingEdges()[v]) {
                    Label ve = g.getEdge(v2, v);
                    updateTopLabelList(ve, vTopLabels);
                }
            }
        }
        
        // propagate the labels up the partial order, to ensure the operator is complete:
        List<Label> tmp = new ArrayList<>();
        tmp.addAll(outgoingEdgeTopLabels.keySet());
        for(Label vl:tmp) {
            for(Label parent:partialOrder.getAncestors(vl)) {
                List<Label> pTopLabels = outgoingEdgeTopLabels.get(parent);
                if (pTopLabels==null) {
                    pTopLabels = new ArrayList<>();
                    outgoingEdgeTopLabels.put(parent, pTopLabels);
                }                
                for(Label el:outgoingEdgeTopLabels.get(vl)) {
                    updateTopLabelList(el, pTopLabels);
                }
            }
        }        
        tmp = new ArrayList<>();
        tmp.addAll(incomingEdgeTopLabels.keySet());
        for(Label vl:tmp) {
            for(Label parent:partialOrder.getAncestors(vl)) {
                List<Label> pTopLabels = incomingEdgeTopLabels.get(parent);
                if (pTopLabels==null) {
                    pTopLabels = new ArrayList<>();
                    incomingEdgeTopLabels.put(parent, pTopLabels);
                }                
                for(Label el:incomingEdgeTopLabels.get(vl)) {
                    updateTopLabelList(el, pTopLabels);
                }
            }
        }        
        
        if (DEBUG>=1) {
            for(Label l:outgoingEdgeTopLabels.keySet()) {
                System.out.println(l + " -> " + outgoingEdgeTopLabels.get(l));
            }
            for(Label l:incomingEdgeTopLabels.keySet()) {
                System.out.println(l + " <- " + incomingEdgeTopLabels.get(l));
            }
        }
    }
    
    
    public void updateTopLabelList(Label label, List<Label> labelList) {
        List<Label> toDelete = new ArrayList<>();
        for(Label l:labelList) {
            // if we already have a more general label, then we don't need to do anything:
            if (partialOrder.subsumes(l, label)) return;
            if (partialOrder.subsumes(label, l)) {
                toDelete.add(l);
            }
        }
        labelList.removeAll(toDelete);
        labelList.add(label);
    }
    
    
    public boolean passesOutgoingFilter(Label vertexLabel, Label edgeLabel) {
        List<Label> l = outgoingEdgeTopLabels.get(vertexLabel);
        if (l!=null) {
            for(Label tl:l) {
                if (partialOrder.subsumes(tl, edgeLabel) || 
                    partialOrder.subsumes(edgeLabel, tl)) return true;
            }
        }
        return false;
    }
    

    public boolean passesIncomingFilter(Label vertexLabel, Label edgeLabel) {
        List<Label> l = incomingEdgeTopLabels.get(vertexLabel);
        if (l!=null) {
            for(Label tl:l) {
                if (partialOrder.subsumes(tl, edgeLabel) || 
                    partialOrder.subsumes(edgeLabel, tl)) return true;
            }
        }
        return false;
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
        dw_stage_SV_next_vertex = 0;
        dw_stage_SV_next_label = 0;
        dw_stage_SE_next_vertex1 = 0;
        dw_stage_SE_next_vertex2 = 0;
        dw_stage_SE_next_label = 0;
    }

    
    public DLG getNextDownwardRefinement() {
        int n = dw_currentDLG.getNVertices();
        switch(dw_incremental_stage) {
            case DW_STAGE_EMPTY_GRAPH: // add a vertex to an empty graph:
                {
                    if (n>0 || dw_stage_EG_next_label>=vertexTopLabels.size()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexTopLabels.get(dw_stage_EG_next_label);
                    dw_stage_EG_next_label++;
                    DLG g2 = new DLG(1);
                    g2.setVertex(0,l);
                    return g2;
                }

            case DW_STAGE_ADD_VERTEX: // add vertices
                {
                    if (dw_stage_AV_next_vertex>=n || edgeTopLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    Label vl = vertexTopLabels.get(dw_stage_AV_next_vlabel);
                    Label el = edgeTopLabels.get(dw_stage_AV_next_elabel);

                    DLG g2 = null;
                    
                    // filter refinements:
                    Label v1l, v2l;
                    if (dw_stage_AV_incoming) {
                        v1l = dw_currentDLG.getVertex(dw_stage_AV_next_vertex);
                        v2l = vl;
                        if (DEBUG>=1) System.out.println("PORefinementFiltered.getNextDownwardRefinement (ADD_VERTEX): considering " + dw_stage_AV_next_vertex + " --(" + el + ")--> " + vl);
                    } else {
                        v1l = vl;
                        v2l = dw_currentDLG.getVertex(dw_stage_AV_next_vertex);
                        if (DEBUG>=1) System.out.println("PORefinementFiltered.getNextDownwardRefinement (ADD_VERTEX): considering " + dw_stage_AV_next_vertex + " <--(" + el + ")-- " + vl);
                    }
                    if (passesOutgoingFilter(v1l, el) &&
                        passesIncomingFilter(v2l, el)) {
                        g2 = new DLG(n+1);            
                        for(int j = 0;j<n;j++) g2.setVertex(j, dw_currentDLG.getVertex(j));
                        g2.setVertex(n, vl);

                        for(int j = 0;j<n;j++) {
                            for(int k = 0;k<n;k++) {
                                g2.setEdge(j, k, dw_currentDLG.getEdge(j, k));
                            }
                        }
                        if (dw_stage_AV_incoming) {
                            g2.setEdge(dw_stage_AV_next_vertex, n, el);
                        } else {
                            g2.setEdge(n, dw_stage_AV_next_vertex, el);
                        }
                    } else {
                        if (DEBUG>=1) {
                            System.out.println("PORefinementFiltered.getNextDownwardRefinement (ADD_VERTEX): filtered.");
                            System.out.println("  outgoing filter test: " + passesOutgoingFilter(v1l, el));
                            System.out.println("  incoming filter test: " + passesIncomingFilter(v2l, el));
                        }
                    }
                    
                    if (dw_stage_AV_incoming) {
                        dw_stage_AV_incoming = false;
                    } else {            
                        dw_stage_AV_incoming = true;
                        dw_stage_AV_next_elabel++;
                        if (dw_stage_AV_next_elabel>=edgeTopLabels.size()) {
                            dw_stage_AV_next_elabel = 0;
                            dw_stage_AV_next_vlabel++;
                        }
                        if (dw_stage_AV_next_vlabel>=vertexTopLabels.size()) {
                            dw_stage_AV_next_vlabel = 0;
                            dw_stage_AV_next_vertex++;
                        }
                    }
                    
                    if (g2==null) return getNextDownwardRefinement();
                    return g2;
                }
                
            case DW_STAGE_ADD_EDGE: // add edges:
                {          
                    DLG g2 = null;
                    do{          
                        if (dw_stage_AE_next_vertex1>=n || edgeTopLabels.isEmpty()) {
                            dw_incremental_stage++;
                            return getNextDownwardRefinement();
                        }

                        Label el = edgeTopLabels.get(dw_stage_AE_next_elabel);
                        Label existing_l = dw_currentDLG.getEdge(dw_stage_AE_next_vertex1, 
                                                         dw_stage_AE_next_vertex2);
                        if (existing_l==null &&
                            passesOutgoingFilter(dw_currentDLG.getVertex(dw_stage_AE_next_vertex1), el) &&
                            passesIncomingFilter(dw_currentDLG.getVertex(dw_stage_AE_next_vertex2), el)) {
                            g2 = new DLG(dw_currentDLG);
                            g2.setEdge(dw_stage_AE_next_vertex1, dw_stage_AE_next_vertex2, el);

                            dw_stage_AE_next_elabel++;
                            if (dw_stage_AE_next_elabel>=edgeTopLabels.size()) {
                                dw_stage_AE_next_elabel = 0;
                                dw_stage_AE_next_vertex2++;
                            }
                            if (dw_stage_AE_next_vertex2>=n) {
                                dw_stage_AE_next_vertex2 = 0;
                                dw_stage_AE_next_vertex1++;
                            }

                        } else {
                            dw_stage_AE_next_elabel = 0;
                            dw_stage_AE_next_vertex2++;
                            if (dw_stage_AE_next_vertex2>=n) {
                                dw_stage_AE_next_vertex2 = 0;
                                dw_stage_AE_next_vertex1++;
                            }
                        }
                    } while(g2==null);
                    return g2;
                }
           case DW_STAGE_SPECIALIZE_VERTEX: // specialize vertex:
                {
                    DLG g2 = null;
                    do{          
                        if (dw_stage_SV_next_vertex>=n) {
                            dw_incremental_stage++;
                            return getNextDownwardRefinement();
                        }

                        Label existing_vl = dw_currentDLG.getVertex(dw_stage_SV_next_vertex);
                        Label candidates[] = partialOrder.getChildren(existing_vl);
                        while(dw_stage_SV_next_label >= candidates.length) {
                            dw_stage_SV_next_vertex++;
                            dw_stage_SV_next_label = 0;                    
                            if (dw_stage_SV_next_vertex>=n) {
                                dw_incremental_stage++;
                                return getNextDownwardRefinement();
                            }
                            existing_vl = dw_currentDLG.getVertex(dw_stage_SV_next_vertex);
                            candidates= partialOrder.getChildren(existing_vl);
                        }

                        boolean passesFilter = true;
                        Label vl = candidates[dw_stage_SV_next_label];
                        for(int v1:dw_currentDLG.getCondensedIncomingEdges()[dw_stage_SV_next_vertex]) {
                            Label el = dw_currentDLG.getEdge(v1, dw_stage_SV_next_vertex);
                            if (!passesIncomingFilter(vl, el)) {
                                passesFilter = false;
                                break;
                            }
                        }
                        if (passesFilter) {
                            for(int v2:dw_currentDLG.getCondensedOutgoingEdges()[dw_stage_SV_next_vertex]) {
                                Label el = dw_currentDLG.getEdge(dw_stage_SV_next_vertex, v2);
                                if (!passesOutgoingFilter(vl, el)) {
                                    passesFilter = false;
                                    break;
                                }
                            }
                        }

                        if (passesFilter) {
                            g2 = new DLG(dw_currentDLG);
                            g2.setVertex(dw_stage_SV_next_vertex, vl);
                        }
                        dw_stage_SV_next_label++;
                        if (dw_stage_SV_next_label >= candidates.length) {
                            dw_stage_SV_next_vertex++;
                            dw_stage_SV_next_label = 0;
                        }
                    }while(g2==null);
                    return g2;
                }
            case DW_STAGE_SPECIALIZE_EDGE: // specialize edge:
                {          
                    DLG g2 = null;
                    do{          
                        if (dw_stage_SE_next_vertex1>=n ||
                            edgeTopLabels.isEmpty()) {
                            dw_incremental_stage++;
                            return getNextDownwardRefinement();
                        }

                        Label el = dw_currentDLG.getEdge(dw_stage_SE_next_vertex1, dw_stage_SE_next_vertex2);
                        if (el==null) {
                            dw_stage_SE_next_label = 0;
                            dw_stage_SE_next_vertex2++;
                            if (dw_stage_SE_next_vertex2>=n) {
                                dw_stage_SE_next_vertex2 = 0;
                                dw_stage_SE_next_vertex1++;
                            }
                            continue;
                        }
                        Label candidates[] = partialOrder.getChildren(el);
                        if (dw_stage_SE_next_label >= candidates.length) {
                            dw_stage_SE_next_label = 0;
                            dw_stage_SE_next_vertex2++;
                            if (dw_stage_SE_next_vertex2>=n) {
                                dw_stage_SE_next_vertex2 = 0;
                                dw_stage_SE_next_vertex1++;
                            }
                            continue;
                        }

                        boolean passesFilter = true;
                        if (!passesIncomingFilter(dw_currentDLG.getVertex(dw_stage_SE_next_vertex1), el) ||
                            !passesOutgoingFilter(dw_currentDLG.getVertex(dw_stage_SE_next_vertex2), el)) passesFilter = false;

                        if (passesFilter) {
                            g2 = new DLG(dw_currentDLG);
                            g2.setEdge(dw_stage_SE_next_vertex1, dw_stage_SE_next_vertex2, candidates[dw_stage_SE_next_label]);
                        }

                        dw_stage_SE_next_label++;
                        if (dw_stage_SE_next_label >= candidates.length) {
                            dw_stage_SE_next_label = 0;
                            dw_stage_SE_next_vertex2++;
                            if (dw_stage_SE_next_vertex2>=n) {
                                dw_stage_SE_next_vertex2 = 0;
                                dw_stage_SE_next_vertex1++;
                            }
                        }
                    } while(g2==null);
                    return g2;
                }      
            default: return null;
        }        
    }
    
    
    public void setDLGForUpwardRefinement(DLG g) throws Exception {
        uw_currentDLG = g;
        
        // 1) identify edges that when removed do not disconnect the graph
        uw_stage_RNBE_bridges = Connectivity.getBridges(g);
        uw_incremental_stage = 0;
        uw_stage_RNBE_next_vertex1 = 0;
        uw_stage_RNBE_next_vertex2 = 0;
        uw_stage_RTL_next_vertex = 0;
        uw_stage_GETL_next_vertex1 = 0;
        uw_stage_GETL_next_vertex2 = 0;
        uw_stage_GETL_next_label = 0;
        uw_stage_GVL_next_vertex = 0;
        uw_stage_GVL_next_label = 0;
        uw_stage_GENTL_next_vertex1 = 0;
        uw_stage_GENTL_next_vertex2 = 0;
        uw_stage_GENTL_next_label = 0;    
        uw_stage_GVNL_next_vertex = 0;
        uw_stage_GVNL_next_label = 0;
        
        if (DEBUG>=1) System.out.println("setDLGForUpwardRefinement: bridges found: " + uw_stage_RNBE_bridges);
    }    
    

    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case 0: // remove a non-bridge top edge:
                {
                    if (uw_stage_RNBE_next_vertex1 >= n) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }
                    
                    Label e = null;
                    while(true) {
                        e = uw_currentDLG.getEdge(uw_stage_RNBE_next_vertex1, uw_stage_RNBE_next_vertex2);
                        if (e==null || 
                            !edgeTopLabels.contains(e)) {
                            e = null;
                            uw_stage_RNBE_next_vertex2++;
                            if (uw_stage_RNBE_next_vertex2>=n) break;
                        } else {
                            break;
                        }
                    } 
                    boolean isBridge = false;
                    if (e!=null) {
                        for(Pair<Integer,Integer> b:uw_stage_RNBE_bridges) {
                            if ((b.m_a == uw_stage_RNBE_next_vertex1 &&
                                 b.m_b == uw_stage_RNBE_next_vertex2) ||
                                (b.m_a == uw_stage_RNBE_next_vertex2 &&
                                 b.m_b == uw_stage_RNBE_next_vertex1)) {
                                isBridge = true;
                                break;
                            }
                        }
                    }
                    
                    if (DEBUG>=1) System.out.println("getNextUpwardRefinement stage 0: " + uw_stage_RNBE_next_vertex1 + "-" + uw_stage_RNBE_next_vertex2 + 
                                                     "(" + e + "), is bridge: " + isBridge);
                    
                    if (e==null || isBridge) {
                        uw_stage_RNBE_next_vertex2++;
                        if (uw_stage_RNBE_next_vertex2>=n) {
                            uw_stage_RNBE_next_vertex1++;
                            uw_stage_RNBE_next_vertex2 = 0;
                        }
                        return getNextUpwardRefinement();
                    }
                    
                    DLG g2 = new DLG(uw_currentDLG);
                    g2.setEdge(uw_stage_RNBE_next_vertex1, uw_stage_RNBE_next_vertex2, null);
                    if (DEBUG>=1) System.out.println("getNextUpwardRefinement: from stage 0");
                    
                    uw_stage_RNBE_next_vertex2++;
                    if (uw_stage_RNBE_next_vertex2>=n) {
                        uw_stage_RNBE_next_vertex1++;
                        uw_stage_RNBE_next_vertex2 = 0;
                    }
                    return g2;
                }
            case 1: // remove a top-leaf connected via a top-edge
                {
                    if (uw_stage_RTL_next_vertex >= n) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }

                    int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_RTL_next_vertex].length + 
                                 uw_currentDLG.getCondensedIncomingEdges()[uw_stage_RTL_next_vertex].length;
                    
                    if (nedges>1 || 
                        !vertexTopLabels.contains(uw_currentDLG.getVertex(uw_stage_RTL_next_vertex))) {
                        uw_stage_RTL_next_vertex++;
                        return getNextUpwardRefinement();
                    }
                    
                    boolean filtered = false;
                    for(int v:uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_RTL_next_vertex]) {
                        if (!edgeTopLabels.contains(uw_currentDLG.getEdge(uw_stage_RTL_next_vertex, v))) {
                            filtered = true;
                            break;
                        }
                    }
                    if (!filtered) {
                        for(int v:uw_currentDLG.getCondensedIncomingEdges()[uw_stage_RTL_next_vertex]) {
                            if (!edgeTopLabels.contains(uw_currentDLG.getEdge(v,uw_stage_RTL_next_vertex))) {
                                filtered = true;
                                break;
                            }
                        }
                    }
                    if (filtered) {
                        uw_stage_RTL_next_vertex++;
                        return getNextUpwardRefinement();
                    }
                
                    DLG g2 = GraphFilter.removeVertex(uw_currentDLG, uw_stage_RTL_next_vertex);
                    if (DEBUG>=1) System.out.println("getNextUpwardRefinement: from stage 1");

                    uw_stage_RTL_next_vertex++;                
                    return g2;                    
                }
                
            case 2: // generalize edge to a top-leaf:
                {          
                    if (uw_stage_GETL_next_vertex1>=n ||
                        edgeTopLabels.isEmpty()) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }

                    Label el = uw_currentDLG.getEdge(uw_stage_GETL_next_vertex1, uw_stage_GETL_next_vertex2);
                    while(el==null) {
                        uw_stage_GETL_next_label = 0;
                        uw_stage_GETL_next_vertex2++;
                        if (uw_stage_GETL_next_vertex2>=n) {
                            uw_stage_GETL_next_vertex2 = 0;
                            uw_stage_GETL_next_vertex1++;
                            if (uw_stage_GETL_next_vertex1>=n) {
                                uw_incremental_stage++;
                                return getNextUpwardRefinement();
                            }
                        }
                        el = uw_currentDLG.getEdge(uw_stage_GETL_next_vertex1, uw_stage_GETL_next_vertex2);
                    }
                    Label candidates[] = partialOrder.getParents(el);
                    if (uw_stage_GETL_next_label >= candidates.length) {
                        uw_stage_GETL_next_label = 0;
                        uw_stage_GETL_next_vertex2++;
                        if (uw_stage_GETL_next_vertex2>=n) {
                            uw_stage_GETL_next_vertex2 = 0;
                            uw_stage_GETL_next_vertex1++;
                        }
                        return getNextUpwardRefinement();
                    }
                    
                    Label v1l = uw_currentDLG.getVertex(uw_stage_GETL_next_vertex1);
                    Label v2l = uw_currentDLG.getVertex(uw_stage_GETL_next_vertex2);
                    boolean topleaf1 = true;
                    boolean topleaf2 = true;
                    {
                        int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GETL_next_vertex1].length + 
                                     uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GETL_next_vertex1].length;
                        if (nedges!=1) {
                            topleaf1 = false;
                        } else {
                            if (!vertexTopLabels.contains(v1l)) topleaf1 = false;
                        }
                    }
                    {
                        int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GETL_next_vertex2].length + 
                                     uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GETL_next_vertex2].length;
                        if (nedges!=1) {
                            topleaf2 = false;
                        } else {
                            if (!vertexTopLabels.contains(v2l)) topleaf2 = false;
                        }
                    }
                    
                    if (topleaf1 || topleaf2) {
                        DLG g2 = new DLG(uw_currentDLG);
                        g2.setEdge(uw_stage_GETL_next_vertex1, uw_stage_GETL_next_vertex2, candidates[uw_stage_GETL_next_label]);

                        uw_stage_GETL_next_label++;
                        if (uw_stage_GETL_next_label >= candidates.length) {
                            uw_stage_GETL_next_label = 0;
                            uw_stage_GETL_next_vertex2++;
                            if (uw_stage_GETL_next_vertex2>=n) {
                                uw_stage_GETL_next_vertex2 = 0;
                                uw_stage_GETL_next_vertex1++;
                            }
                        }

                        return g2;
                    } else {
                        uw_stage_GETL_next_label = 0;
                        uw_stage_GETL_next_vertex2++;
                        if (uw_stage_GETL_next_vertex2>=n) {
                            uw_stage_GETL_next_vertex2 = 0;
                            uw_stage_GETL_next_vertex1++;
                        }
                        return getNextUpwardRefinement();
                    }
                }  
                
                
           case 3: // generalize a leaf:
                {          
                    if (uw_stage_GVL_next_vertex>=n ||
                        edgeTopLabels.isEmpty()) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }
                    
                    boolean leaf = true;
                    int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GVL_next_vertex].length + 
                                 uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GVL_next_vertex].length;
                    if (nedges!=1) leaf = false;
                    
                    if (leaf) {
                        Label vl = uw_currentDLG.getVertex(uw_stage_GVL_next_vertex);
                        Label candidates[] = partialOrder.getParents(vl);
                        if (uw_stage_GVL_next_label >= candidates.length) {
                            uw_stage_GVL_next_vertex++;
                            uw_stage_GVL_next_label = 0;
                            return getNextUpwardRefinement();
                        }

                        DLG g2 = new DLG(uw_currentDLG);
                        g2.setVertex(uw_stage_GVL_next_vertex, candidates[uw_stage_GVL_next_label]);
                        uw_stage_GVL_next_label++;
                        if (uw_stage_GVL_next_label >= candidates.length) {
                            uw_stage_GVL_next_vertex++;
                            uw_stage_GVL_next_label = 0;
                        }
                        return g2;
                    } else {
                        uw_stage_GVL_next_vertex++;
                        uw_stage_GVL_next_label = 0;
                        return getNextUpwardRefinement();
                    }
                }
                
            case 4: // generalize edge to a vertex that is not a top-leaf:
                {          
                    if (uw_stage_GENTL_next_vertex1>=n ||
                        edgeTopLabels.isEmpty()) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }

                    Label el = uw_currentDLG.getEdge(uw_stage_GENTL_next_vertex1, uw_stage_GENTL_next_vertex2);
                    if (el==null) {
                        uw_stage_GENTL_next_label = 0;
                        uw_stage_GENTL_next_vertex2++;
                        if (uw_stage_GENTL_next_vertex2>=n) {
                            uw_stage_GENTL_next_vertex2 = 0;
                            uw_stage_GENTL_next_vertex1++;
                        }
                        return getNextUpwardRefinement();
                    }
                    Label candidates[] = partialOrder.getParents(el);
                    if (uw_stage_GENTL_next_label >= candidates.length) {
                        uw_stage_GENTL_next_label = 0;
                        uw_stage_GENTL_next_vertex2++;
                        if (uw_stage_GENTL_next_vertex2>=n) {
                            uw_stage_GENTL_next_vertex2 = 0;
                            uw_stage_GENTL_next_vertex1++;
                        }
                        return getNextUpwardRefinement();
                    }
                    
                    Label v1l = uw_currentDLG.getVertex(uw_stage_GENTL_next_vertex1);
                    Label v2l = uw_currentDLG.getVertex(uw_stage_GENTL_next_vertex2);
                    boolean topleaf1 = true;
                    boolean topleaf2 = true;
                    {
                        int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GENTL_next_vertex1].length + 
                                     uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GENTL_next_vertex1].length;
                        if (nedges!=1) {
                            topleaf1 = false;
                        } else {
                            if (!vertexTopLabels.contains(v1l)) topleaf1 = false;
                        }
                    }
                    {
                        int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GENTL_next_vertex2].length + 
                                     uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GENTL_next_vertex2].length;
                        if (nedges!=1) {
                            topleaf2 = false;
                        } else {
                            if (!vertexTopLabels.contains(v2l)) topleaf2 = false;
                        }
                    }
                    
                    if (!topleaf1 && !topleaf2) {
                        DLG g2 = new DLG(uw_currentDLG);
                        g2.setEdge(uw_stage_GENTL_next_vertex1, uw_stage_GENTL_next_vertex2, candidates[uw_stage_GENTL_next_label]);

                        uw_stage_GENTL_next_label++;
                        if (uw_stage_GENTL_next_label >= candidates.length) {
                            uw_stage_GENTL_next_label = 0;
                            uw_stage_GENTL_next_vertex2++;
                            if (uw_stage_GENTL_next_vertex2>=n) {
                                uw_stage_GENTL_next_vertex2 = 0;
                                uw_stage_GENTL_next_vertex1++;
                            }
                        }

                        return g2;
                    } else {
                        uw_stage_GENTL_next_label = 0;
                        uw_stage_GENTL_next_vertex2++;
                        if (uw_stage_GENTL_next_vertex2>=n) {
                            uw_stage_GENTL_next_vertex2 = 0;
                            uw_stage_GENTL_next_vertex1++;
                        }
                        return getNextUpwardRefinement();
                    }
                }  
                
           case 5: // generalize a non leaf:
                {          
                    if (uw_stage_GVNL_next_vertex>=n ||
                        edgeTopLabels.isEmpty()) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }
                    

                    boolean leaf = true;
                    int nedges = uw_currentDLG.getCondensedOutgoingEdges()[uw_stage_GVNL_next_vertex].length + 
                                 uw_currentDLG.getCondensedIncomingEdges()[uw_stage_GVNL_next_vertex].length;
                    if (nedges!=1) leaf = false;
                    
                    if (!leaf) {
                        Label vl = uw_currentDLG.getVertex(uw_stage_GVNL_next_vertex);
                        Label candidates[] = partialOrder.getParents(vl);
                        if (uw_stage_GVNL_next_label >= candidates.length) {
                            uw_stage_GVNL_next_vertex++;
                            uw_stage_GVNL_next_label = 0;
                            return getNextUpwardRefinement();
                        }

                        DLG g2 = new DLG(uw_currentDLG);
                        g2.setVertex(uw_stage_GVNL_next_vertex, candidates[uw_stage_GVNL_next_label]);
                        uw_stage_GVNL_next_label++;
                        if (uw_stage_GVNL_next_label >= candidates.length) {
                            uw_stage_GVNL_next_vertex++;
                            uw_stage_GVNL_next_label = 0;
                        }
                        return g2;
                    } else {
                        uw_stage_GVNL_next_vertex++;
                        uw_stage_GVNL_next_label = 0;
                        return getNextUpwardRefinement();
                    }
                }
                                
                
            default:
                return null;
        }
    }
    
}

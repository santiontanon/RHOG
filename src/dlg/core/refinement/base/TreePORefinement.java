/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.base;

import dlg.core.DLG;
import dlg.core.PartialOrder;
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
 */
public class TreePORefinement extends RefinementOperator implements IncrementalRefinementOperator {

    public final int DW_STAGE_EMPTY_GRAPH = 0;
    public final int DW_STAGE_ADD_LEAF = 4;
    public final int DW_STAGE_ADD_ROOT = 3;
    public final int DW_STAGE_SPECIALIZE_VERTEX = 2;
    public final int DW_STAGE_SPECIALIZE_EDGE = 1;
    
    
    List<Label> vertexTopLabels = new ArrayList<>();
    List<Label> edgeTopLabels = new ArrayList<>();
    PartialOrder partialOrder = null;
    
    // internal state of downward incremental refinement:
    TreeDLG dw_currentDLG = null;
    int dw_incremental_stage = 0;
    int dw_stage_EG_next_label = 0;
    int dw_stage_AL_next_vertex = 0;
    int dw_stage_AL_next_vlabel = 0;
    int dw_stage_AL_next_elabel = 0;
    int dw_stage_AR_next_vlabel = 0;
    int dw_stage_AR_next_elabel = 0;
    int dw_stage_SV_next_vertex = 0;
    int dw_stage_SV_next_label = 0;
    int dw_stage_SE_next_vertex = 0;
    int dw_stage_SE_next_child = 0;
    int dw_stage_SE_next_label = 0;
    
    // internal state of upward incremental refinement:
    TreeDLG uw_currentDLG = null;
    int uw_incremental_stage = 0;
    int uw_stage_RL_next_vertex = 0;
    int uw_stage_GLV_next_vertex = 0;
    int uw_stage_GLV_next_label = 0;
    int uw_stage_GNLV_next_vertex = 0;
    int uw_stage_GNLV_next_label = 0;
    int uw_stage_GLE_next_vertex = 0;
    int uw_stage_GLE_next_label = 0;
    int uw_stage_GNLE_next_vertex = 0;
    int uw_stage_GNLE_next_label = 0;
    
    public DLG getTop()
    {
        return new TreeDLG(0);
    }
    
    
    public TreePORefinement(PartialOrder po) {
        vertexTopLabels.add(po.getTop());
        edgeTopLabels.add(po.getTop());
        partialOrder = po;
    }

    
    public TreePORefinement(List<Label> vtl, List<Label> etl, PartialOrder po) {
        if (vtl==null) {
            vertexTopLabels.add(po.getTop());
        } else {
            vertexTopLabels.addAll(vtl);
        }
        if (etl==null) {
            edgeTopLabels.add(po.getTop());
        } else {
            edgeTopLabels.addAll(etl);
        }
        partialOrder = po;
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
        dw_stage_EG_next_label = 0;
        dw_stage_AL_next_vertex = 0;
        dw_stage_AL_next_vlabel = 0;
        dw_stage_AL_next_elabel = 0;
        dw_stage_AR_next_vlabel = 0;
        dw_stage_AR_next_elabel = 0;
        dw_stage_SV_next_vertex = 0;
        dw_stage_SV_next_label = 0;
        dw_stage_SE_next_vertex = 0;
        dw_stage_SE_next_child = 0;
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
                    TreeDLG g2 = new TreeDLG(1);
                    g2.setVertex(0,l);
                    g2.setRoot(0);
                    return g2;
                }

            case DW_STAGE_ADD_LEAF: // add new leaves
                {
                    if (dw_stage_AL_next_vertex>=n || edgeTopLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    Label l = vertexTopLabels.get(dw_stage_AL_next_vlabel);
                    Label l2 = edgeTopLabels.get(dw_stage_AL_next_elabel);

                    TreeDLG g2 = new TreeDLG(n+1);            
                    g2.setRoot(((TreeDLG)dw_currentDLG).getRoot());
                    for(int j = 0;j<n;j++) g2.setVertex(j, dw_currentDLG.getVertex(j));
                    g2.setVertex(n, l);

                    for(int j = 0;j<n;j++) {
                        for(int k = 0;k<n;k++) {
                            g2.setEdge(j, k, dw_currentDLG.getEdge(j, k));
                        }
                    }
                    g2.setEdge(dw_stage_AL_next_vertex, n, l2);
                    
                    dw_stage_AL_next_elabel++;
                    if (dw_stage_AL_next_elabel>=edgeTopLabels.size()) {
                        dw_stage_AL_next_elabel = 0;
                        dw_stage_AL_next_vlabel++;
                    }
                    if (dw_stage_AL_next_vlabel>=vertexTopLabels.size()) {
                        dw_stage_AL_next_vlabel = 0;
                        dw_stage_AL_next_vertex++;
                    }
                    
                    return g2;
                }
                
            case DW_STAGE_ADD_ROOT: // add a new root:
                {          
                    if (dw_stage_AR_next_vlabel>=vertexTopLabels.size() ||
                        edgeTopLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }

                    Label l = vertexTopLabels.get(dw_stage_AR_next_vlabel);
                    Label l2 = edgeTopLabels.get(dw_stage_AR_next_elabel);
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
                    
                    dw_stage_AR_next_elabel++;
                    if (dw_stage_AR_next_elabel>=edgeTopLabels.size()) {
                        dw_stage_AR_next_elabel = 0;
                        dw_stage_AR_next_vlabel++;
                    }
                    
                    return g2;
                }
           case DW_STAGE_SPECIALIZE_VERTEX: // specialize vertex:
                {          
                    if (dw_stage_SV_next_vertex>=n ||
                        edgeTopLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    
                    Label vl = dw_currentDLG.getVertex(dw_stage_SV_next_vertex);
                    Label candidates[] = partialOrder.getChildren(vl);
                    if (dw_stage_SV_next_label >= candidates.length) {
                        dw_stage_SV_next_vertex++;
                        dw_stage_SV_next_label = 0;
                        return getNextDownwardRefinement();
                    }
                    
                    TreeDLG g2 = new TreeDLG(dw_currentDLG);
                    g2.setVertex(dw_stage_SV_next_vertex, candidates[dw_stage_SV_next_label]);
                    
                    dw_stage_SV_next_label++;
                    if (dw_stage_SV_next_label >= candidates.length) {
                        dw_stage_SV_next_vertex++;
                        dw_stage_SV_next_label = 0;
                    }
                    
                    return g2;
                }
           case DW_STAGE_SPECIALIZE_EDGE: // specialize edge:
                {          
                    if (dw_stage_SE_next_vertex>=n ||
                        edgeTopLabels.isEmpty()) {
                        dw_incremental_stage++;
                        return getNextDownwardRefinement();
                    }
                    
                    int children[] = dw_currentDLG.getChildren(dw_stage_SE_next_vertex);
                    if (dw_stage_SE_next_child >= children.length) {
                        dw_stage_SE_next_vertex++;
                        dw_stage_SE_next_child = 0;
                        dw_stage_SE_next_label = 0;
                        return getNextDownwardRefinement();
                    }
                    Label el = dw_currentDLG.getEdge(dw_stage_SE_next_vertex, children[dw_stage_SE_next_child]);
                    Label candidates[] = partialOrder.getChildren(el);
                    if (dw_stage_SE_next_label >= candidates.length) {
                        dw_stage_SE_next_label = 0;
                        dw_stage_SE_next_child++;
                        if (dw_stage_SE_next_child >= children.length) {
                            dw_stage_SE_next_vertex++;
                            dw_stage_SE_next_child = 0;
                            dw_stage_SE_next_label = 0;
                        }
                        return getNextDownwardRefinement();
                    }
                    
                    TreeDLG g2 = new TreeDLG(dw_currentDLG);
                    g2.setEdge(dw_stage_SE_next_vertex, children[dw_stage_SE_next_child], candidates[dw_stage_SE_next_label]);

                    dw_stage_SE_next_label++;
                    if (dw_stage_SE_next_label >= candidates.length) {
                        dw_stage_SE_next_label = 0;
                        dw_stage_SE_next_child++;
                        if (dw_stage_SE_next_child >= children.length) {
                            dw_stage_SE_next_vertex++;
                            dw_stage_SE_next_child = 0;
                            dw_stage_SE_next_label = 0;
                        }
                    }

                    return g2;
                }
           default: return null;
        }        
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
        uw_stage_RL_next_vertex = 0;
        uw_stage_GLV_next_vertex = 0;
        uw_stage_GLV_next_label = 0;
        uw_stage_GNLV_next_vertex = 0;
        uw_stage_GNLV_next_label = 0;
        uw_stage_GLE_next_vertex = 0;
        uw_stage_GLE_next_label = 0;
        uw_stage_GNLE_next_vertex = 0;
        uw_stage_GNLE_next_label = 0;
    }

    public DLG getNextUpwardRefinement() throws Exception {
        int n = uw_currentDLG.getNVertices();
        switch(uw_incremental_stage) {
            case 0: // remove the root if it only has 1 or 0 children:
                {
                    if (uw_currentDLG.getNVertices()==0) return null;
                    int root = uw_currentDLG.getRoot();
                    if (uw_currentDLG.getChildren(root).length>1 ||
                        !vertexTopLabels.contains(uw_currentDLG.getVertex(root))) {
                        uw_incremental_stage++;
                        return getNextUpwardRefinement();
                    }

                    // remove root
                    DLG g2 = GraphFilter.removeVertex(uw_currentDLG, root);
                    uw_incremental_stage = 1;
                    return g2;
                }
            case 1: // remove a leaf
            {
                if (n<=1 || uw_stage_RL_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();
                }

                if (uw_currentDLG.getChildren(uw_stage_RL_next_vertex).length>0 ||
                    !vertexTopLabels.contains(uw_currentDLG.getVertex(uw_stage_RL_next_vertex)) ||
                    !edgeTopLabels.contains(uw_currentDLG.getEdge(uw_currentDLG.getParent(uw_stage_RL_next_vertex), uw_stage_RL_next_vertex))) {
                    uw_stage_RL_next_vertex++;
                    return getNextUpwardRefinement();
                }
                
                // remove leaf:
                DLG g2 = GraphFilter.removeVertex(uw_currentDLG, uw_stage_RL_next_vertex);

                uw_stage_RL_next_vertex++;                
                return g2;
            }
            
            case 2: // generalize an edge to a top leaf
            {
                if (uw_stage_GLE_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();                    
                }
                
                if (uw_currentDLG.getChildren(uw_stage_GLE_next_vertex).length>0 ||
                    !vertexTopLabels.contains(uw_currentDLG.getVertex(uw_stage_GLE_next_vertex))) {
                    uw_stage_GLE_next_vertex++;
                    uw_stage_GLE_next_label = 0;
                    return getNextUpwardRefinement();
                }
                
                int parent = uw_currentDLG.getParent(uw_stage_GLE_next_vertex);
                if (parent==-1) {
                    uw_stage_GLE_next_vertex++;
                    uw_stage_GLE_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label el = uw_currentDLG.getEdge(parent, uw_stage_GLE_next_vertex);
                if (edgeTopLabels.contains(el)) {
                    uw_stage_GLE_next_vertex++;
                    uw_stage_GLE_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label candidates[] = partialOrder.getParents(el);
                if (uw_stage_GLE_next_label >= candidates.length) {
                    uw_stage_GLE_next_vertex++;
                    uw_stage_GLE_next_label = 0;
                    return getNextUpwardRefinement();                    
                }
                
                TreeDLG g2 = new TreeDLG(uw_currentDLG);
                g2.setEdge(parent, uw_stage_GLE_next_vertex, candidates[uw_stage_GLE_next_label]);
                
                uw_stage_GLE_next_label++;
                if (uw_stage_GLE_next_label >= candidates.length) {
                    uw_stage_GLE_next_vertex++;
                    uw_stage_GLE_next_label = 0;
                }
                
                return g2;
            }
            
            case 3: // generalize a leaf vertex
            {
                if (uw_stage_GLV_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();
                }
                
                if (uw_currentDLG.getChildren(uw_stage_GLV_next_vertex).length>0) {
                    uw_stage_GLV_next_vertex++;
                    return getNextUpwardRefinement();
                }                
                
                Label vl = uw_currentDLG.getVertex(uw_stage_GLV_next_vertex);
                if (vertexTopLabels.contains(vl)) {
                    uw_stage_GLV_next_vertex++;
                    uw_stage_GLV_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label candidates[] = partialOrder.getParents(vl);
                if (uw_stage_GLV_next_label >= candidates.length) {
                    uw_stage_GLV_next_vertex++;
                    uw_stage_GLV_next_label = 0;
                    return getNextUpwardRefinement();                    
                }
                
                TreeDLG g2 = new TreeDLG(uw_currentDLG);
                g2.setVertex(uw_stage_GLV_next_vertex, candidates[uw_stage_GLV_next_label]);
                
                uw_stage_GLV_next_label++;
                if (uw_stage_GLV_next_label >= candidates.length) {
                    uw_stage_GLV_next_vertex++;
                    uw_stage_GLV_next_label = 0;
                }
                
                return g2;
            }        
            
            // the previous and this are separated, to generte refinements that generalize 
            // leaves first, maximizing the chances of removing them first, thus making
            // things like disintegration faster
            case 4: // generalize an edge to a vertex that is not a top-leaf
            {
                if (uw_stage_GNLE_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();                    
                }
                
                if (uw_currentDLG.getChildren(uw_stage_GNLE_next_vertex).length==0 &&
                    vertexTopLabels.contains(uw_currentDLG.getVertex(uw_stage_GNLE_next_vertex))) {
                    uw_stage_GNLE_next_vertex++;
                    uw_stage_GNLE_next_label = 0;
                    return getNextUpwardRefinement();
                }

                int parent = uw_currentDLG.getParent(uw_stage_GNLE_next_vertex);
                if (parent==-1) {
                    uw_stage_GNLE_next_vertex++;
                    uw_stage_GNLE_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label el = uw_currentDLG.getEdge(parent, uw_stage_GNLE_next_vertex);
                if (edgeTopLabels.contains(el)) {
                    uw_stage_GNLE_next_vertex++;
                    uw_stage_GNLE_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label candidates[] = partialOrder.getParents(el);
                if (uw_stage_GNLE_next_label >= candidates.length) {
                    uw_stage_GNLE_next_vertex++;
                    uw_stage_GNLE_next_label = 0;
                    return getNextUpwardRefinement();                    
                }
                
                TreeDLG g2 = new TreeDLG(uw_currentDLG);
                g2.setEdge(parent, uw_stage_GNLE_next_vertex, candidates[uw_stage_GNLE_next_label]);
                
                uw_stage_GNLE_next_label++;
                if (uw_stage_GNLE_next_label >= candidates.length) {
                    uw_stage_GNLE_next_vertex++;
                    uw_stage_GNLE_next_label = 0;
                }
                
                return g2;
            }            
                        
            
            // the previous and this are separated, to generte refinements that generalize 
            // leaves first, maximizing the chances of removing them first, thus making
            // things like disintegration faster
            case 5: // generalize a non-leaf vertex
            {
                if (uw_stage_GNLV_next_vertex>=n) {
                    uw_incremental_stage++;
                    return getNextUpwardRefinement();
                }
                
                if (uw_currentDLG.getChildren(uw_stage_GNLV_next_vertex).length==0) {
                    uw_stage_GNLV_next_vertex++;
                    return getNextUpwardRefinement();
                }                
                
                Label vl = uw_currentDLG.getVertex(uw_stage_GNLV_next_vertex);
                if (vertexTopLabels.contains(vl)) {
                    uw_stage_GNLV_next_vertex++;
                    uw_stage_GNLV_next_label = 0;
                    return getNextUpwardRefinement();
                }
                Label candidates[] = partialOrder.getParents(vl);
                if (uw_stage_GNLV_next_label >= candidates.length) {
                    uw_stage_GNLV_next_vertex++;
                    uw_stage_GNLV_next_label = 0;
                    return getNextUpwardRefinement();                    
                }
                
                TreeDLG g2 = new TreeDLG(uw_currentDLG);
                g2.setVertex(uw_stage_GNLV_next_vertex, candidates[uw_stage_GNLV_next_label]);
                
                uw_stage_GNLV_next_label++;
                if (uw_stage_GNLV_next_label >= candidates.length) {
                    uw_stage_GNLV_next_vertex++;
                    uw_stage_GNLV_next_label = 0;
                }
                
                return g2;
            }

            default:
                return null;
        }
    }
    
}

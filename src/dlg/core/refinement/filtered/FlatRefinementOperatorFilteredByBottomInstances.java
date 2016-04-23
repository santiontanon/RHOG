/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.filtered;

import dlg.core.DLG;
import dlg.core.refinement.IncrementalRefinementOperator;
import dlg.core.refinement.RefinementOperator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class FlatRefinementOperatorFilteredByBottomInstances extends RefinementOperator implements IncrementalRefinementOperator {
    RefinementOperator baseRho = null;
    List<DLG> bottomInstances = null;
        
    HashMap<Label, List<Label>> outgoingEdges = new HashMap<>();
    HashMap<Label, List<Label>> incomingEdges = new HashMap<>();
    
    public FlatRefinementOperatorFilteredByBottomInstances(RefinementOperator rho, List<DLG> bi)
    {
        baseRho = rho;
        bottomInstances = bi;
        
        for(DLG g:bottomInstances) {
            for(int v = 0;v<g.getNVertices();v++) {
                Label lv = g.getVertex(v);
                List<Label> outgoing = outgoingEdges.get(lv);
                if (outgoing==null) {
                    outgoing = new ArrayList<>();
                    outgoingEdges.put(lv, outgoing);
                }
                List<Label> incoming = incomingEdges.get(lv);
                if (incoming==null) {
                    incoming = new ArrayList<>();
                    incomingEdges.put(lv, incoming);
                }
                for(int v2:g.getCondensedOutgoingEdges()[v]) {
                    Label le = g.getEdge(v, v2);
                    if (!outgoing.contains(le)) outgoing.add(le);
                }
                for(int v2:g.getCondensedIncomingEdges()[v]) {
                    Label le = g.getEdge(v, v2);
                    if (!incoming.contains(le)) incoming.add(le);
                }
            }
        }
    }
    
    
    public RefinementOperator getBaseRho() {
        return baseRho;
    }
    

    public DLG getTop()
    {
        return baseRho.getTop();
    }
           
    
    public List<? extends DLG> downwardRefinements(DLG g) throws Exception
    {
        List<? extends DLG> l = baseRho.downwardRefinements(g);
        List<DLG> filtered = new ArrayList<>();
        for(DLG g2:l) {
            if (passesFilter(g2)) filtered.add(g2);
        }
        return filtered;
    }
    
    
    public List<? extends DLG> upwardRefinements(DLG g) throws Exception
    {
        return baseRho.upwardRefinements(g);
    }
    
    
    public void setDLGForDownwardRefinement(DLG g) throws Exception
    {
        if (baseRho instanceof IncrementalRefinementOperator) {
            ((IncrementalRefinementOperator)baseRho).setDLGForDownwardRefinement(g);
        } else {
            throw new Exception("Base refinement operator is not incremental!");
        }
    }
    
    
    public DLG getNextDownwardRefinement() throws Exception
    {
        if (baseRho instanceof IncrementalRefinementOperator) {
            do {
                DLG g = ((IncrementalRefinementOperator)baseRho).getNextDownwardRefinement();
                if (g==null) return null;
                if (passesFilter(g)) {
                    return g;
                }
            } while(true);
        } else {
            throw new Exception("Base refinement operator is not incremental!");
        }
    }
    
    
    public void setDLGForUpwardRefinement(DLG g) throws Exception {
        if (baseRho instanceof IncrementalRefinementOperator) {
            ((IncrementalRefinementOperator)baseRho).setDLGForUpwardRefinement(g);
        } else {
            throw new Exception("Base refinement operator is not incremental!");
        }
    }

    public DLG getNextUpwardRefinement() throws Exception {
        if (baseRho instanceof IncrementalRefinementOperator) {            
            return ((IncrementalRefinementOperator)baseRho).getNextUpwardRefinement();
        } else {
            throw new Exception("Base refinement operator is not incremental!");
        }
    }
        
    
    public boolean passesFilter(DLG g) {
        for(int v = 0;v<g.getNVertices();v++) {
            Label lv = g.getVertex(v);
            List<Label> incoming = outgoingEdges.get(lv);
            for(int v2:g.getCondensedOutgoingEdges()[v]) {
                Label le = g.getEdge(v, v2);
                if (incoming==null) return false;
                if (!incoming.contains(le)) return false;
            }
            List<Label> outgoing = incomingEdges.get(lv);
            for(int v2:g.getCondensedIncomingEdges()[v]) {
                Label le = g.getEdge(v, v2);
                if (outgoing==null) return false;
                if (!outgoing.contains(le)) return false;
            }
        }
        return true;
    }
}

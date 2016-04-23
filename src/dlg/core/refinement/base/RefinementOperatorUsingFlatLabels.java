/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement.base;

import dlg.core.refinement.RefinementOperator;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public abstract class RefinementOperatorUsingFlatLabels extends RefinementOperator {
    List<Label> vertexLabels = new ArrayList<>();
    List<Label> edgeLabels = new ArrayList<>();
 
    
    public RefinementOperatorUsingFlatLabels(List<Label> rl, List<Label> el) {
        vertexLabels.addAll(rl);
        edgeLabels.addAll(el);
    }
        
    public void setVertexLabels(List<Label> rl) {
        vertexLabels = rl;
    }
    
    public List<Label> getVertexLabels() {
        return vertexLabels;
    }

    public void setEdgeLabels(List<Label> el) {
        edgeLabels = el;
    }

    public List<Label> getEdgeLabels() {
        return edgeLabels;
    }
    
}

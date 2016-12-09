/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.core.operations.AntiUnification;
import dlg.core.DLG;
import dlg.core.operations.RefinementPathLengthCalculator;
import dlg.core.refinement.RefinementOperator;
import dlg.core.refinement.base.RefinementOperatorUsingFlatLabels;
import dlg.core.subsumption.Subsumption;
import dlg.util.Label;
import dlg.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class AUDistance extends Distance {

    public static int DEBUG = 0;
    
    Subsumption m_s = null;
    RefinementOperator m_rho = null;
    
    public AUDistance(Subsumption s, RefinementOperator rho) {
        m_s = s;
        m_rho = rho;
    }
    
    
    public void train(List<DLG> instances, List<Label> labels) {
        // no training is required
    }
    
    
    public double distance(DLG g1, DLG g2) throws Exception {
        if (DEBUG>=2) {
            System.out.println("Graph 1:");
            System.out.println(g1);
            System.out.println("Graph 2:");
            System.out.println(g2);
        }
        
        List<Label> old_vl = null;
        List<Label> old_el = null;
        
        if (m_rho instanceof RefinementOperatorUsingFlatLabels) {
            old_vl = ((RefinementOperatorUsingFlatLabels)m_rho).getVertexLabels();
            old_el = ((RefinementOperatorUsingFlatLabels)m_rho).getEdgeLabels();
            
            // optimize things a bit, by only considering the set of labels that these two graphs have:
            List<Label> vl = g1.getAllVertexLabels();
            for(Label l:g2.getAllVertexLabels()) { 
                if (!vl.contains(l)) vl.add(l);
            }
            ((RefinementOperatorUsingFlatLabels)m_rho).setVertexLabels(vl);
            List<Label> el = g1.getAllEdgeLabels();
            for(Label l:g2.getAllEdgeLabels()) {
                if (!el.contains(l)) el.add(l);
            }
            ((RefinementOperatorUsingFlatLabels)m_rho).setEdgeLabels(el);
            
            //System.out.println(vl.size() + "-" + el.size() + " vs " + old_vl.size() + "-" + old_el.size());
        }
        
        Pair<DLG, Integer> tmp = AntiUnification.singleAntiunificationSteps(g1, g2, m_s, m_rho);
        DLG au = tmp.m_a;       

        if (DEBUG>=2) {
            System.out.println("AU:");
            System.out.println(tmp.m_a);
        }        
        
        double d_top_au = tmp.m_b;
        double d_au_g1 = RefinementPathLengthCalculator.refinementPathLength(au, g1, m_s, m_rho);
        double d_au_g2 = RefinementPathLengthCalculator.refinementPathLength(au, g2, m_s, m_rho);
        
        if (DEBUG>=1) {
            System.out.println("d_top_au = " + d_top_au);
            System.out.println("d_au_g1 = " + d_au_g1);
            System.out.println("d_au_g2 = " + d_au_g2);
        }
        
        if (m_rho instanceof RefinementOperatorUsingFlatLabels) {
            // restore the labels to reset the refinement operator to the same state as it was when this function was called
            ((RefinementOperatorUsingFlatLabels)m_rho).setVertexLabels(old_vl);
            ((RefinementOperatorUsingFlatLabels)m_rho).setEdgeLabels(old_el);
        }
        
        return 1 - (d_top_au / (d_top_au + d_au_g1 + d_au_g2));
    }
    
    
    public String toString() {
        return this.getClass().getSimpleName() + "(" + m_s + "," + m_rho + ")";
    }
}

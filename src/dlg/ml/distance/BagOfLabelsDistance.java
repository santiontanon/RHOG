/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class BagOfLabelsDistance extends Distance {

    boolean useVertexLabels = true;
    boolean useEdgeLabels = true;
    PartialOrder partialOrder = null;
        
    
    public BagOfLabelsDistance(boolean uv, boolean ue, PartialOrder po) {
        useVertexLabels = uv;
        useEdgeLabels = ue;
        partialOrder = po;
    }
    
    
    public void train(List<DLG> instances, List<Label> labels) {
        // no training is required
    }
    
    
    public double distance(DLG g1, DLG g2) throws Exception {
        List<Label> labels;
        if (partialOrder==null) {
            labels = new ArrayList<>();
            if (useVertexLabels) {
                labels.addAll(g1.getAllVertexLabels());
                for(Label l:g2.getAllVertexLabels()) {
                    if (!labels.add(l)) labels.add(l);
                }
            }
            if (useEdgeLabels) {
                for(Label l:g1.getAllEdgeLabels()) {
                    if (!labels.add(l)) labels.add(l);
                }
                for(Label l:g2.getAllEdgeLabels()) {
                    if (!labels.add(l)) labels.add(l);
                }
            }
        } else {
            labels = partialOrder.getLabels();
        }
        
        double v1[] = buildBagOfLabels(g1, labels);
        double v2[] = buildBagOfLabels(g2, labels);
        
        return cosineDistance(v1, v2);
    }
    
    
    public double cosineDistance(double v1[], double v2[]) {
        double norm1 = 0;
        double norm2 = 0;
        double dotproduct = 0;
        for(int i = 0;i<v1.length;i++) {
            norm1 += v1[i]*v1[i];
            norm2 += v2[i]*v2[i];
            dotproduct += v1[i]*v2[i];
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        dotproduct /= norm1 * norm2;
        
        return 1 - dotproduct;
    }
    
    
    public double[] buildBagOfLabels(DLG g, List<Label> labels) {
        int n = labels.size();
        double v[] = new double[n];
        
        for(int i = 0;i<g.getNVertices();i++) {
            if (useVertexLabels) addLabelToVector(g.getVertex(i), v, labels);
            if (useEdgeLabels) {
                for(int j : g.getCondensedOutgoingEdges()[i]) {
                    addLabelToVector(g.getEdge(i, j), v, labels);
                }
            }
        }
        
        return v;
    }


    public void addLabelToVector(Label l, double v[], List<Label> labels) {
        if (partialOrder==null) {
            v[labels.indexOf(l)]++;
        } else {
            for(Label l2:partialOrder.getAncestors(l)) v[labels.indexOf(l2)]++;
        }        
    }
    
}

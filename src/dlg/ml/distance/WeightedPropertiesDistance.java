/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.core.DLG;
import dlg.core.refinement.RefinementOperator;
import dlg.core.subsumption.Subsumption;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class WeightedPropertiesDistance extends PropertiesDistance {
    public static int DEBUG = 0;
    
    
    double weights[] = null;
    
    public WeightedPropertiesDistance(Subsumption a_s, RefinementOperator a_rho) {
        super(a_s, a_rho);
    }

    
    public WeightedPropertiesDistance(Subsumption a_s, List<DLG> p) {
        super(a_s, p);
    }
    

    public void train(List<DLG> instances, List<Label> labels) throws Exception {
        properties.clear();
        addPropertiesByDisintegration(instances);
        computeWeightsViaInformationGain(instances, labels);
    }
    
    

    public void addPropertiesByDisintegration(DLG g) throws Exception {
        weights = null;
        super.addPropertiesByDisintegration(g);
    }

    
    public void computeWeightsViaInformationGain(List<DLG> instances, List<Label> labels)
    {
        //public static double[] propertyWeights(int [][]reRepresentation, List<Label> labels) {
        int ninstances = instances.size();
        int nproperties = properties.size();
        weights = new double[nproperties];

        double H = entropy(labels);
        
        for(int p = 0;p<nproperties;p++) {
            List<Label> positive_labels = new ArrayList<>();
            List<Label> negative_labels = new ArrayList<>();
            for(int i = 0;i<ninstances;i++) {
                if (s.subsumes(properties.get(p), instances.get(i))!=null) {
                    positive_labels.add(labels.get(i));
                } else {
                    negative_labels.add(labels.get(i));
                }
            }
            double PH = entropy(positive_labels);
            double NH = entropy(negative_labels);
            double information_gain = H - (PH*positive_labels.size() + NH*negative_labels.size())/labels.size();
            weights[p] = information_gain;
        }
        
        if (DEBUG>=1) {
            System.out.println("Weights:");
            for(int i = 0;i<nproperties;i++) {
                System.out.println(i + "\t" + weights[i]);
            }        
        }
    }
    
    
    double entropy(List<Label> labels) {
        List<Label> differentLabels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        for(Label l:labels) {
            int idx = differentLabels.indexOf(l);
            if (idx==-1) {
                differentLabels.add(l);
                counts.add(1);
            } else {
                counts.set(idx, counts.get(idx)+1);
            }
        }
        
        double h = 0;
        double total = labels.size();
        double C = Math.log(2);
        for(int i = 0;i<differentLabels.size();i++) {
            double pi = counts.get(i)/total;
            h -= pi * Math.log(pi)/C;
        }
        
        return h;
    }    
    
        
    public double distance(DLG g1, DLG g2) throws Exception {
        double d = 0;
//        System.out.println("WeightedPropertiesDistance.distance: with " + properties.size() + " properties.");
        for(int i = 0;i<properties.size();i++) {
            DLG p = properties.get(i);
            int s1 = (s.subsumes(p, g1)==null ? 0:1);
            int s2 = (s.subsumes(p, g2)==null ? 0:1);
            if (weights==null) {
                d += Math.abs(s1 - s2);
            } else {
                d += weights[i]*Math.abs(s1 - s2);
            }
        }
        return d/=properties.size();
    }
    
}

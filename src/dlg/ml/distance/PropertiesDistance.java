/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.core.DLG;
import dlg.core.operations.Disintegration;
import dlg.core.refinement.RefinementOperator;
import dlg.core.subsumption.Subsumption;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class PropertiesDistance extends Distance {
    public static int DEBUG = 0;
    public static boolean USE_DISINTEGRATION_CACHE = false;
    
    Subsumption s;
    RefinementOperator rho;
    List<DLG> properties;
    
    
    public PropertiesDistance(Subsumption a_s, RefinementOperator a_rho) {
        s = a_s;
        rho = a_rho;
        properties = new ArrayList<>();
    }

    
    public PropertiesDistance(Subsumption a_s, List<DLG> p) {
        s = a_s;
        properties = p;
    }
    
    
    public void train(List<DLG> instances, List<Label> labels) throws Exception {
        properties.clear();
        addPropertiesByDisintegration(instances);
    }
    
    
    public void addPropertiesByDisintegration(DLG g) throws Exception {
        List<DLG> g_properties = null;
        if (USE_DISINTEGRATION_CACHE) {
            g_properties = Disintegration.greedyDisintegrationWithCache(g, s, rho);
        } else {
            g_properties = Disintegration.greedyDisintegration(g, s, rho);
        }
        for(DLG property:g_properties) {
            boolean found = false;
            for(DLG p2:properties) {
                if (s.subsumes(property, p2)!=null &&
                    s.subsumes(p2, property)!=null) {
                    found = true;
                    break;
                }
            }
            if (!found) properties.add(property);
        }
    }
    
    
    public void addPropertiesByDisintegration(List<DLG> l) throws Exception {
        for(DLG g:l) {
            addPropertiesByDisintegration(g);
            if (DEBUG>=1) System.out.println("addPropertiesByDisintegration: " + properties.size() + " - " + l.indexOf(g) + "/" + l.size());
        }
    }    
    
    
    public double distance(DLG g1, DLG g2) throws Exception {
        double d = 0;
//        System.out.println("PropertiesDistance.distance: with " + properties.size() + " properties.");
        for(DLG p:properties) {
            int s1 = (s.subsumes(p, g1)==null ? 0:1);
            int s2 = (s.subsumes(p, g2)==null ? 0:1);
            d += Math.abs(s1 - s2);
        }
        return d/=properties.size();
    }
    
    public String toString() {
        return this.getClass().getSimpleName() + "(" + s + "," + rho + ")";
    }
    
}

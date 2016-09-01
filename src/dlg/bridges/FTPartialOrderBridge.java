/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.TreeDLG;
import dlg.core.operations.GraphTypeChecker;
import dlg.util.Label;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class FTPartialOrderBridge {
    public static String sortPrefix = "v_";
    public static String featurePrefix = "e_";
    
    public PartialOrder translateOntology(Ontology starting_o) throws Exception {
        return translateOntology(starting_o, true);
    }

    
    public PartialOrder translateOntology(Ontology starting_o, boolean edgesInheritFromTop) throws Exception {
        PartialOrder po = new PartialOrder();
        List<Ontology> ontologies = new ArrayList<>();
                
        List<Ontology> open = new ArrayList<>();
        open.add(starting_o);
        while(!open.isEmpty()) {
            Ontology o = open.remove(0);
            ontologies.add(o);
            for(Ontology o2:o.getUsedOntologies()) {
                if (!ontologies.contains(o2) && !open.contains(o2)) open.add(o2);
            }
        }
        
        for(Ontology o:ontologies) {
            for(Sort s:o.getSorts()) {
                if (s.get().equals("any")) continue;
                Sort parent = s.getSuper();
                Label l = new Label(sortPrefix + s.get());
                po.addLabel(l);
                if (parent!=null && !parent.get().equals("any")) {
                    Label lp = new Label(sortPrefix + parent.get());
                    po.addSubsumptionConstraint(lp, l);
                }
                
                for(Symbol f:s.getFeatures()) {
                    Label fl = new Label(featurePrefix + f);
                    if (!po.contains(fl)) po.addLabel(fl, edgesInheritFromTop);
                }
            }
        }
        
        return po;
    }  


    /*
    Note: the input partial order is assumed to already contain all the sorts in the ontology, 
          and will just be completed with the constants.
    */
    public DLG translate(FeatureTerm f, FTKBase dm, boolean separateConstants, PartialOrder po) throws Exception {
        if (dm==null) dm = new FTKBase();
        List<FeatureTerm> l = FTRefinement.variables(f);
        List<FeatureTerm> l_copy = null;   // this is a separate copy, used only if separateConstants = true
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, ftl.base.core.Symbol>>> parents = FTRefinement.variablesWithAllParents(f);
        
        if (separateConstants) {
            l_copy = new ArrayList<>();
            for(FeatureTerm v:parents.keySet()) {
                if (v.isConstant() || dm.contains(v)) {
                    // duplicate all the constants, as many times as in-links they have:
                    for(int i = 0;i<parents.get(v).size();i++) {
                        l_copy.add(v);
                    }
                } else {
                    l_copy.add(v);
                }
            }
            l.clear();
            l.addAll(l_copy);
        }
        
        int n = l_copy.size();
        DLG g = new DLG(n);
        
        for(int i = 0;i<n;i++) {
            FeatureTerm v = l.get(i);
            Sort s = v.getSort();
            if (v.isConstant()) {
                Label cl = new Label(sortPrefix + v.toStringNOOS());
                if ((v instanceof IntegerFeatureTerm) ||
                    (v instanceof FloatFeatureTerm)) {
                    cl = new Label(v.toStringNOOS());
                }
                g.setVertex(i, cl);
                if (!po.contains(cl)) {
                    po.addLabel(cl);
                    po.addSubsumptionConstraint(new Label(sortPrefix + s.get()), cl);
                }
            } else if (dm.contains(v)) {
                Label cl = new Label(sortPrefix + v.getName().get());
                g.setVertex(i, cl);
                if (!po.contains(cl)) {
                    po.addLabel(cl);
                    po.addSubsumptionConstraint(new Label(sortPrefix + s.get()), cl);
                }
            } else {
                g.setVertex(i, new Label(sortPrefix + s.get()));
            }
            for(ftl.base.core.Symbol feature:s.getFeatures()) {
                List<FeatureTerm> values = v.featureValues(feature);
                for(FeatureTerm f2:values) {
                    if (separateConstants) {
                        int j = l_copy.indexOf(f2);
                        // use each constant only once:
                        if (f2.isConstant() || dm.contains(f2)) {
                            l_copy.set(j, null);
                        }
                        if (g.getEdge(i, j)!=null) throw new Exception("The original Feature Term cannot be represented as a DLG (it's a multigraph)!");
                        g.setEdge(i, j, new Label(featurePrefix + feature.get()));                        
                    } else {
                        int j = l.indexOf(f2);
                        if (g.getEdge(i, j)!=null) throw new Exception("The original Feature Term cannot be represented as a DLG (it's a multigraph)!");
                        g.setEdge(i, j, new Label(featurePrefix + feature.get()));                        
                    }
                }
            }
        }
        
        if (GraphTypeChecker.isTree(g)) {
            g = new TreeDLG(g);
        }
        
        return g;
    }
    
    
    public List<FeatureTerm> discretize(Collection<FeatureTerm> instances, int maxIntervals, Ontology o, FTKBase dm) throws Exception {
        List<FeatureTerm> discretized = new ArrayList<>();
        ArrayList<Symbol> features = new ArrayList<>();
        ArrayList<Sort> featureParentSort = new ArrayList<>();
        ArrayList<List<Float>> featureValues = new ArrayList<>();
        ArrayList<List<Float>> featureIntervals = new ArrayList<>();   
        ArrayList<Sort> discretizedSort = new ArrayList<>();
        ArrayList<List<FeatureTerm>> discretizedValues = new ArrayList<>();
        
        // identify all the places where there are numbers:
        for(FeatureTerm t:instances) {
            HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vwap = FTRefinement.variablesWithAllParents(t);
            
            for(FeatureTerm v:vwap.keySet()) {
                if ((v instanceof IntegerFeatureTerm)) {
                    for(Pair<TermFeatureTerm, Symbol> tmp:vwap.get(v)) {
                        Symbol f = tmp.m_b;
                        Sort s = tmp.m_a.getSort();
                        while(!s.getFeatures().contains(f)) s = s.getSuper();
                        int idx = -1;
                        for(int i = 0;i<features.size();i++) {
                            if (features.get(i).equals(f) && featureParentSort.get(i).equals(s)) {
                                idx = i;
                                break;
                            }
                        }
                        if (idx==-1) {
                            idx = features.size();
                            features.add(f);
                            featureParentSort.add(s);
                            featureValues.add(new ArrayList<Float>());
                        }
                        float value = (float)((IntegerFeatureTerm)v).getValue();
                        if (!featureValues.get(idx).contains(value)) featureValues.get(idx).add(value);
                    }
                } else if ((v instanceof FloatFeatureTerm)) {
                    for(Pair<TermFeatureTerm, Symbol> tmp:vwap.get(v)) {
                        Symbol f = tmp.m_b;
                        Sort s = tmp.m_a.getSort();
                        while(!s.getFeatures().contains(f)) s = s.getSuper();
                        int idx = -1;
                        for(int i = 0;i<features.size();i++) {
                            if (features.get(i).equals(f) && featureParentSort.get(i).equals(s)) {
                                idx = i;
                                break;
                            }
                        }
                        if (idx==-1) {
                            idx = features.size();
                            features.add(f);
                            featureParentSort.add(s);
                            featureValues.add(new ArrayList<Float>());
                        }
                        float value = (float)((FloatFeatureTerm)v).getValue();
                        if (!featureValues.get(idx).contains(value)) featureValues.get(idx).add(value);
                    }
                }
            }
        }
        
        for(int idx = 0;idx<features.size();idx++) {
            System.out.println("-----");
            Symbol f = features.get(idx);
            Collections.sort(featureValues.get(idx));
            System.out.println(featureParentSort.get(idx) + "." + f + ": " + featureValues.get(idx));
            List<Float> intervals = discretizeEqualMass(featureValues.get(idx), maxIntervals);
            System.out.println(featureParentSort.get(idx) + "." +f + " (thresholds): " + intervals);
            featureIntervals.add(intervals);
         
            // Create a new sort per value list:
            Sort parent = featureParentSort.get(idx);
            Sort valuesort = parent.featureSort(f);
            Sort newSort = o.newSort(parent.get() + "-" + f.get(), valuesort.get(), new String[]{}, new String[]{});
            System.out.println("new sort: " + newSort + "( inherits from " + valuesort + ")");
            discretizedSort.add(newSort);
            
            // Create DM elements for the buckets
            List<FeatureTerm> newValues = new ArrayList<>();
            for(int i = 0;i<intervals.size()+1;i++) {
                FeatureTerm v = newSort.createFeatureTerm();
                v.setName(new Symbol(newSort.get() + "-" + i));
                dm.addFT(v);
                newValues.add(v);
            }
            discretizedValues.add(newValues);
            System.out.println(f + " (new values): " + newValues);
            
            // Update the ontology sorts:
            parent.setFeatureSort(f, newSort);
        }
        
        // Update all the feature terms
        for(FeatureTerm t:instances) {
            List<FeatureTerm> variables = FTRefinement.variables(t);
            
            for(FeatureTerm v:variables) {
                for(int idx = 0;idx<features.size();idx++) {
                    if (v.getSort().is_a(featureParentSort.get(idx))) {
                        List<FeatureTerm> values = v.featureValues(features.get(idx));
                        List<FeatureTerm> newValues = new ArrayList<>();
                        for(FeatureTerm value:values) {
                            // discretize:
                            float fvalue = 0;
                            if (value instanceof FloatFeatureTerm) {
                                fvalue = ((FloatFeatureTerm)value).getValue();
                            } else {
                                fvalue = ((IntegerFeatureTerm)value).getValue();
                            }
                            int interval = featureIntervals.get(idx).size();
                            for(int i_idx = 0;i_idx<featureIntervals.get(idx).size();i_idx++) {
                                if (fvalue<=featureIntervals.get(idx).get(i_idx)) {
                                    interval = i_idx;
                                    break;
                                }
                            }
                            FeatureTerm discretizedValue = discretizedValues.get(idx).get(interval);
                            newValues.add(discretizedValue);
                        }
                        
                        // replace:
                        ((TermFeatureTerm)v).removeFeatureValue(features.get(idx));
                        for(FeatureTerm newValue:newValues) {
                            ((TermFeatureTerm)v).addFeatureValue(features.get(idx), newValue);
                        }
                    }
                }
            }
        }
        
        discretized.addAll(instances);
        return discretized;
    }
    
    
    List<Float> discretizeEqualMass(List<Float> sortedList, int maxIntervals) {
        if (sortedList.size() < maxIntervals) maxIntervals = sortedList.size();
        
        List<Float> thresholds = new ArrayList<>();
        int nThresholds = maxIntervals - 1;
        float increment = ((float)sortedList.size()) / maxIntervals;
        float currentIdx = 0;
        for(int i = 0;i<nThresholds;i++) {
            currentIdx += increment;
            int idx = (int)currentIdx;
            float v = (sortedList.get(idx-1) + sortedList.get(idx))/2;
            thresholds.add(v);
        }
        
        return thresholds;
    }
}

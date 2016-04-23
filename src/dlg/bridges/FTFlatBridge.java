/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class FTFlatBridge {
    public DLG translate(FeatureTerm f, FTKBase dm, boolean separateConstants) throws Exception {
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
                g.setVertex(i, new Label(v.toStringNOOS()));
            } else if (dm.contains(v)) {
                g.setVertex(i, new Label(v.getName().get()));
            } else {
                g.setVertex(i, new Label(s.get()));
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
                        g.setEdge(i, j, new Label(feature.get()));                        
                    } else {
                        int j = l.indexOf(f2);
                        if (g.getEdge(i, j)!=null) throw new Exception("The original Feature Term cannot be represented as a DLG (it's a multigraph)!");
                        g.setEdge(i, j, new Label(feature.get()));                        
                    }
                }
            }
        }
        
        return g;
    }
}

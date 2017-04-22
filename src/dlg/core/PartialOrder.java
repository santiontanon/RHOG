/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core;

import dlg.core.operations.GraphTypeChecker;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class PartialOrder {
    Label top = null;
    
    // these are the variables that the user of the ontology will modify:
    List<Label> m_labels = new ArrayList<>();
    HashMap<Label, List<Label>> m_subsumptionConstraints = new HashMap<>();
    
    // the two variables above, will be translated into the following precompiled ones:
    HashMap<Label, Integer> m_labelIndexes = new HashMap<>();
    boolean[][] m_subsumptionCache = null;
    Label[][] m_childrenCache = null;
    Label[][] m_parentsCache = null;
    Label[][] m_descendantsCache = null;
    Label[][] m_ancestorsCache = null;
    
    public PartialOrder() throws Exception {
        top = new Label("top");
        m_labels.add(top);
        m_labelIndexes.put(top, 0);
    }
    

    public PartialOrder(DLG g, boolean safetyCheck) throws Exception {
        if (safetyCheck && !GraphTypeChecker.isUpperBoundedSemiLattice(g)) throw new Exception("PartialOrder input graph is not a upper bounded semi-lattice!");
        int supremum = GraphTypeChecker.getUpperSemiLatticeBound(g);
        
        int n = g.getNVertices();
        
        for(int i = 0;i<n;i++) {
            m_labels.add(g.getVertex(i));
            m_labelIndexes.put(g.getVertex(i), i);
        }
        top = g.getVertex(supremum);
            
        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                if (g.getEdge(i, j)!=null) {
                    addSubsumptionConstraint(m_labels.get(i), m_labels.get(j));
                }
            }
        }
    }

    
    public Label getTop() {
        return top;
    }
        
    
    public void addLabel(Label l) {
        addLabel(l, true);
    }
    
    
    public void addLabel(Label l, boolean inheritFromTop) {
        m_labelIndexes.put(l, m_labels.size());
        m_labels.add(l);
        if (inheritFromTop) addSubsumptionConstraint(top, l);
        
        clearCache();
    }


    public void addSubsumptionConstraint(Label general, Label specific)
    {
        List<Label> tmp = m_subsumptionConstraints.get(general);
        if (tmp==null) {
            tmp = new ArrayList<>();
            m_subsumptionConstraints.put(general, tmp);
        }
        tmp.add(specific);

        clearCache();        
    }
    
    
    public List<Label> getLabels() {
        return m_labels;
    }
    
    
    public Label getLabel(int i) {
        return m_labels.get(i);
    }
    
    
    public boolean contains(Label l) {
        return m_labels.contains(l);
    }
    
    
    public Label[] getChildren(Label l) {
        if (m_childrenCache==null) computeCache();
        return m_childrenCache[m_labelIndexes.get(l)];
    }
    

    public Label[] getParents(Label l) {
        if (m_parentsCache==null) computeCache();
        return m_parentsCache[m_labelIndexes.get(l)];
    }
    
    
    public Label[] getDescendants(Label l) {
        if (m_descendantsCache==null) computeCache();
        return m_descendantsCache[m_labelIndexes.get(l)];
    }
    

    public Label[] getAncestors(Label l) {
        if (m_ancestorsCache==null) computeCache();
        return m_ancestorsCache[m_labelIndexes.get(l)];
    }


    public boolean subsumes(Label l1, Label l2) {
        if (m_subsumptionCache==null) computeCache();
//        if (!m_labelIndexes.containsKey(l1)) System.out.println("Label " + l1 + " not in the PO!!!");
//        if (!m_labelIndexes.containsKey(l2)) System.out.println("Label " + l2 + " not in the PO!!!");
        int idx1 = m_labelIndexes.get(l1);
        int idx2 = m_labelIndexes.get(l2);
        return m_subsumptionCache[idx1][idx2];
    }
    
    
    public DLG translateToDLG() throws Exception {
        int n = m_labels.size();
        Label subsumes_label = new Label(">=");
        DLG g = new DLG(n);
        for(int i = 0;i<n;i++) {
            g.setVertex(i, m_labels.get(i));
        }
        
        for(Label l:m_labels) {
            for(Label l2:getChildren(l)) {
                g.setEdge(m_labelIndexes.get(l), m_labelIndexes.get(l2), subsumes_label);
            }
        }
        return g;
    }
    
    
    void clearCache() {
        m_subsumptionCache = null;
        m_childrenCache = null;
        m_parentsCache = null;
    }
    
    
    public boolean [][]getSubsumptionCache() {
        if (m_subsumptionCache==null) computeCache();
        return m_subsumptionCache;
    }

    
    void computeCache() {
        computeSubsumptionCache();
        computeChildrenCache();
        computeParentsCache();
        computeDescendantsCache();
        computeAncestorsCache();
    }
       
    
    void computeSubsumptionCache()
    {
        int n = m_labels.size();
        m_subsumptionCache = new boolean[n][n];
        
        // labels subsume themselves
        for(int i = 0;i<n;i++) {
            m_subsumptionCache[i][i] = true;    
        }

        // translate the subsumption constraints:
        for(Label l:m_subsumptionConstraints.keySet()) {
            int l_idx = m_labelIndexes.get(l);
            List<Label> tmp = m_subsumptionConstraints.get(l);
            for(Label l2:tmp) {
                int l2_idx = m_labelIndexes.get(l2);
                m_subsumptionCache[l_idx][l2_idx] = true;
            }
        }
        
        // inference:
        do{
            boolean change = false;
            for(int i = 0;i<n;i++) {
                for(int j = 0;j<n;j++) {
                    if (j!=i && m_subsumptionCache[i][j]) {
                        for(int k = 0;k<n;k++) {
                            if (k!=j && m_subsumptionCache[j][k] &&
                                !m_subsumptionCache[i][k]) {
                                m_subsumptionCache[i][k] = true;
                                change = true;
                            }
                        }
                    }
                }
            }
            if (!change) break;
        }while(true);
    }


    void computeChildrenCache()
    {
        int n = m_labels.size();
        m_childrenCache = new Label[n][];
        for(int i = 0;i<n;i++) {
            List<Integer> descendants = new ArrayList<>();
            for(int j = 0;j<n;j++) {
                if (m_subsumptionCache[i][j] && !m_subsumptionCache[j][i]) {
                    boolean immediateChildren = true;
                    for(int k = 0;k<n;k++) {
                        if (k!=i && k!=j) {
                            if (m_subsumptionCache[i][k] && m_subsumptionCache[k][j]) {
                                immediateChildren = false;
                                break;
                            }
                        }
                    }
                    if (immediateChildren) descendants.add(j);
                }
            }
            m_childrenCache[i] = new Label[descendants.size()];
            for(int j = 0;j<descendants.size();j++) {
                m_childrenCache[i][j] = m_labels.get(descendants.get(j));
            }
        }
    }
    
    
    void computeParentsCache()
    {
        int n = m_labels.size();
        m_parentsCache = new Label[n][];
        for(int i = 0;i<n;i++) {
            List<Integer> ascendants = new ArrayList<>();
            for(int j = 0;j<n;j++) {
                if (!m_subsumptionCache[i][j] && m_subsumptionCache[j][i]) {
                    boolean immediateChildren = true;
                    for(int k = 0;k<n;k++) {
                        if (k!=i && k!=j) {
                            if (m_subsumptionCache[j][k] && m_subsumptionCache[k][i]) {
                                immediateChildren = false;
                                break;
                            }
                        }
                    }
                    if (immediateChildren) ascendants.add(j);
                }
            }
            m_parentsCache[i] = new Label[ascendants.size()];
            for(int j = 0;j<ascendants.size();j++) {
                m_parentsCache[i][j] = m_labels.get(ascendants.get(j));
            }
        }
    }    

    
    void computeDescendantsCache()
    {
        int n = m_labels.size();
        m_descendantsCache = new Label[n][];
        for(int i = 0;i<n;i++) {
            List<Integer> descendants = new ArrayList<>();
            for(int j = 0;j<n;j++) {
                if (m_subsumptionCache[i][j]) descendants.add(j);
            }
            m_descendantsCache[i] = new Label[descendants.size()];
            for(int j = 0;j<descendants.size();j++) {
                m_descendantsCache[i][j] = m_labels.get(descendants.get(j));
            }
        }
    }
    
    
    void computeAncestorsCache()
    {
        int n = m_labels.size();
        m_ancestorsCache = new Label[n][];
        for(int i = 0;i<n;i++) {
            List<Integer> ascendants = new ArrayList<>();
            for(int j = 0;j<n;j++) {
                if (m_subsumptionCache[j][i] && i!=j) ascendants.add(j);
            }
            m_ancestorsCache[i] = new Label[ascendants.size()];
            for(int j = 0;j<ascendants.size();j++) {
                m_ancestorsCache[i][j] = m_labels.get(ascendants.get(j));
            }
        }
    }    
    
        
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.refinement.base.FlatRefinement;
import dlg.core.refinement.IncrementalRefinementOperator;
import dlg.core.refinement.RefinementOperator;
import dlg.core.refinement.filtered.FlatRefinementOperatorFilteredByBottomInstances;
import dlg.core.refinement.base.TreeFlatRefinement;
import dlg.core.subsumption.FlatSubsumption;
import dlg.core.subsumption.Subsumption;
import dlg.core.subsumption.TreeFlatSubsumption;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author santi
 */
public class RefinementPathLengthCalculator {
    public static int DEBUG = 0;    
    public static boolean reuse_mappings = true;
    
    // number of refinement operators needed to apply to g1 to get g2:
    public static int refinementPathLength(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {        
        if (s.subsumes(g1, g2)==null) return -1;
        
        // special cases, where this can be done faster:
        if ((s instanceof FlatSubsumption) && ((FlatSubsumption)s).getObjectIdentity()) {
            RefinementOperator baserho = rho;
            if (rho instanceof FlatRefinementOperatorFilteredByBottomInstances) baserho = ((FlatRefinementOperatorFilteredByBottomInstances)rho).getBaseRho();
            if ((baserho instanceof FlatRefinement)) {
                return simpleSizeDifference(g1, g2);
            }
        } else if ((s instanceof TreeFlatSubsumption) && ((TreeFlatSubsumption)s).getObjectIdentity()) {
            RefinementOperator baserho = rho;
            if (rho instanceof FlatRefinementOperatorFilteredByBottomInstances) baserho = ((FlatRefinementOperatorFilteredByBottomInstances)rho).getBaseRho();
            if ((baserho instanceof TreeFlatRefinement)) {
                return simpleSizeDifference(g1, g2);
            }
        }
        
        if (reuse_mappings) return refinementPathLengthInternalReusingMapping(g1, g2, s, rho);
                       else return refinementPathLengthInternal(g1, g2, s, rho);
    }  
    

    static int refinementPathLengthInternal(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        DLG current = g1;
        int steps = 0;

        do {
            DLG next = null;
            if (rho instanceof IncrementalRefinementOperator) {
                // if we have an incremental operator, exploit it!
                IncrementalRefinementOperator irho = (IncrementalRefinementOperator)rho;
                irho.setDLGForDownwardRefinement(current);
                int n = 0;
                do{
                    DLG candidate = irho.getNextDownwardRefinement();
                    if (candidate == null) break;
                    if (DEBUG>=3) System.out.println("RefinementDistance.refinementDistance: current refinement\n" + candidate);
                    n++;
                    if (s.subsumes(candidate, g2)!=null) {
                        next = candidate;
                        steps++;
                        break;
                    }
                } while(true);
                if (DEBUG>=1) System.out.println("RefinementDistance.refinementDistance: refinements tested " + n);
            } else {
                List<? extends DLG> refinements = rho.downwardRefinements(current);
                if (DEBUG>=1) System.out.println("RefinementDistance.refinementDistance: refinements " + refinements.size());
                for(DLG candidate: refinements) {
                    if (s.subsumes(candidate, g2)!=null) {
                        next = candidate;
                        steps++;
                        break;
                    }
                }
            }
            if (next == null) return steps;
            current = next;
            if (DEBUG>=2) {
                System.out.println("RefinementDistance.refinementDistance: current:");
                System.out.println(current);
            }
        }while(true);
    }

    
    static int refinementPathLengthInternalReusingMapping(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        DLG current = g1;
        int steps = 0;
        int last_mapping[] = null;

        do {
            DLG next = null;
            if (rho instanceof IncrementalRefinementOperator) {
                // if we have an incremental operator, exploit it!
                IncrementalRefinementOperator irho = (IncrementalRefinementOperator)rho;
                irho.setDLGForDownwardRefinement(current);
                int n = 0;
                do{
                    DLG candidate = irho.getNextDownwardRefinement();
                    if (candidate == null) break;
                    if (DEBUG>=3) System.out.println("RefinementDistance.refinementDistance: current refinement\n" + candidate);
                    n++;
                    int []mapping = null;
                    if (last_mapping!=null && candidate.getNVertices() == last_mapping.length) {
                        if (DEBUG>=2) System.out.println("mapping reused");
                        mapping = s.subsumes(candidate, g2, last_mapping);
                    } else {
                        if (DEBUG>=2) System.out.println("mapping not reused (" + candidate.getNVertices() + " and last was " + Arrays.toString(last_mapping) + ")");
                        mapping = s.subsumes(candidate, g2);
                    }
                    if (mapping!=null) {
                        last_mapping = mapping;
                        next = candidate;
                        steps++;
                        break;
                    }
                } while(true);
                if (DEBUG>=1) System.out.println("RefinementDistance.refinementDistance: refinements tested " + n);
            } else {
                List<? extends DLG> refinements = rho.downwardRefinements(current);
                if (DEBUG>=1) System.out.println("RefinementDistance.refinementDistance: refinements " + refinements.size());
                for(DLG candidate: refinements) {
                    int []mapping = null;
                    if (last_mapping!=null && candidate.getNVertices() == last_mapping.length) {
                        mapping = s.subsumes(candidate, g2, last_mapping);
                    } else {
                        mapping = s.subsumes(candidate, g2);
                    }
                    if (mapping!=null) {
                        last_mapping = mapping;
                        next = candidate;
                        steps++;
                        break;
                    }
                }
            }
            if (next == null) return steps;
            current = next;
            if (DEBUG>=2) {
                System.out.println("RefinementDistance.refinementDistance: current:");
                System.out.println(current);
                System.out.println("last_mapping: " + Arrays.toString(last_mapping));
            }
        }while(true);
    }
    
    
    public static int simpleSizeDifference(DLG g1, DLG g2) {
        int g1size = DLGsize(g1);
        int g2size = DLGsize(g2);
        
        return g2size - g1size;
    }
    
    public static int DLGsize(DLG g) {
        int size = g.getNVertices();
        if (size==0) {
            return 0;
        } else {
            size = 1;   // since vertices and edges are added together, only the first vertex is not counted in the size computed below
            for(int i = 0;i<g.getNVertices();i++) {
                size += g.getCondensedOutgoingEdges()[i].length;
            }
        }
        return size;
    }
}

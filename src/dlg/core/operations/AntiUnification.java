/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.refinement.IncrementalRefinementOperator;
import dlg.core.refinement.RefinementOperator;
import dlg.core.subsumption.Subsumption;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Pair;

/**
 *
 * @author santi
 */
public class AntiUnification {
    public static int DEBUG = 0;

    
    public static class AntiUnificationResult {
        public DLG m_antiunifier = null;
        public int m_steps;
        public List<int[]> m_mappings;
        
        public AntiUnificationResult(DLG a_antiunifier, int a_steps, List<int[]> a_mappings) {
            m_antiunifier = a_antiunifier;
            m_steps = a_steps;
            m_mappings = a_mappings;
        }
    }
    
    
    public static DLG singleAntiunification(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        return singleAntiunificationSteps(gs, s, rho).m_a;
    }    

    
    public static DLG singleAntiunification(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(g1);
        l.add(g2);
        return singleAntiunificationSteps(l, s, rho).m_a;
    }

    
    public static Pair<DLG,Integer> singleAntiunificationSteps(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(g1);
        l.add(g2);
        return singleAntiunificationSteps(l, s, rho);
    }
   

    public static Pair<DLG,Integer> singleAntiunificationSteps(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        AntiUnificationResult result = singleAntiunificationWithMappings(gs, s, rho);
        return new Pair<DLG,Integer>(result.m_antiunifier, result.m_steps);
    }    
    
    
    public static AntiUnificationResult singleAntiunificationWithMappings(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        DLG au = rho.getTop();
        int steps = 0;
        List<int []> mappings = null;
        
        do {
            DLG next_au = null;
            List<int []> next_mappings = null;
            if (rho instanceof IncrementalRefinementOperator) {
                // if we have an incremental operator, exploit it!
                IncrementalRefinementOperator irho = (IncrementalRefinementOperator)rho;
                irho.setDLGForDownwardRefinement(au);
                int nrefinements_generated = 0;
                do{
                    DLG candidate_au = irho.getNextDownwardRefinement();
                    if (candidate_au == null) break;
                    nrefinements_generated++;
                    List<int []> candidate_mappings = new ArrayList<>();
                    for(DLG g:gs) {
                        int []m = s.subsumes(candidate_au, g);
                        if (m==null) {
                            candidate_mappings = null;
                            break;
                        } else {
                            candidate_mappings.add(m);
                        }
                    }
                    if (candidate_mappings!=null) {
                        next_au = candidate_au;
                        next_mappings = candidate_mappings;
                        steps++;
                        break;
                    }
                } while(true);
                if (DEBUG>=1) System.out.println("AntiUnification.singleAntiunification: incremental refinements " + nrefinements_generated);
            } else {
                List<? extends DLG> refinements = rho.downwardRefinements(au);
                if (DEBUG>=1) System.out.println("AntiUnification.singleAntiunification: refinements " + refinements.size());
                List<int []> candidate_mappings = new ArrayList<>();
                for(DLG candidate_au: refinements) {
                    for(DLG g:gs) {
                        int []m = s.subsumes(candidate_au, g);
                        if (m==null) {
                            candidate_mappings = null;
                            break;
                        } else {
                            candidate_mappings.add(m);
                        }
                    }
                    if (candidate_mappings!=null) {
                        next_au = candidate_au;
                        next_mappings = candidate_mappings;
                        steps++;
                        break;
                    }
                }
            }
            if (next_au == null) return new AntiUnificationResult(au, steps, mappings);
            au = next_au;
            mappings = next_mappings;
            if (DEBUG>=2) {
                System.out.println("AntiUnification.singleAntiunification: current antiunification:");
                System.out.println(au);
            }
        }while(true);
    }

}

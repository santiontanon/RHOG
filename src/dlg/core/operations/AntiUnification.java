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
        DLG au = rho.getTop();
        int steps = 0;
        
        do {
            DLG next_au = null;
            if (rho instanceof IncrementalRefinementOperator) {
                // if we have an incremental operator, exploit it!
                IncrementalRefinementOperator irho = (IncrementalRefinementOperator)rho;
                irho.setDLGForDownwardRefinement(au);
                int nrefinements_generated = 0;
                do{
                    DLG candidate_au = irho.getNextDownwardRefinement();
                    if (candidate_au == null) break;
                    nrefinements_generated++;
                    boolean subsumeAll = true;
                    for(DLG g:gs) {
                        if (s.subsumes(candidate_au, g)==null) {
                            subsumeAll = false;
                            break;
                        }
                    }
                    if (subsumeAll) {
                        next_au = candidate_au;
                        steps++;
                        break;
                    }
                } while(true);
                if (DEBUG>=1) System.out.println("AntiUnification.singleAntiunification: incremental refinements " + nrefinements_generated);
            } else {
                List<? extends DLG> refinements = rho.downwardRefinements(au);
                if (DEBUG>=1) System.out.println("AntiUnification.singleAntiunification: refinements " + refinements.size());
                for(DLG candidate_au: refinements) {
                    boolean subsumeAll = true;
                    for(DLG g:gs) {
                        if (s.subsumes(candidate_au, g)==null) {
                            subsumeAll = false;
                            break;
                        }
                    }
                    if (subsumeAll) {
                        next_au = candidate_au;
                        steps++;
                        break;
                    }
                }
            }
            if (next_au == null) return new Pair<>(au, steps);
            au = next_au;
            if (DEBUG>=2) {
                System.out.println("AntiUnification.singleAntiunification: current antiunification:");
                System.out.println(au);
            }
        }while(true);
    }

}

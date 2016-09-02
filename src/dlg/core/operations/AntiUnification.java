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
    
    
    public static List<AntiUnificationResult> allAntiunificationsWithMappings(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        List<int []> mappings = new ArrayList<>();
        for(DLG g:gs) mappings.add(new int[0]);
        AntiUnificationResult baseResult = new AntiUnificationResult(rho.getTop(), 0, mappings);
        List<AntiUnificationResult> open = new ArrayList<>();
        List<AntiUnificationResult> candidates = new ArrayList<>();
        open.add(baseResult);
        
        do {
            if (DEBUG>=1) System.out.println("AntiUnification.allAntiunificationsWithMappings: open " + open.size() + ", candidates " + candidates.size());
//            System.out.println("First DLG:\n" + open.get(0).m_antiunifier);
            List<AntiUnificationResult> nextOpen = new ArrayList<>();
            for(AntiUnificationResult next:open) {
                List<? extends DLG> refinements = rho.downwardRefinements(next.m_antiunifier);                        
                if (DEBUG>=2) System.out.println("AntiUnification.allAntiunificationsWithMappings: refinements " + refinements.size());

                boolean anyKept = false;
                for(DLG r:refinements) {
                    // check if it is an antiunification:
                    AntiUnificationResult current = isAntiUnifierCandidate(r, gs, s);
                    if (current!=null) {
                        current.m_steps = next.m_steps+1;
                        boolean keep = true;
                        List<AntiUnificationResult> toDeleteCandidates = new ArrayList<>();
                        List<AntiUnificationResult> toDeleteNextOpen = new ArrayList<>();
/*                        for(AntiUnificationResult previous:open) {
                            if (s.subsumes(previous.m_antiunifier, current.m_antiunifier)!=null &&
                                s.subsumes(current.m_antiunifier, previous.m_antiunifier)!=null) {
                                keep = false;
                                System.out.println("current:");
                                System.out.println(current.m_antiunifier);
                                System.out.println("previous:");
                                System.out.println(previous.m_antiunifier);
                            }
                        }*/
                        if (keep) {
//                            for(AntiUnificationResult previous:candidates) {
//                                if (s.subsumes(previous.m_antiunifier, current.m_antiunifier)!=null ||
//                                    s.subsumes(current.m_antiunifier, previous.m_antiunifier)!=null) {
//                                    toDeleteCandidates.add(previous);
//                                }
//                            }
                            for(AntiUnificationResult previous:nextOpen) {
                                if (s.subsumes(previous.m_antiunifier, current.m_antiunifier)!=null) {
                                    keep = false;
                                    break;
                                } else {
                                    if (s.subsumes(current.m_antiunifier, previous.m_antiunifier)!=null) {
                                        toDeleteNextOpen.add(previous);
                                    }
                                }
                            }
                        }
                        if (keep) {
                            candidates.removeAll(toDeleteCandidates);
                            nextOpen.removeAll(toDeleteNextOpen);
                            nextOpen.add(current);
                            anyKept = true;
                        }
                    }
                }
                if (!anyKept) {
                    if (DEBUG>=2) System.out.println("anyKept = false");
                    boolean keep = true;
                    List<AntiUnificationResult> toDeleteCandidates = new ArrayList<>();
                    for(AntiUnificationResult previous:candidates) {
                        if (s.subsumes(previous.m_antiunifier, next.m_antiunifier)!=null) {
                            toDeleteCandidates.add(previous);
                        } else if (s.subsumes(next.m_antiunifier, previous.m_antiunifier)!=null) {
                            keep = false;
                            break;
                        }
                    }
                    if (keep) {
                        if (DEBUG>=2) System.out.println("candidate kept");
                        candidates.removeAll(toDeleteCandidates);
                        candidates.add(next);
                    }
                }
            }
            open = nextOpen;
        }while(!open.isEmpty());
        return candidates;
    }    
    
    
    static AntiUnificationResult isAntiUnifierCandidate(DLG au, List<DLG> gs, Subsumption s)
    {
        List<int []> candidate_mappings = new ArrayList<>();
        for(DLG g:gs) {
            int []m = s.subsumes(au, g);
            if (m==null) {
                candidate_mappings = null;
                break;
            } else {
                candidate_mappings.add(m);
            }
        }
        if (candidate_mappings!=null) return new AntiUnificationResult(au, 0, candidate_mappings);
        return null;
    }

}

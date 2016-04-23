/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.refinement.base.FlatRefinement;
import dlg.core.refinement.base.FlatTransRefinement;
import dlg.core.refinement.base.PORefinement;
import dlg.core.refinement.base.POTransRefinement;
import dlg.core.refinement.RefinementOperator;
import dlg.core.refinement.base.TreeFlatRefinement;
import dlg.core.refinement.base.TreeFlatTransRefinement;
import dlg.core.refinement.base.TreePORefinement;
import dlg.core.refinement.base.TreePOTransRefinement;
import dlg.core.refinement.filtered.PORefinementFiltered;
import dlg.core.refinement.filtered.TreePORefinementFiltered;
import dlg.core.subsumption.FlatSubsumption;
import dlg.core.subsumption.FlatTransSubsumption;
import dlg.core.subsumption.POSubsumption;
import dlg.core.subsumption.POTransSubsumption;
import dlg.core.subsumption.Subsumption;
import dlg.core.subsumption.TreeFlatSubsumption;
import dlg.core.subsumption.TreeFlatTransSubsumption;
import dlg.core.subsumption.TreePOSubsumption;
import dlg.core.subsumption.TreePOTransSubsumption;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author santi
 */
public class RefinementTests {
    
    public static int DEBUG = 0;
    
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Refinement");

        errors += FlatRefinementTest();
        errors += TreeFlatRefinementTest();
        errors += PORefinementTest();
        errors += TreePORefinementTest();

        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
    
    public static int FlatRefinementTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allDLGs();
        List<Label> vertexLabels = new ArrayList<>();
        for(DLG g:gs) {
            for(Label l:g.getAllVertexLabels()) 
                if (!vertexLabels.contains(l)) vertexLabels.add(l);
        }
        List<Label> edgeLabels = new ArrayList<>();    
        for(DLG g:gs) {
            for(Label l:g.getAllEdgeLabels()) 
                if (!edgeLabels.contains(l)) edgeLabels.add(l);
        }
        
        errors += propernessTest(gs, new FlatSubsumption(true), new FlatRefinement(vertexLabels, edgeLabels));
        errors += propernessTest(gs, new FlatTransSubsumption(true), new FlatTransRefinement(vertexLabels, edgeLabels));
        
        errors += completenessTest(gs, new FlatSubsumption(true), new FlatRefinement(vertexLabels, edgeLabels));
        errors += completenessTest(gs, new FlatTransSubsumption(true), new FlatTransRefinement(vertexLabels, edgeLabels));

        return errors;
    }
    
    
    public static int TreeFlatRefinementTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allTreeDLGs();
        List<Label> vertexLabels = new ArrayList<>();
        for(DLG g:gs) {
            for(Label l:g.getAllVertexLabels()) 
                if (!vertexLabels.contains(l)) vertexLabels.add(l);
        }
        List<Label> edgeLabels = new ArrayList<>();    
        for(DLG g:gs) {
            for(Label l:g.getAllEdgeLabels()) 
                if (!edgeLabels.contains(l)) edgeLabels.add(l);
        }
        
        errors += propernessTest(gs, new TreeFlatSubsumption(true), new TreeFlatRefinement(vertexLabels, edgeLabels));
        errors += propernessTest(gs, new TreeFlatTransSubsumption(true), new TreeFlatTransRefinement(vertexLabels, edgeLabels));

        errors += completenessTest(gs, new TreeFlatSubsumption(true), new TreeFlatRefinement(vertexLabels, edgeLabels));
        errors += completenessTest(gs, new TreeFlatTransSubsumption(true), new TreeFlatTransRefinement(vertexLabels, edgeLabels));
        
        return errors;
    }
    

    public static int PORefinementTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += propernessTest(gs, new POSubsumption(true, po), new PORefinement(po));
        errors += propernessTest(gs, new POSubsumption(true, po), new PORefinementFiltered(po, gs));
        errors += propernessTest(gs, new POTransSubsumption(true, po), new POTransRefinement(po));
        
        errors += completenessTest(gs, new POSubsumption(true, po), new PORefinement(po));
        errors += completenessTest(gs, new POSubsumption(true, po), new PORefinementFiltered(po, gs));
        errors += completenessTest(gs, new POTransSubsumption(true, po), new POTransRefinement(po));

        return errors;
    }


    public static int TreePORefinementTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allTreeDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += propernessTest(gs, new TreePOSubsumption(true, po), new TreePORefinement(po));
        errors += propernessTest(gs, new TreePOSubsumption(true, po), new TreePORefinementFiltered(po, gs));
        errors += propernessTest(gs, new TreePOTransSubsumption(true, po), new TreePOTransRefinement(po));

        errors += completenessTest(gs, new TreePOSubsumption(true, po), new TreePORefinement(po));
        errors += completenessTest(gs, new TreePOSubsumption(true, po), new TreePORefinementFiltered(po, gs));
        errors += completenessTest(gs, new TreePOTransSubsumption(true, po), new TreePOTransRefinement(po));
        
        return errors;
    }


    
    public static int propernessTest(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        int errors = 0;
        for(DLG g:gs) errors += propernessTest(g, s, rho);
        return errors;
    }


    public static int propernessTest(DLG g, Subsumption s, RefinementOperator rho) throws Exception {
        int errors = 0;
        System.out.println("TEST START: properness " + rho.getClass().getSimpleName() + " with " + s.getClass().getSimpleName());
        
        List<? extends DLG> dw_refinements = rho.downwardRefinements(g);
        for(DLG r:dw_refinements) {
            if (s.subsumes(g, r)==null) {
                errors++;
                System.out.println("Downward refinement of " + rho.getClass().getSimpleName() + " is not subsumed by original DLG");
                System.out.println("original DLG:\n" + g);
                System.out.println("refinement:\n" + r);
            }
            if (s.subsumes(r, g)!=null) {
                errors++;
                System.out.println("Downward refinement of " + rho.getClass().getSimpleName() + " subsumes original DLG");
                System.out.println("original DLG:\n" + g);
                System.out.println("refinement:\n" + r);
                System.out.println("Rerunning in DEBUG mode:");
                /*
                POSubsumption.DEBUG = 1;
                int []mapping = s.subsumes(r, g);
                System.out.println("mapping: " + Arrays.toString(mapping));
                POSubsumption.DEBUG = 0;
                System.exit(1);
                */
            }
        }
        
        List<? extends DLG> uw_refinements = rho.upwardRefinements(g);
        for(DLG r:uw_refinements) {
            if (s.subsumes(g, r)!=null) {
                errors++;
                System.out.println("Upward refinement of " + rho.getClass().getSimpleName() + " does not subsume original DLG");
                System.out.println("original DLG:\n" + g);
                System.out.println("refinement:\n" + r);
            }
            if (s.subsumes(r, g)==null) {
                errors++;
                System.out.println("Upward refinement of " + rho.getClass().getSimpleName() + " is subsumed by original DLG");
                System.out.println("original DLG:\n" + g);
                System.out.println("refinement:\n" + r);
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");
        return errors;
    }

    
    public static int completenessTest(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        int errors = 0;
        int tests = 0;
        System.out.println("TEST START: completeness " + rho.getClass().getSimpleName() + " with " + s.getClass().getSimpleName());

        for(int i = 0;i<gs.size();i++) {
            for(int j = 0;j<gs.size();j++) {
                if (s.subsumes(gs.get(i), gs.get(j))!=null &&
                    s.subsumes(gs.get(j), gs.get(i))==null) {
                    errors += downwardReachabilityTest(gs.get(i), gs.get(j), s, rho);
                    tests ++;

                    errors += upwardReachabilityTest(gs.get(i), gs.get(j), s, rho);
                    tests ++;
                }
            }
        }
        
        
        for(DLG g:gs) {
            errors += downwardReachabilityTest(rho.getTop(), g, s, rho);
            tests ++;

            errors += upwardReachabilityTest(rho.getTop(), g, s, rho);
            tests ++;
            
            for(DLG r:rho.downwardRefinements(g)) {
                errors += upwardReachabilityTest(r, g, s, rho);
                tests ++;
            }
            for(DLG r:rho.upwardRefinements(g)) {
                errors += downwardReachabilityTest(r, g, s, rho);
                tests ++;
            }
        }
        
        System.out.println("TEST END: " + errors + " errors out of " + tests);
        return errors;
    }
    
    
    public static int downwardReachabilityTest(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        if (s.subsumes(g1, g2)==null) return 0; // g1 does not subsume g2:
        
        DLG current = g1;
        if (DEBUG>=1) System.out.println("downwardReachabilityTest:");
        while(s.subsumes(g2,current)==null) {
            DLG last = current;
            List<? extends DLG> refinements = rho.downwardRefinements(current);
            if (DEBUG>=1) System.out.println(refinements.size() + " refinements");
            current = null;
            for(DLG r:refinements) {
                if (s.subsumes(r, g2)!=null) {
                if (DEBUG>=1) System.out.println("next:\n" + r);
                    current = r;
                    break;
                }
            }
            if (current == null) {
                // cannot reach g2!
                System.out.println("downwardReachabilityTest failed");
                System.out.println("g1:\n" + g1);
                System.out.println("g2:\n" + g2);
                System.out.println("last (had "+refinements.size()+" refinements):\n" + last);
                /*
                if (DEBUG==0) {
                    System.out.println("re-running with DEBUG = 1");
                    DEBUG = 1;
                    PORefinementFiltered.DEBUG = 2;
                    downwardReachabilityTest(g1, g2, s, rho);
                    PORefinementFiltered.DEBUG = 0;
                    DEBUG = 0;
                }*/
                return 1;
            }
        }
        
        return 0;
    }
            

    public static int upwardReachabilityTest(DLG g1, DLG g2, Subsumption s, RefinementOperator rho) throws Exception {
        if (s.subsumes(g1, g2)==null) return 0; // g1 does not subsume g2:
        
        DLG current = g2;
        while(s.subsumes(current, g1)==null) {
            List<? extends DLG> refinements = rho.upwardRefinements(current);
            current = null;
            for(DLG r:refinements) {
                if (s.subsumes(g1, r)!=null) {
                    current = r;
                    break;
                }
            }
            if (current == null) {
                // cannot reach g1!
                System.out.println("upwardReachabilityTest failed");
                System.out.println("g1:\n" + g1);
                System.out.println("g2:\n" + g2);
                return 1;
            }
        }
        
        return 0;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.operations.AntiUnification;
import dlg.core.operations.Disintegration;
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
import java.util.List;

/**
 *
 * @author santi
 */
public class DisintegrationTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Disintegration");

        errors += FlatDisintegrationTest();
        errors += TreeFlatDisintegrationTest();
        errors += PODisintegrationTest();
        errors += TreePODisintegrationTest();

        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
    
    public static int FlatDisintegrationTest() throws Exception {
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
        
        errors += disintegrationTest(gs, new FlatSubsumption(true), new FlatRefinement(vertexLabels, edgeLabels));
        errors += disintegrationTest(gs, new FlatTransSubsumption(true), new FlatTransRefinement(vertexLabels, edgeLabels));
        
        return errors;
    }
    
    
    public static int TreeFlatDisintegrationTest() throws Exception {
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
        
        errors += disintegrationTest(gs, new TreeFlatSubsumption(true), new TreeFlatRefinement(vertexLabels, edgeLabels));
        errors += disintegrationTest(gs, new TreeFlatTransSubsumption(true), new TreeFlatTransRefinement(vertexLabels, edgeLabels));

        return errors;
    }    
    

    public static int PODisintegrationTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += disintegrationTest(gs, new POSubsumption(true, po), new PORefinement(po));
        errors += disintegrationTest(gs, new POSubsumption(true, po), new PORefinementFiltered(po, gs));
        errors += disintegrationTest(gs, new POTransSubsumption(true, po), new POTransRefinement(po));
        
        return errors;
    }
    
    
    public static int TreePODisintegrationTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allTreeDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += disintegrationTest(gs, new TreePOSubsumption(true, po), new TreePORefinement(po));
        errors += disintegrationTest(gs, new TreePOSubsumption(true, po), new TreePORefinementFiltered(po, gs));
        errors += disintegrationTest(gs, new TreePOTransSubsumption(true, po), new TreePOTransRefinement(po));

        return errors;
    }  
    
    
    public static int disintegrationTest(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        int tests = 0;
        int errors = 0;
        System.out.println("TEST START: disintegration " + rho.getClass().getSimpleName() + " with " + s.getClass().getSimpleName());

        for(DLG g:gs) {
            List<DLG> properties = Disintegration.greedyDisintegration(g, s, rho);
            for(DLG p:properties) {
                tests++;
                if (s.subsumes(p, g)==null) {
                    errors++;
                    System.out.println("Property does not subsume original term");
                }
            }
        }

        System.out.println("TEST END: " + errors + " errors out of " + tests);
        return errors;
    }
    
    
}

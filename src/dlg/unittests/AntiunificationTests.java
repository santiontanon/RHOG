/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.operations.AntiUnification;
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
public class AntiunificationTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Antiunification");

        errors += FlatAntiunificationTest();
        errors += TreeFlatAntiunificationTest();
        errors += POAntiunificationTest();
        errors += TreePOAntiunificationTest();
        
        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    

    public static int FlatAntiunificationTest() throws Exception {
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
        
        errors += antiunificationTest(gs, new FlatSubsumption(true), new FlatRefinement(vertexLabels, edgeLabels));
        errors += antiunificationTest(gs, new FlatTransSubsumption(true), new FlatTransRefinement(vertexLabels, edgeLabels));

        return errors;
    }
    
    
    public static int TreeFlatAntiunificationTest() throws Exception {
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
        
        errors += antiunificationTest(gs, new TreeFlatSubsumption(true), new TreeFlatRefinement(vertexLabels, edgeLabels));
        errors += antiunificationTest(gs, new TreeFlatTransSubsumption(true), new TreeFlatTransRefinement(vertexLabels, edgeLabels));

        return errors;
    }
    
    
    public static int POAntiunificationTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += antiunificationTest(gs, new POSubsumption(true, po), new PORefinement(po));
        errors += antiunificationTest(gs, new POSubsumption(true, po), new PORefinementFiltered(po, gs));
        errors += antiunificationTest(gs, new POTransSubsumption(true, po), new POTransRefinement(po));
        

        return errors;
    }
    
    
    public static int TreePOAntiunificationTest() throws Exception {
        int errors = 0;
        
        List<DLG> gs = SampleDLGs.allTreeDLGs();
        PartialOrder po = SampleDLGs.getPartialOrder();
        
        errors += antiunificationTest(gs, new TreePOSubsumption(true, po), new TreePORefinement(po));
        errors += antiunificationTest(gs, new TreePOSubsumption(true, po), new TreePORefinementFiltered(po, gs));
        errors += antiunificationTest(gs, new TreePOTransSubsumption(true, po), new TreePOTransRefinement(po));

        return errors;
    }    
    
    
    public static int antiunificationTest(List<DLG> gs, Subsumption s, RefinementOperator rho) throws Exception {
        int errors = 0;
        System.out.println("TEST START: antiunification " + rho.getClass().getSimpleName() + " with " + s.getClass().getSimpleName());

        for(int i = 0;i<gs.size();i++) {
            for(int j = 0;j<gs.size();j++) {
                DLG au = AntiUnification.singleAntiunification(gs.get(i), gs.get(j), s, rho);
                
                if (s.subsumes(au, gs.get(i))==null ||
                    s.subsumes(au, gs.get(j))==null) {
                    errors++;
                    System.out.println("Antiunification does not subsume one of the terms");
                }
                
                for(DLG r:rho.downwardRefinements(au)) {
                    if (s.subsumes(r, gs.get(i))!=null &&
                        s.subsumes(r, gs.get(j))!=null) {
                        errors++;
                        System.out.println("Antiunification is not most specific");
                    }
                }
            }
        }

        System.out.println("TEST END: " + errors + " errors");
        return errors;
    }
    
}

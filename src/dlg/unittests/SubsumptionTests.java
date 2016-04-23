/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author santi
 */
public class SubsumptionTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Subsumption");
        
        errors +=testFlatSubsumption();
        errors +=testFlatTransSubsumption();
        errors +=testTreeFlatSubsumption();
        errors +=testTreeFlatTransSubsumption();
        errors +=testPOSubsumption();
        errors +=testPOTransSubsumption();
        errors +=testTreePOSubsumption();
        errors +=testTreePOTransSubsumption();
        
        errors +=testSubsumptionSubfunctions();

        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
    
    public static int testFlatSubsumption() throws Exception {
        int errors = 0;
        
        System.out.println("TEST START: FlatSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4(),
                    SampleDLGs.DLG1(), };
        boolean expectedResults[][] = { {true,  true,  false, false, false},
                                        {false, true,  false, false, false},
                                        {false, false, true,  false, false},
                                        {false, false, false, true , true },
                                        {false, false, false, false , true} };
        boolean expectedResultsOI[][] = { {true,  true,  false, false, false},
                                          {false, true,  false, false, false},
                                          {false, false, true,  false, false},
                                          {false, false, false, true , true },
                                          {false, false, false, false , true} };
        Subsumption s = new FlatSubsumption(false);
        Subsumption s_OI = new FlatSubsumption(true);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("FlatSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("FlatSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }
    
    
    public static int testFlatTransSubsumption() throws Exception {
        int errors = 0;

        System.out.println("TEST START: FlatTransSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4(),
                    SampleDLGs.DLG1(), };
        boolean expectedResults[][] = { {true,  true,  true,  false, false},
                                        {false, true,  true,  false, false},
                                        {false, false, true,  false, false},
                                        {false, false, false, true , true },
                                        {false, false, false, true , true} };
        boolean expectedResultsOI[][] = { {true,  true,  true,  false, false},
                                          {false, true,  false, false, false},
                                          {false, false, true,  false, false},
                                          {false, false, false, true , true },
                                          {false, false, false, false , true} };
        Subsumption s = new FlatTransSubsumption(false);
        Subsumption s_OI = new FlatTransSubsumption(true);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("FlatTransSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("Mapping is: " + Arrays.toString(s.subsumes(g[i], g[j])));
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("FlatTransSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("Mapping is: " + Arrays.toString(s.subsumes(g[i], g[j])));
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }  
    
    
    public static int testTreeFlatSubsumption() throws Exception {
        int errors = 0;

        System.out.println("TEST START: TreeFlatSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4() };
        boolean expectedResults[][] = { {true,  true,  false, false},
                                        {false, true,  false, false},
                                        {false, false, true,  false},
                                        {false, false, false, true } };
        boolean expectedResultsOI[][] = { {true,  true,  false, false},
                                          {false, true,  false, false},
                                          {false, false, true,  false},
                                          {false, false, false, true } };
        Subsumption s = new TreeFlatSubsumption(false);
        Subsumption s_OI = new TreeFlatSubsumption(true);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("TreeFlatSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("TreeFlatSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }    

    
    public static int testTreeFlatTransSubsumption() throws Exception {
        int errors = 0;
        System.out.println("TEST START: TreeFlatTransSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4() };
        boolean expectedResults[][] = { {true,  true,  true,  false},
                                        {false, true,  true,  false},
                                        {false, false, true,  false},
                                        {false, false, false, true } };
        boolean expectedResultsOI[][] = { {true,  true,  true,  false},
                                          {false, true,  false, false},
                                          {false, false, true,  false},
                                          {false, false, false, true } };
        Subsumption s = new TreeFlatTransSubsumption(false);
        Subsumption s_OI = new TreeFlatTransSubsumption(true);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("TreeFlatTransSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("TreeFlatTransSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }    
    
    
    public static int testPOSubsumption() throws Exception {
        int errors = 0;
        
        System.out.println("TEST START: POSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4(),
                    SampleDLGs.DLG1(), };
        PartialOrder po = SampleDLGs.getPartialOrder();
        boolean expectedResults[][] = { {true,  true,  false, false, false},
                                        {false, true,  false, false, false},
                                        {false, false, true,  false, false},
                                        {false, false, false, true , true },
                                        {false, false, false, false , true} };
        boolean expectedResultsOI[][] = { {true,  true,  false, false, false},
                                          {false, true,  false, false, false},
                                          {false, false, true,  false, false},
                                          {false, false, false, true , true },
                                          {false, false, false, false , true} };
        Subsumption s = new POSubsumption(false, po);
        Subsumption s_OI = new POSubsumption(true, po);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("POSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("POSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }
    
    
    public static int testPOTransSubsumption() throws Exception {
        int errors = 0;

        System.out.println("TEST START: POTransSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4(),
                    SampleDLGs.DLG1(), };
        PartialOrder po = SampleDLGs.getPartialOrder();
        boolean expectedResults[][] = { {true,  true,  true,  false, false},
                                        {false, true,  true,  false, false},
                                        {false, false, true,  false, false},
                                        {false, false, false, true , true },
                                        {false, false, false, true , true} };
        boolean expectedResultsOI[][] = { {true,  true,  true,  false, false},
                                          {false, true,  false, false, false},
                                          {false, false, true,  false, false},
                                          {false, false, false, true , true },
                                          {false, false, false, false , true} };
        Subsumption s = new POTransSubsumption(false, po);
        Subsumption s_OI = new POTransSubsumption(true, po);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("POTransSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("Mapping is: " + Arrays.toString(s.subsumes(g[i], g[j])));
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("POTransSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("Mapping is: " + Arrays.toString(s.subsumes(g[i], g[j])));
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }  
    
    
    public static int testTreePOSubsumption() throws Exception {
        int errors = 0;

        System.out.println("TEST START: TreePOSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4() };
        PartialOrder po = SampleDLGs.getPartialOrder();
        boolean expectedResults[][] = { {true,  true,  false, false},
                                        {false, true,  false, false},
                                        {false, false, true,  false},
                                        {false, false, false, true } };
        boolean expectedResultsOI[][] = { {true,  true,  false, false},
                                          {false, true,  false, false},
                                          {false, false, true,  false},
                                          {false, false, false, true } };
        Subsumption s = new TreePOSubsumption(false, po);
        Subsumption s_OI = new TreePOSubsumption(true, po);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("TreePOSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("TreePOSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }    

    
    public static int testTreePOTransSubsumption() throws Exception {
        int errors = 0;
        System.out.println("TEST START: TreePOTransSubsumption");
        
        DLG g[] = { SampleDLGs.TreeDLG1(), 
                    SampleDLGs.TreeDLG2(),
                    SampleDLGs.TreeDLG3(),
                    SampleDLGs.TreeDLG4() };
        PartialOrder po = SampleDLGs.getPartialOrder();
        boolean expectedResults[][] = { {true,  true,  true,  false},
                                        {false, true,  true,  false},
                                        {false, false, true,  false},
                                        {false, false, false, true } };
        boolean expectedResultsOI[][] = { {true,  true,  true,  false},
                                          {false, true,  false, false},
                                          {false, false, true,  false},
                                          {false, false, false, true } };
        Subsumption s = new TreePOTransSubsumption(false, po);
        Subsumption s_OI = new TreePOTransSubsumption(true, po);
        
        for(int i = 0;i<g.length;i++) {
            for(int j = 0;j<g.length;j++) {
                if ((s.subsumes(g[i], g[j])!=null) != expectedResults[i][j]) {
                    errors++;
                    System.out.println("TreePOTransSubsumption(false) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
                if ((s_OI.subsumes(g[i], g[j])!=null) != expectedResultsOI[i][j]) {
                    errors++;
                    System.out.println("TreePOTransSubsumption(true) mismatch ("+i+"-"+j+"), graphs are:");
                    System.out.println("graph 1:\n" + g[i]);
                    System.out.println("graph 2:\n" + g[j]);
                }
            }
        }
        
        System.out.println("TEST END: " + errors + " errors");

        return errors;
    }    
    
    
    public static int testSubsumptionSubfunctions() throws Exception {
        int errors = 0;
        System.out.println("TEST START: testSubsumptionSubfunctions");

        // path-finding for trans-subsumption:
        DLG g = SampleDLGs.DLG2();
        FlatTransSubsumption s = new FlatTransSubsumption(true);
        if (!s.pathExistsThroughLabel(g, 0, 5, new Label("f"))) {
            errors++;
            System.out.print("pathExistsThroughLabel error");
        }
        boolean used[] = new boolean[g.getNVertices()];
        List<List<Integer>> paths = s.allPathsThroughLabel(g, 0, 5, new Label("f"), used);
        if (paths.size()!=4) {
            errors++;
            System.out.println("allPathsThroughLabel error");
        }
        
        System.out.println("TEST END: " + errors + " errors");
        return errors;
    }    
    
}

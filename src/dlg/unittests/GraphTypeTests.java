/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.operations.GraphTypeChecker;
import java.util.List;

/**
 *
 * @author santi
 */
public class GraphTypeTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Graph Type");

        errors += treeTests();
        errors += DAGTests();
        errors += upperSemiLatticeTests();
        errors += lowerSemiLatticeTests();
        errors += latticeTests();
        
        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
    
    public static int treeTests() throws Exception {
        int errors = 0;
        
        List<DLG> trees = SampleDLGs.allTreeDLGs();
        List<DLG> nontrees = SampleDLGs.allDLGs();
        SampleDLGs.removeEquivalents(nontrees, trees);
        
        for(DLG g:trees) {
            if (!GraphTypeChecker.isTree(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is not a tree");
                System.out.println(g);
            }
        }
        for(DLG g:nontrees) {
            if (GraphTypeChecker.isTree(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is a tree");
                System.out.println(g);
            }
        }
        
        return errors;
    }
    
    
    public static int DAGTests() throws Exception {
        int errors = 0;
        
        List<DLG> DAGs = SampleDLGs.allDAGDLGs();
        List<DLG> nonDAGs = SampleDLGs.allDLGs();
        SampleDLGs.removeEquivalents(nonDAGs, DAGs);
        
        for(DLG g:DAGs) {
            if (!GraphTypeChecker.isDAG(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is not a DAG");
                System.out.println(g);
            }
        }
        for(DLG g:nonDAGs) {
            if (GraphTypeChecker.isDAG(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is a DAG");
                System.out.println(g);
            }
        }
        
        return errors;
    }
    
    
    public static int upperSemiLatticeTests() throws Exception {
        int errors = 0;
        
        List<DLG> SLs = SampleDLGs.allUpperSemiLatticeDLGs();
        List<DLG> nonSLs = SampleDLGs.allDLGs();
        SampleDLGs.removeEquivalents(nonSLs, SLs);
        
        for(DLG g:SLs) {
            if (!GraphTypeChecker.isUpperSemiLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is not an upper semi-lattice");
                System.out.println(g);
            }
        }
        for(DLG g:nonSLs) {
            if (GraphTypeChecker.isUpperSemiLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is an upper semi-lattice");
                System.out.println(g);
            }
        }
        
        return errors;
    }
    
    
    public static int lowerSemiLatticeTests() throws Exception {
        int errors = 0;
        
        List<DLG> SLs = SampleDLGs.allLowerSemiLatticeDLGs();
        List<DLG> nonSLs = SampleDLGs.allDLGs();
        SampleDLGs.removeEquivalents(nonSLs, SLs);
        
        for(DLG g:SLs) {
            if (!GraphTypeChecker.isLowerSemiLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is not a lower semi-lattice");
                System.out.println(g);
            }
        }
        for(DLG g:nonSLs) {
            if (GraphTypeChecker.isLowerSemiLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is a lower semi-lattice");
                System.out.println(g);
            }
        }
        
        return errors;
    }  
    
    
    public static int latticeTests() throws Exception {
        int errors = 0;
        
        List<DLG> lattices = SampleDLGs.allLatticeDLGs();
        List<DLG> nonLattices = SampleDLGs.allDLGs();
        nonLattices.removeAll(lattices);
        SampleDLGs.removeEquivalents(nonLattices, lattices);
        
        for(DLG g:lattices) {
            if (!GraphTypeChecker.isLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is not a lattice");
                System.out.println(g);
            }
        }
        for(DLG g:nonLattices) {
            if (GraphTypeChecker.isLattice(g)) {
                errors++;
                System.out.println("GraphTypeChecker: believes this is a lattice");
                System.out.println(g);
            }
        }
        
        return errors;
    }      

}

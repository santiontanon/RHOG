/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.util.Label;
import dlg.util.Pair;

/**
 *
 * @author santi
 */
public class PartialOrderTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: PartialOrder");

        PartialOrder po = SampleDLGs.getPartialOrder();

        Pair testPositivePairs[] = {new Pair<>(new Label("top"), new Label("top")),
                                    new Pair<>(new Label("A"), new Label("A")),
                                    new Pair<>(new Label("top"), new Label("A")),
                                    new Pair<>(new Label("A"), new Label("AB-child")),
                                    new Pair<>(new Label("B"), new Label("AB-child")),
                                    };
        Pair testNegativeePairs[] = {new Pair<>(new Label("A"), new Label("top")),
                                     new Pair<>(new Label("AB-child"), new Label("A")),
                                     new Pair<>(new Label("B"), new Label("A-child")),
                                     new Pair<>(new Label("f-child1"), new Label("f")),
                                     new Pair<>(new Label("g"), new Label("f-child2")),
                                     };

        for(Pair<Label, Label> pair:testPositivePairs) {
            if (!po.subsumes(pair.m_a, pair.m_b)) {
                errors++;
                System.out.println("Label '" + pair.m_a+ "' does not subsume label '"+pair.m_b+"'");
            }
        }
        for(Pair<Label, Label> pair:testNegativeePairs) {
            if (po.subsumes(pair.m_a, pair.m_b)) {
                errors++;
                System.out.println("Label '" + pair.m_a+ "' subsumes label '"+pair.m_b+"'");
            }
        }
        
        DLG g = po.translateToDLG();
        
        PartialOrder po2 = new PartialOrder(g, true);
        
        boolean [][]cache1 = po.getSubsumptionCache();
        boolean [][]cache2 = po2.getSubsumptionCache();
        
        for(int i = 0;i<cache1.length;i++) {
            for(int j = 0;j<cache1[0].length;j++) {
                if (cache1[i][j]!=cache2[i][j]) {
                    errors++;
                    System.out.println("Subsumption disagreement when exporting/importing to DLG");
                }
            }
        }
        
        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
}

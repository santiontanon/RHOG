/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.operations.Connectivity;
import dlg.util.Pair;
import java.util.List;

/**
 *
 * @author santi
 */
public class ConnectivityTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Connectivity");

        List<DLG> gs = SampleDLGs.allDLGs();
        for(DLG g:gs) {
            if (!Connectivity.isConnected(g)) {
                errors++;
                System.out.println("Connectivity test failed");
                System.out.println("DLG:\n" + g);
            }
            List<Pair<Integer, Integer>> bridges = Connectivity.getBridges(g);
            for(int i = 0;i<g.getNVertices();i++) {
                for(int j = 0;j<g.getNVertices();j++) {
                    if (g.getEdge(i, j)!=null) {
                        DLG g2 = new DLG(g);
                        g2.setEdge(i, j, null);
                        boolean isBridge = false;
                        for(Pair<Integer, Integer> b:bridges) {
                            if (b.m_a == i && b.m_b == j) {
                                isBridge = true;
                                break;
                            }
                        }
                        if (Connectivity.isConnected(g2) == isBridge) {
                            errors++;
                            System.out.println("Removing a bridge did not agree with connectivity prediction");
                            System.out.println("edge:" + i + " -> " + j);
                            System.out.println("was considered bridge: " + isBridge);
                            System.out.println("DLG:\n" + g);
                        }                    
                    }
                }
            }
        }                

        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }    
}

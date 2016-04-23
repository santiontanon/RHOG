/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

/**
 *
 * @author santi
 */
public class AllTests {
    public static void main(String args[]) throws Exception {
        int errors = 0;
        errors += PartialOrderTests.runTests();
        errors += SubsumptionTests.runTests();
        errors += RefinementTests.runTests();
        errors += AntiunificationTests.runTests();
        errors += DisintegrationTests.runTests();
        errors += ConnectivityTests.runTests();
        errors += GraphTypeTests.runTests();
        errors += BridgeTests.runTests();
        System.out.println("TOTAL ERRORS: " + errors);
    }
}

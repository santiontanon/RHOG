/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.bridges.GMLBridge;
import dlg.bridges.GraphMLBridge;
import dlg.bridges.TGFBridge;
import dlg.core.subsumption.FlatSubsumption;
import dlg.core.subsumption.Subsumption;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 *
 * @author santi
 */
public class BridgeTests {
    public static void main(String args[]) throws Exception {
        runTests();
    }
    
    public static int runTests() throws Exception {
        int errors = 0;
        
        System.out.println("TESTFILE START: Bridges");

        List<DLG> l = SampleDLGs.allDLGs();
        
        // TGF test:
        TGFBridge b1 = new TGFBridge();
        for(DLG g:l) {
            StringWriter sw = new StringWriter();
            b1.save(g, sw);
            String s = sw.toString();
            DLG g2 = b1.load(new BufferedReader(new StringReader(s)));
            if (!equivalents(g, g2)) {
                errors++;
                System.out.println("Saving and loading from TGF did not result in the same term");
                System.out.println("Original " + g.getClass().getSimpleName() + ":\n" + g);
            }   
        }

        // GML test:
        GMLBridge b2 = new GMLBridge();
        for(DLG g:l) {
            StringWriter sw = new StringWriter();
            b2.save(g, sw);
            String s = sw.toString();
            DLG g2 = b2.load(new BufferedReader(new StringReader(s)));
            if (g2.getClass()!=g.getClass() ||
                !equivalents(g, g2)) {
                errors++;
                System.out.println("Saving and loading from GML did not result in the same term");
                System.out.println("Original " + g.getClass().getSimpleName() + ":\n" + g);
            }   
        }

        
        // GraphML test:
        GraphMLBridge b3 = new GraphMLBridge();
        for(DLG g:l) {
            StringWriter sw = new StringWriter();
            b3.save(g, sw);
            String s = sw.toString();
            DLG g2 = b3.load(new BufferedReader(new StringReader(s)));
            if (g2.getClass()!=g.getClass() ||
                !equivalents(g, g2)) {
                errors++;
                System.out.println("Saving and loading from GML did not result in the same term");
                System.out.println("Original " + g.getClass().getSimpleName() + ":\n" + g);
            }   
        }


        System.out.println("TESTFILE END: " + errors + " errors");
        return errors;
    }
    
    
    public static boolean equivalents(DLG g1, DLG g2) throws Exception {
        Subsumption s = new FlatSubsumption(true);
        return s.subsumes(g1,g2)!=null && s.subsumes(g2, g1)!=null;
    }
    
}

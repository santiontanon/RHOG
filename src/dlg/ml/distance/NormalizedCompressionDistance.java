/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.bridges.DLGWriter;
import dlg.core.DLG;
import dlg.ml.distance.Distance;
import dlg.util.Label;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author santi
 */
public class NormalizedCompressionDistance extends Distance {

    DLGWriter writer = null;
    
    public NormalizedCompressionDistance(DLGWriter a_writer) {
        writer = a_writer;
    }
    
    
    public void train(List<DLG> instances, List<Label> labels) throws Exception {
    }

    public double distance(DLG g1, DLG g2) throws Exception {
        StringWriter w1 = new StringWriter();
        StringWriter w2 = new StringWriter();
        
        writer.save(g1, w1);
        writer.save(g2, w2);
        
        w1.flush();
        w2.flush();
        
        String s1 = w1.toString();
        String s2 = w2.toString();
    
        int l1 = compressionLengthGZIP(s1);
        int l2 = compressionLengthGZIP(s2);
        int l3 = compressionLengthGZIP(s1+s2);
        
        float NCS = (l3 - Math.min(l1, l2)) / (float) Math.max(l1, l2);
        // should be normalized, but because of headers in the compression files, there might be small anomalies:
        if (NCS < 0) {
            NCS = 0;
        }
        if (NCS > 1) {
            NCS = 1;
        }
        
        return NCS;
    }
    
    
    public int compressionLengthGZIP(String str) {
        if (str == null || str.length() == 0) return str.length();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
//            System.out.println("String: '" + str + "'");
//            System.out.println("Compression " + str.length() + " -> " + out.size());
            return out.size();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // I know this is bad, but if it crashes, I just want to see the stack trace and quit, so I can debug
        }

        // should never get here:
        return 0;
    }    

}

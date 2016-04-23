/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import dlg.util.Label;

/**
 *
 * @author santi
 */
public class TGFBridge {
    
    public DLG load(BufferedReader br) throws Exception {
        List<String> IDs = new ArrayList<>();
        List<Label> labels = new ArrayList<>();
        
        do{
            String line = br.readLine().trim();
            if (line == null) throw new Exception("File ended before '#' delimiter!");
            if (line.equals("#")) break;
            int idx1 = line.indexOf(" ");
            IDs.add(line.substring(0, idx1));
            labels.add(new Label(line.substring(idx1).trim()));
        }while(true);
        
        int n = IDs.size();
        DLG g = new DLG(n);
        for(int i = 0;i<n;i++) g.setVertex(i,labels.get(i));
        
        do{
            String line = br.readLine();
            if (line == null) break;
            line = line.trim();
            int idx1 = line.indexOf(" ");
            String ID1 = line.substring(0, idx1);
            line = line.substring(idx1).trim();
            int idx2 = line.indexOf(" ");
            String ID2 = line.substring(0, idx2);
            String label = line.substring(idx2).trim();
            
            int v1 = IDs.indexOf(ID1);
            if (v1==-1) throw new Exception("Unrecognized ID in vefrtex '" + ID1 + "'");
            int v2 = IDs.indexOf(ID2);
            if (v2==-1) throw new Exception("Unrecognized ID in vefrtex '" + ID2 + "'");
            g.setEdge(v1, v2, new Label(label));
        }while(true);
        
        return g;
    }
    
    
    public void save(DLG g, Writer w) throws Exception {
        for(int i = 0;i<g.getNVertices();i++) {
            w.write(i + " " + g.getVertex(i) + "\n");
        }
        w.write("#\n");
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                if (g.getEdge(i, j)!=null) {
                    w.write(i + " " + j + " " + g.getEdge(i, j) + "\n");
                }
            }
        }
    }
}

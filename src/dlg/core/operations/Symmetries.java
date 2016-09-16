/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class Symmetries {
    public static List<List<Integer>> symmetricVertexes(DLG g) {
        List<List<Integer>> symmetryGroups = new ArrayList<>();
        List<Integer> toProcess = new ArrayList<>();
        for(int i = 0;i<g.getNVertices();i++) toProcess.add(i);
        
        while(!toProcess.isEmpty()) {
            List<Integer> symmetryGroup = new ArrayList<>();
            int current = toProcess.remove(0);
            symmetryGroup.add(current);
            for(int other:toProcess) {
                if (symmetric(g, current, other)) {
                    symmetryGroup.add(other);
                }
            }
            toProcess.removeAll(symmetryGroup);
            if (symmetryGroup.size()>1) symmetryGroups.add(symmetryGroup);
        }
        
        return symmetryGroups;
    }
    
    
    public static boolean symmetric(DLG g, int v1, int v2) {
        if (!g.getVertex(v1).equals(g.getVertex(v2))) return false;
        
        int [][]cie = g.getCondensedIncomingEdges();
        int [][]coe = g.getCondensedOutgoingEdges();
        if (cie[v1].length != cie[v2].length) return false;
        if (coe[v1].length != coe[v2].length) return false;
        
        for(int i = 0;i<cie[v1].length;i++) {
            if (cie[v1][i] != cie[v2][i]) return false;
        }
        for(int i = 0;i<coe[v1].length;i++) {
            if (coe[v1][i] != coe[v2][i]) return false;
        }
        
        return true;
    }
}

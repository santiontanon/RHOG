/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.subsumption;

import dlg.core.DLG;

/**
 *
 * @author santi
 */
public abstract class Subsumption {
    public abstract int []subsumes(DLG g1, DLG g2, int mapping[]);


    public int []subsumes(DLG g1, DLG g2) {
        return subsumes(g1, g2, null);
    }
    
}

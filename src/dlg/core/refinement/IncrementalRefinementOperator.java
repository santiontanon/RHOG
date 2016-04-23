/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement;

import dlg.core.DLG;

/**
 *
 * @author santi
 */
public abstract interface IncrementalRefinementOperator {
    public abstract void setDLGForDownwardRefinement(DLG g) throws Exception;
    public abstract DLG getNextDownwardRefinement() throws Exception;    

    public abstract void setDLGForUpwardRefinement(DLG g) throws Exception;
    public abstract DLG getNextUpwardRefinement() throws Exception;    

}

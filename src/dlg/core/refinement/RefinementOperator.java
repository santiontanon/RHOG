/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.refinement;

import dlg.core.DLG;
import java.util.List;

/**
 *
 * @author santi
 */
public abstract class RefinementOperator {
    
    public abstract DLG getTop();
    public abstract List<? extends DLG> downwardRefinements(DLG g) throws Exception;
    public abstract List<? extends DLG> upwardRefinements(DLG g) throws Exception;
}

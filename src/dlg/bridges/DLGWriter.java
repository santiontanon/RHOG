/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import java.io.Writer;

/**
 *
 * @author santi
 */
public interface DLGWriter {
    public void save(DLG g, Writer w) throws Exception;
}

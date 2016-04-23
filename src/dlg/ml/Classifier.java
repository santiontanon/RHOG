/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml;

import dlg.core.DLG;
import dlg.util.Label;
import java.util.List;

/**
 *
 * @author santi
 */
public abstract class Classifier {
    public abstract void train(List<DLG> instances, List<Label> labels) throws Exception;
    public abstract Label predict(DLG test_instance) throws Exception;
}

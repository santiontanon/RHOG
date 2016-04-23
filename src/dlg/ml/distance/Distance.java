/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml.distance;

import dlg.core.DLG;
import dlg.util.Label;
import java.util.List;

/**
 *
 * @author santi
 */
public abstract class Distance {
    public abstract void train(List<DLG> instances, List<Label> labels) throws Exception;
    public abstract double distance(DLG g1, DLG g2) throws Exception;
}

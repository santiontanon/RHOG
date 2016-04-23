/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.unittests;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.TreeDLG;
import dlg.core.subsumption.FlatSubsumption;
import dlg.core.subsumption.Subsumption;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class SampleDLGs {
    /*
    This class just contains a few routines to create small DLGs that will be
    used in all the unit tests. These DLGs were selected, since were the ones that
    were helpful during debugging in uncovering bugs.
    */
    
    public static List<DLG> allTreeDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG1());
        l.add(TreeDLG2());
        l.add(TreeDLG3());
        l.add(TreeDLG4());
        return l;
    }
    
    public static List<DLG> allDAGDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG1());
        l.add(TreeDLG2());
        l.add(TreeDLG3());
        l.add(TreeDLG4());
        l.add(DLG1());
        l.add(DLG4());
        return l;
    }
    
    public static List<DLG> allUpperSemiLatticeDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG1());
        l.add(TreeDLG2());
        l.add(TreeDLG3());
        l.add(TreeDLG4());
        l.add(DLG1());
        l.add(DLG2());
        l.add(DLG3());
        return l;
    }

    public static List<DLG> allLowerSemiLatticeDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG4());
        l.add(DLG1());
        l.add(DLG2());
        l.add(DLG3());
        return l;
    }

    public static List<DLG> allLatticeDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG4());
        l.add(DLG1());
        l.add(DLG2());
        l.add(DLG3());
        return l;
    }
    
    public static List<DLG> allDLGs() throws Exception {
        List<DLG> l = new ArrayList<>();
        l.add(TreeDLG1());
        l.add(TreeDLG2());
        l.add(TreeDLG3());
        l.add(TreeDLG4());
        l.add(DLG1());
        l.add(DLG2());
        l.add(DLG3());
        l.add(DLG4());
        return l;
    }
    
    
    public static List<DLG> removeEquivalents(List<DLG> all, List<DLG> remove) 
    {
        List<DLG> toDelete = new ArrayList<>();
        Subsumption s = new FlatSubsumption(true);
        for(DLG g:all) {
            for(DLG g2:remove) {
                if (s.subsumes(g, g2)!=null && s.subsumes(g2, g)!=null) toDelete.add(g);
            }
        }
        all.removeAll(toDelete);
        return all;
    }
    
    
    
    public static PartialOrder getPartialOrder() throws Exception {
        PartialOrder po = new PartialOrder();
        
        po.addLabel(new Label("A-parent"));
        po.addLabel(new Label("A"));
        po.addLabel(new Label("A-child"));
        po.addLabel(new Label("B"));
        po.addLabel(new Label("AB-child"));
        
        po.addSubsumptionConstraint(new Label("A-parent"), new Label("A"));
        po.addSubsumptionConstraint(new Label("A"), new Label("A-child"));
        po.addSubsumptionConstraint(new Label("A"), new Label("AB-child"));
        po.addSubsumptionConstraint(new Label("B"), new Label("AB-child"));
        
        po.addLabel(new Label("f-parent"));
        po.addLabel(new Label("f"));
        po.addLabel(new Label("f-child1"));
        po.addLabel(new Label("f-child2"));
        po.addLabel(new Label("g"));

        po.addSubsumptionConstraint(new Label("f-parent"), new Label("f"));
        po.addSubsumptionConstraint(new Label("f"), new Label("f-child1"));
        po.addSubsumptionConstraint(new Label("f"), new Label("f-child2"));
        
        return po;
    }
    
    
    public static TreeDLG TreeDLG1() throws Exception {
        TreeDLG t = new TreeDLG(3);
        t.setRoot(0);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("A"));
        t.setVertex(2, new Label("B"));
        t.setEdge(0,1, new Label("g"));
        t.setEdge(0,2, new Label("f"));        
        return t;
    }
    
    public static TreeDLG TreeDLG2() throws Exception {
        TreeDLG t = new TreeDLG(4);
        t.setRoot(0);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("A"));
        t.setVertex(2, new Label("B"));
        t.setVertex(3, new Label("B"));
        t.setEdge(0,1, new Label("g"));
        t.setEdge(0,2, new Label("f"));
        t.setEdge(0,3, new Label("g"));
        return t;
    }
    
    public static TreeDLG TreeDLG3() throws Exception {
        TreeDLG t = new TreeDLG(4);
        t.setRoot(0);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("B"));
        t.setVertex(2, new Label("B"));
        t.setVertex(3, new Label("A"));
        t.setEdge(0,1, new Label("g"));
        t.setEdge(0,2, new Label("f"));
        t.setEdge(1,3, new Label("g"));
        return t;
    }

    public static TreeDLG TreeDLG4() throws Exception {
        TreeDLG t = new TreeDLG(4);
        t.setRoot(0);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("A"));
        t.setVertex(2, new Label("A"));
        t.setVertex(3, new Label("A"));
        t.setEdge(0,1, new Label("f"));
        t.setEdge(1,2, new Label("f"));
        t.setEdge(2,3, new Label("f"));
        return t;
    }
   
    public static DLG DLG1() throws Exception {
        DLG t = new DLG(4);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("A"));
        t.setVertex(2, new Label("A"));
        t.setVertex(3, new Label("A"));
        t.setEdge(0,1, new Label("f"));
        t.setEdge(0,2, new Label("f"));
        t.setEdge(1,2, new Label("f"));
        t.setEdge(2,3, new Label("f"));
        return t;
    }

    public static DLG DLG2() throws Exception {
        DLG t = new DLG(8);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("B"));
        t.setVertex(2, new Label("A"));
        t.setVertex(3, new Label("B"));
        t.setVertex(4, new Label("B"));
        t.setVertex(5, new Label("A"));
        t.setVertex(6, new Label("B"));
        t.setVertex(7, new Label("B"));
        t.setEdge(0,1, new Label("f"));
        t.setEdge(0,3, new Label("f"));
        t.setEdge(0,5, new Label("f"));
        t.setEdge(0,7, new Label("f"));
        t.setEdge(1,1, new Label("f"));
        t.setEdge(1,4, new Label("f"));
        t.setEdge(1,5, new Label("f"));
        t.setEdge(2,0, new Label("f"));
        t.setEdge(2,5, new Label("f"));
        t.setEdge(3,6, new Label("g"));
        t.setEdge(4,5, new Label("f"));
        t.setEdge(6,5, new Label("f"));
        t.setEdge(7,5, new Label("f"));
        return t;
    }

    public static DLG DLG3() throws Exception {
        DLG t = new DLG(1);
        t.setVertex(0, new Label("A"));
        t.setEdge(0,0, new Label("f"));
        return t;
    }

    public static DLG DLG4() throws Exception {
        DLG t = new DLG(5);
        t.setVertex(0, new Label("A"));
        t.setVertex(1, new Label("A"));
        t.setVertex(2, new Label("B"));
        t.setVertex(3, new Label("A"));
        t.setVertex(4, new Label("B"));
        t.setEdge(0,1, new Label("f"));
        t.setEdge(0,2, new Label("f"));
        t.setEdge(1,3, new Label("f"));
        t.setEdge(1,4, new Label("g"));
        t.setEdge(2,3, new Label("g"));
        t.setEdge(2,4, new Label("f"));
        return t;
    }

    
    
}

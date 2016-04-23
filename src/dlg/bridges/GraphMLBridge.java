/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import dlg.util.Label;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author santi
 */
public class GraphMLBridge {
    public List<DLG> loadWithHeader(BufferedReader br) throws Exception {
        List<DLG> l = new ArrayList<>();
        Element root = new SAXBuilder().build(br).getRootElement();
        for(Object o:root.getChildren("graph", root.getNamespace())) {
            Element gxml = (Element)o;
            DLG g = load(gxml);
            l.add(g);
        }
        return l;
    }


    public DLG loadSingleGraphWithHeader(BufferedReader br) throws Exception {
        Element root = new SAXBuilder().build(br).getRootElement();
        return load(root.getChild("graph", root.getNamespace()));
    }


    public DLG load(BufferedReader br) throws Exception {
        Element root = new SAXBuilder().build(br).getRootElement();
        return load(root);
    }
    
    
    public DLG load(Element xml) throws Exception {
        DLG g = null;
        boolean isTree = false;
        int n;
        List<String> vertexIds = new ArrayList<>();
        
        if (xml.getAttributeValue("istree")!=null && 
            xml.getAttributeValue("istree").equals("true")) isTree = true;
        n = xml.getChildren("node", xml.getNamespace()).size();
        
        if (isTree) {
            g = new TreeDLG(n);
        } else {
            g = new DLG(n);
        }
        
        int i = 0;
        for(Object o:xml.getChildren("node", xml.getNamespace())) {
            Element vxml = (Element)o;
            vertexIds.add(vxml.getAttributeValue("id"));
            g.setVertex(i, new Label(vxml.getAttributeValue("label")));
            if (vxml.getAttributeValue("root")!=null && 
                vxml.getAttributeValue("root").equals("true")) {
                ((TreeDLG)g).setRoot(i);
            }
            i++;
        }
        
        for(Object o:xml.getChildren("edge", xml.getNamespace())) {
            Element exml = (Element)o;
            int idx1 = vertexIds.indexOf(exml.getAttributeValue("source"));
            int idx2 = vertexIds.indexOf(exml.getAttributeValue("target"));
            g.setEdge(idx1, idx2, new Label(exml.getAttributeValue("label")));
        }        
        
        return g;
    }

    
    public void saveHeader(Writer w) throws Exception {
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n");
        w.write("   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        w.write("   xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n");
    }
    
    
    public void saveFooter(Writer w) throws Exception {
        w.write("</graphml>\n");
    }


    public void saveWithHeader(DLG g, Writer w, String label) throws Exception {
        saveHeader(w);
        save(g,w,label);
        saveFooter(w);
    }
    
    
    public void save(DLG g, Writer w) throws Exception {
        save(g, w, null);
    }
    
    
    public void save(DLG g, Writer w, String label) throws Exception {
        String isTree = "";
        if (g instanceof TreeDLG) {
            isTree = " istree=\"true\"";
        }
        if (label==null) {
            w.write("<graph edgedefault=\"directed\""+isTree+">\n");
        } else {
            w.write("<graph id=\""+label+"\" edgedefault=\"directed\""+isTree+">\n");
        }
        for(int i = 0;i<g.getNVertices();i++) {
            if ((g instanceof TreeDLG) && ((TreeDLG)g).getRoot()==i) {
                w.write("  <node id=\""+i+"\" label=\""+g.getVertex(i)+"\" root=\"true\"/>\n");
            } else {
                w.write("  <node id=\""+i+"\" label=\""+g.getVertex(i)+"\"/>\n");
            }
        }
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                if (g.getEdge(i, j)!=null) {
                    w.write("  <edge source=\""+i+"\" target=\""+j+"\" label=\""+g.getEdge(i,j)+"\"/>\n");
                }
            }
        }
        w.write("</graph>\n");
    }    
}

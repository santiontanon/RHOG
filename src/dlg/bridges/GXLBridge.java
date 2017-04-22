/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.bridges;

import dlg.core.DLG;
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
public class GXLBridge implements DLGWriter {

    
    public DLG load(BufferedReader br) throws Exception {
        Element root = new SAXBuilder().build(br).getRootElement();
        return load(root);
    }
    
    
    public DLG load(Element xml) throws Exception {
        DLG g = null;
        List<Integer> vertexIDs = new ArrayList<Integer>();
        boolean directed = true;
        
        Element graph_e = xml.getChild("graph");
        String directedness = graph_e.getAttributeValue("edgemode");
        if (directedness.equals("undirected")) directed = false;
        
        // vertices:
        List nodes_e = graph_e.getChildren("node");
        int n = nodes_e.size();
        g = new DLG(n);
        for(int i = 0;i<n;i++) {
            Element node_e = (Element)nodes_e.get(i);
            vertexIDs.add(Integer.parseInt(node_e.getAttributeValue("id")));
            Element attr_e = node_e.getChild("attr");
            if (attr_e!=null) {
                Element value_e = null;
                value_e = attr_e.getChild("string");
                if (value_e == null) value_e = attr_e.getChild("int");
                if (value_e != null) {
                    String label_text = value_e.getValue().trim();
                    g.setVertex(i, new Label(label_text));
                }
            }
        }
        
        // edges:
        List edges_e = graph_e.getChildren("edge");
        for(Object o:edges_e) {
            Element edge_e = (Element)o;
            int from = Integer.parseInt(edge_e.getAttributeValue("from"));
            int to = Integer.parseInt(edge_e.getAttributeValue("to"));
            int v1 = vertexIDs.indexOf(from);
            int v2 = vertexIDs.indexOf(to);
            Element attr_e = edge_e.getChild("attr");
            if (attr_e!=null) {
                Element value_e = null;
                value_e = attr_e.getChild("string");
                if (value_e == null) value_e = attr_e.getChild("int");
                if (value_e != null) {
                    String label_text = value_e.getValue().trim();
                    g.setEdge(v1, v2, new Label(label_text));
                    if (!directed) g.setEdge(v2, v1, new Label(label_text));
                }
            }
        }
        
        return g;
    }

    
    public void save(DLG g, Writer w) throws Exception {
        w.write("<?xml version=\"1.0\"?>\n");
        w.write("<gxl>\n");
        w.write("<graph edgeids=\"true\" edgemode=\"directed\">\n");
        for(int i = 0;i<g.getNVertices();i++) {
            w.write("<node id=\""+(i+1)+"\"><attr name=\"label\"><string>"+g.getVertex(i)+"</string</attr></node>\n");
        }
        for(int i = 0;i<g.getNVertices();i++) {
            for(int j = 0;j<g.getNVertices();j++) {
                Label l = g.getEdge(i, j);
                if (l!=null) {
                    w.write("<edge from=\""+(i+1)+"\" to=\""+(j+1)+"\"><attr name=\"label\"><string>"+l+"</string</attr></edge>\n");
                }
            }
        }        
        w.write("</graph>\n");
        w.write("</gxl>\n");
    }
    
}

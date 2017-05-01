/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml;

import dlg.bridges.GMLBridge;
import dlg.bridges.GXLBridge;
import dlg.bridges.GraphMLBridge;
import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.util.Label;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author santi
 * 
 * This class contains useful functions to load and use machine learning datasets with DLG
 * 
 */
public class MLDataSet {
    public static int DEBUG = 0;
    
    public static String MLDataSet_format_GML = "gml";
    public static String MLDataSet_format_GraphML = "graphml";
    
    public String m_name = null;
    public List<DLG> m_instances = null;
    public List<Label> m_labels[] = null;
    public PartialOrder m_partialOrder = null;
    public List<Label> m_vertex_labels = null;
    public List<Label> m_edge_labels = null;
    
    
    MLDataSet(String name) {
        m_name = name;
    }
   
    
    public static MLDataSet loadDataSet(String name, String instancesFile, String labelFiles[], String format) throws Exception {
        return loadDataSet(name, instancesFile, labelFiles, null, format);
    }
    
    public static MLDataSet loadDataSet(String name, String instancesFile, String labelFiles[], String partialOrderFile, String format) throws Exception {
        MLDataSet ds = new MLDataSet(name);
        
        if (format.equals(MLDataSet_format_GML)) {
            GMLBridge b = new GMLBridge();
            // load instances:
            {
                BufferedReader br = new BufferedReader(new FileReader(instancesFile));
                List<DLG> l = new ArrayList<>();
                while(true) {
                    DLG g = b.load(br);
                    if (g==null) break;
                    l.add(g);
                }
                ds.m_instances = l;
            }
                       
            // load partial order:
            if (partialOrderFile!=null) {
                BufferedReader br = new BufferedReader(new FileReader(partialOrderFile));
                DLG po = b.load(br);
                ds.m_partialOrder = new PartialOrder(po, false);
            }
            
        } else if (format.equals(MLDataSet_format_GraphML)) {
            GraphMLBridge b = new GraphMLBridge();
            // load instances:
            {
                BufferedReader br = new BufferedReader(new FileReader(instancesFile));
                ds.m_instances = b.loadWithHeader(br);
            }
                       
            // load partial order:
            if (partialOrderFile!=null) {
                BufferedReader br = new BufferedReader(new FileReader(partialOrderFile));
                DLG po = b.loadSingleGraphWithHeader(br);
                ds.m_partialOrder = new PartialOrder(po, false);
            }
        } else {
            throw new Exception("Unsupported format '" + format+ "'");
        }
        
        // load labels:
        ds.m_labels = new List[labelFiles.length];
        for(int i = 0;i<labelFiles.length;i++) {
            ds.m_labels[i] = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(labelFiles[i]));
            while(true) {
                String line = br.readLine();
                if (line==null) break;
                StringTokenizer st = new StringTokenizer(line,"\t");
                st.nextToken(); // graph name (can be ignored, since it's in the same order as the instances)
                ds.m_labels[i].add(new Label(st.nextToken()));
            }
        }
        
        // extract all the labels from the instances:
        {
            ds.m_vertex_labels = new ArrayList<>();
            for(DLG g:ds.m_instances) {
                for(Label l:g.getAllVertexLabels()) 
                    if (!ds.m_vertex_labels.contains(l)) ds.m_vertex_labels.add(l);
            }
            ds.m_edge_labels = new ArrayList<>();    
            for(DLG g:ds.m_instances) {
                for(Label l:g.getAllEdgeLabels()) 
                    if (!ds.m_edge_labels.contains(l)) ds.m_edge_labels.add(l);
            }
        }
               
        return ds;
    }
    
    public static MLDataSet loadGXLDataSet(String name, String folder, String cxlFiles[]) throws Exception
    {
        return loadGXLDataSet(name, folder, cxlFiles, -1, GXLBridge.FIRST_ATTRIBUTE_AS_LABEL_AND_REST_AS_EDGES, true, true);
    }
    
    public static MLDataSet loadGXLDataSet(String name, String folder, String cxlFiles[], int maxInstances, int mode, boolean loadInteger, boolean loadFloat) throws Exception
    {
        MLDataSet ds = new MLDataSet(name);
        ds.m_instances = new ArrayList<>();
        ds.m_labels = new List[1];
        ds.m_labels[0] = new ArrayList<>();
        GXLBridge b = new GXLBridge(mode, loadInteger, loadFloat);
        
        for(String cxlFile:cxlFiles) {
            BufferedReader br = new BufferedReader(new FileReader(folder + cxlFile));
            Element root = new SAXBuilder().build(br).getRootElement();
            Element dataset_e = (Element)root.getChildren().get(0);
            List instances_l = dataset_e.getChildren("print");
            for(Object o:instances_l) {
                Element instance_e = (Element)o;
                String gxl_file = instance_e.getAttributeValue("file");
                String instance_label = instance_e.getAttributeValue("class");
                DLG g = b.load(new BufferedReader(new FileReader(folder + gxl_file)));
                ds.m_instances.add(g);
                ds.m_labels[0].add(new Label(instance_label));
                if (maxInstances>0 && ds.m_instances.size()>=maxInstances) break;
                if (DEBUG>=1) System.out.println("loaded instance " + ds.m_instances.size() + " -> " + instance_label + "(vertices: " + g.getNVertices() + ")");
            }
            if (maxInstances>0 && ds.m_instances.size()>=maxInstances) break;
        }
        
        // extract all the labels from the instances:
        {
            ds.m_vertex_labels = new ArrayList<>();
            for(DLG g:ds.m_instances) {
                for(Label l:g.getAllVertexLabels()) 
                    if (!ds.m_vertex_labels.contains(l)) ds.m_vertex_labels.add(l);
            }
            ds.m_edge_labels = new ArrayList<>();    
            for(DLG g:ds.m_instances) {
                for(Label l:g.getAllEdgeLabels()) 
                    if (!ds.m_edge_labels.contains(l)) ds.m_edge_labels.add(l);
            }
        }        
        
        // construct the basic partial order:
        ds.m_partialOrder = new PartialOrder();
        for(Label l:ds.m_vertex_labels) {
            ds.m_partialOrder.addLabel(l);
        }
        for(Label l:ds.m_edge_labels) {
            ds.m_partialOrder.addLabel(l);
        }
        
        return ds;
    }    
    
    
    public MLDataSet filterLabels(Label []labels, int labelIndex) {
        MLDataSet ds = new MLDataSet(m_name + "-filtered-by-"+labelIndex+"-" + labels);

        ds.m_instances = new ArrayList<>();
        ds.m_labels = new ArrayList[1];
        ds.m_labels[0] = new ArrayList<>();
        for(int i = 0;i<m_instances.size();i++) {
            boolean found = false;
            for(Label l:labels) {
                if (m_labels[labelIndex].get(i).equals(l)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                ds.m_instances.add(m_instances.get(i));
                ds.m_labels[0].add(m_labels[labelIndex].get(i));
            }
        }
        ds.m_partialOrder = m_partialOrder;
        ds.m_vertex_labels = m_vertex_labels;
        ds.m_edge_labels = m_edge_labels;

        return ds;
    }
}

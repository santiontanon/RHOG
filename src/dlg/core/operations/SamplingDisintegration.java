/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.refinement.RefinementOperator;
import dlg.core.refinement.base.FlatRefinement;
import dlg.core.refinement.base.FlatTransRefinement;
import dlg.core.refinement.base.PORefinement;
import dlg.core.refinement.base.POTransRefinement;
import dlg.core.subsumption.POSubsumption;
import dlg.core.subsumption.POTransSubsumption;
import dlg.core.subsumption.Subsumption;
import dlg.util.Label;
import dlg.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author santi
 * 
 * This class generates patterns randomly in a constructive way ensuring they always subsume the given DLG
 * It does so by iteratively adding new vertices and edges to a pattern initially constructed by selecting a
 * random vertex from the original DLG.
 * - The input probability "p" is the probability to keep adding new elements (vertices/edges)
 * 
 */
public class SamplingDisintegration {
    public static int DEBUG = 0;
    static Random r = new Random();
    

    public static List<DLG> samplingDisintegration(DLG g, Subsumption s, RefinementOperator rho, int num_properties, double p) throws Exception  {
        if (rho instanceof FlatRefinement ||
            rho instanceof FlatTransRefinement) {
            return samplingDisintegration(g, num_properties, p);
        } else if (rho instanceof PORefinement) {
            return samplingDisintegrationPO(g, ((POSubsumption)s).getPartialOrder(), num_properties, p);
        } else if (rho instanceof POTransRefinement) {
            return samplingDisintegrationPO(g, ((POTransSubsumption)s).getPartialOrder(), num_properties, p);
        } else {
            return null;
        }
    }


    public static List<DLG> samplingDisintegration(DLG g, int num_properties, double p) throws Exception  {
        List<DLG> properties = new ArrayList<>();

        for(int i = 0;i<num_properties;i++) {
            properties.add(sampleOneProperty(g, p));
        }
        
        return properties;
    }    
    
    
    public static List<DLG> samplingDisintegrationPO(DLG g, PartialOrder po, int num_properties, double p) throws Exception  {
        List<DLG> properties = new ArrayList<>();

        for(int i = 0;i<num_properties;i++) {
            properties.add(sampleOnePropertyPO(g, p, po));
        }
        
        return properties;
    }      
    
    /*
        - p: probability of executing one more iteration
    */
    public static DLG sampleOneProperty(DLG g, double p) throws Exception {
        int n = g.getNVertices();
        List<Pair<Integer,Integer>> nextEdgesToAdd = new ArrayList<>();
        List<Integer> vertices = new ArrayList<>();
        
        // 1) pick a vertex at random
        int v = r.nextInt(n);
        vertices.add(v);
        
        // 2) Add all the outgoing edges of "v" to the posslble edges to add
        for(int i = 0;i<n;i++) {
            if (g.getEdge(v, i) != null) nextEdgesToAdd.add(new Pair<>(v,i));
        }        
        
        // 3) with probability p add an additional vertex or edge:
        while(nextEdgesToAdd.size()>0 && r.nextDouble()<p) {
            Pair<Integer,Integer> edge = nextEdgesToAdd.remove(r.nextInt(nextEdgesToAdd.size()));
            int v1 = edge.m_a;
            int v2 = edge.m_b;
            if (!vertices.contains(v2)) {
                vertices.add(v2);
                for(int i = 0;i<n;i++) {
                    if (i!=v1 && g.getEdge(v2, i) != null) nextEdgesToAdd.add(new Pair<>(v2,i));
                }        
            }
        }
        
        // 4) construct the property and return:
        DLG g2 = g.subgraph(vertices);
        // 4.1) remove all the edges that we did not add:
        for(Pair<Integer,Integer> e:nextEdgesToAdd) {
            int v1 = vertices.indexOf(e.m_a);
            int v2 = vertices.indexOf(e.m_b);
            if (v1>=0 && v2>=0) {
                g2.setEdge(v1, v2, null);
            }
        }
        
        return g2;
    }



    /*
        - p: probability of executing one more iteration
    */
    public static DLG sampleOnePropertyPO(DLG g, double p, PartialOrder po) throws Exception {
        int n = g.getNVertices();
        List<Pair<Integer,Integer>> nextEdgesToAdd = new ArrayList<>();
        List<Integer> verticesToSpecialize = new ArrayList<>();
        List<Integer> vertices = new ArrayList<>();
        List<List<Label>> vertexLabels = new ArrayList<>();
        
        // 1) pick a vertex at random
        int v = r.nextInt(n);
        vertices.add(v);
        List<Label> path = labelPathToTop(g.getVertex(v), po);
        vertexLabels.add(path);
        if (path.size()>1) verticesToSpecialize.add(v);
        
        // 2) Add all the outgoing edges of "v" to the posslble edges to add
        for(int i = 0;i<n;i++) {
            if (g.getEdge(v, i) != null) nextEdgesToAdd.add(new Pair<>(v,i));
        }        
        
        // 3) with probability p add an additional vertex, edge, or specialize a vertex:
        while((nextEdgesToAdd.size()>0 || verticesToSpecialize.size()>0) && r.nextDouble()<p) {
//            System.out.println(vertices.size() + "-" + nextEdgesToAdd.size() + " - " + verticesToSpecialize.size());
//            System.out.println(vertexLabels);
            int n1 = nextEdgesToAdd.size();
            int n2 = verticesToSpecialize.size();
            int selection = r.nextInt(n1+n2);
            if (selection<n1) {
                // add an edge:
                Pair<Integer,Integer> edge = nextEdgesToAdd.remove(selection);
                int v1 = edge.m_a;
                int v2 = edge.m_b;
                if (!vertices.contains(v2)) {
                    vertices.add(v2);
                    path = labelPathToTop(g.getVertex(v2), po);
                    vertexLabels.add(path);
                    if (path.size()>1) verticesToSpecialize.add(v2);
                    for(int i = 0;i<n;i++) {
                        if (i!=v1 && g.getEdge(v2, i) != null) nextEdgesToAdd.add(new Pair<>(v2,i));
                    }        
                }
            } else {
                // spezialize a vertex:
                selection -= n1;
                vertexLabels.get(selection).remove(0);
                if (vertexLabels.get(selection).size()<=1) {
                    verticesToSpecialize.remove(selection);
                    vertexLabels.remove(selection);
                }
            }
        }
        
        // 4) construct the property and return:
        DLG g2 = g.subgraph(vertices);
        // 4.1) remove all the edges that we did not add:
        for(Pair<Integer,Integer> e:nextEdgesToAdd) {
            int v1 = vertices.indexOf(e.m_a);
            int v2 = vertices.indexOf(e.m_b);
            if (v1>=0 && v2>=0) {
                g2.setEdge(v1, v2, null);
            }
        }
        // 4.2) set the sorts of the vertices:
        for(int i = 0;i<vertexLabels.size();i++) {
            g2.setVertex(i, vertexLabels.get(i).get(0));
        }
        
        return g2;
    }

    
    public static List<Label> labelPathToTop(Label l, PartialOrder po)
    {
        List<Label> path = new ArrayList<>();
        path.add(l);
        Label current = l;
        Label[] ancestors = po.getAncestors(current);
        while(ancestors!=null && ancestors.length>0) {
            current = ancestors[0];
            path.add(0, current);
            ancestors = po.getAncestors(current);
        }
        return path;
    }
}

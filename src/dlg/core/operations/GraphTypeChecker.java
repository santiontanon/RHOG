/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.core.TreeDLG;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 * 
 * This class contains a collection of methods to check whether a graph is a tree,
 * a directed-acyclic graph (DAG), a lattice or a semi-lattice.
 * 
 */
public class GraphTypeChecker {
    public static int DEBUG = 0;
    
    
    public static boolean isTree(DLG g) {    
        int root = -1;
        for(int i = 0;i<g.getNVertices();i++) {
            int nparents = 0;
            for(int j = 0;j<g.getNVertices();j++) {
                if (g.getEdge(j, i)!=null) {
                    nparents++;
                    if (nparents>1) return false;
                }
            }
            if (nparents==0) {
                if (root == -1) {
                    root = i;
                    if (g instanceof TreeDLG) {
                        // this is just a sanity check:
                        if (((TreeDLG)g).getRoot()!=root) {
                            System.err.println("GraphTypeChecker: root node found does not correspond with TreeDLG.getRoot()");
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        if (root==-1) return false;
        return true;
    }    
    

    // tests whether 'g' is a directed acyclic graph (DAG):
    public static boolean isDAG(DLG g) {
        int n = g.getNVertices();
        
        for(int v = 0;v<n;v++) {
            boolean closed[] = new boolean[n];
            List<Integer> open = new ArrayList<>();
            open.add(v);
            // try to find cycles:
            while(!open.isEmpty()) {
                int current = open.remove(0);
                closed[current] = true;
                for(int w:g.getCondensedOutgoingEdges()[current]) {
                    if (w==v) return false;
                    if (!closed[w] && !open.contains(w)) {
                        open.add(w);
                    }
                }
            }
        }
        
        return true;
    }
    
    
    public static boolean isLattice(DLG g) {
        return isUpperSemiLattice(g) && isLowerSemiLattice(g);
    }
    

    public static boolean isBoundedLattice(DLG g) {
        return isUpperBoundedSemiLattice(g) && isLowerBoundedSemiLattice(g);
    }

    
    public static boolean isUpperBoundedSemiLattice(DLG g) {
        return getUpperSemiLatticeBound(g)!=1;
    }
    
    
    public static int getUpperSemiLatticeBound(DLG g) {
//        if (!isUpperSemiLattice(g)) return -1;
        int n = g.getNVertices();
        for(int v = 0;v<n;v++) {
            List<Integer> d = descendants(v, g);
            if (d.size() == n) {
                // we found a supremum!
                return v;
            }
        }
        return -1;
    }
    

    public static boolean isLowerBoundedSemiLattice(DLG g) {
        return getLowerSemiLatticeBound(g)!=1;
    }
    
    
    public static int getLowerSemiLatticeBound(DLG g) {
        if (!isLowerSemiLattice(g)) return -1;
        int n = g.getNVertices();
        for(int v = 0;v<n;v++) {
            List<Integer> d = ancestors(v, g);
            if (d.size() == n) {
                // we found an infimum!
                return v;
            }
        }
        return -1;
    }
    
    
    public static boolean isUpperSemiLattice(DLG g) {
        int n = g.getNVertices();
        
        for(int i = 0;i<n;i++) {
            for(int j = i+1;j<n;j++) {
                if (upperBounds(i, j, g).size()!=1) return false;
            }
        }
        
        return true;
    }

    
    public static boolean isLowerSemiLattice(DLG g) {
        int n = g.getNVertices();
        
        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                if (lowerBounds(i, j, g).size()!=1) return false;
            }
        }
        
        return true;
    }


    public static List<Integer> upperBounds(int v1, int v2, DLG g) {
        int n = g.getNVertices();
        List descendants[] = new List[n];
        for(int v = 0;v<n;v++) descendants[v] = descendants(v, g);
        List<Integer> ancestorsv1 = ancestors(v1, g);
        List<Integer> ancestorsv2 = ancestors(v2, g);
        List<Integer> upperBounds = new ArrayList<>();
        for(int a:ancestorsv1) {
            if (ancestorsv2.contains(a)) {
                boolean ignore = false;
                List<Integer> toDelete = new ArrayList<>();
                for(int ub:upperBounds) {
                    if (descendants[ub].contains(a)) {
                        toDelete.add(ub);
                    }
                    if (descendants[a].contains(ub)) {
                        ignore = true;
                    }
                }
                upperBounds.removeAll(toDelete);
                if (!ignore) upperBounds.add(a);
            }
        }
        if (DEBUG>=1) System.out.println("upperBounds("+v1+","+v2+") = " + upperBounds);
        return upperBounds;
    }
    
    
    public static List<Integer> lowerBounds(int v1, int v2, DLG g) {
        int n = g.getNVertices();
        List ancestors[] = new List[n];
        for(int v = 0;v<n;v++) ancestors[v] = ancestors(v, g);
        List<Integer> descendantsv1 = descendants(v1, g);
        List<Integer> descendantsv2 = descendants(v2, g);
        List<Integer> lowerBounds = new ArrayList<>();
        for(int a:descendantsv1) {
            if (descendantsv2.contains(a)) {
                boolean ignore = false;
                List<Integer> toDelete = new ArrayList<>();
                for(int ub:lowerBounds) {
                    if (ancestors[ub].contains(a)) {
                        toDelete.add(ub);
                    }
                    if (ancestors[a].contains(ub)) {
                        ignore = true;
                    }
                }
                lowerBounds.removeAll(toDelete);
                if (!ignore) lowerBounds.add(a);
            }
        }
        if (DEBUG>=1) System.out.println("lowerBounds("+v1+","+v2+") = " + lowerBounds);
        return lowerBounds;    
    }
    
    
    // returns a list of all the nodes reachable from "v" (including "v" itself)
    public static List<Integer> descendants(int v, DLG g) {
        List<Integer> open = new ArrayList<>();
        List<Integer> closed = new ArrayList<>();
        open.add(v);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            closed.add(current);
            for(int next:g.getCondensedOutgoingEdges()[current]) {
                if (!open.contains(next) && !closed.contains(next)) {
                    open.add(next);
                }
            }
        }
        if (DEBUG>=2) System.out.println("descendants("+v+") = " + closed);
        return closed;
    }


    // returns a list of all the nodes from whom "v" is reachable (including "v" itself)
    public static List<Integer> ancestors(int v, DLG g) {
        List<Integer> open = new ArrayList<>();
        List<Integer> closed = new ArrayList<>();
        open.add(v);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            closed.add(current);
            for(int next:g.getCondensedIncomingEdges()[current]) {
                if (!open.contains(next) && !closed.contains(next)) {
                    open.add(next);
                }
            }
        }
        if (DEBUG>=2) System.out.println("ancestors("+v+") = " + closed);
        return closed;
    }    
}

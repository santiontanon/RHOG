/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.core.operations;

import dlg.core.DLG;
import dlg.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class Connectivity {
    /*
    The implementation in this class is basically an adapte version of the
    code found in this website:
    http://www.geeksforgeeks.org/bridge-in-a-graph/
    
    Moreover, since the graphs I am considering are connected, I have added a
    special check: when two edges are connected by two different edges (one in 
    each direction), none of these edges can be a bridge. The rest of the 
    algorithm remains unchanged.
    */
    
    
    // checks if the graph is connected or not:
    public static boolean isConnected(DLG g) {
        boolean closed[] = new boolean[g.getNVertices()];
        List<Integer> open = new ArrayList<>();
        int n = 0;
        
        open.add(0);
        while(!open.isEmpty()) {
            int current = open.remove(0);
            closed[current] = true;
            n++;
            for(int v:g.getCondensedOutgoingEdges()[current]) {
                if (!closed[v] && !open.contains(v)) open.add(v);
            }
            for(int v:g.getCondensedIncomingEdges()[current]) {
                if (!closed[v] && !open.contains(v)) open.add(v);
            }
        }
        
        if (n==g.getNVertices()) return true;
        return false;
    }
    
    
    // finds all the edges that if removed make the graph not connected:
    public static List<Pair<Integer,Integer>> getBridges(DLG g) {
        List<Pair<Integer,Integer>> bridges = new ArrayList<>();
        
        // Mark all the vertices as not visited
        boolean visited[] = new boolean[g.getNVertices()];
        int disc[] = new int[g.getNVertices()];
        int low[] = new int[g.getNVertices()];
        int parent[] = new int[g.getNVertices()];
        int time[] = {0};

        // Initialize parent and visited arrays
        for (int i = 0; i < g.getNVertices(); i++)
        {
            parent[i] = -1;
            visited[i] = false;
        }

        // Call the recursive helper function to find Bridges
        // in DFS tree rooted with vertex 'i'
        for (int i = 0; i < g.getNVertices(); i++)
            if (visited[i] == false) {
                bridgeUtil(g, i, visited, disc, low, parent, time, bridges);
            }
        
        return bridges;
    }    
    
    
    static void bridgeUtil(DLG g, int u, boolean visited[], int disc[], int low[], int parent[], int time[], List<Pair<Integer,Integer>> bridges)
    {
        // Mark the current node as visited
        visited[u] = true;

        // Initialize discovery time and low value
        time[0]++;
        disc[u] = low[u] = time[0];

        for(int v:g.getCondensedOutgoingEdges()[u]) {
            if (!visited[v]) {
                parent[v] = u;
                bridgeUtil(g, v, visited, disc, low, parent, time, bridges);

                // Check if the subtree rooted with v has a connection to
                // one of the ancestors of u
                low[u]  = Math.min(low[u], low[v]);

                // If the lowest vertex reachable from subtree under v is 
                // below u in DFS tree, then u-v is a bridge
                if (low[v] > disc[u] &&
                    (g.getEdge(u, v)==null ||
                     g.getEdge(v, u)==null)) {
                    bridges.add(new Pair<>(u,v));
                }
            }

            // Update low value of u for parent function calls.
            else if (v != parent[u])
                low[u]  = Math.min(low[u], disc[v]);
        }
        for(int v:g.getCondensedIncomingEdges()[u]) {
            if (!visited[v]) {
                parent[v] = u;
                bridgeUtil(g, v, visited, disc, low, parent, time, bridges);

                // Check if the subtree rooted with v has a connection to
                // one of the ancestors of u
                low[u]  = Math.min(low[u], low[v]);

                // If the lowest vertex reachable from subtree under v is 
                // below u in DFS tree, then u-v is a bridge
                if (low[v] > disc[u] &&
                    (g.getEdge(u, v)==null ||
                     g.getEdge(v, u)==null)) {
                    bridges.add(new Pair<>(u,v));
                }
            }

            // Update low value of u for parent function calls.
            else if (v != parent[u])
                low[u]  = Math.min(low[u], disc[v]);
        }
    }    
}

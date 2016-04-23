/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml;

import dlg.core.DLG;
import dlg.ml.distance.Distance;
import dlg.util.Label;
import dlg.util.Pair;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author santi
 */
public class KNearestNeighbors extends Classifier {
    public static int DEBUG = 0;
    
    int m_k = 1;
    Distance m_d = null;
    
    List<DLG> m_training_instances = null;
    List<Label> m_training_labels = null;
    
    public KNearestNeighbors(int k, Distance d) {
        m_k = k;
        m_d = d;
    }
    
    
    public void train(List<DLG> instances, List<Label> labels) throws Exception {
        m_training_instances = instances;
        m_training_labels = labels;
        m_d.train(instances, labels);
    }
    
    
    public Label predict(DLG test_instance) throws Exception {
        int retrieved[] = knn(test_instance);
        Pair<Label, Integer> votingResult = voting(retrieved);
        return votingResult.m_a;
    }
    
    
    public int[] knn(DLG test_instance) throws Exception {
        int n = m_training_instances.size();
        int retrieved[] = new int[m_k];
        double retrievedDistances[] = new double[m_k];
        for(int j = 0;j<m_k;j++) retrieved[j] = -1;
                
        for(int j = 0;j<n;j++) {
            double distance = m_d.distance(test_instance, m_training_instances.get(j));
            if (DEBUG>=1) System.out.println("knn: (" + j + "/" + n + "), d = " + distance);
            addToRetrieved(retrieved, retrievedDistances, j, distance);
        }
        
        return retrieved;
    }
    
    
    public void addToRetrieved(int retrieved[], double retrievedDistanves[], int idx, double d) {
        int k = retrieved.length;
        for(int i = 0;i<k;i++) {
            if (retrieved[i]==-1 || retrievedDistanves[i]>d) {
                // we found the spot!
                // move everything to the right:
                for(int j = k-1;j>i;j--) {
                    retrieved[j] = retrieved[j-1];
                    retrievedDistanves[j] = retrievedDistanves[j-1];
                }
                
                // insert the new instance:
                retrieved[i] = idx;
                retrievedDistanves[i] = d;
                return;
            }
        }
    }
      
    
    public Pair<Label,Integer> voting(int []retrieved) {
        HashMap<Label, Integer> votes = new HashMap<>();
        Label mostVoted = null;
        int mostVotedVotes = 0;
        for(int r:retrieved) {
            Label label = m_training_labels.get(r);
            if (votes.get(label)==null) {
                votes.put(label, 1);
                if (mostVotedVotes==0) {
                    mostVoted = label;
                    mostVotedVotes = 1;
                }
            } else {
                int v = votes.get(label)+1;
                votes.put(label, v);
                if (v > mostVotedVotes) {
                    mostVoted = label;
                    mostVotedVotes = v;
                }
            }
        }
        
        return new Pair<>(mostVoted, mostVotedVotes);
    }
    
    
}

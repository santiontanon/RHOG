/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dlg.ml;

import dlg.core.DLG;
import dlg.util.Label;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author santi
 */
public class Experimenter {
    public static int DEBUG = 0;
    
    
    public static List<Label> differentLabels(List<Label> labels) {
        List<Label> different = new ArrayList<Label>();
        for(Label l:labels) {
            if (!different.contains(l)) different.add(l);
        }
        
        return different;
    }
    
    
    public static int[][] leaveOneOut(MLDataSet ds, int labels, Classifier c) throws Exception {
        List<Label> different = differentLabels(ds.m_labels[labels]);
        int confusionMatrix[][] = new int[different.size()][different.size()];
        long start_time = System.currentTimeMillis();
        
        for(int test_index = 0;test_index<ds.m_instances.size();test_index++) {
            if (DEBUG>=1) System.out.println("Experimented.leaveOneOut: text index: " + test_index);

            List<DLG> trainingSet = new ArrayList<>();
            List<Label> trainingLabels = new ArrayList<>();
            trainingSet.addAll(ds.m_instances);
            trainingLabels.addAll(ds.m_labels[labels]);
            trainingSet.remove(test_index);
            trainingLabels.remove(test_index);
            
            if (DEBUG>=1) System.out.println("Experimented.leaveOneOut: learning... ");
            c.train(trainingSet, trainingLabels);
            if (DEBUG>=1) System.out.println("Ok");

            if (DEBUG>=2) System.out.println("Experimented.leaveOneOut: predicting... ");
            Label l = c.predict(ds.m_instances.get(test_index));
            Label gt = ds.m_labels[labels].get(test_index);
            if (DEBUG>=1) System.out.println(l + "\t (ground truth was "+gt+")");

            confusionMatrix[different.indexOf(gt)][different.indexOf(l)]++;
        }
        long end_time = System.currentTimeMillis();
        
        double correct = 0;
        double total = 0;
        for(int i = 0;i<different.size();i++) {
            System.out.print(different.get(i) + "\t");
            for(int j = 0;j<different.size();j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
                total+=confusionMatrix[i][j];
            }
            correct+=confusionMatrix[i][i];
            System.out.println("");
        }
        System.out.println("leave-one-out accuracy: " + (correct/total) + " (" + correct + " / " + total + ")");
        System.out.println("Time (total / per instance ): " + (end_time - start_time) + " / " + (end_time - start_time)/((double)total));
        return confusionMatrix;
    }
    
    
    public static int[][] percentageSplit(MLDataSet ds, int labels, Classifier c, double testPercentage) throws Exception {
        List<Label> different = differentLabels(ds.m_labels[labels]);
        int confusionMatrix[][] = new int[different.size()][different.size()];
        
        List<DLG> trainingSet = new ArrayList<>();
        List<Label> trainingLabels = new ArrayList<>();
        List<DLG> testSet = new ArrayList<>();
        List<Label> testLabels = new ArrayList<>();
        trainingSet.addAll(ds.m_instances);
        trainingLabels.addAll(ds.m_labels[labels]);
        int testSize = (int)(trainingSet.size()*testPercentage/100.0);
        Random r = new Random();
        for(int i = 0;i<testSize;i++) {
            int idx = r.nextInt(trainingSet.size());
            testSet.add(trainingSet.remove(idx));
            testLabels.add(trainingLabels.remove(idx));
        }
        if (DEBUG>=1) System.out.println("Experimented.percentageSplit: training set size: " + trainingSet.size() + ", test set size: " + testSet.size());
            
        if (DEBUG>=1) System.out.println("Experimented.percentageSplit: learning... ");
        c.train(trainingSet, trainingLabels);
        if (DEBUG>=1) System.out.println("Ok");

        for(int i = 0;i<testSet.size();i++) {
            if (DEBUG>=2) System.out.println("Experimented.percentageSplit: predicting... ");
            Label l = c.predict(testSet.get(i));
            Label gt = testLabels.get(i);
            if (DEBUG>=1) System.out.println(l + "\t (ground truth was "+gt+")");

            confusionMatrix[different.indexOf(gt)][different.indexOf(l)]++;
        }
        
        double correct = 0;
        double total = 0;
        for(int i = 0;i<different.size();i++) {
            System.out.print(different.get(i) + "\t");
            for(int j = 0;j<different.size();j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
                total+=confusionMatrix[i][j];
            }
            correct+=confusionMatrix[i][i];
            System.out.println("");
        }
        System.out.println("leave-one-out accuracy: " + (correct/total) + " (" + correct + " / " + total + ")");
        return confusionMatrix;
    }    

    
    public static int[][] kFoldCrossValidation(MLDataSet ds, int labels, Classifier c, int folds) throws Exception {
        List<Label> different = differentLabels(ds.m_labels[labels]);
        int confusionMatrix[][] = new int[different.size()][different.size()];
        
        List<DLG> foldInstances[] = new List[folds];
        List<Label> foldLabels[] = new List[folds];
        
        List<DLG> allInstances = new ArrayList<>();
        List<Label> allLabels = new ArrayList<>();
        allInstances.addAll(ds.m_instances);
        allLabels.addAll(ds.m_labels[labels]);
        
        Random r = new Random();
        int f = 0;
        while(!allInstances.isEmpty()) {
            if (foldInstances[f]==null) {
                foldInstances[f] = new ArrayList<>();
                foldLabels[f] = new ArrayList<>();
            }
            int i = r.nextInt(allInstances.size());
            foldInstances[f].add(allInstances.remove(i));
            foldLabels[f].add(allLabels.remove(i));
            f = (f+1)%folds;
        }
        
        for(int fold = 0;fold<folds;fold++) {
            List<DLG> trainingSet = new ArrayList<>();
            List<Label> trainingLabels = new ArrayList<>();
            List<DLG> testSet = new ArrayList<>();
            List<Label> testLabels = new ArrayList<>();
            
            for(int i = 0;i<folds;i++) {
                if (i==fold) {
                    testSet.addAll(foldInstances[i]);
                    testLabels.addAll(foldLabels[i]);
                } else {
                    trainingSet.addAll(foldInstances[i]);
                    trainingLabels.addAll(foldLabels[i]);
                }
            }
            
            if (DEBUG>=1) System.out.println("Experimented.kFoldCrossValidation: fold "+(fold+1)+", training set size: " + trainingSet.size() + ", test set size: " + testSet.size());

            if (DEBUG>=1) System.out.println("Experimented.kFoldCrossValidation: learning... ");
            c.train(trainingSet, trainingLabels);
            if (DEBUG>=1) System.out.println("Ok");

            for(int i = 0;i<testSet.size();i++) {
                if (DEBUG>=2) System.out.println("Experimented.kFoldCrossValidation: predicting... ");
                Label l = c.predict(testSet.get(i));
                Label gt = testLabels.get(i);
                if (DEBUG>=1) System.out.println(l + "\t (ground truth was "+gt+")");

                confusionMatrix[different.indexOf(gt)][different.indexOf(l)]++;
            }
        }
        
        double correct = 0;
        double total = 0;
        for(int i = 0;i<different.size();i++) {
            System.out.print(different.get(i) + "\t");
            for(int j = 0;j<different.size();j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
                total+=confusionMatrix[i][j];
            }
            correct+=confusionMatrix[i][i];
            System.out.println("");
        }
        System.out.println("leave-one-out accuracy: " + (correct/total) + " (" + correct + " / " + total + ")");
        return confusionMatrix;
        
    }
    

    public static int[][] testWithSpecificTrainingAndTestSets(List<DLG> trainingSet, List<Label> trainingLabels,
                                                           List<DLG> testSet, List<Label> testLabels,
                                                           Classifier c) throws Exception {
        List<Label> different = new ArrayList<Label>();
        for(Label l:trainingLabels) {
            if (!different.contains(l)) different.add(l);
        }
        for(Label l:testLabels) {
            if (!different.contains(l)) different.add(l);
        }
        int confusionMatrix[][] = new int[different.size()][different.size()];
        
        if (DEBUG>=1) System.out.println("Experimented.testWithSpecificTrainingAndTestSets: training set size: " + trainingSet.size() + ", test set size: " + testSet.size());
            
        if (DEBUG>=1) System.out.println("Experimented.testWithSpecificTrainingAndTestSets: learning... ");
        c.train(trainingSet, trainingLabels);
        if (DEBUG>=1) System.out.println("Ok");

        for(int i = 0;i<testSet.size();i++) {
            if (DEBUG>=2) System.out.println("Experimented.testWithSpecificTrainingAndTestSets: predicting... ");
            Label l = c.predict(testSet.get(i));
            Label gt = testLabels.get(i);
            if (DEBUG>=1) System.out.println(l + "\t (ground truth was "+gt+")");

            confusionMatrix[different.indexOf(gt)][different.indexOf(l)]++;
        }
        
        double correct = 0;
        double total = 0;
        for(int i = 0;i<different.size();i++) {
            System.out.print(different.get(i) + "\t");
            for(int j = 0;j<different.size();j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
                total+=confusionMatrix[i][j];
            }
            correct+=confusionMatrix[i][i];
            System.out.println("");
        }
        System.out.println("leave-one-out accuracy: " + (correct/total) + " (" + correct + " / " + total + ")");
        return confusionMatrix;
    }    


}

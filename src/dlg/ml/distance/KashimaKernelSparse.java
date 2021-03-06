/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dlg.ml.distance;

import java.util.List;
import java.util.ArrayList;

import dlg.core.DLG;
import dlg.core.PartialOrder;
import dlg.core.TreeDLG;
import dlg.util.Label;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;


public class KashimaKernelSparse extends Distance {

    public static int DEBUG = 0;

    public double q = 0.1;
    public PartialOrder m_po = null;
    Algebra a = new Algebra();

    public KashimaKernelSparse(double a_q, PartialOrder a_po) {
        q = a_q;
        m_po = a_po;
    }


    public void train(List<DLG> instances, List<Label> labels) throws Exception {

    }


    public double distance(DLG g1, DLG g2) throws Exception {
        return 1.0 - similarity(g1, g2);
    }


    public double similarity(DLG g1, DLG g2) throws Exception {
        int n1 = g1.getNVertices(), n2 = g2.getNVertices();
        double[][] b = new double[1][n1 * n2];
        double[][] r1 = new double[n1 * n2][1];
        // double []rinfinity = new double[n1*n2];
        double[][] t = new double[n1 * n2][n1 * n2];
        double[][] ImT = new double[n1 * n2][n1 * n2];

        // compute 'b' matrix:
        if (DEBUG >= 1) {
            System.out.println("b matrix:");
        }
        if (g1 instanceof TreeDLG
                && g2 instanceof TreeDLG) {
            b[0][0] = labelSimilarity(g1.getVertex(0), g2.getVertex(0));
        } else {
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    b[0][i * n2 + j] = labelSimilarity(g1.getVertex(i), g2.getVertex(j)) / (n1 * n2);
                    if (DEBUG >= 1 && b[0][i * n2 + j] != 0) {
                        System.out.println(i + "," + j + " -> " + b[0][i * n2 + j]);
                    }
                }
            }
        }

        // compute 'q' and 'r1' matrices (they are the same):
        if (DEBUG >= 1) System.out.println("q matrix:");
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                double pq1 = 1;
                double pq2 = 1;
                if (g1.getCondensedOutgoingEdges()[i].length>0) pq1 = q;
                if (g2.getCondensedOutgoingEdges()[j].length>0) pq2 = q;
                r1[i * n2 + j][0] = pq1 * pq2;
                if (DEBUG >= 1 && r1[i * n2 + j][0] != 0) System.out.println(i + "," + j + " -> " + r1[i * n2 + j][0]);
            }
        }

        // compute the 't' matrix:
        if (DEBUG >= 1) {
            System.out.println("t matrix:");
        }
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int ip = 0; ip < n1; ip++) {
                    for (int jp = 0; jp < n2; jp++) {
                        double pt1 = 0; // transition probability in g1
                        double pt2 = 0; // transition probability in g2
                        double vs = 0; // vertex similarity
                        double es = 0; // edge similarity

                        if (g1.getEdge(ip, i)!=null) {
                            int ns1 = g1.getCondensedOutgoingEdges()[ip].length;
                            pt1 = (1.0 - q) / ns1;
                        }
                        if (g2.getEdge(jp, j)!=null) {
                            int ns2 = g2.getCondensedOutgoingEdges()[jp].length;
                            pt2 = (1.0 - q) / ns2;
                        }

                        if (g1.getEdge(ip,i) != null && g2.getEdge(jp,j) != null) {
                            vs = labelSimilarity(g1.getVertex(i), g2.getVertex(j));
                            es = labelSimilarity(g1.getEdge(ip,i), g2.getEdge(jp,j));
                            // System.out.println(i + "," + j + "," + ip + "," + jp + " -> " + "(" + pt1 + "," + pt2 +
                            // "," + vs + "," + es + ") <- " + g1.m_label_dictionary.get(el1) + "," +
                            // g2.m_label_dictionary.get(el2));
                        }

                        t[i * n2 + j][ip * n2 + jp] = pt1 * pt2 * vs * es;
                        if (DEBUG >= 1 && t[i * n2 + j][ip * n2 + jp] != 0) {
                            System.out.println(i + "," + j + "," + ip + "," + jp + " -> " + t[i * n2 + j][ip * n2 + jp]);
                        }
                    }
                }
            }
        }

        // compute the I - t matrix:
        for (int i = 0; i < n1 * n2; i++) {
            for (int j = 0; j < n1 * n2; j++) {
                double tmp = 0;
                if (i == j) {
                    tmp = 1;
                }
                ImT[i][j] = tmp - t[i][j];
            }
        }

        // invert the "I - t" matrix:
        DoubleMatrix2D ImT_matrix = new SparseDoubleMatrix2D(ImT);
//        System.out.println("inverting " + ImT_matrix.rows());
        DoubleMatrix2D ImT_matrix_inverse = a.inverse(ImT_matrix);
//        System.out.println("done!");
        DoubleMatrix2D r1_matrix = new SparseDoubleMatrix2D(r1);
        DoubleMatrix2D b_matrix = new SparseDoubleMatrix2D(b);
        DoubleMatrix2D tmp = multiplyMatrix(b_matrix, multiplyMatrix(a.transpose(ImT_matrix_inverse), r1_matrix));
        
       
        // System.out.println(tmp.get(0,0));
        return tmp.get(0, 0);
    }
    

    static DoubleMatrix2D multiplyMatrix(DoubleMatrix2D A, DoubleMatrix2D B) {
            DoubleMatrix2D C = new SparseDoubleMatrix2D(A.rows(), B.columns());
            A.zMult(B, C);
            return C;
    }


    public double labelSimilarity(Label l1, Label l2) throws Exception {
        if (m_po == null) {
            if (l1.equals(l2)) {
                return 1;
            }
            return 0;
        } else {
            Label[] sl1 = m_po.getAncestors(l1);
            Label[] sl2 = m_po.getAncestors(l2);

            double shared = 0;
            List<Label> total = new ArrayList<>();
            if (!total.contains(l1)) {
                total.add(l1);
            }
            if (!total.contains(l2)) {
                total.add(l2);
            }
            for (Label al1 : sl1) {
                for (Label al2 : sl2) {
                    if (al2.equals(al1)) {
                        shared++;
                        break;
                    }
                }
                if (!total.contains(al1)) {
                    total.add(al1);
                }
            }
            if (l1.equals(l2)) {
                shared++;
            }
            for (Label l : sl2) {
                if (!total.contains(l)) {
                    total.add(l);
                }
            }
            return shared / total.size();
        }
    }
}

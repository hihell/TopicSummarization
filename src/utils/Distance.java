package utils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import java.util.ArrayList;

/**
 * Created by jiusi on 2/11/14.
 */
public class Distance {
    public static double euclideanDistance(ArrayList<Double> v1, ArrayList<Double> v2) {
        assert (v1.size() == v2.size());

        double accu = 0.0;

        for(int i = 0; i < v1.size(); i++) {
            accu += Math.pow(v1.get(i) - v2.get(i), 2);
        }

        return Math.pow(accu, .5);
    }

    public static double euclideanDistance(DoubleMatrix2D matrix, int v1dex, int v2dex) {

        assert (v1dex < matrix.rows());
        assert (v2dex < matrix.rows());

        DoubleMatrix1D v1 = matrix.viewRow(v1dex);
        DoubleMatrix1D v2 = matrix.viewRow(v2dex);

        double accu = 0.0;

        for(int i = 0; i < v1.size(); i++) {
            accu += Math.pow(v1.get(i) - v2.get(i), 2);
        }

        return Math.pow(accu, .5);
    }

    public static boolean isOrthogonality(DoubleMatrix2D matrix, int v1dex, int v2dex) {

        assert (v1dex < matrix.rows());
        assert (v2dex < matrix.rows());

        DoubleMatrix1D v1 = matrix.viewRow(v1dex);
        DoubleMatrix1D v2 = matrix.viewRow(v2dex);

        double accu = 0.0;

        for(int i = 0; i < v1.size(); i++) {
            accu += Math.pow(v1.get(i) - v2.get(i), 2);
            if(accu > 0.0) {
                return false;
            }
        }

        assert (accu == 0.0);
        return true;
    }

    public static double euclideanDistance(DoubleMatrix1D v1, ArrayList<Double> v2) {
        assert (v1.size() == v2.size()) ;

        ArrayList<Double> nv1 = new ArrayList<Double>();
        for(int i = 0; i < v1.size(); i++) {
            nv1.add(v1.get(i));
        }

        return euclideanDistance(nv1, v2);
    }

}

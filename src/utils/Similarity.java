package utils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import org.omg.CORBA._IDLTypeStub;

import java.util.ArrayList;

/**
 * Created by jiusi on 2/18/14.
 */
public class Similarity {

    public static Double cosine(ArrayList<Double> v1, ArrayList<Double> v2) {
        double sum = 0.0;
        for(int i=0; i<v1.size(); i++) {
            sum += v1.get(i) * v2.get(i);
        }

        double lv1 = 0.0;
        for(double v : v1) {
            lv1 += Math.pow(v, 2);
        }
        lv1 = Math.pow(lv1, .5);

        double lv2 = 0.0;
        for(double v : v2) {
            lv2 += Math.pow(v, 2);
        }
        lv2 = Math.pow(lv2, .5);

        if (lv1 * lv2 == 0.0) {
            return 0.0;
        }

        return sum / (lv1 * lv2);
    }

    public static Double cosine(DoubleMatrix1D d1, ArrayList<Double> v2) {
        ArrayList<Double> v1 = new ArrayList<Double>();

        for(int i = 0; i < d1.size(); i++) {
            v1.add(d1.get(i));
        }

        return cosine(v1, v2);
    }

    public static boolean isOrthogonality(DoubleMatrix2D matrix, int v1dex, int v2dex) {

        assert (v1dex < matrix.rows());
        assert (v2dex < matrix.rows());

        DoubleMatrix1D v1 = matrix.viewRow(v1dex);
        DoubleMatrix1D v2 = matrix.viewRow(v2dex);

        double accu = 0.0;

        for(int i = 0; i < v1.size(); i++) {

            accu = v1.get(i) * v2.get(i);

            if(accu > 0.0) {
                return false;
            }
        }

        assert (accu == 0.0);
        return true;
    }

}

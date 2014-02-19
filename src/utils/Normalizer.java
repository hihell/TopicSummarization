package utils;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;

import java.util.ArrayList;

/**
 * Created by jiusi on 2/12/14.
 */
public class Normalizer {
    public static ArrayList<Double> normalizeVector(ArrayList<Double> vector) {

        ArrayList<Double> nv = new ArrayList<Double>();

        double accu = 0.0;
        for(double val : vector) {
            accu += Math.pow(val, 2);
        }

        if(accu == 0.0) {
            return vector;
        }

        for(int i = 0; i < vector.size(); i++) {
            double old = vector.get(i);
            nv.add(i, old / Math.sqrt(accu));
        }

        return nv;
    }

    public static DoubleMatrix1D normalizeVector(DoubleMatrix1D vector) {
        ArrayList<Double> vl = new ArrayList<Double>();

        for(int i = 0; i < vector.size(); i++) {
            vl.add(Math.pow(vector.get(i), 2));
        }

        vl = normalizeVector(vl);

        DoubleMatrix1D rt = DoubleFactory1D.sparse.make(vl.size());

        for(int i = 0; i < vector.size(); i++) {
            rt.set(i, vl.get(i));
        }

        return rt;
    }

}

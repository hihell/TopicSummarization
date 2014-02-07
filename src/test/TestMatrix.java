package test;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import java.util.ArrayList;


public class TestMatrix {


    public void testSparse(int size) {
        System.out.println("sparse");
        DoubleMatrix2D ma = DoubleFactory2D.sparse.make(size,size);

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {

                if(j / 13 * 13 == j) {
                    ma.set(i,j, j);
                } else {
                    ma.set(i,j, 0.0);
                }
            }
        }

        System.out.println(ma.size());
    }

    public void testDense(int size) {
        System.out.println("dense");
        ArrayList<ArrayList<Double>> ma = new ArrayList<ArrayList<Double>>();
        for(int i = 0; i < size; i++) {
            ArrayList<Double> arr = new ArrayList<Double>();
            ma.add(arr);
            for(int j = 0; j < size; j++) {

                if(j / 2 * 2 == j) {
                    arr.add(Double.valueOf(j));
                } else {
                    arr.add(0.0);
                }
            }
        }

        System.out.println(ma.size());


    }

    public static void main(String args[]) {
        TestMatrix tm = new TestMatrix();
        int size = 10000;
//        tm.testDense(size);
        tm.testSparse(size);
        System.out.println("here");

    }
}


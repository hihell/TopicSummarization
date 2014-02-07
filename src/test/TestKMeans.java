package test;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 30/11/2013
 * Time: 22:22
 * To change this template use File | Settings | File Templates.
 */

import edu.uci.ics.jung.algorithms.util.KMeansClusterer;

import java.util.*;

public class TestKMeans {

    public static void main(String[] args) {

        Map<String, double[]> testmap = new HashMap<String, double[]>();
        double[] cc11 = {1.0, 1.0};
        testmap.put("11", cc11);

        double[] cc31 = {3.0, 1.0};
        testmap.put("31", cc31);


        double[] cc13 = {1.0, 1.0};
        testmap.put("13", cc13);

        double[] cctest = {1.1, 1.1};
        testmap.put("test", cctest);

        double[] cctest1 = {1.2, 1.1};
        testmap.put("test1", cctest1);

        double[] cctest2 = {1.1, 3.1};
        testmap.put("test2", cctest2);

        double[] cctest3 = {0.9, 2.9};
        testmap.put("test3", cctest3);

        double[] cctest4 = {2.9, 0.9};
        testmap.put("test4", cctest4);

        KMeansClusterer<String> kmc = new KMeansClusterer<String>();

        Collection<Map<String ,double[]>> cl =  kmc.cluster(testmap, 3);

        Iterator<Map<String, double[]>> clusters = cl.iterator();
        while (clusters.hasNext()) {
            Map<String, double[]> items = clusters.next();

            Iterator<String> it = items.keySet().iterator();

            while (it.hasNext()) {
                String name = it.next();
                System.out.println(name);

            }

            System.out.println();
        }

    }



}

package kmeans;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 30/11/2013
 * Time: 18:27
 * To change this template use File | Settings | File Templates.
 */

import edu.uci.ics.jung.algorithms.util.KMeansClusterer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//这里的输入是文件名对应的特征向量，文件名是[数字].txt（小写），比如43.txt
//               processor ,
public class KMProcessor {

    public static final int clusterCount = 5;
    public static final double convergeThreshold = 0.00000000000011;
    public static final int maxConvergeLoop = 100;
    public static final int minClusterSize = 3;

    public Collection<Map<String, double[]>> clusters;
    public ArrayList<Map<String, double[]>> clusterList;

    KMeansClusterer<String> kmc;
    Map<String, double[]> aritcleEigenVectorMap;

//  stupid java
    private double[] double2double(Double[] uD) {
        double[] lD = new double[uD.length];
        for(int i=0; i < uD.length; i++) {
            lD[i] = uD[i];
        }
        return lD;
    }

    private void fillMap(String eigenVectorPath) throws Exception {
        this.aritcleEigenVectorMap = new HashMap<String, double[]>();

        try {
            FileInputStream fis = new FileInputStream(new File(eigenVectorPath));
            BufferedReader  br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            String articleName = null;
            ArrayList<Double> eigenVector = new ArrayList<Double>();
            while ((line = br.readLine()) != null) {
                Pattern pattern = Pattern.compile("[.\\d]*.txt");
                Matcher matcher = pattern.matcher(line);

                if(matcher.find()) {
                    if(articleName != null) {
                        Double[] eigenArr = new Double[eigenVector.size()];
                        eigenArr = eigenVector.toArray(eigenArr);
                        aritcleEigenVectorMap.put(articleName, double2double(eigenArr));
                    }
                    articleName = line.trim();
                    eigenVector = new ArrayList<Double>();
                } else {
                    String[] ele = line.trim().split(" ");
                    eigenVector.add(Double.valueOf(ele[1]));
                }
            }

        } catch (Exception e) {
            throw new Exception("EigenVectorFileNotFound");
        }

    }

    public KMProcessor(String eigenVectorPath) throws Exception{
        this.kmc = new KMeansClusterer<String>();
        fillMap(eigenVectorPath);
    }

    public ArrayList<Map<String, double[]>> processKMeans() {
        this.clusters = kmc.cluster(this.aritcleEigenVectorMap, this.clusterCount);
        this.clusterList = new ArrayList<Map<String, double[]>>(this.clusters);
        return clusterList;
    }

    private  ArrayList<SimpleEntry<Integer, double[]>> getCentroidList() {
        if(clusters == null) {
            processKMeans();
        }

        ArrayList<SimpleEntry<Integer, double[]>> cenroidList =
                new ArrayList<SimpleEntry<Integer, double[]>>();

        for(int i = 0; i < clusterList.size(); i++) {
            double[] centroid = getCentroids(clusterList.get(i));

            SimpleEntry<Integer, double[]> newPair =
                    new SimpleEntry<Integer, double[]>(i, centroid);

            cenroidList.add(newPair);
        }

        return cenroidList;
    }

    public double[] getCentroids(Map<String, double[]> clusterMap) {
        Iterator<Map.Entry<String, double[]>> it = clusterMap.entrySet().iterator();
        int length = 1;
        double[] centroid = null;
        while (it.hasNext()) {
            Map.Entry<String, double[]> en = it.next();

            if(centroid == null) {
                length = en.getValue().length;
                centroid = new double[length];
            }

            for(int i = 0; i < length; i++) {
                centroid[i] += en.getValue()[i];
            }
        }

        for(int i = 0; i < length; i++) {
            centroid[i] /= clusterMap.size();
        }

        return centroid;
    }

    private double getDistance(double[] p1, double[] p2) {
        assert (p1.length == p2.length);

        double distance = 0.0;

        for(int i = 0; i < p1.length; i++) {
            distance += Math.pow(p1[i] - p2[i], 2);
        }

        return Math.pow(distance, 0.5);
    }

    private SimpleEntry<Double, int[]> findMinPair(ArrayList<SimpleEntry<Integer, double[]>> centroidList) {
        int[] pair = new int[2];
        double min = Double.POSITIVE_INFINITY;
//        todo consider this matrix
        for (int i = 0; i < centroidList.size(); i++) {
            for(int j = i+1; j < centroidList.size(); j++) if(i != j) {
                double[] pi = centroidList.get(i).getValue();
                double[] pj = centroidList.get(j).getValue();
                double distance = getDistance(pi, pj);
                if(distance< min) {
                    min =  distance;
                    pair[0] = i;
                    pair[1] = j;
                }
            }
        }
        return new SimpleEntry<Double, int[]>(min, pair);
    }

    private void mergeCluster(int i, int j) {
        if(i == j) {
            return;
        }

        try {
    //      merge mj into mi
            Map<String, double[]> mi = clusterList.get(i);
            Map<String, double[]> mj = clusterList.get(j);
            Iterator<Map.Entry<String, double[]>> it = mj.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, double[]> en = it.next();
                mi.put(en.getKey(), en.getValue());
            }

            this.clusterList.remove(j);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Map<String, double[]>> converge() {

        System.out.println("cluster converge started");
        int loop = 0;

        while (true) {
            ArrayList<SimpleEntry<Integer, double[]>> centroidList = getCentroidList();
            SimpleEntry<Double, int[]> distancePair = findMinPair(centroidList);
            double distance = distancePair.getKey();
            int[] pair = distancePair.getValue();

            if(this.clusterList.size() <= this.minClusterSize) {
                break;
            }

            if(distance > this.convergeThreshold) {
                mergeCluster(pair[0], pair[1]);
            } else {
                break;
            }

            loop++;
            if(loop >= this.maxConvergeLoop) {
                System.out.println("Hitting upper limit, converge exit prematurely");
                break;
            } else {
                System.out.println("converge loop: " + loop);
            }
        }

        return this.clusterList;
    }
}

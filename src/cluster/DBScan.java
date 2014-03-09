package cluster;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 12/4/13
 * Time: 12:14 AM
 * To change this template use File | Settings | File Templates.
 */

public class DBScan {
//  eps: the distance that defines the ε-neighborhood of a point
//    public static final double eps = 0.5;
    public static final double eps = 0.6;
//    public static final double eps = 0.7;

//  minPoints: the minimum number of density-connected points required to form a topic.cluster
    public static final int minPoints = 1;

//  the root folder for collect files into topic.cluster folders they belong

    public ArrayList<FileVectorPair> fileVectorPairList;

    public List<Cluster<FileVectorPair>> dbsResult;
    public ArrayList<Map<String, double[]>> dbsResultList;

    public DBSCANClusterer dbsc;

    public DBScan(String eigenVectorPath) throws Exception{
        this.dbsc = new DBSCANClusterer(eps, minPoints, new Distance());
        fillCluster(eigenVectorPath);

        System.out.println("DBScan file count: " + this.fileVectorPairList.size());
    }

    public ArrayList<Map<String, double[]>> processDBS() {
        this.dbsResult = dbsc.cluster(this.fileVectorPairList);
        this.dbsResultList = new ArrayList<Map<String, double[]>>();

        for(int i = 0; i < dbsResult.size(); i++) {
            Cluster<FileVectorPair> c = dbsResult.get(i);
            List<FileVectorPair> l = c.getPoints();

            Map<String, double[]> m = new HashMap<String, double[]>();

            for(int j = 0; j < l.size(); j++) {
                FileVectorPair fvp = l.get(j);
                m.put(fvp.fileName, fvp.getPoint());
            }

            dbsResultList.add(m);
        }

        return dbsResultList;
    }



    private void fillCluster(String eigenVectorPath) throws Exception {
        this.fileVectorPairList = new ArrayList<FileVectorPair>();

        try {
            FileInputStream fis = new FileInputStream(new File(eigenVectorPath));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            FileVectorPair fvp = null;
            while ((line = br.readLine()) != null) {
                Pattern pattern = Pattern.compile("[.\\d]*.txt");
                Matcher matcher = pattern.matcher(line);

                if(matcher.find()) {

                    String fileName = line.trim();
                    fvp = new FileVectorPair(fileName);

                    fileVectorPairList.add(fvp);
                } else {
//                  this line is the word value pair, we just need the value
                    String[] wordValuePair = line.trim().split(" ");
                    fvp.vector.add(Double.valueOf(wordValuePair[1]));
                }
            }

            for(int i = 0; i < fileVectorPairList.size(); i++) {
                fileVectorPairList.get(i).list2arr();
            }

        } catch (Exception e) {
            throw new Exception("EigenVectorFileNotFound");
        }

    }

    public class FileVectorPair implements Clusterable {

        public String fileName;
        public ArrayList<Double> vector;
        public double[] eigenVector;

        public FileVectorPair(String fileName, double[] eigenVector) {
            this.fileName = fileName;
            this.eigenVector = eigenVector;
        }

        public FileVectorPair(String fileName){
            this.fileName = fileName;
            this.vector = new ArrayList<Double>();
        }

        private double[] double2double(ArrayList<Double> uD) {
            double[] lD = new double[uD.size()];
            for(int i=0; i < uD.size(); i++) {
                lD[i] = uD.get(i);
            }
            return lD;
        }

        public void list2arr() {
            this.eigenVector = double2double(this.vector);
        }

        public double[] getPoint() {
            return eigenVector;
        }
    }

    public class Distance implements DistanceMeasure {
        public double compute(double[] a, double[] b) {
            assert (a.length == b.length);

            double distance = 0.0;

            for(int i = 0; i < a.length; i++) {
                distance += Math.pow(a[i] - b[i], 2);
            }

            return Math.pow(distance, 0.5);
        }
    }

    public static void main(String[] args) {


    }
}

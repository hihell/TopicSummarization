package cluster;
import edu.gatech.gtisc.jbirch.cftree.CFTree;
import edu.gatech.gtisc.jbirch.test.Test3;
import edu.gatech.gtisc.jbirch.test.Test2;
import edu.gatech.gtisc.jbirch.test.Test1;


import java.io.*;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 07/12/2013
 * Time: 01:05
 * To change this template use File | Settings | File Templates.
 */
public class BIRCH {

    public final int maxNodeEntries = 1000;
    // distance threshold (= sqrt(radius))
    public final double distThreshold = 2.5;
    // in MB
    public final int memoryLimit = 1000;
    // verify memory usage after every 10000 inserted instances
    public final int memoryLimitPeriodicCheck = 10000;

    public String datasetFile;

    public ArrayList<SimpleEntry<String, double[]>> fileEigenPairList;

    public ArrayList<Map<String, double[]>> clusterList;

    public BIRCH(String filePath) throws Exception{
        datasetFile = filePath;

        fillList();

        System.out.println("BIRCH file count: " + this.fileEigenPairList.size());
    }

    private double[] double2double(Double[] uD) {
        double[] lD = new double[uD.length];
        for(int i=0; i < uD.length; i++) {
            lD[i] = uD[i];
        }
        return lD;
    }

    public void fillList() throws Exception{
        fileEigenPairList = new ArrayList<SimpleEntry<String, double[]>>();
        ArrayList<SimpleEntry<String, ArrayList<Double>>> fileEigenPairListRaw =
            new ArrayList<SimpleEntry<String, ArrayList<Double>>>();
        try {
            FileInputStream fis = new FileInputStream(new File(this.datasetFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            String articleName = null;
            ArrayList<Double> eigenVector = null;
            while ((line = br.readLine()) != null) {
                Pattern pattern = Pattern.compile("[.\\d]*.txt");
                Matcher matcher = pattern.matcher(line);

                if(matcher.find()) {
                    articleName = line.trim();
                    eigenVector = new ArrayList<Double>();
                    fileEigenPairListRaw.add(new SimpleEntry<String, ArrayList<Double>>(
                            articleName,
                            eigenVector
                    ));
                } else {
//                  this line is the word value pair, we just need the value
                    String[] wordValuePair = line.trim().split(" ");
                    eigenVector.add(Double.valueOf(wordValuePair[1]));
                }
            }

            for(int i = 0; i < fileEigenPairListRaw.size(); i++) {
                String fileName = fileEigenPairListRaw.get(i).getKey();
                ArrayList<Double> eigenList = fileEigenPairListRaw.get(i).getValue();

                Double[] tmp = eigenList.toArray(new Double[eigenList.size()]);
                double[] eigenArr = double2double(tmp);

                fileEigenPairList.add(new SimpleEntry<String, double[]>(fileName, eigenArr));
            }

        } catch (Exception e) {
            throw new Exception("EigenVectorFileNotFound");
        }
    }

    public ArrayList<Map<String, double[]>> processBIRCH() throws Exception{

        int distFunction = CFTree.D0_DIST;
        boolean applyMergingRefinement = true;

        // This initializes the tree
        CFTree birchTree = new CFTree(maxNodeEntries,distThreshold,distFunction,applyMergingRefinement);

        // comment the following three lines, if you do not want auto rebuild based on memory usage constraints
        // if auto-rebuild is not active, you need to set distThreshold by hand
        birchTree.setAutomaticRebuild(true);
        birchTree.setMemoryLimitMB(memoryLimit);
        birchTree.setPeriodicMemLimitCheck(memoryLimitPeriodicCheck); // verify memory usage after every memoryLimitPeriodicCheck

        for(int i = 0; i < fileEigenPairList.size(); i++) {
            double[] eigenArr = fileEigenPairList.get(i).getValue();
            boolean inserted = birchTree.insertEntry(eigenArr);

            if(!inserted) {
                System.err.println("ERROR: NOT INSERTED!");
                System.exit(1);
            }
        }
        birchTree.finishedInsertingData();

        this.clusterList = new ArrayList<Map<String, double[]>>();
        // get the results
        ArrayList<ArrayList<Integer>> subclusters = birchTree.getSubclusterMembers();

        // print the index of instances in each subcluster
        for(ArrayList<Integer> subcluster : subclusters) {
            HashMap<String, double[]> c = new HashMap<String, double[]>();
            for(int i = 0; i < subcluster.size(); i++) {
                int fileIndex = subcluster.get(i) - 1;

                String articleName = this.fileEigenPairList.get(fileIndex).getKey();
                double[] eigenArr = this.fileEigenPairList.get(fileIndex).getValue();
                c.put(articleName, eigenArr);
            }
            this.clusterList.add(c);
        }
        return this.clusterList;
    }

}
package controller;

import cluster.LDA.src.jgibblda.LDA;
import cluster.BIRCH;
import cutter.Cutter;
import cluster.DBScan;
import filter.Filter;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import keywords.KeyWord;
import cluster.KMProcessor;
import statistics.Statistics;
import tfidf.TFIDF;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import converger.Converger;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 15:09
 * To change this template use File | Settings | File Templates.
 */
public class Controller {

    public final String LDAInputPath = "./LDAFiles/trndoc.txt";
    public final String LDAResultPath = "./LDAFiles/model-final.twords";

    public final String articlePath = "/Users/jiusi/Desktop/gmm";

    public final String topicFileTablePath = "./topicFileTable.txt";

    public final String topKResultPath = "./topK.txt";

    public final String eigenVectorPath = "./eigenV.txt";

    public final String kMeansDir = "./kMeans";
    public final String kMeansResult = "./kMeans.txt";

    public final String kMeansConvergeDir = "./kMeansConverge";
    public final String kMeansConvergeResult = "./kMeansConvergeResult.txt";

    public final String dbScanDir = "./dbScan";
    public final String dbScanResult = "./dbScan.txt";

    public final String dbScanConvergeDir = "./dbScanConverge";
    public final String dbScanConvergeResult = "./dbScanConvergeResult.txt";
        
    public final String birchDir = "./birch";
    public final String birchResult = "./birch.txt";

    public final String birchConvergeDir = "./birchConverge";
    public final String birchConvergeResult = "./birchConvergeResult.txt";
    
    
    public Hashtable<String, Hashtable<String, Integer>> readCutResult(String path) {
        Hashtable<String, Hashtable<String, Integer>> fileWordTable = new Hashtable<String, Hashtable<String, Integer>>();

        InputStream fis;
        BufferedReader br;
        try {
            fis = new FileInputStream(new File(path));
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {
                Hashtable<String, Integer> wordTable = new Hashtable<String, Integer>();
                fileWordTable.put(line, wordTable);

                line = br.readLine();
                String words[] = line.split(" ");

                for(String w : words) {

                    if(wordTable.containsKey(w)) {
                        Integer count = wordTable.get(w) + 1;
                        wordTable.put(w, count);
                    } else {
                        wordTable.put(w, 1);
                    }
                }
            }

            return fileWordTable;
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

    public void writeLDAReadable(Hashtable<String, Hashtable<String, Integer>> fileWordTable) throws IOException {

    PrintWriter writer = new PrintWriter(this.LDAInputPath, "UTF-8");

int num = fileWordTable.keySet().size();
        writer.println(num);

        Enumeration<String> fileNames = fileWordTable.keys();
        while(fileNames.hasMoreElements()) {
            String fileName = fileNames.nextElement();

            Hashtable<String, Integer> wordTable = fileWordTable.get(fileName);
            Enumeration<String> words = wordTable.keys();
            while(words.hasMoreElements()) {
                String word = words.nextElement();
                for(int i=0; i < wordTable.get(word); i++) {
                    writer.print(word + " ");
                }
            }
            writer.println();
        }

        writer.close();

    }

    public ArrayList<Hashtable<String, Integer>> removeEmptySentence(ArrayList<Hashtable<String, Integer>> sList) {
        ArrayList<Hashtable<String, Integer>> newSList = new ArrayList<Hashtable<String, Integer>>();
        for (Hashtable<String, Integer> s: sList) {
            if(s.size() != 0) {
                newSList.add(s);
            }
        }
        System.out.println("removed:" + (sList.size() - newSList.size()));
        return newSList;
    }

    private void topK2file(Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicKeyWordTable ) throws IOException{
        PrintWriter writer = new PrintWriter(this.topKResultPath, "UTF-8");

        Enumeration<String> topics = topicKeyWordTable.keys();
        while (topics.hasMoreElements()) {
            String topic = topics.nextElement();
            writer.println(topic);
            ArrayList<SimpleEntry<String, Double>> keywords = topicKeyWordTable.get(topic);

            for(SimpleEntry<String, Double> keyword : keywords) {
                writer.println("    " + keyword.getKey() + " " + keyword.getValue());
            }
        }
        writer.close();
    }

    private Hashtable<String, ArrayList<SimpleEntry<String, Double>>> file2topK() throws IOException{
        File topkFile = new File(this.topKResultPath);

        InputStream fis;
        BufferedReader br;

        fis = new FileInputStream(topkFile);
        br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicKeyWordTable = new Hashtable<String, ArrayList<SimpleEntry<String, Double>>>();

        String line;
        ArrayList<SimpleEntry<String, Double>> topK = new ArrayList<SimpleEntry<String, Double>>();
        while((line = br.readLine()) != null) {

            Pattern pattern = Pattern.compile("Topic\\s[.\\d]*th:");
            Matcher matcher = pattern.matcher(line);

            if(matcher.find()) {
                topK = new ArrayList<SimpleEntry<String, Double>>();
                topicKeyWordTable.put(line, topK);
            } else {
                String[] ele = line.split(" ");
                int i = 0;
                for(; i < ele.length; i++) {
                    if (ele[i].length() > 0) {
                        break;
                    }
                }
                System.out.println(line);
                String word = line.split(" ")[i].trim();
                Double prob = Double.valueOf(line.split(" ")[i+1]);
                topK.add(new SimpleEntry(word, prob));
            }
        }

        return topicKeyWordTable;
    }

    private void topicFileTable2file( Hashtable<String, ArrayList<String>> topicFileTable) throws IOException{
        PrintWriter writer = new PrintWriter(this.topicFileTablePath, "UTF-8");

        Enumeration<String> topics = topicFileTable.keys();
        while (topics.hasMoreElements()) {
            String topic = topics.nextElement();
            writer.println(topic);
            ArrayList<String> files = topicFileTable.get(topic);

            for(String fileName : files) {
                writer.println("    " + fileName);
            }
        }
        writer.close();
    }


    private void eigenVector2file(Hashtable<String, ArrayList<SimpleEntry<String, Double>>>
                                          fileEigenvectorTable) throws IOException{
        PrintWriter writer = new PrintWriter(this.eigenVectorPath, "UTF-8");

        Enumeration<String> files = fileEigenvectorTable.keys();
        while (files.hasMoreElements()) {
            String file = files.nextElement();
            writer.println(file);
            ArrayList<SimpleEntry<String, Double>> eigenVector = fileEigenvectorTable.get(file);

            for(SimpleEntry<String, Double> p : eigenVector) {
                writer.println("    " + p.getKey() + " " + p.getValue());
            }
        }
        writer.close();
    }

    private void clusterResult2file(ArrayList<Map<String, double[]>> clusterList, String path, long timeCost) throws IOException{
    	PrintWriter writer;
    	writer = new PrintWriter(path, "UTF-8");
        
    	writer.println("used:" + timeCost + " ms");
    	
        for(int i = 0; i < clusterList.size(); i++) {
            writer.println(i + ":");
            Iterator<Map.Entry<String, double[]>> it = clusterList.get(i).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, double[]> en = it.next();
                writer.println("    " + en.getKey());

                String strD = new String();
                for(int d = 0; d < en.getValue().length; d++) {
                    strD += (en.getValue()[d] + " ");
                }

                writer.println("        " + strD);
            }
        }
        writer.close();
    }

    private void cpFilesByClusterResult(ArrayList<Map<String, double[]>> resultList, String dir) throws IOException {
    	
    	assert( dir.equals(this.kMeansConvergeDir) || dir.equals(this.kMeansDir) ||
                dir.equals(this.dbScanConvergeDir) || dir.equals(this.dbScanDir) ||
                dir.equals(this.birchConvergeDir) || dir.equals(this.birchDir));
    	
    	File folder = new File(dir);
    	if(folder.isDirectory()) {
    		FileUtils.cleanDirectory(folder); 
    	} else {
    		folder.mkdir();
    	}   
    	
    	for(int i = 0; i < resultList.size(); i++) {
    		Map<String, double[]> cluster = resultList.get(i);
    		File cFolder = new File(dir + "/" + i);
    		if(cFolder.isDirectory()) {
    			FileUtils.cleanDirectory(cFolder);
    		} else {
    			cFolder.mkdir();
    		}
    		
    		Iterator<Map.Entry<String, double[]>> it = cluster.entrySet().iterator();
    		while(it.hasNext()) {
    			Map.Entry<String, double[]> en = it.next();
    			File article = new File(this.articlePath + "/" + en.getKey());
	    		FileUtils.copyFileToDirectory(article, cFolder);    		    		    		    	
    		}	
		}    	
    }


    private void stage1(Controller controller, Cutter cutter, Filter filter,
                        Hashtable<String, Hashtable<String, Integer>> fileWordTable) throws IOException {

        System.out.println("Stage1 started!");

        LDA.main(null);

        Hashtable<String, ArrayList<String>> topicFileTable = new Statistics(controller.LDAResultPath)
                .getTopicFileTable(fileWordTable);
//              cluster.LDA and statistics result to file
        controller.topicFileTable2file(topicFileTable);

        Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicKeyWordTable =
                new Hashtable<String, ArrayList<SimpleEntry<String, Double>>>();

        Enumeration<String> topics = topicFileTable.keys();
        while (topics.hasMoreElements()) {
            String topic = topics.nextElement();

//                fill sentence list and wordTable
            ArrayList<Hashtable<String, Integer>> sList = new ArrayList<Hashtable<String, Integer>>();
            Hashtable<String, Integer> wordTable = new Hashtable<String, Integer>();
            cutter.cutTopicIntoLists(topicFileTable.get(topic), sList, wordTable);

//                filter stop words
            filter.filterSListStopWords(sList);
            filter.filterWordTableStopWords(wordTable);
//                filter low and high words
            filter.filterSListByThreshold(sList);
            filter.filterWTableByThreshold(wordTable);

//                after cut and filter, some sentence is empty, remove them
            sList = controller.removeEmptySentence(sList);

//                calculate keyword based on topic
            KeyWord keyWord = new KeyWord(sList, wordTable);
            ArrayList<SimpleEntry<String, Double>> topK = keyWord.calculateKeyWord();

//                top K keywords result to file
            topicKeyWordTable.put(topic, topK);
        }

        System.out.println(topicKeyWordTable.size());

        controller.topK2file(topicKeyWordTable);
    }

    private void stage2(Controller controller,
                        Hashtable<String, Hashtable<String, Integer>> fileWordTable) throws IOException{
        System.out.println("Stage2 started!");

        Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicKeyWordTable = controller.file2topK();

        TFIDF tfidf = new TFIDF(topicKeyWordTable, fileWordTable);

        Hashtable<String, ArrayList<SimpleEntry<String, Double>>>
                fileEigenvectorTable = tfidf.processEigenvector();

        controller.eigenVector2file(fileEigenvectorTable);
    }

    private void stage3KMeans() throws Exception{
        KMProcessor kmp = new KMProcessor(this.eigenVectorPath);
        
        long start = System.nanoTime();
        kmp.processKMeans();
        long end = System.nanoTime();
        
        long used = TimeUnit.NANOSECONDS.toMillis(end - start);
        clusterResult2file(kmp.clusterList, this.kMeansResult, used);
        cpFilesByClusterResult(kmp.clusterList, this.kMeansDir);
        
//        converge previous result
        Converger converger = new Converger(kmp.clusterList);
        start = System.nanoTime();                
        converger.converge();       
        end = System.nanoTime();
        
        used = TimeUnit.NANOSECONDS.toMillis(end - start);
        clusterResult2file(kmp.clusterList, this.kMeansConvergeResult, used);
        cpFilesByClusterResult(kmp.clusterList, this.kMeansConvergeDir);

    }

    public void stage4DBScan() throws Exception {
        DBScan dbs = new DBScan(this.eigenVectorPath);

        long start = System.nanoTime();
        ArrayList<Map<String, double[]>> list = dbs.processDBS();
        long end = System.nanoTime();
        
        long used = TimeUnit.NANOSECONDS.toMillis(end - start);
        
        clusterResult2file(list, this.dbScanResult, used);        
        cpFilesByClusterResult(list, this.dbScanDir);

//        converge previous result
        Converger converger = new Converger(list);
        start = System.nanoTime();        
        converger.converge();
        end = System.nanoTime();
        
        used = TimeUnit.NANOSECONDS.toMillis(end - start);
        
        clusterResult2file(list, this.dbScanConvergeResult, used);
        cpFilesByClusterResult(list, this.dbScanConvergeDir);
        
        System.out.println("dbs fin");
    }

    public void stage5BIRCH() throws Exception {
        BIRCH birch = new BIRCH(this.eigenVectorPath);
        
        long start = System.nanoTime();
        ArrayList<Map<String, double[]>> list = birch.processBIRCH();
        long end = System.nanoTime();
        
        long used = TimeUnit.NANOSECONDS.toMillis(end - start);
        clusterResult2file(list, this.birchResult, used);

        cpFilesByClusterResult(list, this.birchDir);
        
        Converger converger = new Converger(list);
        start = System.nanoTime();
        converger.converge();
        end = System.nanoTime();
        used = TimeUnit.NANOSECONDS.toMillis(end - start);
        clusterResult2file(list, this.birchConvergeResult, used);

        cpFilesByClusterResult(list, this.birchConvergeDir);
        
        System.out.println("birch fin");
    }

    public static void main(String args[]) {

        try {
            Controller controller = new Controller();
            Cutter cutter = new Cutter(controller.articlePath);
            Filter filter = new Filter();

            cutter.cutIntoOneFile();

            Hashtable<String, Hashtable<String, Integer>> fileWordTable = controller.readCutResult(cutter.outPath);

            filter.filterStopWords(fileWordTable);

            filter.fillThresholdWords(fileWordTable);

            filter.filterFileWordTableByThreshold(fileWordTable);

            controller.writeLDAReadable(fileWordTable);

            controller.stage1(controller, cutter, filter, fileWordTable);

            controller.stage2(controller, fileWordTable);

            controller.stage3KMeans();

            controller.stage4DBScan();

            controller.stage5BIRCH();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

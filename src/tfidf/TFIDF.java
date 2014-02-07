package tfidf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.AbstractMap.SimpleEntry;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 11/28/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class TFIDF {

    public HashSet<String> keywordSet;
    public ArrayList<String> keywordList;

    public Hashtable<String, ArrayList<SimpleEntry<String, Double>>> fileEigenvectorTable;
    Hashtable<String, Hashtable<String, Integer>> fileWordTable;

    public TFIDF(Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicKeyWordTable,
                 Hashtable<String, Hashtable<String, Integer>> fileWordTable) {

        this.fileWordTable = fileWordTable;
        keywordSet = new HashSet<String>();

        Enumeration<String> topics = topicKeyWordTable.keys();
        while(topics.hasMoreElements()) {
            String topic = topics.nextElement();

            ArrayList<SimpleEntry<String, Double>> words = topicKeyWordTable.get(topic);
            for(SimpleEntry<String, Double> word : words) {
                keywordSet.add(word.getKey());
            }
        }

        keywordList = new ArrayList<String>(keywordSet);
    }

    public Double TF(int i, String fileName, int total) {
        String keyword = keywordList.get(i);
        int appearTime = 0;

        if(fileWordTable.get(fileName).containsKey(keyword)) {
            appearTime = fileWordTable.get(fileName).get(keyword);
        }

        return (double) appearTime / total;
    }

    public Double IDF(int i) {
        int fileCount = fileWordTable.size();
        String keyword = keywordList.get(i);
        int appearTime = 0;

        Enumeration<String> files = fileWordTable.keys();
        while (files.hasMoreElements()) {
            String file = files.nextElement();

            if(fileWordTable.get(file).containsKey(keyword)) {
                appearTime ++;
            }
        }

        if(appearTime == 0) {
            return 0.0;
        } else {
            return Math.log((double) fileCount / appearTime);
        }
    }

    private int wordCount(Hashtable<String , Integer> wordTable) {
        int count = 0;

        for(String keyword : keywordList) {
            if(wordTable.containsKey(keyword)) {
                count += wordTable.get(keyword);
            }
        }
        return count;
    }

    public Hashtable<String, ArrayList<SimpleEntry<String, Double>>> processEigenvector() {
        fileEigenvectorTable = new Hashtable<String, ArrayList<SimpleEntry<String, Double>>>();

        Enumeration<String> files = this.fileWordTable.keys();
        while (files.hasMoreElements()) {
            String file = files.nextElement();
            Hashtable<String, Integer> wordTable = fileWordTable.get(file);
            int t = wordCount(wordTable);

            ArrayList<SimpleEntry<String, Double>> eigenVector = new ArrayList<SimpleEntry<String, Double>>();
            for(int i = 0; i < keywordList.size(); i++){
                Double tf_idf = TF(i, file, t) * IDF(i);
                SimpleEntry<String, Double> p = new SimpleEntry<String, Double>(keywordList.get(i), tf_idf);
                eigenVector.add(p);
            }
            fileEigenvectorTable.put(file, eigenVector);
        }

        return fileEigenvectorTable;
    }


    public static void main(String args[]) {
        HashSet<String> s = new HashSet<String>();

        String a = "abc";
        String b = "abcd";

        s.add(a);
        s.add(b);
        System.out.println(s.size());


        ArrayList<String> li = new ArrayList<String>(s);

        System.out.println(li.get(0));

        Integer x = 1;
        Integer y = 2;

        double ra = (double)x / y;
        Double raa = ra;
        System.out.println(raa);

    }
}

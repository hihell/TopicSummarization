package tfidf;

import java.lang.reflect.Array;
import java.util.*;
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

    public TFIDF() {

    }

    public Double TF(int i, String fileName, int total) {
        String keyword = keywordList.get(i);
        int appearTime = 0;

        if(fileWordTable.get(fileName).containsKey(keyword)) {
            appearTime = fileWordTable.get(fileName).get(keyword);
        }

        return (double) appearTime / total;
    }

    public static Double TF(String word, HashMap<String, Integer> snippet) {

        int total = 0;
        for(Map.Entry<String, Integer> pair : snippet.entrySet()) {
            total += pair.getValue();
        }

        if(snippet.containsKey(word)) {
            return (double) snippet.get(word) / total;
        } else {
            return 0.0;
        }
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

    public static Double IDF(String word, ArrayList<HashMap<String, Integer>> snippets) {

        int appearTime = 0;
        int snippetCount = snippets.size();

        for(HashMap<String, Integer> snippet : snippets) {
            if (snippet.containsKey(word)) {
                appearTime ++;
            }
        }

        if(appearTime == 0) {
            return 0.0;
        } else {
            return Math.log((double) snippetCount / appearTime);
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
        HashMap<String, Integer> snippet = new HashMap<String, Integer>();
        snippet.put("我们", 2);
        snippet.put("今天", 1);
        snippet.put("很开心", 1);

        HashMap<String, Integer> snippet1 = new HashMap<String, Integer>();
        snippet1.put("他们", 2);
        snippet1.put("昨天", 1);
        snippet1.put("很开心", 1);

        ArrayList<HashMap<String, Integer>> sList = new ArrayList<HashMap<String, Integer>>();
        sList.add(snippet);
        sList.add(snippet1);

        TFIDF tfidf = new TFIDF();
//        System.out.println(tfidf.TF("很开心", snippet));
//        System.out.println(tfidf.TF("不爽", snippet));

        System.out.println(tfidf.IDF("很开心", sList));
        System.out.println(tfidf.IDF("他们", sList));

    }
}

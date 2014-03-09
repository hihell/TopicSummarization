package filter;

import com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators;
import fx.sunjoy.SmallSeg;
import snippet.Snippet;
import snippet.SnippetGenerator;
import tfidf.TFIDF;

import javax.annotation.processing.SupportedSourceVersion;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class Filter {

    public List<String> stopWords;

    public final String stopWordPath = "./stopcn.txt";
    public final Integer freqency_threshold_low = 1;
    public final Integer freqency_threshold_high = 500;
    public ArrayList<String> thresholdWords;

    public static final int smallSnippetSize = 10;

    public Filter() {
        stopWords = new ArrayList<String>();

        InputStream fis;
        BufferedReader br;
        try {
            fis = new FileInputStream(new File(this.stopWordPath));
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.add(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void filterStopWords(Hashtable<String, Hashtable<String, Integer>> fileWordTable) {
        Enumeration<String> fileNames = fileWordTable.keys();

        while(fileNames.hasMoreElements()) {
            String fileName = fileNames.nextElement();
            Hashtable<String, Integer> wordTable = fileWordTable.get(fileName);
            for(String stop : this.stopWords) {
                if(wordTable.containsKey(stop)) {
                    wordTable.remove(stop);
                }
            }
        }
    }

    public void filterSListStopWords(ArrayList<Hashtable<String, Integer>> sList){
        for(Hashtable<String, Integer> s: sList) {
            filterSentenceStopWords(s);
        }
    }

    private void filterSentenceStopWords(Hashtable<String, Integer> sentence) {
        for(String stop : this.stopWords) {
            if(sentence.containsKey(stop)){
                sentence.remove(stop);
            }
        }
    }

    public void filterWordTableStopWords(Hashtable<String, Integer> wordTable) {
        for(String stop: this.stopWords) {
            if(wordTable.containsKey(stop)) {
                wordTable.remove(stop);
            }
        }
    }

    public void fillThresholdWords(Hashtable<String, Hashtable<String, Integer>> fileWordTable) {
        Enumeration<String> fileNames = fileWordTable.keys();
        while(fileNames.hasMoreElements()) {
            String fileName = fileNames.nextElement();
            Hashtable<String, Integer> wordTable = fileWordTable.get(fileName);

            Enumeration<String> words = wordTable.keys();
            this.thresholdWords = new ArrayList<String>();
            while(words.hasMoreElements()) {
                String word = words.nextElement();
                if (wordTable.get(word) >= this.freqency_threshold_high ||
                        wordTable.get(word) <= this.freqency_threshold_low) {
                    thresholdWords.add(word);
                }
            }
        }
    }

    public void fillThresholdWords(HashMap<String, Integer> wordTable) {
        this.thresholdWords = new ArrayList<String>();
        for(Map.Entry<String, Integer> word : wordTable.entrySet()) {
            if(word.getValue() <= this.freqency_threshold_low ||
                    word.getValue() >= this.freqency_threshold_high) {
                thresholdWords.add(word.getKey());
            }
        }
    }


    public void filterFileWordTableByThreshold(Hashtable<String, Hashtable<String, Integer>> fileWordTable) {
        Enumeration<String> fileNames = fileWordTable.keys();
        while(fileNames.hasMoreElements()) {
            String fileName = fileNames.nextElement();
            Hashtable<String, Integer> wordTable = fileWordTable.get(fileName);

            for(String del : thresholdWords) {
                wordTable.remove(del);
            }
        }
    }

    public void filterSListByThreshold(ArrayList<Hashtable<String, Integer>> sList) {

        assert (this.thresholdWords != null);

        if(thresholdWords.size() == 0) {
            System.err.println("没有高于或者低于阈值的词汇，可能有问题");
        }

        for(Hashtable sentence: sList) {
            for(String del : thresholdWords) {
                if(sentence.containsKey(del)) {
                    sentence.remove(del);
                }
            }
        }
    }

    public void filterWTableByThreshold(Hashtable<String, Integer> wordTable) {
        assert (this.thresholdWords != null);


        if(thresholdWords.size() == 0) {
            System.err.println("没有高于或者低于阈值的词汇，可能有问题");
        }

        for(String del : thresholdWords) {
            if(wordTable.containsKey(del)) {
                wordTable.remove(del);
            }
        }
    }

    public void filterWordTableByThreshold(HashMap<String, Integer> wordTable) {
        if(this.thresholdWords == null) {
            fillThresholdWords(wordTable);
        }

        if(thresholdWords.size() == 0) {
            System.err.println("没有高于或者低于阈值的词汇，可能有问题");
        }

        for(String del : thresholdWords) {
            if(wordTable.containsKey(del)) {
                wordTable.remove(del);
            }
        }

        System.out.println("Filter.filterWordTableByThreshold wordTable.size:" + wordTable.size());
    }

    public static ArrayList<Snippet> filterSmallSnippet(ArrayList<Snippet> snippets) {
        ArrayList<HashMap<String, Integer>> snippetList = Snippet.getHashMapSnippets(snippets);
        ArrayList<String> snippetTextList = Snippet.getOriginalSnippets(snippets);

        ArrayList<Integer> smallList = SnippetGenerator.findSmallSnippet(snippetList, smallSnippetSize);

        ArrayList<HashMap<String, Integer>> filteredSnippetList = new ArrayList<HashMap<String, Integer>>();
        ArrayList<String> filteredSentenceList = new ArrayList<String>();
        ArrayList<Snippet> filteredSnippets = new ArrayList<Snippet>();


        for(int i = 0; i < snippetList.size(); i++) if(!smallList.contains(i)) {
            filteredSnippetList.add(snippetList.get(i));
            filteredSentenceList.add(snippetTextList.get(i));
            filteredSnippets.add(snippets.get(i));
        }

        return filteredSnippets;
    }

    public static ArrayList<Snippet> filterLowRankSnippet(ArrayList<Snippet> snippets, int afterSize) {

        if(afterSize >= snippets.size()) {
            return snippets;
        }

        ArrayList<Double> tfidfList = new ArrayList<Double>();
        ArrayList<HashMap<String, Integer>> snippetList = Snippet.getHashMapSnippets(snippets);
        for(HashMap<String, Integer> snippet : snippetList) {

            double sum = 0.0;
            for(Map.Entry<String, Integer> word : snippet.entrySet()) {
                double tf = TFIDF.TF(word.getKey(), snippet);
                double idf = TFIDF.IDF(word.getKey(), snippetList);

                sum += (tf * idf);
            }

            tfidfList.add(sum);
        }

        ArrayList<Double> sortList = new ArrayList<Double>(tfidfList);
        Collections.sort(sortList);
        Collections.reverse(sortList);

        double threshold = sortList.get(afterSize - 1);

        ArrayList<Snippet> afterList = new ArrayList<Snippet>();
        for(int i = 0; i < snippets.size(); i++) {
            if(tfidfList.get(i) >= threshold) {
                afterList.add(snippets.get(i));
            }
        }

        assert (afterList.size() == afterSize);
        return afterList;
    }

    public static void main(String args[]) {
//        Filter f = new Filter();
//
//        Hashtable<String, Integer> words = new Hashtable<String, Integer>();
//        words.put("哎呀", 10);
//        words.put("知乎", 1);
//
////        f.filterStopWords(wordList);
//
//        System.out.println(words.size());
//        System.out.println("stop wordList read from file");


        ArrayList<Double> ll = new ArrayList<Double>();
        ll.add(1.1);
        ll.add(1.0);
        ll.add(1.2);

        System.out.println(ll);
        Collections.sort(ll);
        Collections.reverse(ll);

        System.out.println(ll);

    }

}

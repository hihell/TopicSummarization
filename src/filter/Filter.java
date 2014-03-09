package filter;

import fx.sunjoy.SmallSeg;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

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
    public final Integer freqency_threshold_high = 100;
    public ArrayList<String> thresholdWords;

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



    public void removeEmptyFiles(String dir) {
        File files = new File(dir);
        if(files.isDirectory()) {

            for(File file :files.listFiles()){
                if(file.length() == 0) {
                    file.delete();
                }
            }
        }

    }

    public static void main(String args[]) {

    }



}

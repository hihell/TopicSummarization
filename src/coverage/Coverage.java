package coverage;


import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import snippet.Snippet;
import sun.java2d.pipe.ShapeSpanIterator;
import tfidf.TFIDF;
import filter.Filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiusi on 2/10/14.
 */
public class Coverage {

//  Snippet Word Coverage (sparse) Matrix
    public DoubleMatrix2D SWCM = null;

//  Snippet Coverage List
    public ArrayList<Double> SCL;

    public ArrayList<HashMap<String, Integer>> snippetList;

    public ArrayList<String> wordList;
    public int wordCount;
    public HashMap<String, Integer> wordTable;

    public int maxCoverageIndex;

    private final Double verySmallValue = 0.000001;

    public Coverage(ArrayList<HashMap<String, Integer>> snippetList) {
        this.snippetList = snippetList;
        this.SCL = new ArrayList<Double>();

        wordTable = Snippet.extractWordsFromSnippetList(snippetList);
        (new Filter()).filterWordTableByThreshold(wordTable);
        wordList = new ArrayList<String>(wordTable.keySet());

        System.out.println("Coverage.Coverage wordTable.size:" + wordTable.size());

        fillCoverageMatrix();

        this.wordCount = getWordCount();

        maxCoverageIndex = getMax();

        normalizeSWCM();
    }



    private void fillCoverageMatrix() {
        this.SWCM = DoubleFactory2D.sparse.make(snippetList.size(), wordList.size());

        for(int i = 0; i < snippetList.size(); i++) {

            if(i % 100 == 0) {
                System.out.println("filling matrix:" + i + " , total:" + snippetList.size());
            }

            for (int j = 0; j < wordList.size(); j++) {
                Double coverage = snippetWordCoverage(snippetList.get(i), snippetList, wordList.get(j));
                SWCM.set(i, j, coverage);
            }
        }
    }

    public int getMax() {
        int max = 0;

        for(int i = 0; i < snippetList.size(); i++) {
            double coverage = 0.0;
            for(int j = 0; j < wordList.size(); j++) {
                coverage += SWCM.get(i, j);
            }
            SCL.add(coverage);

            if(coverage > SCL.get(max)) {
                max = i;
            }
        }
        return max;
    }

    public Double snippetsWordsCoverage(ArrayList<Integer> snippetIndexList) {

        Double coverage = 0.0;
        for(int j = 0; j < wordList.size(); j++) {
            String word = wordList.get(j);
            double lamda = (double) wordTable.get(word) / wordCount;

            coverage += lamda * snippetsWordCoverage(snippetIndexList, j);
        }

        return coverage;
    }

    public Double snippetsWordCoverage(ArrayList<Integer> snippetIndexList, Integer wordIndex) {
        assert (SWCM != null);
        double irrelevance = 1;
        for(Integer snippetIndex : snippetIndexList) {
            irrelevance *= (1 - snippetWordCoverage(snippetIndex, wordIndex));
        }

        return 1 - irrelevance;
    }

    public Double snippetWordCoverage(HashMap<String, Integer> snippet,
                                      ArrayList<HashMap<String, Integer>> snippets,
                                      String word) {
        Double tf = TFIDF.TF(word, snippet);
        Double idf = TFIDF.IDF(word, snippets);

        assert (!tf.isNaN());
        assert (!idf.isNaN());

        return tf * idf;
    }

    public Double snippetWordCoverage(int snippetIndex, int wordIndex) {
        return SWCM.get(snippetIndex, wordIndex);
    }

    private void normalizeSWCM() {
        for(int i = 0; i < snippetList.size(); i++) {
            double count = 0.0;
            for(int j = 0; j < wordList.size(); j++) {
                count += SWCM.get(i, j);
            }

            for(int j = 0; j < wordList.size(); j++) {
                double nval = SWCM.get(i, j) / count;
                SWCM.set(i, j, nval);
            }
        }
    }

    private void testNormalization(ArrayList<Double> vector) {
        Double accu = 0.0;
        for(Double d : vector) {
            accu += d;
        }

        assert (1 - accu < verySmallValue);
        assert (accu < 1);
    }


    private void testNormalization(DoubleMatrix2D matrx, int index) {
        Double accu = 0.0;

        DoubleMatrix1D row = matrx.viewRow(index);

        for(int i = 0; i < row.size(); i++) {
            accu += row.get(i);
        }

        if(Math.abs(1.0 - accu) > verySmallValue) {
            System.out.println("accu" + accu);
        }
    }

    private int getWordCount() {
        int wordCount = 0;
        for(String word : wordList) {
            wordCount += wordTable.get(word);
        }

        return wordCount;
    }
}

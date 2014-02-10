package coverage;


import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import tfidf.TFIDF;

import java.net.Inet4Address;
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

    public ArrayList<HashMap<String, Integer>> snippets;
//    public ArrayList<String> sentences;
    public ArrayList<String> words;
    public HashMap<String, Integer> wordTable;

    public int maxCoverageIndex;

    public Coverage(ArrayList<HashMap<String, Integer>> snippets) {
        this.snippets = snippets;
        this.SCL = new ArrayList<Double>();

        fillWords();
        fillCoverageMatrix();

        maxCoverageIndex = getMax();

        normalizeSWCM();
    }

    private void fillWords() {
        wordTable = new HashMap<String, Integer>();
        for(HashMap<String, Integer> snippet : snippets) {
            for(Map.Entry<String, Integer> pair : snippet.entrySet()) {
                String word = pair.getKey();
                Integer count = pair.getValue();

                if(wordTable.containsKey(word)) {
                    Integer oldValue = wordTable.get(word);
                    wordTable.put(word, oldValue + count);
                } else {
                    wordTable.put(word, count);
                }
            }
        }

        words = new ArrayList<String>(wordTable.keySet());
    }

    private void fillCoverageMatrix() {
        this.SWCM = DoubleFactory2D.sparse.make(snippets.size(), words.size());

        for(int i = 0; i < snippets.size(); i++) {
            for (int j = 0; j < words.size(); j++) {
                Double coverage = snippetWordCoverage(snippets.get(i), snippets, words.get(j));
                SWCM.set(i, j, coverage);
            }
        }
    }

    public int getMax() {
        int max = 0;

        for(int i = 0; i < snippets.size(); i++) {
            double coverage = 0.0;
            for(int j = 0; j < words.size(); j++) {
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

        Integer wordCount = 0;
        for(String word : words) {
            wordCount += wordTable.get(word);
        }

        Double coverage = 0.0;
        for(int j = 0; j < words.size(); j++) {
            String word = words.get(j);
            double lamda = (double) wordTable.get(word) / wordCount;

            coverage += lamda * snippetsWordCoverage(snippetIndexList, j);
        }

        return coverage;
    }

    public Double snippetsWordCoverage(ArrayList<Integer> snippetIndexList, Integer wordIndex) {
        assert (SWCM != null);
        double irrevalence = 1;
        for(Integer snippetIndex : snippetIndexList) {
            irrevalence *= (1 - SWCM.get(snippetIndex, wordIndex));
        }

        return 1 - irrevalence;
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

    private void normalizeSWCM() {
        for(int i = 0; i < snippets.size(); i++) {
            double count = 0.0;
            for(int j = 0; j < words.size(); j++) {
                count += SWCM.get(i, j);
            }

            for(int j = 0; j < words.size(); j++) {
                double nval = SWCM.get(i, j) / count;
                SWCM.set(i, j, nval);
            }
        }
    }

}

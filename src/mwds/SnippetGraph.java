package mwds;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import filter.Filter;
import snippet.Snippet;
import tfidf.TFIDF;
import utils.Distance;
import utils.Similarity;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by jiusi on 2/11/14.
 */
public class SnippetGraph {

    public DoubleMatrix2D graph;
    public DoubleMatrix2D eigenVectorList;

    public ArrayList<HashMap<String, Integer>> snippetList;

    public ArrayList<String> wordList;
    public HashMap<String, Integer> wordTable;

    public SnippetGraph(ArrayList<Snippet> snippetList) {
        this.snippetList = Snippet.getHashMapSnippets(snippetList);
        wordTable = Snippet.extractWordsFromSnippetList(this.snippetList);
        (new Filter()).filterWordTableByThreshold(wordTable);
        wordList = new ArrayList<String>(wordTable.keySet());

        System.out.println("Coverage.Coverage wordTable.size:" + wordTable.size());

        fillEigenVectorList();

        makeGraph();
    }

    private void fillEigenVectorList() {
        eigenVectorList = DoubleFactory2D.sparse.make(snippetList.size(), wordList.size());

        for(int i = 0; i < snippetList.size(); i++) {
            if(i % 100 == 0) {
                System.out.println("SnippetGraph.fillEigenVectorList i:" + i + " total:" + snippetList.size());
            }
            for(int j = 0; j < wordList.size(); j++) {

                HashMap<String, Integer> snippet = snippetList.get(i);
                String word = wordList.get(j);

                Double tf = TFIDF.TF(word, snippet);
                Double idf = TFIDF.IDF(word, snippetList);

                Double tfidf = tf * idf;

                assert (!tfidf.isNaN());

                eigenVectorList.set(i, j, tfidf);
            }
        }
    }

    public DoubleMatrix2D makeGraph() {
        graph = DoubleFactory2D.sparse.make(snippetList.size(), snippetList.size());

        for(int i = 0; i < snippetList.size(); i++) {
            graph.set(i, i, 0.0);
        }

        for(int i = 0; i < snippetList.size(); i++) {

            if(i % 100 == 0) {
                System.out.println("SnippetGraph.makeGraph i:" + i + " total:" + snippetList.size());
            }

            for(int j = 0; j < snippetList.size(); j++) if (i != j) {
//                double distance = Distance.euclideanDistance(eigenVectorList, i, j);
                double distance = Similarity.isOrthogonality(eigenVectorList, i, j) ? 0.0 : 1.0;
                graph.set(i, j, distance);
            }
        }

        return graph;
    }

}

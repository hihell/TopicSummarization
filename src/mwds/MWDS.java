package mwds;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import tfidf.TFIDF;
import utils.Distance;
import utils.Normalizer;
import utils.Similarity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jiusi on 2/11/14.
 * Warning!!! This algorithm is a approximation
 */
public class MWDS {
//  snippet connection graph
    DoubleMatrix2D graph;
    DoubleMatrix2D eigenVectorList;

    public ArrayList<String> wordList;
    public HashMap<String, Integer> wordTable;

    ArrayList<HashMap<String, Integer>> snippetList;
    ArrayList<Double> snippetWeightList;

    HashMap<String, Integer> query;
    ArrayList<Double> queryEigenVector;

    public MWDS(SnippetGraph snippetGraph, HashMap<String, Integer> query) {
        this.graph = snippetGraph.graph;
        this.eigenVectorList = snippetGraph.eigenVectorList;
        this.snippetList = snippetGraph.snippetList;
        this.wordTable = snippetGraph.wordTable;
        this.wordList = snippetGraph.wordList;
        this.query = query;
        calculateQueryEigenVector();
        fillWeightList();
    }

    private void calculateQueryEigenVector() {
        queryEigenVector = new ArrayList<Double>();

        ArrayList<HashMap<String, Integer>> newSnippetList = new ArrayList<HashMap<String, Integer>>(snippetList);
        newSnippetList.add(query);

//        ArrayList<String> newWordList = new ArrayList<String>(wordList);
//        for(String word : query.keySet()) {
//            if(!newWordList.contains(word)) {
//                newWordList.add(word);
//            }
//        }

        for(int i = 0; i < wordList.size(); i++) {
            double tf = TFIDF.TF(wordList.get(i), query);
            double idf =  TFIDF.IDF(wordList.get(i), newSnippetList);

            Double tfidf = tf * idf;
            assert (!tfidf.isNaN());

            queryEigenVector.add(tfidf);
        }
    }


    public void fillWeightList() {
        snippetWeightList = new ArrayList<Double>();

        for(int i = 0; i < snippetList.size(); i++) {
            DoubleMatrix1D eigenVector = eigenVectorList.viewRow(i);
            DoubleMatrix1D ne = Normalizer.normalizeVector(eigenVector);

            ArrayList<Double> nq = Normalizer.normalizeVector(queryEigenVector);

//            Double distance = Distance.euclideanDistance(ne, nq);

            Double similarity = Similarity.cosine(ne, nq);

            if(similarity >= 1) {

                double size1 = 0.0;
                double size2 = 0.0;

                for(int j = 0; j < ne.size(); j++) {
                    size1 += ne.get(j);
                    size2 += nq.get(j);
                }

                similarity = Similarity.cosine(ne, nq);

                System.out.println("something is wrong");
            }

            System.out.println("similarity:" + similarity);
            similarity = Similarity.cosine(ne, nq);
            assert (similarity < 1);



            snippetWeightList.add(1 - similarity);
        }
    }

    public ArrayList<Integer> calculateMWDS() {

        Set<Integer> s = new HashSet<Integer>();
        Set<Integer> t = new HashSet<Integer>();

        while(s.size() != snippetList.size()) {

            int lastS = s.size();
            int lastT = t.size();

            for(int si = 0; si < snippetList.size(); si++) if(!s.contains(si)) {
//              find all vertex connected to si
                ArrayList<Integer> connected = new ArrayList<Integer>();
                for(int j = 0; j < snippetList.size(); j++) {
                    if(graph.get(si, j) > 0.0) {
                        connected.add(j);
                    }
                }

                ArrayList<Integer> filtered = new ArrayList<Integer>();
                for(Integer co : connected) {
                    if(!t.contains(co)) {
                        filtered.add(co);
                    }
                }

                if(filtered.size() == 0)
                    continue;

//              find the minimal weight vertex
                int min = 0;
                for(int j : filtered) {
                    if(snippetWeightList.get(j) < snippetWeightList.get(min)) {
                        min = j;
                    }
                }

                s.add(min);

//              find all vertex connected to min
                connected = new ArrayList<Integer>();
                for(int j = 0; j < snippetList.size(); j++) {
                    if(graph.get(min, j) > 0.0) {
                        connected.add(j);
                    }
                }
                t.addAll(connected);
            }

            if(lastS == s.size() && lastT == t.size()) {
                return new ArrayList<Integer>(s);
            }
        }

        ArrayList<Integer> indexList = new ArrayList<Integer>(s);
        return indexList;
    }

}

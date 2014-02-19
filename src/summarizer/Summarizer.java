package summarizer;


import coverage.Coverage;

import filter.Filter;
import mwds.MWDS;
import mwds.SnippetGraph;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import snippet.IKWordSpliter;
import snippet.Snippet;
import snippet.SnippetGenerator;
import utils.ResultReader;
import utils.ResultWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by jiusi on 2/11/14.
 */
public class Summarizer {

    public final int L = 5;


    public final String coverageResultPath = "./summarization/coverage";
    public final String dominateResultPath = "./summarization/dominate";

    public final String cdResultPath = "./summarization/coverage_dominate";
    public final String dcResultPath = "./summarization/dominate_coverage";

    public final String csResultPath = "./summarization/cover_simplification";
    public final String dsResultPath = "./summarization/dominate_simplification";

    public final String summarizationPath = "./summarization";

    public static final int subsetSize = 3;
    public static final int maxSize4BF = 100;

    public double candidateScore = 0.0;
    public ArrayList<Integer> candidateList = new ArrayList<Integer>();
    public ArrayList<Snippet> snippets;
    public Coverage coverage;
    public SnippetGraph snippetGraph;

    public int callCount;

    public Summarizer() {
        callCount = 0;
    }

    public ArrayList<Snippet> coverage(ArrayList<Snippet> snippets) {

        ArrayList<HashMap<String, Integer>> snippetList = Snippet.getHashMapSnippets(snippets);
        ArrayList<String> snippetTextList = Snippet.getOriginalSnippets(snippets);

        Coverage coverage = new Coverage(snippetList);

        ArrayList<Integer> indexList = new ArrayList<Integer>();
        indexList.add(coverage.maxCoverageIndex);
        System.out.println("max:" + snippetTextList.get(indexList.get(0)));

        double currentCoverage = coverage.snippetsWordsCoverage(indexList);
        while(indexList.size() < L) {

            int candidateIndex = -1;
            double maxDelta = 0.0;
            for(int i = 0; i < snippets.size(); i++) if(!indexList.contains(i)) {

                ArrayList<Integer> newIndexList = new ArrayList<Integer>(indexList);
                newIndexList.add(i);

                double currentDelta = coverage.snippetsWordsCoverage(newIndexList) - currentCoverage;

                if(currentDelta > maxDelta) {
                    candidateIndex = i;
                    maxDelta = currentDelta;
                }
            }

            if(candidateIndex == -1) {
                break;
            }

            indexList.add(candidateIndex);
            currentCoverage += maxDelta;

            System.out.println("currentCoverage:" + currentCoverage);
            System.out.println("cadidateIndex:" + candidateIndex);
        }

        ArrayList<Snippet> maxCoverage = new ArrayList<Snippet>();

        for(Integer i : indexList) {
            maxCoverage.add(snippets.get(i));
        }

        return maxCoverage;
    }

    public ArrayList<Snippet> dominate(ArrayList<Snippet> snippets, HashMap<String, Integer> query) {

        SnippetGraph snippetGraph = new SnippetGraph(snippets);
        MWDS mwds = new MWDS(snippetGraph, query);

        ArrayList<Integer> indexList = mwds.calculateMWDS();

        ArrayList<Snippet> minWeight = new ArrayList<Snippet>();
        for(Integer index : indexList) {
            minWeight.add(snippets.get(index));
        }

        return minWeight;
    }

    public ArrayList<Snippet> cover_dominate_method(String topicName, ArrayList<Snippet> snippetList, HashMap<String, Integer> query) throws IOException {

        ArrayList<Snippet> co = coverage(snippetList);
        if(co.size() == 0) {
            System.err.println("didn't find coverage set");
            return null;
        }
        ArrayList<Snippet> mw = dominate(co, query);

        ResultWriter.writeSummerization(topicName, mw, this.cdResultPath);

        return mw;
    }


    public ArrayList<Snippet> dominate_cover_method(String topicName,
                                                    ArrayList<Snippet> snippetList,
                                                    HashMap<String, Integer> query) throws IOException {

        ArrayList<Snippet> mw = dominate(snippetList, query);
        if (mw.size() == 0) {
            System.err.println("didn't find dominate set");
            return null;
        }
        ArrayList<Snippet> co = coverage(mw);

        ResultWriter.writeSummerization(topicName, co, this.dcResultPath);

        return co;
    }


    public void getAllCombinations(ArrayList<Integer> pre, int c, int n, String func) {

        callCount ++;

        if(callCount % 100 == 0) {
            System.out.println("callCount:" + callCount);
        }

        int start = 0;
        if(pre != null) {
            if(pre.size() != 0) {
                start = pre.get(pre.size() - 1) + 1;
            }
        } else {
            pre = new ArrayList<Integer>();
        }

        for(int i = start; i < n; i++) {
            ArrayList<Integer> cur = new ArrayList<Integer>(pre);
            cur.add(i);

            if(cur.size() == c) {

                double score;
                if(func.equals("cs")) {
                    score = cs(cur);
                } else if (func.equals("ds")) {
                    score = ds(cur);
                } else {
                    System.err.println("function:" + func + " not found!");
                    return;
                }

                if(score > candidateScore) {
                    candidateList = cur;
                    candidateScore = score;
                }
            } else {
                getAllCombinations(cur, c, n, func);
            }

        }
    }

    public ArrayList<Snippet> cover_simplification_method(String topicName,
                                                             ArrayList<Snippet> snippets) throws IOException {

        ArrayList<HashMap<String, Integer>> snippetList = Snippet.getHashMapSnippets(snippets);
        ArrayList<String> snippetTextList = Snippet.getOriginalSnippets(snippets);

        this.callCount = 0;
        this.candidateList = new ArrayList<Integer>();
        this.candidateScore = 0;

        this.snippets = snippets;
        this.coverage = new Coverage(snippetList);


        int n = snippets.size();
        int k = subsetSize;
        int ways =(int) org.apache.commons.math3.util.ArithmeticUtils.binomialCoefficient(n, k);

        System.out.println("cover_simplification_method, will call:" + ways);
         getAllCombinations(null, subsetSize, snippets.size(), "cs");

        System.out.println("candidate score:" + this.candidateScore);

        ArrayList<Snippet> result = new ArrayList<Snippet>();

        for(Integer i : candidateList) {
            result.add(snippets.get(i));
        }

        ResultWriter.writeSummerization(topicName, result, this.csResultPath);

        return result;
    }

    public double cs(ArrayList<Integer> indexList) {

        double cov = coverage.snippetsWordsCoverage(indexList);
        double sim = simplification(indexList.size(), snippets.size());

        double score = fMeasure(cov, sim);
        return score;
    }


    public ArrayList<Snippet> dominate_simplification_method(String topicName,
                                                             ArrayList<Snippet> snippets) throws IOException {

        this.callCount = 0;
        this.candidateList = new ArrayList<Integer>();
        this.candidateScore = 0;

        this.snippets = snippets;
        this.snippetGraph = new SnippetGraph(snippets);
        snippetGraph.makeGraph();


        int n = snippets.size();
        int k = subsetSize;
        int ways =(int) org.apache.commons.math3.util.ArithmeticUtils.binomialCoefficient(n, k);

        System.out.println("cover_simplification_method, will call:" + ways);

        getAllCombinations(null, subsetSize, snippets.size(), "ds");

        System.out.println("candidate score:" + this.candidateScore);

        ArrayList<Snippet> result = new ArrayList<Snippet>();

        for(Integer i : candidateList) {
            result.add(snippets.get(i));
        }

        ResultWriter.writeSummerization(topicName, result, this.dsResultPath);

        return result;
    }

    public double ds(ArrayList<Integer> indexList) {
        double dom = connectionCoverage(this.snippetGraph, indexList);
        double sim = simplification(indexList.size(), snippets.size());

        double score = fMeasure(dom, sim);
        return score;
    }

    private double connectionCoverage(SnippetGraph snippetGraph, ArrayList<Integer> indexList) {

        int rowCount = snippetGraph.graph.rows();
        Set<Integer> supplementaryList = new HashSet<Integer>();
        Set<Integer> subSet = new HashSet<Integer>(indexList);

        for(int i =0; i < rowCount; i++) if(!indexList.contains(i)) {
            supplementaryList.add(i);
        }

        double bridgeCount = 0;
        for(int i : indexList) {
            for(int j = 0; j < rowCount; j++) if(!subSet.contains(j)) {
                if(snippetGraph.graph.get(i,j) > 0.0) {
                    bridgeCount ++;
                }
            }
        }

        double totalEdgeCount = 0;
        for(int i = 0; i < rowCount; i++) {
            for(int j = i + 1; j < rowCount; j++) {
                if(snippetGraph.graph.get(i, j) > 0.0) {
                    totalEdgeCount++;
                }
            }
        }

        assert (bridgeCount < totalEdgeCount);

        return bridgeCount/totalEdgeCount;
    }

    public double simplification(int subsetSize, int totalSize) {
        return 1 - ((double) subsetSize / totalSize);
    }

    public double fMeasure(double p, double r) {
        int beta = 1;

        double numerator = (Math.pow(beta, 2) + 1) * p * r;
        double denominator = Math.pow(beta, 2) * p + r;

        return numerator / denominator;
    }

    public static void main(String args[]) {
        Summarizer s = new Summarizer();

        SnippetGenerator snippetGenerator = new SnippetGenerator(new IKWordSpliter("stopcn.txt"));

        HashMap<String, Integer> query = new HashMap<String, Integer>();
        query.put("委员会", 1);
        query.put("调查", 1);
        query.put("反思", 1);
        query.put("贫困", 1);

        try {
            HashMap<String, ArrayList<String>> topicFilesPath = ResultReader.topicResultReader("./kmeans.txt");
            ArrayList<String> topicList = new ArrayList<String>(topicFilesPath.keySet());

            for(String topic: topicList) {
                System.out.println("topic:" + topic);

                ArrayList<String> filesPath = topicFilesPath.get(topic);
                snippetGenerator.Process(filesPath);
                ArrayList<Snippet> snippets = Filter.filterSmallSnippet(snippetGenerator.snippets);
                snippets = Filter.filterLowRankSnippet(snippets, maxSize4BF);

//                s.cover_dominate_method(topic, snippets, query);
//                s.dominate_cover_method(topic, snippets, query);
//                s.cover_simplification_method(topic, snippets);

                s.dominate_simplification_method(topic, snippets);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}

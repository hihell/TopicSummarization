package summarizer;


import coverage.Coverage;
import org.omg.CORBA.INTERNAL;
import snippet.IKWordSpliter;
import snippet.Sentence;
import snippet.Snippet;
import snippet.SnippetGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by jiusi on 2/11/14.
 */
public class Summarizer {

    public final int L = 5;


    public ArrayList<HashMap<String, Integer>> getHashMapSnippets(ArrayList<Snippet> snippets) {
        ArrayList<HashMap<String, Integer>> snippetList = new ArrayList<HashMap<String, Integer>>();
        for(Snippet snippet : snippets) {
            HashMap<String, Integer> s = new HashMap<String, Integer>();
            for(Sentence sentence : snippet.sentence_list) {
                for(String word : sentence.split_words) {
                    if(s.containsKey(word)) {
                        int count = s.get(word);
                        s.put(word, count+1);
                    } else {
                        s.put(word, 1);
                    }
                }
            }
            snippetList.add(s);
        }

        return snippetList;
    }

    public ArrayList<String> getOriginalSnippets(ArrayList<Snippet> snippets) {
        ArrayList<String> sentenceList = new ArrayList<String>();

        for(Snippet snippet: snippets) {
            String str = "";
            for(Sentence sentence: snippet.sentence_list) {
                str += sentence.original;
            }
            sentenceList.add(str);
        }

        return sentenceList;
    }


    public void cover_dominate(ArrayList<Snippet> snippets) {



        ArrayList<HashMap<String, Integer>> snippetList = getHashMapSnippets(snippets);
        ArrayList<String> sentenceList = getOriginalSnippets(snippets);

        Coverage coverage = new Coverage(snippetList);

        ArrayList<Integer> indexList = new ArrayList<Integer>();
        indexList.add(coverage.maxCoverageIndex);
        System.out.println("max:" + sentenceList.get(indexList.get(0)));

        double currentCoverage = coverage.snippetsWordsCoverage(indexList);
        while(indexList.size() < 5) {

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
        }


        for(Integer i : indexList) {
            System.out.println(sentenceList.get(i));
        }
    }

    public void dominate_cover() {

    }


    public static void main(String args[]) {
        Summarizer s = new Summarizer();


        SnippetGenerator snippetGenerator = new SnippetGenerator(new IKWordSpliter("stopcn.txt"));

        try {
            snippetGenerator.Process("/Users/jiusi/Desktop/gmm/1.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

        s.cover_dominate(snippetGenerator.snippets);

    }


}

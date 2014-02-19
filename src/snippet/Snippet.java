package snippet;

import java.lang.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Snippet {
	public Sentence head_sentence;
	public ArrayList<Sentence> sentence_list = null;
	public static InfluenceFunc ifunc = null;
	public double threshold = 1.0;
	
	Snippet(Sentence head_sentence) {
		this.head_sentence = head_sentence;
		sentence_list = new ArrayList<Sentence>();
		sentence_list.add(head_sentence);
	}
	
	static void SetInfluenceFunc(InfluenceFunc ifunc) {
		Snippet.ifunc = ifunc;
	}
	
	void SetThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	int size() {
		return sentence_list.size();
	}
	
	double DistFunc(Sentence s1, Sentence s2) {
		//如果s1和s2相临，为了防止得到INF，给一个较大的值
		if (Sentence.GetDist(s1, s2) == 0) {
			return 100;
		} else {
			return Math.log(size()/(Math.abs(Sentence.GetDist(s1, s2))));
		}
	}
	
	boolean TryAdd(Sentence s) {
		double sum = 0.0;
		for (Sentence s1 : sentence_list) {
			sum += ifunc.Calculate(Sentence.GetDist(head_sentence, s1)) 
				*  DistFunc(s, s1) * Sentence.GetCommon(s, s1);
		}
		sum /= size();
		if (sum > threshold) {
			sentence_list.add(s);
			return true;
		} else {
			return false;
		}
	}

    public static ArrayList<HashMap<String, Integer>> getHashMapSnippets(ArrayList<Snippet> snippets) {
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

    public static ArrayList<String> getOriginalSnippets(ArrayList<Snippet> snippets) {
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

    public static HashMap<String, Integer> extractWordsFromSnippetList(ArrayList<HashMap<String, Integer>> snippetList) {

        HashMap<String, Integer> wordTable = new HashMap<String, Integer>();
        for(HashMap<String, Integer> snippet : snippetList) {
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

        return wordTable;
    }

}

package snippet;

import java.util.ArrayList;
import java.lang.Math;

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
	
}

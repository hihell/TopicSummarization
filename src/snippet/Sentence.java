package snippet;

import java.util.ArrayList;


public class Sentence {
	ArrayList<String> split_words;
	int pos;
	
	Sentence(ArrayList<String> split_words, int pos) {
		this.split_words = split_words;
		this.pos = pos;
	}
	
	/*s1与s2的距离指的是s1和s2之间的句子数量*/
	static public int GetDist(Sentence s1, Sentence s2) {
		if (s1.pos > s2.pos) return s1.pos - s2.pos - 1;
		else return s1.pos - s2.pos + 1;
	}
	
	static public int GetCommon(Sentence s1, Sentence s2) {
		int common = 0;
		for (String st : s1.split_words) {
			if (s2.split_words.contains(st)) {
				common++;
			}
		}
		return common;
	}
	
	@Override
	public String toString() {
		return split_words.toString();
	}
}

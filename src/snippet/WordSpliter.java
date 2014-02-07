package snippet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class WordSpliter {
	
	WordSpliter(String stop_path) {
		stop_words = new HashMap<String, String>();
		try {
			ReadStopWord(stop_path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> Process(String sentence) {
		ArrayList<String> split_words = SplitWords(sentence);
		FilterStopWord(split_words);
		return split_words;
	}
	
	/*重写该函数即可使用不同的分词器*/
	abstract ArrayList<String> SplitWords(String sentence);
	
	void ReadStopWord(String stop_list_path) throws IOException {		
		BufferedReader reader = new BufferedReader(new FileReader(new File(stop_list_path)));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			stop_words.put(line, null);
		}	
		reader.close();
	}
	
	void FilterStopWord(ArrayList<String> word_list) {
		boolean has_stop_word = true;
		while (has_stop_word && word_list.size() > 0) {
			for (int i = 0; i != word_list.size(); ++i) {
				has_stop_word = false;
				if (stop_words.containsKey(word_list.get(i))) {
					word_list.remove(i);
					has_stop_word = true;
					break;
				}
			}
		}
	}
	
	HashMap<String, String> stop_words;
}

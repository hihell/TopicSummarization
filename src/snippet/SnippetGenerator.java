package snippet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class SnippetGenerator {
	ArrayList<Sentence> sentences = null;
	ArrayList<Snippet> snippets = null;
	WordSpliter spliter = null;
	
	
	SnippetGenerator(WordSpliter spliter) {
		this.spliter = spliter;
	}
	
	public ArrayList<Sentence> SplitArticle(String article_path) throws IOException {
		sentences = new ArrayList<Sentence>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(article_path)));
		int tmp;
		String one_sentence = new String();
		int pos = 0;
		while ((tmp = reader.read()) != -1) {
			char c = (char)tmp;
			switch (c) {
			case '。':
			//case '，':
			case '\n':
				if (one_sentence.length() != 0) {
					ArrayList<String> splited_words = spliter.Process(one_sentence);
					sentences.add(new Sentence(splited_words, pos++));
					one_sentence = new String();
				}
				break;
			case '\r':
				break;
			default:
				String s = String.valueOf(c);
				one_sentence += s;
				break;
			}
		}
		reader.close();
		return sentences;
	}
	
	public void SetSpliter(WordSpliter spliter) {
		this.spliter = spliter;
	}
	
	public void Process(String article_path) throws IOException {
		snippets = new ArrayList<Snippet>();
		SplitArticle(article_path);
		Snippet.SetInfluenceFunc(new iFuncList.LogNormalFunc(0.3, 0, 1));
		int index = 0;
		while (index < sentences.size()) {
			Snippet snippet = new Snippet(sentences.get(index));
			int left_pos = index - 1;
			int right_pos = index + 1;
			boolean left_added = true, right_added = true;
			while (left_added || right_added) {
				if (left_pos >= 0 && snippet.TryAdd(sentences.get(left_pos))) {
					--left_pos;
				} else {
					left_added = false;
				}
				
				if (right_pos < sentences.size() && snippet.TryAdd(sentences.get(right_pos))) {
					++right_pos;
				} else {
					right_added = false;
				}
				
			}
			index = right_pos;
			snippets.add(snippet);
		}
	}
	
	public static void main(String[] args) {
		SnippetGenerator test = new SnippetGenerator(new IKWordSpliter("stopword.dic"));
		try {
//			test.Process("test.txt");
            test.Process("/Users/jiusi/Desktop/gmm/1.txt");

			for (Snippet s : test.snippets) {
				System.out.println(s.head_sentence);
				System.out.println(s.sentence_list.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

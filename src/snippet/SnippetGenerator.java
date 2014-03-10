package snippet;
import org.omg.DynamicAny._DynFixedStub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SnippetGenerator {
	public ArrayList<Sentence> sentences = null;
	public ArrayList<Snippet> snippets = null;
	public WordSpliter spliter = null;

    public final String articlePath;
	
	public SnippetGenerator(WordSpliter spliter, String articlePath) {
		this.spliter = spliter;
        this.articlePath = articlePath;
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
					sentences.add(new Sentence(splited_words, pos++, one_sentence));
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

    public void Process(ArrayList<String> files_path) throws IOException{
        ArrayList<Snippet> snippetList = new ArrayList<Snippet>();
        for(String filePath : files_path) {
            filePath = articlePath + "/" + filePath;
            Process(filePath);
            snippetList.addAll(this.snippets);
        }
        snippets = snippetList;

//        System.out.println("SnippetGenerator.Process snippetList.size:" + snippetList.size());
    }

    public static ArrayList<Integer> findSmallSnippet(ArrayList<HashMap<String, Integer>> snippetList, int smallValue) {
        ArrayList<Integer> smallIndexList = new ArrayList<Integer>();

        for(int i = 0; i < snippetList.size(); i++) {
            int accu = 0;
            HashMap<String, Integer> snippet = snippetList.get(i);
            for(Integer v : snippet.values()) {
                accu += v;
            }

            if(accu < smallValue) {
                smallIndexList.add(i);
            }
        }

        return smallIndexList;
    }


	public static void main(String[] args) {
        SnippetGenerator test = new SnippetGenerator(new IKWordSpliter("stopcn.txt"), "");
		try {
//			test.Process("test.txt");

            ArrayList<String> fileList = new ArrayList<String>();
            fileList.add("/Users/jiusi/Desktop/gmm/23.txt");
            fileList.add("/Users/jiusi/Desktop/gmm/24.txt");
            fileList.add("/Users/jiusi/Desktop/gmm/25.txt");


            test.Process(fileList);

			for (Snippet s : test.snippets) {

//				System.out.println(s.head_sentence);
//				System.out.println(s.sentence_list.toString());

                for(Sentence sen : s.sentence_list) {
                    System.out.println(sen.original);
                }

                if(s.size() == 0) {
                    System.out.println("this snippet is empty");
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package snippet;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class IKWordSpliter extends WordSpliter {

	IKWordSpliter(String stop_path) {
		super(stop_path);
	}

	@Override
	ArrayList<String> SplitWords(String sentence) {
		ArrayList<String> word_list = new ArrayList<String>();
		StringReader reader = new StringReader(sentence);
		IKSegmenter segmenter = new IKSegmenter(reader, true);
		Lexeme word = null;
		try {
			while ((word = segmenter.next()) != null) {
				word_list.add(word.getLexemeText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return word_list;
	}

}

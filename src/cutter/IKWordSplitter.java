package cutter;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by jiusi on 2/7/14.
 */

public class IKWordSplitter extends WordSplitter {

    @Override
    public ArrayList<String> splitSentence(String sentence) {
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
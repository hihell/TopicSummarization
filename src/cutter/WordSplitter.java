package cutter;


import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public abstract class WordSplitter {
    abstract ArrayList<String> splitSentence(String sentence);
}

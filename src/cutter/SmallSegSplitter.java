package cutter;

import fx.sunjoy.SmallSeg;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by jiusi on 2/7/14.
 */
public class SmallSegSplitter extends WordSplitter{

    @Override
    public ArrayList<String> splitSentence(String sentence) {
        return new ArrayList<String>(SmallSeg.cut(sentence));
    }

}



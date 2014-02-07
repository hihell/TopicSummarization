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
        return new ArrayList(SmallSeg.cut(sentence));
    }


    public static void main(String[] args) {
        SmallSegSplitter sss = new SmallSegSplitter() ;
        String sen = "当我问起这是否意味着今后人们的打字习惯需要改变时，Thad 说：“手机已经改变了我们打字的习惯。";
        ArrayList<String> words = sss.splitSentence(sen);

        System.out.println(words);

    }


}



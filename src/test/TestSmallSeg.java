package test;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 03:04
 * To change this template use File | Settings | File Templates.
 */



import java.util.ArrayList;
import cutter.IKWordSplitter;
import cutter.SmallSegSplitter;

public class TestSmallSeg {

    public static void main(String args[]) {

        SmallSegSplitter sss = new SmallSegSplitter();
        IKWordSplitter iks = new IKWordSplitter();
        String sen = "当我问起这是否意味着今后人们的打字习惯需要改变时，Thad 说：“手机已经改变了我们打字的习惯。";
        ArrayList<String> ssswords = sss.splitSentence(sen);
        ArrayList<String> ikswords = iks.splitSentence(sen);

        System.out.println(ssswords);
        System.out.println(ikswords);


        ArrayList<String> diffList = new ArrayList<String>();

        for(String w : ssswords) {
            if (ikswords.contains(w)) {
                ikswords.remove(w);
            } else {
                diffList.add(w);
            }
        }

        diffList.addAll(ikswords);

        System.out.println(diffList);
    }

}

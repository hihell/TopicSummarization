package test;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 03:04
 * To change this template use File | Settings | File Templates.
 */


import fx.sunjoy.SmallSeg;
import java.util.List;

public class TestSmallSeg {

    public static void main(String args[]) {
        String input = "青岛有几个媒体人，天天在羞辱“媒体”这个行业，一出事就高喊：“青岛雄起！青岛奋进！青岛迸发！青岛挺住！爆炸让你更坚强！”那是52个鲜活生命啊，52个家庭的灭顶之灾啊，尸骨未寒、责任未明、搜救未完、人心未暖……你说你雄起奋进挺住迸发你MLGB啊";
        List<String> result = SmallSeg.cut(input);

        for (String w : result) {
            System.out.println(w);
        }
    }

}

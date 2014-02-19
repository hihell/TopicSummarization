package cutter;

/**
 * Created with IntelliJ IDEA.                                                               `
 * User: jiusi
 * Date: 25/11/2013
 * Time: 03:26
 * To change this template use File | Settings | File Templates.
 */




import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Cutter {

    public final String outPath = "./cutResult.txt";
    private File[] listOfFiles;
    private String filesDir;
    private WordSplitter wordSplitter;

    public Cutter(String filesDir) throws Exception {
        File folder = new File(filesDir);
        this.listOfFiles = folder.listFiles();
        this.filesDir = filesDir;
        this.wordSplitter = new IKWordSplitter();

        if(this.listOfFiles == null) {
            throw new Exception("ArticleNotFoundException");
        }
    }

    public void cutIntoOneFile() throws IOException {
        PrintWriter writer = new PrintWriter(this.outPath, "UTF-8");

        InputStream    fis;
        BufferedReader br;

        for(int i=0; i < listOfFiles.length; i++) if (listOfFiles[i].isFile()) {
            String fileName = listOfFiles[i].getName();
            if (fileName.toLowerCase().endsWith(".txt")) {
                String line;

                fis = new FileInputStream(listOfFiles[i]);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                // write file name
                writer.println(fileName);

                while ((line = br.readLine()) != null) {

//                    List<String> cutResult = SmallSeg.cut(line);
                    List<String> cutResult = wordSplitter.splitSentence(line);

//                  write wordList, separate with space
                    for(String w : cutResult) {
                        writer.print(w + " ");
                    }

                }
                writer.println();
            }
        }

        writer.close();
    }

    public void cutTopicIntoLists(ArrayList<String> files,
                                      ArrayList<Hashtable<String, Integer>> sList,
                                      Hashtable<String, Integer> wordTable) throws IOException{

        if ((wordTable == null) || (sList == null)) {
            throw new AssertionError();
        }

        InputStream    fis;
        BufferedReader br;

        for(String fileName: files) {
            File file = new File(this.filesDir + "/" + fileName);

            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

            String line;
            while ((line = br.readLine()) != null) {
                ArrayList<String> sentenceList = this.cutIntoSentences(line);

                for (String sentence : sentenceList) {
//                    List<String> cutResult = SmallSeg.cut(sentence);
                    List<String> cutResult = wordSplitter.splitSentence(sentence);
                    Hashtable<String, Integer> sen = listIntoTable(cutResult);
                    sList.add(sen);

                }
            }
        }

        for(Hashtable<String, Integer> sentence : sList) {
            Enumeration<String> words = sentence.keys();
            while(words.hasMoreElements()) {
                String word = words.nextElement();
                if(wordTable.containsKey(word)) {
                    wordTable.put(word, wordTable.get(word) + sentence.get(word));
                } else {
                    wordTable.put(word, sentence.get(word));
                }
            }
        }

//        todo check if there is any repeat word
    }

    // 句子只会以中文标点切割
    // 如果要将此函数改成切割英文要小心英文句号的转义
    public ArrayList<String> cutIntoSentences(String line) {

        String[] marks = {"。", "？", "！"};
        ArrayList<String> pre = new ArrayList<String>();
        pre.add(line);
        ArrayList<String> post;
        for (String mark: marks) {
            post = new ArrayList<String>();

            for(String s: pre) {
                String[] sp = s.split(mark);
                for(String spp: sp) {
                    post.add(spp);
                }
            }
            pre = post;
        }

        return pre;
    }

    Hashtable<String, Integer> listIntoTable(List<String> list) {
        Hashtable<String, Integer> table = new Hashtable<String, Integer>();
        for(String word : list) {
            if(table.containsKey(word)) {
                table.put(word, table.get(word) + 1);
            } else {
                table.put(word, 1);
            }
        }
        return table;
    }


}

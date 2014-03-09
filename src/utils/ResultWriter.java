package utils;

import com.sun.java.swing.plaf.windows.resources.windows;
import snippet.Sentence;
import snippet.Snippet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by jiusi on 2/17/14.
 */
public class ResultWriter {

    public static void writeSummerization(String topicName, ArrayList<Snippet> snippetList, String path) throws IOException{

        topicName = topicName.replace(":" , "_");

        PrintWriter writer = new PrintWriter(path + "/" + topicName + ".txt", "UTF-8");

        for(Snippet snippet : snippetList) {
            writer.println("snippet:");
            for(Sentence sentence : snippet.sentence_list) {
                writer.println(sentence.original);
            }
        }

        writer.close();
    }


    public static void clearFolder(File folder) {
        System.out.println("delete files under:" + folder);

        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                f.delete();
            }
        }
    }


}

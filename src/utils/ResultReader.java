package utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiusi on 2/11/14.
 */
public class ResultReader {

    public static HashMap<String, ArrayList<String>> topicResultReader(String path) throws IOException {

        HashMap<String, ArrayList<String>> topicFileTable = new HashMap<String, ArrayList<String>>();

        InputStream fis = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        Pattern topicPattern = Pattern.compile("[.\\d]:");
        Pattern filePattern = Pattern.compile("[.\\d].txt");

        String line;
        ArrayList<String> fileList = new ArrayList<String>();
        while((line = br.readLine()) != null) {
            line = line.trim();
            Matcher topicMatcher = topicPattern.matcher(line);
            Matcher fileMatcher = filePattern.matcher(line);

            if(topicMatcher.find()) {
                fileList = new ArrayList<String>();
                topicFileTable.put(line, fileList);
            } else if(fileMatcher.find()) {
                fileList.add(line);
            }
        }

        return topicFileTable;
    }





}

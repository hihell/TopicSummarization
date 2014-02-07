package statistics;

//import javafx.util.SimpleEntry;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.AbstractMap.SimpleEntry;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 17:37
 * To change this template use File | Settings | File Templates.
 */
public class Statistics {

    public Hashtable<String, ArrayList<SimpleEntry<String, Double>>> topicWordTable;

    public Statistics(String LDAResultPath) throws IOException {
        topicWordTable = new Hashtable<String, ArrayList<SimpleEntry<String, Double>>>();
        FileInputStream fis = new FileInputStream(new File(LDAResultPath));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

        String line;
        ArrayList<SimpleEntry<String, Double>> probTable = new ArrayList<SimpleEntry<String, Double>>();
        while((line = br.readLine()) != null) {

            Pattern pattern = Pattern.compile("Topic\\s[.\\d]*th:");
            Matcher matcher = pattern.matcher(line);

            if(matcher.find()) {
                probTable = new ArrayList<SimpleEntry<String, Double>>();
                topicWordTable.put(line, probTable);
            } else {
                String word = line.split(" ")[0].trim();
                Double prob = Double.valueOf(line.split(" ")[1]);
                probTable.add(new SimpleEntry(word, prob));
            }
        }

    }

    public Hashtable<String, ArrayList<String>> getTopicFileTable(Hashtable<String, Hashtable<String, Integer>> fileWordTable) {
        Hashtable<String, ArrayList<String>> topicFileTable = new Hashtable<String, ArrayList<String>>();

        Enumeration<String> fileNames = fileWordTable.keys();
        while(fileNames.hasMoreElements()) {
            String fileName = fileNames.nextElement();

            ArrayList<SimpleEntry<String, Double>> topicProbList = getProbList(fileWordTable.get(fileName));
            SimpleEntry<String, Double> daTopic = Collections.max(topicProbList, new SimpleEntryComparator());

            if(topicFileTable.containsKey(daTopic.getKey())) {
                topicFileTable.get(daTopic.getKey()).add(fileName);
            } else {
                ArrayList<String> newList = new ArrayList<String>();
                newList.add(fileName);
                topicFileTable.put(daTopic.getKey(), newList);
            }
        }

        return topicFileTable;
    }

    public ArrayList<SimpleEntry<String, Double>> getProbList(Hashtable<String, Integer> wordTable) {
        ArrayList<SimpleEntry<String, Double>> topicProbList = new ArrayList<SimpleEntry<String, Double>>();

        Enumeration<String> topics = this.topicWordTable.keys();
        while(topics.hasMoreElements()) {
            String topic = topics.nextElement();
            Double score = 0.0;

            for(SimpleEntry<String, Double> p : topicWordTable.get(topic)) {

                if(wordTable.containsKey(p.getKey())) {
                    score += wordTable.get(p.getKey()) * p.getValue();
                }
            }

            topicProbList.add(new SimpleEntry<String, Double>(topic, score));
        }


        return topicProbList;
    }



    class SimpleEntryComparator implements Comparator<SimpleEntry<String, Double>> {

        public int compare(SimpleEntry<String, Double> first,
                           SimpleEntry<String, Double> second)
        {
            // TODO: Null checking, both for maps and values
            Double firstValue = first.getValue();
            Double secondValue = second.getValue();
            return firstValue.compareTo(secondValue);
        }
    }
}

package keywords;

//import javafx.util.SimpleEntry;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * Created with IntelliJ IDEA.
 * User: jiusi
 * Date: 25/11/2013
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class KeyWord {

    public ArrayList<ArrayList<Double>> MS;
//    public ArrayList<ArrayList<Double>> MW;
    public DoubleMatrix2D SMW;

    public ArrayList<Hashtable<String, Integer>> sList; // each sentence is a hashtable, word is the key, appear time is value
    public ArrayList<SimpleEntry<String, Integer>> wList; // word with its appear time

    public ArrayList<Double> sRank;
    public ArrayList<Double> wRank;

    public final double d = 0.5; //range from 0 to 1

    public final double sError = 0.001;
    public final double wError = 0.001;

    public final double sRankThreshold = 0.001;
    public final double supportThreshold = 0.0001;


//  K, how many keyword do you want
    public final int k = 10;

    public final int log_granularity = 100;

    public KeyWord() {

    }

    public KeyWord(ArrayList<Hashtable<String, Integer>> sList,  Hashtable<String, Integer> wTable) {
        System.out.println("!KeyWord!");
        this.sList = sList;

        this.wList = new ArrayList<SimpleEntry<String, Integer>>();
        Enumeration<String> words = wTable.keys();
        while (words.hasMoreElements()) {
            String word = words.nextElement();
            if(word.length() > 0) {
                this.wList.add(new SimpleEntry<String, Integer>(word, wTable.get(word)));
            }
        }

        int ecount = 0;
        for(Hashtable<String, Integer> s : sList) {
            if (s.size() == 0) {
                ecount++;
            }
        }
        System.out.println("sentences, empty/total:" + ecount + "/" + sList.size());
        System.out.println("wordList:" + wList.size());

        MS = getMatrix(0.0, sList.size());
        SMW = DoubleFactory2D.sparse.make(wList.size(), wList.size());
    }

    public ArrayList<SimpleEntry<String, Double>> calculateKeyWord() {
        this.fillMS();
        processSRank();

//        this.fillMW();
        this.fillSparseMW();

//        testSparse();

        processWRank();

        System.out.println("Matrix size:" + SMW.size());

        return topK(0, wList.size());
    }

    public ArrayList<ArrayList<Double>> getMatrix(Double junk, int size) {
        ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();
        for(int i=0; i < size; i++) {
            matrix.add(new ArrayList<Double>(size));
            for(int j=0; j < size; j++) {
                matrix.get(i).add(junk);
            }
        }
        return matrix;
    }


//  keyword generation flow
    public Integer maxCo(Hashtable<String, Integer> s1, Hashtable<String, Integer> s2) {
        Integer commonWordCount = 0;
        Enumeration<String> words = s1.keys();
        while(words.hasMoreElements()) {
            String word = words.nextElement();

            if(s2.containsKey(word)) {
                commonWordCount += Math.min(s1.get(word), s2.get(word));
            }
        }
        return commonWordCount;
    }

    public Integer wordCount(Hashtable<String, Integer> sentence) {
        Integer len = 0;
        Enumeration<String> words = sentence.keys();
        while(words.hasMoreElements()) {
            String word = words.nextElement();
            len += sentence.get(word);
        }

        return len;
    }


//    there are some empty sentences
    public double IF(Hashtable<String, Integer> s1, Hashtable<String, Integer> s2) {
        if(wordCount(s2) != 0) {
            return (double)(maxCo(s1, s2) / wordCount(s2));
        } else {
            return 0;
        }
    }

    public void fillMS() {
        for(int i=0; i<sList.size(); i++) {
            for (int j=0; j<sList.size(); j++) {
                if(i == j) {
                    MS.get(i).set(j, 0.0);
                } else {
                    MS.get(i).set(j, IF(sList.get(i), sList.get(j)));
                }
            }
        }
        normalization(MS);
    }

    public void processSRank() {
        System.out.println("Process sRank... size:" + sList.size());

//        initialize rank list with value of 1
        sRank = new ArrayList<Double>();
        for(int i=0; i<sList.size(); i++) {
            this.sRank.add(1.0);
        }

        while(true) {
            ArrayList<Double> newSRank = new ArrayList<Double>();

            for(int i=0; i<sList.size(); i++) {

                double mid = 0.0;
                for(int j=0; j<sList.size(); j++) if(i != j) {
//                    mid += MS[j][i] * sRank.get(j);
                    mid += MS.get(j).get(i) * sRank.get(j);
                }

                double x = d * mid + (1 - d) / sList.size();

                newSRank.add(x);
            }

            if (variance(sRank, newSRank) < sError) {
                System.out.println("Process sRank... Finished");

                sRank = newSRank;

                this.trimSListSRankByThreshold(this.sRankThreshold);
                return;
            }

            sRank = newSRank;
        }
    }

    public void normalization(ArrayList<ArrayList<Double>> matrix) {

        System.out.println("normalize array started");

        assert (matrix.size() == matrix.get(0).size());

        for(int i=0; i < matrix.size(); i++) {
            Double div = 0.0;
            for(int j=0; j < matrix.size(); j++) {
                div += matrix.get(i).get(j);
            }

            for(int j=0; j < matrix.size(); j++) {
                if(div != 0) {
                    matrix.get(i).set(j, matrix.get(i).get(j) / div);
                } else {
                    matrix.get(i).set(j, 0.0);
                }
            }
        }

        System.out.println("normalize array finished");
    }

    public void normalization(DoubleMatrix2D smatrix) {

        System.out.println("normalize sparse matrix started");

        Double tmp = Math.pow(smatrix.size(), 0.5);
        Integer height = tmp.intValue();

        assert (height * height == smatrix.size());

        for(int i=0; i < height; i++) {
            Double div = 0.0;
            for(int j=0; j < height; j++) {
//                div += matrix.get(i).get(j);
                try {
                    div += smatrix.get(i, j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("j:" + j);
                }

            }

            for(int j=0; j < height; j++) {
                if(div != 0 && smatrix.get(i, j) != 0) {
                    smatrix.set(i, j, smatrix.get(i, j) / div);
                } else {
                    smatrix.set(i, j, 0.0);
                }
            }
        }

        System.out.println("normalize array finished");
    }



    public double support(int i, int j) {
        String wi = wList.get(i).getKey();
        String wj = wList.get(j).getKey();

        double sup = 0.0;

        for(int s=0; s<sList.size(); s++) {
            Hashtable<String, Integer> sentence = sList.get(s);
            if(sentence.containsKey(wi) && sentence.containsKey(wj)) {
                sup += sRank.get(s);
            }
        }
        return sup;
    }

    public String getMWKey(Integer i, Integer j) {
        String key = i.toString() + " " + j.toString();
        return key;
    }

    public void fillSparseMW() {

        System.out.println("fillSparseMW... size:" + wList.size());
        Integer smallcount = 0;
        for (int i=0; i<wList.size(); i++) {
            for (int j=i; j<wList.size(); j++) {
                if(i == j) {
//                    MW.get(i).set(j, 0.0);
                    SMW.set(i, j, 0.0);
                } else {
                    Double sij = support(i, j);
                    if(sij < supportThreshold) {
                        smallcount++;
                    }
//                    MW.get(i).set(j, sij);
//                    MW.get(j).set(i, sij);
                    SMW.set(i, j, sij);
                    SMW.set(j, i, sij);
                }
            }
            if(i - i / log_granularity * log_granularity == 0) {
                System.out.println("    filled:" + i + "/" + wList.size());
            }
        }

        System.out.println("small count:" + smallcount);
        System.out.println("total size:" + Math.pow(wList.size(), 2));

        normalization(SMW);
    }




    public void processWRank() {
        System.out.println("Process wRank... size:" + wList.size());

//        initialize rank list with value of 1
        wRank = new ArrayList<Double>();

        for(int i=0; i<wList.size(); i++) {
            this.wRank.add(1.0);
        }

        int loop = 0;
        while(true) {
            ArrayList<Double> newWRank = new ArrayList<Double>();

            for(int i=0; i<wList.size(); i++) {
                double mid = 0.0;
                for(int j=0; j<wList.size(); j++) if(i != j) {
                    mid += SMW.get(j, i) * wRank.get(j);
                }
                newWRank.add(d * mid + (1 - d) / wList.size());
            }

            double v = variance(wRank, newWRank);
            System.out.println("variance:" + v + " wError:" + this.wError);
            if (v < wError) {
                wRank = newWRank;
                System.out.println("Process wRank... Finished");
                return;
            }

            wRank = newWRank;
            System.out.println("process wRank, loop:" + loop);
            loop++;
        }
    }

    public Double variance(ArrayList<Double> rank, ArrayList<Double> newRank) {

        assert (rank.size() == newRank.size());

        double var = 0.0;

        for (int i=0; i<rank.size(); i++) {
            var += Math.pow((rank.get(i) - newRank.get(i)), 2);
        }

        return var;
    }

    public ArrayList<SimpleEntry<String, Double>> topK(int start, int stop) {
        int m = partition(start, stop);

        if(m == this.k) {

            // make the top k word list with its rank
            ArrayList<SimpleEntry<String, Double>> topK = new ArrayList<SimpleEntry<String, Double>>();
            for(int i=0; i<k; i++) {
                topK.add(new SimpleEntry<String, Double>(wList.get(i).getKey(), wRank.get(i)));
            }
            return topK;

        } else if(m > this.k) {
            return topK(start, m);
        } else {
            return topK(m + 1, stop);
        }
    }

    public int partition(int start, int stop) {
        int p = start;
        Double mid = wRank.get(stop - 1);

        for(int i=start; i < stop; i++) {
            if (wRank.get(i) > mid) {
                Double tmp = wRank.get(p);
                wRank.set(p, wRank.get(i));
                wRank.set(i, tmp);

                SimpleEntry<String, Integer> temp = new SimpleEntry<String, Integer>(wList.get(p).getKey(), wList.get(p).getValue());
                wList.set(p, new SimpleEntry<String, Integer>(wList.get(i).getKey(), wList.get(i).getValue()));
                wList.set(i, temp);

                p++;
            }
        }

        Double tmp = wRank.get(p);
        wRank.set(p, wRank.get(stop-1));
        wRank.set(stop-1, tmp);

        SimpleEntry<String, Integer> temp = new SimpleEntry<String, Integer>(wList.get(p).getKey(), wList.get(p).getValue());
        wList.set(p, new SimpleEntry<String, Integer>(wList.get(stop-1).getKey(), wList.get(stop-1).getValue()));
        wList.set(stop-1, temp);

        return p;
    }

    public void trimSListSRankByThreshold(double threshold) {
        ArrayList<Hashtable<String, Integer>> newSList = new ArrayList<Hashtable<String, Integer>>();
        ArrayList<Double> newSRank = new ArrayList<Double>();

        for(int i = 0; i < sRank.size(); i++) {
            if(sRank.get(i) > threshold) {
                newSRank.add(sRank.get(i));

                Hashtable<String, Integer> s = new Hashtable<String, Integer>(sList.get(i));
                newSList.add(s);
            }
        }

        System.out.println("removed: " + (sList.size() - newSList.size()) + " small value sentences");
        System.out.println(newSList.size() + " left");
        this.sList = newSList;
        this.sRank = newSRank;
    }

    public static void main(String[] args) {
        double x = 0.000000;
        Double y = 1.0;

        System.out.println(y/x);
        System.out.println(x == 0);
    }
}

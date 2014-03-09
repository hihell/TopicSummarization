package utils;

/**
 * Created by jiusi on 2/19/14.
 */

import sun.net.www.content.audio.wav;

import java.util.*;

public class Comb {

    public int c;
    public int n;

    public Comb(int c, int n) {
        this.c = c;
        this.n = n;
    }

    public ArrayList<ArrayList<Integer>> getAllCombinatinos(ArrayList<Integer> pre) {

        int start = 0;
        if(pre != null) {
            if(pre.size() != 0) {
                start = pre.get(pre.size() - 1) + 1;
            }
        } else {
            pre = new ArrayList<Integer>();
        }

        ArrayList<ArrayList<Integer>> allList = new ArrayList<ArrayList<Integer>>();
        for(int i = start; i < n; i++) {
            ArrayList<Integer> cur = new ArrayList<Integer>(pre);
            cur.add(i);

            if(cur.size() == this.c) {
                allList.add(cur);
            } else {
                allList.addAll(getAllCombinatinos(cur));
            }

        }

        return allList;
    }



    public static void main(String[] args) {
        int n = 400;
        int k = 3;
        double ways = org.apache.commons.math3.util.ArithmeticUtils.binomialCoefficient(n, k);

        assert (ways == 10586800);
        System.out.println(ways);
    }

}
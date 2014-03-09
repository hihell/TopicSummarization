package converger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

public class Converger {
	public static final double convergeThreshold = 0.00000000000011;
    public static final int maxConvergeLoop = 100;
    public static final int minClusterSize = 3;
    
    public ArrayList<Map<String, double[]>> clusterList;
    
    public Converger(ArrayList<Map<String, double[]>> clusterList) {
    	this.clusterList = clusterList;
    }            
    
    private double getDistance(double[] p1, double[] p2) {
        assert (p1.length == p2.length);

        double distance = 0.0;

        for(int i = 0; i < p1.length; i++) {
            distance += Math.pow(p1[i] - p2[i], 2);
        }

        return Math.pow(distance, 0.5);
    }
    
    private SimpleEntry<Double, int[]> findMinPair(ArrayList<SimpleEntry<Integer, double[]>> centroidList) {
        int[] pair = new int[2];
        double min = Double.POSITIVE_INFINITY;
//        todo consider this matrix
        for (int i = 0; i < centroidList.size(); i++) {
            for(int j = i+1; j < centroidList.size(); j++) if(i != j) {
                double[] pi = centroidList.get(i).getValue();
                double[] pj = centroidList.get(j).getValue();
                double distance = getDistance(pi, pj);
                if(distance< min) {
                    min =  distance;
                    pair[0] = i;
                    pair[1] = j;
                }
            }
        }
        return new SimpleEntry<Double, int[]>(min, pair);
    }
    
	private ArrayList<SimpleEntry<Integer, double[]>> getCentroidList() {
		
		ArrayList<SimpleEntry<Integer, double[]>> centroidList = new ArrayList<SimpleEntry<Integer, double[]>>();

		for (int i = 0; i < clusterList.size(); i++) {
			double[] centroid = getCentroids(clusterList.get(i));

			SimpleEntry<Integer, double[]> newPair = new SimpleEntry<Integer, double[]>(
					i, centroid);

            centroidList.add(newPair);
		}

		return centroidList;
	}
	 
	private void mergeCluster(int i, int j) {
		if (i == j) {
			return;
		}

		try {
			// merge mj into mi
			Map<String, double[]> mi = clusterList.get(i);
			Map<String, double[]> mj = clusterList.get(j);
			Iterator<Map.Entry<String, double[]>> it = mj.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, double[]> en = it.next();
				mi.put(en.getKey(), en.getValue());
			}

			this.clusterList.remove(j);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double[] getCentroids(Map<String, double[]> clusterMap) {
        Iterator<Map.Entry<String, double[]>> it = clusterMap.entrySet().iterator();
        int length = 1;
        double[] centroid = null;
        while (it.hasNext()) {
            Map.Entry<String, double[]> en = it.next();

            if(centroid == null) {
                length = en.getValue().length;
                centroid = new double[length];
            }

            for(int i = 0; i < length; i++) {
                centroid[i] += en.getValue()[i];
            }
        }

        try{
            for(int i = 0; i < length; i++) {
                centroid[i] /= clusterMap.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return centroid;
    }
	
	public ArrayList<Map<String, double[]>> converge() {

		System.out.println("cluster converge started");
		int loop = 0;

		while (true) {
			ArrayList<SimpleEntry<Integer, double[]>> centroidList = getCentroidList();
			SimpleEntry<Double, int[]> distancePair = findMinPair(centroidList);
			double distance = distancePair.getKey();
			int[] pair = distancePair.getValue();

			if (this.clusterList.size() <= this.minClusterSize) {
				break;
			}

			if (distance > this.convergeThreshold) {
				mergeCluster(pair[0], pair[1]);
			} else {
				break;
			}

			loop++;
			if (loop >= this.maxConvergeLoop) {
				System.out
						.println("Hitting upper limit, converge exit prematurely");
				break;
			} else {
				System.out.println("converge loop: " + loop);
			}
		}

		return this.clusterList;
	}
}

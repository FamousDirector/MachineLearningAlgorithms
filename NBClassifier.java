import java.util.HashMap;
import java.util.Map;

public class NBClassifier implements Classifier {

    ClassifierData data;
    public NBClassifier(ClassifierData classifierData)
    {
        reTrain(classifierData);
    }

    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;
        //get data for each unique class
        Map<String, int[]> classMap = new HashMap<>();
        for (int i = 0; i < classifierData.getClassArray().length; i++) {

            String classLabel = classifierData.getClassArray()[i];
            if(classMap.containsKey(classLabel))
            {
                int[] temp = classMap.get(classLabel);
                classMap.remove(classLabel);
                int[] newIndex = new int[temp.length+1];
                newIndex[0] = i;
                for (int j = 0; j < temp.length; j++) {
                    newIndex[j] = temp[j];
                }
                classMap.put(classLabel,newIndex);
            }
            else {
                classMap.put(classLabel,new int[]{i});
            }
        }

        //calculate probabilities for each class
        for (String classL : classMap.keySet()){
            int[] index = classMap.get(classL);
        }
        //create some sort of map of prob

        //maybe do something different for discreet?

    }

    public String classify(String[] featureArray) {
        return null;
    }

    private class Probabilities {

        double mean;
        double variance;
        double stdDev;

        Probabilities(double[] data){
            this.mean = getMean(data);
            this.variance = getMean(data);
            this.stdDev = getStdDev(data);
        }

        Probabilities(double mean,double variance, double stdDev)
        {
            this.mean = mean;
            this.variance = variance;
            this.stdDev = stdDev;
        }

        private double getMean(double[] data) {
            double sum = 0.0;
            for (double a : data)
                sum += a;
            return sum / data.length;
        }

        private double getVariance( double[] data) {
            double mean = getMean(data);
            double temp = 0;
            for (double a : data)
                temp += (a - mean) * (a - mean);
            return temp / (data.length - 1);
        }

        private double getStdDev(double[] data) {
            return Math.sqrt(getVariance(data));
        }

    }
}



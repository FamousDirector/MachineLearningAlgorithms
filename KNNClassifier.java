import java.util.*;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;

public class KNNClassifier implements Classifier{
    public static void main(String[] args){
        System.out.println("---KNN---");

//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//breast-cancer-wisconsin.data"; //10
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data"; //6
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//ecoli.data"; //8
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";


//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
////            fullDataset.removeDataColumn(0);
//            KNNClassifier knn = new KNNClassifier(fullDataset,5,2);
//            CrossValidation cv = CrossValidation.kFold(5, knn, fullDataset,10);
//            System.out.println("Error = " + cv.mean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
            ClassifierData partialDataset = ClassifierData.createSubsetOfClassifierData(fullDataset,0,1000);
            fullDataset.removeDataColumn(0);
            KNNClassifier knn = new KNNClassifier(partialDataset,3,true);
            CrossValidation cv = CrossValidation.kFold(5, knn, partialDataset,5);
            System.out.println("Error = " + cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ClassifierData classifierData;
    public int k;
    public double minkowskiDistance = 2;
    private double[] stds; //standard deviations used for Mahalanobis
    private boolean useMahalanobis = false;

    public KNNClassifier(ClassifierData classifierData, int k, boolean useMahalanobis)
    {
        this.classifierData = classifierData;
        this.k = k;
        this.useMahalanobis = useMahalanobis;
        if (useMahalanobis) {
            //prep
            this.stds = new double[classifierData.getNumberOfDataColumns()];
            for (int i = 0; i < stds.length; i++) {
                double[] temp = new double[classifierData.getNumberOfDataRows()];
                for (int j = 0; j < classifierData.getNumberOfDataRows(); j++) {
                    temp[j] = Double.parseDouble(classifierData.dataArray[j][i]);
                }
                this.stds[i] = getStdDev(temp);
            }
        }
    }

    public KNNClassifier(ClassifierData classifierData, int k)
    {
        this.classifierData = classifierData;
        this.k = k;
    }

    public KNNClassifier(ClassifierData classifierData, int k, double minkowskiDistance)
    {
        this.classifierData = classifierData;
        this.minkowskiDistance = minkowskiDistance;
        this.k = k;
    }

    public void reTrain(ClassifierData classifierData) {
        this.classifierData = classifierData;
        if (useMahalanobis) {
            //prep
            this.stds = new double[classifierData.getNumberOfDataColumns()];
            for (int i = 0; i < stds.length; i++) {
                double[] temp = new double[classifierData.getNumberOfDataRows()];
                for (int j = 0; j < classifierData.getNumberOfDataRows(); j++) {
                    temp[j] = Double.parseDouble(classifierData.dataArray[j][i]);
                }
                this.stds[i] = getStdDev(temp);
            }
        }
    }

    public String classify(String[] featureArray){
        //compute distances
        double distanceData[] = new double[this.classifierData.getNumberOfDataRows()];
        for (int i = 0; i < this.classifierData.getNumberOfDataRows(); i++) {
            distanceData[i] = computeDistance(featureArray,this.classifierData.getDataArray()[i]);
        }

        //normalize distances
        double normalizedDistanceData[] = normalizeDistance(distanceData);

        //find k lowest distances
        int[] indiceOfAscendingValue =  getAscendingValueIndice(normalizedDistanceData);

        String[] arrayOfClosestClasses = new String[getK()];

        //create array of k lowest distances
        for (int i = 0; i < getK(); i++) {
            arrayOfClosestClasses[i] = classifierData.getClassArray()[indiceOfAscendingValue[i]];
        }

        return returnMostCommonClass(arrayOfClosestClasses);
    }

    public ClassifierData getClassifierData() {
        return classifierData;
    }

    public void setClassifierData(ClassifierData classifierData) {
        this.classifierData = classifierData;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public Classifier clone(ClassifierData data) {
        return new KNNClassifier(data,this.k,this.minkowskiDistance);
    }

    private double computeDistance(String[] array1, String[] array2){
        //hamming distance
        if (this.minkowskiDistance <= 0)
        {
            return computeHammingDistanceFromArray(array1,array2);
        }
        else {
            double sum = 0;
            for (int i = 0; i < array1.length; i++) {
                if (isNumeric(array1[i]) && isNumeric(array2[i]))
                {
                    if (this.useMahalanobis)
                    {
                        double dif = Math.abs(Double.parseDouble(array1[i]) - Double.parseDouble(array2[i]))/Math.pow(this.stds[i], this.minkowskiDistance);
                        sum = sum + Math.pow(dif, this.minkowskiDistance);
                    }
                    else {  //Minkowski
                        double dif = Math.abs(Double.parseDouble(array1[i]) - Double.parseDouble(array2[i]));
                        sum = sum + Math.pow(dif, this.minkowskiDistance);
                    }
                }else {
                    //do hamming distance if values are string
                    sum = sum + computeHammingDistance(array1[i],array2[i]);
                }

                }
            return Math.pow(sum, ((double) 1)/this.minkowskiDistance);
        }
    }

    private static double computeHammingDistanceFromArray(String[] array1, String[] array2)    {
        double sum = 0;
        for (int i = 0; i < array1.length; i++) {
            sum = sum + computeHammingDistance(array1[i],array2[i]);
        }
        return sum;
    }

    /**
     * returns
     * @param string1
     * @param string2
     * @return
     */
    private static double computeHammingDistance(String string1, String string2) {
        //true hamming distance
//        char[] s1 = string1.toCharArray();
//        char[] s2 = string2.toCharArray();
//
//        int minLength = Math.min(s1.length, s2.length);
//        int maxLength = Math.max(s1.length, s2.length);
//
//        int result = 0;
//        for (int i=0; i<minLength; i++) {
//            if (s1[i] != s2[i]) result++;
//        }
//
//        result += maxLength - minLength;
//
//        return result;

        //not true hamming - just sees if they are equal
        if(string1.equals(string2))
        {
            return 1.0;
        }
        else
        {
            return 0.0;
        }
    }

    private double computeMahalanobisDistance()
    {

        return 0;
    }


    private static double[] normalizeDistance(double [] distances)    {
        double[] normalizedDistances = new double[distances.length];
        double max = 0;
        double min = Double.POSITIVE_INFINITY;

        //find min and max
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] > max) {
                max = distances[i];
            }
            if (distances[i] < min)
            {
                min = distances[i];
            }
        }

        for (int i = 0; i < distances.length; i++) {
            normalizedDistances[i] = (distances[i] - min) / (max - min);
        }
        return normalizedDistances;
    }
    private static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    private static int[] getAscendingValueIndice(double[] originalArray){
        int len = originalArray.length;

        double[] sortedCopy = originalArray.clone();
        int[] indices = new int[len];

        // Sort the copy
        Arrays.sort(sortedCopy);

        // Go through the original array: for the same index, fill the position where the
        // corresponding number is in the sorted array in the indices array
        for (int index = 0; index < len; index++)
            indices[Arrays.binarySearch(sortedCopy, originalArray[index])] = index;

        return indices;
    }

    private String returnMostCommonClass(String[] arrayOfClasses){
        int count = -1, tempCount;
        String popular = "";
        for (String c : classifierData.listOfClasses)
        {
            tempCount = 0;
            for (int j = 1; j < arrayOfClasses.length; j++)
            {
                if (c.equals(arrayOfClasses[j]))
                    tempCount++;
            }
            if (tempCount > count)
            {
                popular = c;
                count = tempCount;
            }
        }
        return popular;
    }

    private static double getMean(double[] data) {
        double sum = 0.0;
        for (double a : data)
            sum += a;
        return sum / data.length;
    }

    private static double getVariance( double[] data) {
        double mean = getMean(data);
        double temp = 0;
        for (double a : data)
            temp += (a - mean) * (a - mean);
        return temp / (data.length - 1);
    }

    private static double getStdDev(double[] data) {
        return Math.sqrt(getVariance(data));
    }
}

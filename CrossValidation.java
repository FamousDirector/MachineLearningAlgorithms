import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CrossValidation {

    double mean;
    double variance;
    double standardDeviation;

    public static void main(String[] args) {
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data";
        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
            ClassifierData partialDataset = ClassifierData.createSubsetOfClassifierData(fullDataset,0,100);
            NBClassifier knn = new NBClassifier(partialDataset);
            CrossValidation cv = kFold(3, knn, partialDataset,3);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CrossValidation(double[] data) {
        this.mean = getMean(data);
        this.standardDeviation = getStdDev(data);
        this.variance = getVariance(data);
    }
    public CrossValidation(double mean, double standardDeviation, double variance) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
    }

    public static CrossValidation kFold(int k, Classifier classifier, ClassifierData classifierData) {
        double[] errors = new double[k];
        int numberOfDataPointsPerK = classifierData.getDataArray().length / k;

        //shuffle data
        classifierData.shuffle();

        for (int i = 0; i < k; i++) {
            int lowerBound = i * numberOfDataPointsPerK;
            int upperBound = (i + 1) * numberOfDataPointsPerK;

            ClassifierData lowerTrainingSet = ClassifierData.createSubsetOfClassifierData(classifierData, 0, lowerBound);
            ClassifierData upperTrainingSet = ClassifierData.createSubsetOfClassifierData(classifierData, upperBound, classifierData.getDataArray().length);
            ClassifierData fullTrainingSet = ClassifierData.concatenateClassifierData(lowerTrainingSet, upperTrainingSet);

            ClassifierData testSet = ClassifierData.createSubsetOfClassifierData(classifierData, lowerBound, upperBound);
            String testSetData[][] = testSet.getDataArray();
            String testSetClass[] = testSet.getClassArray();

            classifier.reTrain(fullTrainingSet);

            int incorrectClassCount = 0;
            for (int j = 0; j < testSet.getDataArray().length; j++) {
                String guessedClass = classifier.classify(testSetData[j]);
                String actualClass = testSetClass[j];
                if (!guessedClass.equals(actualClass)) {
                    ++incorrectClassCount;
                }
            }
            errors[i] = (double) incorrectClassCount / testSet.getDataArray().length;
        }
        return new CrossValidation(errors);
    }

    public static CrossValidation kFold(int k, Classifier classifier, ClassifierData classifierData, int totalReps) {
        double[] meanArray = new double[totalReps];
        double [] standardDeviationArray = new double[totalReps];
        double [] varianceArray = new double[totalReps];

        for (int i = 0; i < totalReps; i++) {
            CrossValidation cv = kFold(k, classifier, classifierData);
            meanArray[i] = cv.mean;
            standardDeviationArray[i] = cv.standardDeviation;
            varianceArray[i] = cv.variance;
        }

        CrossValidation crossValidation = new CrossValidation(getMean(meanArray),getMean(standardDeviationArray),getMean(varianceArray));
        return crossValidation;
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

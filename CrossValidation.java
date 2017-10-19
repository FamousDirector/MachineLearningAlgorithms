public class CrossValidation {

    public static void main(String[] args){
    double mean;
    double variance;
    double standardDeviation;

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

        for (int i = 0; i < k; i++) {
            int lowerBound = k * numberOfDataPointsPerK;
            int upperBound = (k + 1) * numberOfDataPointsPerK;

            ClassifierData lowerTrainingSet = ClassifierData.createSubsetOfClassifierData(classifierData, 0, lowerBound);
            ClassifierData upperTrainingSet = ClassifierData.createSubsetOfClassifierData(classifierData, upperBound, classifierData.getDataArray().length);
            ClassifierData fullTrainingSet = ClassifierData.concatenateClassifierData(lowerTrainingSet, upperTrainingSet);

            ClassifierData testSet = ClassifierData.createSubsetOfClassifierData(classifierData, lowerBound, upperBound);
            String testSetData[][] = testSet.getDataArray();
            String testSetClass[] = testSet.getClassArray();

            classifier.reTrain(fullTrainingSet);

            int incorrectClassCount = 0;
            for (int j = 0; j < testSet.getDataArray().length; j++) {
                String result = classifier.classify(testSetData[j]);
                if (result != testSetClass[j]) {
                    ++incorrectClassCount;
                }
            }
            errors[k] = incorrectClassCount / testSet.getDataArray().length;
        }


        return new CrossValidation(errors);
    }

    public static long kFold(int k, Classifier classifier){
        long averageAccuracy = 0;
        return averageAccuracy;
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

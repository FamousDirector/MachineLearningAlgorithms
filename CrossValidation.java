public class CrossValidation {

    public static void main(String[] args){
    double mean;
    double variance;
    double standardDeviation;


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

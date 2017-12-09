import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AdaBoost implements Classifier {

    public static void main(String[] args) {
        System.out.println("---AdaBoost---");

        //        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//breast-cancer-wisconsin.data"; //10
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data"; //6
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//ecoli.data"; //8
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";

        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 6);
//            fullDataset.removeDataColumn(0);
            AdaBoost ada = new AdaBoost(fullDataset, new NBClassifier(fullDataset, true),100);
            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 1);
            System.out.println("Error = " + cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
////            fullDataset.removeDataColumn(0);
//            AdaBoost ada = new AdaBoost(fullDataset, new IDThreeClassifier(fullDataset,false));
//            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 2);
//            System.out.println("Error = " + cv.mean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public Classifier weakClassifier;
    public ClassifierData data;
    HashMap<Integer, Double> rowWeights = new HashMap<>();
    HashMap<Integer, Double> modelWeights = new HashMap<>();
    HashMap<Integer, Classifier> weakClassifiers = new HashMap<>();
    private int maxIterations = 100;
    private int START_ITERATIONS = 100;

    public AdaBoost(ClassifierData data, Classifier classifier, int maxIterations) {
        this.data = data;
        this.weakClassifier = classifier;
        this.maxIterations = maxIterations;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.put(i, 1.0 / data.getNumberOfDataRows());
        }
        reTrain(data);
    }

    public AdaBoost(ClassifierData data, Classifier classifier) {
        this.data = data;
        this.weakClassifier = classifier;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.put(i, 1.0 / data.getNumberOfDataRows());
        }

        double prevError = 2;
        double error = 1;
        int iterations = START_ITERATIONS;
        while (prevError > error) {
            prevError = error;
            AdaBoost ada = new AdaBoost(data, classifier, iterations);
            CrossValidation cv = CrossValidation.kFold(2, ada, data, 5);
            error = cv.mean;
            System.out.println("Error: " + error + " --- Depth " + iterations); //debug
            iterations++;
        }

        //once best depth is known
        maxIterations = iterations - 1;
        reTrain(data);

    }

    public void reTrain(ClassifierData classifierData) {
        //prepare data
        this.data = classifierData;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.put(i, 1.0 / data.getNumberOfDataRows());
        }

        int numberOfIterations = 0;
        while (numberOfIterations < maxIterations) { //early termination

            //create new ClassifierData
            ClassifierData newData = new ClassifierData(data.getNumberOfDataColumns(), data.getDataArray(), data.getClassArray(), rowWeights);

            //prepare classifier
            Classifier classifier = weakClassifier.clone(newData);

            CrossValidation cv = CrossValidation.kFold(2, classifier, newData, 5);

            //test classifier
            double incorrectSum = 0;
            double totalSum = 0;
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                String[] featureArray = data.getDataArray()[i];
                if (!classifier.classify(featureArray).equals(data.getClassArray()[i])) //correct classification
                {
                    incorrectSum += rowWeights.get(i);
                }
                totalSum += rowWeights.get(i);
            }
            double errorRate = incorrectSum / totalSum;

//            System.out.println("Debug = " + errorRate + " Depth: " + numberOfIterations); //debug

            // set model weights
            setModelWeight(numberOfIterations, errorRate);

            // set new row weights
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                String[] featureArray = data.getDataArray()[i];
                if (!classifier.classify(featureArray).equals(data.getClassArray()[i])) //incorrect classification
                {
                    updateRowWeight(modelWeights.get(numberOfIterations), i);
                }
            }

            //normalize weights
            normalizeRowWeights();

            //add weak classifier to ensemble
            weakClassifiers.put(numberOfIterations, classifier);

            numberOfIterations++; //next
        }

    }

    public Classifier clone(ClassifierData data) {
        System.out.println("DON'T ADABOOST-ADABOOST");
        return null;
    }

    public String classify(String[] featureArray) {
        HashMap<String, Double> results = new HashMap<>();
        //get results
        for (int i = 0; i < weakClassifiers.size(); i++) {
            Classifier classifier = weakClassifiers.get(i);
            String guess = classifier.classify(featureArray);
            if (!results.containsValue(guess))
                results.put(guess, modelWeights.get(i));
            else
                results.replace(guess, results.get(guess) + modelWeights.get(i));
        }
        //take value with highest
        String bestGuess = "";
        double highestValue = -1;
        for (String guess : results.keySet()) {
            if (results.get(guess) > highestValue) {
                highestValue = results.get(guess);
                bestGuess = guess;
            }
        }
        return bestGuess;
    }

    private void setModelWeight(int i, double errorRate) {
        this.modelWeights.put(i, Math.log((1 - errorRate) / errorRate) + Math.log(data.listOfClasses.size() - 1));
    }

    private void updateRowWeight(double alpha, int row) {
        rowWeights.replace(row, rowWeights.get(row) * Math.exp(alpha));
    }

    private void normalizeRowWeights() {
        Double[] array = rowWeights.values().toArray(new Double[rowWeights.values().size()]);
        double[] normalizedArray = normalizeArray(array);
        for (int i = 0; i < rowWeights.size(); i++) {
            rowWeights.replace(i, normalizedArray[i]);
        }
    }

    private static double[] normalizeArray(Double[] distances) {
        double[] normalizedArray = new double[distances.length];
        double max = 0;
        double min = Double.POSITIVE_INFINITY;

        //find min and max
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] > max) {
                max = distances[i];
            }
            if (distances[i] < min) {
                min = distances[i];
            }
        }

        for (int i = 0; i < distances.length; i++) {
            normalizedArray[i] = (distances[i] - min) / (max - min);
        }
        return normalizedArray;
    }

    private static class NBClassifier implements Classifier {

        ClassifierData data;
        private Boolean isDiscrete = true;
        private HashMap<String, Double> priorMap = new HashMap<>();
        private HashMap<String, HashMap<Integer, Probabilities>> continuousProbabilityMap = new HashMap<>();
        private HashMap<String, HashMap<Integer, HashMap<String, Double>>> discreteProbabilityMap = new HashMap<>();

        public NBClassifier(ClassifierData classifierData) {
            reTrain(classifierData);
        }

        public NBClassifier(ClassifierData classifierData, Boolean isDiscrete) {
            this.isDiscrete = isDiscrete;
            reTrain(classifierData);
        }

        public void reTrain(ClassifierData classifierData) {
            this.data = classifierData;
            if(data.rowWeights.isEmpty())
            {
                for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                    this.data.rowWeights.put(i,1.0);
                }
            }

            if (!this.isDiscrete && !data.isDataNumeric()) {
                this.isDiscrete = true;
                System.out.println("Incorrect assumption, data is discrete");
            }

            //get data for each unique class
            for (String label : data.listOfClasses) {
                int labelCount = 0;
                for (int i = 0; i < data.classArray.length; i++) {
                    String c = data.classArray[i];
                    if (label.equals(c))
                        labelCount++;
                }
                double priorProb = ((double) labelCount) / data.classArray.length;
                priorMap.put(label, priorProb);

                if (this.isDiscrete) {
                    HashMap<Integer, HashMap<String, Double>> columnAttributeMap = new HashMap<>();
                    for (int i = 0; i < data.getNumberOfDataColumns(); i++) {
                        String[] col = data.flippedDataArray[i];
                        HashSet<String> uniqueAttributes = getUniqueAttributes(col);
                        HashMap<String, Double> attributeMap = new HashMap<>();

                        for (String attr : uniqueAttributes) {
                            double attrCount = 0;
                            double classAndAttrCount = 0;
                            for (int j = 0; j < col.length; j++) {
                                if (attr.equals(col[j])) {
                                    attrCount += data.rowWeights.get(i);
                                    if (label.equals(data.classArray[j])) {
                                        classAndAttrCount+= data.rowWeights.get(i);;
                                    }
                                }
                            }
                            attributeMap.put(attr, classAndAttrCount / attrCount);
                        }
                        columnAttributeMap.put(i, attributeMap);
                    }
                    this.discreteProbabilityMap.put(label, columnAttributeMap);
                } else //continuous
                {
                    HashMap<Integer, Probabilities> probabilityMap = new HashMap<>();
                    for (int i = 0; i < data.getNumberOfDataColumns(); i++) {
                        double[] col = ClassifierData.makeDataNumeric(data.flippedDataArray[i]);
                        probabilityMap.put(i, new Probabilities(col));
                    }
                    continuousProbabilityMap.put(label, probabilityMap);
                }
            }
        }

        public Classifier clone(ClassifierData data) {
            return new NBClassifier(data, this.isDiscrete);
        }

        public String classify(String[] featureArray) {
            double highestProb = -1;
            String likelyClass = "";
            for (String label : data.listOfClasses) {
                double newProb = priorMap.get(label);
                for (int i = 0; i < featureArray.length; i++) {

                    if (isDiscrete) {
                        String attr = featureArray[i];
                        double prob = discreteProbabilityMap.get(label).get(i).getOrDefault(attr, 0.0);
                        newProb = newProb * prob;

                    } else {
                        double val = Double.parseDouble(featureArray[i]);
                        double prob = continuousProbabilityMap.get(label).get(i).pdf(val);
                        newProb = newProb * prob;
                    }
                }
                if (highestProb < newProb) {
                    likelyClass = label;
                    highestProb = newProb;
                }
            }
            return likelyClass;
        }

        private HashSet<String> getUniqueAttributes(String[] array) {
            HashSet<String> set = new HashSet<>();
            for (int i = 0; i < array.length; i++) {
                set.add(array[i]);
            }

            return set;
        }

        private class Probabilities {

            double mean;
            double variance;
            double stdDev;

            Probabilities(double[] data) {
                this.mean = getMean(data);
                this.variance = getMean(data);
                this.stdDev = getStdDev(data);
            }

            Probabilities(double mean, double variance, double stdDev) {
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

            private double getVariance(double[] data) {
                double mean = getMean(data);
                double temp = 0;
                for (double a : data)
                    temp += (a - mean) * (a - mean);
                return temp / (data.length - 1);
            }

            private double getStdDev(double[] data) {
                return Math.sqrt(getVariance(data));
            }

            public double pdf(double x) {
                double exponent = Math.exp(-(Math.pow(x - mean, 2) / (2 * Math.pow(stdDev, 2))));
                double val = (1 / (Math.sqrt(2 * Math.PI) * stdDev)) * exponent;
                return val;
            }
        }
    }

}

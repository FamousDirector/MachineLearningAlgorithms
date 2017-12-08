import java.util.ArrayList;
import java.util.HashMap;

public class AdaBoost implements Classifier {
    public Classifier weakClassifier;
    public ClassifierData data;
    HashMap<Integer, Double> rowWeights = new HashMap<>();
    HashMap<Integer, Double> modelWeights = new HashMap<>();
    HashMap<Integer, Classifier> weakClassifiers = new HashMap<>();
    private int maxIterations = 1;
    private int START_ITERATIONS = 10;


    public static void main(String[] args) {
        System.out.println("---AdaBoost---");
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";
//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 4);
//            fullDataset.removeDataColumn(0);
//            AdaBoost ada = new AdaBoost(fullDataset,new NBClassifier(fullDataset,true));
//            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 10);
//            System.out.println("Error = " + cv.mean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data";
        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
            AdaBoost ada = new AdaBoost(fullDataset, new IDThreeClassifier(fullDataset,false));
            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 2);
            System.out.println("Error = " + cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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
            //get smallest weight to normalize
            double smallestWeight = Double.MAX_VALUE;
            for (int i = 0; i < rowWeights.size(); i++) {
                if (rowWeights.get(i) < smallestWeight) {
                    smallestWeight = rowWeights.get(i);
                }
            }
            //create data based on weights
            ArrayList<String> newDataClasses = new ArrayList<>();
            ArrayList<String[]> newDataArray = new ArrayList<>();
            //use same data multiple times according to weight
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                int timesToReAdd = (int) Math.ceil(rowWeights.get(i) / smallestWeight);
                for (int j = 0; j < timesToReAdd; j++) {
                    newDataClasses.add(data.classArray[i]);
                    newDataArray.add(data.dataArray[i]);
                }
            }

            //create new ClassifierData

            ClassifierData newData = new ClassifierData(data.getNumberOfDataColumns(), newDataArray.toArray(new String[newDataArray.size()][data.getNumberOfDataColumns()]), newDataClasses.toArray(new String[newDataClasses.size()]));

            //prepare classifier
            Classifier classifier = weakClassifier.clone(newData);

            CrossValidation cv = CrossValidation.kFold(2, classifier, data, 5);

            double debug = cv.mean;
            System.out.println("Debug = " + debug + " Depth: " + numberOfIterations);

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

}

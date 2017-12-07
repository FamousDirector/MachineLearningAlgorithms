import java.util.Dictionary;
import java.util.HashMap;

public class AdaBoost implements Classifier {
    public Classifier weakClassifier;
    public ClassifierData data;
    HashMap<Integer,Double> rowWeights = new HashMap<>();
    HashMap<Integer,Double> modelWeights = new HashMap<>();
    HashMap <Integer,Classifier> weakClassifiers = new HashMap<>();


    public static void main(String[] args) {
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data";
        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 6);
            ClassifierData partialDataset = ClassifierData.createSubsetOfClassifierData(fullDataset, 0, 10);
//            partialDataset.removeDataColumn(0);
            AdaBoost ada = new AdaBoost(partialDataset,new IDThreeClassifier(partialDataset,true));
            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 10);
            System.out.println(cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public AdaBoost(ClassifierData data, Classifier classifier)
    {
        this.data = data;
        this.weakClassifier = classifier;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.put(i,1.0/data.getNumberOfDataRows());
        }
        reTrain(data);
    }
    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;
        int numberOfIterations = 0;
        while (true){ //TODO some constraint
            //get smallest weight to normalize
            double smallestWeight = Double.MAX_VALUE;
            for (int i = 0; i < rowWeights.size(); i++) {
                if (rowWeights.get(i) < smallestWeight)
                {
                    smallestWeight = rowWeights.get(i);
                }
            }
            //create data based on weights
            //use same data multiple times according to weight

            ClassifierData newData = new ClassifierData(null,null,null);
            Classifier classifier = weakClassifier.clone(newData);
            double errorRate = CrossValidation.kFold(2, classifier, newData, 5).mean;
            setModelWeight(numberOfIterations,errorRate);
            numberOfIterations++;
        }

    }

    public Classifier clone(ClassifierData data) {
        System.out.println("DON'T ADABOOST-ADABOOST");
        return null;
    }

    public String classify(String[] featureArray) {
        HashMap <String,Double> results = new HashMap<>();
        //get results
        for (int i = 0; i < weakClassifiers.size(); i++) {
            Classifier classifier = weakClassifiers.get(i);
            String guess = classifier.classify(featureArray);
            if(!results.containsValue(guess))
                results.put(guess,modelWeights.get(i));
            else
                results.replace(guess,results.get(guess) + modelWeights.get(i);
        }
        //take value with highest
        String bestGuess = "";
        double highestValue = -1;
        for (String guess : results.keySet()){
            if(results.get(guess)> highestValue)
            {
                highestValue = results.get(guess);
                bestGuess = guess;
            }
        }
        return bestGuess;
    }

    private void setModelWeight(int i, double errorRate)
    {
        this.modelWeights.put(i,Math.log((1-errorRate)/errorRate));
    }

    private double updateWeight(double w, double errorRate, boolean correctClassification)
    {
        if (!correctClassification)

            return w * Math.exp(errorRate);
        else
                return w;
    }

}

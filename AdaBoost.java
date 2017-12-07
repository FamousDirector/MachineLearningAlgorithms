public class AdaBoost implements Classifier {
    public Classifier weakClassifier;
    public ClassifierData data;

    public static void main(String[] args) {
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data";
        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 6);
            ClassifierData partialDataset = ClassifierData.createSubsetOfClassifierData(fullDataset, 0, 10);
//            partialDataset.removeDataColumn(0);
            IDThreeClassifier id3 = new IDThreeClassifier(partialDataset,true);
            CrossValidation cv = CrossValidation.kFold(5, id3, fullDataset, 10);
            System.out.println(cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public AdaBoost(ClassifierData data, Classifier classifier)
    {
        this.data = data;
        this.weakClassifier = classifier;
        reTrain(data);
    }
    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;

    }

    public String classify(String[] featureArray) {
        return null;
    }

}

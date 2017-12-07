public class AdaBoost implements Classifier {
    public Classifier weakClassifier;
    public ClassifierData data;


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

public class KNNClassifier implements Classifier{
    private  ClassifierData classifierData;
    private int k;
    public KNNClassifier(ClassifierData classifierData, int k)
    {
        this.classifierData = classifierData;
        this.k = k;
    }

    public String classify(String[] featureArray ){
        //todo
        return null;
    }
}

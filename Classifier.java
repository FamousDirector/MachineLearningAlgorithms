public interface Classifier {
    public String classify(String[] featureArray );
    public void reTrain(ClassifierData classifierData);
    public Classifier clone(ClassifierData data);
}

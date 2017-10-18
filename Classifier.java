public interface Classifier {
    String classify(String[] featureArray );
    void reTrain(ClassifierData classifierData);
}

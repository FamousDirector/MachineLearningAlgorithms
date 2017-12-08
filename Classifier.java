/**
 * Interface for all the classifiers to be created
 */
public interface Classifier {
    /**
     * This method is used to classify data based on the features given
     * @param featureArray features used to classify the array
     * @return the guessed class based on the features given
     */
    public String classify(String[] featureArray );

    /**
     * Used to re-train the classifier on new data
     * @param classifierData used to re-train the classifier with new data
     */
    public void reTrain(ClassifierData classifierData);

    /**
     * Used to create a shallow copy
     * @param data used to train the new copy of the classifier
     * @return a shallow copy of the Classifier
     */
    public Classifier clone(ClassifierData data);
}

public class IDThree implements Classifier {

    private ClassifierData data;

    public IDThree(ClassifierData data)
    {
        reTrain(data);
    }

    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;
    }

    public String classify(String [] features)
    {return null;}

}

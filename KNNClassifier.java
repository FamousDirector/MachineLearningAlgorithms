public class KNNClassifier implements Classifier{
    public ClassifierData classifierData;
    public int k;

    public KNNClassifier(ClassifierData classifierData, int k)
    {
        this.classifierData = classifierData;
        this.k = k;
    }


    public void reTrain(ClassifierData classifierData) {
        this.classifierData = classifierData;
    }

    public String classify(String[] featureArray ){
        //todo
        return null;
    }

    public ClassifierData getClassifierData() {
        return classifierData;
    }

    public void setClassifierData(ClassifierData classifierData) {
        this.classifierData = classifierData;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}

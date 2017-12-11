import java.util.HashMap;
import java.util.HashSet;

public class NBClassifier implements Classifier {

    public static void main(String[] args) {
        System.out.println("---NaiveBayes---");


//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//breast-cancer-wisconsin.data"; //10
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data"; //6
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//ecoli.data"; //8
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";

        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 8);
            fullDataset.removeDataColumn(0);
            NBClassifier nb = new NBClassifier(fullDataset);
            CrossValidation cv = CrossValidation.kFold(5, nb, fullDataset, 10);
            System.out.println("Error = " + cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //simple test
//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 4);
//            fullDataset.removeDataColumn(0);
//            NBClassifier nb = new NBClassifier(fullDataset);
//            String c = nb.classify(new String[]{"Sunny","No","Rich"});
//            System.out.println(c);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    ClassifierData data;
    private HashMap<String, Double> priorMap = new HashMap<>();
    private HashMap<String, HashMap<Integer, HashMap<String, Double>>> discreteProbabilityMap = new HashMap<>();

    public NBClassifier(ClassifierData classifierData) {
        reTrain(classifierData);
    }

    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;

        //get data for each unique class
        for (String label : data.listOfClasses) {
            int labelCount = 0;
            for (int i = 0; i < data.classArray.length; i++) {
                String c = data.classArray[i];
                if (label.equals(c))
                    labelCount++;
            }
            double priorProb = ((double) labelCount) / data.classArray.length;
            priorMap.put(label, priorProb);

            HashMap<Integer, HashMap<String, Double>> columnAttributeMap = new HashMap<>();
            for (int i = 0; i < data.getNumberOfDataColumns(); i++) {
                String[] col = data.flippedDataArray[i];
                HashSet<String> uniqueAttributes = getUniqueAttributes(col);
                HashMap<String, Double> attributeMap = new HashMap<>();

                for (String attr : uniqueAttributes) {
                    int attrCount = 0;
                    int classAndAttrCount =0;
                    for (int j = 0; j < col.length; j++) {
                        if(attr.equals(col[j]))
                        {
                            attrCount++;
                            if (  label.equals(data.classArray[j])) {
                                classAndAttrCount++;
                            }
                        }
                    }
                    attributeMap.put(attr, ((double) classAndAttrCount) /attrCount);
                }
                columnAttributeMap.put(i, attributeMap);
            }
            this.discreteProbabilityMap.put(label, columnAttributeMap);
        }
    }

    public Classifier clone(ClassifierData data) {
        return new NBClassifier(data);
    }

    public String classify(String[] featureArray) {
        double highestProb = -1;
        String likelyClass = "";
        for (String label : data.listOfClasses) {
            double newProb = priorMap.get(label);
            for (int i = 0; i < featureArray.length; i++) {
                String attr = featureArray[i];
                double prob = discreteProbabilityMap.get(label).get(i).getOrDefault(attr,0.0);
                    newProb = newProb * prob;
            }
            if (highestProb < newProb) {
                likelyClass = label;
                highestProb = newProb;
            }
        }
        return likelyClass;
    }

    private HashSet<String> getUniqueAttributes(String[] array) {
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < array.length; i++) {
            set.add(array[i]);
        }

        return set;
    }
}



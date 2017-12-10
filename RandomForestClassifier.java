import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RandomForestClassifier implements Classifier {
    private ClassifierData data;
    private DecisionNode tree;
    private int numOfBags = 10;
    LinkedList<DecisionNode> trees = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("---RandomForest---");


//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//breast-cancer-wisconsin.data"; //10
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data"; //6
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//ecoli.data"; //8
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";

        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
            ClassifierData partialDataset = ClassifierData.createSubsetOfClassifierData(fullDataset, 0, 10);
//            partialDataset.removeDataColumn(0);
            RandomForestClassifier id3 = new RandomForestClassifier(partialDataset);
            CrossValidation cv = CrossValidation.kFold(5, id3, fullDataset, 10);
            System.out.println("Error: " +cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";
//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 4);
//            fullDataset.removeDataColumn(0);
//            RandomForestClassifier id3 = new RandomForestClassifier(fullDataset);
//            String c = id3.classify(new String[]{"Rainy","Yes","Poor"});
//            System.out.println(c);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public RandomForestClassifier(ClassifierData data)
    {
        reTrain(data);
    }


    public Classifier clone(ClassifierData data) {
        return new RandomForestClassifier(data);
    }

    public void reTrain(ClassifierData classifierData) {
        this.data = classifierData;

        //create forest
        for (int i = 0; i < this.numOfBags; i++) {
            trees.add(createTree());
        }
    }

    private DecisionNode createTree() {
        return new DecisionNode();
    }

    public String classify(String [] features) {
        HashMap<String,Integer> results = new HashMap<>();
        for (DecisionNode tree : trees){
            DecisionNode node = tree;
            while (!node.isLeaf) {
                if (node.children.containsKey(features[node.colToSplitOn])) {
                    node = node.children.get(features[node.colToSplitOn]);
                } else //just take the next node
                {
                    node = node.children.values().iterator().next();
                }

            }
            String result = node.leafClass;
            if(!results.containsKey(result))
            {
                results.put(result,1);
            }
            else
            {
                results.replace(result,results.get(result)+1);
            }
        }

        int highestCount = -1;
        String highestResult = "";

        for (String resultClass : results.keySet())
        {
            if(results.get(resultClass) > highestCount)
            {
                highestCount = results.get(resultClass);
                highestResult = resultClass;
            }
        }
        return highestResult;
    }

    private static double totalEntropy(String[] a, HashSet<String> l)
    {
        double total = 0;
        for (String label : l) {
            int count = 0;
            for (int j = 0; j < a.length; j++) {
                if(label.equals(a[j])){
                    count++;
                }
            }
            total += entropy(((double) count)/a.length);
        }
        return total;
    }

    private class DecisionNode
    {
        Boolean isLeaf = false;
        String leafClass;
        int colToSplitOn; //the column that gives the best entropy at this node moving forward
        String splitOnAttr; //the value the parent used to create this node
        HashMap<String, DecisionNode> children = new HashMap<>();
        int depth;
        double entropy;
        DecisionNode parent;
        HashSet<Integer> cols = new HashSet<>();
        HashSet<Integer> rows = new HashSet<>();

        DecisionNode(){ //root
            parent = null;
            depth = 0;
            for (Integer i = 0; i < data.getNumberOfDataRows(); i++) {
                rows.add(i);
            }
            for (Integer i = 0; i < data.getNumberOfDataColumns(); i++) {
                cols.add(i);
            }
            entropy = totalEntropy(data.classArray,data.listOfClasses);

            double highestGain = -1;
            int bestCol = -1;
            for (Integer col : cols) {
                String[] d = data.flippedDataArray[col];
                HashSet<String> attrs = new HashSet<String>(Arrays.asList(d));
                HashMap<String,Integer> classCount = new HashMap<>();
                double colEntropy = 0;
                for (String attr : attrs)
                {
                    int attrCount = 0;
                    double classEnt = 0;
                    for (Integer row : rows) {
                        String a = data.dataArray[row][col];
                        if(attr.equals(a))
                        {
                            attrCount++;
                            String c = data.classArray[row];
                            if(classCount.containsKey(c))
                            {
                                classCount.put(c,classCount.get(c)+1);
                            }
                            else{
                                classCount.put(c,1);
                            }
                        }
                    }
                    for (String c : classCount.keySet()) {
                        double p = ((double)classCount.get(c))/attrCount;
                        double e = entropy(p);
                        classEnt += e;
                    }
                    classCount.clear();
                    colEntropy += ((double) attrCount / rows.size()) * classEnt;
                }
                double gain = entropy - colEntropy;
                if(gain>highestGain) {
                    highestGain = gain;
                    bestCol = col;
                }
            }
            this.colToSplitOn = bestCol;
            HashSet<String> values = new HashSet<String>(Arrays.asList(data.flippedDataArray[bestCol]));
            for (String attr : values) {
                this.children.put(attr,new DecisionNode(this, attr));
            }

        }

        DecisionNode(DecisionNode parent, String splitOnAttr){
            this.splitOnAttr = splitOnAttr;
            this.parent = parent;
            this.cols = new HashSet<>(parent.cols);
            this.rows = new HashSet<>(parent.rows);
            depth = parent.depth +1;
//            System.out.println(depth);//debug todo

            //get rows that don't match attToSplitOn
            HashSet<Integer> toBeRemoved = new HashSet<>();
            for (Integer row : rows)
            {
                if (!splitOnAttr.equals(data.dataArray[row][parent.colToSplitOn]))
                    toBeRemoved.add(row);
            }
            rows.removeAll(toBeRemoved);
            //remove col
            cols.remove(parent.colToSplitOn);

            //see if pure
            HashSet<String> classesLeft = new HashSet<>();
            String[] classRowsLeft = new String[rows.size()];
            int i =0;
            for(Integer row : rows)
            {
                classesLeft.add(data.classArray[row]);
                classRowsLeft[i] = data.classArray[row];
                i++;
            }
            if ( classesLeft.size() ==1)
            {
                isLeaf = true;
                leafClass = classesLeft.iterator().next();
                return;
            }

            entropy = totalEntropy(classRowsLeft,classesLeft);

            if (entropy == (double)0)
            {
                isLeaf = true;
                leafClass = data.classArray[toBeRemoved.iterator().next()];
                return;
            }

            double highestGain = -1;
            int bestCol = -1;
            for (Integer col : cols) {
                String[] d = data.flippedDataArray[col];
                HashSet<String> attrs = new HashSet<String>(Arrays.asList(d));
                HashMap<String,Integer> classCount = new HashMap<>();
                double colEntropy = 0;
                for (String attr : attrs)
                {
                    int attrCount = 0;
                    double classEnt = 0;
                    for (Integer row : rows) {
                        String a = data.dataArray[row][col];
                        if(attr.equals(a))
                        {
                            attrCount++;
                            String c = data.classArray[row];
                            if(classCount.containsKey(c))
                            {
                                classCount.put(c,classCount.get(c)+1);
                            }
                            else{
                                classCount.put(c,1);
                            }
                        }
                    }
                    for (String c : classCount.keySet()) {
                        double p = ((double)classCount.get(c))/attrCount;
                        double e = entropy(p);
                        classEnt += e;
                    }
                    classCount.clear();
                    colEntropy += ((double) attrCount / rows.size()) * classEnt;
                }
                double gain = entropy - colEntropy;
                if(gain>highestGain) {
                    highestGain = gain;
                    bestCol = col;
                }
            }
            this.colToSplitOn = bestCol;
            HashSet<String> values = new HashSet<String>(Arrays.asList(data.flippedDataArray[bestCol]));
            for (String attr : values) {
                this.children.put(attr,new DecisionNode(this, attr));
            }
        }

    }

    public static double entropy(double p) {
        if (p == 0 || p == 1)
            return 0;
        return -p * (Math.log(p)/Math.log(2));
    }
}

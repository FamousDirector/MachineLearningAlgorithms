import java.util.*;

public class AdaBoostClassifier implements Classifier {

    public static void main(String[] args) {
        System.out.println("---AdaBoostClassifier---");

        //        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//breast-cancer-wisconsin.data"; //10
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data"; //6
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//ecoli.data"; //8
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data"; //0
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//test.data";

//        try {
//            ClassifierData fullDataset = new ClassifierData(samplePath, 6);
////            fullDataset.removeDataColumn(0);
//            AdaBoostClassifier ada = new AdaBoostClassifier(fullDataset, new NBClassifier(fullDataset, true),100);
//            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 1);
//            System.out.println("Error = " + cv.mean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            ClassifierData fullDataset = new ClassifierData(samplePath, 0);
//            fullDataset.removeDataColumn(0);
            AdaBoostClassifier ada = new AdaBoostClassifier(fullDataset, new IDThreeClassifier(fullDataset,false));
            CrossValidation cv = CrossValidation.kFold(5, ada, fullDataset, 2);
            System.out.println("Error = " + cv.mean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Classifier weakClassifier;
    public ClassifierData data;
    LinkedList<Double> rowWeights = new LinkedList<>();
    LinkedList<Double> modelWeights = new LinkedList<>();
    LinkedList<Classifier> weakClassifiers = new LinkedList<>();
    private int maxIterations = 100;
    private int START_ITERATIONS = 1;

    public AdaBoostClassifier(ClassifierData data, Classifier classifier, int maxIterations) {
        this.data = data;
        this.weakClassifier = classifier;
        this.maxIterations = maxIterations;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.add(i, 1.0 / data.getNumberOfDataRows());
        }
        reTrain(data);
    }

    public AdaBoostClassifier(ClassifierData data, Classifier classifier) {
        this.data = data;
        this.weakClassifier = classifier;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.add(i, 1.0 / data.getNumberOfDataRows());
        }

        double prevError = 2;
        double error = 1;
        int iterations = START_ITERATIONS;
        while (prevError > error) {
            prevError = error;
            AdaBoostClassifier ada = new AdaBoostClassifier(data, classifier, iterations);
            CrossValidation cv = CrossValidation.kFold(2, ada, data, 5);
            error = cv.mean;
            System.out.println("Error: " + error + " --- Depth " + iterations); //debug
            iterations++;
        }

        //once best depth is known
        maxIterations = iterations - 1;
        reTrain(data);

    }

    public void reTrain(ClassifierData classifierData) {
        //prepare data
        this.data = classifierData;
        for (int i = 0; i < data.getNumberOfDataRows(); i++) {
            rowWeights.add(i, 1.0 / data.getNumberOfDataRows());
        }

        int numberOfIterations = 0;
        while (numberOfIterations < maxIterations) { //early termination

            //create new ClassifierData
            ClassifierData newData = new ClassifierData(data.getNumberOfDataColumns(), data.getDataArray(), data.getClassArray(), (LinkedList<Double>) rowWeights.clone());

            //prepare classifier
            Classifier classifier = weakClassifier.clone(newData);

            //add weak classifier to ensemble
            weakClassifiers.add(classifier);

            //test classifier
            double incorrectSum = 0;
            double totalSum = 0;
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                String[] featureArray = data.getDataArray()[i];
                if (!classifier.classify(featureArray).equals(data.getClassArray()[i])) //correct classification
                {
                    incorrectSum += rowWeights.get(i);
                }
                totalSum += rowWeights.get(i);
            }
            double errorRate = incorrectSum / totalSum;

//            System.out.println("Debug = " + errorRate + " Depth: " + numberOfIterations); //debug

            // set model weights
            double newModelWeight = Math.log((1 - errorRate) / errorRate) + Math.log(data.listOfClasses.size() - 1);
            this.modelWeights.add(numberOfIterations, newModelWeight);

            // set new row weights
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                String[] featureArray = data.getDataArray()[i];
                if (!classifier.classify(featureArray).equals(data.getClassArray()[i])) //incorrect classification
                {
                    double newRowWeight = Math.exp(-newModelWeight) * rowWeights.get(i);
                    rowWeights.set(i,newRowWeight);
                }
            }

            //normalize weights
            normalizeRowWeights(rowWeights);

            numberOfIterations++; //next
        }

    }

    public Classifier clone(ClassifierData data) {
        System.out.println("DON'T ADABOOST-ADABOOST");
        return null;
    }

    public String classify(String[] featureArray) {
        HashMap<String, Double> results = new HashMap<>();
        //get results
        for (int i = 0; i < weakClassifiers.size(); i++) {
            Classifier classifier = weakClassifiers.get(i);
            String guess = classifier.classify(featureArray);
            if (!results.containsValue(guess))
                results.put(guess, modelWeights.get(i));
            else
                results.replace(guess, results.get(guess) + modelWeights.get(i));
        }
        //take value with highest
        String bestGuess = "";
        double highestValue = -1;
        for (String guess : results.keySet()) {
            if (results.get(guess) > highestValue) {
                highestValue = results.getOrDefault(guess,-1.0);
                bestGuess = guess;
            }
        }
        return bestGuess;
    }


    private void normalizeRowWeights(LinkedList<Double> rowWeights) {
        Double[] array = rowWeights.toArray(new Double[rowWeights.size()]);
        double[] normalizedArray = normalizeArray(array,rowWeights);
        for (int i = 0; i < this.rowWeights.size(); i++) {
            this.rowWeights.set(i, normalizedArray[i]);
        }
    }

    private double[] normalizeArray(Double[] inputArray, LinkedList<Double> rowWeights) { //normalize weights to equal 1
        double[] normalizedArray = new double[inputArray.length];
        double total = getTotalRowWeight(rowWeights);

        //normalize
        for (int i = 0; i < inputArray.length; i++) {
            normalizedArray[i] = inputArray[i]/total;
        }

        return normalizedArray;
    }

    private double getTotalRowWeight(LinkedList<Double> rowWeights)
    {
        double total = 0;
        for (int i = 0; i < rowWeights.size(); i++) {
            total += rowWeights.get(i);
        }
        return total;
    }

    private static class NBClassifier implements Classifier {

        ClassifierData data;
        private HashMap<String, Double> priorMap = new HashMap<>();
        private HashMap<String, HashMap<Integer, HashMap<String, Double>>> discreteProbabilityMap = new HashMap<>();

        public NBClassifier(ClassifierData classifierData) {
            reTrain(classifierData);
        }

        public void reTrain(ClassifierData classifierData) {
            this.data = classifierData;
            if(data.rowWeights.isEmpty())
            {
                for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                    this.data.rowWeights.add(i,1.0);
                }
            }

            //get data for each unique class
            for (String label : data.listOfClasses) {
                double labelCount = 0;
                double totalCount = 0;
                for (int i = 0; i < data.classArray.length; i++) {
                    String c = data.classArray[i];
                    totalCount += data.rowWeights.get(i);
                    if (label.equals(c))
                        labelCount += data.rowWeights.get(i);
                }
                double priorProb =  labelCount / totalCount;
                priorMap.put(label, priorProb);

                HashMap<Integer, HashMap<String, Double>> columnAttributeMap = new HashMap<>();
                for (int i = 0; i < data.getNumberOfDataColumns(); i++) {
                    String[] col = data.flippedDataArray[i];
                    HashSet<String> uniqueAttributes = getUniqueAttributes(col);
                    HashMap<String, Double> attributeMap = new HashMap<>();

                    for (String attr : uniqueAttributes) {
                        double attrCount = 0;
                        double classAndAttrCount = 0;
                        for (int j = 0; j < col.length; j++) {
                            if (attr.equals(col[j])) {
                                attrCount += data.rowWeights.get(i);
                                if (label.equals(data.classArray[j])) {
                                    classAndAttrCount+= data.rowWeights.get(i);
                                }
                            }
                        }
                        attributeMap.put(attr, classAndAttrCount / attrCount);
                    }
                    columnAttributeMap.put(i, attributeMap);
                }
                this.discreteProbabilityMap.put(label, columnAttributeMap);
            }
        }

        public Classifier clone(ClassifierData data) {
            return new NBClassifier(new ClassifierData(data.NumberOfDataColumns, data.getDataArray(),
                    data.getClassArray(), data.rowWeights));
        }

        public String classify(String[] featureArray) {
            double highestProb = -1;
            String likelyClass = "";
            for (String label : data.listOfClasses) {
                double newProb = priorMap.get(label);
                for (int i = 0; i < featureArray.length; i++) {
                    String attr = featureArray[i];
                    double prob = discreteProbabilityMap.get(label).get(i).getOrDefault(attr, 0.0);
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

        private class Probabilities {

            double mean;
            double variance;
            double stdDev;

            Probabilities(double[] data) {
                this.mean = getMean(data);
                this.variance = getMean(data);
                this.stdDev = getStdDev(data);
            }

            Probabilities(double mean, double variance, double stdDev) {
                this.mean = mean;
                this.variance = variance;
                this.stdDev = stdDev;
            }

            private double getMean(double[] data) {
                double sum = 0.0;
                for (double a : data)
                    sum += a;
                return sum / data.length;
            }

            private double getVariance(double[] data) {
                double mean = getMean(data);
                double temp = 0;
                for (double a : data)
                    temp += (a - mean) * (a - mean);
                return temp / (data.length - 1);
            }

            private double getStdDev(double[] data) {
                return Math.sqrt(getVariance(data));
            }

            public double pdf(double x) {
                double exponent = Math.exp(-(Math.pow(x - mean, 2) / (2 * Math.pow(stdDev, 2))));
                double val = (1 / (Math.sqrt(2 * Math.PI) * stdDev)) * exponent;
                return val;
            }
        }
    }

    private static class IDThreeClassifier implements Classifier {

        private ClassifierData data;
        private DecisionNode tree;
        private int maxDepth = -1;
        private boolean stopEarly = false;

        public IDThreeClassifier(ClassifierData data)
        {
            reTrain(data);
        }

        public IDThreeClassifier(ClassifierData data, int maxDepth)
        {
            this.stopEarly = false;
            createTree(data,maxDepth);
        }

        public IDThreeClassifier(ClassifierData data, boolean stopEarly)
        {
            this.stopEarly = stopEarly;
            reTrain(data);
        }

        public Classifier clone(ClassifierData data) {
            return new IDThreeClassifier(data,this.stopEarly);
        }

        public void createTree(ClassifierData classifierData, int maxDepth) {
            this.maxDepth = maxDepth;
            createTree(classifierData);
        }

        public void reTrain(ClassifierData classifierData) {
            this.data =classifierData;

            if(data.rowWeights.isEmpty())
            {
                for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                    this.data.rowWeights.add(i,1.0);
                }
            }

            if(!this.stopEarly)
            {
                createTree(classifierData);
            }
            else {
                double prevError = 2;
                double error = 1;
                maxDepth = 1;
                while (prevError > error)
                {
                    maxDepth++;
                    prevError = error;
                    IDThreeClassifier iDT = new IDThreeClassifier(classifierData,maxDepth);
                    CrossValidation cv = CrossValidation.kFold(2, iDT, classifierData, 5);
                    error = cv.mean;
//                System.out.println("Error: " + error + " --- Depth " + maxDepth); //debug
                }

                //once best depth is known
                createTree(classifierData,maxDepth-1);
            }
        }

        private void createTree(ClassifierData classifierData) {
            this.data = classifierData;
            this.tree = new DecisionNode();
        }

        public String classify(String [] features)
        {
            DecisionNode node = tree;
            while (!node.isLeaf) {
                if (node.children.containsKey(features[node.colToSplitOn])) {
                    node = node.children.get(features[node.colToSplitOn]);
                } else //just take the next node
                {
                    node = node.children.values().iterator().next();
                }

            }
            return node.leafClass;
        }

        private double totalEntropy(String[] a, HashSet<String> l, LinkedList<Double> rowWeights)
        {
            double total = 0;
            double weightSum = 0;
            for (String label : l) {
                double count = 0;
                for (int j = 0; j < a.length; j++) {
                    weightSum += rowWeights.get(j);
                    if(label.equals(a[j])){
                        count += rowWeights.get(j);
                    }
                }
                total += entropy((count)/weightSum);
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
            LinkedList<Double> rowWeights = new LinkedList<>();

            DecisionNode(){ //root
                parent = null;
                this.rowWeights = data.rowWeights;
                depth = 0;
                for (Integer i = 0; i < data.getNumberOfDataRows(); i++) {
                    rows.add(i);
                }
                for (Integer i = 0; i < data.getNumberOfDataColumns(); i++) {
                    cols.add(i);
                }
                entropy = totalEntropy(data.classArray,data.listOfClasses,data.rowWeights);

                if (cols.size() == 0) //no splitting left
                {
                    isLeaf = true;

                    int highestCount = -1;
                    String highestClass = "";
                    for (String c : data.listOfClasses) {
                        int count = 0;
                        for (String r : data.classArray) {
                            if (r.equals(c))
                                count++;
                        }
                        if(highestCount < count)
                        {
                            highestClass = c;
                            highestCount = count;
                        }
                    }
                    leafClass = highestClass;
                    return;
                }

                double highestGain = -1;
                int bestCol = -1;
                for (Integer col : cols) {
                    String[] d = data.flippedDataArray[col];
                    HashSet<String> attrs = new HashSet<String>(Arrays.asList(d));
                    HashMap<String,Double> classCount = new HashMap<>();
                    double colEntropy = 0;
                    for (String attr : attrs)
                    {
                        double attrCount = 0;
                        double classEnt = 0;
                        for (Integer row : rows) {
                            String a = data.dataArray[row][col];
                            if(attr.equals(a))
                            {
                                attrCount += data.rowWeights.get(row);
                                String c = data.classArray[row];
                                if(classCount.containsKey(c))
                                {
                                    classCount.put(c,classCount.get(c)+data.rowWeights.get(row));
                                }
                                else{
                                    classCount.put(c,data.rowWeights.get(row));
                                }
                            }
                        }
                        for (String c : classCount.keySet()) {
                            double p = (classCount.get(c))/attrCount;
                            double e = entropy(p);
                            classEnt += e;
                        }
                        classCount.clear();
                        colEntropy += ( attrCount / getTotalRowWeight()) * classEnt;
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
                this.rowWeights = new LinkedList<>(parent.rowWeights);
                depth = parent.depth +1;
//            System.out.println(depth);//debug todo

                //get rows that don't match attToSplitOn
                HashSet<Integer> toBeRemoved = new HashSet<>();;
                for (Integer row : rows)
                {
                    if (!splitOnAttr.equals(data.dataArray[row][parent.colToSplitOn])) {
                        toBeRemoved.add(row);
                        rowWeights.remove(row);
                    }
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

                entropy = totalEntropy(classRowsLeft,classesLeft, rowWeights);

                if (entropy == (double)0)
                {
                    isLeaf = true;
                    leafClass = data.classArray[toBeRemoved.iterator().next()];
                    return;
                }

                if (depth >= maxDepth) //early stop
                {
                    double mostCommonClassCount = -1;
                    String mostCommonClass = "";
                    for (String c :classesLeft){
                        double count = 0;
                        for(int row : rows){
                            if(c.equals(data.classArray[row]))
                                count += data.rowWeights.get(row);
                        }
                        if (count > mostCommonClassCount)
                        {
                            mostCommonClass = c;
                            mostCommonClassCount = count;
                        }
                    }
                    isLeaf = true;
                    leafClass = mostCommonClass;
                    return;
                }

                if (cols.size() == 0) //no splitting left
                {
                    isLeaf = true;

                    int highestCount = -1;
                    String highestClass = "";
                    for (String c : data.listOfClasses) {
                        int count = 0;
                        for (String r : data.classArray) {
                            if (r.equals(c))
                                count++;
                        }
                        if(highestCount < count)
                        {
                            highestClass = c;
                            highestCount = count;
                        }
                    }
                    leafClass = highestClass;
                    return;
                }

                double highestGain = -1;
                int bestCol = -1;
                for (Integer col : cols) {
                    String[] d = data.flippedDataArray[col];
                    HashSet<String> attrs = new HashSet<String>(Arrays.asList(d));
                    HashMap<String,Double> classCount = new HashMap<>();
                    double colEntropy = 0;
                    for (String attr : attrs)
                    {
                        double attrCount = 0;
                        double classEnt = 0;
                        for (Integer row : rows) {
                            String a = data.dataArray[row][col];
                            if(attr.equals(a))
                            {
                                attrCount+= data.rowWeights.get(row);
                                String c = data.classArray[row];
                                if(classCount.containsKey(c))
                                {
                                    classCount.put(c,classCount.get(c)+data.rowWeights.get(row));
                                }
                                else{
                                    classCount.put(c,data.rowWeights.get(row));
                                }
                            }
                        }
                        for (String c : classCount.keySet()) {
                            double p = classCount.get(c)/attrCount;
                            double e = entropy(p);
                            classEnt += e;
                        }
                        classCount.clear();
                        colEntropy += attrCount / getTotalRowWeight() * classEnt;
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

        private double getTotalRowWeight()
        {
            double total = 0;
            for (int i = 0; i < data.getNumberOfDataRows(); i++) {
                total += data.rowWeights.get(i);
            }
            return total;
        }

        private double entropy(double p) {
            if (p == 0 || p == 1)
                return 0;
            return -p * (Math.log(p)/Math.log(2));
        }
    }


}

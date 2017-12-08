import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
 */
public class ClassifierData {
    public int NumberOfDataRows;
    public int NumberOfDataColumns;
    public String[][] dataArray;
    public String[][] flippedDataArray;
    public String[] classArray;
    public HashSet<String> listOfClasses = new HashSet<>();
    private String missingValueString = "?";

    public static void main(String[] args){
//        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//car.data";
//        try {
//            HashMap<String,String> valuesToBeReplaced = new HashMap<String,String>();
//            valuesToBeReplaced.put("low","1");
//            valuesToBeReplaced.put("small","1");
//            valuesToBeReplaced.put("med","2");
//            valuesToBeReplaced.put("high","3");
//            valuesToBeReplaced.put("big","3");
//            valuesToBeReplaced.put("vhigh","4");
//            valuesToBeReplaced.put("5more","5.5");
//            valuesToBeReplaced.put("more","5.5");
//
//            ClassifierData newClassifierData = new ClassifierData(samplePath,6,valuesToBeReplaced);
//            System.out.println();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//mushroom.data";
        try {

            ClassifierData newClassifierData = new ClassifierData(samplePath,6);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Constructs a 2 dimensional array of values based upon the specified text file from @param dataFilePath
     * @param dataFilePath the path to the data file that
     * @param classRowPosition the zero indexed row of the class data
     * @throws IOException
     */
    public ClassifierData(String dataFilePath, int classRowPosition) throws IOException{
        this.NumberOfDataRows = countRows(dataFilePath);
        this.NumberOfDataColumns = countColumns(dataFilePath);
        this.dataArray = new String [NumberOfDataRows][NumberOfDataColumns];
        this.flippedDataArray = new String [NumberOfDataColumns][NumberOfDataRows];
        this.classArray = new String [NumberOfDataRows];

        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            int row = 0;
            int column = 0;
            int dataColumn = 0;
            byte[] c = new byte[1024];
            byte prevChar = '0';
            int readChars;
            while ((readChars = iStream.read(c)) != -1) { // while there is a non empty line in the file
                for (int i = 0; i < readChars; ++i) { //for every byte in the line
                    if (c[i] == '\n') { //if end of line
                        ++row;
                        column = 0;
                        dataColumn = 0;
                    }
                    else if ((c[i] == ',' || c[i] == ' ')) { //if column separator
                        if (prevChar != ' '){ //if this is a unique column separator
                            if (column != classRowPosition){
                                ++dataColumn;
                            }
                            ++column;
                        }
                    } else {
                        String newStringLetter = (new String(new byte[]{ c[i] }, "US-ASCII")); //convert byte to string

                        //System.out.println(row + " " + column + " " + newStringLetter); //debug
                        if(newStringLetter.matches("[A-Za-z0-9"+this.missingValueString +".]+")) { //ensure value is alpha numeric
                            if (column == classRowPosition) {
                                if (this.classArray[row] == null){
                                    this.classArray[row] = newStringLetter; //add value to array
                                }
                                else {
                                    this.classArray[row] = this.classArray[row] + newStringLetter; //add value to array
                                }
                            }
                            else {
                                if (this.dataArray[row][dataColumn] == null) {
                                    this.dataArray[row][dataColumn] = newStringLetter; //add value to array
                                }
                                else {
                                    this.dataArray[row][dataColumn] = this.dataArray[row][dataColumn] + newStringLetter; //add value to array
                                }
                            }
                        }

                    }
                    prevChar = c[i];
                }
            }
        } finally {
            iStream.close();
        }


        getListOfClasses();
        flipDataArray();

        //remove this.missingValueString
        replaceEmptyVales();
    }

    public ClassifierData(String dataFilePath, int classRowPosition, HashMap valuesToBeReplaced) throws IOException{
        this(dataFilePath,classRowPosition);
        replaceDataWithValue(valuesToBeReplaced);
    }

    public ClassifierData(int numberOfDataColumns,String[][] dataArray, String[] dataClasses) {
        this.NumberOfDataRows = dataArray.length;
        this.NumberOfDataColumns = numberOfDataColumns;
        this.dataArray = dataArray;
        this.classArray = dataClasses;
        this.getListOfClasses();
        this.flipDataArray();
    }

    public ClassifierData getSingleRowDataset(int i)
    {
        String[][] dataArray = new String[getNumberOfDataRows()][1];
        for (int j = 0; j < getNumberOfDataRows(); j++) {
            dataArray [j][0] = flippedDataArray[i][j];
        }
        return new ClassifierData(1,dataArray,classArray);
    }

    /**
     * Returns the number of data rows in the file specified by the @param dataFilePath
     * @param dataFilePath a string that is the path to the data file
     * @return an int of data rows provided in the data file
     * @throws IOException
     */
    public static int countRows(String dataFilePath) throws IOException {
        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean isEmpty = true;
            while ((readChars = iStream.read(c)) != -1) {
                isEmpty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') { //if end of line
                        ++count;
                    }
                    else{
                        //do nothing
                    }
                }
            }
            return (count == 0 && !isEmpty) ? 1 : count;
        } finally {
            iStream.close();
        }
    }

    /**
     * Returns the number of data columns in the file specified by the @param dataFilePath
     * @param dataFilePath a string that is the path to the data file
     * @return an int of data columns provided in the data file
     * @throws IOException
     */
    public static int countColumns(String dataFilePath) throws IOException {
        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean isEmpty = true;
            boolean isEndOfLine = false;
            while (((readChars = iStream.read(c)) != -1) && !isEndOfLine) {
                isEmpty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        isEndOfLine = true;
                        break;
                    }
                    else if ((c[i] == ',' || c[i] == ' ') && i > 0) {
                        if (c[i-1] != ' '){
                            ++count;
                        }
                        
                    } else {
                      //do nothing  
                    }
                }
            }
            return (count == 0 && !isEmpty) ? 1 : count;
        } finally {
            iStream.close();
        }
    }
    public void removeDataColumn(int colToBeRemoved)
    {
        String[][] newDataArray = new String[this.NumberOfDataRows][this.NumberOfDataColumns -1];
        try {
            if (this.dataArray != null && this.dataArray.length > 0 && this.dataArray[0].length > colToBeRemoved) {
                newDataArray = new String[this.dataArray.length][this.dataArray[0].length - 1];
                for (int i = 0; i < dataArray.length; i++) {
                    int newColIdx = 0;
                    for (int j = 0; j < this.dataArray[i].length; j++) {
                        if (j != colToBeRemoved) {
                            newDataArray[i][newColIdx] = this.dataArray[i][j];
                            newColIdx++;
                        }
                    }
                }
            }
            this.dataArray = newDataArray;
            this.NumberOfDataColumns = this.NumberOfDataColumns -1;
            flipDataArray();
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public void removeDataColumns(int[] colsToBeRemoved)
    {
        Arrays.sort(colsToBeRemoved);
        for (int i = 0; i < colsToBeRemoved.length; i++) {
            removeDataColumn(colsToBeRemoved[i]-i);
        }
    }


    public static ClassifierData concatenateClassifierData(ClassifierData classifierData1, ClassifierData classifierData2){
        String[][] array1and2 = new String[classifierData1.getNumberOfDataRows() + classifierData2.getNumberOfDataRows()][];
        String[][] array1 = classifierData1.getDataArray();
        String[][] array2 = classifierData2.getDataArray();
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);

        String[] array3and4 = new String[classifierData1.getNumberOfDataRows() + classifierData2.getNumberOfDataRows()];
        String[] array3 = classifierData1.getClassArray();
        String[] array4 = classifierData2.getClassArray();
        System.arraycopy(array3, 0, array3and4, 0, array3.length);
        System.arraycopy(array4, 0, array3and4, array3.length, array4.length);

        return new ClassifierData(classifierData1.getNumberOfDataColumns(),array1and2,array3and4);
    }

    public static ClassifierData createSubsetOfClassifierData(ClassifierData classifierData, int startRow, int endRow){
        if(endRow-startRow>classifierData.getNumberOfDataRows())
            return classifierData;
        String[] newC =  Arrays.copyOfRange(classifierData.getClassArray(),startRow,endRow);
        String[][] newD = Arrays.copyOfRange(classifierData.getDataArray(),startRow,endRow);
        return new ClassifierData(classifierData.getNumberOfDataColumns(),newD,newC);
    }

    private void replaceDataWithValue(HashMap valuesToBeReplaced){
        for (int i = 0; i < getNumberOfDataRows(); i++) {
            for (int j = 0; j < getNumberOfDataColumns(); j++) {
                String value = dataArray[i][j];
                if (valuesToBeReplaced.containsKey(value)){
                    dataArray[i][j] = valuesToBeReplaced.get(value).toString();
                }
            }
        }
    }

    private void replaceEmptyVales(){

        HashMap<String, String[]> averageValueMap = new HashMap<>();

        //fills map with most common values per class
        for(String label: this.listOfClasses) {
            String[] avgValues = new String[getNumberOfDataColumns()];
            for (int i = 0; i < getNumberOfDataColumns(); i++) {
                avgValues[i] = returnMostCommonStringPerClass(flippedDataArray[i],label);
            }
            averageValueMap.put(label,avgValues);
        }

        //replaces missingValueString with most common value of class
        for (int i = 0; i < getNumberOfDataRows(); i++) {
            for (int j = 0; j < getNumberOfDataColumns(); j++) {
                String value = dataArray[i][j];
                if(value.equals(this.missingValueString))
                {
                    String[] values = averageValueMap.get(classArray[i]);
                    values[j] = dataArray[i][j];
                }
            }
        }

        //flips data array with corrected data
        flipDataArray();

    }

    private void getListOfClasses() {
        for (int i = 0; i < classArray.length; i++) {
            this.listOfClasses.add(classArray[i]);
        }
    }

    private void flipDataArray() {
        this.flippedDataArray = new String[getNumberOfDataColumns()][getNumberOfDataRows()];

        //flips data array so it is [column][row] not [row][col]
        for (int j = 0; j < getNumberOfDataColumns(); j++) {
            for (int i = 0; i < getNumberOfDataRows(); i++) {
                flippedDataArray[j][i] = dataArray[i][j];
            }
        }
    }

    public boolean isDataNumeric()
    {
        for (int i = 0; i < getNumberOfDataRows(); i++) {
            for (int j = 0; j < getNumberOfDataColumns(); j++) {
                String s = dataArray[i][j];
                if(!isNumeric(s))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isDataNumeric(int column)
    {
        for (int i = 0; i < getNumberOfDataRows(); i++) {
            if(!isNumeric(getDataArray()[i][column]))
            {
                return false;
            }
        }
        return true;
    }

    public static double[] makeDataNumeric(String[] column)
    {
        double[] newCol = new double[column.length];
        for (int i = 0; i < column.length; i++) {
            newCol[i] = Double.parseDouble(column[i]);
        }
        return newCol;
    }


    private static boolean isNumeric(String s) {
        return s != null && s.matches("\\d*\\.?\\d*");
    }

    public int getNumberOfDataRows() {
        return NumberOfDataRows;
    }

    public void setNumberOfDataRows(int numberOfDataRows) {
        NumberOfDataRows = numberOfDataRows;
    }

    public int getNumberOfDataColumns() {
        return NumberOfDataColumns;
    }

    public void setNumberOfDataColumns(int numberOfDataColumns) {
        NumberOfDataColumns = numberOfDataColumns;
    }

    public String[][] getDataArray() {
        return dataArray;
    }

    public void setDataArray(String[][] dataArray) {
        this.dataArray = dataArray;
    }

    public String[] getClassArray() {
        return classArray;
    }

    public void setClassArray(String[] classArray) {
        this.classArray = classArray;
    }

    public void shuffle() {
        Random rnd = new Random();
        int size = this.getNumberOfDataRows();
        for (int i = size; i > 1; i--) {
            swapRow(i - 1, rnd.nextInt(i));
        }
    }

    private void swapRow(int i, int j) {
        //swap data rows
        String[] tmp1 = this.dataArray[i];
        this.dataArray[i] = this.dataArray[j];
        this.dataArray[j] = tmp1;

        //swap class rows
        String tmp2 = this.classArray[i];
        this.classArray[i] = this.classArray[j];
        this.classArray[j] = tmp2;
    }

    private static String returnMostCommonString(String[] arrayOfClasses){
        int count = 1, tempCount;
        String popular = arrayOfClasses[0];
        String temp = "";
        for (int i = 0; i < (arrayOfClasses.length - 1); i++)
        {
            temp = arrayOfClasses[i];
            tempCount = 0;
            for (int j = 1; j < arrayOfClasses.length; j++)
            {
                if (temp.equals(arrayOfClasses[j]))
                    tempCount++;
            }
            if (tempCount > count)
            {
                popular = temp;
                count = tempCount;
            }
        }
        return popular;
    }

    private String returnMostCommonStringPerClass(String[] arrayOfClasses, String classLabel){
        int count = 0, tempCount = 0;
        String popular = "";
        String temp = "";
        HashSet<String> evaluatedValues = new HashSet<>();

        for (int i = 0; i < (arrayOfClasses.length); i++)
        {
            temp = arrayOfClasses[i];
            if(temp.equals(this.missingValueString)) //ignore empty values
                continue;
            else if(!classLabel.equals(classArray[i])) //ignore wrong class
                continue;
            else if(evaluatedValues.contains(temp)) //avoid duplicate values
                continue;
            else {
                evaluatedValues.add(temp);
                tempCount = 0;
                for (int j = i; j < arrayOfClasses.length; j++) {
                    if (temp.equals(arrayOfClasses[j]))
                        tempCount++;
                }
                if (tempCount > count) {
                    popular = temp;
                    count = tempCount;
                }
            }
        }
        return popular;
    }
} 
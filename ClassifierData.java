import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ClassifierData {
    public int NumberOfDataRows;
    public int NumberOfDataColumns;
    public String[][] dataArray;
    public String[] classArray;

    public static void main(String[] args){
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data";
        try {
        ClassifierData newClassifierData = new ClassifierData(samplePath,0);

        System.out.println(newClassifierData.classArray[1]);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     *
     * Constructs a 2 dimensional array of values based upon the specified text file from @param dataFilePath
     * @param dataFilePath the path to the data file that
     * @throws IOException
     */
    public ClassifierData(String dataFilePath, int classRowPosition) throws IOException{
        this.NumberOfDataRows = countRows(dataFilePath);
        this.NumberOfDataColumns = countColumns(dataFilePath);

        this.dataArray = new String [NumberOfDataRows][NumberOfDataColumns];
        this.classArray = new String [NumberOfDataRows];

        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            int row = 0;
            int column = 0;
            int dataColumn = 0;
            byte[] c = new byte[1024];
            int readChars;
            while ((readChars = iStream.read(c)) != -1) { // while there is a non empty line in the file

                for (int i = 0; i < readChars; ++i) { //for every byte in the line
                    if (c[i] == '\n') { //if end of line
                        ++row;
                        column = 0;
                        dataColumn = 0;
                    }
                    else if ((c[i] == ',' || c[i] == ' ') && i > 0) { //if column separator
                        if (c[i-1] != ',' || c[i-1] != ' '){ //if this is a unique column separator
                            if (column != classRowPosition){
                                ++dataColumn;
                            }
                            ++column;

                        }
                        
                    } else {

                        String newStringLetter = (new String(new byte[]{ c[i] }, "US-ASCII")); //convert byte to string

                        //System.out.println(row + " " + column + " " + newStringLetter); //debug
                        if(newStringLetter.matches("[A-Za-z0-9]+")) { //ensure value is alpha numeric
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
                }
            }
        } finally {
            iStream.close();
        }
        

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
                        if (c[i-1] != ',' || c[i-1] != ' '){
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
} 
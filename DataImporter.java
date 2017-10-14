import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * DataImporter
 */
public class DataImporter {
    public int NumberOfRows;
    public int NumberOfColumns;
    public String[][] dataArray;

    public static void main(String[] args){
        String samplePath = "C://Users//james//Code//CS6735//MachineLearningAlgorithms//data//letter-recognition.data";
        try {
        DataImporter newData = new DataImporter(samplePath);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public DataImporter (String dataFilePath) throws IOException{
        this.NumberOfRows = countRows(dataFilePath);
        this.NumberOfColumns = countColumns(dataFilePath);

        this.dataArray = new String [NumberOfRows][NumberOfColumns];

        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            int row = 0;
            int column = 0;
            byte[] c = new byte[1024];
            int readChars = 0;
            while ((readChars = iStream.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++row;
                        column = 0;
                    }
                    else if ((c[i] == ',' || c[i] == ' ') && i > 0) {
                        if (c[i-1] != ',' || c[i-1] != ' '){
                            ++column;
                        }
                        
                    } else {

                        String newStringLetter = (new String(new byte[]{ c[i] }, "US-ASCII"));

                        //System.out.println(row + " " + column + " " + newStringLetter);
                        if(newStringLetter.matches("[A-Za-z0-9]+")) { //ensure value is alpha numeric
                            this.dataArray[row][column] = this.dataArray[row][column] + newStringLetter;
                        }
                    }
                }
            }
        } finally {
            iStream.close();
        }
        

    }

    public static int countRows(String dataFilePath) throws IOException {
        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            byte[] c = new byte[1024];
            int count = 1;
            int readChars = 0;
            boolean isEmpty = true;
            while ((readChars = iStream.read(c)) != -1) {
                isEmpty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
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

    public static int countColumns(String dataFilePath) throws IOException {
        InputStream iStream = new BufferedInputStream(new FileInputStream(dataFilePath));
        try {
            byte[] c = new byte[1024];
            int count = 1;
            int readChars = 0;
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
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {

  /**
   * Extracts the data from the file and turns it into an array
   * @return double[]
   * @throws IOException
   */
  public static double[] extractData(String fileName) throws IOException {
    RandomAccessFile raf = null;
    try {
      raf = new RandomAccessFile(fileName, "r");
      System.out.println("Found file " + fileName + " yippee!");
    } catch (IOException e) {
      System.out.println("Error reading file " + fileName + "\nExiting...");
      System.exit(1);
    }

    raf.seek(0); //reset file pointer
    int count = 0;
    String line;
    while ((line = raf.readLine()) != null) {
      line = line.trim();
      if (!line.isEmpty()) count++;
    }

    double[] rows = new double[count];
    raf.seek(0); // reset again
    int i = 0;
    while ((line = raf.readLine()) != null) {
      line = line.trim();
      if (line.isEmpty()) continue;
      rows[i++] = Double.parseDouble(line);
    }
    return rows;
  }
}

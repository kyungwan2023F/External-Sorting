import java.io.IOException;

public class ReplacementSelectionTest {

    public static void main(String[] args) throws IOException {
        String filename = "testData.bin";
        int numBlocks = 100; // Define the number of blocks for testing

        // Step 1: Create a binary file with random records
        ByteFile byteFile = new ByteFile(filename, numBlocks);
        byteFile.writeRandomRecords();

        Controller controller = new Controller(filename);

        controller.performSorting();

        // Step 3: Verify if the output file is sorted
        boolean isSorted = byteFile.isSorted(); 
        System.out.println();
        if (isSorted) {
            System.out.println("The file is sorted correctly.");
        } else {
            System.out.println("The file is not sorted correctly.");
        }
    }
    // ~ Fields ................................................................

    // ~ Constructors ..........................................................

    // ~Public Methods ........................................................

}

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Controller {
    // ~ Fields ................................................................
    private ReplacementSelection replacementSelection;
    private byte[] inputBuffer;
    private byte[] outputBuffer;
    private MinHeap<Record> minHeap;
    private FileParser fileParser;

    // ~ Constructors ..........................................................
    public Controller(String filename) throws IOException {
        // Initialize buffers
        this.inputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        this.outputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];

        // Initialize the file parser for reading the binary file
        this.fileParser = new FileParser(filename);

        // Initialize MinHeap with capacity for 8 blocks of records
        Record[] emptyHeapArray = new Record[ByteFile.RECORDS_PER_BLOCK * 8];
        this.minHeap = new MinHeap<>(emptyHeapArray, 0,
            ByteFile.RECORDS_PER_BLOCK * 8);

        // Populate heap with 8 blocks of records
        this.initializeHeap();

        // Initialize ReplacementSelection with input buffer, output buffer, and
        // minHeap
        this.replacementSelection = new ReplacementSelection(minHeap,
            inputBuffer, outputBuffer);
    }

    // ~Public Methods ........................................................
// private void initializeHeap() throws IOException {
// int recordsLoaded = 0;
//
// // Loop to read 8 blocks, and until heap is full
// while (recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8 && fileParser
// .readNextBlock(inputBuffer)) {
// ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);
//
// while (byteBuffer.hasRemaining()
// && recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8) {
// long recID = byteBuffer.getLong(); // Read 8 bytes for recID
// double key = byteBuffer.getDouble(); // Read 8 bytes for key
//
// Record record = new Record(recID, key); // Create the record
// minHeap.insert(record); // Insert record into the heap
// recordsLoaded++;
// }
// }
// }


    private void initializeHeap() throws IOException {
        // Define a buffer to hold 8 blocks of data
        byte[] largeInputBuffer = new byte[ByteFile.BYTES_PER_BLOCK * 8];

        // Use FileParser to read the first 8 blocks directly into the buffer
        if (fileParser.file.getFilePointer() == 0) { // Start from the beginning
            fileParser.file.readFully(largeInputBuffer);
        }

        // Wrap the buffer in a ByteBuffer for easy data access
        ByteBuffer byteBuffer = ByteBuffer.wrap(largeInputBuffer);

        // Iterate over each record in the buffer and add to the heap
        for (int rec = 0; rec < ByteFile.RECORDS_PER_BLOCK * 8; rec++) {
            long recID = byteBuffer.getLong(); // Read 8 bytes for recID
            double key = byteBuffer.getDouble(); // Read 8 bytes for key

            // Create and insert the record into the minHeap
            Record record = new Record(recID, key);
            minHeap.insert(record);
        }
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @throws IOException
     */
    public void replacementSelectionSort() throws IOException {
        replacementSelection.inMemorySort(fileParser);
//        if (fileParser.file.length() <= ByteFile.BYTES_PER_BLOCK * 8) {
//            replacementSelection.inMemorySort(fileParser);
//        }
//        else {
//            System.out.println("The file has more than 8 blocks.");
//        }
        this.report();
    }


    private void report() throws IOException {
        fileParser.file.seek(0);
        
        int recordsPerLine = 0; // Counter to track the number of records
                                // printed per line

        while (fileParser.readNextBlock(inputBuffer)) {
            // Read the first record of the block (16 bytes)
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);
            long recID = byteBuffer.getLong(); // Get the record ID
            double key = byteBuffer.getDouble(); // Get the key

            // Print the record (ID and key)
            System.out.print(recID + " " + key + " ");
            recordsPerLine++;

            // Print a new line after every 5 records
            if (recordsPerLine == 5) {
                System.out.println();
                recordsPerLine = 0;
            }
        }
        fileParser.close();
    }
}

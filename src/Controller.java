import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Controller class controls processes including initializing, sorting, and
 * reporting.
 *
 * @author Kyungwan Do, Jaeyoung Shin
 * @version 11/12/2024
 */

public class Controller {
    // ~ Fields ................................................................
    /**
     * The ReplacementSelection instance that handles the replacement selection
     * sorting algorithm and merge process.
     */
    private ReplacementSelection replacementSelection;

    /**
     * Buffer for reading input data blocks from the input file.
     */
    private byte[] inputBuffer;

    /**
     * Buffer for writing output data blocks to the output file.
     */
    private byte[] outputBuffer;

    /**
     * MinHeap structure to manage Record objects during sorting.
     */
    private MinHeap<Record> minHeap;

    /**
     * FileParser instance for parsing and reading data from the input file.
     */
    private FileParser fileParser;

    /**
     * FileParser instance for managing intermediate runs during sorting.
     */
    private FileParser runFileParser;

    /**
     * FileParser instance for handling the final merged runs.
     */
    private FileParser mergeFileParser;

    // ----------------------------------------------------------
    /**
     * Create a new Controller object.
     * 
     * @param filename
     *            string
     * @throws IOException
     */
    public Controller(String inputFilename) throws IOException {
        // Initialize buffers for reading and writing blocks
        this.inputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        this.outputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];

        // Initialize the input file parser to read the binary file
        this.fileParser = new FileParser(inputFilename);

        // Define filenames for intermediate runs and merged results
        String intermediateRunFilename = "intermediateRuns.bin";
        String mergeResultFilename = "mergedResult.bin";

        // Initialize the file parsers for writing the sorted output and merged
        // data
        this.runFileParser = new FileParser(intermediateRunFilename);
        this.mergeFileParser = new FileParser(mergeResultFilename);

        // Initialize MinHeap with capacity for 8 blocks of records
        Record[] emptyHeapArray = new Record[ByteFile.RECORDS_PER_BLOCK * 8];
        this.minHeap = new MinHeap<>(emptyHeapArray, 0,
            ByteFile.RECORDS_PER_BLOCK * 8);

        // Populate heap with initial blocks of records
        this.initializeHeap();

        // Initialize ReplacementSelection with input and output buffers, and
        // minHeap
        this.replacementSelection = new ReplacementSelection(minHeap,
            inputBuffer, outputBuffer);
    }


    // ----------------------------------------------------------
    /**
     * Initialize heap.
     * 
     * @throws IOException
     */
    private void initializeHeap() throws IOException {
        // Define a buffer to hold 8 blocks of data
        byte[] largeInputBuffer = new byte[ByteFile.BYTES_PER_BLOCK * 8];
        // Use FileParser to read the first 8 blocks directly into the buffer
        if (fileParser.getFile().getFilePointer() == 0) { // Start from the
                                                          // beginning
            fileParser.getFile().readFully(largeInputBuffer);
        }

        // Wrap the buffer in a ByteBuffer for easy data access
        ByteBuffer byteBuffer = ByteBuffer.wrap(largeInputBuffer);

        // Iterate over each record in the buffer and add to the heap
        for (int rec = 0; rec < ByteFile.RECORDS_PER_BLOCK * 8; rec++) {
            long recID = byteBuffer.getLong(); // Read 8 bytes for recID
            double key = byteBuffer.getDouble(); // Read 8 bytes for key

            // Create and insert the record into the minHeap
            Record record = new Record(recID, key, -1);
            minHeap.insert(record);
        }
    }


    // ----------------------------------------------------------
    /**
     * Performs sorting.
     * 
     * @throws IOException
     */
    public void performSorting() throws IOException {
        if (fileParser.getFile().length() <= ByteFile.BYTES_PER_BLOCK * 8) {
            replacementSelection.inMemorySort(fileParser);
        }
        else {
            // Phase 1: Perform Replacement Selection Sort to create initial
            // sorted
            // runs
            DLList initialRuns = replacementSelection
                .performReplacementSelection(fileParser, runFileParser);

            // Phase 2: Perform Recursive Multiway Merge to sort all runs into
            // one
            // sorted file
            // Perform recursive multiway merge on the initial runs until there
            // is only one run left
            runFileParser = new FileParser("intermediateRuns.bin");
            replacementSelection.recursiveMultiwayMerge(runFileParser,
                mergeFileParser, initialRuns);
        }
        this.report();
    }


    // ----------------------------------------------------------
    /**
     * Reports data in buffers.
     * 
     * @throws IOException
     */
    private void report() throws IOException {
        fileParser.getFile().seek(0);

        int recordsPerLine = 0; // Counter to track the number of records
                                // printed per line

        while (fileParser.readNextBlock(inputBuffer) != -1) {
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

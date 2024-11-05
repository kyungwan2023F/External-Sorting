import java.io.IOException;
import java.nio.ByteBuffer;

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
            inputBuffer, outputBuffer, fileParser);
    }


    // ~Public Methods ........................................................
//    private void initializeHeap() throws IOException {
//        int recordsLoaded = 0;
//
//        // Loop to read 8 blocks, and until heap is full
//        while (recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8 && fileParser
//            .readNextBlock(inputBuffer)) {
//            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);
//
//            while (byteBuffer.hasRemaining()
//                && recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8) {
//                long recID = byteBuffer.getLong(); // Read 8 bytes for recID
//                double key = byteBuffer.getDouble(); // Read 8 bytes for key
//
//                Record record = new Record(recID, key); // Create the record
//                minHeap.insert(record); // Insert record into the heap
//                recordsLoaded++;
//            }
//        }
//    }


    private void initializeHeap() throws IOException {
        byte[] largeInputBuffer = new byte[ByteFile.BYTES_PER_BLOCK * 8];

        if (fileParser.readNextBlock(largeInputBuffer)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(largeInputBuffer);

            // Iterate over each record
            for (int rec = 0; rec < ByteFile.RECORDS_PER_BLOCK * 8; rec++) {
                long recID = byteBuffer.getLong(); // 8 bytes for recID
                double key = byteBuffer.getDouble(); // 8 bytes for key

                // Create and insert record into the heap
                Record record = new Record(recID, key);
                minHeap.insert(record);
            }
        }
    }


    public void replacementSelectionSort() throws IOException {
        System.out.println(fileParser.file.length());
        if (fileParser.file.length() <= ByteFile.BYTES_PER_BLOCK * 8) {
            replacementSelection.inMemorySort();
        }
        else {
            System.out.println("The file has more than 8 blocks.");
        }
        fileParser.close();
    }
}

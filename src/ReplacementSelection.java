import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReplacementSelection {
    // ~ Fields ................................................................
    private MinHeap<Record> minheap;
    private byte[] inputBuffer;
    private byte[] outputBuffer;
    private FileParser fileParser;

    // ~ Constructors ..........................................................
    // ----------------------------------------------------------
    /**
     * Create a new ReplacementSelection object.
     * 
     * @param minheap
     * @param inputBuffer
     * @param outputBuffer
     */
    public ReplacementSelection(FileParser parser) {
        Record[] emptyHeapArray = new Record[ByteFile.RECORDS_PER_BLOCK * 8];
        this.minheap = new MinHeap<>(emptyHeapArray, 0,
            ByteFile.RECORDS_PER_BLOCK * 8);

        this.inputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        this.outputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];

        this.fileParser = parser;
    }


    // ~Public Methods ........................................................
    public void initializeHeap() throws IOException {
        int recordsLoaded = 0;

        // Loop to read 8 blocks, and until heap is full
        while (recordsLoaded < ByteFile.RECORDS_PER_BLOCK && fileParser
            .readNextBlock(inputBuffer)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);

            for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK
                && recordsLoaded < ByteFile.RECORDS_PER_BLOCK; i++) {
                long recID = byteBuffer.getLong(); // Read 8 bytes for recID
                double key = byteBuffer.getDouble(); // Read 8 bytes for key

                Record record = new Record(recID, key); // Create the record
                minheap.insert(record); // Insert record into the heap
                recordsLoaded++;
            }
        }
    }
}

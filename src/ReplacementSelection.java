import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
    public ReplacementSelection(
        MinHeap<Record> minheap,
        byte[] inputBuffer,
        byte[] outputBuffer,
        FileParser fileParser) {
        this.minheap = minheap;
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.fileParser = fileParser;
    }
// public ReplacementSelection(FileParser parser) {
// Record[] emptyHeapArray = new Record[ByteFile.RECORDS_PER_BLOCK * 8];
// this.minheap = new MinHeap<>(emptyHeapArray, 0,
// ByteFile.RECORDS_PER_BLOCK * 8);
//
// this.inputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
// this.outputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
//
// this.fileParser = parser;
// }


    // ~Public Methods ........................................................
    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @throws IOException
     */
    public void initializeHeap() throws IOException {
        int recordsLoaded = 0;

        // Loop to read 8 blocks, and until heap is full
        while (recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8 && fileParser
            .readNextBlock(inputBuffer)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);

            while (byteBuffer.hasRemaining()
                && recordsLoaded < ByteFile.RECORDS_PER_BLOCK * 8) {
                long recID = byteBuffer.getLong(); // Read 8 bytes for recID
                double key = byteBuffer.getDouble(); // Read 8 bytes for key

                Record record = new Record(recID, key); // Create the record
                minheap.insert(record); // Insert record into the heap
                recordsLoaded++;
            }
        }
        fileParser.readNextBlock(inputBuffer);
    }


    public void inMemorySort() throws IOException {
        int outputIndex = 0;
        while (minheap.heapSize() != 0) {
            Record minRecord = minheap.removeMin();

            // Allocate ByteBuffer for ID and key separately
            ByteBuffer idBuffer = ByteBuffer.allocate(Long.BYTES);
            ByteBuffer keyBuffer = ByteBuffer.allocate(Double.BYTES);

            idBuffer.putLong(minRecord.getID());
            keyBuffer.putDouble(minRecord.getKey());

            // Copy to output buffer, starting at the current output index
            System.arraycopy(idBuffer.array(), 0, outputBuffer, outputIndex,
                Long.BYTES);
            outputIndex += Long.BYTES;
            System.arraycopy(keyBuffer.array(), 0, outputBuffer, outputIndex,
                Double.BYTES);
            outputIndex += Double.BYTES;

            if (outputIndex >= ByteFile.BYTES_PER_BLOCK) {
                List<Record> records = this.convertOutputBufferToRecords(
                    outputBuffer);
                fileParser.writeBlock(outputBuffer); // Write buffer to file
                outputIndex = 0; // Reset index for new data
            }
        }
    }


    public List<Record> convertOutputBufferToRecords(byte[] outputBuffer) {
        List<Record> records = new ArrayList<>();
        ByteBuffer byteBuffer = ByteBuffer.wrap(outputBuffer);

        while (byteBuffer.remaining() >= ByteFile.BYTES_PER_RECORD) {
            long recID = byteBuffer.getLong();
            double key = byteBuffer.getDouble();
            records.add(new Record(recID, key));
        }

        return records;
    }


    public void performReplacementSelection() throws IOException {
        Record minRecord = minheap.storeMin();
        int outputIndex = 0;

        // Allocate ByteBuffer for ID and key separately
        ByteBuffer idBuffer = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer keyBuffer = ByteBuffer.allocate(Double.BYTES);

        // Put the ID and key into their respective buffers
        idBuffer.putLong(minRecord.getID());
        keyBuffer.putDouble(minRecord.getKey());

        // Copy to output buffer, starting at the current output index
        System.arraycopy(idBuffer.array(), 0, outputBuffer, outputIndex,
            Long.BYTES);
        outputIndex += Long.BYTES;
        System.arraycopy(keyBuffer.array(), 0, outputBuffer, outputIndex,
            Double.BYTES);
        outputIndex += Double.BYTES;
    }

}

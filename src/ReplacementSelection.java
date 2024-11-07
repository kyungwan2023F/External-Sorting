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
    private DLList<Run> runList;

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
        byte[] outputBuffer) {
        this.minheap = minheap;
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
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
     * @param fileParser
     *            the fileParser object so that we can write into existing file
     * 
     * @throws IOException
     */
    public void inMemorySort(FileParser fileParser) throws IOException {
        int outputIndex = 0;

        fileParser.file.seek(0);
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
                fileParser.writeBlock(outputBuffer); // Write buffer to file
                outputIndex = 0; // Reset index for new data
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @param outputBuffer
     * @return
     */
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


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @param fileParser
     * 
     * @throws IOException
     */
    public void performReplacementSelection(FileParser fileParser)
        throws IOException {
        int outputIndex = 0;
        int inputIndex = 0;
        int storedMins = 0;

        long start = 0; // Start position of the run
        long end = 0; // End position of the run

        while (fileParser.hasRemainingData()) {
            fileParser.readNextBlock(inputBuffer);
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);

            // Track the start position of the run
            start = end; // Start from the last end position

            while (minheap.heapSize() > 0) {
                if (inputIndex >= byteBuffer.capacity()) {
                    if (fileParser.readNextBlock(inputBuffer)) {
                        inputIndex = 0;
                    }
                    else {
                        break;
                    }
                }
                long recID = byteBuffer.getLong(); // Read 8 bytes for recID
                double key = byteBuffer.getDouble(); // Read 8 bytes for key

                Record record = new Record(recID, key); // Create a Record
                // object

                Record minRecord = minheap.removeMin();
                if (record.getKey() >= minRecord.getKey()) {
                    minheap.insert(record);
                }
                else {
                    minheap.insert(record);
                    minheap.storeMin(); // Hide the record from the current run
                    storedMins++;
                }
                inputIndex++;

                // Add minRecord to output buffer
                outputIndex = addToOutputBuffer(minRecord, outputIndex);

                // Update end position with each record processed
                end += ByteFile.BYTES_PER_RECORD;

                // Write output buffer to file when full
                if (outputIndex >= ByteFile.BYTES_PER_BLOCK) {
                    fileParser.writeBlock(outputBuffer);
                    outputIndex = 0; // Reset for next block
                }
            }
            if (outputIndex > 0) {
                fileParser.writeBlock(outputBuffer);
            }
            minheap.setHeapSize(storedMins);
            minheap.buildHeap(); // Rebuild heap based on underlying heap
                                 // array
            // Calculate run length
            long runLength = end - start;

            // Create and store the new Run object
            Run newRun = new Run(start, runLength, end);
            
            // Add the run to the list
            runList.add(newRun); 
        }

// while (byteBuffer.remaining() >= ByteFile.BYTES_PER_RECORD) {
// long recID = byteBuffer.getLong(); // Read 8 bytes for recID
// double key = byteBuffer.getDouble(); // Read 8 bytes for key
// Record record = new Record(recID, key); // Create a Record
// // object
//
// Record minRecord = minheap.removeMin();
// if (record.getKey() >= minRecord.getKey()) {
// minheap.insert(record);
// }
// else {
// minheap.insert(record);
// minheap.storeMin(); // Hide the record from the current run
// storedMins++;
// }
//
// // Add minRecord to output buffer
// outputIndex = addToOutputBuffer(minRecord, outputIndex);
//
// // Write output buffer to file when full
// if (outputIndex >= ByteFile.BYTES_PER_BLOCK) {
// fileParser.writeBlock(outputBuffer);
// outputIndex = 0; // Reset for next block
// }
//
// // Check if all records in heap are hidden
// if (minheap.heapSize() == 0) {
// fileParser.writeBlock(outputBuffer);
// minheap.setHeapSize(storedMins);
// minheap.buildHeap();
//
// }
// }
// if (outputIndex > 0) {
// fileParser.writeBlock(outputBuffer);
// }

    }


    private int addToOutputBuffer(Record record, int outputIndex)
        throws IOException {
        ByteBuffer idBuffer = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer keyBuffer = ByteBuffer.allocate(Double.BYTES);
        idBuffer.putLong(record.getID());
        keyBuffer.putDouble(record.getKey());

        System.arraycopy(idBuffer.array(), 0, outputBuffer, outputIndex,
            Long.BYTES);
        outputIndex += Long.BYTES;
        System.arraycopy(keyBuffer.array(), 0, outputBuffer, outputIndex,
            Double.BYTES);
        outputIndex += Double.BYTES;

        return outputIndex;
    }

}

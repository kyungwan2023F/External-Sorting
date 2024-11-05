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
     * @throws IOException
     */
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

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

        fileParser.getFile().seek(0);
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
            records.add(new Record(recID, key, -1));
        }

        return records;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @param inputParser
     * @param runFileParser
     * @return
     * @throws IOException
     */
    public DLList performReplacementSelection(
        FileParser inputParser,
        FileParser runFileParser)
        throws IOException {
        runFileParser.getFile().seek(0);
        int inputIndex = 0;
        int outputIndex = 0;
        int storedMins = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(0); // Empty buffer with
                                                        // zero capacity
        long position = inputParser.getFile().getFilePointer();
        long start = 0; // Start position of the run
        long end = 0; // End position of the run
        DLList runList = new DLList();
        int runNum = 0; // Start runNum at 0
        Record minRecord;

        while (inputParser.hasRemainingData() || minheap.heapSize() > 0) {
            // Track the start position of the run
            start = end; // Start from the last end position

            if (inputParser.hasRemainingData() && !byteBuffer.hasRemaining()) {
                inputParser.readNextBlock(inputBuffer);
                byteBuffer = ByteBuffer.wrap(inputBuffer); // 0 for new block
            }

            while (minheap.heapSize() > 0
                || storedMins >= ByteFile.RECORDS_PER_BLOCK * 8) {
                if (minheap.heapSize() > 0) {
                    if (!byteBuffer.hasRemaining() && !inputParser
                        .hasRemainingData()) {
                        minRecord = minheap.removeMin();
                    }
                    else {
                        minRecord = minheap.getMin();
                    }

                    outputIndex = addToOutputBuffer(minRecord, outputIndex);
                    end += ByteFile.BYTES_PER_RECORD;

                    if (outputIndex >= ByteFile.BYTES_PER_BLOCK) {
                        runFileParser.writeBlock(outputBuffer);
                        outputIndex = 0;
                    }

                    if (byteBuffer.remaining() >= ByteFile.BYTES_PER_RECORD) {
                        long recID = byteBuffer.getLong();
                        double key = byteBuffer.getDouble();
                        inputIndex += ByteFile.BYTES_PER_RECORD;

                        Record record = new Record(recID, key, -1);

                        if (record.getKey() >= minRecord.getKey()) {
                            minheap.replaceMin(record);
                        }
                        else {
                            minheap.replaceMin(record);
                            minheap.removeMin();
                            storedMins++;
                        }
                    }

                    if (!byteBuffer.hasRemaining() && inputParser
                        .hasRemainingData()) {
                        inputParser.readNextBlock(inputBuffer);
                        byteBuffer = ByteBuffer.wrap(inputBuffer);
                    }
                }
                else {
                    // Heap is empty
                    // Break out to start a new run
                    break;
                }
            }

            if (outputIndex > 0) {
                // Set up a ByteBuffer around the outputBuffer to extract
                // records
                ByteBuffer bb = ByteBuffer.wrap(outputBuffer);

                // Calculate the number of records to write based on outputIndex
                int numRecords = outputIndex / ByteFile.BYTES_PER_RECORD;

                for (int i = 0; i < numRecords; i++) {
                    long recID = bb.getLong(); // Extract the long (recID)
                    double key = bb.getDouble(); // Extract the double (key)

                    // Write the record directly to the file
                    runFileParser.getFile().writeLong(recID);
                    runFileParser.getFile().writeDouble(key);
                }
                outputIndex = 0; // Reset for the next run
            }

            // Calculate run length and create new Run object
            long runLength = end - start;

            Run newRun = new Run(start, runLength, end, runNum);
            runList.add(newRun);
            runNum++;

            if (storedMins < ByteFile.RECORDS_PER_BLOCK * 8) {
                minheap.setHeapSize(storedMins);
                minheap.buildHeap();
            }
            else {
                minheap.setHeapSize(ByteFile.RECORDS_PER_BLOCK * 8);
                minheap.buildHeap();
            }
            storedMins = 0;
        }
        runFileParser.close();
        inputParser.replaceWith(runFileParser.getFileName());
        System.out.println(runList.size());
        return runList;
    }


    // ----------------------------------------------------------
    // ----------------------------------------------------------
    /**
     * Performs a recursive multiway merge on the run list by grouping runs into
     * batches of 8.
     * 
     * @param runFileParser
     *            the FileParser object to read run data.
     * @param mergeFileParser
     *            the FileParser object to write merged data.
     * @param runs
     *            the list of runs to merge.
     * @throws IOException
     */
    public void recursiveMultiwayMerge(
        FileParser runFileParser,
        FileParser mergeFileParser,
        DLList runs)
        throws IOException {

        // Base Case: If only one run remains, sorting is complete
        if (runs.size() <= 1) {
            return;
        }

        // Initialize a new list to hold merged runs after this pass
        DLList newRunList = new DLList();

        int totalRuns = runs.size();
        int index = 0;
        int groupRunNum = 0;

        while (index < totalRuns) {
            // Determine the number of runs in this batch (up to 8)
            int currentBatchSize = Math.min(8, totalRuns - index);

            // Extract the current batch of runs
            DLList currentBatch = new DLList();
            for (int i = 0; i < currentBatchSize; i++) {
                currentBatch.add(runs.get(index + i));
            }

            // Merge the current batch into a single run
            Run mergedRun = mergeRuns(runFileParser, mergeFileParser,
                currentBatch, groupRunNum);

            // Add the merged run to the new run list
            newRunList.add(mergedRun);

            // Move to the next batch
            index += currentBatchSize;

            groupRunNum++;
        }

        // Replace the old run list with the new merged run list
        runs.clear();
        for (int i = 0; i < newRunList.size(); i++) {
            runs.add(newRunList.get(i));
        }

        // Replace old run file with the newly merged run file
        runFileParser.replaceWith(mergeFileParser.getFileName());

        // Recursive call to handle the next pass of merging
        recursiveMultiwayMerge(runFileParser, mergeFileParser, runs);
    }


    private Run mergeRuns(
        FileParser runFileParser,
        FileParser mergeFileParser,
        DLList runsToMerge,
        int groupRunNum)
        throws IOException {
        long start = runsToMerge.get(0).getStartPosition();
        long end = start;
        long currentPos;
        // Initialize MinHeap for merging runs
        // Initialize MinHeap with capacity for 8 blocks of records
        runFileParser.getFile().seek(0);
        Record[] emptyHeapArray = new Record[ByteFile.RECORDS_PER_BLOCK * 8];
        this.minheap = new MinHeap<>(emptyHeapArray, 0,
            ByteFile.RECORDS_PER_BLOCK * 8);

        // Create buffers for reading from each run
        this.inputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        this.outputBuffer = new byte[ByteFile.BYTES_PER_BLOCK];

        // Load the first record from each run into the heap
        for (int i = 0; i < runsToMerge.size(); i++) {
            Run currentRun = runsToMerge.get(i);
            currentPos = currentRun.getCurrentPosition();
            runFileParser.getFile().seek(currentPos);

            // Read the first block of the current run
            // if (currentPos + bytesRead >= end)) {

            int bytesRead = runFileParser.readNextBlock(inputBuffer);
            // Wrap the buffer in a ByteBuffer for easy data access
            ByteBuffer byteBuffer = ByteBuffer.wrap(inputBuffer);

            // Iterate over each record in the buffer and add to the heap
            for (int rec = 0; rec < ByteFile.RECORDS_PER_BLOCK; rec++) {
                long recID = byteBuffer.getLong(); // Read 8 bytes for recID
                double key = byteBuffer.getDouble(); // Read 8 bytes for key

                // Check if this is the last record in the block
                if (rec == ByteFile.RECORDS_PER_BLOCK - 1) {
                    // If this is the last record, mark it with the runNum
                    Record record = new Record(recID, key, runsToMerge.get(i)
                        .getRunNum()); // put in run number for last record of
                                       // block in the run
                    currentRun.setCurrentPosition(currentPos
                        + ByteFile.BYTES_PER_BLOCK);
                    minheap.insert(record);
                }
                else {
                    // Regular record without run number
                    Record record = new Record(recID, key, -1); // -1 indicates
                                                                // no specific
                                                                // run number
                    minheap.insert(record);
                }
            }
        }

        // Set up output buffer for writing merged records
        ByteBuffer outputByteBuffer = ByteBuffer.wrap(outputBuffer);
        int outputIndex = 0;

        // Merge records from all runs
        while (minheap.heapSize() > 0) {
            Record minRecord = minheap.removeMin();
            int minRecordRunNum = minRecord.getRunNum();
            // Write the minimum record to the output buffer
            outputByteBuffer.putLong(minRecord.getID());
            outputByteBuffer.putDouble(minRecord.getKey());
            outputIndex += ByteFile.BYTES_PER_RECORD;

            if (minRecordRunNum != -1) {
                Run currentRun = runsToMerge.getRunByNumber(minRecordRunNum);
                long currentPositionRun = currentRun.getCurrentPosition();
                long endPositionRun = currentRun.getEndPosition();

                // Check if there are fewer bytes left than a full block
                if (currentPositionRun
                    + ByteFile.BYTES_PER_BLOCK >= endPositionRun) {
                    // Calculate how many bytes remain until the end of the run
                    int remainingBytes = (int)(endPositionRun
                        - currentPositionRun);
                    // Create a temporary byte array of size `remainingBytes`
                    byte[] tempBuffer = new byte[remainingBytes];
                    runFileParser.getFile().seek(currentPositionRun);
                    int bytesRead = runFileParser.getFile().read(tempBuffer, 0,
                        remainingBytes);

                    // If we successfully read the remaining bytes
                    if (bytesRead > 0) {
                        // Wrap the tempBuffer in a ByteBuffer for easy data
                        // access
                        ByteBuffer tempByteBuffer = ByteBuffer.wrap(tempBuffer);

                        // Iterate over the records in the temporary buffer and
                        // insert them into the minheap
                        while (tempByteBuffer
                            .remaining() >= ByteFile.BYTES_PER_RECORD) {
                            long recID = tempByteBuffer.getLong(); // Read 8
                                                                   // bytes for
                                                                   // recID
                            double key = tempByteBuffer.getDouble(); // Read 8
                                                                     // bytes
                                                                     // for key

                            // Insert the record into the minheap
                            Record record = new Record(recID, key,
                                minRecordRunNum); // Mark with runNum
                            minheap.insert(record);
                        }
                    }
                    // Update the current position of the run to indicate that
                    // we have read till the end
                    currentRun.setCurrentPosition(endPositionRun);
                }
                else {
                    // Regular case: More than a full block left to read
                    runFileParser.getFile().seek(currentPositionRun);
                    int bytesRead = runFileParser.readNextBlock(inputBuffer);

                    if (bytesRead > 0) {
                        ByteBuffer newBuffer = ByteBuffer.wrap(inputBuffer);

                        // Corrected code for reading from newBuffer
                        while (newBuffer
                            .remaining() >= ByteFile.BYTES_PER_RECORD) {
                            // Safely read the record only if there are enough
                            // bytes available
                            long recID = newBuffer.getLong(); // Read 8 bytes
                                                              // for recID
                            double key = newBuffer.getDouble(); // Read 8 bytes
                                                                // for key

                            Record newRecord = new Record(recID, key,
                                minRecordRunNum);
                            minheap.insert(newRecord);
                        }
                        // Update the current position of the run after reading
                        // the block
                        currentRun.setCurrentPosition(currentPositionRun
                            + ByteFile.BYTES_PER_BLOCK);
                    }
                }

            }
            // Check if output buffer is full
            if (outputIndex >= ByteFile.BYTES_PER_BLOCK) {
                mergeFileParser.writeBlock(outputBuffer);
                outputByteBuffer.clear(); // Reset buffer for the next block
                end += ByteFile.BYTES_PER_BLOCK;
                outputIndex = 0;
            }

        }

        // Write any remaining records in the output buffer
        if (outputIndex > 0) {
            runFileParser.getFile().write(outputBuffer, 0, outputIndex);
            end += outputIndex;
            outputIndex = 0; // Reset
        }

        long runLength = end - start;

        // Create and return the new merged Run object
        return new Run(start, runLength, end, groupRunNum);
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class FileParser {
    // ~ Fields ................................................................
    RandomAccessFile file;

    // ~ Constructors ..........................................................
    /**
     * Initializes the FileParser with the file to read.
     *
     * @param filename
     *            The name of the binary file to parse.
     * @throws IOException
     *             if the file cannot be opened.
     */
    public FileParser(String filename) throws IOException {
        this.file = new RandomAccessFile(new File(filename), "rw");
    }

    
    /**
     * Reads the next block of data into the provided buffer.
     *
     * @param buffer
     *            input buff of size 8192 to store one block of data.
     * @return true if data was read successfully, false if end of file was
     *         reached.
     * @throws IOException
     *             if there is an error reading the file or if end of file is
     *             reached unexpectedly.
     */
    public boolean readNextBlock(byte[] buffer) throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return false; // End of file reached
        }

        try {
            file.readFully(buffer); // Reads exactly buffer.length bytes into
                                    // buffer
            return true;
        }
        catch (IOException e) {
            // This exception could indicate that we're trying to read beyond
            // the end of the file
            return false;
        }
    }
    
    


    /**
     * Closes the file after reading is complete.
     *
     * @throws IOException
     *             if an error occurs while closing the file.
     */
    public void close() throws IOException {
        if (file != null) {
            file.close();
        }
    }


    /**
     * Writes a block of data from the provided buffer to the file.
     *
     * @param buffer
     *            output buffer containing data to write.
     * @throws IOException
     *             if there is an error writing to the file.
     */
    public void writeBlock(byte[] buffer) throws IOException {
        // Set up a ByteBuffer around the buffer
        ByteBuffer bb = ByteBuffer.wrap(buffer);

        // Iterate over each record in the buffer and write them individually
        for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK; i++) {
            long recID = bb.getLong();
            double key = bb.getDouble();

            // Write the record directly to the file as bytes
            file.writeLong(recID);
            file.writeDouble(key);
        }
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return
     * @throws IOException
     */
    public boolean hasRemainingData() throws IOException {
        return file.getFilePointer() < file.length();
    }

}

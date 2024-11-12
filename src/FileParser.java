import java.io.*;
import java.nio.ByteBuffer;

public class FileParser {
    // ~ Fields ................................................................
    private RandomAccessFile file;
    private String filePath;

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
        this.filePath = filename;
    }


    /**
     * Returns the name of the file associated with this FileParser.
     *
     * @return The name of the file.
     */
    public String getFileName() {
        return this.filePath;
    }


    /**
     * Returns the name of the file associated with this FileParser.
     *
     * @return The name of the file.
     */
    public RandomAccessFile getFile() {
        return this.file;
    }


    /**
     * Replaces the current file with the sorted file.
     * 
     * @param newFilePath
     *            Path of the new file to replace the current file with.
     * @throws IOException
     *             if any file operation fails.
     */
    public void replaceWith(String newFilePath) throws IOException {
        File oldFile = new File(this.filePath);
        File newFile = new File(newFilePath);

        // Close the current file before performing file operations
        this.close();
        newFile.close();

        // Delete the old file if it exists
        if (oldFile.exists()) {
            if (!oldFile.delete()) {
                throw new IOException("Failed to delete the old file.");
            }
        }
        else {
            throw new IOException("Old file doesn't exist.");
        }

        // Rename the new file to have the same name as the old file
        if (!newFile.renameTo(oldFile)) {
            throw new IOException(
                "Failed to rename the new file to the old file's name.");
        }

        // Reopen the file after replacement and reset the RandomAccessFile
        // instance
        this.file = new RandomAccessFile(oldFile, "rw");
    }


    /**
     * Reads the next block of data into the provided buffer.
     *
     * @param buffer
     *            input buffer to store data (exactly buffer.length bytes).
     * @return the number of bytes actually read, or -1 if end of file was
     *         reached before any bytes were read.
     * @throws IOException
     *             if there is an error reading the file.
     */
    public int readNextBlock(byte[] buffer) throws IOException {
        try {
            file.readFully(buffer);
            return buffer.length;
        }
        catch (EOFException e) {
            // Return -1 if end of file is reached before filling the buffer
            return -1;
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
     * 
     * @return
     * @throws IOException
     */
    public boolean hasRemainingData() throws IOException {
        return file.getFilePointer() < file.length();
    }

}

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;

// -------------------------------------------------------------------------
/**
 * FileParser class provides methods to read, write, replace, and check
 * remaining data in a binary file using RandomAccessFile operations.
 * 
 * @author Kyungwan Do, Jaeyoung Shin
 * @version Nov 12, 2024
 */
public class FileParser
{
    // ~ Fields ................................................................
    private RandomAccessFile file; // file
    private String filePath; // file path

    // ~ Constructors ..........................................................
    /**
     * Initializes the FileParser with the file to read.
     *
     * @param filename
     *            The name of the binary file to parse.
     * @throws IOException
     *             if the file cannot be opened.
     */
    public FileParser(String filename)
        throws IOException
    {
        this.file = new RandomAccessFile(new File(filename), "rw");
        this.filePath = filename;
    }


    /**
     * Returns the name of the file associated with this FileParser.
     *
     * @return The name of the file.
     */
    public String getFileName()
    {
        return this.filePath;
    }


    /**
     * Returns the name of the file associated with this FileParser.
     *
     * @return The name of the file.
     */
    public RandomAccessFile getFile()
    {
        return this.file;
    }


    /**
     * Replaces the current file with a new file by deleting the old file and
     * renaming the new file.
     *
     * @param newFilePath
     *            The path to the new file that will replace the current file.
     * @throws IOException
     *             If an I/O error occurs during the replacement process.
     */
    public void replaceWith(String newFilePath)
        throws IOException
    {
        // Close the current RandomAccessFile to release system resources
        this.close();

        File originalFile = new File(this.filePath);
        File newFile = new File(newFilePath);

        // Check if the new file exists before attempting to rename
        if (!newFile.exists())
        {
            throw new IOException(
                "The file to replace with does not exist: " + newFilePath);
        }

        // Delete the original file if it exists
        if (originalFile.exists() && !originalFile.delete())
        {
            throw new IOException(
                "Failed to delete the original file: " + this.filePath);
        }
        newFile.renameTo(originalFile);
        // Reopen the replaced file for further operations
        this.file = new RandomAccessFile(originalFile, "rw");
    }


    /**
     * Reads the next block of data into the provided buffer.
     *
     * @param buffer
     *            input buffer to store data (exactly buffer.length bytes).
     * @return the number of bytes actually read, or -1 if end of file was
     *             reached before any bytes were read.
     * @throws IOException
     *             if there is an error reading the file.
     */
    public int readNextBlock(byte[] buffer)
        throws IOException
    {
        try
        {
            file.readFully(buffer);
            return buffer.length;
        }
        catch (EOFException e)
        {
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
    public void close()
        throws IOException
    {
        if (file != null)
        {
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
    public void writeBlock(byte[] buffer)
        throws IOException
    {
        // Set up a ByteBuffer around the buffer
        ByteBuffer bb = ByteBuffer.wrap(buffer);

        // Iterate over each record in the buffer and write them individually
        for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK; i++)
        {
            long recID = bb.getLong();
            double key = bb.getDouble();

            // Write the record directly to the file as bytes
            file.writeLong(recID);
            file.writeDouble(key);
        }
    }


    // ----------------------------------------------------------
    /**
     * Checks if it has remaining data.
     * 
     * @return true or false
     * @throws IOException
     */
    public boolean hasRemainingData()
        throws IOException
    {
        return file.getFilePointer() < file.length();
    }

}

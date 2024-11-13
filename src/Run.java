// -------------------------------------------------------------------------
/**
 * Represents a sorted run in external sorting, having data about the
 * run's
 * position, length, and unique identifier. The `Run` class maintains the
 * starting
 * position, length, and end position of each run in a binary file and allows
 * tracking
 * of the current reading position within the run.
 * 
 * 
 * @author Kyungwan Do, Jaeyoung Shin
 * @version Nov 12, 2024
 */
public class Run {
    // ~ Fields ................................................................
    /**
     * The starting position of the run in the binary file.
     */
    private long startPosition;

    /**
     * The length of the run in bytes.
     */
    private long length;

    /**
     * The end position of the run in the binary file, calculated as
     * startPosition + length.
     */
    private long endPosition;

    /**
     * The current position within the run, used to track reading progress.
     */
    private long currentPos;

    /**
     * Unique number identifying each run, used to differentiate runs during
     * sorting and merging.
     */
    private int runNum;
    // ~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Run object with specified start position, length, end
     * position,
     * and a unique identifier.
     * 
     * @param startPosition
     *            the starting position of the run in the file
     * @param length
     *            the length of the run in bytes
     * @param endPosition
     *            the ending position of the run in the file
     * @param runNum
     *            the unique identifier number of this run
     */
    public Run(long startPosition, long length, long endPosition, int runNum) {
        this.startPosition = startPosition;
        this.length = length;
        this.endPosition = endPosition;
        this.currentPos = startPosition;
        this.runNum = runNum;
    }


    // ~Public Methods ........................................................
    // ----------------------------------------------------------
    /**
     * Returns the starting position of this run.
     * 
     * @return the starting position in the file
     */
    public long getStartPosition() {
        return startPosition;
    }


    // ----------------------------------------------------------
    /**
     * Returns the length of this run in bytes.
     * 
     * @return the length of the run
     */
    public long getLength() {
        return length;
    }


    // ----------------------------------------------------------
    /**
     * Returns the end position of this run.
     * 
     * @return the ending position in the file
     */
    public long getEndPosition() {
        return endPosition;
    }


    // ----------------------------------------------------------
    /**
     * Retrieves the current reading position within the run.
     * 
     * @return the current position in the file for this run
     */
    public long getCurrentPosition() {
        return currentPos;
    }


    // ----------------------------------------------------------
    /**
     * Updates the current reading position within the run.
     * 
     * @param pos
     *            the new current position within the run
     */
    public void setCurrentPosition(long pos) {
        this.currentPos = pos;
    }


    // ----------------------------------------------------------
    /**
     * Returns the unique identifier number of this run.
     * 
     * @return the run's unique identifier number
     */
    public int getRunNum() {
        return runNum;
    }
}

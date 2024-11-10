public class Run {
    // ~ Fields ................................................................
    private long startPosition;
    private long length;
    private long endPosition;
    private long currentPos;
    private int runNum; // Unique number to identify each run
    // ~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new Run object.
     * @param startPosition
     * @param length
     * @param endPosition
     * @param runNum
     */
    public Run(long startPosition, long length, long endPosition, int runNum) {
        this.startPosition = startPosition;
        this.length = length;
        this.endPosition = endPosition;
        this.currentPos = startPosition;
        this.runNum = runNum;
    }


    // ~Public Methods ........................................................
    public long getStartPosition() {
        return startPosition;
    }


    public long getLength() {
        return length;
    }


    public long getEndPosition() {
        return endPosition;
    }


    public long getCurrentPosition() {
        return currentPos;
    }


    public void setCurrentPosition(long pos) {
        this.currentPos = pos;
    }


    public int getRunNum() {
        return runNum;
    }


    /**
     * Checks if two Run objects are equal based on their run number.
     * 
     * @param run
     *            The other Run object to compare against.
     * @return True if the runs are equal, false otherwise.
     */
    public boolean isEquals(Run run) {
        return this.runNum == run.getRunNum();
    }
}

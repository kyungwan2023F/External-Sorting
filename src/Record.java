/**
 * Holds a single record
 *
 * @author CS Staff
 * @version Fall 2024
 */
public class Record implements Comparable<Record> {
    /**
     * 16 bytes per record
     */
    public static final int BYTES = 16;

    private long recID;
    private double key;
    private int runNum;

    /**
     * The constructor for the Record class
     *
     * @param recID
     *            record ID
     * @param key
     *            record key
     * @param runNum
     *            the runNumber a unique number identifying each run
     */
    public Record(long recID, double key, int runNum) {
        this.recID = recID;
        this.key = key;
        this.runNum = runNum;
    }


    // ----------------------------------------------------------
    /**
     * Return the ID value from the record
     *
     * @return record ID
     */
    public long getID() {
        return recID;
    }


    // ----------------------------------------------------------
    /**
     * Return the key value from the record
     *
     * @return record key
     */
    public double getKey() {
        return key;
    }


    // ----------------------------------------------------------
    /**
     * removes the runNum of the record
     * 
     * @return the runNum
     */
    public int getRunNum() {
        return runNum;
    }


    // ----------------------------------------------------------
    /**
     * Compare two records based on their keys
     *
     * @return int
     */
    @Override
    public int compareTo(Record toBeCompared) {
        return Double.compare(this.key, toBeCompared.key);
    }
}

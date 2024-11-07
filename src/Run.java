public class Run {
    //~ Fields ................................................................
    private long startPosition;
    private long length;
    private long endPosition;
    //~ Constructors ..........................................................
    public Run(long startPosition, long length, long endPosition) {
        this.startPosition = startPosition;
        this.length = length;
        this.endPosition = endPosition;
    }
    //~Public  Methods ........................................................
    public long getStartPosition() {
        return startPosition;
    }
    
    public long getLength() {
        return length;
    }
    
    public long getEndPosition() {
        return endPosition;
    }
}

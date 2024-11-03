public class Buffer {
    //~ Fields ................................................................
    private static final int BLOCK_SIZE = 8192; // bytes in a block
    private static final int RECORD_SIZE = 16; // bytes in a record
       
    private byte[] data; 
    
    
    //~ Constructors ..........................................................
    public Buffer() {
        data = new byte[BLOCK_SIZE];
    }
    //~Public  Methods ........................................................
    public byte[] getBlock(int block) {
        return data;
    }
    
    public int blocksize() {
        return BLOCK_SIZE;
    }
}

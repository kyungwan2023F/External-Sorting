public class BufferPool {
    //~ Fields ................................................................       
    private byte[] data; 
     
    //~ Constructors ..........................................................
    public BufferPool() {
        data = new byte[ByteFile.BYTES_PER_BLOCK];
    }
    //~Public  Methods ........................................................
    public byte[] getBlock() {
        return data;
    }
    
    public int blocksize() {
        return ByteFile.BYTES_PER_BLOCK;
    }
    
    public void loadBlock(byte[] newData)  {
        data = newData;
    }
}

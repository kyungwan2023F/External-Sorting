import student.TestCase;

/**
 * Test class for externalsort class.
 * 
 * @author Kyungwan Do, Jaeyoung Shin
 * @version 11/12/2024
 */
public class ExternalsortTest
    extends TestCase
{

    /**
     * set up for tests
     */
    public void setUp()
    {
        // nothing to set up.
    }


    /**
     * T
     */
    public void testExternalsort()
    {
        String[] args = { "MakeAFileUsingByteFile.data" };
        Externalsort.main(args);
    }
}

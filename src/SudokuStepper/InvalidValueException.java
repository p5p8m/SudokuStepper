package SudokuStepper;
/**
 * 
 */

/**
 * @author Pascal
 *
 */
public class InvalidValueException extends Exception
{

    /**
     * 
     */
    public InvalidValueException()
    {
    }

    /**
     * @param arg0
     */
    public InvalidValueException(String arg0)
    {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public InvalidValueException(Throwable arg0)
    {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidValueException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public InvalidValueException(String arg0, Throwable arg1, boolean arg2, boolean arg3)
    {
        super(arg0, arg1, arg2, arg3);
    }

}

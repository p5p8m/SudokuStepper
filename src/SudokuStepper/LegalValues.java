package SudokuStepper;
/**
 * 
 */

/**
 * @author Pascal
 *
 */
public enum LegalValues
{
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9);
    private final int val;

    private LegalValues(int value)
    {
        val = value;
    }

    private static LegalValues[] vals = null;

    public static LegalValues from(int i)
    {
        if (LegalValues.vals == null)
        {
            LegalValues.vals = LegalValues.values();
        }
        return LegalValues.vals[i - 1];
    }

    public int val()
    {
        return (val);
    }
}
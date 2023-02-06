package SudokuStepper;
/**
 * 
 */

/**
 * @author Pascal
 *
 */
public class LegalValues extends LegalValuesGenClass
{
    // ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
    // NINE(9);
    static
    {
        RECTANGLELENGTH = 3;
        CANDIDATESNUMBER = 9;
        CANDIDATESPERROW = 3;
        CANDIDATESPERCOL = 3;
        SINGLESUDOKUMAXROWS = 9;
        SINGLESUDOKUMAXCOLS = 9;
        CELLSPERROW = 3;
        CELLSPERCOL = 3;
        LOWBOUND = 1;
        HIGHBOUND = 9;
    }

    public LegalValues(int value) throws IllegalArgumentException
    {
        super(value);
    }

    // @Override
    public static LegalValues newInstance(int Value)
    {
        return (new LegalValues(Value));
    }
}
package SudokuStepper;

/**
 * @author Pascal For values from 1 to 16
 *
 */

public class LegalValues_16 extends LegalValuesGenClass
{
    // ONE(1),
    // TWO(2),
    // THREE(3),
    // FOUR(4),
    // FIVE(5),
    // SIX(6),
    // SEVEN(7),
    // EIGHT(8),
    // NINE(9),
    // TEN(10),
    // ELEVEN(11),
    // TWELVE(12),
    // THIRTEEN(13),
    // FOURTEEN(14),
    // FIFTEEN(15),
    // SIXTEEN(16);
    static
    {
        RECTANGLELENGTH = 4;
        CANDIDATESNUMBER = 16;
        CANDIDATESPERROW = 4;
        CANDIDATESPERCOL = 4;
        OVERALLMAXROWS = 16; // No samurai sudoku supported
        OVERALLMAXCOLS = 16; // No samurai sudoku supported
        SINGLESUDOKUMAXROWS = 16;
        SINGLESUDOKUMAXCOLS = 16;
        CELLSPERROW = 4;
        CELLSPERCOL = 4;
        LOWBOUND = 1;
        HIGHBOUND = 16;
    }

    public LegalValues_16(int value) throws IllegalArgumentException
    {
        super(value);
    }

    // @Override
    public static LegalValues_16 newInstance(int Value)
    {
        return (new LegalValues_16(Value));
    }
}

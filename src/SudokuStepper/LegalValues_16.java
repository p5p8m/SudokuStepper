package SudokuStepper;

/**
 * @author Pascal
 *
 */

public enum LegalValues_16 // implements LegalValuesInterface
{
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    ELEVEN(11),
    TWELVE(12),
    THIRTEEN(13),
    FOURTEEN(14),
    FIFTEEN(15),
    SIXTEEN(16);

    private final int        val;
    private static final int RECTANGLELENGTH     = 4;
    private static final int CANDIDATESNUMBER    = 16;
    private static final int CANDIDATESPERROW    = 4;
    private static final int CANDIDATESPERCOL    = 4;
    private static final int OVERALLMAXROWS      = 16; // No samurai sudoku supported
    private static final int OVERALLMAXCOLS      = 16; // No samurai sudoku supported
    private static final int SINGLESUDOKUMAXROWS = 16;
    private static final int SINGLESUDOKUMAXCOLS = 16;
    private static final int CELLSPERROW         = 4;
    private static final int CELLSPERCOL         = 4;

    private LegalValues_16(int value)
    {
        val = value;
    }

    private static LegalValues_16[] vals = null;

    public static LegalValues_16 from(int i)
    {
        if (LegalValues_16.vals == null)
        {
            LegalValues_16.vals = LegalValues_16.values();
        }
        return LegalValues_16.vals[i - 1];
    }

    public int val()
    {
        return (val);
    }

    public static int getCandidatesNumber()
    {
        return (CANDIDATESNUMBER);
    }

    public static int getRectangleLength()
    {
        return (RECTANGLELENGTH);
    }

    public static int getCandidatesPerRow()
    {
        return (CANDIDATESPERROW);
    }

    public static int getCandidatesPerCol()
    {
        return (CANDIDATESPERCOL);
    }

    public static int getMaxRows()
    {
        return (OVERALLMAXROWS);
    }

    public static int getMaxCols()
    {
        return (OVERALLMAXCOLS);
    }

    public static int getSingleSudokuMaxRows()
    {
        return (SINGLESUDOKUMAXROWS);
    }

    public static int getSingleSudokuMaxCols()
    {
        return (SINGLESUDOKUMAXCOLS);
    }

    public static int getCellsPerRow()
    {
        return (CELLSPERROW);
    }

    public static int getCellsPerCol()
    {
        return (CELLSPERCOL);
    }
}

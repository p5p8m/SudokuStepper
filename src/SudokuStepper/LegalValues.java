package SudokuStepper;
/**
 * 
 */

/**
 * @author Pascal
 *
 */
public enum LegalValues // implements LegalValuesInterface
{
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9);

    private final int        val;
    private static final int RECTANGLELENGTH     = 3;
    private static final int CANDIDATESNUMBER    = 9;
    private static final int CANDIDATESPERROW    = 3;
    private static final int CANDIDATESPERCOL    = 3;
    private static final int SINGLESUDOKUMAXROWS = 9;
    private static final int SINGLESUDOKUMAXCOLS = 9;
    private static final int CELLSPERROW         = 3;
    private static final int CELLSPERCOL         = 3;

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
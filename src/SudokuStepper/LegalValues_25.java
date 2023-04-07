package SudokuStepper;

import java.util.Arrays;
import java.util.List;

/**
 * @author Pascal For values from 1 to 25
 *
 */

public class LegalValues_25 extends LegalValuesGenClass
{
    private static int               RECTANGLELENGTH     = 5;
    private static int               CANDIDATESNUMBER    = 25;
    private static int               CANDIDATESPERROW    = 5;
    private static int               CANDIDATESPERCOL    = 5;
    private static int               OVERALLMAXROWS      = 25;                                                       // No
                                                                                                                     // samurai
                                                                                                                     // sudoku
                                                                                                                     // supported
    private static int               OVERALLMAXCOLS      = 25;                                                       // No
                                                                                                                     // samurai
                                                                                                                     // sudoku
                                                                                                                     // supported
    private static int               SINGLESUDOKUMAXROWS = 25;
    private static int               SINGLESUDOKUMAXCOLS = 25;
    private static int               CELLSPERROW         = 5;
    private static int               CELLSPERCOL         = 5;
    private static int               LOWBOUND            = 1;
    private static int               HIGHBOUND           = 25;
    // Only a to g expected as non-digit values
    public static final List<String> valuesPattern       = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p");
    private static Class<?>          ownClass            = null;
    static
    {
        try
        {
            ownClass = Class.forName("SudokuStepper.LegalValues_25");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        // alternatePatterns.put("00", "1");
        // alternatePatterns.put("01", "2");
        // alternatePatterns.put("02", "3");
        // alternatePatterns.put("03", "4");
        // alternatePatterns.put("04", "5");
        // alternatePatterns.put("05", "6");
        // alternatePatterns.put("07", "7");
        // alternatePatterns.put("08", "8");
        // alternatePatterns.put("09", "9");
        // alternatePatterns.put("x0", "a");
        // alternatePatterns.put("x1", "b");
        // alternatePatterns.put("x2", "c");
        // alternatePatterns.put("x3", "d");
        // alternatePatterns.put("x4", "e");
        // alternatePatterns.put("x5", "f");
        // alternatePatterns.put("x6", "g");
    }

    // public static HashMap getAlternatePatterns()
    // {
    // return (alternatePatterns);
    // }

    public static int getLowBound()
    {
        return (LOWBOUND);
    }

    public static int getHighBound()
    {
        return (HIGHBOUND);
    }

    // to
    // avoid
    // exception
    // @Override
    // public static Class getOwnClass()
    // {
    // Class retVal = ownClass;
    // return (retVal);
    // }

    public LegalValues_25(String value) throws IllegalArgumentException
    {
        super(ownClass, value);
    }

    public LegalValues_25(int value) throws IllegalArgumentException
    {
        super(ownClass, value);
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

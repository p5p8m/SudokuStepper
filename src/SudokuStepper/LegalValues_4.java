package SudokuStepper;

import java.util.Arrays;
import java.util.List;

public class LegalValues_4 extends LegalValuesGenClass
{
    private static int               RECTANGLELENGTH     = 2;
    private static int               CANDIDATESNUMBER    = 4;
    private static int               CANDIDATESPERROW    = 2;
    private static int               CANDIDATESPERCOL    = 2;
    private static int               OVERALLMAXROWS      = 4;                                // No
                                                                                             // samurai
                                                                                             // sudoku
                                                                                             // supported
    private static int               OVERALLMAXCOLS      = 4;                                // No
                                                                                             // samurai
                                                                                             // sudoku
                                                                                             // supported
    private static int               SINGLESUDOKUMAXROWS = 4;
    private static int               SINGLESUDOKUMAXCOLS = 4;
    private static int               CELLSPERROW         = 2;
    private static int               CELLSPERCOL         = 2;
    private static int               LOWBOUND            = 1;
    private static int               HIGHBOUND           = 4;
    // Only a to g expected as non-digit values
    public static final List<String> valuesPattern       = Arrays.asList("1", "2", "3", "4");
    // public static final HashMap<String, String> alternatePatterns = new
    // HashMap<String, String>(); // just
    private static Class<?>          ownClass            = null;
    static
    {
        try
        {
            ownClass = Class.forName("SudokuStepper.LegalValues_4");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        // alternatePatterns.put("00", "1");
        // alternatePatterns.put("01", "2");
        // alternatePatterns.put("02", "3");
        // alternatePatterns.put("03", "4");
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

    public LegalValues_4(String value) throws IllegalArgumentException
    {
        super(ownClass, value);
    }

    public LegalValues_4(int value) throws IllegalArgumentException
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

    // // @Override
    // public static LegalValues_16 newInstance(String val)
    // {
    // int valInt = 0;
    // if (val != null)
    // {
    // val = val.trim().toUpperCase();
    // if (val == "G")
    // {
    // valInt = 16;
    // }
    // else
    // {
    // valInt = Integer.parseInt(val, 16);
    // }
    // }
    // return (newInstance(valInt));
    // }
    //
    // // @Override
    // public static LegalValues_16 newInstance(int val)
    // {
    // return (new LegalValues_16(val));
    // }

}

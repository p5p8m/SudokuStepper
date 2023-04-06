package SudokuStepper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static int               RECTANGLELENGTH     = 4;
    private static int               CANDIDATESNUMBER    = 16;
    private static int               CANDIDATESPERROW    = 4;
    private static int               CANDIDATESPERCOL    = 4;
    private static int               OVERALLMAXROWS      = 16;                                                       // No
                                                                                                                     // samurai
                                                                                                                     // sudoku
                                                                                                                     // supported
    private static int               OVERALLMAXCOLS      = 16;                                                       // No
                                                                                                                     // samurai
                                                                                                                     // sudoku
                                                                                                                     // supported
    private static int               SINGLESUDOKUMAXROWS = 16;
    private static int               SINGLESUDOKUMAXCOLS = 16;
    private static int               CELLSPERROW         = 4;
    private static int               CELLSPERCOL         = 4;
    private static int               LOWBOUND            = 1;
    private static int               HIGHBOUND           = 16;
    // Only a to g expected as non-digit values
    public static final List<String> valuesPattern       = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g");
    // public static final HashMap<String, String> alternatePatterns = new
    // HashMap<String, String>(); // just
    private static Class             ownClass            = null;
    static
    {
        try
        {
            ownClass = Class.forName("SudokuStepper.LegalValues_16");
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
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

    public LegalValues_16(String value) throws IllegalArgumentException
    {
        super(ownClass, value);
    }

    public LegalValues_16(int value) throws IllegalArgumentException
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

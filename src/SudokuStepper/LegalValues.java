package SudokuStepper;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pascal
 *
 */
public class LegalValues extends LegalValuesGenClass
{
    // ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
    // NINE(9);
    private static int               RECTANGLELENGTH     = 3;
    private static int               CANDIDATESNUMBER    = 9;
    private static int               CANDIDATESPERROW    = 3;
    private static int               CANDIDATESPERCOL    = 3;
    private static int               OVERALLMAXROWS      = 21;                                                        // Samurai
                                                                                                                      // sudoku
                                                                                                                      // supported
    private static int               OVERALLMAXCOLS      = 21;                                                        // Samurai
                                                                                                                      // sudoku
                                                                                                                      // supported
    private static int               SINGLESUDOKUMAXROWS = 9;
    private static int               SINGLESUDOKUMAXCOLS = 9;
    private static int               CELLSPERROW         = 3;
    private static int               CELLSPERCOL         = 3;
    private static int               LOWBOUND            = 1;
    private static int               HIGHBOUND           = 9;
    public static final List<String> valuesPattern       = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    // public static final HashMap<String, String> alternatePatterns = new
    // HashMap<String, String>();
    private static Class             ownClass            = null;
    static
    {
        try
        {
            ownClass = Class.forName("SudokuStepper.LegalValues");
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

    // @Override
    // public static Class getOwnClass()
    // {
    // Class retVal = ownClass;
    // return (retVal);
    // }

    public LegalValues(String value) throws IllegalArgumentException
    {
        super(ownClass, value);
    }

    public LegalValues(int value) throws IllegalArgumentException
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
    // public static LegalValues newInstance(String val)
    // {
    // return (newInstance(Integer.parseInt(val)));
    // }
    //
    // // @Override
    // public static LegalValues newInstance(int val)
    // {
    // return (new LegalValues(val));
    // }
}
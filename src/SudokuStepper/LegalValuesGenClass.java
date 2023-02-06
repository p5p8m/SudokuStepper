package SudokuStepper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface LegalValuesConstr
{
    static LegalValuesGenClass newInstance(int val)
    {
        // TODO Auto-generated method stub
        return null;
    }
}

public abstract class LegalValuesGenClass implements LegalValuesConstr
{
    private final int    val;
    // All dummy initializations
    protected static int RECTANGLELENGTH     = 1;
    protected static int CANDIDATESNUMBER    = 1;
    protected static int CANDIDATESPERROW    = 1;
    protected static int CANDIDATESPERCOL    = 1;
    protected static int OVERALLMAXROWS      = 1; // No samurai sudoku supported
    protected static int OVERALLMAXCOLS      = 1; // No samurai sudoku supported
    protected static int SINGLESUDOKUMAXROWS = 1;
    protected static int SINGLESUDOKUMAXCOLS = 1;
    protected static int CELLSPERROW         = 1;
    protected static int CELLSPERCOL         = 1;
    protected static int LOWBOUND            = 1;
    protected static int HIGHBOUND           = 2;

    LegalValuesGenClass(int value) throws IllegalArgumentException
    {
        if (value < LOWBOUND || value > HIGHBOUND)
        {
            throw new IllegalArgumentException("Value " + Integer.toString(value) + " is not between "
                    + Integer.toString(LOWBOUND) + " and " + Integer.toString(HIGHBOUND));
        }
        val = value;
    }

    private static List<LegalValuesGenClass> vals = null;

    public static List<LegalValuesGenClass> values()
    {
        try
        {
            if (vals == null)
            {

                Class currClass = MethodHandles.lookup().getClass();
                System.out.println("LegalValue current class : " + currClass.getSimpleName());
                List<Integer> intVals = Stream.iterate(LOWBOUND, n -> n + 1).limit(HIGHBOUND - LOWBOUND + 1)
                        .collect(Collectors.toList());
                for (Integer intVal : intVals)
                {
                    vals.add((LegalValuesGenClass) currClass.getConstructor(int.class).newInstance(intVal));
                }
            }
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex)
        {
            System.out.println(ex.toString());
        }
        return (vals);
    }
    // public LegalValuesGenClass from(int i)
    // {
    // if (LegalValuesGenClass.vals == null)
    // {
    // LegalValuesGenClass.vals = LegalValuesGenClass.values();
    // }
    // return LegalValuesGenClass.vals[i - 1];
    // }

    @Override
    public boolean equals(Object o)
    {
        boolean retVal = false;
        if (o == this)
        {
            retVal = true;
        }
        else if (o instanceof LegalValuesGenClass)
        {
            LegalValuesGenClass v = (LegalValuesGenClass) o;
            retVal = v.val == this.val;
        }
        return (retVal);
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

    protected static LegalValuesGenClass newInstance(int parseInt)
    {
        // TODO Auto-generated method stub
        return null;
    }

}

package SudokuStepper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//interface LegalValuesConstr
//{
//    public static LegalValuesGenClass newInstance(int val)
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    public static LegalValuesGenClass newInstance(String val)
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
//}

public abstract class LegalValuesGenClass // implements LegalValuesConstr
{
    private final int      val;
    protected static Class ownClass = null;
    static
    {
        try
        {
            ownClass = Class.forName("SudokuStepper.LegalValuesGenClass");
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // All dummy initializations
    // protected static int RECTANGLELENGTH = 1;
    // protected static int CANDIDATESNUMBER = 1;
    // protected static int CANDIDATESPERROW = 1;
    // protected static int CANDIDATESPERCOL = 1;
    // protected static int OVERALLMAXROWS = 1; // No samurai sudoku supported
    // protected static int OVERALLMAXCOLS = 1; // No samurai sudoku supported
    // protected static int SINGLESUDOKUMAXROWS = 1;
    // protected static int SINGLESUDOKUMAXCOLS = 1;
    // protected static int CELLSPERROW = 1;
    // protected static int CELLSPERCOL = 1;
    // protected static int LOWBOUND = 1;
    // protected static int HIGHBOUND = 2;
    protected static int getBound(String methodName)
    {
        int retVal = 0;
        try
        {
            retVal = (int) ownClass.getMethod(methodName, null).invoke(null, null);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e)
        {
            e.printStackTrace();
        }
        return (retVal);
    }

    private static List<String> getValuesPatternList()
    {
        List<String> retVal = null;
        try
        {
            retVal = (List<String>) ownClass.getField("valuesPattern").get(null);
        }
        catch (IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        return (retVal);
    }

    LegalValuesGenClass(String valueStr) throws IllegalArgumentException
    {
        this(getValuesPatternList().indexOf(valueStr.toLowerCase()) + 1);
    }

    LegalValuesGenClass(int value) throws IllegalArgumentException
    {
        checkRange(value);
        val = value;
    }

    /**
     * @param value
     * @throws IllegalArgumentException
     */
    protected void checkRange(int value) throws IllegalArgumentException
    {
        int lowBound = getBound("getLowBound");
        int highBound = getBound("getHighBound");
        if (value < lowBound || value > highBound)
        {
            throw new IllegalArgumentException("Value " + Integer.toString(value) + " is not between "
                    + Integer.toString(lowBound) + " and " + Integer.toString(highBound));
        }
    }

    private static HashMap<Class, List<LegalValuesGenClass>> vals = new HashMap<Class, List<LegalValuesGenClass>>();

    public static List<LegalValuesGenClass> values(Class currClass)
    {
        List<LegalValuesGenClass> retVal = null;
        try
        {
            if (vals.containsKey(currClass))
            {
                retVal = vals.get(currClass);
            }
            else
            {

                // Class currClass = MethodHandles.lookup().getClass();
                int lowBound = getBound("getLowBound");
                int highBound = getBound("getHighBound");
                System.out.println("LegalValue current class : " + currClass.getSimpleName());
                List<Integer> intVals = Stream.iterate(lowBound, n -> n + 1).limit(highBound - lowBound + 1)
                        .collect(Collectors.toList());
                List<LegalValuesGenClass> newList = new ArrayList<LegalValuesGenClass>();
                for (Integer intVal : intVals)
                {
                    newList.add((LegalValuesGenClass) currClass.getConstructor(int.class).newInstance(intVal));
                }
                vals.put(currClass, newList);
                retVal = newList;
            }
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex)
        {
            ex.printStackTrace();
        }
        return (retVal);
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

    // public static int getCandidatesNumber()
    // {
    // return (CANDIDATESNUMBER);
    // }

    // public static int getRectangleLength()
    // {
    // return (RECTANGLELENGTH);
    // }
    //
    // public static int getCandidatesPerRow()
    // {
    // return (CANDIDATESPERROW);
    // }
    //
    // public static int getCandidatesPerCol()
    // {
    // return (CANDIDATESPERCOL);
    // }
    //
    // public static int getMaxRows()
    // {
    // return (OVERALLMAXROWS);
    // }
    //
    // public static int getMaxCols()
    // {
    // return (OVERALLMAXCOLS);
    // }
    //
    // public static int getSingleSudokuMaxRows()
    // {
    // return (SINGLESUDOKUMAXROWS);
    // }
    //
    // public static int getSingleSudokuMaxCols()
    // {
    // return (SINGLESUDOKUMAXCOLS);
    // }
    //
    // public static int getCellsPerRow()
    // {
    // return (CELLSPERROW);
    // }
    //
    // public static int getCellsPerCol()
    // {
    // return (CELLSPERCOL);
    // }

    // protected static LegalValuesGenClass newInstance(String val)
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // protected static LegalValuesGenClass newInstance(int val)
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }

    public static Class getOwnClass()
    {
        Class retVal = ownClass;
        return (retVal);
    }

    // return the correct display value (must match the newInstance functionnal
    // behaviour)
    public static String toDisplayString(int val)
    {
        String retVal = "";
        if (val == 16)
        {
            retVal = "g";
        }
        else
        {
            retVal = Integer.toHexString(val);
        }
        return (retVal);
    }

    // public static LegalValuesGenClass newInstance(int val)
    // {
    // return (null);
    // }
    //
    // public static LegalValuesGenClass newInstance(String val)
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }
}

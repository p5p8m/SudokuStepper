/**
 * 
 */
package SudokuStepper;

import java.util.ArrayList;

import SudokuStepper.Values.SudokuType;

/**
 * @author Pascal
 *
 */
public class MasterSudoku
{
    private SingleCellValue[][]  masterSudoku     = new SingleCellValue[AppMain.OVERALLMAXROWS][AppMain.OVERALLMAXCOLS];
    private ArrayList<SubSudoku> subSudokus       = new ArrayList<SubSudoku>();
    private Values               values           = null;
    private int                  candidatesNumber = 0;                                                                  // Dummy
                                                                                                                        // Initialization

    public MasterSudoku(SudokuType type, Values valuesIn, int candNbr)
    {
        setSudokuType(type);
        values = valuesIn;
        candidatesNumber = candNbr;
    }

    public void updateCandidatesNumber(Class legalValuesClass, int newVal)
    {
        candidatesNumber = newVal;
        reset(legalValuesClass);
    }

    public void setSudokuType(SudokuType type)
    {
        subSudokus.add(new SubSudoku(this, 0, 0));
        switch (type)
        {
        case SAMURAI:
            subSudokus.add(new SubSudoku(this, 12, 0));
            subSudokus.add(new SubSudoku(this, 0, 12));
            subSudokus.add(new SubSudoku(this, 12, 12));
            subSudokus.add(new SubSudoku(this, 6, 6));
            break;
        default:
            break;
        }

    }

    public Values getValues()
    {
        return (values);
    }

    public ArrayList<SubSudoku> getSubSudokus()
    {
        return (subSudokus);
    }

    public MasterSudoku(MasterSudoku src, SudokuType type)
    {
        for (int globalRow = 0; globalRow < src.getHeight(); globalRow++)
        {
            for (int globalCol = 0; globalCol < src.getWidth(); globalCol++)
            {
                masterSudoku[globalRow][globalCol] = new SingleCellValue(src.getRowCol(globalRow, globalCol));
            }
        }
        setSudokuType(type);
        this.values = src.getValues();
    }

    public SingleCellValue getRowCol(int globalRow, int globalCol)
    {
        return (masterSudoku[globalRow][globalCol]);
    }

    public int getHeight()
    {
        return (masterSudoku.length);
    }

    public int getWidth()
    {
        return (masterSudoku[0].length);
    }

    public SingleCellValue[][] getArray()
    {
        // TODO Auto-generated method stub
        return masterSudoku;
    }

    public void resetCell(Class legalValuesClass, int globalRow, int globalCol)
    {
        masterSudoku[globalRow][globalCol] = new SingleCellValue<LegalValuesGenClass>(legalValuesClass,
                candidatesNumber);
    }

    public void reset(Class legalValuesClass)
    {
        for (int globalRow = 0; globalRow < getHeight(); globalRow++)
        {
            for (int globalCol = 0; globalCol < getWidth(); globalCol++)
            {
                resetCell(legalValuesClass, globalRow, globalCol);
            }
        }
    }

    public boolean isRowColUsed(SudokuType type, int globalRow, int globalCol)
    {
        boolean retVal = true;
        switch (type)
        {
        case SAMURAI:
            if (globalRow / AppMain.getCellsPerRow() == 3
                    && (globalCol / AppMain.getCellsPerCol() < 2 || globalCol / AppMain.getCellsPerCol() > 4))
            {
                retVal = false;
            }
            else if (globalCol / AppMain.getCellsPerCol() == 3
                    && (globalRow / AppMain.getCellsPerRow() < 2 || globalRow / AppMain.getCellsPerRow() > 4))
            {
                retVal = false;
            }
            break;
        default:
            if (globalRow >= AppMain.getSingleSudokuMaxRows() || globalCol >= AppMain.getSingleSudokuMaxCols())
            {
                retVal = false;
            }
            break;
        }
        return retVal;
    }

    public ArrayList<SubSudoku> isRowColShared(int globalRow, int globalCol)
    {
        ArrayList<SubSudoku> retVal = new ArrayList<SubSudoku>();
        for (SubSudoku subSudoku : subSudokus)
        {
            if (subSudoku.isInSubSudoku(globalRow, globalCol))
            {
                retVal.add(subSudoku);
            }
        }
        return retVal;
    }
}

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
    private SingleCellValue[][]  masterSudoku = new SingleCellValue[AppMain.MAXROWS][AppMain.MAXCOLS];
    private ArrayList<SubSudoku> subSudokus   = new ArrayList<SubSudoku>();

    public MasterSudoku(SudokuType type)
    {
        setSudokuType(type);
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

    public void resetCell(int globalRow, int globalCol)
    {
        masterSudoku[globalRow][globalCol] = new SingleCellValue();
    }

    public void reset()
    {
        for (int globalRow = 0; globalRow < getHeight(); globalRow++)
        {
            for (int globalCol = 0; globalCol < getWidth(); globalCol++)
            {
                resetCell(globalRow, globalCol);
            }
        }
    }

    public boolean isRowColUsed(SudokuType type, int globalRow, int globalCol)
    {
        boolean retVal = true;
        switch (type)
        {
        case SAMURAI:
            if (globalRow / AppMain.CELLSPERROW == 3
                    && (globalCol / AppMain.CELLSPERCOL < 2 || globalCol / AppMain.CELLSPERCOL > 4))
            {
                retVal = false;
            }
            else if (globalCol / AppMain.CELLSPERCOL == 3
                    && (globalRow / AppMain.CELLSPERROW < 2 || globalRow / AppMain.CELLSPERROW > 4))
            {
                retVal = false;
            }
            break;
        default:
            if (globalRow >= AppMain.SINGLESUDOKUMAXROWS || globalCol >= AppMain.SINGLESUDOKUMAXCOLS)
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

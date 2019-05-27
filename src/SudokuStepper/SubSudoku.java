/**
 * 
 */
package SudokuStepper;

/**
 * @author Pascal Does not contain real sudoku, only a way to access a single
 *         sudoku in the master sudoku
 *
 */
public class SubSudoku
{
    private MasterSudoku masterSudoku = null;
    private int          upperLeftCornerGlobalRow;
    private int          upperLeftCornerGlobalCol;

    public SubSudoku(MasterSudoku master, int upperLeftCornerGlobalRowInput, int upperLeftCornerGlobalColInput)
    {
        masterSudoku = master;
        upperLeftCornerGlobalRow = upperLeftCornerGlobalRowInput;
        upperLeftCornerGlobalCol = upperLeftCornerGlobalColInput;
    }

    public boolean isInSubSudoku(int globalRow, int globalCol)
    {
        boolean retVal = false;
        if (globalRow >= upperLeftCornerGlobalRow && globalRow < upperLeftCornerGlobalRow + AppMain.SINGLESUDOKUMAXROWS
                && globalCol >= upperLeftCornerGlobalCol
                && globalCol < upperLeftCornerGlobalCol + AppMain.SINGLESUDOKUMAXCOLS)
        {
            retVal = true;
        }
        return retVal;
    }

    /**
     * @param localRow
     *            local row number in this subSudoku
     * @return global row number in the master sudoku
     */
    public int getGlobalRow(int localRow)
    {
        return (upperLeftCornerGlobalRow + localRow);
    }

    /**
     * @param localCol
     *            local column number in this subSudoku
     * @return global column number in the master sudoku
     */
    public int getGlobalCol(int localCol)
    {
        return (upperLeftCornerGlobalCol + localCol);
    }

    /**
     * @param globalRow
     *            row number in the master sudoku
     * @return local row number in the current subsudoku
     */
    public int getLocalRow(int globalRow)
    {
        return (globalRow - upperLeftCornerGlobalRow);
    }

    /**
     * @param globalCol
     *            column number in the master sudoku
     * @return local column number in the current subsudoku
     */
    public int getLocalCol(int globalCol)
    {
        return (globalCol - upperLeftCornerGlobalCol);
    }

    public SingleCellValue getRowCol(int localRow, int localCol)
    {
        return (masterSudoku.getRowCol(getGlobalRow(localRow), getGlobalCol(localCol)));
    }
}

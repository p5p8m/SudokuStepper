package SudokuStepper;

import java.util.Arrays;

public class Tentative
{
    private SingleCellValue[][] sudoku      = new SingleCellValue[Values.DIMENSION][Values.DIMENSION];
    private Bifurcation         bifurcation = null;

    public SingleCellValue[][] getSudoku()
    {
        return (sudoku);
    }

    public Bifurcation getNextTry()
    {
        Bifurcation retVal = null;
        int numPossibleTries = sudoku[bifurcation.getRow()][bifurcation.getCol()].candidates.size();
        int numPrevTries = bifurcation.getNumPreviousTries();
        if (numPrevTries < numPossibleTries)
        {
            LegalValues toBeEliminatedVal = sudoku[bifurcation.getRow()][bifurcation.getCol()].candidates
                    .get(numPrevTries);
            retVal = bifurcation.addNewTry(toBeEliminatedVal);
        }
        return retVal;
    }

    public LegalValues setBifurcation(int row, int col)
    {
        LegalValues eliminatedVal = sudoku[row][col].candidates.get(0);
        bifurcation = new Bifurcation(row, col, eliminatedVal);
        return (eliminatedVal);
    }

    public Tentative(Tentative src)
    {
        bifurcation = null;
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                sudoku[row][col] = new SingleCellValue(src.sudoku[row][col]);
            }
        }
    }

    public Tentative()
    {
    }
}
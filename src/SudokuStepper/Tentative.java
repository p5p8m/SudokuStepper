package SudokuStepper;

import java.util.Arrays;

import SudokuStepper.Values.SudokuType;

public class Tentative // Contains the tentative solution for a complete sudoku samurai
{
    private MasterSudoku sudoku      = null;
    private Bifurcation  bifurcation = null;

    public MasterSudoku getSudoku()
    {
        return (sudoku);
    }

    public Bifurcation getNextTry()
    {
        Bifurcation retVal = null;
        int numPossibleTries = sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).candidates.size();
        int numPrevTries = bifurcation.getNumPreviousTries();
        if (numPrevTries < numPossibleTries)
        {
            LegalValues toBeEliminatedVal = sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).candidates
                    .get(numPrevTries);
            retVal = bifurcation.addNewTry(toBeEliminatedVal);
        }
        return retVal;
    }

    public LegalValues setBifurcation(int globalRow, int globalCol)
    {
        LegalValues eliminatedVal = sudoku.getRowCol(globalRow, globalCol).candidates.get(0);
        // Interested to see if the other solution is also legal?
        // eliminatedVal = sudoku.getRowCol(globalRow, globalCol).candidates
        // .get(sudoku.getRowCol(globalRow, globalCol).candidates.size() - 1); // For a
        // test
        bifurcation = new Bifurcation(globalRow, globalCol, eliminatedVal);
        return (eliminatedVal);
    }

    public Tentative(Tentative src, SudokuType type)
    {
        bifurcation = null;
        sudoku = new MasterSudoku(src.sudoku, type);
    }

    public Tentative(SudokuType type)
    {
        sudoku = new MasterSudoku(type);
    }
}
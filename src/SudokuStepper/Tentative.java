package SudokuStepper;

import SudokuStepper.Values;

import java.util.Arrays;
import java.util.List;

import SudokuStepper.Values.SolutionTrace;
import SudokuStepper.Values.SudokuType;

public class Tentative // Contains the tentative solution for a complete sudoku samurai
{
    private MasterSudoku sudoku      = null;
    private Bifurcation  bifurcation = null;
    private Values       values      = null;

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
            // SudokuStepper.Values.SolutionTrace s = new SolutionTrace(1, 1,
            // LegalValues.EIGHT, null);
            sudoku.getValues().getSolutionTrace()
                    .add(sudoku.getValues().new SolutionTrace(bifurcation.getRow(), bifurcation.getCol(),
                            toBeEliminatedVal,
                            sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).candidates));
        }
        return retVal;
    }

    public LegalValues setBifurcation(int globalRow, int globalCol)
    {
        List<LegalValues> candidates = sudoku.getRowCol(globalRow, globalCol).candidates;
        LegalValues eliminatedVal = candidates.get(0);
        // Interested to see if the other solution is also legal?
        // eliminatedVal = sudoku.getRowCol(globalRow, globalCol).candidates
        // .get(sudoku.getRowCol(globalRow, globalCol).candidates.size() - 1); // For a
        // test
        sudoku.getValues().getSolutionTrace()
                .add(sudoku.getValues().new SolutionTrace(globalRow, globalCol, eliminatedVal, candidates));
        bifurcation = new Bifurcation(globalRow, globalCol, eliminatedVal);
        return (eliminatedVal);
    }

    public Tentative(Tentative src, SudokuType type)
    {
        bifurcation = null;
        sudoku = new MasterSudoku(src.sudoku, type);
    }

    public Tentative(SudokuType type, Values valuesIn)
    {
        sudoku = new MasterSudoku(type, valuesIn);
    }
}
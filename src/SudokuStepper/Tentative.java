package SudokuStepper;

import SudokuStepper.Values;

import java.util.ArrayList;

import SudokuStepper.ListOfSolTraces;
import SudokuStepper.Values.SudokuType;

public class Tentative<LegalValuesGen extends LegalValuesGenClass> // Contains the tentative solution for a complete
// sudoku samurai
{
    private MasterSudoku                sudoku      = null;

    private Bifurcation<LegalValuesGen> bifurcation = null;
    // private Values<LegalValuesGenClass> values = null;

    public MasterSudoku getSudoku()
    {
        return (sudoku);
    }

    public Bifurcation<LegalValuesGen> getNextTry()
    {
        Bifurcation<LegalValuesGen> retVal = null;
        int numPossibleTries = sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).getCandidates().size();
        int numPrevTries = bifurcation.getNumPreviousTries();
        if (numPrevTries < numPossibleTries)
        {
            LegalValuesGen toBeEliminatedVal = (LegalValuesGen) sudoku
                    .getRowCol(bifurcation.getRow(), bifurcation.getCol()).getCandidates()
                    // If you start with index 0 in the list of candidates:
                    // .get(numPrevTries);
                    // if you start with the last in the list
                    // Change accordingly with code in setBifurcation
                    .get(sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).getCandidates().size() - 1
                            - numPrevTries);
            retVal = bifurcation.addNewTry(toBeEliminatedVal);
            // SudokuStepper.Values.SolutionTrace s = new SolutionTrace(1, 1,
            // LegalValues.EIGHT, null);
            sudoku.getValues().addToSolutionTrace(sudoku.getValues(), bifurcation.getRow(), bifurcation.getCol(),
                    toBeEliminatedVal, sudoku.getRowCol(bifurcation.getRow(), bifurcation.getCol()).getCandidates());
        }
        return retVal;
    }

    public LegalValuesGen setBifurcation(int globalRow, int globalCol)
    {
        ArrayList<LegalValuesGen> candidates = sudoku.getRowCol(globalRow, globalCol).getCandidates();
        LegalValuesGen eliminatedVal = candidates
                .get(sudoku.getRowCol(globalRow, globalCol).getCandidates().size() - 1); // .get(0);
        // Interested to see if the other solution is also legal?
        // eliminatedVal = sudoku.getRowCol(globalRow, globalCol).candidates
        // .get(sudoku.getRowCol(globalRow, globalCol).candidates.size() - 1);
        // Change accordingly with code in getNextTry
        sudoku.getValues().addToSolutionTrace(sudoku.getValues(), globalRow, globalCol, eliminatedVal, candidates);
        bifurcation = new Bifurcation<LegalValuesGen>(globalRow, globalCol, eliminatedVal);
        return (eliminatedVal);
    }

    public Tentative(Tentative<?> src, SudokuType type)
    {
        bifurcation = null;
        sudoku = new MasterSudoku(src.sudoku, type);
    }

    public Tentative(SudokuType type, Values<LegalValuesGen> valuesIn, int candNbr)
    {
        sudoku = new MasterSudoku(type, valuesIn, candNbr);
    }
}
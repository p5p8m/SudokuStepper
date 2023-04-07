package SudokuStepper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SingleCellValue<LegalValuesGen extends LegalValuesGenClass>
{
    private boolean                   isInput          = false;
    private boolean                   isTryNError      = false;
    private boolean                   isAConflict      = false;
    private ArrayList<LegalValuesGen> candidates       = null;
    private int                       candidatesNumber = 0;    // Dummy initialization
    private LegalValuesGen            solution         = null;

    public void setSolution(LegalValuesGen val, int row, int col, ArrayList<SolutionListener> solutionListeners,
            boolean runsInUiThread, boolean markLastSolutionFound)
    {
        solution = val;
        if (solutionListeners != null)
        {
            for (SolutionListener listener : solutionListeners)
            {
                listener.solutionUpdated(row, col, runsInUiThread, markLastSolutionFound);
            }
        }
    }

    public SingleCellValue(SingleCellValue<LegalValuesGen> src)
    {
        candidates = new ArrayList<LegalValuesGen>(candidatesNumber);
        for (LegalValuesGen val : src.getCandidates())
        {
            getCandidates().add(val);
        }
        setInput(src.isInput());
        isTryNError = src.isTryNError;
        setAConflict(src.isAConflict());
        solution = src.solution;
        candidatesNumber = src.candidatesNumber;
    }

    public LegalValuesGen getSolution()
    {
        return (solution);
    }

    public SingleCellValue(Class legalValuesClass, int candidatesNbr)
    {
        candidatesNumber = candidatesNbr;
        candidates = new ArrayList<>(candidatesNumber);
        initCandidates(legalValuesClass);
    }

    /**
     * Reinitialize the list of candidates to contain all legal values
     */
    void initCandidates(Class legalValuesClass)
    {
        getCandidates().clear();
        for (LegalValuesGenClass val : LegalValuesGen.values(legalValuesClass))
        {
            getCandidates().add((LegalValuesGen) val);
        }
    }

    public boolean isTryNError()
    {
        return isTryNError;
    }

    public void setTryNError(boolean isTryNError)
    {
        this.isTryNError = isTryNError;
    }

    public boolean isInput()
    {
        return isInput;
    }

    public void setInput(boolean isInput)
    {
        this.isInput = isInput;
    }

    public boolean isAConflict()
    {
        return isAConflict;
    }

    public void setAConflict(boolean isAConflict)
    {
        this.isAConflict = isAConflict;
    }

    public ArrayList<LegalValuesGen> getCandidates()
    {
        return candidates;
    }

    public void setCandidates(ArrayList<LegalValuesGen> newCandidates)
    {
        this.candidates = newCandidates;
    }
}
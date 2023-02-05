package SudokuStepper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SingleCellValue<LegalValuesGen extends Enum<LegalValuesGen>> // expected to be an enum for legalValues
{
    private boolean              isInput          = false;
    private boolean              isTryNError      = false;
    private boolean              isAConflict      = false;
    private List<LegalValuesGen> candidates       = null;
    private int                  candidatesNumber = 0;    // Dummy initialization
    private LegalValuesGen       solution         = null;

    public void setSolution(LegalValuesGen val, int row, int col, List<SolutionListener> solutionListeners,
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
        for (LegalValuesGen val : src.getCandidates())
        {
            getCandidates().add(val);
        }
        setInput(src.isInput());
        isTryNError = src.isTryNError;
        setAConflict(src.isAConflict());
        solution = src.solution;
        candidatesNumber = src.candidatesNumber;
        candidates = new Vector<LegalValuesGen>(candidatesNumber);
    }

    public LegalValuesGen getSolution()
    {
        return (solution);
    }

    public SingleCellValue(int candidatesNbr)
    {
        candidatesNumber = candidatesNbr;
        candidates = new Vector<LegalValuesGen>(candidatesNumber);
        initCandidates();
    }

    /**
     * Reinitialize the list of candidates to contain all legal values
     */
    void initCandidates()
    {
        getCandidates().clear();
        for (LegalValuesGen val : LegalValuesGen.values())
        {
            getCandidates().add(val);
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

    public List<LegalValuesGen> getCandidates()
    {
        return candidates;
    }

    public void setCandidates(List<LegalValuesGen> candidates)
    {
        this.candidates = candidates;
    }
}
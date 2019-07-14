package SudokuStepper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SingleCellValue
{
    private boolean           isInput     = false;
    private boolean           isTryNError = false;
    private boolean           isAConflict = false;
    private List<LegalValues> candidates  = new Vector<LegalValues>(AppMain.CANDIDATESNUMBER);
    private LegalValues       solution    = null;

    public void setSolution(LegalValues val, int row, int col, List<SolutionListener> solutionListeners,
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

    public SingleCellValue(SingleCellValue src)
    {
        for (LegalValues val : src.getCandidates())
        {
            getCandidates().add(val);
        }
        setInput(src.isInput());
        isTryNError = src.isTryNError;
        setAConflict(src.isAConflict());
        solution = src.solution;
    }

    public LegalValues getSolution()
    {
        return (solution);
    }

    public SingleCellValue()
    {
        initCandidates();
    }

    /**
     * Reinitialize the list of candidates to contain all legal values
     */
    void initCandidates()
    {
        getCandidates().clear();
        for (LegalValues val : LegalValues.values())
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

    public List<LegalValues> getCandidates()
    {
        return candidates;
    }

    public void setCandidates(List<LegalValues> candidates)
    {
        this.candidates = candidates;
    }
}
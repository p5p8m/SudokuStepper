package SudokuStepper;

import java.util.ArrayList;
import java.util.List;

public class SingleCellValue
{
    public boolean           isInput     = false;
    public boolean           isAConflict = false;
    public List<LegalValues> candidates  = new ArrayList<LegalValues>(Values.DIMENSION);
    private LegalValues      solution    = null;

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
        for (LegalValues val : src.candidates)
        {
            candidates.add(val);
        }
        isInput = src.isInput;
        isAConflict = src.isAConflict;
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
        candidates.clear();
        for (LegalValues val : LegalValues.values())
        {
            candidates.add(val);
        }
    }
}
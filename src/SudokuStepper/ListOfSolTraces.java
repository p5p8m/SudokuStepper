package SudokuStepper;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ListOfSolTraces<LegalValuesGen extends LegalValuesGenClass> implements Iterable<SolutionTrace>
{
    private List<SolutionTrace> listTraces = new Vector<SolutionTrace>();

    public void addToSolutionTrace(Values values, int globalRow, int globalCol, LegalValuesGen eliminatedVal,
            List<LegalValuesGen> candidates)
    {
        listTraces.add(new SolutionTrace(globalRow, globalCol, eliminatedVal, candidates));
        System.out.println("Added  trace: row: " + globalRow + ", col: " + globalCol + ", Eliminated: "
                + eliminatedVal.toString() + ", Candidates: " + (candidates == null ? "null" : candidates.toString()));
    }

    public void clear()
    {
        listTraces.clear();
    }

    public int size()
    {
        return (listTraces.size());
    }

    public Iterator<SolutionTrace> iterator()
    {
        return new TraceIterator<SolutionTrace>(listTraces);
    }
}

class TraceIterator<SolutionTrace> implements Iterator<SolutionTrace>
{
    SolutionTrace       current;
    int                 currentInd = -1;
    List<SolutionTrace> intListTraces;

    // constructor
    public TraceIterator(List<SolutionTrace> traceList)
    {
        intListTraces = traceList;
        if (intListTraces.size() > 0)
        {
            currentInd = 0;
        }
    }

    // Checks if the next element exists
    public boolean hasNext()
    {
        return currentInd < intListTraces.size() - 1;
    }

    // moves the cursor/iterator to next element
    public SolutionTrace next()
    {
        SolutionTrace trace = intListTraces.get(currentInd);
        currentInd++;
        return (trace);
    }
}

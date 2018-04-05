package SudokuStepper;

public interface SolutionListener
{
    void solutionUpdated(int row, int col, boolean runsInUiThread, boolean markLastSolutionFound);
}
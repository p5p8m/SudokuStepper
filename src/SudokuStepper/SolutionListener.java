package SudokuStepper;

public interface SolutionListener // <LegalValuesGen extends LegalValuesGenClass>
{
    // Returns a not null value if the thread should wait for "next" being pressed
    <LegalValuesGen extends LegalValuesGenClass> Thread solutionUpdated(int row, int col, boolean runsInUiThread,
            boolean markLastSolutionFound);
}
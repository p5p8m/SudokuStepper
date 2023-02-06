package SudokuStepper;

public interface SolutionListener // <LegalValuesGen extends LegalValuesGenClass>
{
    <LegalValuesGen extends LegalValuesGenClass> void solutionUpdated(int row, int col, boolean runsInUiThread,
            boolean markLastSolutionFound);
}
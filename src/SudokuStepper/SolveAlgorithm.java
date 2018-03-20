/**
 * 
 */
package SudokuStepper;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class SolveAlgorithm extends SudokuAction implements Runnable
{
    public SolveAlgorithm(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("Running solution algorithm");
        boolean activateSolveBtn = true;
        try
        {
            Values sudoku = app.getSudokuPb();
            int oldNumOfSolutions = 0;
            int newNumOfSolutions = 0;
            SolutionProgress updated = SolutionProgress.NONE;
            boolean errorDetected = false;
            Integer slideShowPause = null;
            boolean slideShowEnabled = false;
            int loopCount = 0;
            do
            {
                slideShowPause = app.getSlideShowPause();
                slideShowEnabled = app.getSlideShowEnabled();
                oldNumOfSolutions = newNumOfSolutions;
                updated = removeImpossibleCands(sudoku, slideShowEnabled && slideShowPause == null);
                updated = updated
                        .combineWith(detectSingleCandidates(sudoku, slideShowEnabled && slideShowPause == null));
                if (updated == SolutionProgress.NONE)
                {
                    updated = updated
                            .combineWith(detectUniqueMatches(sudoku, slideShowEnabled && slideShowPause == null));
                }
                errorDetected = !sudoku.areContentsLegal().isEmpty();
                newNumOfSolutions = sudoku.getNumberOfSolutions();
                loopCount++;
                System.out.println("====loopCount: " + loopCount);
                System.out.println("updated: " + updated);
                System.out.println("loopCnewNumOfSolutionsount: " + newNumOfSolutions);
                System.out.println("oldNumOfSolutions: " + oldNumOfSolutions);
                System.out.println("errorDetected: " + errorDetected);
            }
            while ((updated != SolutionProgress.NONE || (newNumOfSolutions < Values.DIMENSION * Values.DIMENSION
                    && oldNumOfSolutions < newNumOfSolutions)) && !errorDetected
            /* && (!slideShowEnabled || slideShowPause != null) */);
            System.out.println("Leaved loop");
            // app.getDisplay().asyncExec(new Runnable()
            // {
            // public void run()
            // {
            // app.updateSudokuFields(true, false); // needed to make sure conflicts are
            // represented since the check
            // // is not performed at every stage
            // }
            // });
            app.updateSudokuFields(true, false, false); // needed to make sure conflicts are represented since the check
            // is not performed at every stage
            if (errorDetected)
            {
                app.getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                        errorBox.setMessage(
                                "Could not solve Sudoku\n'" + sudoku.getName() + "' without contradictions.");
                        errorBox.open();
                    }
                });
            }
            else if (slideShowEnabled && slideShowPause == null)
            {
                // Do nothing is OK
            }
            else if (sudoku.getNumberOfSolutions() != Values.DIMENSION * Values.DIMENSION)
            {
                app.getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                        errorBox.setMessage("Could not find a solution for the sudoku\n'" + sudoku.getName() + "'");
                        errorBox.open();
                    }
                });
            }
            else
            {
                activateSolveBtn = false;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            app.getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {

                    MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                    errorBox.setMessage("Could not solve Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                            + "\n" + ex.toString());
                    errorBox.open();
                }
            });
        }
        finally
        {
            app.getDisplay().asyncExec(new SolveBtnRunnable(activateSolveBtn));
            app.setSolvingThread(null);
            System.out.println("Leaving Solving thread");
        }
    }

    class SolveBtnRunnable implements Runnable
    {
        private boolean activateSolveBtn;

        SolveBtnRunnable(boolean activateBtn)
        {
            super();
            activateSolveBtn = activateBtn;
        }

        @Override
        public void run()
        {
            app.setState(AppState.EMPTY);
            app.setSolveEnabled(activateSolveBtn);
        }
    }

    private SolutionProgress detectSingleCandidates(Values sudoku, boolean stopAfterFirstSolution)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        // same row
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (sudoku.getCell(row, col).candidates.size() == 1)
                {
                    SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, col, null, true, false, true);
                    updated = updated.combineWith(nowUpdated);
                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                    {
                        break;
                    }
                }
            }
            if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
            {
                break;
            }
        }
        return updated;
    }

    // Detect if there is only one single cell in a row, column or block that still
    // accepts a given value
    private SolutionProgress detectUniqueMatches(Values sudoku, boolean stopAfterFirstSolution)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        // same row
        if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
        {
            for (int row = 0; row < Values.DIMENSION; row++)
            {
                for (LegalValues val : LegalValues.values())
                {
                    List<Integer> cols = new ArrayList<Integer>();
                    for (int col = 0; col < Values.DIMENSION; col++)
                    {
                        if (sudoku.getCell(row, col).candidates.contains(val))
                        {
                            cols.add(col);
                        }
                        if (cols.size() > 1)
                        {
                            break; // we can break, we look only for unique values
                        }
                    }
                    if (cols.size() == 1)
                    {
                        if (sudoku.getCell(row, cols.get(0)).candidates.size() == 1)
                        {
                            SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, cols.get(0), null, true, false,
                                    true);
                            updated = updated.combineWith(nowUpdated);
                            if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                            {
                                break;
                            }
                        }
                        else
                        {
                            for (LegalValues otherVal : LegalValues.values())
                            {
                                if (otherVal != val)
                                {
                                    SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, cols.get(0), otherVal,
                                            true, false, true);
                                    // next block superfluous?
                                    if (nowUpdated != SolutionProgress.NONE
                                            && sudoku.getCell(row, cols.get(0)).candidates.isEmpty())
                                    {
                                        sudoku.reduceInfluencedCellCandidates(row, cols.get(0),
                                                sudoku.getCell(row, cols.get(0)).getSolution(), true, false, true);
                                    }
                                    updated = updated.combineWith(nowUpdated);
                                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                    {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                {
                    break;
                }
            }
        }
        if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
        {
            // same column
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                for (LegalValues val : LegalValues.values())
                {
                    List<Integer> rows = new ArrayList<Integer>();
                    for (int row = 0; row < Values.DIMENSION; row++)
                    {
                        if (sudoku.getCell(row, col).candidates.contains(val))
                        {
                            rows.add(row);
                        }
                        if (rows.size() > 1)
                        {
                            break; // we can break, we look only for unique values
                        }
                    }
                    if (rows.size() == 1)
                    {
                        if (sudoku.getCell(rows.get(0), col).candidates.size() == 1)
                        {
                            SolutionProgress nowUpdated = sudoku.eliminateCandidate(rows.get(0), col, null, true, false,
                                    true);
                            updated = updated.combineWith(nowUpdated);
                            if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                            {
                                break;
                            }
                        }
                        else
                        {
                            for (LegalValues otherVal : LegalValues.values())
                            {
                                if (otherVal != val)
                                {
                                    SolutionProgress nowUpdated = sudoku.eliminateCandidate(rows.get(0), col, otherVal,
                                            true, false, true);
                                    if (nowUpdated != SolutionProgress.NONE
                                            && sudoku.getCell(rows.get(0), col).candidates.isEmpty())
                                    {
                                        sudoku.reduceInfluencedCellCandidates(rows.get(0), col,
                                                sudoku.getCell(rows.get(0), col).getSolution(), true, false, true);
                                    }
                                    updated = updated.combineWith(nowUpdated);
                                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                    {
                                        break;
                                    }
                                }
                            }
                            if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                            {
                                break;
                            }
                        }
                    }
                }
                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                {
                    break;
                }
            }
        }
        if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
        {
            for (int rowBlock = 0; rowBlock < AppMain.RECTLENGTH; rowBlock++)
            {
                for (int colBlock = 0; colBlock < AppMain.RECTLENGTH; colBlock++)
                {
                    for (LegalValues val : LegalValues.values())
                    {
                        List<Integer[]> cells = new ArrayList<Integer[]>();
                        // Same block
                        for (int rowInBlock = AppMain.RECTLENGTH * rowBlock; rowInBlock < AppMain.RECTLENGTH
                                * (rowBlock + 1); rowInBlock++)
                        {
                            for (int colInBlock = AppMain.RECTLENGTH * colBlock; colInBlock < AppMain.RECTLENGTH
                                    * (colBlock + 1); colInBlock++)
                            {
                                if (sudoku.getCell(rowInBlock, colInBlock).candidates.contains(val))
                                {
                                    cells.add(new Integer[]
                                    { rowInBlock, colInBlock });
                                }
                                if (cells.size() > 1)
                                {
                                    break; // we can break, we look only for unique values
                                }
                            }
                            if (cells.size() > 1)
                            {
                                break; // we can break, we look only for unique values
                            }
                        }
                        if (cells.size() == 1)
                        {
                            if (sudoku.getCell(cells.get(0)[0], cells.get(0)[1]).candidates.size() == 1)
                            {
                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(cells.get(0)[0],
                                        cells.get(0)[1], null, true, false, true);
                                updated = updated.combineWith(nowUpdated);
                                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                {
                                    break;
                                }
                            }
                            else
                            {
                                for (LegalValues otherVal : LegalValues.values())
                                {
                                    if (otherVal != val)
                                    {
                                        SolutionProgress nowUpdated = sudoku.eliminateCandidate(cells.get(0)[0],
                                                cells.get(0)[1], otherVal, true, false, true);
                                        if (nowUpdated != SolutionProgress.NONE
                                                && sudoku.getCell(cells.get(0)[0], cells.get(0)[1]).candidates
                                                        .isEmpty())
                                        {
                                            sudoku.reduceInfluencedCellCandidates(cells.get(0)[0], cells.get(0)[1],
                                                    sudoku.getCell(cells.get(0)[0], cells.get(0)[1]).getSolution(),
                                                    true, false, true);
                                        }
                                        updated = updated.combineWith(nowUpdated);
                                        if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                        {
                                            break;
                                        }
                                    }
                                }
                                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                {
                                    break;
                                }
                            }
                        }
                    }
                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                    {
                        break;
                    }
                }
                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                {
                    break;
                }
            }
        }
        return updated;
    }

    // Remove candidates who are illegal in a row, column or block
    private SolutionProgress removeImpossibleCands(Values sudoku, boolean stopAfterFirstSolution)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (sudoku.getCell(row, col).candidates.isEmpty())
                {
                    LegalValues valToEliminate = sudoku.getCell(row, col).getSolution();
                    // Same column
                    if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
                    {
                        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
                        {
                            if (rowInCol != row)
                            {
                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(rowInCol, col, valToEliminate,
                                        true, false, true);
                                updated = updated.combineWith(nowUpdated);
                                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                {
                                    break;
                                }
                            }
                        }
                    }
                    if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
                    {
                        // Same row
                        for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
                        {
                            if (colInRow != col)
                            {
                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, colInRow, valToEliminate,
                                        true, false, true);
                                updated = updated.combineWith(nowUpdated);
                                if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                {
                                    break;
                                }
                            }
                        }
                    }
                    if (!stopAfterFirstSolution || updated != SolutionProgress.SOLUTION)
                    {
                        // Same block
                        for (int rowInBlock = AppMain.RECTLENGTH
                                * (row / AppMain.RECTLENGTH); rowInBlock < AppMain.RECTLENGTH
                                        * (row / AppMain.RECTLENGTH + 1); rowInBlock++)
                        {
                            for (int colInBlock = AppMain.RECTLENGTH
                                    * (col / AppMain.RECTLENGTH); colInBlock < AppMain.RECTLENGTH
                                            * (col / AppMain.RECTLENGTH + 1); colInBlock++)
                            {
                                if (rowInBlock != row || colInBlock != col)
                                {
                                    SolutionProgress nowUpdated = sudoku.eliminateCandidate(rowInBlock, colInBlock,
                                            valToEliminate, true, false, true);
                                    updated = updated.combineWith(nowUpdated);
                                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                                    {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
                    {
                        break;
                    }
                }
            }
            if (stopAfterFirstSolution && updated == SolutionProgress.SOLUTION)
            {
                break;
            }
        }
        return updated;
    }
}

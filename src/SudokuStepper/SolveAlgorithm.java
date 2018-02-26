/**
 * 
 */
package SudokuStepper;

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

        try
        {
            Values sudoku = app.getSudokuPb();
            int oldNumOfSolutions = 0;
            int newNumOfSolutions = 0;
            boolean updated = false;
            boolean errorDetected = false;
            do
            {
                oldNumOfSolutions = newNumOfSolutions;
                updated = removeImpossibleCands(sudoku);
                updated |= detectSingleCandidates(sudoku);
                if (!updated)
                {
                    updated |= detectUniqueMatches(sudoku);
                }
                errorDetected = !sudoku.areContentsLegal().isEmpty();
                newNumOfSolutions = sudoku.getNumberOfSolutions();
            }
            while ((updated || (newNumOfSolutions < Values.DIMENSION * Values.DIMENSION
                    && oldNumOfSolutions < newNumOfSolutions)) && !errorDetected);
            app.getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    app.updateSudokuFields(true); // needed to make sure conflicts are represented since the check
                    // is not performed at every stage
                }
            });
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
            app.getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    app.setState(AppState.EMPTY);
                }
            });
        }
    }

    private boolean detectSingleCandidates(Values sudoku)
    {
        boolean updated = false;
        // same row
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (sudoku.getCell(row, col).candidates.size() == 1)
                {
                    boolean nowUpdated = sudoku.eliminateCandidate(row, col, null, true);
                    updated |= nowUpdated;
                }
            }
        }
        return updated;
    }

    // Detect if there is only one single cell in a row, column or block that still
    // accepts a given value
    private boolean detectUniqueMatches(Values sudoku)
    {
        boolean updated = false;
        // same row
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
                        boolean nowUpdated = sudoku.eliminateCandidate(row, cols.get(0), null, true);
                        updated |= nowUpdated;
                    }
                    else
                    {
                        for (LegalValues otherVal : LegalValues.values())
                        {
                            if (otherVal != val)
                            {
                                boolean nowUpdated = sudoku.eliminateCandidate(row, cols.get(0), otherVal, true);
                                // next block superfluous?
                                if (nowUpdated && sudoku.getCell(row, cols.get(0)).candidates.isEmpty())
                                {
                                    sudoku.reduceInfluencedCellCandidates(row, cols.get(0),
                                            sudoku.getCell(row, cols.get(0)).solution, true);
                                }
                                updated |= nowUpdated;
                            }
                        }
                    }
                }
            }
        }
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
                        boolean nowUpdated = sudoku.eliminateCandidate(rows.get(0), col, null, true);
                        updated |= nowUpdated;
                    }
                    else
                    {
                        for (LegalValues otherVal : LegalValues.values())
                        {
                            if (otherVal != val)
                            {
                                boolean nowUpdated = sudoku.eliminateCandidate(rows.get(0), col, otherVal, true);
                                if (nowUpdated && sudoku.getCell(rows.get(0), col).candidates.isEmpty())
                                {
                                    sudoku.reduceInfluencedCellCandidates(rows.get(0), col,
                                            sudoku.getCell(rows.get(0), col).solution, true);
                                }
                                updated |= nowUpdated;
                            }
                        }
                    }
                }
            }
        }
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
                            boolean nowUpdated = sudoku.eliminateCandidate(cells.get(0)[0], cells.get(0)[1], null,
                                    true);
                            updated |= nowUpdated;
                        }
                        else
                        {
                            for (LegalValues otherVal : LegalValues.values())
                            {
                                if (otherVal != val)
                                {
                                    boolean nowUpdated = sudoku.eliminateCandidate(cells.get(0)[0], cells.get(0)[1],
                                            otherVal, true);
                                    if (nowUpdated
                                            && sudoku.getCell(cells.get(0)[0], cells.get(0)[1]).candidates.isEmpty())
                                    {
                                        sudoku.reduceInfluencedCellCandidates(cells.get(0)[0], cells.get(0)[1],
                                                sudoku.getCell(cells.get(0)[0], cells.get(0)[1]).solution, true);
                                    }
                                    updated |= nowUpdated;
                                }
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }

    // Remove candidates who are illegal in a row, column or block
    private boolean removeImpossibleCands(Values sudoku)
    {
        boolean updated;
        updated = false;
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (sudoku.getCell(row, col).candidates.isEmpty())
                {
                    LegalValues valToEliminate = sudoku.getCell(row, col).solution;
                    // Same column
                    for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
                    {
                        if (rowInCol != row)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(rowInCol, col, valToEliminate, true);
                            updated |= nowUpdated;
                        }
                    }
                    // Same row
                    for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
                    {
                        if (colInRow != col)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(row, colInRow, valToEliminate, true);
                            updated |= nowUpdated;
                        }
                    }
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
                                boolean nowUpdated = sudoku.eliminateCandidate(rowInBlock, colInBlock, valToEliminate,
                                        true);
                                updated |= nowUpdated;
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }
}

package SudokuStepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SolveSudokuAction extends SudokuAction
{
    public SolveSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("Solve");
    }

    @Override
    public void run()
    {
        System.out.println("SolveSudokuAction.run");
        Values sudoku = app.getSudokuPb();
        try
        {
            app.setState(AppState.SOLVING);
            boolean updated = false;
            boolean errorDetected = false;
            do
            {
                updated = removeImpossibleCands(sudoku);
                if (!updated)
                {
                    updated |= detectUniqueMatches(sudoku);
                }
                errorDetected = !sudoku.areContentsLegal().isEmpty();
            }
            while (updated && !errorDetected);
            app.updateSudokuFields(); // needed to make sure conflicts are represented since the check
                                      // is not performed at every stage
            if (errorDetected)
            {
                MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                errorBox.setMessage("Could not solve Sudoku\n'" + sudoku.getName() + "' without contradictions.");
                errorBox.open();
            }
            else if (sudoku.getNumberOfSolutions() != Values.DIMENSION * Values.DIMENSION)
            {
                MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                errorBox.setMessage("Could not find a solution for the sudoku\n'" + sudoku.getName() + "'");
                errorBox.open();
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not solve Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n"
                    + ex.toString());
            errorBox.open();
        }
        finally
        {
            app.setState(AppState.EMPTY);
        }
    }

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
                    for (LegalValues otherVal : LegalValues.values())
                    {
                        if (otherVal != val)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(row, cols.get(0), otherVal);
                            updated |= nowUpdated;
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
                    for (LegalValues otherVal : LegalValues.values())
                    {
                        if (otherVal != val)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(rows.get(0), col, otherVal);
                            updated |= nowUpdated;
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
                        for (LegalValues otherVal : LegalValues.values())
                        {
                            if (otherVal != val)
                            {
                                boolean nowUpdated = sudoku.eliminateCandidate(cells.get(0)[0], cells.get(0)[1],
                                        otherVal);
                                updated |= nowUpdated;
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }

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
                    // Same column
                    for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
                    {
                        if (rowInCol != row)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(rowInCol, col,
                                    sudoku.getCell(row, col).solution);
                            updated |= nowUpdated;
                        }
                    }
                    // Same row
                    for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
                    {
                        if (colInRow != col)
                        {
                            boolean nowUpdated = sudoku.eliminateCandidate(row, colInRow,
                                    sudoku.getCell(row, col).solution);
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
                                boolean nowUpdated = sudoku.eliminateCandidate(rowInBlock, colInBlock,
                                        sudoku.getCell(row, col).solution);
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

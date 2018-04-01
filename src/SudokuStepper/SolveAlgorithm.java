/**
 * 
 */
package SudokuStepper;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            // Integer slideShowPause = null;
            // boolean slideShowEnabled = false;
            int loopCount = 0;
            do
            {
                // slideShowPause = app.getSlideShowPause();
                // slideShowEnabled = app.getSlideShowEnabled();
                oldNumOfSolutions = newNumOfSolutions;
                updated = removeImpossibleCands(sudoku);
                updated = updated.combineWith(detectSingleCandidates(sudoku));
                if (updated == SolutionProgress.NONE)
                {
                    updated = updated.combineWith(detectUniqueMatches(sudoku));
                }
                if (updated == SolutionProgress.NONE)
                {
                    updated = detectTuples(sudoku);
                }
                if (updated == SolutionProgress.NONE)
                {
                    updated = useTryAndError(sudoku);
                }
                errorDetected = !sudoku.areContentsLegal().isEmpty();
                if (errorDetected && sudoku.isRollbackPossible())
                {
                    updated = rollbackAndTryNext(sudoku);
                }
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
        catch (

        Exception ex)
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

    private SolutionProgress detectSingleCandidates(Values sudoku)
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
                }
            }
        }
        return updated;
    }

    // class ClosedTuples
    // {
    // // Always sort the elements in increasing order when adding them
    // private ArrayList<LegalValues> values = new ArrayList<LegalValues>();
    // private ArrayList<ValidityContext> contexts = new
    // ArrayList<ValidityContext>();
    //
    // public void addValue(LegalValues newVal)
    // {
    // if (!values.contains(newVal))
    // {
    // for (LegalValues oldVal : values)
    // {
    // if (oldVal.val() > newVal.val())
    // {
    // values.add(values.indexOf(oldVal), newVal);
    // }
    // }
    // }
    // }
    //
    // public void addContext(ValidityContext newContext)
    // {
    // if (contexts.contains(newContext))
    // {
    // contexts.add(newContext);
    // }
    // }
    // }

    private ArrayList<LegalValues> deepCopy(List<LegalValues> src)
    {
        ArrayList<LegalValues> retVal = null;
        if (src != null)
        {
            retVal = new ArrayList<LegalValues>(src.size());
            for (LegalValues val : src)
            {
                retVal.add(val);
            }
        }
        return (retVal);
    }

    Comparator<LegalValues> sortLegalValues = new Comparator<LegalValues>()
    {
        public int compare(LegalValues v1, LegalValues v2)
        {
            return (v1.val() - v2.val());
        }
    };

    private boolean sameContents(List<LegalValues> l1, List<LegalValues> l2)
    {
        boolean retVal = l1.size() == l2.size();
        if (retVal)
        {
            for (int ind = 0; ind < l1.size(); ind++)
            {
                if (l1.get(ind) != l2.get(ind))
                {
                    retVal = false;
                    break;
                }
            }
        }
        return (retVal);
    }

    private SolutionProgress rollbackAndTryNext(Values sudoku)
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        try
        {
            retVal = sudoku.bifurqueOnceMore();
        }
        catch (CloneNotSupportedException ex)
        {
            System.out.println("Could not rollback the sudoku: " + ex.getMessage());
            retVal = SolutionProgress.NONE;
        }
        return (retVal);
    }

    private SolutionProgress useTryAndError(Values sudoku) throws CloneNotSupportedException
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        try
        {
            int numCandidates = LegalValues.values().length;
            Integer rowMarked = null;
            Integer colMarked = null;
            for (int row = 0; row < Values.DIMENSION; row++)
            {
                for (int col = 0; col < Values.DIMENSION; col++)
                {
                    if (!sudoku.getCell(row, col).candidates.isEmpty())
                    {
                        if (sudoku.getCell(row, col).candidates.size() < numCandidates)
                        {
                            rowMarked = row;
                            colMarked = col;
                            numCandidates = sudoku.getCell(row, col).candidates.size();
                            if (numCandidates == 2)
                            {
                                break;
                            }
                        }
                    }
                }
                if (numCandidates == 2)
                {
                    break;
                }
            }
            if (rowMarked != null && colMarked != null)
            {
                // clone and try the first candidate
                retVal = sudoku.addBifurcationNClone(rowMarked, colMarked);
            }
        }
        catch (CloneNotSupportedException ex)
        {
            System.out.println("Could not clone the sudoku: " + ex.getMessage());
            retVal = SolutionProgress.NONE;
        }
        return (retVal);
    }

    private SolutionProgress detectTuples(Values sudoku)
    {
        // ArrayList<ClosedTuples> retVal1 = new ArrayList<ClosedTuples>();
        SolutionProgress retVal = SolutionProgress.NONE;
        // check by rows first
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (!sudoku.getCell(row, col).candidates.isEmpty())
                {
                    // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                    // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                    // col).candidates);
                    // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                    // unionList.size());
                    for (int scndCol = col + 1; scndCol < Values.DIMENSION; scndCol++)
                    {
                        if (!sudoku.getCell(row, scndCol).candidates.isEmpty())
                        {
                            if (sameContents(sudoku.getCell(row, col).candidates,
                                    sudoku.getCell(row, scndCol).candidates)
                                    && sudoku.getCell(row, col).candidates.size() == 2)
                            {
                                for (LegalValues val : sudoku.getCell(row, col).candidates)
                                {
                                    for (int cleanedCol = 0; cleanedCol < Values.DIMENSION; cleanedCol++)
                                    {
                                        if (cleanedCol != col && cleanedCol != scndCol
                                                && !sudoku.getCell(row, cleanedCol).candidates.isEmpty())
                                        {
                                            SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, cleanedCol,
                                                    val, true, false, true);
                                            retVal = retVal.combineWith(nowUpdated);
                                        }
                                    }
                                }
                            }
                            // intersectList.retainAll(sudoku.getCell(row, scndCol).candidates);
                            // unionList.addAll(sudoku.getCell(row, scndCol).candidates);
                            // unionList = unionList.stream().distinct().collect(Collectors.toList());
                            // Collections.sort(unionList, sortLegalValues);
                            // Collections.sort(intersectList, sortLegalValues);
                            // System.out.println("row: " + row + ", col: " + col + ", union: " +
                            // unionList);
                            // System.out.println("row: " + row + ", col: " + col + ", intersection: " +
                            // intersectList);
                            // System.out.println("row: " + row + ", col: " + col + ", Both same contents: "
                            // + sameContents(intersectList, unionList));
                            // System.out.println("row: " + row + ", col: " + col + ", original cands: "
                            // + sudoku.getCell(row, col).candidates);
                        }
                    }
                }
            }
        }
        // check by columns then
        for (int col = 0; col < Values.DIMENSION; col++)
        {
            for (int row = 0; row < Values.DIMENSION; row++)
            {
                if (!sudoku.getCell(row, col).candidates.isEmpty())
                {
                    // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                    // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                    // col).candidates);
                    // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                    // unionList.size());
                    for (int scndRow = row + 1; scndRow < Values.DIMENSION; scndRow++)
                    {
                        if (!sudoku.getCell(scndRow, col).candidates.isEmpty())
                        {
                            if (sameContents(sudoku.getCell(row, col).candidates,
                                    sudoku.getCell(scndRow, col).candidates)
                                    && sudoku.getCell(row, col).candidates.size() == 2)
                            {
                                for (LegalValues val : sudoku.getCell(row, col).candidates)
                                {
                                    for (int cleanedRow = 0; cleanedRow < Values.DIMENSION; cleanedRow++)
                                    {
                                        if (cleanedRow != row && cleanedRow != scndRow
                                                && !sudoku.getCell(cleanedRow, col).candidates.isEmpty())
                                        {
                                            SolutionProgress nowUpdated = sudoku.eliminateCandidate(cleanedRow, col,
                                                    val, true, false, true);
                                            retVal = retVal.combineWith(nowUpdated);
                                        }
                                    }
                                }
                            }
                            // intersectList.retainAll(sudoku.getCell(row, scndCol).candidates);
                            // unionList.addAll(sudoku.getCell(row, scndCol).candidates);
                            // unionList = unionList.stream().distinct().collect(Collectors.toList());
                            // Collections.sort(unionList, sortLegalValues);
                            // Collections.sort(intersectList, sortLegalValues);
                            // System.out.println("row: " + row + ", col: " + col + ", union: " +
                            // unionList);
                            // System.out.println("row: " + row + ", col: " + col + ", intersection: " +
                            // intersectList);
                            // System.out.println("row: " + row + ", col: " + col + ", Both same contents: "
                            // + sameContents(intersectList, unionList));
                            // System.out.println("row: " + row + ", col: " + col + ", original cands: "
                            // + sudoku.getCell(row, col).candidates);
                        }
                    }
                }
            }
        }
        // check by blocks finally
        for (int rowBlock = 0; rowBlock < AppMain.RECTLENGTH; rowBlock++)
        {
            for (int colBlock = 0; colBlock < AppMain.RECTLENGTH; colBlock++)
            {
                // Same block
                for (int rowInBlock = AppMain.RECTLENGTH * rowBlock; rowInBlock < AppMain.RECTLENGTH
                        * (rowBlock + 1); rowInBlock++)
                {
                    for (int colInBlock = AppMain.RECTLENGTH * colBlock; colInBlock < AppMain.RECTLENGTH
                            * (colBlock + 1); colInBlock++)
                    {
                        if (!sudoku.getCell(rowInBlock, colInBlock).candidates.isEmpty())
                        {
                            // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                            // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                            // col).candidates);
                            // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                            // unionList.size());
                            // Same block
                            for (int scndRowInBlock = AppMain.RECTLENGTH * rowBlock; scndRowInBlock < AppMain.RECTLENGTH
                                    * (rowBlock + 1); scndRowInBlock++)
                            {
                                for (int scndColInBlock = AppMain.RECTLENGTH
                                        * colBlock; scndColInBlock < AppMain.RECTLENGTH
                                                * (colBlock + 1); scndColInBlock++)
                                {

                                    if (scndRowInBlock > rowInBlock
                                            || (scndRowInBlock == rowInBlock && scndColInBlock > colInBlock))
                                    {
                                        if (!sudoku.getCell(scndRowInBlock, scndColInBlock).candidates.isEmpty())
                                        {
                                            if (sameContents(sudoku.getCell(rowInBlock, colInBlock).candidates,
                                                    sudoku.getCell(scndRowInBlock, scndColInBlock).candidates)
                                                    && sudoku.getCell(rowInBlock, colInBlock).candidates.size() == 2)
                                            {
                                                for (LegalValues val : sudoku.getCell(rowInBlock,
                                                        colInBlock).candidates)
                                                {
                                                    for (int cleanedRowInBlock = AppMain.RECTLENGTH
                                                            * rowBlock; cleanedRowInBlock < AppMain.RECTLENGTH
                                                                    * (rowBlock + 1); cleanedRowInBlock++)
                                                    {
                                                        for (int cleanedColInBlock = AppMain.RECTLENGTH
                                                                * colBlock; cleanedColInBlock < AppMain.RECTLENGTH
                                                                        * (colBlock + 1); cleanedColInBlock++)
                                                        {
                                                            if ((cleanedRowInBlock != rowInBlock
                                                                    || cleanedColInBlock != colInBlock)
                                                                    && (cleanedRowInBlock != scndRowInBlock
                                                                            || cleanedColInBlock != scndColInBlock)
                                                                    && !sudoku.getCell(cleanedRowInBlock,
                                                                            cleanedColInBlock).candidates.isEmpty())
                                                            {
                                                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(
                                                                        cleanedRowInBlock, cleanedColInBlock, val, true,
                                                                        false, true);
                                                                retVal = retVal.combineWith(nowUpdated);
                                                            }
                                                        }
                                                    }
                                                }
                                                // intersectList.retainAll(sudoku.getCell(row, scndCol).candidates);
                                                // unionList.addAll(sudoku.getCell(row, scndCol).candidates);
                                                // unionList =
                                                // unionList.stream().distinct().collect(Collectors.toList());
                                                // Collections.sort(unionList, sortLegalValues);
                                                // Collections.sort(intersectList, sortLegalValues);
                                                // System.out.println("row: " + row + ", col: " + col + ", union: " +
                                                // unionList);
                                                // System.out.println("row: " + row + ", col: " + col + ", intersection:
                                                // " +
                                                // intersectList);
                                                // System.out.println("row: " + row + ", col: " + col + ", Both same
                                                // contents: "
                                                // + sameContents(intersectList, unionList));
                                                // System.out.println("row: " + row + ", col: " + col + ", original
                                                // cands: "
                                                // + sudoku.getCell(row, col).candidates);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (retVal);
    }

    // Detect if there is only one single cell in a row, column or block that still
    // accepts a given value
    private SolutionProgress detectUniqueMatches(Values sudoku)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        // same row
        if (updated != SolutionProgress.SOLUTION)
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
                                }
                            }
                        }
                    }
                }
            }
        }
        if (updated != SolutionProgress.SOLUTION)
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
                                }
                            }
                        }
                    }
                }
            }
        }
        if (updated != SolutionProgress.SOLUTION)
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
                                    }
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
    private SolutionProgress removeImpossibleCands(Values sudoku)
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
                    if (updated != SolutionProgress.SOLUTION)
                    {
                        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
                        {
                            if (rowInCol != row)
                            {
                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(rowInCol, col, valToEliminate,
                                        true, false, true);
                                updated = updated.combineWith(nowUpdated);
                            }
                        }
                    }
                    if (updated != SolutionProgress.SOLUTION)
                    {
                        // Same row
                        for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
                        {
                            if (colInRow != col)
                            {
                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, colInRow, valToEliminate,
                                        true, false, true);
                                updated = updated.combineWith(nowUpdated);
                            }
                        }
                    }
                    if (updated != SolutionProgress.SOLUTION)
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
                                }
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }
}

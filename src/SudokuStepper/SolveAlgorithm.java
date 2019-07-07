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

import SudokuStepper.Values.SudokuType;

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
            sudoku.getSolutionTrace().clear();
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
                    errorDetected = !sudoku.areContentsLegal().isEmpty();
                }
                newNumOfSolutions = sudoku.getNumberOfSolutions();
                loopCount++;
                System.out.println("====loopCount: " + loopCount);
                System.out.println("updated: " + updated);
                System.out.println("loopCnewNumOfSolutionsount: " + newNumOfSolutions);
                System.out.println("oldNumOfSolutions: " + oldNumOfSolutions);
                System.out.println("errorDetected: " + errorDetected);
            }
            while (((updated != SolutionProgress.NONE || (newNumOfSolutions < sudoku.getNumberOfCellsToBeSolved()
                    && oldNumOfSolutions < newNumOfSolutions)) && !errorDetected)
                    && !(newNumOfSolutions == sudoku.getNumberOfCellsToBeSolved() && !errorDetected)
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
            else if (sudoku.getNumberOfSolutions() != sudoku.getNumberOfCellsToBeSolved())
            {
                int numSol = sudoku.getNumberOfSolutions();
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

    private SolutionProgress detectSingleCandidates(Values masterSudoku)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
        {
            // same row
            for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
            {
                for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                {
                    if (subSudoku.getRowCol(row, col).candidates.size() == 1)
                    {
                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(subSudoku.getGlobalRow(row),
                                subSudoku.getGlobalCol(col), null, true, false, true);
                        updated = updated.combineWith(nowUpdated);
                    }
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
        SolutionProgress retVal = sudoku.bifurqueOnceMore();
        return (retVal);
    }

    private SolutionProgress useTryAndError(Values masterSudoku)
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        try
        {
            int numCandidates = LegalValues.values().length;
            Integer globalRowMarked = null;
            Integer globalColMarked = null;
            for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
            {
                for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
                {
                    for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                    {
                        if (!subSudoku.getRowCol(row, col).candidates.isEmpty())
                        {
                            if (subSudoku.getRowCol(row, col).candidates.size() < numCandidates)
                            {
                                globalRowMarked = subSudoku.getGlobalRow(row);
                                globalColMarked = subSudoku.getGlobalCol(col);
                                numCandidates = subSudoku.getRowCol(row, col).candidates.size();
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
                if (numCandidates == 2)
                {
                    break;
                }
            }
            if (globalRowMarked != null && globalColMarked != null)
            {
                // clone and try the first candidate
                SolutionProgress newUpdated = masterSudoku.addBifurcationNClone(globalRowMarked, globalColMarked);
                retVal = retVal.combineWith(newUpdated);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Could not clone the sudoku: " + ex.getMessage());
            retVal = SolutionProgress.NONE;
        }
        return (retVal);
    }

    private SolutionProgress detectTuples(Values sudoku) // Single sudoku
    {
        // ArrayList<ClosedTuples> retVal1 = new ArrayList<ClosedTuples>();
        SolutionProgress retVal = SolutionProgress.NONE;
        for (SubSudoku subSudoku : sudoku.getSudoku().getSubSudokus())
        {
            // check by rows first
            for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
            {
                for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                {
                    if (!sudoku.getCell(row, col).candidates.isEmpty())
                    {
                        // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                        // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                        // col).candidates);
                        // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                        // unionList.size());
                        for (int scndCol = col + 1; scndCol < AppMain.SINGLESUDOKUMAXCOLS; scndCol++)
                        {
                            if (!sudoku.getCell(row, scndCol).candidates.isEmpty())
                            {
                                if (sameContents(sudoku.getCell(row, col).candidates,
                                        sudoku.getCell(row, scndCol).candidates)
                                        && sudoku.getCell(row, col).candidates.size() == 2)
                                {
                                    // Make a deep copy to avoid problems when the list is modified within the loop
                                    List<LegalValues> locCandidates = new ArrayList<LegalValues>(
                                            sudoku.getCell(row, col).candidates);
                                    for (LegalValues val : locCandidates)
                                    {
                                        for (int cleanedCol = 0; cleanedCol < AppMain.SINGLESUDOKUMAXCOLS; cleanedCol++)
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
            for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
            {
                for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
                {
                    if (!sudoku.getCell(row, col).candidates.isEmpty())
                    {
                        // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                        // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                        // col).candidates);
                        // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                        // unionList.size());
                        for (int scndRow = row + 1; scndRow < AppMain.SINGLESUDOKUMAXROWS; scndRow++)
                        {
                            if (!sudoku.getCell(scndRow, col).candidates.isEmpty())
                            {
                                if (sameContents(sudoku.getCell(row, col).candidates,
                                        sudoku.getCell(scndRow, col).candidates)
                                        && sudoku.getCell(row, col).candidates.size() == 2)
                                {
                                    // Make a deep copy to avoid problems when the list is modified whithin the loop
                                    List<LegalValues> locCandidates = new ArrayList<LegalValues>(
                                            sudoku.getCell(row, col).candidates);
                                    for (LegalValues val : locCandidates)
                                    {
                                        for (int cleanedRow = 0; cleanedRow < AppMain.SINGLESUDOKUMAXROWS; cleanedRow++)
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
            for (int rowBlock = 0; rowBlock < AppMain.SINGLESUDOKUMAXROWS / AppMain.RECTANGLELENGTH; rowBlock++)
            {
                for (int colBlock = 0; colBlock < AppMain.SINGLESUDOKUMAXCOLS / AppMain.RECTANGLELENGTH; colBlock++)
                {
                    // Same block
                    for (int rowInBlock = AppMain.RECTANGLELENGTH * rowBlock; rowInBlock < AppMain.RECTANGLELENGTH
                            * (rowBlock + 1); rowInBlock++)
                    {
                        for (int colInBlock = AppMain.RECTANGLELENGTH * colBlock; colInBlock < AppMain.RECTANGLELENGTH
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
                                for (int scndRowInBlock = AppMain.RECTANGLELENGTH
                                        * rowBlock; scndRowInBlock < AppMain.RECTANGLELENGTH
                                                * (rowBlock + 1); scndRowInBlock++)
                                {
                                    for (int scndColInBlock = AppMain.RECTANGLELENGTH
                                            * colBlock; scndColInBlock < AppMain.RECTANGLELENGTH
                                                    * (colBlock + 1); scndColInBlock++)
                                    {

                                        if (scndRowInBlock > rowInBlock
                                                || (scndRowInBlock == rowInBlock && scndColInBlock > colInBlock))
                                        {
                                            if (!sudoku.getCell(scndRowInBlock, scndColInBlock).candidates.isEmpty())
                                            {
                                                if (sameContents(sudoku.getCell(rowInBlock, colInBlock).candidates,
                                                        sudoku.getCell(scndRowInBlock, scndColInBlock).candidates)
                                                        && sudoku.getCell(rowInBlock, colInBlock).candidates
                                                                .size() == 2)
                                                {
                                                    List<LegalValues> locCandidates = new ArrayList<LegalValues>(
                                                            sudoku.getCell(rowInBlock, colInBlock).candidates);
                                                    for (LegalValues val : locCandidates)
                                                    {
                                                        for (int cleanedRowInBlock = AppMain.RECTANGLELENGTH
                                                                * rowBlock; cleanedRowInBlock < AppMain.RECTANGLELENGTH
                                                                        * (rowBlock + 1); cleanedRowInBlock++)
                                                        {
                                                            for (int cleanedColInBlock = AppMain.RECTANGLELENGTH
                                                                    * colBlock; cleanedColInBlock < AppMain.RECTANGLELENGTH
                                                                            * (colBlock + 1); cleanedColInBlock++)
                                                            {
                                                                if ((cleanedRowInBlock != rowInBlock
                                                                        || cleanedColInBlock != colInBlock)
                                                                        && (cleanedRowInBlock != scndRowInBlock
                                                                                || cleanedColInBlock != scndColInBlock)
                                                                        && !sudoku.getCell(cleanedRowInBlock,
                                                                                cleanedColInBlock).candidates.isEmpty())
                                                                {
                                                                    SolutionProgress nowUpdated = sudoku
                                                                            .eliminateCandidate(cleanedRowInBlock,
                                                                                    cleanedColInBlock, val, true, false,
                                                                                    true);
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
                                                    // System.out.println("row: " + row + ", col: " + col + ", union: "
                                                    // +
                                                    // unionList);
                                                    // System.out.println("row: " + row + ", col: " + col + ",
                                                    // intersection:
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
        }
        return (retVal);
    }

    // Detect if there is only one single cell in a row, column or block that still
    // accepts a given value
    private SolutionProgress detectUniqueMatches(Values masterSudoku)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        // same row
        if (updated != SolutionProgress.SOLUTION)
        {
            for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
            {
                for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
                {
                    for (LegalValues val : LegalValues.values())
                    {
                        List<Integer> cols = new ArrayList<Integer>();
                        for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                        {
                            if (subSudoku.getRowCol(row, col).candidates.contains(val))
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
                            if (subSudoku.getRowCol(row, cols.get(0)).candidates.size() == 1)
                            {
                                SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                        subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(cols.get(0)), null, true,
                                        false, true);
                                updated = updated.combineWith(nowUpdated);
                            }
                            else
                            {
                                for (LegalValues otherVal : LegalValues.values())
                                {
                                    if (otherVal != val)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(cols.get(0)),
                                                otherVal, true, false, true);
                                        updated = updated.combineWith(nowUpdated);
                                        // next block superfluous?
                                        if (nowUpdated != SolutionProgress.NONE
                                                && subSudoku.getRowCol(row, cols.get(0)).candidates.isEmpty())
                                        {
                                            nowUpdated = masterSudoku.reduceInfluencedCellCandidates(
                                                    subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(cols.get(0)),
                                                    subSudoku.getRowCol(row, cols.get(0)).getSolution(), true, false,
                                                    true);
                                            updated = updated.combineWith(nowUpdated);

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
        if (updated != SolutionProgress.SOLUTION)
        {
            for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
            {
                // same column
                for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                {
                    for (LegalValues val : LegalValues.values())
                    {
                        List<Integer> rows = new ArrayList<Integer>();
                        for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
                        {
                            if (subSudoku.getRowCol(row, col).candidates.contains(val))
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
                            if (subSudoku.getRowCol(rows.get(0), col).candidates.size() == 1)
                            {
                                SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                        subSudoku.getGlobalRow(rows.get(0)), subSudoku.getGlobalCol(col), null, true,
                                        false, true);
                                updated = updated.combineWith(nowUpdated);
                            }
                            else
                            {
                                for (LegalValues otherVal : LegalValues.values())
                                {
                                    if (otherVal != val)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(rows.get(0)), subSudoku.getGlobalCol(col),
                                                otherVal, true, false, true);
                                        if (nowUpdated != SolutionProgress.NONE
                                                && subSudoku.getRowCol(rows.get(0), col).candidates.isEmpty())
                                        {
                                            nowUpdated = masterSudoku.reduceInfluencedCellCandidates(
                                                    subSudoku.getGlobalRow(rows.get(0)), subSudoku.getGlobalCol(col),
                                                    masterSudoku.getCell(subSudoku.getGlobalRow(rows.get(0)),
                                                            subSudoku.getGlobalCol(col)).getSolution(),
                                                    true, false, true);
                                            updated = updated.combineWith(nowUpdated);
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
        if (updated != SolutionProgress.SOLUTION)
        {
            for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
            {
                for (int rowBlock = 0; rowBlock < AppMain.SINGLESUDOKUMAXROWS / AppMain.RECTANGLELENGTH; rowBlock++)
                {
                    for (int colBlock = 0; colBlock < AppMain.SINGLESUDOKUMAXCOLS / AppMain.RECTANGLELENGTH; colBlock++)
                    {
                        for (LegalValues val : LegalValues.values())
                        {
                            List<Integer[]> cells = new ArrayList<Integer[]>();
                            // Same block
                            for (int rowInBlock = AppMain.RECTANGLELENGTH
                                    * rowBlock; rowInBlock < AppMain.RECTANGLELENGTH * (rowBlock + 1); rowInBlock++)
                            {
                                for (int colInBlock = AppMain.RECTANGLELENGTH
                                        * colBlock; colInBlock < AppMain.RECTANGLELENGTH * (colBlock + 1); colInBlock++)
                                {
                                    SingleCellValue sVal = subSudoku.getRowCol(rowInBlock, colInBlock);
                                    if (sVal != null && sVal.candidates.contains(val))
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
                                if (subSudoku.getRowCol(cells.get(0)[0], cells.get(0)[1]).candidates.size() == 1)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(cells.get(0)[0]),
                                            subSudoku.getGlobalCol(cells.get(0)[1]), null, true, false, true);
                                    updated = updated.combineWith(nowUpdated);
                                }
                                else
                                {
                                    for (LegalValues otherVal : LegalValues.values())
                                    {
                                        if (otherVal != val)
                                        {
                                            SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                    subSudoku.getGlobalRow(cells.get(0)[0]),
                                                    subSudoku.getGlobalCol(cells.get(0)[1]), otherVal, true, false,
                                                    true);
                                            if (nowUpdated != SolutionProgress.NONE
                                                    && subSudoku.getRowCol(cells.get(0)[0], cells.get(0)[1]).candidates
                                                            .isEmpty())
                                            {
                                                nowUpdated = masterSudoku.reduceInfluencedCellCandidates(
                                                        subSudoku.getGlobalRow(cells.get(0)[0]),
                                                        subSudoku.getGlobalCol(cells.get(0)[1]),
                                                        subSudoku.getRowCol(cells.get(0)[0], cells.get(0)[1])
                                                                .getSolution(),
                                                        true, false, true);
                                                updated = updated.combineWith(nowUpdated);
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
        }
        return updated;
    }

    // Remove candidates who are illegal in a row, column or block
    private SolutionProgress removeImpossibleCands(Values masterSudoku)
    {
        SolutionProgress updatedGlobal = SolutionProgress.NONE;
        SolutionProgress updatedSub = SolutionProgress.NONE;
        for (SubSudoku subSudoku : masterSudoku.getSudoku().getSubSudokus())
        {
            updatedSub = SolutionProgress.NONE;
            for (int row = 0; row < AppMain.SINGLESUDOKUMAXROWS; row++)
            {
                for (int col = 0; col < AppMain.SINGLESUDOKUMAXCOLS; col++)
                {
                    if (subSudoku.getRowCol(row, col).candidates.isEmpty())
                    {
                        LegalValues valToEliminate = subSudoku.getRowCol(row, col).getSolution();
                        // Same column
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            for (int rowInCol = 0; rowInCol < AppMain.SINGLESUDOKUMAXROWS; rowInCol++)
                            {
                                if (rowInCol != row)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(rowInCol), subSudoku.getGlobalCol(col),
                                            valToEliminate, true, false, true);
                                    updatedSub = updatedSub.combineWith(nowUpdated);
                                }
                            }
                        }
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            // Same row
                            for (int colInRow = 0; colInRow < AppMain.SINGLESUDOKUMAXCOLS; colInRow++)
                            {
                                if (colInRow != col)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(colInRow),
                                            valToEliminate, true, false, true);
                                    updatedSub = updatedSub.combineWith(nowUpdated);
                                }
                            }
                        }
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            // Same block
                            for (int rowInBlock = AppMain.RECTANGLELENGTH
                                    * (row / AppMain.RECTANGLELENGTH); rowInBlock < AppMain.RECTANGLELENGTH
                                            * (row / AppMain.RECTANGLELENGTH + 1); rowInBlock++)
                            {
                                for (int colInBlock = AppMain.RECTANGLELENGTH
                                        * (col / AppMain.RECTANGLELENGTH); colInBlock < AppMain.RECTANGLELENGTH
                                                * (col / AppMain.RECTANGLELENGTH + 1); colInBlock++)
                                {
                                    if (rowInBlock != row || colInBlock != col)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(rowInBlock), subSudoku.getGlobalCol(colInBlock),
                                                valToEliminate, true, false, true);
                                        updatedSub = updatedSub.combineWith(nowUpdated);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            updatedGlobal = updatedGlobal.combineWith(updatedSub);
        }
        return updatedGlobal;
    }
}

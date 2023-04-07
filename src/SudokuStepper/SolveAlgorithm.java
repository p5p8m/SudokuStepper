/**
 * 
 */
package SudokuStepper;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import SudokuStepper.Values.SudokuType;

/**
 * @author Pascal
 *
 */
public class SolveAlgorithm<LegalValuesGen extends LegalValuesGenClass> extends SudokuAction implements Runnable
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
        int noOfPossibleSolutions = 0;
        try
        {
            Values<LegalValuesGenClass> sudoku = app.getSudokuPb();
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
                if (updated == SolutionProgress.NONE && AppMain.useTripleRecognition)
                {
                    updated = detectTriples(sudoku);
                }
                if (updated == SolutionProgress.NONE)
                {
                    updated = useTryAndError(sudoku);
                }
                errorDetected = !sudoku.areContentsLegal().isEmpty();
                while (errorDetected && sudoku.isRollbackPossible())
                {
                    updated = rollbackAndTryNext(sudoku);
                    errorDetected = !sudoku.areContentsLegal().isEmpty();
                }
                newNumOfSolutions = sudoku.getNumberOfSolutions();
                loopCount++;
                System.out.println("====loopCount: " + loopCount);
                System.out.println("updated: " + updated);
                System.out.println("loopCnewNumOfSolutionsCount: " + newNumOfSolutions);
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
                // int numSol = sudoku.getNumberOfSolutions();
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

                // Check if there are possibly other solutions
                ListOfSolTraces<LegalValuesGen> firstSolution = (ListOfSolTraces<LegalValuesGen>) (app.getSudokuPb()
                        .getSolutionTrace());
                for (SolutionTrace<?> trace : firstSolution)
                {
                    ArrayList<LegalValuesGen> choices = (ArrayList<LegalValuesGen>) trace.getChoices();
                    if (choices != null)
                    {
                        noOfPossibleSolutions += choices.size();
                    }
                }
                if (noOfPossibleSolutions > 0)
                {
                    final int noOfPossibleSolutionsInt = noOfPossibleSolutions;
                    app.getDisplay().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            MessageBox infoBox = new MessageBox(new Shell(), SWT.ICON_INFORMATION);
                            infoBox.setMessage("Number of possible solutions: " + noOfPossibleSolutionsInt);
                            infoBox.open();
                        }
                    });
                }
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
            app.getDisplay().asyncExec(new SolveBtnRunnable(activateSolveBtn, noOfPossibleSolutions > 0));
            app.setSolvingThread(null);
            System.out.println("Leaving Solving thread");
        }
    }

    class SolveBtnRunnable implements Runnable
    {
        private boolean activateSolveBtn;
        private boolean activateNextSolBtn;

        SolveBtnRunnable(boolean activateSolveBtnIn, boolean activateNextSolBtnIn)
        {
            super();
            activateSolveBtn = activateSolveBtnIn;
            activateNextSolBtn = activateNextSolBtnIn;
        }

        @Override
        public void run()
        {
            app.setState(AppState.EMPTY);
            app.setSolveEnabled(activateSolveBtn, activateNextSolBtn);
        }

    }

    private SolutionProgress detectSingleCandidates(Values<LegalValuesGenClass> masterSudoku)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
        {
            // same row
            for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
            {
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    if (subSudoku.getRowCol(row, col).getCandidates().size() == 1)
                    {
                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(subSudoku.getGlobalRow(row),
                                subSudoku.getGlobalCol(col), null, true, false, true, false);
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

    private ArrayList<LegalValuesGenClass> deepCopy(ArrayList<LegalValuesGenClass> arrayList)
    {
        ArrayList<LegalValuesGenClass> retVal = null;
        if (arrayList != null)
        {
            retVal = new ArrayList<LegalValuesGenClass>(arrayList.size());
            for (LegalValuesGenClass val : arrayList)
            {
                retVal.add(val);
            }
        }
        return (retVal);
    }

    Comparator<LegalValuesGenClass> sortLegalValues = new Comparator<LegalValuesGenClass>()
    {
        public int compare(LegalValuesGenClass v1, LegalValuesGenClass v2)
        {
            return (v1.val() - v2.val());
        }
    };

    private boolean sameContents(ArrayList<LegalValuesGenClass> arrayList, ArrayList<LegalValuesGenClass> arrayList2)
    {
        boolean retVal = arrayList.size() == arrayList2.size();
        if (retVal)
        {
            for (int ind = 0; ind < arrayList.size(); ind++)
            {
                if (arrayList.get(ind) != arrayList2.get(ind))
                {
                    retVal = false;
                    break;
                }
            }
        }
        return (retVal);
    }

    private SolutionProgress rollbackAndTryNext(Values<LegalValuesGenClass> sudoku)
    {
        SolutionProgress retVal = sudoku.bifurqueOnceMore();
        return (retVal);
    }

    private SolutionProgress useTryAndError(Values<LegalValuesGenClass> masterSudoku)
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        try
        {
            int numCandidates = LegalValuesGen.values(masterSudoku.getLegalValueClass()).size();
            Integer globalRowMarked = null;
            Integer globalColMarked = null;
            for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
            {
                for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
                {
                    for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                    {
                        if (!subSudoku.getRowCol(row, col).getCandidates().isEmpty())
                        {
                            if (subSudoku.getRowCol(row, col).getCandidates().size() < numCandidates)
                            {
                                globalRowMarked = subSudoku.getGlobalRow(row);
                                globalColMarked = subSudoku.getGlobalCol(col);
                                numCandidates = subSudoku.getRowCol(row, col).getCandidates().size();
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

    private SolutionProgress detectTuples(Values<LegalValuesGenClass> sudoku) // Single sudoku
    {
        // ArrayList<ClosedTuples> retVal1 = new ArrayList<ClosedTuples>();
        SolutionProgress retVal = SolutionProgress.NONE;
        for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (sudoku.getSudoku().getSubSudokus()))
        {
            // check by rows first
            for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
            {
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    if (!sudoku.getCell(row, col).getCandidates().isEmpty())
                    {
                        // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                        // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                        // col).candidates);
                        // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                        // unionList.size());
                        for (int scndCol = col + 1; scndCol < AppMain.getSingleSudokuMaxCols(); scndCol++)
                        {
                            if (!sudoku.getCell(row, scndCol).getCandidates().isEmpty())
                            {
                                if (sameContents(sudoku.getCell(row, col).getCandidates(),
                                        sudoku.getCell(row, scndCol).getCandidates())
                                        && sudoku.getCell(row, col).getCandidates().size() == 2)
                                {
                                    // Make a deep copy to avoid problems when the list is modified within the loop
                                    List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                            sudoku.getCell(row, col).getCandidates());
                                    for (LegalValuesGenClass val : locCandidates)
                                    {
                                        for (int cleanedCol = 0; cleanedCol < AppMain
                                                .getSingleSudokuMaxCols(); cleanedCol++)
                                        {
                                            if (cleanedCol != col && cleanedCol != scndCol
                                                    && !sudoku.getCell(row, cleanedCol).getCandidates().isEmpty())
                                            {
                                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(row, cleanedCol,
                                                        val, true, false, true, false);
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
            for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
            {
                for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
                {
                    if (!sudoku.getCell(row, col).getCandidates().isEmpty())
                    {
                        // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                        // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                        // col).candidates);
                        // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                        // unionList.size());
                        for (int scndRow = row + 1; scndRow < AppMain.getSingleSudokuMaxRows(); scndRow++)
                        {
                            if (!sudoku.getCell(scndRow, col).getCandidates().isEmpty())
                            {
                                if (sameContents(sudoku.getCell(row, col).getCandidates(),
                                        sudoku.getCell(scndRow, col).getCandidates())
                                        && sudoku.getCell(row, col).getCandidates().size() == 2)
                                {
                                    // Make a deep copy to avoid problems when the list is modified whithin the loop
                                    List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                            sudoku.getCell(row, col).getCandidates());
                                    for (LegalValuesGenClass val : locCandidates)
                                    {
                                        for (int cleanedRow = 0; cleanedRow < AppMain
                                                .getSingleSudokuMaxRows(); cleanedRow++)
                                        {
                                            if (cleanedRow != row && cleanedRow != scndRow
                                                    && !sudoku.getCell(cleanedRow, col).getCandidates().isEmpty())
                                            {
                                                SolutionProgress nowUpdated = sudoku.eliminateCandidate(cleanedRow, col,
                                                        val, true, false, true, false);
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
            for (int rowBlock = 0; rowBlock < AppMain.getSingleSudokuMaxRows()
                    / AppMain.getRectangleLength(); rowBlock++)
            {
                for (int colBlock = 0; colBlock < AppMain.getSingleSudokuMaxCols()
                        / AppMain.getRectangleLength(); colBlock++)
                {
                    // Same block
                    for (int rowInBlock = AppMain.getRectangleLength() * rowBlock; rowInBlock < AppMain
                            .getRectangleLength() * (rowBlock + 1); rowInBlock++)
                    {
                        for (int colInBlock = AppMain.getRectangleLength() * colBlock; colInBlock < AppMain
                                .getRectangleLength() * (colBlock + 1); colInBlock++)
                        {
                            if (!sudoku.getCell(rowInBlock, colInBlock).getCandidates().isEmpty())
                            {
                                // List<LegalValues> unionList = deepCopy(sudoku.getCell(row, col).candidates);
                                // ArrayList<LegalValues> intersectList = deepCopy(sudoku.getCell(row,
                                // col).candidates);
                                // System.out.println("row: " + row + ", col: " + col + ", start numCands: " +
                                // unionList.size());
                                // Same block
                                for (int scndRowInBlock = AppMain.getRectangleLength()
                                        * rowBlock; scndRowInBlock < AppMain.getRectangleLength()
                                                * (rowBlock + 1); scndRowInBlock++)
                                {
                                    for (int scndColInBlock = AppMain.getRectangleLength()
                                            * colBlock; scndColInBlock < AppMain.getRectangleLength()
                                                    * (colBlock + 1); scndColInBlock++)
                                    {

                                        if (scndRowInBlock > rowInBlock
                                                || (scndRowInBlock == rowInBlock && scndColInBlock > colInBlock))
                                        {
                                            if (!sudoku.getCell(scndRowInBlock, scndColInBlock).getCandidates()
                                                    .isEmpty())
                                            {
                                                if (sameContents(sudoku.getCell(rowInBlock, colInBlock).getCandidates(),
                                                        sudoku.getCell(scndRowInBlock, scndColInBlock).getCandidates())
                                                        && sudoku.getCell(rowInBlock, colInBlock).getCandidates()
                                                                .size() == 2)
                                                {
                                                    List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                                            sudoku.getCell(rowInBlock, colInBlock).getCandidates());
                                                    for (LegalValuesGenClass val : locCandidates)
                                                    {
                                                        for (int cleanedRowInBlock = AppMain.getRectangleLength()
                                                                * rowBlock; cleanedRowInBlock < AppMain
                                                                        .getRectangleLength()
                                                                        * (rowBlock + 1); cleanedRowInBlock++)
                                                        {
                                                            for (int cleanedColInBlock = AppMain.getRectangleLength()
                                                                    * colBlock; cleanedColInBlock < AppMain
                                                                            .getRectangleLength()
                                                                            * (colBlock + 1); cleanedColInBlock++)
                                                            {
                                                                if ((cleanedRowInBlock != rowInBlock
                                                                        || cleanedColInBlock != colInBlock)
                                                                        && (cleanedRowInBlock != scndRowInBlock
                                                                                || cleanedColInBlock != scndColInBlock)
                                                                        && !sudoku
                                                                                .getCell(cleanedRowInBlock,
                                                                                        cleanedColInBlock)
                                                                                .getCandidates().isEmpty())
                                                                {
                                                                    SolutionProgress nowUpdated = sudoku
                                                                            .eliminateCandidate(cleanedRowInBlock,
                                                                                    cleanedColInBlock, val, true, false,
                                                                                    true, false);
                                                                    retVal = retVal.combineWith(nowUpdated);
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
                    }
                }
            }
        }
        return (retVal);
    }

    private SolutionProgress detectTriples(Values<LegalValuesGenClass> sudoku) // Single sudoku
    {
        // ArrayList<ClosedTuples> retVal1 = new ArrayList<ClosedTuples>();
        SolutionProgress retVal = SolutionProgress.NONE;
        for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (sudoku.getSudoku().getSubSudokus()))
        {
            // check by rows first
            for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
            {
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    if (!sudoku.getCell(row, col).getCandidates().isEmpty())
                    {
                        for (int scndCol = col + 1; scndCol < AppMain.getSingleSudokuMaxCols(); scndCol++)
                        {
                            if (!sudoku.getCell(row, scndCol).getCandidates().isEmpty())
                            {
                                for (int thrdCol = scndCol + 1; thrdCol < AppMain.getSingleSudokuMaxCols(); thrdCol++)
                                {
                                    if (!sudoku.getCell(row, thrdCol).getCandidates().isEmpty())
                                    {
                                        List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                                contentsUnion(sudoku.getCell(row, col).getCandidates(),
                                                        sudoku.getCell(row, scndCol).getCandidates(),
                                                        sudoku.getCell(row, thrdCol).getCandidates()));
                                        if (locCandidates.size() == 3)
                                        {
                                            // Make a deep copy to avoid problems when the list is modified within the
                                            // loop
                                            for (LegalValuesGenClass val : locCandidates)
                                            {
                                                for (int cleanedCol = 0; cleanedCol < AppMain
                                                        .getSingleSudokuMaxCols(); cleanedCol++)
                                                {
                                                    if (cleanedCol != col && cleanedCol != scndCol
                                                            && cleanedCol != thrdCol && !sudoku.getCell(row, cleanedCol)
                                                                    .getCandidates().isEmpty())
                                                    {
                                                        SolutionProgress nowUpdated = sudoku.eliminateCandidate(row,
                                                                cleanedCol, val, true, false, true, false);
                                                        retVal = retVal.combineWith(nowUpdated);
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
            // check by columns then
            for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
            {
                for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
                {
                    if (!sudoku.getCell(row, col).getCandidates().isEmpty())
                    {
                        for (int scndRow = row + 1; scndRow < AppMain.getSingleSudokuMaxRows(); scndRow++)
                        {
                            if (!sudoku.getCell(scndRow, col).getCandidates().isEmpty())
                            {
                                for (int thrdRow = scndRow + 1; thrdRow < AppMain.getSingleSudokuMaxCols(); thrdRow++)
                                {
                                    if (!sudoku.getCell(thrdRow, col).getCandidates().isEmpty())
                                    {
                                        List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                                contentsUnion(sudoku.getCell(row, col).getCandidates(),
                                                        sudoku.getCell(scndRow, col).getCandidates(),
                                                        sudoku.getCell(thrdRow, col).getCandidates()));
                                        if (locCandidates.size() == 3)
                                        {
                                            // Make a deep copy to avoid problems when the list is modified within the
                                            // loop
                                            for (LegalValuesGenClass val : locCandidates)
                                            {
                                                for (int cleanedRow = 0; cleanedRow < AppMain
                                                        .getSingleSudokuMaxCols(); cleanedRow++)
                                                {
                                                    if (cleanedRow != row && cleanedRow != scndRow
                                                            && cleanedRow != thrdRow && !sudoku.getCell(cleanedRow, col)
                                                                    .getCandidates().isEmpty())
                                                    {
                                                        SolutionProgress nowUpdated = sudoku.eliminateCandidate(
                                                                cleanedRow, col, val, true, false, true, false);
                                                        retVal = retVal.combineWith(nowUpdated);
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
            // check by blocks finally
            for (int rowBlock = 0; rowBlock < AppMain.getSingleSudokuMaxRows()
                    / AppMain.getRectangleLength(); rowBlock++)
            {
                for (int colBlock = 0; colBlock < AppMain.getSingleSudokuMaxCols()
                        / AppMain.getRectangleLength(); colBlock++)
                {
                    // Same block
                    for (int rowInBlock = AppMain.getRectangleLength() * rowBlock; rowInBlock < AppMain
                            .getRectangleLength() * (rowBlock + 1); rowInBlock++)
                    {
                        for (int colInBlock = AppMain.getRectangleLength() * colBlock; colInBlock < AppMain
                                .getRectangleLength() * (colBlock + 1); colInBlock++)
                        {
                            if (!sudoku.getCell(rowInBlock, colInBlock).getCandidates().isEmpty())
                            {
                                for (int scndRowInBlock = AppMain.getRectangleLength()
                                        * rowBlock; scndRowInBlock < AppMain.getRectangleLength()
                                                * (rowBlock + 1); scndRowInBlock++)
                                {
                                    for (int scndColInBlock = AppMain.getRectangleLength()
                                            * colBlock; scndColInBlock < AppMain.getRectangleLength()
                                                    * (colBlock + 1); scndColInBlock++)
                                    {

                                        if (scndRowInBlock > rowInBlock
                                                || (scndRowInBlock == rowInBlock && scndColInBlock > colInBlock))
                                        {
                                            if (!sudoku.getCell(scndRowInBlock, scndColInBlock).getCandidates()
                                                    .isEmpty())
                                            {
                                                for (int thrdRowInBlock = AppMain.getRectangleLength()
                                                        * rowBlock; thrdRowInBlock < AppMain.getRectangleLength()
                                                                * (rowBlock + 1); thrdRowInBlock++)
                                                {
                                                    for (int thrdColInBlock = AppMain.getRectangleLength()
                                                            * colBlock; thrdColInBlock < AppMain.getRectangleLength()
                                                                    * (colBlock + 1); thrdColInBlock++)
                                                    {
                                                        if (thrdRowInBlock > scndRowInBlock
                                                                || (thrdRowInBlock == scndRowInBlock
                                                                        && thrdColInBlock > scndColInBlock))
                                                        {
                                                            if (!sudoku.getCell(thrdRowInBlock, thrdColInBlock)
                                                                    .getCandidates().isEmpty())
                                                            {
                                                                List<LegalValuesGenClass> locCandidates = new ArrayList<LegalValuesGenClass>(
                                                                        contentsUnion(
                                                                                sudoku.getCell(rowInBlock, colInBlock)
                                                                                        .getCandidates(),
                                                                                sudoku.getCell(scndRowInBlock,
                                                                                        scndColInBlock).getCandidates(),
                                                                                sudoku.getCell(thrdRowInBlock,
                                                                                        thrdColInBlock)
                                                                                        .getCandidates()));
                                                                if (locCandidates.size() == 3)
                                                                {
                                                                    // Make a deep copy to avoid problems when the list
                                                                    // is
                                                                    // modified within the loop
                                                                    for (LegalValuesGenClass val : locCandidates)
                                                                    {
                                                                        for (int cleanedRowInBlock = AppMain
                                                                                .getRectangleLength()
                                                                                * rowBlock; cleanedRowInBlock < AppMain
                                                                                        .getRectangleLength()
                                                                                        * (rowBlock
                                                                                                + 1); cleanedRowInBlock++)
                                                                        {
                                                                            for (int cleanedColInBlock = AppMain
                                                                                    .getRectangleLength()
                                                                                    * colBlock; cleanedColInBlock < AppMain
                                                                                            .getRectangleLength()
                                                                                            * (colBlock
                                                                                                    + 1); cleanedColInBlock++)
                                                                            {
                                                                                if ((!((cleanedRowInBlock == rowInBlock
                                                                                        && cleanedColInBlock == colInBlock)
                                                                                        || (cleanedRowInBlock == scndRowInBlock
                                                                                                && cleanedColInBlock == scndColInBlock)
                                                                                        || (cleanedRowInBlock == thrdRowInBlock
                                                                                                && cleanedColInBlock == thrdColInBlock)))
                                                                                        && !sudoku.getCell(
                                                                                                cleanedRowInBlock,
                                                                                                cleanedColInBlock)
                                                                                                .getCandidates()
                                                                                                .isEmpty())
                                                                                {
                                                                                    SolutionProgress nowUpdated = sudoku
                                                                                            .eliminateCandidate(
                                                                                                    cleanedRowInBlock,
                                                                                                    cleanedColInBlock,
                                                                                                    val, true, false,
                                                                                                    true, false);
                                                                                    retVal = retVal
                                                                                            .combineWith(nowUpdated);
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

    private ArrayList<LegalValuesGenClass> contentsUnion(ArrayList<LegalValuesGenClass> arrayList,
            ArrayList<LegalValuesGenClass> arrayList2, ArrayList<LegalValuesGenClass> arrayList3)
    {
        // None of the input parameter may be null
        ArrayList<LegalValuesGenClass> retVal = deepCopy(arrayList);
        for (LegalValuesGenClass val : arrayList2)
        {
            if (!retVal.contains(val))
            {
                retVal.add(val);
            }
        }
        for (LegalValuesGenClass val : arrayList3)
        {
            if (!retVal.contains(val))
            {
                retVal.add(val);
            }
        }
        return retVal;
    }

    // Detect if there is only one single cell in a row, column or block that still
    // accepts a given value
    private SolutionProgress detectUniqueMatches(Values<LegalValuesGenClass> masterSudoku)
    {
        SolutionProgress updated = SolutionProgress.NONE;
        // same row
        if (updated != SolutionProgress.SOLUTION)
        {
            for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
            {
                for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
                {
                    for (LegalValuesGenClass val : LegalValuesGen.values(masterSudoku.getLegalValueClass()))
                    {
                        List<Integer> cols = new ArrayList<Integer>();
                        for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                        {
                            if (subSudoku.getRowCol(row, col).getCandidates().contains(val))
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
                            if (subSudoku.getRowCol(row, cols.get(0)).getCandidates().size() == 1)
                            {
                                SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                        subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(cols.get(0)), null, true,
                                        false, true, false);
                                updated = updated.combineWith(nowUpdated);
                            }
                            else
                            {
                                for (LegalValuesGenClass otherVal : LegalValuesGen
                                        .values(masterSudoku.getLegalValueClass()))
                                {
                                    if (otherVal != val)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(cols.get(0)),
                                                otherVal, true, false, true, false);
                                        updated = updated.combineWith(nowUpdated);
                                        // next block superfluous?
                                        if (nowUpdated != SolutionProgress.NONE
                                                && subSudoku.getRowCol(row, cols.get(0)).getCandidates().isEmpty())
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
            for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
            {
                // same column
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    for (LegalValuesGenClass val : LegalValuesGen.values(masterSudoku.getLegalValueClass()))
                    {
                        List<Integer> rows = new ArrayList<Integer>();
                        for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
                        {
                            if (subSudoku.getRowCol(row, col).getCandidates().contains(val))
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
                            if (subSudoku.getRowCol(rows.get(0), col).getCandidates().size() == 1)
                            {
                                SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                        subSudoku.getGlobalRow(rows.get(0)), subSudoku.getGlobalCol(col), null, true,
                                        false, true, false);
                                updated = updated.combineWith(nowUpdated);
                            }
                            else
                            {
                                for (LegalValuesGenClass otherVal : LegalValuesGen
                                        .values(masterSudoku.getLegalValueClass()))
                                {
                                    if (otherVal != val)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(rows.get(0)), subSudoku.getGlobalCol(col),
                                                otherVal, true, false, true, false);
                                        if (nowUpdated != SolutionProgress.NONE
                                                && subSudoku.getRowCol(rows.get(0), col).getCandidates().isEmpty())
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
            for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
            {
                for (int rowBlock = 0; rowBlock < AppMain.getSingleSudokuMaxRows()
                        / AppMain.getRectangleLength(); rowBlock++)
                {
                    for (int colBlock = 0; colBlock < AppMain.getSingleSudokuMaxCols()
                            / AppMain.getRectangleLength(); colBlock++)
                    {
                        for (LegalValuesGenClass val : LegalValuesGen.values(masterSudoku.getLegalValueClass()))
                        {
                            List<Integer[]> cells = new ArrayList<Integer[]>();
                            // Same block
                            for (int rowInBlock = AppMain.getRectangleLength() * rowBlock; rowInBlock < AppMain
                                    .getRectangleLength() * (rowBlock + 1); rowInBlock++)
                            {
                                for (int colInBlock = AppMain.getRectangleLength() * colBlock; colInBlock < AppMain
                                        .getRectangleLength() * (colBlock + 1); colInBlock++)
                                {
                                    SingleCellValue sVal = subSudoku.getRowCol(rowInBlock, colInBlock);
                                    if (sVal != null && sVal.getCandidates().contains(val))
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
                                if (subSudoku.getRowCol(cells.get(0)[0], cells.get(0)[1]).getCandidates().size() == 1)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(cells.get(0)[0]),
                                            subSudoku.getGlobalCol(cells.get(0)[1]), null, true, false, true, false);
                                    updated = updated.combineWith(nowUpdated);
                                }
                                else
                                {
                                    for (LegalValuesGenClass otherVal : LegalValuesGen
                                            .values(masterSudoku.getLegalValueClass()))
                                    {
                                        if (otherVal != val)
                                        {
                                            SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                    subSudoku.getGlobalRow(cells.get(0)[0]),
                                                    subSudoku.getGlobalCol(cells.get(0)[1]), otherVal, true, false,
                                                    true, false);
                                            if (nowUpdated != SolutionProgress.NONE
                                                    && subSudoku.getRowCol(cells.get(0)[0], cells.get(0)[1])
                                                            .getCandidates().isEmpty())
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
    private SolutionProgress removeImpossibleCands(Values<LegalValuesGenClass> masterSudoku)
    {
        SolutionProgress updatedGlobal = SolutionProgress.NONE;
        SolutionProgress updatedSub = SolutionProgress.NONE;
        for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (masterSudoku.getSudoku().getSubSudokus()))
        {
            updatedSub = SolutionProgress.NONE;
            for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
            {
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    if (subSudoku.getRowCol(row, col).getCandidates().isEmpty())
                    {
                        LegalValuesGenClass valToEliminate = subSudoku.getRowCol(row, col).getSolution();
                        // Same column
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            for (int rowInCol = 0; rowInCol < AppMain.getSingleSudokuMaxRows(); rowInCol++)
                            {
                                if (rowInCol != row)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(rowInCol), subSudoku.getGlobalCol(col),
                                            valToEliminate, true, false, true, false);
                                    updatedSub = updatedSub.combineWith(nowUpdated);
                                }
                            }
                        }
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            // Same row
                            for (int colInRow = 0; colInRow < AppMain.getSingleSudokuMaxCols(); colInRow++)
                            {
                                if (colInRow != col)
                                {
                                    SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                            subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(colInRow),
                                            valToEliminate, true, false, true, false);
                                    updatedSub = updatedSub.combineWith(nowUpdated);
                                }
                            }
                        }
                        if (updatedSub != SolutionProgress.SOLUTION)
                        {
                            // Same block
                            for (int rowInBlock = AppMain.getRectangleLength()
                                    * (row / AppMain.getRectangleLength()); rowInBlock < AppMain.getRectangleLength()
                                            * (row / AppMain.getRectangleLength() + 1); rowInBlock++)
                            {
                                for (int colInBlock = AppMain.getRectangleLength()
                                        * (col / AppMain.getRectangleLength()); colInBlock < AppMain
                                                .getRectangleLength()
                                                * (col / AppMain.getRectangleLength() + 1); colInBlock++)
                                {
                                    if (rowInBlock != row || colInBlock != col)
                                    {
                                        SolutionProgress nowUpdated = masterSudoku.eliminateCandidate(
                                                subSudoku.getGlobalRow(rowInBlock), subSudoku.getGlobalCol(colInBlock),
                                                valToEliminate, true, false, true, false);
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

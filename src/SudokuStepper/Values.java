package SudokuStepper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

// import SudokuStepper.SolutionTrace;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * 
 */
interface CandidatesListener
{
    <LegalValuesGen extends LegalValuesGenClass> void candidatesUpdated(int row, int col, LegalValuesGen val);
}

interface CandidatesResetListener // <LegalValuesGen extends LegalValuesGenClass>
{
    public <LegalValuesGen extends LegalValuesGenClass> void candidatesReset();
}

interface RollbackListener
{
    void rollbackSudoku();
}

interface SavedListener
{
    void savedUpdated(boolean saved, boolean runsInUiThread);
}

// interface NewStartListener
// {
// void newStarted();
// }

interface FreezeListener
{
    void freeze();
}

/**
 * @author Pascal Represents the complete Sudoku Samurai
 *
 */
public class Values<LegalValuesGen extends LegalValuesGenClass>
{

    public enum SudokuType
    {
        SINGLE, SAMURAI
    }

    private Stack<Tentative<LegalValuesGen>>   sudokuCands              = new Stack<Tentative<LegalValuesGen>>();
    private String                             sudokuName               = null;
    private SudokuType                         sudokuType               = SudokuType.SINGLE;
    private Class<?>                           legalValuesClass         = LegalValues_9.class;
    private String                             inputFile                = null;
    private boolean                            saved                    = true;
    private AppMain                            appMain                  = null;
    private ListOfSolTraces<LegalValuesGen>    solutionTrace            = new ListOfSolTraces<LegalValuesGen>();

    private ArrayList<SolutionListener>        solutionListeners        = new ArrayList<SolutionListener>();
    private ArrayList<CandidatesListener>      candidatesListeners      = new ArrayList<CandidatesListener>();
    private ArrayList<CandidatesResetListener> candidatesResetListeners = new ArrayList<CandidatesResetListener>();
    private ArrayList<RollbackListener>        rollbackListeners        = new ArrayList<RollbackListener>();
    private ArrayList<SavedListener>           savedListeners           = new ArrayList<SavedListener>();

    // private List<NewStartListener> newStartListeners = new
    // ArrayList<NewStartListener>();
    public int getNumberOfCellsToBeSolved()
    {
        int retVal = 0;
        switch (sudokuType)
        {
        case SAMURAI:
            retVal = AppMain.getMaxRows() * AppMain.getMaxCols()
                    - 8 * AppMain.getRectangleLength() * AppMain.getRectangleLength();
            break;
        case SINGLE:
        default:
            retVal = AppMain.getSingleSudokuMaxCols() * AppMain.getSingleSudokuMaxRows();
            break;
        }
        return (retVal);
    }

    public void addToSolutionTrace(Values<LegalValuesGen> values, int globalRow, int globalCol,
            LegalValuesGen eliminatedVal, ArrayList<LegalValuesGen> candidates)
    {
        solutionTrace.addToSolutionTrace(values, globalRow, globalCol, eliminatedVal, candidates);
    }

    public boolean isSaved()
    {
        return saved;
    }

    public AppMain getApp()
    {
        return (appMain);
    }

    public void setSaved(boolean saved)
    {
        this.saved = saved;
    }

    public String getName()
    {
        String retVal = sudokuName;
        if (retVal == null)
        {
            retVal = StringUtils.EMPTY;
        }
        return (retVal);
    }

    public SudokuType getType()
    {
        return (sudokuType);
    }

    public Class<?> getLegalValueClass()
    {
        return (legalValuesClass);
    }

    // public SubAreaWidth getSubAreaWidth()
    // {
    // return (singleSu);
    // }

    public SolutionProgress addBifurcationNClone(int globalRow, int globalCol) throws InterruptedException
    {
        // // For debugging
        // try
        // {
        // Values pb = new Values();
        // Tentative oldSudoku = pb.sudokuCands.peek();
        // oldSudoku.setBifurcation(0, 0);
        // oldSudoku.getSudoku()[1][1].setSolution(LegalValuesGen.NINE, 1, 1, new
        // ArrayList<SolutionListener>(), true,
        // false);
        // Tentative newSudoku = new Tentative(oldSudoku);
        // pb.sudokuCands.push(newSudoku);
        // oldSudoku.getSudoku()[1][1].setSolution(LegalValuesGen.ONE, 1, 1, new
        // ArrayList<SolutionListener>(), true,
        // false);
        // oldSudoku.getSudoku()[0][0].candidates.remove(8);
        // pb.sudokuCands.peek().getSudoku()[1][1].setSolution(LegalValuesGen.TWO, 1, 1,
        // new ArrayList<SolutionListener>(), true, false);
        // pb.sudokuCands.peek().getSudoku()[0][1].candidates.removeIf(x -> x ==
        // LegalValuesGen.FIVE);
        // // System.out.println(System.identityHashCode(array[i]));
        // }
        // catch (Exception e)
        // {
        // System.out.println(e.getMessage());
        // e.printStackTrace();
        // }
        // // end of debugging section
        SolutionProgress retVal = SolutionProgress.NONE;
        Tentative<LegalValuesGen> oldSudoku = sudokuCands.peek();
        Tentative<LegalValuesGen> newSudoku = new Tentative<LegalValuesGen>(oldSudoku, sudokuType);
        LegalValuesGen toBeEliminatedVal = (LegalValuesGen) oldSudoku.setBifurcation(globalRow, globalCol);
        System.out.println("Try and Error with row: " + globalRow + ", col: " + globalCol + ", eliminating value: "
                + toBeEliminatedVal.val());
        sudokuCands.push(newSudoku);
        retVal = eliminateCandidate(globalRow, globalCol, toBeEliminatedVal, true, false, true, true);
        oldSudoku.getSudoku().getRowCol(globalRow, globalCol).setTryNError(true);
        return (retVal);
    }

    public boolean isRollbackPossible()
    {
        return (sudokuCands.size() > 1);
    }

    public SolutionProgress bifurqueOnceMore() throws InterruptedException
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        Bifurcation<LegalValuesGen> nextTry = null;
        Tentative<LegalValuesGen> oldSudoku = null;
        while (nextTry == null && sudokuCands.size() > 1)
        {
            sudokuCands.pop();
            oldSudoku = sudokuCands.peek();
            nextTry = oldSudoku.getNextTry();
        }
        if (nextTry != null)
        {
            Tentative<LegalValuesGen> newSudoku = new Tentative<LegalValuesGen>(oldSudoku, sudokuType);
            sudokuCands.push(newSudoku);
            for (RollbackListener listener : rollbackListeners)
            {
                listener.rollbackSudoku();
            }
            int row = nextTry.getRow();
            int col = nextTry.getCol();
            LegalValuesGen toBeEliminatedVal = nextTry.getNextTry();
            System.out.println("Try and Error rollback with row: " + row + ", col: " + col + ", now eliminating value: "
                    + toBeEliminatedVal.val());
            retVal = eliminateCandidate(row, col, toBeEliminatedVal, true, false, true, true);
            oldSudoku.getSudoku().getRowCol(row, col).setTryNError(true);
        }
        return (retVal);
    }

    public void setName(String input)
    {
        if (input == null)
        {
            sudokuName = StringUtils.EMPTY;
        }
        else
        {
            sudokuName = input.trim();
        }
    }

    public MasterSudoku<?> getSudoku()
    {
        return (sudokuCands.peek().getSudoku());
    }

    public String getInputFile()
    {
        String retVal = inputFile;
        if (retVal == null)
        {
            retVal = StringUtils.EMPTY;
        }
        return (retVal);
    }

    public SingleCellValue<LegalValuesGen> getCell(int globalRow, int globalCol)
    {
        return (getSudoku().getRowCol(globalRow, globalCol));
    }

    public void resetCell(Class<?> legalValClass, int globalRow, int globalCol)
    {
        getSudoku().resetCell(legalValClass, globalRow, globalCol);
    }

    public Values(SudokuType type, Class<?> legalValClass, AppMain app)
    {
        sudokuCands.push(new Tentative<LegalValuesGen>(type, this, AppMain.getCandidatesNumber()));
        sudokuType = type;
        legalValuesClass = legalValClass;
        appMain = app;
        reset(legalValClass);
    }

    public Values()
    {

    }

    public void initCell(int row, int col, int value, boolean runsInUiThread, boolean markLastSolutionFound,
            boolean isAnInput, boolean isATry) throws InvalidValueException
    {
        try
        {
            row -= 1;
            col -= 1;
            LegalValuesGen val = (LegalValuesGen) (legalValuesClass.getConstructor(int.class).newInstance(value));
            MasterSudoku<?> sudoku = getSudoku();
            sudoku.getRowCol(row, col).setSolution(val, row, col, null, runsInUiThread, markLastSolutionFound);
            sudoku.getRowCol(row, col).setInput(isAnInput);
            sudoku.getRowCol(row, col).setTryNError(isATry);
            sudoku.getRowCol(row, col).getCandidates().clear();
            setSaved(false);
        }
        catch (Exception ex)
        {
            throw new InvalidValueException(Integer.toString(value));
        }
    }

    public void addSolutionListener(SolutionListener listener)
    {
        solutionListeners.add(listener);
    }

    public void addCandidatesListener(CandidatesListener listener)
    {
        candidatesListeners.add(listener);
    }

    public void addCandidatesResetListener(CandidatesResetListener listener)
    {
        candidatesResetListeners.add(listener);
    }

    public void addRollbackListener(RollbackListener listener)
    {
        rollbackListeners.add(listener);
    }

    public void addSavedListener(SavedListener listener)
    {
        savedListeners.add(listener);
    }

    // public void newStartListener(NewStartListener listener)
    // {
    // newStartListeners.add(listener);
    // }

    // returns true if an update has occurred, false else
    // To be used when solving a sudoku
    // alsoSetSolution: if true candidates lists with only one member are converted
    // to a solution, this is for SOLVING only
    // If values to eliminate (=val) is null, only check if we can set the only
    // remaining candidate as a solution

    public SolutionProgress eliminateCandidate(int globalRow, int globalCol, LegalValuesGen val,
            boolean alsoSetSolution, boolean runsInUiThread, boolean markLastSolutionFound, boolean isATry)
            throws InterruptedException
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        MasterSudoku<?> sudoku = getSudoku();
        if (val == null || sudoku.getRowCol(globalRow, globalCol).getCandidates().contains(val))
        {
            retVal = retVal.combineWith(SolutionProgress.CANDIDATES);
            if (val != null)
            {
                sudoku.getRowCol(globalRow, globalCol).getCandidates().remove(val);
                System.out.println("Row: " + globalRow + ", Col: " + globalCol + ", Eliminated: " + val.val()
                        + ", Number of remaining values: "
                        + sudoku.getRowCol(globalRow, globalCol).getCandidates().size());
                for (CandidatesListener listener : candidatesListeners)
                {
                    listener.candidatesUpdated(globalRow, globalCol, val);
                }
            }
            setSaved(false);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved(), runsInUiThread);
            }
            Thread threadNeedingToWait = null;
            if (sudoku.getRowCol(globalRow, globalCol).getCandidates().size() == 1)
            {
                if (alsoSetSolution)
                {
                    LegalValuesGen solution = (LegalValuesGen) sudoku.getRowCol(globalRow, globalCol).getCandidates()
                            .get(0);
                    retVal = retVal.combineWith(SolutionProgress.SOLUTION);
                    sudoku.getRowCol(globalRow, globalCol).setInput(false);
                    sudoku.getRowCol(globalRow, globalCol).setTryNError(isATry);
                    sudoku.getRowCol(globalRow, globalCol).getCandidates().clear();
                    // Must be the last thing because the thread could end if step by step mode
                    threadNeedingToWait = sudoku.getRowCol(globalRow, globalCol).setSolution(solution, globalRow,
                            globalCol, solutionListeners, runsInUiThread, markLastSolutionFound);
                }
                SolutionProgress newUpdated = reduceInfluencedCellCandidates(globalRow, globalCol,
                        (LegalValuesGen) sudoku.getRowCol(globalRow, globalCol).getSolution(),
                        alsoSetSolution && threadNeedingToWait == null, runsInUiThread, markLastSolutionFound);
                retVal = retVal.combineWith(newUpdated);
                if (threadNeedingToWait != null)
                {
                    // End thread
                    synchronized (threadNeedingToWait)
                    {
                        threadNeedingToWait.wait();
                    }
                }
            }
        }
        return (retVal);
    }

    void resetCandidates(Class<?> legalValuesClass)
    {
        for (int globalRow = 0; globalRow < AppMain.getMaxRows(); globalRow++)
        {
            for (int globalCol = 0; globalCol < AppMain.getMaxCols(); globalCol++)
            {
                SingleCellValue<?> cell = getCell(globalRow, globalCol);
                if (cell.getSolution() == null)
                {
                    cell.initCandidates(legalValuesClass);
                }
            }
        }
        for (CandidatesResetListener listener : candidatesResetListeners)
        {
            listener.candidatesReset();
        }
    }

    // remove the value just set in the given cell the list of candidates from
    // all influenced cells, for master sudoku
    public void updateCandidateList(int globalRow, int globalCol, LegalValuesGen val, boolean runsInUiThread,
            boolean markLastSolutionFound) throws InterruptedException
    {
        MasterSudoku<?> masterSudoku = getSudoku();
        for (SubSudoku subSudoku : (List<SubSudoku>) (masterSudoku.isRowColShared(globalRow, globalCol)))
        {
            int localRow = subSudoku.getLocalRow(globalRow);
            int localCol = subSudoku.getLocalCol(globalCol);
            if (subSudoku.getRowCol(localRow, localCol).getSolution() != null)
            { // First undo the value restrictions due to the previous value, but only where
              // not another cell continues justifying them
                LegalValuesGen oldVal = (LegalValuesGen) subSudoku.getRowCol(localRow, localCol).getSolution();
                // Same column
                for (int rowInCol = 0; rowInCol < AppMain.getSingleSudokuMaxRows(); rowInCol++)
                {
                    if (!subSudoku.getRowCol(rowInCol, localCol).getCandidates().contains(oldVal)
                            && isValueACandidate(rowInCol, localCol, oldVal))
                    {
                        subSudoku.getRowCol(rowInCol, localCol).getCandidates().add(oldVal);
                    }
                }
                // Same row
                for (int colInRow = 0; colInRow < AppMain.getSingleSudokuMaxCols(); colInRow++)
                {
                    if (!subSudoku.getRowCol(localRow, colInRow).getCandidates().contains(oldVal)
                            && isValueACandidate(localRow, colInRow, oldVal))
                    {
                        subSudoku.getRowCol(localRow, colInRow).getCandidates().add(oldVal);
                    }
                }
                // Same block
                for (int rowInBlock = AppMain.getRectangleLength()
                        * (localRow / AppMain.getRectangleLength()); rowInBlock < AppMain.getRectangleLength()
                                * (localRow / AppMain.getRectangleLength() + 1); rowInBlock++)
                {
                    for (int colInBlock = AppMain.getRectangleLength()
                            * (localCol / AppMain.getRectangleLength()); colInBlock < AppMain.getRectangleLength()
                                    * (localCol / AppMain.getRectangleLength() + 1); colInBlock++)
                    {
                        if (!subSudoku.getRowCol(rowInBlock, colInBlock).getCandidates().contains(oldVal)
                                && isValueACandidate(rowInBlock, colInBlock, oldVal))
                        {
                            subSudoku.getRowCol(rowInBlock, colInBlock).getCandidates().add(oldVal);
                        }
                    }
                }
            }
            if (val != null)
            {
                reduceInfluencedCellCandidates(globalRow, globalCol, val, false, runsInUiThread, markLastSolutionFound);
            }
        }
    }

    // Check if the value is a possible candidate based on the values already set in
    // all influencing cells, val should not be null
    // Check is performed for a single sudoku, use local coordinates
    private boolean isValueACandidate(int localRow, int localCol, LegalValuesGen val)
    {
        boolean retVal = true;
        MasterSudoku<?> sudoku = getSudoku();
        // Same column
        for (int rowInCol = 0; rowInCol < AppMain.getSingleSudokuMaxRows(); rowInCol++)
        {
            if (localRow != rowInCol && sudoku.getRowCol(rowInCol, localCol).getSolution() != null
                    && val.equals(sudoku.getRowCol(rowInCol, localCol).getSolution().val()))
            {
                retVal = false;
                break;
            }
        }
        // Same row
        if (retVal)
        {
            for (int colInRow = 0; colInRow < AppMain.getSingleSudokuMaxCols(); colInRow++)
            {
                if (localCol != colInRow && sudoku.getRowCol(localRow, colInRow).getSolution() != null
                        && val.equals(sudoku.getRowCol(localRow, colInRow).getSolution().val()))
                {
                    retVal = false;
                    break;
                }
            }
        }
        // Same block
        if (retVal)
        {
            for (int rowInBlock = AppMain.getRectangleLength()
                    * (localRow / AppMain.getRectangleLength()); rowInBlock < AppMain.getRectangleLength()
                            * (localRow / AppMain.getRectangleLength() + 1); rowInBlock++)
            {
                for (int colInBlock = AppMain.getRectangleLength()
                        * (localCol / AppMain.getRectangleLength()); colInBlock < AppMain.getRectangleLength()
                                * (localCol / AppMain.getRectangleLength() + 1); colInBlock++)
                {
                    if ((localCol != colInBlock || localRow != rowInBlock)
                            && sudoku.getRowCol(rowInBlock, colInBlock).getSolution() != null
                            && val.equals(sudoku.getRowCol(rowInBlock, colInBlock).getSolution().val()))
                    {
                        retVal = false;
                        break;
                    }
                }
            }
        }
        return (retVal);
    }

    // remove unconditionally from the value just set in the given cell the list of
    // candidates from
    // all influenced cells
    SolutionProgress reduceInfluencedCellCandidates(int globalRow, int globalCol, LegalValuesGen val,
            boolean alsoSetSolution, boolean runsInUiThread, boolean markLastSolutionFound) throws InterruptedException
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        MasterSudoku<?> masterSudoku = getSudoku();
        for (SubSudoku subSudoku : (List<SubSudoku>) (masterSudoku.isRowColShared(globalRow, globalCol)))
        {
            int localRow = subSudoku.getLocalRow(globalRow);
            int localCol = subSudoku.getLocalCol(globalCol);
            // Same column
            for (int rowInCol = 0; rowInCol < AppMain.getSingleSudokuMaxRows(); rowInCol++)
            {
                if (subSudoku.getRowCol(rowInCol, localCol).getCandidates().contains(val))
                {
                    SolutionProgress nowUpdated = eliminateCandidate(subSudoku.getGlobalRow(rowInCol), globalCol, val,
                            alsoSetSolution, runsInUiThread, markLastSolutionFound, false);
                    retVal = retVal.combineWith(nowUpdated);
                    // sudoku[rowInCol, col].candidates.remove(val);
                    // for (CandidatesListener listener : candidatesListeners)
                    // {
                    // listener.candidatesUpdated(rowInCol, col, val);
                    // }
                }
            }
            // Same row
            for (int colInRow = 0; colInRow < AppMain.getSingleSudokuMaxCols(); colInRow++)
            {
                if (subSudoku.getRowCol(localRow, colInRow).getCandidates().contains(val))
                {
                    SolutionProgress nowUpdated = eliminateCandidate(globalRow, subSudoku.getGlobalCol(colInRow), val,
                            alsoSetSolution, runsInUiThread, markLastSolutionFound, false);
                    retVal = retVal.combineWith(nowUpdated);
                    // sudoku[row, colInRow].candidates.remove(val);
                    // for (CandidatesListener listener : candidatesListeners)
                    // {
                    // listener.candidatesUpdated(row, colInRow, val);
                    // }
                }
            }
            // Same block
            for (int rowInBlock = AppMain.getRectangleLength()
                    * (localRow / AppMain.getRectangleLength()); rowInBlock < AppMain.getRectangleLength()
                            * (localRow / AppMain.getRectangleLength() + 1); rowInBlock++)
            {
                for (int colInBlock = AppMain.getRectangleLength()
                        * (localCol / AppMain.getRectangleLength()); colInBlock < AppMain.getRectangleLength()
                                * (localCol / AppMain.getRectangleLength() + 1); colInBlock++)
                {
                    if (subSudoku.getRowCol(rowInBlock, colInBlock).getCandidates().contains(val))
                    {
                        SolutionProgress nowUpdated = eliminateCandidate(subSudoku.getGlobalRow(rowInBlock),
                                subSudoku.getGlobalCol(colInBlock), val, alsoSetSolution, runsInUiThread,
                                markLastSolutionFound, false);
                        retVal = retVal.combineWith(nowUpdated);
                        // sudoku[rowInBlock, colInBlock].candidates.remove(val);
                        // for (CandidatesListener listener : candidatesListeners)
                        // {
                        // listener.candidatesUpdated(rowInBlock, colInBlock, val);
                        // }
                    }
                }
            }
        }
        return (retVal);
    }

    public void reset(Class<?> legalValClass)
    {
        getSudoku().reset(legalValClass);
    }

    private static String SCHEMAFILENAMEDBG  = "SudokuStepper\\SudokuStepper.xsd";
    private static String SCHEMAFILENAMEJAR  = "SudokuStepper.xsd";
    private static String SUDOKU             = "sudoku";
    private static String INITIAL            = "initial";
    private static String SOLUTION           = "solution";
    private static String PROGRESS           = "progress";
    private static String CONTENT            = "content";
    private static String ROW                = "row";
    private static String COL                = "col";
    private static String CHOICES            = "choices";
    private static String SUDOKUNAME         = "name";
    private static String SUDOKUTYPE         = "type";
    private static String HIGHESTVALUEINCELL = "highestValueInCell";
    private static String SEPARATOR          = ", ";

    public SudokuType read(String fromFile, boolean alsoReadSolution)
            throws InvalidValueException, ParserConfigurationException, SAXException, IOException
    {
        SudokuType retVal = SudokuType.SINGLE;
        reset(this.appMain.getLegalValClassUi()); // as a default initialization
        if (validateXmlSchema(fromFile))
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setErrorHandler(new XmlErrorHandler());
            Document doc = dBuilder.parse((new File(fromFile)).toURI().toString());
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            sudokuName = doc.getDocumentElement().getAttribute(SUDOKUNAME);
            String sudokuTypeString = doc.getDocumentElement().getAttribute(SUDOKUTYPE);

            try
            {
                retVal = SudokuType.valueOf(sudokuTypeString.toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                retVal = SudokuType.SINGLE;
            }
            String highestValueInCell = doc.getDocumentElement().getAttribute(HIGHESTVALUEINCELL);
            try
            {
                int highestValueInCellInt = Integer.parseInt(highestValueInCell);
                switch (highestValueInCellInt)
                {
                case 25:
                    legalValuesClass = LegalValues_25.class;
                    break;
                case 16:
                    legalValuesClass = LegalValues_16.class;
                    break;
                case 4:
                    legalValuesClass = LegalValues_4.class;
                    break;
                case 9:
                default:
                    legalValuesClass = LegalValues_9.class;
                    break;
                }
                getSudoku().updateCandidatesNumber(legalValuesClass, highestValueInCellInt);
            }
            catch (IllegalArgumentException ex) // also occurs when no explicit attribute present
            {
                legalValuesClass = LegalValues_9.class;
            }

            getSudoku().setSudokuType(retVal);
            NodeList initialContents = doc.getElementsByTagName(INITIAL); // Only one expected
            NodeList solutionContents = doc.getElementsByTagName(SOLUTION); // Only one expected
            List<NodeList> allContents = new ArrayList<NodeList>();
            allContents.add(initialContents);
            if (alsoReadSolution)
            {
                allContents.add(solutionContents);
            }
            for (NodeList allContent : allContents)
            {
                for (int initInd = 0; initInd < allContent.getLength(); initInd++)
                {
                    if (allContent.item(initInd).getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element xmlContent = (Element) allContent.item(initInd);
                        if (xmlContent != null)
                        {
                            NodeList contents = xmlContent.getChildNodes();
                            for (int ind = 0; ind < contents.getLength(); ind++)
                            {
                                Node content = contents.item(ind);
                                // Boolean b1 = content.getNodeType() == Node.ELEMENT_NODE;
                                // Boolean b2 = content.getNodeName() == CONTENT;
                                // System.out.println("Tests: " + b1 + "/" + b2 + "/" + contents.getLength());
                                if (content.getNodeType() == Node.ELEMENT_NODE && content.getNodeName() == CONTENT)
                                {
                                    try
                                    {
                                        int value = Integer.parseInt(content.getTextContent());
                                        int col = Integer.parseInt(((Element) content).getAttribute(COL));
                                        int row = Integer.parseInt(((Element) content).getAttribute(ROW));
                                        initCell(row, col, value, false, false, allContent == initialContents, false);
                                    }
                                    catch (InvalidValueException ex)
                                    {
                                        MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                                        errorBox.setMessage("Invalid value: " + content.getNodeValue() + " in row: "
                                                + ((Element) content).getAttribute(ROW) + ", column: "
                                                + ((Element) content).getAttribute(COL) + ". "
                                                + ex.getLocalizedMessage());
                                        errorBox.open();
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            inputFile = fromFile;
            setSaved(true);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved(), true);
            }
        }
        sudokuType = retVal;
        return (retVal);
    }

    // Returns a list of conflicts, if the list is empty there are no conflicts
    // as a side effect sets the "isAConflict" flag to conflicting values
    public List<List<int[]>> areContentsLegal()
    {
        List<List<int[]>> retVal = new ArrayList<List<int[]>>();
        // First reset from a possible previous run
        for (int row = 0; row < AppMain.OVERALLMAXROWS; row++)
        {
            for (int col = 0; col < AppMain.OVERALLMAXCOLS; col++)
            {
                getCell(row, col).setAConflict(false);
            }
        }
        for (SubSudoku subSudoku : (ArrayList<SubSudoku>) (getSudoku().getSubSudokus()))
        {
            for (int row = 0; row < AppMain.getSingleSudokuMaxRows(); row++)
            {
                for (int col = 0; col < AppMain.getSingleSudokuMaxCols(); col++)
                {
                    if (subSudoku.getRowCol(row, col).getCandidates().isEmpty())
                    {
                        // Same column
                        for (int rowInCol = row + 1; rowInCol < AppMain.getSingleSudokuMaxRows(); rowInCol++)
                        {
                            if ((subSudoku.getRowCol(rowInCol, col).getCandidates().isEmpty()
                                    && subSudoku.getRowCol(rowInCol, col).getSolution() == null)
                                    || (subSudoku.getRowCol(rowInCol, col).getSolution() != null
                                            && subSudoku.getRowCol(row, col).getSolution() != null
                                            && subSudoku.getRowCol(rowInCol, col).getSolution().val() == subSudoku
                                                    .getRowCol(row, col).getSolution().val()))
                            {
                                subSudoku.getRowCol(row, col).setAConflict(true);
                                subSudoku.getRowCol(rowInCol, col).setAConflict(true);
                                ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                                newConflict.add(new int[]
                                { subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(col) });
                                newConflict.add(new int[]
                                { subSudoku.getGlobalRow(rowInCol), subSudoku.getGlobalCol(col) });
                                retVal.add(newConflict);
                            }
                        }
                        // Same row
                        for (int colInRow = col + 1; colInRow < AppMain.getSingleSudokuMaxCols(); colInRow++)
                        {
                            if ((subSudoku.getRowCol(row, colInRow).getCandidates().isEmpty()
                                    && subSudoku.getRowCol(row, colInRow).getSolution() == null)
                                    || (subSudoku.getRowCol(row, colInRow).getSolution() != null
                                            && subSudoku.getRowCol(row, col).getSolution() != null
                                            && subSudoku.getRowCol(row, colInRow).getSolution().val() == subSudoku
                                                    .getRowCol(row, col).getSolution().val()))
                            {
                                subSudoku.getRowCol(row, col).setAConflict(true);
                                subSudoku.getRowCol(row, colInRow).setAConflict(true);
                                ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                                newConflict.add(new int[]
                                { subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(col) });
                                newConflict.add(new int[]
                                { subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(colInRow) });
                                retVal.add(newConflict);
                            }
                        }
                        // Same block
                        for (int rowInBlock = AppMain.getRectangleLength()
                                * (row / AppMain.getRectangleLength()); rowInBlock < AppMain.getRectangleLength()
                                        * (row / AppMain.getRectangleLength() + 1); rowInBlock++)
                        {
                            for (int colInBlock = AppMain.getRectangleLength()
                                    * (col / AppMain.getRectangleLength()); colInBlock < AppMain.getRectangleLength()
                                            * (col / AppMain.getRectangleLength() + 1); colInBlock++)
                            {
                                if ((rowInBlock >= row || colInBlock >= col)
                                        && (rowInBlock != row || colInBlock != col))
                                {
                                    if ((subSudoku.getRowCol(rowInBlock, colInBlock).getCandidates().isEmpty()
                                            && subSudoku.getRowCol(rowInBlock, colInBlock).getSolution() == null)
                                            || (subSudoku.getRowCol(rowInBlock, colInBlock).getSolution() != null
                                                    && subSudoku.getRowCol(row, col).getSolution() != null
                                                    && subSudoku.getRowCol(rowInBlock, colInBlock).getSolution()
                                                            .val() == subSudoku.getRowCol(row, col).getSolution()
                                                                    .val()))
                                    {
                                        subSudoku.getRowCol(row, col).setAConflict(true);
                                        subSudoku.getRowCol(rowInBlock, colInBlock).setAConflict(true);
                                        ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                                        newConflict.add(new int[]
                                        { subSudoku.getGlobalRow(row), subSudoku.getGlobalCol(col) });
                                        newConflict.add(new int[]
                                        { subSudoku.getGlobalRow(rowInBlock), subSudoku.getGlobalCol(colInBlock) });
                                        retVal.add(newConflict);
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

    int getNumberOfSolutions()
    {
        Stream<? super SingleCellValue> stream = Arrays.stream(this.getSudoku().getArray())
                .flatMap(x -> Arrays.stream(x));
        long retVal = stream.filter(x -> (x == null || ((SingleCellValue<?>) x).getSolution() != null)).count();
        return ((int) retVal);
    }

    private boolean validateXmlSchema(String xmlPath)
    {
        boolean retVal = false;
        try
        {
            URL classesRootDir = getClass().getProtectionDomain().getCodeSource().getLocation();
            // Check if exists and is well-formed
            final String xsdPath = (Paths.get(Paths.get(classesRootDir.toURI()).toString(), SCHEMAFILENAMEDBG)
                    .resolve(StringUtils.EMPTY)).toString();
            final File schemaFile = new File(xsdPath);
            InputStream schemaStr = null;
            // System.out.println("class: " + this.getClass().toString());
            // System.out.println("classesRootDir: " + classesRootDir.toString());
            // System.out.println("try resource: " + SCHEMAFILERELJAR);
            // schemaStr = this.getClass().getResourceAsStream(SCHEMAFILERELJAR);
            /*
             * File file = null; String resource = "/com/myorg/foo.xml"; URL res =
             * getClass().getResource(resource); if (res.getProtocol().equals("jar")) {
             */
            // final String root = "/";
            // // final List<String> resources = new LinkedList<>();
            // String resPath = root;
            // try (final Scanner scanner = new
            // Scanner(getClass().getResourceAsStream(resPath)))
            // {
            // while (scanner.hasNextLine())
            // {
            // final String line = scanner.nextLine();
            // // System.out.println(resPath + line);
            // if (line.equals("SudokuStepper.jar"))
            // {
            // resPath = resPath + line + "/";
            // System.out.println("One level down: " + resPath);
            // try (final Scanner scanner2 = new
            // Scanner(getClass().getResourceAsStream(resPath)))
            // {
            // while (scanner2.hasNextLine())
            // {
            // final String line2 = scanner2.nextLine();
            // // System.out.println(resPath + line2);
            // if (line2.contains("SudokuStepper.xsd"))
            // {
            // System.out.println(resPath + line2);
            // // resources.add(root + "/" + line); } }
            // }
            // }
            // }
            // }
            // }
            // resPath = root; // resources.add(root + "/" + line); } }
            // // return resources;
            // }
            if (!schemaFile.exists() || schemaFile.isDirectory() || !schemaFile.canRead())
            {
                // Path xsdPathJar = Paths.get(SCHEMAFILENAMEJAR);
                /*
                 * xsdPathJar = xsdPathJar.resolve(StringUtils.EMPTY); f = new
                 * File(xsdPathJar.toString());
                 */
                // if (schemaStr == null)
                // {
                // System.out.println("try resource: " + SCHEMAFILENAMEJAR);
                // ClassLoader classLoader = this.getClass().getClassLoader();
                // System.out.println("Class Loader: " + classLoader.toString());
                // schemaStr = classLoader
                // .getResourceAsStream(Paths.get(Paths.get(classesRootDir.toURI()).toString(),
                // SCHEMAFILENAMEJAR)
                // .toString()); /* SCHEMAFILENAMEJAR); */
                // if (schemaStr == null)
                // {
                // System.out.println("schemaStr/2: failed: " + SCHEMAFILENAMEJAR);
                // String p = Paths.get(Paths.get(classesRootDir.toURI()).toString(),
                // SCHEMAFILENAMEJAR).toString();
                // schemaStr = this.getClass().getResourceAsStream(p);
                // if (schemaStr == null)
                // {
                // System.out.println("schemaStr/3: failed: " + p);
                // p = SCHEMAFILENAMEJAR; // "SudokuStepper.xsd";
                // System.out.println("try resource: " + p);
                // schemaStr = this.getClass().getResourceAsStream(p);
                // if (schemaStr == null)
                // {
                // System.out.println("schemaStr/4: failed: " + p);
                // }
                // else
                // {
                // System.out.println("schemaStr/4: succeeded: " + p);
                // }
                // }
                // }
                // }
                schemaStr = this.getClass().getResourceAsStream(SCHEMAFILENAMEJAR);
                if (schemaStr == null)
                {
                    throw (new IOException("Cannot find or read Schema: " + xsdPath + " or " + SCHEMAFILENAMEJAR));
                }
            }
            else
            {
                schemaStr = new FileInputStream(schemaFile);
            }
            try
            {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(schemaStr));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new File(xmlPath)));
                retVal = true;
            }
            catch (IOException | SAXException ex)
            {
                System.out.println("Exception: " + ex.getMessage());
                MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                errorBox.setMessage("XML Validator could not load Sudoku. \n" + ex.getMessage() + "\n"
                        + ex.getLocalizedMessage() + "\n" + ex.toString());
                errorBox.open();
                retVal = false;
            }
        }
        catch (IOException | URISyntaxException ex)
        {
            System.out.println("Exception looking for Schema: " + ex.getMessage());
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("XML Validator could not load Schema. \n" + ex.getMessage() + "\n"
                    + ex.getLocalizedMessage() + "\n" + ex.toString());
            errorBox.open();
            retVal = false;

        }
        return retVal;
    }

    public void save(String outputFile, ListOfSolTraces<LegalValuesGen> trace)
    {
        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

            Document newDoc = domBuilder.newDocument();
            Element rootElement = newDoc.createElement(SUDOKU);
            rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns",
                    "http://www.example.org/SudokuStepper");
            rootElement.setAttribute(SUDOKUNAME, sudokuName);
            rootElement.setAttribute(SUDOKUTYPE, sudokuType.name().toLowerCase());
            int attrValInt = 9;
            if (legalValuesClass == LegalValues_25.class)
            {
                attrValInt = 25;
            }
            else if (legalValuesClass == LegalValues_16.class)
            {
                attrValInt = 16;
            }
            else if (legalValuesClass == LegalValues_4.class)
            {
                attrValInt = 4;
            }
            rootElement.setAttribute(HIGHESTVALUEINCELL, Integer.toString(attrValInt));
            newDoc.appendChild(rootElement);
            Element initialElt = newDoc.createElement(INITIAL);
            rootElement.appendChild(initialElt);
            Element solutionElt = newDoc.createElement(SOLUTION);

            Element[] elts =
            { initialElt, solutionElt };
            MasterSudoku<?> sudoku = getSudoku();
            for (Element elt : elts)
            {
                int numberNodes = 0;
                for (int row = 0; row < AppMain.getMaxRows(); row++)
                {
                    for (int col = 0; col < AppMain.getMaxCols(); col++)
                    {
                        if (sudoku.isRowColUsed(sudokuType, row, col))
                        {
                            if (sudoku.getRowCol(row, col).getSolution() != null
                                    && ((sudoku.getRowCol(row, col).isInput() && elt.equals(initialElt))
                                            || (!sudoku.getRowCol(row, col).isInput() && elt.equals(solutionElt))))
                            {
                                if (!sudoku.getRowCol(row, col).isInput() && elt.equals(solutionElt))
                                {
                                    numberNodes++;
                                }
                                Element content = newDoc.createElement(CONTENT);
                                content.setAttribute(ROW, Integer.toString(row + 1));
                                content.setAttribute(COL, Integer.toString(col + 1));
                                Text text = newDoc.createTextNode(
                                        Integer.toString(sudoku.getRowCol(row, col).getSolution().val()));
                                content.appendChild(text);
                                elt.appendChild(content);
                            }
                        }
                    }
                }
                Element progressElt = newDoc.createElement(PROGRESS);
                if (elt.equals(solutionElt) && numberNodes > 0)
                {
                    // also save the trace
                    Text text = newDoc.createTextNode("");
                    for (SolutionTrace<?> singleTrace : trace)
                    {
                        Element content = newDoc.createElement(CONTENT);
                        content.setAttribute(ROW, Integer.toString(singleTrace.getRow() + 1));
                        content.setAttribute(COL, Integer.toString(singleTrace.getCol() + 1));
                        String attrVal = "";
                        if (singleTrace.getChoices() != null)
                        {
                            int loopCount = 0;
                            for (LegalValuesGen val : (ArrayList<LegalValuesGen>) (singleTrace.getChoices()))
                            {
                                if (loopCount > 0)
                                {
                                    attrVal += " ";
                                }
                                attrVal += Integer.toString(val.val());
                                loopCount++;
                            }
                            content.setAttribute(CHOICES, attrVal);
                            text = newDoc.createTextNode("");
                        }
                        else
                        {
                            text = newDoc.createTextNode(Integer.toString(singleTrace.getValue().val()));
                        }
                        content.appendChild(text);
                        progressElt.appendChild(content);
                    }
                    if (trace.size() > 0)
                    {
                        rootElement.appendChild(solutionElt);
                        rootElement.appendChild(progressElt);
                    }
                }
            }
            toString(newDoc, outputFile);
            setSaved(true);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved(), true);
            }
            /*
             * new XmlAdapter(new FileOutputStream(fileName), new OutputFormat() { {
             * setEncoding("UTF-8"); setIndent("    "); setTrimText(false);
             * setNewlines(true); setPadText(true); } }).write(document);
             */
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void toString(Document newDoc, String outputFile) throws Exception
    {
        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        aTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        aTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        Source src = new DOMSource(newDoc);
        if (outputFile != null)
        {
            this.inputFile = outputFile;
        }

        Result dest = new StreamResult(new File(this.inputFile));
        aTransformer.transform(src, dest);
    }

    /**
     * @return the solutionTrace
     */
    public ListOfSolTraces<LegalValuesGen> getSolutionTrace()
    {
        return solutionTrace;
    }
}

/*
 * public class ListOfSolTraces implements Iterable<SolutionTrace> { private
 * List<SolutionTrace> listTraces = new Vector<SolutionTrace>();
 * 
 * public void addToSolutionTrace(Values values, int globalRow, int globalCol,
 * LegalValuesGen eliminatedVal, List<LegalValuesGen> candidates) {
 * listTraces.add(values.new SolutionTrace(globalRow, globalCol, eliminatedVal,
 * candidates)); }
 * 
 * public void clear() { listTraces.clear(); }
 * 
 * public int size() { return (listTraces.size()); }
 * 
 * public Iterator<SolutionTrace> iterator() { return new
 * TraceIterator<SolutionTrace>(); } }
 * 
 * public class TraceIterator<SolutionTrace> implements Iterator<SolutionTrace>
 * { SolutionTrace current; int currentInd = -1; List<SolutionTrace>
 * intListTraces; // constructor public
 * TraceIterator<SolutionTrace>(ListOfSolTraces traceList) { intListTraces =
 * traceList.listTraces; if ( intListTraces.size() > 0) { currentInd = 0; } }
 * 
 * // Checks if the next element exists public boolean hasNext() { return
 * currentInd < traceList.size() - 1; }
 * 
 * // moves the cursor/iterator to next element public SolutionTrace next() {
 * SolutionTrace trace = intListTraces.get(currentInd); currentInd++;
 * return(trace); } }
 */
/*
 * private class SolutionTrace { private int row; private int col; private
 * LegalValuesGen val; private List<LegalValuesGen> choices;
 * 
 * public SolutionTrace(int rowIn, int colIn, LegalValuesGen valIn,
 * List<LegalValuesGen> choicesIn) { row = rowIn; col = colIn; val = valIn;
 * choices = choicesIn; }
 * 
 * public int getRow() { return (row); }
 * 
 * public int getCol() { return (col); }
 * 
 * public LegalValuesGen getValue() { return (val); }
 * 
 * public List<LegalValuesGen> getChoices() { return (choices); }
 * 
 * }
 */

class Bifurcation<LegalValuesGen extends LegalValuesGenClass>
{
    private int                  rowInt;
    private int                  colInt;
    // The last choice in the list shows how to come from the (n-1)-th sudoku (to
    // which this object is attached) to the n-th sudoku in the stack
    // The list gets longer the more different solution you try
    private List<LegalValuesGen> choices = new ArrayList<LegalValuesGen>();

    public Bifurcation(int row, int col, LegalValuesGen val)
    {
        rowInt = row;
        colInt = col;
        choices.add(val);
    }
    // public static Bifurcation newInstance(int row, int col, LegalValuesGen val)
    // {
    // Bifurcation<LegalValuesGen> newObj = new Bifurcation<LegalValuesGen>(int row,
    // int col, LegalValuesGen val);
    // return (newObj);
    // }

    public LegalValuesGen getNextTry()
    {
        return (this.choices.get(choices.size() - 1));
    }

    public Bifurcation<LegalValuesGen> addNewTry(LegalValuesGenClass toBeEliminatedVal)
    {
        Bifurcation<LegalValuesGen> retVal = (Bifurcation<LegalValuesGen>) this;
        retVal.choices.add((LegalValuesGen) toBeEliminatedVal);
        return retVal;
    }

    public int getNumPreviousTries()
    {
        return choices.size();
    }

    public int getCol()
    {
        return colInt;
    }

    public int getRow()
    {
        return rowInt;
    }
}

package SudokuStepper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * 
 */
interface CandidatesListener
{
    void candidatesUpdated(int row, int col, LegalValues val, boolean runsInUiThread);
}

interface CandidatesResetListener
{
    void candidatesReset();
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
 * @author Pascal
 *
 */
public class Values
{

    public static final int               DIMENSION                = AppMain.RECTLENGTH * AppMain.RECTLENGTH;

    private Stack<Tentative>              sudokuCands              = new Stack<Tentative>();
    private String                        sudokuName               = null;
    private String                        inputFile                = null;
    private boolean                       saved                    = true;

    private List<SolutionListener>        solutionListeners        = new ArrayList<SolutionListener>();
    private List<CandidatesListener>      candidatesListeners      = new ArrayList<CandidatesListener>();
    private List<CandidatesResetListener> candidatesResetListeners = new ArrayList<CandidatesResetListener>();
    private List<RollbackListener>        rollbackListeners        = new ArrayList<RollbackListener>();
    private List<SavedListener>           savedListeners           = new ArrayList<SavedListener>();

    // private List<NewStartListener> newStartListeners = new
    // ArrayList<NewStartListener>();
    public boolean isSaved()
    {
        return saved;
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

    public SolutionProgress addBifurcationNClone(int row, int col)
    {
        // // For debugging
        // try
        // {
        // Values pb = new Values();
        // Tentative oldSudoku = pb.sudokuCands.peek();
        // oldSudoku.setBifurcation(0, 0);
        // oldSudoku.getSudoku()[1][1].setSolution(LegalValues.NINE, 1, 1, new
        // ArrayList<SolutionListener>(), true,
        // false);
        // Tentative newSudoku = new Tentative(oldSudoku);
        // pb.sudokuCands.push(newSudoku);
        // oldSudoku.getSudoku()[1][1].setSolution(LegalValues.ONE, 1, 1, new
        // ArrayList<SolutionListener>(), true,
        // false);
        // oldSudoku.getSudoku()[0][0].candidates.remove(8);
        // pb.sudokuCands.peek().getSudoku()[1][1].setSolution(LegalValues.TWO, 1, 1,
        // new ArrayList<SolutionListener>(), true, false);
        // pb.sudokuCands.peek().getSudoku()[0][1].candidates.removeIf(x -> x ==
        // LegalValues.FIVE);
        // // System.out.println(System.identityHashCode(array[i]));
        // }
        // catch (Exception e)
        // {
        // // TODO Auto-generated catch block
        // System.out.println(e.getMessage());
        // e.printStackTrace();
        // }
        // // end of debugging section
        SolutionProgress retVal = SolutionProgress.NONE;
        Tentative oldSudoku = sudokuCands.peek();
        Tentative newSudoku = new Tentative(oldSudoku);
        LegalValues toBeEliminatedVal = oldSudoku.setBifurcation(row, col);
        System.out.println(
                "Try and Error with row: " + row + ", col: " + col + ", eliminating value: " + toBeEliminatedVal);
        sudokuCands.push(newSudoku);
        retVal = eliminateCandidate(row, col, toBeEliminatedVal, true, false, true);
        return (retVal);
    }

    public boolean isRollbackPossible()
    {
        return (sudokuCands.size() > 1);
    }

    public SolutionProgress bifurqueOnceMore()
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        if (sudokuCands.size() > 1)
        {
            sudokuCands.pop();
            Tentative oldSudoku = sudokuCands.peek();
            Bifurcation nextTry = oldSudoku.getNextTry();
            if (nextTry != null)
            {
                Tentative newSudoku = new Tentative(oldSudoku);
                sudokuCands.push(newSudoku);
                for (RollbackListener listener : rollbackListeners)
                {
                    listener.rollbackSudoku();
                }
                int row = nextTry.getRow();
                int col = nextTry.getCol();
                LegalValues toBeEliminatedVal = nextTry.getNextTry();
                System.out.println("Try and Error rollback with row: " + row + ", col: " + col
                        + ", now eliminating value: " + toBeEliminatedVal);
                retVal = eliminateCandidate(row, col, toBeEliminatedVal, true, false, true);
            }
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

    public SingleCellValue[][] getSudoku()
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

    public SingleCellValue getCell(int row, int col)
    {
        return (getSudoku()[row][col]);
    }

    public void resetCell(int row, int col)
    {
        getSudoku()[row][col] = new SingleCellValue();
    }

    public Values()
    {
        sudokuCands.push(new Tentative());
        reset();
    }

    public void initCell(int row, int col, int value, boolean runsInUiThread, boolean markLastSolutionFound,
            boolean isAnInput) throws InvalidValueException
    {
        try
        {
            row -= 1;
            col -= 1;
            LegalValues val = LegalValues.from(value);
            SingleCellValue[][] sudoku = getSudoku();
            sudoku[row][col].setSolution(val, row, col, null, runsInUiThread, markLastSolutionFound);
            sudoku[row][col].isInput = isAnInput;
            sudoku[row][col].candidates.clear();
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

    public SolutionProgress eliminateCandidate(int row, int col, LegalValues val, boolean alsoSetSolution,
            boolean runsInUiThread, boolean markLastSolutionFound)
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        SingleCellValue[][] sudoku = getSudoku();
        if (val == null || sudoku[row][col].candidates.contains(val))
        {
            retVal = retVal.combineWith(SolutionProgress.CANDIDATES);
            if (val != null)
            {
                sudoku[row][col].candidates.remove(val);
                for (CandidatesListener listener : candidatesListeners)
                {
                    listener.candidatesUpdated(row, col, val, runsInUiThread);
                }
            }
            setSaved(false);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved(), runsInUiThread);
            }
            if (sudoku[row][col].candidates.size() == 1)
            {
                if (alsoSetSolution)
                {
                    LegalValues solution = sudoku[row][col].candidates.get(0);
                    retVal = retVal.combineWith(SolutionProgress.SOLUTION);
                    sudoku[row][col].isInput = false;
                    sudoku[row][col].candidates.clear();
                    // Must be the last thing because the thread could end if step by step mode
                    sudoku[row][col].setSolution(solution, row, col, solutionListeners, runsInUiThread,
                            markLastSolutionFound);
                }
                SolutionProgress newUpdated = reduceInfluencedCellCandidates(row, col, sudoku[row][col].getSolution(),
                        alsoSetSolution, runsInUiThread, markLastSolutionFound);
                retVal = retVal.combineWith(newUpdated);
            }
        }
        return (retVal);
    }

    void resetCandidates()
    {
        for (int row = 0; row < DIMENSION; row++)
        {
            for (int col = 0; col < DIMENSION; col++)
            {
                SingleCellValue cell = getCell(row, col);
                if (cell.getSolution() == null)
                {
                    cell.initCandidates();
                }
            }
        }
        for (CandidatesResetListener listener : candidatesResetListeners)
        {
            listener.candidatesReset();
        }
    }

    // remove from the value just set in the given cell the list of candidates from
    // all influenced cells
    public void updateCandidateList(int row, int col, LegalValues val, boolean runsInUiThread,
            boolean markLastSolutionFound)
    {
        SingleCellValue[][] sudoku = getSudoku();
        if (sudoku[row][col].getSolution() != null)
        { // First undo the value restrictions due to the previous value, but only where
          // not another cell continues justifying them
            LegalValues oldVal = sudoku[row][col].getSolution();
            // Same column
            for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
            {
                if (!sudoku[rowInCol][col].candidates.contains(oldVal) && isValueACandidate(rowInCol, col, oldVal))
                {
                    sudoku[rowInCol][col].candidates.add(oldVal);
                }
            }
            // Same row
            for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
            {
                if (!sudoku[row][colInRow].candidates.contains(oldVal) && isValueACandidate(row, colInRow, oldVal))
                {
                    sudoku[row][colInRow].candidates.add(oldVal);
                }
            }
            // Same block
            for (int rowInBlock = AppMain.RECTLENGTH * (row / AppMain.RECTLENGTH); rowInBlock < AppMain.RECTLENGTH
                    * (row / AppMain.RECTLENGTH + 1); rowInBlock++)
            {
                for (int colInBlock = AppMain.RECTLENGTH * (col / AppMain.RECTLENGTH); colInBlock < AppMain.RECTLENGTH
                        * (col / AppMain.RECTLENGTH + 1); colInBlock++)
                {
                    if (!sudoku[rowInBlock][colInBlock].candidates.contains(oldVal)
                            && isValueACandidate(rowInBlock, colInBlock, oldVal))
                    {
                        sudoku[rowInBlock][colInBlock].candidates.add(oldVal);
                    }
                }
            }
        }
        if (val != null)
        {
            reduceInfluencedCellCandidates(row, col, val, false, runsInUiThread, markLastSolutionFound);
        }
    }

    // Check if the value is a possible candidate based on the values already set in
    // all influencing cells, val should not be null
    private boolean isValueACandidate(int row, int col, LegalValues val)
    {
        boolean retVal = true;
        SingleCellValue[][] sudoku = getSudoku();
        // Same column
        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
        {
            if (row != rowInCol && val.equals(sudoku[rowInCol][col].getSolution()))
            {
                retVal = false;
                break;
            }
        }
        // Same row
        if (retVal)
        {
            for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
            {
                if (col != colInRow && val.equals(sudoku[row][colInRow].getSolution()))
                {
                    retVal = false;
                    break;
                }
            }
        }
        // Same block
        if (retVal)
        {
            for (int rowInBlock = AppMain.RECTLENGTH * (row / AppMain.RECTLENGTH); rowInBlock < AppMain.RECTLENGTH
                    * (row / AppMain.RECTLENGTH + 1); rowInBlock++)
            {
                for (int colInBlock = AppMain.RECTLENGTH * (col / AppMain.RECTLENGTH); colInBlock < AppMain.RECTLENGTH
                        * (col / AppMain.RECTLENGTH + 1); colInBlock++)
                {
                    if ((col != colInBlock || row != rowInBlock)
                            && val.equals(sudoku[rowInBlock][colInBlock].getSolution()))
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
    SolutionProgress reduceInfluencedCellCandidates(int row, int col, LegalValues val, boolean alsoSetSolution,
            boolean runsInUiThread, boolean markLastSolutionFound)
    {
        SolutionProgress retVal = SolutionProgress.NONE;
        SingleCellValue[][] sudoku = getSudoku();
        // Same column
        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
        {
            if (sudoku[rowInCol][col].candidates.contains(val))
            {
                SolutionProgress nowUpdated = eliminateCandidate(rowInCol, col, val, alsoSetSolution, runsInUiThread,
                        markLastSolutionFound);
                retVal = retVal.combineWith(nowUpdated);
                // sudoku[rowInCol][col].candidates.remove(val);
                // for (CandidatesListener listener : candidatesListeners)
                // {
                // listener.candidatesUpdated(rowInCol, col, val);
                // }
            }
        }
        // Same row
        for (int colInRow = 0; colInRow < Values.DIMENSION; colInRow++)
        {
            if (sudoku[row][colInRow].candidates.contains(val))
            {
                SolutionProgress nowUpdated = eliminateCandidate(row, colInRow, val, alsoSetSolution, runsInUiThread,
                        markLastSolutionFound);
                retVal = retVal.combineWith(nowUpdated);
                // sudoku[row][colInRow].candidates.remove(val);
                // for (CandidatesListener listener : candidatesListeners)
                // {
                // listener.candidatesUpdated(row, colInRow, val);
                // }
            }
        }
        // Same block
        for (int rowInBlock = AppMain.RECTLENGTH * (row / AppMain.RECTLENGTH); rowInBlock < AppMain.RECTLENGTH
                * (row / AppMain.RECTLENGTH + 1); rowInBlock++)
        {
            for (int colInBlock = AppMain.RECTLENGTH * (col / AppMain.RECTLENGTH); colInBlock < AppMain.RECTLENGTH
                    * (col / AppMain.RECTLENGTH + 1); colInBlock++)
            {
                if (sudoku[rowInBlock][colInBlock].candidates.contains(val))
                {
                    SolutionProgress nowUpdated = eliminateCandidate(rowInBlock, colInBlock, val, alsoSetSolution,
                            runsInUiThread, markLastSolutionFound);
                    retVal = retVal.combineWith(nowUpdated);
                    // sudoku[rowInBlock][colInBlock].candidates.remove(val);
                    // for (CandidatesListener listener : candidatesListeners)
                    // {
                    // listener.candidatesUpdated(rowInBlock, colInBlock, val);
                    // }
                }
            }
        }
        return (retVal);
    }

    public void reset()
    {
        SingleCellValue[][] sudoku = getSudoku();
        for (int row = 0; row < DIMENSION; row++)
        {
            for (int col = 0; col < DIMENSION; col++)
            {
                sudoku[row][col] = new SingleCellValue();
            }
        }
    }

    private static String SCHEMAFILENAME = "SudokuStepper\\SudokuStepper.xsd";
    private static String SUDOKU         = "sudoku";
    private static String INITIAL        = "initial";
    private static String SOLUTION       = "solution";
    private static String CONTENT        = "content";
    private static String ROW            = "row";
    private static String COL            = "col";
    private static String SUDOKUNAME     = "name";

    public void read(String fromFile, boolean alsoReadSolution)
            throws InvalidValueException, ParserConfigurationException, SAXException, IOException
    {
        reset();

        if (validateXmlSchema(fromFile))
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.setErrorHandler(new XmlErrorHandler());
            Document doc = dBuilder.parse(fromFile);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            sudokuName = doc.getDocumentElement().getAttribute(SUDOKUNAME);
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
                                Boolean b1 = content.getNodeType() == Node.ELEMENT_NODE;
                                Boolean b2 = content.getNodeName() == CONTENT;
                                // System.out.println("Tests: " + b1 + "/" + b2 + "/" + contents.getLength());
                                if (content.getNodeType() == Node.ELEMENT_NODE && content.getNodeName() == CONTENT)
                                {
                                    try
                                    {
                                        int value = Integer.parseInt(content.getTextContent());
                                        int col = Integer.parseInt(((Element) content).getAttribute(COL));
                                        int row = Integer.parseInt(((Element) content).getAttribute(ROW));
                                        initCell(row, col, value, false, false, allContent == initialContents);
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
    }

    // Returns a list of conflicts, if the list is empty there are no conflicts
    // as a side effect sets the "isAConflict" flag to conflicting values
    public List<List<int[]>> areContentsLegal()
    {
        List<List<int[]>> retVal = new ArrayList<List<int[]>>();
        // First reset from a possible previous run
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                getCell(row, col).isAConflict = false;
            }
        }
        for (int row = 0; row < Values.DIMENSION; row++)
        {
            for (int col = 0; col < Values.DIMENSION; col++)
            {
                if (getCell(row, col).candidates.isEmpty())
                {
                    // Same column
                    for (int rowInCol = row + 1; rowInCol < Values.DIMENSION; rowInCol++)
                    {
                        if (getCell(rowInCol, col).candidates.isEmpty()
                                && getCell(rowInCol, col).getSolution() == getCell(row, col).getSolution())
                        {
                            getCell(row, col).isAConflict = true;
                            getCell(rowInCol, col).isAConflict = true;
                            ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                            newConflict.add(new int[]
                            { row, col });
                            newConflict.add(new int[]
                            { rowInCol, col });
                            retVal.add(newConflict);
                        }
                    }
                    // Same row
                    for (int colInRow = col + 1; colInRow < Values.DIMENSION; colInRow++)
                    {
                        if (getCell(row, colInRow).candidates.isEmpty()
                                && getCell(row, colInRow).getSolution() == getCell(row, col).getSolution())
                        {
                            getCell(row, col).isAConflict = true;
                            getCell(row, colInRow).isAConflict = true;
                            ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                            newConflict.add(new int[]
                            { row, col });
                            newConflict.add(new int[]
                            { row, colInRow });
                            retVal.add(newConflict);
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
                            if ((rowInBlock >= row || colInBlock >= col) && (rowInBlock != row || colInBlock != col))
                            {
                                if (getCell(rowInBlock, colInBlock).candidates.isEmpty()
                                        && getCell(rowInBlock, colInBlock).getSolution() == getCell(row, col)
                                                .getSolution())
                                {
                                    getCell(row, col).isAConflict = true;
                                    getCell(rowInBlock, colInBlock).isAConflict = true;
                                    ArrayList<int[]> newConflict = new ArrayList<int[]>(2);
                                    newConflict.add(new int[]
                                    { row, col });
                                    newConflict.add(new int[]
                                    { rowInBlock, colInBlock });
                                    retVal.add(newConflict);
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
        Stream<? super SingleCellValue> stream = Arrays.stream(this.getSudoku()).flatMap(x -> Arrays.stream(x));
        long retVal = stream.filter(x -> ((SingleCellValue) x).getSolution() != null).count();
        return ((int) retVal);
    }

    private boolean validateXmlSchema(String xmlPath)
    {
        boolean retVal = false;
        try
        {
            URL classesRootDir = getClass().getProtectionDomain().getCodeSource().getLocation();
            // Check if exists and is well-formed
            Path xsdPath = Paths.get(Paths.get(classesRootDir.toURI()).toString(), SCHEMAFILENAME);
            xsdPath = xsdPath.resolve(StringUtils.EMPTY);
            File f = new File(xsdPath.toString());
            if (!f.exists() || f.isDirectory() || !f.canRead())
            {
                throw (new IOException("Cannot find or read Schema: " + xsdPath.toString()));
            }
            try
            {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new File(xsdPath.toString()));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new File(xmlPath)));
                retVal = true;
            }
            catch (IOException | SAXException ex)
            {
                System.out.println("Exception: " + ex.getMessage());
                MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                errorBox.setMessage("Could not load Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                        + "\n" + ex.toString());
                errorBox.open();
                retVal = false;
            }
        }
        catch (IOException | URISyntaxException ex)
        {
            System.out.println("Exception looking for Schema: " + ex.getMessage());
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not load Schema. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n"
                    + ex.toString());
            errorBox.open();
            retVal = false;

        }
        return retVal;
    }

    public void save(String outputFile)
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
            newDoc.appendChild(rootElement);
            Element initialElt = newDoc.createElement(INITIAL);
            rootElement.appendChild(initialElt);
            Element solutionElt = newDoc.createElement(SOLUTION);
            rootElement.appendChild(solutionElt);
            Element[] elts =
            { initialElt, solutionElt };
            SingleCellValue[][] sudoku = getSudoku();
            for (Element elt : elts)
            {
                for (int row = 0; row < DIMENSION; row++)
                {
                    for (int col = 0; col < DIMENSION; col++)
                    {
                        if (sudoku[row][col].getSolution() != null
                                && ((sudoku[row][col].isInput && elt.equals(initialElt))
                                        || (!sudoku[row][col].isInput && elt.equals(solutionElt))))
                        {
                            Element content = newDoc.createElement(CONTENT);
                            content.setAttribute(ROW, Integer.toString(row + 1));
                            content.setAttribute(COL, Integer.toString(col + 1));
                            Text text = newDoc.createTextNode(Integer.toString(sudoku[row][col].getSolution().val()));
                            content.appendChild(text);
                            elt.appendChild(content);
                        }
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
            // TODO Auto-generated catch block
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

}

class Bifurcation
{
    private int               rowInt;
    private int               colInt;
    // The last choice in the list shows how to come from the (n-1)-th sudoku (to
    // which this object is attached) to the n-th sudoku in the stack
    private List<LegalValues> choices = new ArrayList<LegalValues>();

    public Bifurcation(int row, int col, LegalValues val)
    {
        rowInt = row;
        colInt = col;
        choices.add(val);
    }

    public LegalValues getNextTry()
    {
        return (this.choices.get(choices.size() - 1));
    }

    public Bifurcation addNewTry(LegalValues toBeEliminatedVal)
    {
        Bifurcation retVal = this;
        retVal.choices.add(toBeEliminatedVal);
        return retVal;
    }

    public int getNumPreviousTries()
    {
        return choices.size();
    }

    public int getCol()
    {
        // TODO Auto-generated method stub
        return colInt;
    }

    public int getRow()
    {
        // TODO Auto-generated method stub
        return rowInt;
    }
}

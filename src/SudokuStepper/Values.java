package SudokuStepper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    void candidatesUpdated(int row, int col, LegalValues val);
}

interface SolutionListener
{
    void solutionUpdated(int row, int col);
}

interface SavedListener
{
    void savedUpdated(boolean saved);
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
    class SingleCellValue
    {
        public LegalValues       solution    = null;
        public boolean           isInput     = false;
        public boolean           isAConflict = false;
        public List<LegalValues> candidates  = new ArrayList<LegalValues>(DIMENSION);

        public SingleCellValue()
        {
            for (LegalValues val : LegalValues.values())
            {
                candidates.add(val);
            }
        }
    }

    public static final int          DIMENSION           = AppMain.RECTLENGTH * AppMain.RECTLENGTH;

    private SingleCellValue[][]      sudoku              = new SingleCellValue[DIMENSION][DIMENSION];
    private String                   sudokuName          = null;
    private String                   inputFile           = null;
    private boolean                  saved               = true;

    private List<SolutionListener>   solutionListeners   = new ArrayList<SolutionListener>();
    private List<CandidatesListener> candidatesListeners = new ArrayList<CandidatesListener>();
    private List<SavedListener>      savedListeners      = new ArrayList<SavedListener>();

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
        return (sudoku[row][col]);
    }

    public void resetCell(int row, int col)
    {
        sudoku[row][col] = new SingleCellValue();
    }

    public Values()
    {
        reset();
    }

    public void initCell(int row, int col, int value) throws InvalidValueException
    {
        try
        {
            row -= 1;
            col -= 1;
            LegalValues val = LegalValues.from(value);
            sudoku[row][col].solution = val;
            sudoku[row][col].isInput = true;
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

    public boolean eliminateCandidate(int row, int col, LegalValues val, boolean alsoSetSolution)
    {
        boolean retVal = false;
        if (sudoku[row][col].candidates.contains(val))
        {
            sudoku[row][col].candidates.remove(val);
            for (CandidatesListener listener : candidatesListeners)
            {
                listener.candidatesUpdated(row, col, val);
            }
            if (sudoku[row][col].candidates.size() == 1)
            {
                if (alsoSetSolution)
                {
                    sudoku[row][col].solution = sudoku[row][col].candidates.get(0);
                    sudoku[row][col].isInput = false;
                    sudoku[row][col].candidates.clear();
                    for (SolutionListener listener : solutionListeners)
                    {
                        listener.solutionUpdated(row, col);
                    }
                }
                reduceInfluencedCellCandidates(row, col, sudoku[row][col].solution, alsoSetSolution);
            }
            setSaved(false);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved());
            }
            retVal = true;
        }
        return (retVal);
    }

    // remove from the value just set in the given cell the list of candidates from
    // all influenced cells
    public void updateCandidateList(int row, int col, LegalValues val)
    {
        if (sudoku[row][col].solution != null)
        { // First undo the value restrictions due to the previous value, but only where
          // not another cell continues justifying them
            LegalValues oldVal = sudoku[row][col].solution;
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
            reduceInfluencedCellCandidates(row, col, val, false);
        }
    }

    // Check if the value is a possible candidate based on the values already set in
    // all influencing cells, val should not be null
    private boolean isValueACandidate(int row, int col, LegalValues val)
    {
        boolean retVal = true;
        // Same column
        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
        {
            if (row != rowInCol && val.equals(sudoku[rowInCol][col].solution))
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
                if (col != colInRow && val.equals(sudoku[row][colInRow].solution))
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
                    if ((col != colInBlock || row != rowInBlock) && val.equals(sudoku[rowInBlock][colInBlock].solution))
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
    void reduceInfluencedCellCandidates(int row, int col, LegalValues val, boolean alsoSetSolution)
    {
        // Same column
        for (int rowInCol = 0; rowInCol < Values.DIMENSION; rowInCol++)
        {
            if (sudoku[rowInCol][col].candidates.contains(val))
            {
                eliminateCandidate(rowInCol, col, val, alsoSetSolution);
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
                eliminateCandidate(row, colInRow, val, alsoSetSolution);
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
                    eliminateCandidate(rowInBlock, colInBlock, val, alsoSetSolution);
                    // sudoku[rowInBlock][colInBlock].candidates.remove(val);
                    // for (CandidatesListener listener : candidatesListeners)
                    // {
                    // listener.candidatesUpdated(rowInBlock, colInBlock, val);
                    // }
                }
            }
        }
    }

    public void reset()
    {
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

    public void read(String fromFile)
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
            for (int initInd = 0; initInd < initialContents.getLength(); initInd++)
            {
                if (initialContents.item(initInd).getNodeType() == Node.ELEMENT_NODE)
                {
                    Element initialContent = (Element) initialContents.item(initInd);
                    if (initialContent != null)
                    {
                        NodeList contents = initialContent.getChildNodes();
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
                                    initCell(row, col, value);
                                }
                                catch (InvalidValueException ex)
                                {
                                    MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                                    errorBox.setMessage("Invalid value: " + content.getNodeValue() + " in row: "
                                            + ((Element) content).getAttribute(ROW) + ", column: "
                                            + ((Element) content).getAttribute(COL) + ". " + ex.getLocalizedMessage());
                                    errorBox.open();
                                }
                            }
                        }
                        break;
                    }
                }
            }
            inputFile = fromFile;
            setSaved(true);
            for (SavedListener listener : savedListeners)
            {
                listener.savedUpdated(isSaved());
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
                                && getCell(rowInCol, col).solution == getCell(row, col).solution)
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
                                && getCell(row, colInRow).solution == getCell(row, col).solution)
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
                                        && getCell(rowInBlock, colInBlock).solution == getCell(row, col).solution)
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
        Stream<? super SingleCellValue> stream = Arrays.stream(this.sudoku).flatMap(x -> Arrays.stream(x));
        long retVal = stream.filter(x -> ((SingleCellValue) x).solution != null).count();
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
            System.out.println("Exception lookking for Schema: " + ex.getMessage());
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
            for (Element elt : elts)
            {
                for (int row = 0; row < DIMENSION; row++)
                {
                    for (int col = 0; col < DIMENSION; col++)
                    {
                        if (sudoku[row][col].solution != null && ((sudoku[row][col].isInput && elt.equals(initialElt))
                                || (!sudoku[row][col].isInput && elt.equals(solutionElt))))
                        {
                            Element content = newDoc.createElement(CONTENT);
                            content.setAttribute(ROW, Integer.toString(row + 1));
                            content.setAttribute(COL, Integer.toString(col + 1));
                            Text text = newDoc.createTextNode(Integer.toString(sudoku[row][col].solution.val()));
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
                listener.savedUpdated(isSaved());
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

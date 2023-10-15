package SudokuStepper;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import SudokuStepper.Values.SudokuType;
import SudokuStepper.ListOfSolTraces;

public class AppMain extends ApplicationWindow
        implements SolutionListener, CandidatesListener, CandidatesResetListener, SavedListener, RollbackListener
{
    private Action                      action;
    private Values<LegalValuesGenClass> mySudoku                  = null;
    // remember the last candidate whose status was changed
    private Text                        lastUpdatedCandText       = null;
    private Font                        solutionFont              = null; // SWTResourceManager.getFont("Segoe
                                                                          // UI",
                                                                          // 30,
                                                                          // SWT.BOLD);
    // null means: image by image, 0 means no pause
    private Integer                     slideShowPause            = null;
    private boolean                     stopSlideShow             = false;
    private Thread                      solvingThread             = null;
    private Composite                   appParent                 = null;
    private Composite[][]               cellCompositesPtr         = null;
    private Group                       grpSudokuScrolledContents = null;
    // For performance useTripleRecognition should be true. False is to be used for
    // debugging
    static final boolean                useTripleRecognition      = true;

    void setSolvingThread(Thread solvingTh)
    {
        solvingThread = solvingTh;
    }

    // private Integer previousSlideShowPause = null;
    private Font solutionSmallFont = null; // SWTResourceManager.getFont("Segoe
                                           // UI",
                                           // 8,
                                           // SWT.NORMAL);
    // private static final int OVERALLRECTANGLELENGTH = 4;

    // static final int CANDIDATESNUMBER = 9;
    public static int getCandidatesNumber()
    {
        return (getIntByReflection("getCandidatesNumber"));
    }

    // static final int CANDIDATESPERROW = 3;
    public static int getCandidatesPerRow()
    {
        return (getIntByReflection("getCandidatesPerRow"));
    }

    static final int OVERALLMAXCOLS = 25; // 21; // No samurai with 4x4 sudokus because
    // of memory limitations
    static final int OVERALLMAXROWS = 25; // 21;

    // 9 for single sudoku, 21 for sudoku
    // samurai when 9 values are possible
    // 16 for single sudoku, 40 for sudoku
    // samurai when 16 values are possible
    public static int getMaxRows()
    {
        return (getIntByReflection("getMaxRows"));
    }

    public static int getMaxCols()
    {
        return (getIntByReflection("getMaxCols"));
    }

    // static final int OVERALLSINGLESUDOKUMAXROWS = 16;
    // static final int OVERALLSINGLESUDOKUMAXCOLS = 16;
    // static final int OVERALLCELLSPERROW = 4;
    // static final int OVERALLCELLSPERCOL = 4;

    public static int getSingleSudokuMaxRows()
    {
        return (getIntByReflection("getSingleSudokuMaxRows"));

        // int retVal = 0; // Dummy initialization
        // switch (singleSudokuWidth)
        // {
        // case FOUR:
        // retVal = 16;
        // break;
        // case THREE:
        // default:
        // retVal = 9;
        // break;
        // }
        // return (retVal);
    }

    public static int getSingleSudokuMaxCols()
    {
        return (getIntByReflection("getSingleSudokuMaxCols"));
    }

    public static int getCellsPerRow()
    {
        return (getIntByReflection("getCellsPerRow"));
        // int retVal = 0; // Dummy initialization
        // switch (singleSudokuWidth)
        // {
        // case FOUR:
        // retVal = 4;
        // break;
        // case THREE:
        // default:
        // retVal = 3;
        // break;
        // }
        // return (retVal);
    }

    public static int getCellsPerCol()
    {
        return (getIntByReflection("getCellsPerCol"));
    }

    private static final int    INITIAL_WIDTH         = 1250;                 // 552;
    private static final int    INITIAL_HEIGHT        = 4200;                 // 2100; // 915;
    private static final int    NAME_BOX_HEIGHT       = 55;
    private static final int    TOP_MARGIN            = 5;
    private static final int    COLOR_INPUT_BCKGRD    = SWT.COLOR_WHITE;
    private static final int    COLOR_INPUT_FOREGRD   = SWT.COLOR_BLACK;
    private static final int    COLOR_CONFLICT_BCKGRD = SWT.COLOR_RED;
    private static final int    COLOR_SOLT_BCKGRD     = SWT.COLOR_DARK_YELLOW;
    private static final int    COLOR_SOLT_FOREGRD    = SWT.COLOR_BLACK;
    private static final int    COLOR_LAST_FOREGRD    = SWT.COLOR_MAGENTA;
    private static final int    COLOR_TNERR_FOREGRD   = SWT.COLOR_BLUE;
    private static final int    COLOR_PREV_FOREGRD    = SWT.COLOR_WHITE;

    private static final String secondUnitStr         = " s";

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String args[])
    {
        try
        {
            AppMain window = new AppMain();
            window.setBlockOnOpen(true);
            window.open();
            window.dispose();
            // never called: Display.getCurrent().dispose(); // display.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // first key is the row (1...9/16), second key is the column(1...9/16)
    private Map<Integer, Map<Integer, SolNCandTexts>> uiFields = null;

    /**
     * Create the application window.
     */
    public AppMain()
    {
        super(null);
        // addToolBar(SWT.NONE);
        // renameSudokuAction = new RenameSudokuAction(this, "Rename", KeyEvent.VK_R);
        addMenuBar();
        addStatusLine();
        this.myDisplay = new Display();
        this.myShell = new Shell(myDisplay, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
        myShell.addListener(SWT.Close, new Listener()
        {
            // Does not seem to be ever called in tests
            public void handleEvent(Event event)
            {
                System.out.println("Running close listener");
                exitSudokuAction.run();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Running Shutdown Hook");
            }
        });
        solutionFont = SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD);
        solutionSmallFont = SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL);
        createActions();
    }

    private boolean slideShowEnabled = false;

    boolean getSlideShowEnabled()
    {
        return (slideShowEnabled);
    }

    Integer getSlideShowPause()
    { // Only valid if slide show is enabled
        return (slideShowPause);
    }

    boolean getStopSlideShow()
    { // Only valid if slide show is enabled
        return (stopSlideShow);
    }

    @Override
    protected boolean canHandleShellCloseEvent()
    {
        System.out.println("Running canHandleShellCloseEvent");
        exitSudokuAction.run();
        // If it returns from this method the user has answered "No" thus return false
        return false;
    }

    void terminate()
    {
        System.out.println("Running AppMain.terminate");
        this.dispose();
        System.exit(0);
    }

    public void dispose()
    {
        System.out.println("AppMain.dispose");
        if (solutionFont != null)
        {
            solutionFont.dispose();
        }
        if (solutionSmallFont != null)
        {
            solutionSmallFont.dispose();
        }
    }

    private static Class<?> legalValClassUi = LegalValues_9.class;
    private AppState        status          = AppState.EMPTY;

    public void setState(AppState val)
    {
        // Check old status before
        AppState oldStatus = status;
        status = val;
        if (val == AppState.SOLVING || mySudoku == null)
        {
            setSolveEnabled(false, false);
            if (btnNext != null)
            {
                btnNext.setEnabled((status == AppState.SOLVING || btnManual.isEnabled()) && btnManual.getSelection());
            }
            if (slider != null)
            {
                slider.setEnabled(
                        (status == AppState.SOLVING || btnAutomatic.isEnabled()) && btnAutomatic.getSelection());
            }
        }
        else if (val == AppState.EMPTY && mySudoku != null)
        {
            if (oldStatus == AppState.SOLVING)
            {
                if (btnNext != null)
                {
                    btnNext.setEnabled(false);
                }
            }
            setSolveEnabled(true, false);
        }
        else if (val == AppState.CREATING)
        {
            recursiveSetEnabled(groupSlide, false);
        }
    }

    private Display myDisplay;

    public Display getDisplay()
    {
        return (myDisplay);
    }

    private Shell myShell;

    public Shell getShell()
    {
        return (myShell);
    }

    public Values<LegalValuesGenClass> getSudokuPb()
    {
        return (mySudoku);
    }

    public <LegalValuesGen extends LegalValuesGenClass> void setSudokuPb(Values<LegalValuesGenClass> newSudoku)
    {
        mySudoku = newSudoku;
        if (mySudoku != null)
        {
            for (int row = 0; row < getMaxRows(); row++)
            {
                for (int col = 0; col < getMaxCols(); col++)
                {
                    SolNCandTexts uiField = uiFields.get(row).get(col);
                    SingleCellValue<?> sVal = mySudoku.getCell(row, col);
                    if (sVal != null)
                    {
                        LegalValuesGenClass value = sVal.getSolution();
                        if (value != null)
                        {
                            // String s = value.toDisplayString();
                            uiField.solution.setText(value.toDisplayString());
                        }
                        else
                        {
                            uiField.solution.setText(StringUtils.EMPTY);
                        }
                    }
                    else
                    {
                        uiField.solution.setText(StringUtils.EMPTY);
                    }
                    uiField.input.setText(StringUtils.EMPTY);
                    if (uiField.candidatesWidgets != null)
                    {
                        for (int ind = 0; ind < getCandidatesNumber(); ind++)
                        { // Eliminate single settings from other sudokus
                            uiField.candidatesWidgets.get(ind).setVisible(true);
                        }
                        // uiField.candidates.get(0).getParent().setVisible(true);;
                    }
                }
            }
            mySudoku.addCandidatesListener(this);
            mySudoku.addCandidatesResetListener(this);
            mySudoku.addRollbackListener(this);
            mySudoku.addSolutionListener(this);
            mySudoku.addSavedListener(this);
        }
    }

    private class SolNCandTexts
    {
        public Text       solution;
        public Combo      input;
        public List<Text> candidatesWidgets = null;
        String            toolTip           = "";

        public SolNCandTexts()
        {
            Class legalValuesClass = getLegalValClassUi();
            if (legalValuesClass == LegalValues_4.class || legalValuesClass == LegalValues_9.class
                    || legalValuesClass == LegalValues_16.class)
            {
                candidatesWidgets = new Vector<Text>(getCandidatesNumber());
            }
            else
            {
                // Nothing to do for the tool tip
            }
        }
    }

    private void setCompositeVisibility(SudokuType type)
    {
        // System.out
        // .println("SIZE: cellComposites[" + cellCompositesPtr.length + "][" +
        // cellCompositesPtr[0].length + "]");
        // System.out.println("getMaxRows() / getRectangleLength() = " + getMaxRows() +
        // "/" + getRectangleLength());
        if (cellCompositesPtr != null)
        {
            for (int rowBlock = 0; rowBlock < getMaxRows() / getRectangleLength(); rowBlock++)
            {
                for (int colBlock = 0; colBlock < getMaxCols() / getRectangleLength(); colBlock++)
                {
                    Class<?> legalClass = getLegalValClassUi();
                    if (legalClass == SudokuStepper.LegalValues_4.class
                            || legalClass == SudokuStepper.LegalValues_16.class
                            || legalClass == SudokuStepper.LegalValues_25.class)
                    {
                        // Only SINGLE supported
                        cellCompositesPtr[rowBlock][colBlock].setVisible(true);
                    }
                    else if (legalClass == SudokuStepper.LegalValues_9.class)
                    {
                        switch (type)
                        {
                        case SAMURAI:
                            // System.out.println("cellComposites[" + rowBlock + "][" + colBlock + "]");
                            cellCompositesPtr[rowBlock][colBlock]
                                    .setVisible((colBlock != 3 || (rowBlock > 1 && rowBlock < 5))
                                            && (rowBlock != 3 || (colBlock > 1 && colBlock < 5)));
                            break;
                        default:
                            cellCompositesPtr[rowBlock][colBlock].setVisible(rowBlock < 3 && colBlock < 3);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Create contents of the application window.
     * 
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent)
    {
        appParent = parent;
        // parent.setSize(500, 300);
        System.out.println("createContents");
        setStatus(StringUtils.EMPTY);
        Composite OverallContainer = new Composite(parent, SWT.NONE);
        // OverallContainer.setLayout(new GridLayout(1, false));
        OverallContainer.setLayout(new FormLayout());
        //
        // Text box for sudoku name
        //
        grpSudokuName = new Group(OverallContainer, SWT.BORDER | SWT.SHADOW_OUT);
        grpSudokuName.setText("Sudoku Name:");
        grpSudokuName.setLayout(new FormLayout());
        FormData fd_grpSudokuName = new FormData();
        fd_grpSudokuName.bottom = new FormAttachment(0, 60);
        fd_grpSudokuName.right = new FormAttachment(100, -3);
        fd_grpSudokuName.top = new FormAttachment(0, TOP_MARGIN);
        fd_grpSudokuName.left = new FormAttachment(0, 3);
        grpSudokuName.setLayoutData(fd_grpSudokuName);
        grpSudokuName.setVisible(false);

        // Label lblName = new Label(grpSudokuName, SWT.BORDER | SWT.SHADOW_OUT);
        // FormData fd_lblName = new FormData();
        // fd_lblName.bottom = new FormAttachment(0, 25);
        // fd_lblName.top = new FormAttachment(0, 3);
        // fd_lblName.left = new FormAttachment(0, 5);
        // fd_lblName.right = new FormAttachment(0, 100);
        // lblName.setLayoutData(fd_lblName);
        // lblName.setText("Sudoku name: ");

        txtName = new Text(grpSudokuName, SWT.BORDER | SWT.SHADOW_OUT);
        txtName.setEditable(false);
        txtName.setText(StringUtils.EMPTY);
        FormData fd_txtName = new FormData();
        fd_txtName.bottom = new FormAttachment(0, 25);
        fd_txtName.top = new FormAttachment(0, 3);
        fd_txtName.left = new FormAttachment(0, 5); // new FormAttachment(lblName, 6);
        fd_txtName.right = new FormAttachment(100, -3);
        txtName.setLayoutData(fd_txtName);
        txtName.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent arg0)
            {
                String input = txtName.getText();
                if (input != null)
                {
                    input = input.trim();
                }
                if (cellCompositesPtr != null)
                {
                    mySudoku.setName(input);
                    ((Group) (cellCompositesPtr[0][0].getParent())).setText(input);
                }
            }
        });
        //
        // Scrolled sudoku
        //
        FormData fd_grpSudokublocksScrolled = new FormData();
        grpSudokuScrolled = new ScrolledComposite(OverallContainer, SWT.V_SCROLL | SWT.H_SCROLL);

        fd_grpSudokublocksScrolled.right = new FormAttachment(100, -3);
        fd_grpSudokublocksScrolled.top = new FormAttachment(0, TOP_MARGIN);
        fd_grpSudokublocksScrolled.left = new FormAttachment(0, 3);
        grpSudokuScrolled.setLayoutData(fd_grpSudokublocksScrolled);
        grpSudokuScrolled.setBackground(myDisplay.getSystemColor(SWT.COLOR_DARK_MAGENTA));

        grpSudokuScrolledContents = new Group(grpSudokuScrolled, SWT.NONE);
        grpSudokuScrolledContents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // for (int i = 0; i < 20; i++)
        // {
        // Text textSub = new Text(grpSudokuScrolledContents, SWT.BORDER);
        // textSub.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        // }

        // GridLayout layoutData = new GridLayout(MAXCOLS / RECTANGLELENGTH, true);
        // layoutData.marginRight = 5;
        // layoutData.marginLeft = 5;
        // layoutData.marginTop = 5;
        // layoutData.marginBottom = 5;
        // grpSudokuBlocks.setLayoutData(layoutData);
        createSudokuContents(SudokuType.SAMURAI); // Must be compatible with legalValClassUi
        //
        // Buttons and other rulers...
        //
        Group grpButtons = new Group(OverallContainer, SWT.NONE);
        FormData fd_grpButtons = new FormData();
        fd_grpButtons.top = new FormAttachment(100, -72);
        fd_grpButtons.bottom = new FormAttachment(100, -26);
        fd_grpButtons.left = new FormAttachment(0);
        fd_grpButtons.right = new FormAttachment(100);
        grpButtons.setLayoutData(fd_grpButtons);
        // grpButtons.setText("Buttons");
        RowLayout rl_grpButtons = new RowLayout(SWT.HORIZONTAL);
        rl_grpButtons.wrap = false;
        rl_grpButtons.pack = false;
        rl_grpButtons.justify = true;
        rl_grpButtons.fill = true;
        grpButtons.setLayout(rl_grpButtons);
        // AppMain app = this;
        btnFreeze = new Button(grpButtons, SWT.NONE);
        btnFreeze.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Freeze");
                freezeSudokuAction.run();
            }
        });
        btnFreeze.setText("Freeze");
        setFreezeEnabled(false);

        btnSlideShow = new Button(grpButtons, SWT.NONE);
        btnSlideShow.setText("Slide Show On/Off");
        btnSlideShow.setEnabled(true);

        btnSolve = new Button(grpButtons, SWT.NONE);
        btnSolve.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Solve");
                btnManual.setEnabled(false);
                btnAutomatic.setEnabled(false);
                stopSlideShow = false;
                btnPause.setEnabled(slider.getEnabled());
                // if (btnManual.getSelection())
                // {
                // btnNext.setEnabled(true);
                // }
                solveSudokuAction.run();
                System.out.println("Started solving");
            }
        });
        btnSolve.setText("Solve");
        setSolveEnabled(false, false);

        btnOthSolutions = new Button(grpButtons, SWT.NONE);
        btnOthSolutions.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed OtherSolutions");
                btnManual.setEnabled(false);
                btnAutomatic.setEnabled(false);
                stopSlideShow = false;
                btnPause.setEnabled(false);
                // if (btnManual.getSelection())
                // {
                // btnNext.setEnabled(true);
                // }
                nextSolutionSudokuAction.run();
                System.out.println("Started searching for next solution");
            }
        });
        btnOthSolutions.setText("Next Solution");
        btnOthSolutions.setEnabled(false);

        //
        // Slides for manual and automatic modes
        //
        groupSlide = new Group(OverallContainer, SWT.NONE);
        // groupSlide.setText("Slide");
        // groupSlide.setBackground(SWTResourceManager.getColor(128, 128, 128));
        groupSlide.setLayout(new FormLayout());
        FormData fd_groupSlide = new FormData();
        fd_groupSlide.bottom = new FormAttachment(grpButtons, -6);
        fd_groupSlide.top = new FormAttachment(grpSudokuScrolled, 9);
        fd_groupSlide.right = new FormAttachment(100);
        fd_groupSlide.left = new FormAttachment(grpButtons, 0, SWT.LEFT);
        groupSlide.setLayoutData(fd_groupSlide);
        groupSlide.setEnabled(false);
        slideShowEnabled = groupSlide.getEnabled();

        btnManual = new Button(groupSlide, SWT.RADIO);
        FormData fd_btnManual = new FormData();
        fd_btnManual.top = new FormAttachment(0, 5);
        fd_btnManual.left = new FormAttachment(0, 5);
        btnManual.setLayoutData(fd_btnManual);
        btnManual.setText("Manual");

        btnNext = new Button(groupSlide, SWT.NONE);
        btnNext.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Next");
                if (solvingThread != null)
                {
                    synchronized (solvingThread)
                    {
                        solvingThread.notify();
                    }
                }
            }
        });
        FormData fd_btnNext = new FormData();
        fd_btnNext.bottom = new FormAttachment(btnManual, 2, SWT.BOTTOM);
        fd_btnNext.left = new FormAttachment(btnManual, 25);
        btnNext.setLayoutData(fd_btnNext);
        btnNext.setText("Next");

        btnAutomatic = new Button(groupSlide, SWT.RADIO);
        FormData fd_btnAutomatic = new FormData();
        fd_btnAutomatic.top = new FormAttachment(0, 28);
        fd_btnAutomatic.left = new FormAttachment(0, 5);
        btnAutomatic.setLayoutData(fd_btnAutomatic);
        btnAutomatic.setText("Automatic");

        Label lblPause = new Label(groupSlide, SWT.NONE);
        FormData fd_lblPause = new FormData();
        fd_lblPause.bottom = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        fd_lblPause.left = new FormAttachment(btnNext, 0, SWT.LEFT);
        lblPause.setLayoutData(fd_lblPause);
        lblPause.setText("Pause:");
        final int rightMarginSlider = -45;
        final int rightMarginCurrentPauseValue = -5;
        slider = new Slider(groupSlide, SWT.NONE);
        FormData fd_slider = new FormData();
        fd_slider.left = new FormAttachment(lblPause, 5, SWT.RIGHT);
        fd_slider.bottom = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        fd_slider.right = new FormAttachment(100, rightMarginSlider);
        slider.setLayoutData(fd_slider);
        slider.setBackground(myDisplay.getSystemColor(SWT.COLOR_DARK_GRAY));
        slider.setToolTipText("Set the time to pause in seconds after findng a new cell solution value");
        final int minSecondsPause = 0;
        final int maxSecondsPause = 60;
        final int thumbWidth = 1;

        slider.setBounds(0, 0, 40, 200);
        slider.setThumb(thumbWidth);
        slider.setMaximum(maxSecondsPause + thumbWidth);
        slider.setMinimum(minSecondsPause);
        slider.setIncrement(1);
        slider.setPageIncrement(10);

        Label lblNoPause = new Label(groupSlide, SWT.NONE);
        lblNoPause.setText(Integer.toString(minSecondsPause) + secondUnitStr);
        FormData fd_lblNoPause = new FormData();
        fd_lblNoPause.top = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        fd_lblNoPause.left = new FormAttachment(slider, 0, SWT.LEFT);
        lblNoPause.setLayoutData(fd_lblNoPause);

        Label lblMinute = new Label(groupSlide, SWT.NONE);
        lblMinute.setText(Integer.toString(maxSecondsPause) + secondUnitStr);
        FormData fd_ldlMinute = new FormData();
        fd_ldlMinute.top = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        fd_ldlMinute.right = new FormAttachment(100, rightMarginSlider - 10);
        lblMinute.setLayoutData(fd_ldlMinute);

        btnPause = new Button(groupSlide, SWT.NONE);
        btnPause.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Pause");
                stopSlideShow = true;
                // if (solvingThread != null)
                // {
                // synchronized (solvingThread)
                // {
                // solvingThread.notify();
                // }
                // }
                setSolveEnabled(true, false);
                btnPause.setEnabled(false);
            }
        });
        FormData fd_btnPause = new FormData();
        fd_btnPause.top = new FormAttachment(btnAutomatic, 3, SWT.BOTTOM);
        fd_btnPause.right = new FormAttachment(100, rightMarginCurrentPauseValue);
        btnPause.setLayoutData(fd_btnPause);
        btnPause.setText("Pause");

        lblCurrent = new Label(groupSlide, SWT.RIGHT);
        FormData fd_lblCurrent = new FormData();
        fd_lblCurrent.bottom = new FormAttachment(btnAutomatic, 2, SWT.BOTTOM);
        fd_lblCurrent.right = new FormAttachment(100, rightMarginCurrentPauseValue);
        lblCurrent.setLayoutData(fd_lblCurrent);
        // groupSlideLabels.setBackground(SWTResourceManager.getColor(56, 56, 0));
        lblCurrent.setText(lblMinute.getText());

        fd_grpSudokublocksScrolled.bottom = new FormAttachment(grpButtons, -128);

        slider.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                slideShowPause = slider.getSelection();
                // System.out.println("Selected: " + slideShowPause);
                lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            { // Obviously never called
                slideShowPause = slider.getSelection();
                // System.out.println("DefaultSelected: " + slideShowPause);
                lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
            }
        });
        // Button btnStop = new Button(groupSlide, SWT.NONE);
        // FormData fd_btnStop = new FormData();
        // fd_btnStop.bottom = new FormAttachment(btnAutomatic, 2, SWT.BOTTOM);
        // fd_btnStop.right = new FormAttachment(100, -5);
        // btnStop.setLayoutData(fd_btnStop);
        // btnStop.setText("Stop");
        //
        // Button btnStart = new Button(groupSlide, SWT.NONE);
        // FormData fd_button = new FormData();
        // fd_button.right = new FormAttachment(btnStop, -5, SWT.LEFT);
        // fd_button.bottom = new FormAttachment(btnAutomatic, 2, SWT.BOTTOM);
        // btnStart.setLayoutData(fd_button);
        // btnStart.addSelectionListener(new SelectionAdapter()
        // {
        // @Override
        // public void widgetSelected(SelectionEvent e)
        // {
        // }
        // });
        // btnStart.setText("Start");

        // Slider slider = new Slider(groupSlide, SWT.NONE);
        // FormData fd_slider = new FormData();
        // fd_slider.left = new FormAttachment(lblSpeed, 5, SWT.RIGHT);
        // fd_slider.bottom = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        // fd_slider.right = new FormAttachment(btnStart, -5, SWT.LEFT);
        // slider.setLayoutData(fd_slider);
        // final int minSecondsPause = 0;
        // final int maxSecondsPause = 60;
        // final int thumbWidth = 1;
        //
        // slider.setBounds(0, 0, 40, 200);
        // slider.setThumb(thumbWidth);
        // slider.setMaximum(maxSecondsPause + thumbWidth);
        // slider.setMinimum(minSecondsPause);
        // slider.setIncrement(1);
        // slider.setPageIncrement(10);
        //
        // Group groupSlideLabels = new Group(groupSlide, SWT.NONE);
        // groupSlideLabels.setLayout(new GridLayout(3, true));
        // FormData fd_groupSlideLabels = new FormData();
        // fd_groupSlideLabels.bottom = new FormAttachment(btnManual, 0, SWT.BOTTOM);
        // fd_groupSlideLabels.top = new FormAttachment(groupSlide, 0, SWT.TOP);
        // fd_groupSlideLabels.right = new FormAttachment(slider, 0, SWT.RIGHT);
        // fd_groupSlideLabels.left = new FormAttachment(slider, 0, SWT.LEFT);
        // groupSlideLabels.setLayoutData(fd_groupSlideLabels);
        // groupSlideLabels.setEnabled(false);
        //
        // Label lblNoPause = new Label(groupSlideLabels, SWT.NONE);
        // lblNoPause.setText(Integer.toString(minSecondsPause) + secondUnitStr);
        // GridData gd_lblNoPause = new GridData(SWT.LEFT, SWT.CENTER);
        // lblNoPause.setLayoutData(gd_lblNoPause);
        //
        // Label lblCurrent = new Label(groupSlideLabels, SWT.NONE);
        // lblCurrent.setText(Integer.toString(slider.getSelection()) + secondUnitStr);
        // GridData gd_lblCurrent = new GridData(SWT.CENTER, SWT.CENTER);
        // lblCurrent.setLayoutData(gd_lblCurrent);
        //
        // Label lblMinute = new Label(groupSlideLabels, SWT.NONE);
        // lblMinute.setText(Integer.toString(maxSecondsPause) + secondUnitStr);
        // GridData gd_ldlMinute = new GridData(SWT.RIGHT, SWT.CENTER);
        // lblMinute.setLayoutData(gd_ldlMinute);
        //
        // slider.addSelectionListener(new SelectionListener()
        // {
        //
        // @Override
        // public void widgetSelected(SelectionEvent arg0)
        // {
        // // System.out.println("Selected: " + slider.getSelection());
        // slideShowPause = slider.getSelection();
        // lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
        // }
        //
        // @Override
        // public void widgetDefaultSelected(SelectionEvent arg0)
        // { // Obviously never called
        // // System.out.println("DefaultSelected: " + slider.getSelection());
        // slideShowPause = slider.getSelection();
        // lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
        // }
        // });

        btnManual.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Button source = (Button) e.getSource();

                if (source.getSelection())
                {
                    System.out.println("Manual set");
                    // btnStart.setEnabled(false);
                    // btnStop.setEnabled(false);
                    lblNoPause.setEnabled(false);
                    lblPause.setEnabled(false);
                    lblMinute.setEnabled(false);
                    lblCurrent.setEnabled(false);
                    btnPause.setEnabled(false);
                    slider.setEnabled(false);
                    btnNext.setEnabled(status == AppState.SOLVING);
                    slideShowPause = null;
                    stopSlideShow = false;
                }
            }

        });

        btnAutomatic.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Button source = (Button) e.getSource();

                if (source.getSelection())
                {
                    // btnStart.setEnabled(true);
                    // btnStop.setEnabled(true);
                    lblNoPause.setEnabled(true);
                    lblPause.setEnabled(true);
                    lblMinute.setEnabled(true);
                    lblCurrent.setEnabled(true);
                    btnPause.setEnabled(false);
                    slider.setEnabled(true);
                    btnNext.setEnabled(false);
                    slideShowPause = slider.getSelection();
                    stopSlideShow = false;
                    lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
                }
            }

        });
        btnSlideShow.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                toggleSlideShowSudokuAction.run();
            }
        });
        btnManual.setSelection(manualWasEnabled);
        // btnStart.setEnabled(!manualWasEnabled);
        // btnStop.setEnabled(!manualWasEnabled);
        lblNoPause.setEnabled(!manualWasEnabled);
        lblPause.setEnabled(!manualWasEnabled);
        lblMinute.setEnabled(!manualWasEnabled);
        lblCurrent.setEnabled(!manualWasEnabled);
        btnPause.setEnabled(!manualWasEnabled);
        slider.setEnabled(!manualWasEnabled);
        btnNext.setEnabled(manualWasEnabled);
        recursiveSetEnabled(groupSlide, false);

        // Menus
        // Menu menuBar = new Menu(myShell, SWT.BAR);
        // MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        // fileMenuHeader.setText("&File");
        //
        // Menu fileMenu = new Menu(myShell, SWT.DROP_DOWN);
        // fileMenuHeader.setMenu(fileMenu);
        //
        // MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
        // fileSaveItem.setText("&Save");
        //
        // MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
        // fileExitItem.setText("E&xit");
        //
        // MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        // helpMenuHeader.setText("&Help");
        //
        // Menu helpMenu = new Menu(myShell, SWT.DROP_DOWN);
        // helpMenuHeader.setMenu(helpMenu);
        //
        // MenuItem helpGetHelpItem = new MenuItem(helpMenu, SWT.PUSH);
        // helpGetHelpItem.setText("&Get Help");
        //
        // fileExitItem.addSelectionListener(new fileExitItemListener());
        // fileSaveItem.addSelectionListener(new fileSaveItemListener());
        // helpGetHelpItem.addSelectionListener(new helpGetHelpItemListener());

        return OverallContainer;
    }

    public static int getRectangleLength()
    {
        return (getIntByReflection("getRectangleLength"));
    }

    /**
     * @param sudokuContents
     */
    protected <LegalValuesGen extends LegalValuesGenClass> void createSudokuContents(SudokuType sudokuType)
    {
        uiFields = new HashMap<Integer, Map<Integer, SolNCandTexts>>(getCandidatesPerRow());
        int textCounter = 0;
        grpSudokuScrolledContents.setLayout(new GridLayout(getMaxCols() / getRectangleLength(), true));
        // Delete all children if they already exist
        Control[] children = grpSudokuScrolledContents.getChildren();
        for (Control child : children)
        {
            child.dispose();
        }
        //
        // Sudoku itsself
        //
        cellCompositesPtr = new Composite[getMaxRows() / getRectangleLength()][getMaxCols() / getRectangleLength()];
        // Compute size
        Text candidateTextSample = new Text(grpSudokuScrolledContents, SWT.BORDER | SWT.READ_ONLY);
        candidateTextSample.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        candidateTextSample.setBackground(myDisplay.getSystemColor(SWT.COLOR_DARK_GRAY));
        candidateTextSample.setFont(solutionSmallFont);
        candidateTextSample.setText(
                LegalValuesGenClass.toDisplayString(legalValClassUi, getRectangleLength() * getRectangleLength() - 1));
        Rectangle textRect = candidateTextSample.getBounds();
        candidateTextSample.dispose();

        for (int blockRow = 1; blockRow <= getMaxRows() / getRectangleLength(); blockRow++)
        {
            for (int blockCol = 1; blockCol <= getMaxCols() / getRectangleLength(); blockCol++)
            {
                // System.out.println("blockRow=" + blockRow + ", blockCol=" + blockCol);
                Composite cellComposite = new Composite(grpSudokuScrolledContents, SWT.NONE);
                cellCompositesPtr[blockRow - 1][blockCol - 1] = cellComposite;
                cellComposite.setBackground(myDisplay.getSystemColor(SWT.COLOR_GREEN));
                cellComposite.setLayout(new GridLayout(getRectangleLength(), false));
                for (int row = 1; row <= getRectangleLength(); row++)
                {
                    for (int col = 1; col <= getRectangleLength(); col++)
                    {
                        // System.out.println("row=" + row + ", col=" + col);

                        Composite composite_111 = new Composite(cellComposite, SWT.NONE);
                        StackLayout gl_composite_111 = new StackLayout();

                        composite_111.setLayout(gl_composite_111);
                        // Create text for the display of solutions
                        Text solutionText = new Text(composite_111, SWT.BORDER | SWT.CENTER);
                        solutionText.setBackground(myDisplay.getSystemColor(COLOR_SOLT_BCKGRD));
                        solutionText.setFont(solutionFont);
                        solutionText.setText(LegalValuesGenClass.toDisplayString(legalValClassUi,
                                ((row - 1) * getRectangleLength() + col)));
                        solutionText.setToolTipText("Input or solution");
                        gl_composite_111.topControl = solutionText;
                        int totalRow = (blockRow - 1) * getRectangleLength() + row - 1;
                        int totalCol = (blockCol - 1) * getRectangleLength() + col - 1;
                        if (!uiFields.containsKey(totalRow))
                        {
                            uiFields.put(totalRow, new HashMap<Integer, SolNCandTexts>(getCandidatesPerRow()));
                        }
                        if (!uiFields.get(totalRow).containsKey(totalCol))
                        {
                            uiFields.get(totalRow).put(totalCol, new SolNCandTexts());
                        }
                        uiFields.get(totalRow).get(totalCol).solution = solutionText;
                        // uiFields.get(totalRow).get(totalCol).solution.addListener(SWT.Hide, new
                        // Listener()
                        // {
                        // public void handleEvent(Event e)
                        // {
                        //// System.out.println(e.widget + " just hidden, row: " + totalRow + ", col: "
                        // + totalCol);
                        // }
                        // });
                        // uiFields.get(totalRow).get(totalCol).solution.addListener(SWT.Show, new
                        // Listener()
                        // {
                        // public void handleEvent(Event e)
                        // {
                        //// System.out.println(e.widget + " just shown, row: " + totalRow + ", col: " +
                        // totalCol);
                        // }
                        // });
                        // Create combo box for input of a new Sudoku
                        Combo combo = new Combo(composite_111, SWT.DROP_DOWN);
                        // HashMap<String, String> alternateInputs = new HashMap<String, String>();
                        // try
                        // {
                        // Method m = legalValClassUi.getMethod("getAlternatePatterns");
                        // alternateInputs = (HashMap) m.invoke(null);
                        // }
                        // catch (IllegalArgumentException | IllegalAccessException | SecurityException
                        // | NoSuchMethodException | InvocationTargetException e1)
                        // {
                        // // TODO Auto-generated catch block
                        // e1.printStackTrace();
                        // }
                        String[] items = new String[LegalValuesGen.values(legalValClassUi).size() // +
                                                                                                  // alternateInputs.size()
                        ];
                        // You need to set a list of items to avoid an exception
                        int valInd = 0;
                        for (LegalValuesGenClass val : LegalValuesGen.values(legalValClassUi))
                        {
                            items[valInd] = LegalValuesGenClass.toDisplayString(legalValClassUi, val.val());
                            valInd++;
                        }
                        // for (String val : alternateInputs.keySet())
                        // {
                        // items[valInd] = val;
                        // valInd++;
                        // }
                        combo.setItems(items);
                        combo.setFont(solutionFont);
                        combo.setToolTipText("Choose the initial value for this cell if any ");
                        combo.addModifyListener(new ModifyListener()
                        {

                            @Override
                            public void modifyText(ModifyEvent arg0)
                            {
                                if (status == AppState.CREATING)
                                {
                                    String input = combo.getText();
                                    if (input != null)
                                    {
                                        input = input.trim();
                                    }
                                    // combo.setText(input); Infinite stack
                                    if (!input.isEmpty())
                                    {
                                        boolean found = false;
                                        for (String valid : combo.getItems())
                                        {
                                            if (input.equals(valid))
                                            {
                                                found = true;
                                                break;
                                            }
                                        }
                                        // Class<?> valClass = legalValClassUi;
                                        LegalValuesGen val = null;
                                        try
                                        {
                                            // if (found)
                                            // {
                                            // try
                                            // {
                                            // HashMap<String, String> alternateInputs = (HashMap) legalValClassUi
                                            // .getMethod("getAlternatePatterns").invoke(null);
                                            // if (alternateInputs.containsKey(input))
                                            // {
                                            // input = alternateInputs.get(input);
                                            // combo.setText(input);
                                            // }
                                            // }
                                            // catch (NoSuchMethodException ex)
                                            // {
                                            // // just swallow
                                            // }
                                            // catch (IllegalArgumentException | IllegalAccessException
                                            // | SecurityException | InvocationTargetException ex)
                                            // {
                                            // ex.printStackTrace();
                                            // }
                                            // }
                                            if (!found)
                                            {
                                                setStatus(input + " is an invalid value for this cell!");
                                                combo.setText(StringUtils.EMPTY);
                                            }
                                            else
                                            {
                                                setStatus(StringUtils.EMPTY);
                                                val = (LegalValuesGen) (legalValClassUi.getConstructor(String.class)
                                                        .newInstance(input));
                                                // mySudoku.updateCandidateList(totalRow, totalCol, val, true, false);
                                                mySudoku.getCell(totalRow, totalCol).getCandidates().clear();
                                                mySudoku.getCell(totalRow, totalCol).setSolution(val, totalRow,
                                                        totalCol, null, true, false);
                                                mySudoku.getCell(totalRow, totalCol).setInput(true);
                                                mySudoku.setSaved(false);
                                            }
                                        }
                                        catch (InstantiationException | IllegalAccessException
                                                | IllegalArgumentException | InvocationTargetException
                                                | NoSuchMethodException | SecurityException e)
                                        {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                            setStatus(input + " is an invalid value for this cell!");
                                            combo.setText(StringUtils.EMPTY);
                                        }
                                    }
                                    else
                                    {
                                        mySudoku.resetCell(legalValClassUi, totalRow, totalCol);
                                    }
                                    List<List<int[]>> conflicts = mySudoku.areContentsLegal();
                                    inputUpdated();
                                    boolean freezeAllowed = conflicts.size() == 0
                                            && mySudoku.getNumberOfSolutions() > 0;
                                    setFreezeEnabled(freezeAllowed);
                                }

                            }
                        });
                        combo.setBackground(myDisplay.getSystemColor(COLOR_INPUT_BCKGRD));
                        uiFields.get(totalRow).get(totalCol).input = combo;
                        // Create the small boxes showing the still possible solutions if visible
                        if (uiFields.get(totalRow).get(totalCol).candidatesWidgets != null)
                        {
                            Composite possibleSolValues = new Composite(composite_111, SWT.NONE);
                            possibleSolValues.setBackground(myDisplay.getSystemColor(SWT.COLOR_BLUE));
                            GridLayout gl_possibleSolValues = new GridLayout(getRectangleLength(), false);
                            gl_possibleSolValues.horizontalSpacing = 2;
                            gl_possibleSolValues.verticalSpacing = 1;
                            gl_possibleSolValues.marginWidth = 0;
                            gl_possibleSolValues.marginHeight = 0;
                            possibleSolValues.setLayout(gl_possibleSolValues);
                            gl_composite_111.topControl = possibleSolValues;
                            for (int rowSub = 1; rowSub <= getRectangleLength(); rowSub++)
                            {
                                for (int colSub = 1; colSub <= getRectangleLength(); colSub++)
                                {
                                    textCounter++;
                                    // System.out.println(
                                    // "textCounter=" + textCounter + ", rowSub=" + rowSub + ", colSub=" + colSub);

                                    Text candidateText = new Text(possibleSolValues, SWT.BORDER | SWT.READ_ONLY);
                                    candidateText
                                            .setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
                                    candidateText.setBackground(myDisplay.getSystemColor(SWT.COLOR_DARK_GRAY));
                                    candidateText.setFont(solutionSmallFont);
                                    candidateText.setText(LegalValuesGenClass.toDisplayString(legalValClassUi,
                                            (rowSub - 1) * getRectangleLength() + colSub));
                                    candidateText.setBounds(textRect);
                                    // solutionText.setToolTipText("01234");
                                    uiFields.get(totalRow).get(totalCol).candidatesWidgets.add(candidateText);
                                }
                            }
                        }
                        else
                        {
                            for (int rowSub = 1; rowSub <= getRectangleLength(); rowSub++)
                            {
                                for (int colSub = 1; colSub <= getRectangleLength(); colSub++)
                                {
                                    String tooltip = "";
                                    boolean notEmpty = false;
                                    for (LegalValuesGenClass val : LegalValuesGen.values(legalValClassUi))
                                    {
                                        if (notEmpty)
                                        {
                                            tooltip += ", ";
                                        }
                                        tooltip += val.toDisplayString();
                                        notEmpty = true;
                                    }
                                    // Need parent in next line to display while there are no solution yet
                                    uiFields.get(totalRow).get(totalCol).solution.getParent().setToolTipText(tooltip);
                                }
                            }
                        }
                    }
                }
            }
        }
        grpSudokuScrolled.setContent(grpSudokuScrolledContents);
        grpSudokuScrolled.setExpandHorizontal(true);
        grpSudokuScrolled.setExpandVertical(true);

        Point minSize = grpSudokuScrolledContents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        grpSudokuScrolled.setMinSize(minSize);

        setCompositeVisibility(sudokuType);
        System.out.println("Created " + textCounter + " text elements");
    }

    private boolean firstTimeEnabled = true;
    private boolean manualWasEnabled = true;

    public void recursiveSetEnabled(Control ctrl, boolean enabled)
    {
        if (ctrl instanceof Composite)
        {
            Composite comp = (Composite) ctrl;
            comp.setEnabled(enabled);
            for (Control c : comp.getChildren())
            {
                recursiveSetEnabled(c, enabled);

            }
        }
        else
        {
            ctrl.setEnabled(enabled);
        }
    }

    public void setSolveEnabled(boolean enabled, boolean activateNextSolBtnIn)
    {
        if (btnSolve != null)
        {
            btnSolve.setEnabled(enabled);
        }
        if (btnSlideShow != null)
        {
            btnSlideShow.setEnabled(enabled);
        }
        if (btnOthSolutions != null)
        {
            btnOthSolutions.setEnabled(enabled);
        }
        if (solveSudokuAction != null)
        {
            solveSudokuAction.setEnabled(enabled);
        }
        if (nextSolutionSudokuAction != null)
        {
            nextSolutionSudokuAction.setEnabled(!enabled);
        }
        if (toggleSlideShowSudokuAction != null)
        {
            toggleSlideShowSudokuAction.setEnabled(enabled);
        }
        if (btnOthSolutions != null)
        {
            btnOthSolutions.setEnabled(activateNextSolBtnIn);
        }
        // if (!enabled)
        // {
        // if (btnManual != null)
        // {
        // btnManual.setEnabled(enabled);
        // }
        // if (btnAutomatic != null)
        // {
        // btnAutomatic.setEnabled(enabled);
        // }
        // }
    }

    private void setFreezeEnabled(boolean enabled)
    {
        if (btnFreeze != null)
        {
            btnFreeze.setEnabled(enabled);
        }
        if (freezeSudokuAction != null)
        {
            freezeSudokuAction.setEnabled(enabled);
        }
    }

    private void setUpdateEnabled(boolean enabled)
    {
        if (updateProblemSudokuAction != null)
        {
            updateProblemSudokuAction.setEnabled(enabled);
        }
    }
    // private class fileExitItemListener implements SelectionListener
    // {
    // public void widgetSelected(SelectionEvent event)
    // {
    // // shell.close();
    // // display.dispose();
    // }
    //
    // public void widgetDefaultSelected(SelectionEvent event)
    // {
    // // shell.close();
    // // display.dispose();
    // }
    // }

    class fileSaveItemListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent event)
        {
            // label.setText("Saved");
        }

        public void widgetDefaultSelected(SelectionEvent event)
        {
            // label.setText("Saved");
        }
    }

    class helpGetHelpItemListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent event)
        {
            // label.setText("No worries!");
        }

        public void widgetDefaultSelected(SelectionEvent event)
        {
            // label.setText("No worries!");
        }

    }

    /**
     * Create the actions.
     */
    private void createActions()
    {
        // Create the actions
        System.out.println("createActions");
    }

    private Action            renameSudokuAction          = new RenameSudokuAction(this, "&Rename",
            SWT.CTRL + KeyEvent.VK_R);
    private Action            newSudokuSingleAction2x2    = new NewSudokuAction(this, Values.SudokuType.SINGLE,
            LegalValues_4.class, "&New Single 2x2", SWT.CTRL + KeyEvent.VK_2);
    private Action            newSudokuSingleAction3x3    = new NewSudokuAction(this, Values.SudokuType.SINGLE,
            LegalValues_9.class, "&New Single 3x3", SWT.CTRL + KeyEvent.VK_3);
    private Action            newSudokuSingleAction4x4    = new NewSudokuAction(this, Values.SudokuType.SINGLE,
            LegalValues_16.class, "&New Single 4x4", SWT.CTRL + KeyEvent.VK_4);
    private Action            newSudokuSingleAction5x5    = new NewSudokuAction(this, Values.SudokuType.SINGLE,
            LegalValues_25.class, "&New Single 5x5", SWT.CTRL + KeyEvent.VK_5);
    private Action            newSudokuSamuraiAction      = new NewSudokuAction(this, Values.SudokuType.SAMURAI,
            LegalValues_9.class, "&New Samurai 3x3", SWT.CTRL + KeyEvent.VK_M);
    private Action            openProblemSudokuAction     = new OpenProblemSudokuAction(this, "&Open",
            SWT.CTRL + KeyEvent.VK_O, false, false);
    private Action            updateProblemSudokuAction   = new OpenProblemSudokuAction(this, "&Update Sudoku",
            SWT.CTRL + KeyEvent.VK_U, false, true);
    private Action            openSolutionSudokuAction    = new OpenSolutionSudokuAction(this, "&Open Solution",
            SWT.CTRL + KeyEvent.VK_L, true);
    private Action            saveSudokuAction            = new SaveSudokuAction(this, "&Save",
            SWT.CTRL + KeyEvent.VK_S);
    private Action            saveAsSudokuAction          = new SaveAsSudokuAction(this, "Save &As",
            SWT.CTRL + KeyEvent.VK_A);
    private Action            toggleSlideShowSudokuAction = new ToggleSlideShowSudokuAction(this, "S&lide Show On/Off",
            KeyEvent.VK_L);
    private Action            solveSudokuAction           = new SolveSudokuAction(this, "&Solve", KeyEvent.VK_S);
    private Action            nextSolutionSudokuAction    = new NextSolutionSudokuAction(this, "&Next Solution",
            KeyEvent.VK_N);
    private Action            exitSudokuAction            = new ExitSudokuAction(this, "&Exit",
            SWT.CTRL + KeyEvent.VK_E);
    private Action            freezeSudokuAction          = new FreezeSudokuAction(this, "&Freeze",
            SWT.CTRL + KeyEvent.VK_F);
    private Action            aboutSudokuAction           = new AboutSudokuAction(this, "&About",
            SWT.CTRL + SWT.SHIFT + KeyEvent.VK_A);
    private Button            btnOthSolutions             = null;
    private Button            btnSolve                    = null;
    private Button            btnFreeze                   = null;
    private Button            btnSlideShow                = null;
    private Button            btnAutomatic                = null;
    private Slider            slider                      = null;
    private Button            btnNext                     = null;
    private Button            btnPause                    = null;
    private Button            btnManual                   = null;
    private Group             groupSlide                  = null;
    private ScrolledComposite grpSudokuScrolled           = null;
    private Group             grpSudokuName               = null;
    private Text              txtName                     = null;
    private Label             lblCurrent                  = null;

    /**
     * Create the menu manager.
     * 
     * @return the menu manager
     */
    @Override
    protected MenuManager createMenuManager()
    {
        System.out.println("createMenuManager");
        MenuManager menuMgr = new MenuManager();

        MenuManager fileMenuMgr = new MenuManager("File");
        fileMenuMgr.setVisible(true);
        menuMgr.add(fileMenuMgr);
        fileMenuMgr.add(newSudokuSingleAction2x2);
        fileMenuMgr.add(newSudokuSingleAction3x3);
        fileMenuMgr.add(newSudokuSingleAction4x4);
        fileMenuMgr.add(newSudokuSingleAction5x5);
        fileMenuMgr.add(newSudokuSamuraiAction);
        // condEnableNewSamuraiAction();
        fileMenuMgr.add(openProblemSudokuAction);
        fileMenuMgr.add(updateProblemSudokuAction);
        setUpdateEnabled(true);
        fileMenuMgr.add(openSolutionSudokuAction);
        fileMenuMgr.add(saveSudokuAction);
        saveSudokuAction.setEnabled(false);
        fileMenuMgr.add(saveAsSudokuAction);
        saveAsSudokuAction.setEnabled(false);
        fileMenuMgr.add(exitSudokuAction);
        exitSudokuAction.setEnabled(true);

        MenuManager actionMenuMgr = new MenuManager("Action");
        actionMenuMgr.setVisible(true);
        menuMgr.add(actionMenuMgr);
        actionMenuMgr.add(freezeSudokuAction);
        setFreezeEnabled(false);

        actionMenuMgr.add(toggleSlideShowSudokuAction);
        toggleSlideShowSudokuAction.setEnabled(false);
        actionMenuMgr.add(solveSudokuAction);
        setSolveEnabled(false, false);

        actionMenuMgr.add(renameSudokuAction);
        renameSudokuAction.setEnabled(false);

        // MenuManager settingsMenuMgr = new MenuManager("Settings");
        // settingsMenuMgr.setVisible(true);
        // menuMgr.add(settingsMenuMgr);
        // settingsMenuMgr.add(use9ValuesAction);
        // settingsMenuMgr.add(use16ValuesAction);
        // setLegalValuesSwitchEnabled();

        MenuManager helpMenuMgr = new MenuManager("Help");
        helpMenuMgr.setVisible(true);
        menuMgr.add(helpMenuMgr);
        helpMenuMgr.add(aboutSudokuAction);

        return menuMgr;
    }

    /**
     * 
     */
    // public void setLegalValuesSwitchEnabled()
    // {
    // use16ValuesAction.setEnabled(legalValClassUi == LegalValues.class);
    // use9ValuesAction.setEnabled(legalValClassUi == LegalValues_16.class);
    // }

    /**
     * Create the toolbar manager.
     * 
     * @return the toolbar manager
     */
    @Override
    protected ToolBarManager createToolBarManager(int style)
    {
        System.out.println("createToolBarManager");
        ToolBarManager toolBarManager = new ToolBarManager(style);
        toolBarManager.add(action);
        return toolBarManager;
    }

    /**
     * Create the status line manager.
     * 
     * @return the status line manager
     */
    @Override
    protected StatusLineManager createStatusLineManager()
    {
        System.out.println("createStatusLineManager");
        StatusLineManager statusLineManager = new StatusLineManager();
        statusLineManager.setErrorMessage("AAAAA");
        return statusLineManager;
    }

    void startRenamingGui()
    {
        grpSudokuName.setVisible(true);
        ((FormData) (grpSudokuScrolled.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN + NAME_BOX_HEIGHT);
        if (mySudoku != null && mySudoku.getName() != null)
        {
            txtName.setText(mySudoku.getName());
        }
        else
        {
            txtName.setText(StringUtils.EMPTY);
        }
        txtName.setEditable(true);
        grpSudokuScrolled.getParent().layout(true, true);
        grpSudokuScrolled.redraw();
        grpSudokuScrolled.update();
        // grpSudokuBlocks.setEnabled(false);
        setSolveEnabled(false, false);
        setFreezeEnabled(true);
        renameSudokuAction.setEnabled(false);

        // must reset visible all solution fields because they got hidden by the line:
        // grpSudokuBlocks.getParent().layout(true, true);
        for (int row = 0; row < getMaxRows(); row++)
        {
            for (int col = 0; col < getMaxCols(); col++)
            {
                if (!uiFields.get(row).get(col).solution.getText().isEmpty())
                {
                    uiFields.get(row).get(col).solution.setVisible(true);
                }
            }
        }
    }

    void initGuiForNew(boolean updatingSudoku)
    {
        // It is important to first relayout and then set the uiFields
        setFreezeEnabled(false);
        setSolveEnabled(false, false);
        grpSudokuName.setVisible(true);
        ((FormData) (grpSudokuScrolled.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN + NAME_BOX_HEIGHT);
        if (mySudoku != null && mySudoku.getName() != null)
        {
            txtName.setText(mySudoku.getName());
        }
        else
        {
            txtName.setText(StringUtils.EMPTY);
        }
        txtName.setEditable(true);
        grpSudokuScrolled.getParent().layout(true, true);
        grpSudokuScrolled.redraw();
        grpSudokuScrolled.update();
        for (Integer row : uiFields.keySet())
        {
            for (Integer col : uiFields.get(row).keySet())
            {
                SolNCandTexts uiField = uiFields.get(row).get(col);
                if (updatingSudoku)
                {
                    SingleCellValue<?> cell = mySudoku.getCell(row, col);
                    if (cell.isInput())
                    {
                        uiField.input.setText(cell.getSolution().toDisplayString());
                    }
                }
                setSolutionNInputBckgrdColor(row, col, false);
                uiField.solution.setVisible(false);
                // uiField.input.setText(StringUtils.EMPTY);
                uiField.input.setVisible(true);
                if (uiField.candidatesWidgets != null)
                {
                    Text cand = uiField.candidatesWidgets.get(0);
                    cand.getParent().setVisible(false);
                    // for (int ind = 0; ind < getCandidatesNumber(); ind++)
                    // {
                    // Text cand = uiField.candidatesWidgets.get(ind);
                    // // cand.setVisible(false);
                    // cand.getParent().setVisible(false);
                    // }
                }
            }
        }
        // grpSudokuScrolled.setMinSize(grpSudokuScrolled.getContent().computeSize(SWT.DEFAULT,
        // SWT.DEFAULT));

        txtName.setFocus();

    }

    <LegalValuesGen extends LegalValuesGenClass> void freeze(boolean keepCandidatesVisibility, boolean runsInUiThread,
            boolean markLastSolutionFound)
    {
        // It is important to first relayout and then set the uiFields
        setFreezeEnabled(false);
        setSolveEnabled(status != AppState.SOLVING, false);
        grpSudokuName.setVisible(false);
        ((FormData) (grpSudokuScrolled.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN);
        txtName.setEditable(false);
        grpSudokuScrolled.getParent().layout(true, true);
        grpSudokuScrolled.redraw();
        grpSudokuScrolled.update();
        for (Integer row : uiFields.keySet())
        {
            for (Integer col : uiFields.get(row).keySet())
            {
                SolNCandTexts uiField = uiFields.get(row).get(col);
                SingleCellValue<?> sVal = mySudoku.getCell(row, col);
                if (sVal != null && sVal.getCandidates().isEmpty())
                {
                    solutionUpdated(row, col, runsInUiThread, markLastSolutionFound);
                }
                else
                {
                    uiField.solution.setVisible(false);
                    // Combo c = uiField.input;
                    // String t = uiField.input.getText();
                    // uiField.input.setText(StringUtils.EMPTY);
                    uiField.solution.setText(StringUtils.EMPTY);
                    uiField.input.setVisible(false);
                    Boolean visible = true;
                    String candidate = null;
                    Boolean alreadyVisible = false;
                    if (uiField.candidatesWidgets != null)
                    {
                        for (int ind = 0; ind < getCandidatesNumber(); ind++)
                        {
                            Text cand = uiField.candidatesWidgets.get(ind);
                            if (!keepCandidatesVisibility)
                            {
                                candidate = cand.getText();
                                try
                                {
                                    visible = candidate != null && sVal != null
                                            && sVal.getCandidates().contains((LegalValuesGen) (legalValClassUi
                                                    .getConstructor(String.class).newInstance(candidate)));
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                        | InvocationTargetException | NoSuchMethodException | SecurityException e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                cand.setVisible(visible);
                                if (visible)
                                {
                                    alreadyVisible = visible;
                                }
                                // cand.getParent().setBackground(myDisplay.getSystemColor(SWT.COLOR_BLUE));
                                cand.getParent().setVisible(visible | alreadyVisible);
                            }
                        }
                    }
                }
            }
        }
        // grpSudokuScrolled.setMinSize(uiFields.get(0).get(0).solution.getParent().computeSize(SWT.DEFAULT,
        // SWT.DEFAULT));
        grpSudokuScrolled.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return;
    }

    void updateSudokuFields(boolean keepCandidatesVisibility, boolean runsInUiThread, boolean markLastSolutionFound,
            boolean updateProblemSudoku)
    {
        if (runsInUiThread)
        {
            updateSudokuFieldsInUiThread(keepCandidatesVisibility, markLastSolutionFound, updateProblemSudoku);
        }
        else
        {
            myDisplay.asyncExec(() ->
            {
                updateSudokuFieldsInUiThread(keepCandidatesVisibility, markLastSolutionFound, updateProblemSudoku);
            });
        }
    }

    private void updateSudokuFieldsInUiThread(boolean keepCandidatesVisibility, boolean markLastSolutionFound,
            boolean updateProblemSudoku)
    {
        ((Group) (cellCompositesPtr[0][0].getParent())).setText(mySudoku.getName());
        txtName.setText(mySudoku.getName());
        setStatus(mySudoku.getInputFile());
        setCompositeVisibility(mySudoku.getType());
        List<List<int[]>> conflicts = mySudoku.areContentsLegal();
        if (updateProblemSudoku)
        {
            setState(AppState.CREATING);
            // updateSudokuFields(false, true, false, updateProblemSudoku);
            // app.setSlideShowMode(app.getSlideShowEnabled());
            disableSlideShow();
            initGuiForNew(updateProblemSudoku);
        }
        else
        {
            freeze(keepCandidatesVisibility, true, markLastSolutionFound);
        }
        if (!conflicts.isEmpty())
        {
            for (List<int[]> conflict : conflicts)
            {
                System.out.println("conflict between cell ( row: " + (conflict.get(0)[0] + 1) + ", col: "
                        + (conflict.get(0)[1] + 1) + ") and cell ( row: " + (conflict.get(1)[0] + 1) + ", col: "
                        + (conflict.get(1)[1] + 1) + ")");
            }
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("There are illegal values in the sudoku");
            errorBox.open();
        }
        grpSudokuScrolled.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        setSolveEnabled(status != AppState.CREATING && status != AppState.SOLVING, false);
        saveAsSudokuAction.setEnabled(status != AppState.CREATING);
        condEnableSaveSudokuAction(mySudoku.isSaved() && status != AppState.CREATING);
        renameSudokuAction.setEnabled(status != AppState.CREATING && status != AppState.RENAMING);
        setFreezeEnabled(status == AppState.CREATING);
    }

    /**
     * Configure the shell.
     * 
     * @param newShell
     */
    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Sudoku Stepper");
        // newShell.setMenuBar(menu);
    }

    /**
     * Return the initial size of the window.
     */
    @Override
    protected Point getInitialSize()
    {
        return new Point(INITIAL_WIDTH, INITIAL_HEIGHT);
    }

    // set the given candidate invisible for the given cell
    // @Override
    // public void candidatesUpdated(int row, int col, LegalValuesGenClass val,
    // boolean runsInUiThread)
    // {
    // candidatesUpdatedInternals(row, col, val);
    // }
    //
    // public void candidatesUpdated(int row, int col, LegalValues_16 val, boolean
    // runsInUiThread)
    // {
    // candidatesUpdatedInternals(row, col, val);
    // }
    //
    /**
     * @param row
     * @param col
     * @param val
     */
    public <LegalValuesGen extends LegalValuesGenClass> void candidatesUpdated(int row, int col, LegalValuesGen val)
    {
        myDisplay.asyncExec(new Runnable()
        {
            public void run()
            {
                if (uiFields.get(row).get(col).candidatesWidgets != null)
                {
                    // Better is to look for the text contents and mask the one
                    for (Text candText : uiFields.get(row).get(col).candidatesWidgets)
                    {
                        if (val.toDisplayString().equals(candText.getText()))
                        {
                            candText.setVisible(false);
                        }
                    }
                }
                else
                {
                    String toolTip = uiFields.get(row).get(col).solution.getParent().getToolTipText();
                    String valStr = val.toDisplayString();
                    toolTip = toolTip.replaceFirst("^" + valStr + ",", ",");
                    toolTip = toolTip.replaceFirst(", " + valStr, ",-");
                    uiFields.get(row).get(col).solution.getParent().setToolTipText(toolTip);
                }
            }
        });
    }

    public <LegalValuesGen extends LegalValuesGenClass> void candidatesReset()
    {
        for (int row = 0; row < getMaxRows(); row++)
        {
            for (int col = 0; col < getMaxCols(); col++)
            {
                if (mySudoku.getCell(row, col).getSolution() == null)
                {
                    if (uiFields.get(row).get(col).candidatesWidgets != null)
                    {
                        for (Text candText : uiFields.get(row).get(col).candidatesWidgets)
                        {
                            try
                            {
                                if (mySudoku.getCell(row, col).getCandidates()
                                        .contains((LegalValuesGen) (legalValClassUi.getConstructor(String.class)
                                                .newInstance(candText.getText()))))
                                {
                                    candText.setVisible(true);
                                }
                            }
                            catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                    | InvocationTargetException | NoSuchMethodException | SecurityException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public void rollbackSudoku()
    {
        updateSudokuFields(false, false, true, false);
    }

    public void savedUpdated(boolean saved, boolean runsInUiThread)
    {
        myDisplay.asyncExec(new Runnable()
        {
            public void run()
            {
                condEnableSaveSudokuAction(saved);
                myDisplay.readAndDispatch();
            }
        });
    }

    private void condEnableSaveSudokuAction(boolean saved)
    {
        if (saved || status == AppState.CREATING || mySudoku == null || mySudoku.getInputFile().isEmpty())
        {
            saveSudokuAction.setEnabled(false);
        }
        else
        {
            saveSudokuAction.setEnabled(true);
        }
        setUpdateEnabled(saved || status == AppState.CREATING);
    }

    // To be called only when manually creating a sudoku
    public void inputUpdated()
    {
        for (int row = 0; row < getMaxRows(); row++)
        {
            for (int col = 0; col < getMaxCols(); col++)
            {
                uiFields.get(row).get(col).input.setVisible(true);
                uiFields.get(row).get(col).solution.setVisible(false);
                setSolutionNInputBckgrdColor(row, col, false);
            }
        }
    }

    private <LegalValuesGen extends LegalValuesGenClass> void updateSolution(int row, int col,
            boolean markLastSolutionFound)
    {
        uiFields.get(row).get(col).input.setVisible(false);
        uiFields.get(row).get(col).solution.setVisible(true);
        LegalValuesGenClass solutionVal = mySudoku.getCell(row, col).getSolution();
        uiFields.get(row).get(col).solution.setText(solutionVal.toDisplayString());
        setSolutionNInputBckgrdColor(row, col, markLastSolutionFound);
        // Also update the solution trace (even if not necessary in the case of
        // currently freezing)
        // if (markLastSolutionFound)
        // { // we are only updating the UI so no need to update the trace
        // mySudoku.addToSolutionTrace(mySudoku, row, col, solutionVal, null);
        // }
    }

    public <LegalValuesGen extends LegalValuesGenClass> void solutionUpdated(int row, int col, boolean runsInUiThread,
            boolean markLastSolutionFound)
    {
        if (markLastSolutionFound && !runsInUiThread)
        { // we are only updating the UI so no need to update the trace
            LegalValuesGenClass solutionVal = mySudoku.getCell(row, col).getSolution();
            mySudoku.addToSolutionTrace(mySudoku, row, col, solutionVal, null);
        }
        // myDisplay.asyncExec(new Runnable()
        // {
        // public void run()
        // {
        // uiFields.get(row).get(col).input.setVisible(false);
        // uiFields.get(row).get(col).solution.setVisible(true);
        // uiFields.get(row).get(col).solution
        // .setText(legalValueClass.toDisplayString(mySudoku.getCell(row,
        // col).getSolution().val()));
        // setSolutionNInputBckgrdColor(row, col);
        // }
        // });
        if (runsInUiThread)
        {
            updateSolution(row, col, markLastSolutionFound);
        }
        else
        {
            myDisplay.asyncExec(() ->
            {
                updateSolution(row, col, markLastSolutionFound);
            });
        }

        if (!runsInUiThread && slideShowEnabled)
        {
            try
            {
                if (slideShowPause == null || // step by step
                        stopSlideShow)
                {
                    // End thread
                    synchronized (solvingThread)
                    {
                        if (solvingThread != null)
                        {
                            solvingThread.wait();
                        }
                    }
                }
                else if (slideShowPause > 0)
                {
                    // Thread.sleep(slideShowPause);
                    Thread.sleep(slideShowPause * 1000);
                }
            }
            catch (InterruptedException ex)
            {
                System.out.println(ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n" + ex.toString());
            }
        }
    }

    private void setSolutionNInputBckgrdColor(int row, int col, boolean markLastSolutionFound)
    {
        SingleCellValue<?> sVal = mySudoku.getCell(row, col);
        if (sVal != null)
        {
            if (sVal.isInput())
            {
                uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(COLOR_SOLT_FOREGRD));
            }
            else
            {
                for (Map<Integer, SolNCandTexts> currRow : uiFields.values())
                {
                    for (SolNCandTexts currCell : currRow.values())
                    {
                        if (!currCell.solution.getText().isEmpty() && currCell.solution
                                .getForeground().handle == myDisplay.getSystemColor(COLOR_LAST_FOREGRD).handle)
                        {
                            currCell.solution.setForeground(myDisplay.getSystemColor(COLOR_PREV_FOREGRD));
                        }
                    }
                }
                int color = COLOR_PREV_FOREGRD;
                if (markLastSolutionFound)
                {
                    if (sVal.isTryNError())
                    {
                        color = COLOR_TNERR_FOREGRD;
                    }
                    else
                    {
                        color = COLOR_LAST_FOREGRD;
                    }
                }
                uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(color));
            }
            if (sVal.isAConflict())
            {
                uiFields.get(row).get(col).solution.setBackground(myDisplay.getSystemColor(COLOR_CONFLICT_BCKGRD));
                uiFields.get(row).get(col).input.setBackground(myDisplay.getSystemColor(COLOR_CONFLICT_BCKGRD));
            }
            else
            {
                uiFields.get(row).get(col).solution.setBackground(myDisplay.getSystemColor(COLOR_SOLT_BCKGRD));
                uiFields.get(row).get(col).input.setBackground(myDisplay.getSystemColor(COLOR_INPUT_BCKGRD));
            }
            if (uiFields.get(row).get(col).candidatesWidgets != null)
            {
                for (Text cand : uiFields.get(row).get(col).candidatesWidgets)
                {
                    // cand.setVisible(false);
                    cand.getParent().setVisible(false);
                }
            }
        }
    }

    boolean canDiscardOldSudokuIfAnyExists()
    {
        boolean reallyDo = true;
        if (mySudoku != null && !mySudoku.isSaved())
        {
            MessageBox questionBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
            questionBox.setText("About to switch to another sudoku");
            questionBox.setMessage("The sudoku " + mySudoku.getName()
                    + " has not been saved. Do you want to continue and discard it?");
            int response = questionBox.open();
            switch (response)
            {
            case SWT.YES:
                reallyDo = true;
                break;
            case SWT.NO:
            default:
                reallyDo = false;
                break;
            }
        }
        return reallyDo;
    }

    void toggleSlideShow()
    {
        System.out.println("Slide Show enabled:" + groupSlide.getEnabled());
        System.out.println("ToggleSlideShowSudokuAction.run");
        try
        {
            // app.getSudokuPb().save(null);
            System.out.println("Pressed Slide Show");
            boolean newEnabledState = !getSlideShowEnabled();
            setSlideShowMode(newEnabledState);
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not toggle slide show. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
    }

    void disableSlideShow()
    {
        System.out.println("Slide Show to be disabled");
        try
        {
            setSlideShowMode(false);
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not disable slide show. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
    }

    /**
     * @param newEnabledState
     */
    void setSlideShowMode(boolean newEnabledState)
    {
        groupSlide.setEnabled(newEnabledState);
        slideShowEnabled = groupSlide.getEnabled();
        if (!newEnabledState)
        {
            manualWasEnabled = btnManual.getSelection();
        }
        recursiveSetEnabled(groupSlide, newEnabledState);
        if (firstTimeEnabled)
        {
            btnAutomatic.setSelection(false);
            btnManual.setSelection(true);
            btnManual.notifyListeners(SWT.Selection, new Event());
            firstTimeEnabled = false;
        }
        else if (newEnabledState)
        {
            btnAutomatic.setSelection(!manualWasEnabled);
            btnManual.setSelection(manualWasEnabled);
            if (manualWasEnabled)
            {
                btnManual.notifyListeners(SWT.Selection, new Event());
            }
            else
            {
                btnAutomatic.notifyListeners(SWT.Selection, new Event());
            }
        }
        if (lblCurrent != null && slideShowPause != null)
        {
            lblCurrent.setText(Integer.toString(slideShowPause) + secondUnitStr);
        }
    }

    public void startUpdatingNumOfFields(Class<?> newLegalValClassUi, SudokuType newSudokuType)
    {
        Class<?> oldClassUi = this.getLegalValClassUi();
        if (newLegalValClassUi != oldClassUi)
        {
            Display display = this.getDisplay();
            Shell shell = new Shell(display);
            Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
            shell.setCursor(cursor);
            display.update();
            // singleSudokuWidth = newVal;
            legalValClassUi = newLegalValClassUi;
            // condEnableNewSamuraiAction();
            createSudokuContents(newSudokuType);
            // if (mySudoku != null) // Already included in createSudokuContents
            // {
            // setCompositeVisibility(mySudoku.getType());
            // }
            cursor.dispose();
        }
        if (mySudoku != null)
        {
            mySudoku.getSudoku().setSudokuType(newSudokuType);
        }
    }

    /**
     * @param newVal
     */
    // void condEnableNewSamuraiAction()
    // {
    // if (legalValClassUi == LegalValues_16.class)
    // {
    // newSudokuSamuraiAction.setEnabled(false);
    // }
    // else // LegalValues.class
    // {
    // newSudokuSamuraiAction.setEnabled(true);
    // }
    // }

    // public SubAreaWidth getSubAreaWidth()
    // {
    // return singleSudokuWidth;
    // }

    public Class<?> getLegalValClassUi()
    {
        return legalValClassUi;

    }

    private static int getIntByReflection(String methodName)
    {
        int retVal = 0;
        try
        {
            retVal = (int) legalValClassUi.getMethod(methodName, (Class<?>[]) null).invoke(null, (Object[]) null);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (retVal);
    }
}

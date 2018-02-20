package SudokuStepper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class AppMain extends ApplicationWindow implements SolutionListener, CandidatesListener, SavedListener
{
    private Action           action;
    private Values           mySudoku              = null;
    // remember the last candidate whose status was changed
    private Text             lastUpdatedCandText   = null;
    private Font             solutionFont          = null;                 // SWTResourceManager.getFont("Segoe UI", 30,
                                                                           // SWT.BOLD);
    private Font             solutionSmallFont     = null;                 // SWTResourceManager.getFont("Segoe UI", 8,
                                                                           // SWT.NORMAL);

    private static final int INITIAL_WIDTH         = 552;
    private static final int INITIAL_HEIGHT        = 820;
    private static final int NAME_BOX_HEIGHT       = 55;
    private static final int TOP_MARGIN            = 5;
    private static final int COLOR_INPUT_BCKGRD    = SWT.COLOR_WHITE;
    private static final int COLOR_INPUT_FOREGRD   = SWT.COLOR_BLACK;
    private static final int COLOR_CONFLICT_BCKGRD = SWT.COLOR_RED;
    private static final int COLOR_SOLT_BCKGRD     = SWT.COLOR_DARK_YELLOW;
    private static final int COLOR_SOLT_FOREGRD    = SWT.COLOR_BLACK;

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

    /**
     * Create the application window.
     */
    public AppMain()
    {
        super(null);
        // addToolBar(SWT.NONE);
        renameSudokuAction = new RenameSudokuAction(this);
        newSudokuAction = new NewSudokuAction(this);
        openSudokuAction = new OpenSudokuAction(this);
        saveSudokuAction = new SaveSudokuAction(this);
        saveAsSudokuAction = new SaveAsSudokuAction(this);
        solveSudokuAction = new SolveSudokuAction(this);
        exitSudokuAction = new ExitSudokuAction(this);
        freezeSudokuAction = new FreezeSudokuAction(this);
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

    private AppState status = AppState.EMPTY;

    public void setState(AppState val)
    {
        status = val;
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

    public Values getSudokuPb()
    {
        return (mySudoku);
    }

    public void setSudokuPb(Values newSudoku)
    {
        mySudoku = newSudoku;
        if (mySudoku != null)
        {
            mySudoku.addCandidatesListener(this);
            mySudoku.addSolutionListener(this);
            mySudoku.addSavedListener(this);
        }
    }

    private class SolNCandTexts
    {
        public Text       solution;
        public Combo      input;
        public List<Text> candidates = new ArrayList<Text>(RECTLENGTH * RECTLENGTH);
    }

    static final int                                  RECTLENGTH      = 3;
    // first key is the row (1...9), second key is the column(1...9)
    private Map<Integer, Map<Integer, SolNCandTexts>> uiFields        = new HashMap<Integer, Map<Integer, SolNCandTexts>>(
            RECTLENGTH);
    private Group                                     grpSudokuBlocks = null;
    private Group                                     grpSudokuName   = null;
    private Text                                      txtName         = null;

    /**
     * Create contents of the application window.
     * 
     * @param parent
     */
    // @Override
    protected Control createContents(Composite parent)
    {
        System.out.println("createContents");
        setStatus(StringUtils.EMPTY);
        Composite OverallContainer = new Composite(parent, SWT.NONE);
        OverallContainer.setLayout(new FormLayout());

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
                mySudoku.setName(input);
                grpSudokuBlocks.setText(input);
            }
        });

        grpSudokuBlocks = new Group(OverallContainer, SWT.BORDER | SWT.SHADOW_OUT);
        FormData fd_grpSudokublocks = new FormData();
        fd_grpSudokublocks.right = new FormAttachment(100, -3);
        fd_grpSudokublocks.top = new FormAttachment(0, TOP_MARGIN);
        fd_grpSudokublocks.left = new FormAttachment(0, 3);
        grpSudokuBlocks.setLayoutData(fd_grpSudokublocks);
        grpSudokuBlocks.setText(StringUtils.EMPTY);
        grpSudokuBlocks.setLayout(new GridLayout(RECTLENGTH, true));
        for (int blockRow = 1; blockRow <= RECTLENGTH; blockRow++)
        {
            for (int blockCol = 1; blockCol <= RECTLENGTH; blockCol++)
            {
                Composite cellComposite = new Composite(grpSudokuBlocks, SWT.NONE);
                cellComposite.setBackground(myDisplay.getSystemColor(SWT.COLOR_GREEN));
                cellComposite.setLayout(new GridLayout(RECTLENGTH, false));
                for (int row = 1; row <= RECTLENGTH; row++)
                {
                    for (int col = 1; col <= RECTLENGTH; col++)
                    {

                        Composite composite_111 = new Composite(cellComposite, SWT.NONE);
                        StackLayout gl_composite_111 = new StackLayout();

                        composite_111.setLayout(gl_composite_111);
                        // Create text for the display of solutions
                        Text solutionText = new Text(composite_111, SWT.BORDER | SWT.CENTER);
                        solutionText.setBackground(myDisplay.getSystemColor(COLOR_SOLT_BCKGRD));
                        solutionText.setFont(solutionFont);
                        solutionText.setText(Integer.toString(((row - 1) * RECTLENGTH + col)));
                        solutionText.setToolTipText("Input or solution");
                        gl_composite_111.topControl = solutionText;
                        int totalRow = (blockRow - 1) * RECTLENGTH + row - 1;
                        int totalCol = (blockCol - 1) * RECTLENGTH + col - 1;
                        if (!uiFields.containsKey(totalRow))
                        {
                            uiFields.put(totalRow, new HashMap<Integer, SolNCandTexts>(RECTLENGTH));
                        }
                        if (!uiFields.get(totalRow).containsKey(totalCol))
                        {
                            uiFields.get(totalRow).put(totalCol, new SolNCandTexts());
                        }
                        uiFields.get(totalRow).get(totalCol).solution = solutionText;
                        // Create combo box for input of a new Sudoku
                        Combo combo = new Combo(composite_111, SWT.DROP_DOWN);

                        String[] items = new String[LegalValues.values().length];
                        // You need to set a list of items to avoid an exception
                        int valInd = 0;
                        for (LegalValues val : LegalValues.values())
                        {
                            items[valInd] = Integer.toString(val.val());
                            valInd++;
                        }
                        combo.setItems(items);
                        combo.setFont(solutionFont);
                        combo.setToolTipText("Choose the initial value for this cell if any");
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
                                        if (!found)
                                        {
                                            setStatus(input + " is an invalid value for this cell!");
                                            combo.setText(StringUtils.EMPTY);
                                        }
                                        else
                                        {
                                            setStatus(StringUtils.EMPTY);
                                            LegalValues val = LegalValues.from(Integer.parseInt(input));
                                            mySudoku.updateCandidateList(totalRow, totalCol, val);
                                            mySudoku.getCell(totalRow, totalCol).candidates.clear();
                                            mySudoku.getCell(totalRow, totalCol).solution = val;
                                            mySudoku.getCell(totalRow, totalCol).isInput = true;
                                            mySudoku.setSaved(false);

                                        }
                                    }
                                    else
                                    {
                                        mySudoku.resetCell(totalRow, totalCol);
                                    }
                                    List<List<int[]>> conflicts = mySudoku.areContentsLegal();
                                    inputUpdated();
                                    boolean freezeAllowed = conflicts.size() == 0
                                            && mySudoku.getNumberOfSolutions() > 0;
                                    btnFreeze.setEnabled(freezeAllowed);
                                    freezeSudokuAction.setEnabled(freezeAllowed);
                                }

                            }
                        });
                        combo.setBackground(myDisplay.getSystemColor(COLOR_INPUT_BCKGRD));
                        uiFields.get(totalRow).get(totalCol).input = combo;

                        Composite composite_1110 = new Composite(composite_111, SWT.NONE);
                        composite_1110.setBackground(myDisplay.getSystemColor(SWT.COLOR_BLUE));
                        GridLayout gl_composite_1110 = new GridLayout(RECTLENGTH, false);
                        gl_composite_1110.horizontalSpacing = 2;
                        gl_composite_1110.verticalSpacing = 1;
                        gl_composite_1110.marginWidth = 0;
                        gl_composite_1110.marginHeight = 0;
                        composite_1110.setLayout(gl_composite_1110);
                        gl_composite_111.topControl = composite_1110;
                        for (int rowSub = 1; rowSub <= RECTLENGTH; rowSub++)
                        {
                            for (int colSub = 1; colSub <= RECTLENGTH; colSub++)
                            {

                                Text candidateText = new Text(composite_1110, SWT.BORDER | SWT.READ_ONLY);
                                candidateText.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
                                candidateText.setBackground(myDisplay.getSystemColor(SWT.COLOR_DARK_GRAY));
                                candidateText.setFont(solutionSmallFont);
                                candidateText.setText(Integer.toString(((rowSub - 1) * RECTLENGTH + colSub)));
                                // solutionText.setToolTipText("01234");
                                uiFields.get(totalRow).get(totalCol).candidates.add(candidateText);
                            }
                        }
                    }
                }
            }
        }
        Group grpButtons = new Group(OverallContainer, SWT.NONE);
        fd_grpSudokublocks.bottom = new FormAttachment(grpButtons, -6);
        FormData fd_grpButtons = new FormData();
        fd_grpButtons.bottom = new FormAttachment(100);
        fd_grpButtons.left = new FormAttachment(0);
        fd_grpButtons.right = new FormAttachment(100);
        // fd_grpButtons.right = new FormAttachment(0, 3);
        grpButtons.setLayoutData(fd_grpButtons);
        grpButtons.setText("Buttons");
        RowLayout rl_grpButtons = new RowLayout(SWT.HORIZONTAL);
        rl_grpButtons.wrap = false;
        rl_grpButtons.pack = false;
        rl_grpButtons.justify = true;
        rl_grpButtons.fill = true;
        grpButtons.setLayout(rl_grpButtons);
        AppMain app = this;
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
        btnFreeze.setEnabled(false);
        btnSolve = new Button(grpButtons, SWT.NONE);
        btnSolve.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Solve");
                solveSudokuAction.run();
            }
        });
        btnSolve.setText("Solve");
        btnSolve.setEnabled(false);
        Button btnSlideShow = new Button(grpButtons, SWT.NONE);
        btnSlideShow.setText("Slide Show");
        btnSlideShow.setEnabled(false);

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

    class fileExitItemListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent event)
        {
            // shell.close();
            // display.dispose();
        }

        public void widgetDefaultSelected(SelectionEvent event)
        {
            // shell.close();
            // display.dispose();
        }
    }

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

    private Action renameSudokuAction = null;
    private Action newSudokuAction    = null;
    private Action openSudokuAction   = null;
    private Action saveSudokuAction   = null;
    private Action saveAsSudokuAction = null;
    private Action solveSudokuAction  = null;
    private Action exitSudokuAction   = null;
    private Action freezeSudokuAction = null;
    private Button btnSolve           = null;
    private Button btnFreeze          = null;

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

        MenuManager helpMenuMgr = new MenuManager("Help");
        helpMenuMgr.setVisible(true);
        menuMgr.add(helpMenuMgr);
        MenuManager helpAboutMgr = new MenuManager("About");
        helpAboutMgr.setVisible(true);
        helpMenuMgr.add(helpAboutMgr);

        MenuManager fileMenuMgr = new MenuManager("File");
        fileMenuMgr.setVisible(true);
        menuMgr.add(fileMenuMgr);
        MenuManager fileNewMgr = new MenuManager("New");
        fileNewMgr.setVisible(true);
        fileMenuMgr.add(fileNewMgr);
        fileMenuMgr.add(newSudokuAction);
        MenuManager fileOpenMgr = new MenuManager("Open");
        fileOpenMgr.setVisible(true);
        fileMenuMgr.add(fileOpenMgr);
        fileMenuMgr.add(openSudokuAction);
        MenuManager fileSaveMgr = new MenuManager("Save");
        fileSaveMgr.setVisible(true);
        fileMenuMgr.add(fileSaveMgr);
        fileMenuMgr.add(saveSudokuAction);
        saveSudokuAction.setEnabled(false);
        MenuManager fileSaveAsMgr = new MenuManager("Save As");
        fileSaveAsMgr.setVisible(true);
        fileMenuMgr.add(fileSaveAsMgr);
        fileMenuMgr.add(saveAsSudokuAction);
        saveAsSudokuAction.setEnabled(false);
        MenuManager fileExitMgr = new MenuManager("Exit");
        fileExitMgr.setVisible(true);
        fileMenuMgr.add(fileExitMgr);
        fileMenuMgr.add(exitSudokuAction);
        exitSudokuAction.setEnabled(true);

        MenuManager actionMenuMgr = new MenuManager("Action");
        actionMenuMgr.setVisible(true);
        menuMgr.add(actionMenuMgr);
        MenuManager freezeSolveMgr = new MenuManager("Freeze");
        freezeSolveMgr.setVisible(true);
        actionMenuMgr.add(freezeSolveMgr);
        actionMenuMgr.add(freezeSudokuAction);
        freezeSudokuAction.setEnabled(false);

        MenuManager actionSolveMgr = new MenuManager("Solve");
        actionSolveMgr.setVisible(true);
        actionMenuMgr.add(actionSolveMgr);
        actionMenuMgr.add(solveSudokuAction);
        solveSudokuAction.setEnabled(false);

        MenuManager actionRenameMgr = new MenuManager("Rename");
        actionRenameMgr.setVisible(true);
        actionMenuMgr.add(actionRenameMgr);
        actionMenuMgr.add(renameSudokuAction);
        renameSudokuAction.setEnabled(false);

        return menuMgr;
    }

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

    void initGuiForNew()
    {
        // It is important to first relayout and then set the uiFields
        freezeSudokuAction.setEnabled(false);
        solveSudokuAction.setEnabled(false);
        btnFreeze.setEnabled(false);
        btnSolve.setEnabled(false);
        grpSudokuName.setVisible(true);
        ((FormData) (grpSudokuBlocks.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN + NAME_BOX_HEIGHT);
        txtName.setText(StringUtils.EMPTY);
        txtName.setEditable(true);
        grpSudokuBlocks.getParent().layout(true, true);
        grpSudokuBlocks.redraw();
        grpSudokuBlocks.update();
        for (Integer row : uiFields.keySet())
        {
            for (Integer col : uiFields.get(row).keySet())
            {
                SolNCandTexts uiField = uiFields.get(row).get(col);
                setSolutionNInputBckgrdColor(row, col);
                uiField.solution.setVisible(false);
                // uiField.input.setText(StringUtils.EMPTY);
                uiField.input.setVisible(true);
                for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                {
                    Text cand = uiField.candidates.get(ind);
                    cand.setVisible(false);
                    cand.getParent().setVisible(false);
                }
            }
        }
    }

    void freeze()
    {
        // It is important to first relayout and then set the uiFields
        freezeSudokuAction.setEnabled(false);
        btnFreeze.setEnabled(false);
        solveSudokuAction.setEnabled(true);
        btnSolve.setEnabled(true);
        grpSudokuName.setVisible(false);
        ((FormData) (grpSudokuBlocks.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN);
        txtName.setEditable(false);
        grpSudokuBlocks.getParent().layout(true, true);
        grpSudokuBlocks.redraw();
        grpSudokuBlocks.update();
        for (Integer row : uiFields.keySet())
        {
            for (Integer col : uiFields.get(row).keySet())
            {
                SolNCandTexts uiField = uiFields.get(row).get(col);
                if (mySudoku.getCell(row, col).candidates.isEmpty())
                {
                    solutionUpdated(row, col);
                }
                else
                {
                    uiField.solution.setVisible(false);
                    uiField.input.setText(StringUtils.EMPTY);
                    uiField.input.setVisible(false);
                    Boolean visible = true;
                    for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                    {
                        Text cand = uiField.candidates.get(ind);
                        // String candidate = cand.getText();
                        // Boolean visible = candidate != null && mySudoku.getCell(row, col).candidates
                        // .contains(LegalValues.from(Integer.parseInt(candidate)));
                        cand.setVisible(visible);
                        cand.getParent().setVisible(visible);
                    }
                }
            }
        }
    }

    void updateSudokuFields()
    {
        grpSudokuBlocks.setText(mySudoku.getName());
        txtName.setText(mySudoku.getName());
        setStatus(mySudoku.getInputFile());
        List<List<int[]>> conflicts = mySudoku.areContentsLegal();
        freeze();
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
        solveSudokuAction.setEnabled(true);
        // freezeSudokuAction.setEnabled(false);
        btnSolve.setEnabled(true);
        saveAsSudokuAction.setEnabled(status != AppState.CREATING);
        condEnableSaveSudokuAction(mySudoku.isSaved());
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

    @Override
    public void candidatesUpdated(int row, int col, LegalValues val)
    {
        // Better is to look for the text contents and mask the one
        for (Text candText : uiFields.get(row).get(col).candidates)
        {
            if (Integer.toString(val.val()).equals(candText.getText()))
            {
                candText.setVisible(false);
            }
        }
    }

    public void savedUpdated(boolean saved)
    {
        condEnableSaveSudokuAction(saved);
        myDisplay.readAndDispatch();
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
    }

    // To be called only when manually creating a sudoku
    public void inputUpdated()
    {
        for (int row = 0; row < RECTLENGTH * RECTLENGTH; row++)
        {
            for (int col = 0; col < RECTLENGTH * RECTLENGTH; col++)
            {
                uiFields.get(row).get(col).input.setVisible(true);
                uiFields.get(row).get(col).solution.setVisible(false);
                setSolutionNInputBckgrdColor(row, col);
            }
        }
    }

    public void solutionUpdated(int row, int col)
    {
        uiFields.get(row).get(col).input.setVisible(false);
        uiFields.get(row).get(col).solution.setVisible(true);
        uiFields.get(row).get(col).solution.setText(Integer.toString(mySudoku.getCell(row, col).solution.val()));
        setSolutionNInputBckgrdColor(row, col);
    }

    private void setSolutionNInputBckgrdColor(int row, int col)
    {
        if (mySudoku.getCell(row, col).isInput)
        {
            uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(COLOR_SOLT_FOREGRD));
        }
        else
        {
            uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(SWT.COLOR_WHITE));
        }
        if (mySudoku.getCell(row, col).isAConflict)
        {
            uiFields.get(row).get(col).solution.setBackground(myDisplay.getSystemColor(COLOR_CONFLICT_BCKGRD));
            uiFields.get(row).get(col).input.setBackground(myDisplay.getSystemColor(COLOR_CONFLICT_BCKGRD));
        }
        else
        {
            uiFields.get(row).get(col).solution.setBackground(myDisplay.getSystemColor(COLOR_SOLT_BCKGRD));
            uiFields.get(row).get(col).input.setBackground(myDisplay.getSystemColor(COLOR_INPUT_BCKGRD));
        }
        for (Text cand : uiFields.get(row).get(col).candidates)
        {
            cand.setVisible(false);
            cand.getParent().setVisible(false);
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
}

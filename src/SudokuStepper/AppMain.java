package SudokuStepper;

import java.awt.event.KeyEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ibm.icu.impl.duration.TimeUnit;

public class AppMain extends ApplicationWindow
        implements SolutionListener, CandidatesListener, CandidatesResetListener, SavedListener
{
    private Action           action;
    private Values           mySudoku              = null;
    // remember the last candidate whose status was changed
    private Text             lastUpdatedCandText   = null;
    private Font             solutionFont          = null;                 // SWTResourceManager.getFont("Segoe UI",
                                                                           // 30,
                                                                           // SWT.BOLD);
    // null means: image by image, 0 means no pause
    private Integer          slideShowPause        = null;
    // private Integer previousSlideShowPause = null;
    private Font             solutionSmallFont     = null;                 // SWTResourceManager.getFont("Segoe UI", 8,
                                                                           // SWT.NORMAL);
    static final int         RECTLENGTH            = 3;

    private static final int INITIAL_WIDTH         = 552;
    private static final int INITIAL_HEIGHT        = 915;
    private static final int NAME_BOX_HEIGHT       = 55;
    private static final int TOP_MARGIN            = 5;
    private static final int COLOR_INPUT_BCKGRD    = SWT.COLOR_WHITE;
    private static final int COLOR_INPUT_FOREGRD   = SWT.COLOR_BLACK;
    private static final int COLOR_CONFLICT_BCKGRD = SWT.COLOR_RED;
    private static final int COLOR_SOLT_BCKGRD     = SWT.COLOR_DARK_YELLOW;
    private static final int COLOR_SOLT_FOREGRD    = SWT.COLOR_BLACK;
    private static final int COLOR_LAST_FOREGRD    = SWT.COLOR_RED;
    private static final int COLOR_PREV_FOREGRD    = SWT.COLOR_WHITE;

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

    // first key is the row (1...9), second key is the column(1...9)
    private Map<Integer, Map<Integer, SolNCandTexts>> uiFields = new HashMap<Integer, Map<Integer, SolNCandTexts>>(
            RECTLENGTH);

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
        if (val == AppState.SOLVING || mySudoku == null)
        {
            setSolveEnabled(false);
        }
        else if (val == AppState.EMPTY && mySudoku != null)
        {
            setSolveEnabled(true);
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

    public Values getSudokuPb()
    {
        return (mySudoku);
    }

    public void setSudokuPb(Values newSudoku)
    {
        mySudoku = newSudoku;
        if (mySudoku != null)
        {
            for (int row = 0; row < RECTLENGTH * RECTLENGTH; row++)
            {
                for (int col = 0; col < RECTLENGTH * RECTLENGTH; col++)
                {
                    SolNCandTexts uiField = uiFields.get(row).get(col);
                    LegalValues value = mySudoku.getCell(row, col).getSolution();
                    if (value != null)
                    {
                        uiField.solution.setText(Integer.toString(value.val()));
                    }
                    else
                    {
                        uiField.solution.setText(StringUtils.EMPTY);
                    }
                    uiField.input.setText(StringUtils.EMPTY);
                    for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                    { // Eliminate single settings from other sudokus
                        uiField.candidates.get(ind).setVisible(true);
                    }
                    // uiField.candidates.get(0).getParent().setVisible(true);;
                }
            }
            mySudoku.addCandidatesListener(this);
            mySudoku.addCandidatesResetListener(this);
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
                                            mySudoku.updateCandidateList(totalRow, totalCol, val, true, false);
                                            mySudoku.getCell(totalRow, totalCol).candidates.clear();
                                            mySudoku.getCell(totalRow, totalCol).setSolution(val, totalRow, totalCol,
                                                    null, true, false);
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
                                    setFreezeEnabled(freezeAllowed);
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
        fd_grpSudokublocks.bottom = new FormAttachment(grpButtons, -88);
        FormData fd_grpButtons = new FormData();
        fd_grpButtons.top = new FormAttachment(100, -72);
        fd_grpButtons.bottom = new FormAttachment(100, -26);
        fd_grpButtons.left = new FormAttachment(0);
        fd_grpButtons.right = new FormAttachment(100);
        // fd_grpButtons.right = new FormAttachment(0, 3);
        grpButtons.setLayoutData(fd_grpButtons);
        // grpButtons.setText("Buttons");
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
                solveSudokuAction.run();
            }
        });
        btnSolve.setText("Solve");
        setSolveEnabled(false);

        groupSlide = new Group(OverallContainer, SWT.NONE);
        // groupSlide.setText("Slide");
        // groupSlide.setBackground(SWTResourceManager.getColor(128, 128, 128));
        groupSlide.setLayout(new FormLayout());
        FormData fd_groupSlide = new FormData();
        fd_groupSlide.bottom = new FormAttachment(grpButtons, -6);
        fd_groupSlide.top = new FormAttachment(grpSudokuBlocks, 9);
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

        Button btnNext = new Button(groupSlide, SWT.NONE);
        btnNext.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                System.out.println("Pressed Next");
                solveSudokuAction.run();
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

        Slider slider = new Slider(groupSlide, SWT.NONE);
        FormData fd_slider = new FormData();
        fd_slider.left = new FormAttachment(lblPause, 5, SWT.RIGHT);
        fd_slider.bottom = new FormAttachment(btnAutomatic, 0, SWT.BOTTOM);
        fd_slider.right = new FormAttachment(100, -35);
        slider.setLayoutData(fd_slider);
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
        lblNoPause.setText(Integer.toString(minSecondsPause) + " s");
        FormData fd_lblNoPause = new FormData();
        fd_lblNoPause.bottom = new FormAttachment(btnManual, 0, SWT.BOTTOM);
        fd_lblNoPause.left = new FormAttachment(slider, 0, SWT.LEFT);
        lblNoPause.setLayoutData(fd_lblNoPause);

        Label lblMinute = new Label(groupSlide, SWT.NONE);
        lblMinute.setText(Integer.toString(maxSecondsPause) + " s");
        FormData fd_ldlMinute = new FormData();
        fd_ldlMinute.bottom = new FormAttachment(btnManual, 0, SWT.BOTTOM);
        fd_ldlMinute.right = new FormAttachment(slider, 0, SWT.RIGHT);
        lblMinute.setLayoutData(fd_ldlMinute);

        lblCurrent = new Label(groupSlide, SWT.NONE);
        FormData fd_lblCurrent = new FormData();
        fd_lblCurrent.bottom = new FormAttachment(btnAutomatic, 2, SWT.BOTTOM);
        fd_lblCurrent.right = new FormAttachment(100, -5);
        lblCurrent.setLayoutData(fd_lblCurrent);
        // groupSlideLabels.setBackground(SWTResourceManager.getColor(56, 56, 0));
        lblCurrent.setText(lblMinute.getText());

        slider.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                slideShowPause = slider.getSelection();
                // System.out.println("Selected: " + slideShowPause);
                lblCurrent.setText(Integer.toString(slideShowPause) + " s");
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0)
            { // Obviously never called
                slideShowPause = slider.getSelection();
                // System.out.println("DefaultSelected: " + slideShowPause);
                lblCurrent.setText(Integer.toString(slideShowPause) + " s");
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
        // lblNoPause.setText(Integer.toString(minSecondsPause) + " s");
        // GridData gd_lblNoPause = new GridData(SWT.LEFT, SWT.CENTER);
        // lblNoPause.setLayoutData(gd_lblNoPause);
        //
        // Label lblCurrent = new Label(groupSlideLabels, SWT.NONE);
        // lblCurrent.setText(Integer.toString(slider.getSelection()) + " s");
        // GridData gd_lblCurrent = new GridData(SWT.CENTER, SWT.CENTER);
        // lblCurrent.setLayoutData(gd_lblCurrent);
        //
        // Label lblMinute = new Label(groupSlideLabels, SWT.NONE);
        // lblMinute.setText(Integer.toString(maxSecondsPause) + " s");
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
        // lblCurrent.setText(Integer.toString(slideShowPause) + " s");
        // }
        //
        // @Override
        // public void widgetDefaultSelected(SelectionEvent arg0)
        // { // Obviously never called
        // // System.out.println("DefaultSelected: " + slider.getSelection());
        // slideShowPause = slider.getSelection();
        // lblCurrent.setText(Integer.toString(slideShowPause) + " s");
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
                    slider.setEnabled(false);
                    btnNext.setEnabled(true);
                    slideShowPause = null;
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
                    slider.setEnabled(true);
                    btnNext.setEnabled(false);
                    slideShowPause = slider.getSelection();
                    lblCurrent.setText(Integer.toString(slideShowPause) + " s");
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

    public void setSolveEnabled(boolean enabled)
    {
        if (btnSolve != null)
        {
            btnSolve.setEnabled(enabled);
        }
        if (btnSlideShow != null)
        {
            btnSlideShow.setEnabled(enabled);
        }
        if (solveSudokuAction != null)
        {
            solveSudokuAction.setEnabled(enabled);
        }
        if (toggleSlideShowSudokuAction != null)
        {
            toggleSlideShowSudokuAction.setEnabled(enabled);
        }
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

    private Action renameSudokuAction          = new RenameSudokuAction(this, "&Rename", SWT.CTRL + KeyEvent.VK_R);;
    private Action newSudokuAction             = new NewSudokuAction(this, "&New", SWT.CTRL + KeyEvent.VK_N);
    private Action openSudokuAction            = new OpenSudokuAction(this, "&Open", SWT.CTRL + KeyEvent.VK_O);
    private Action saveSudokuAction            = new SaveSudokuAction(this, "&Save", SWT.CTRL + KeyEvent.VK_S);
    private Action saveAsSudokuAction          = new SaveAsSudokuAction(this, "Save &As", SWT.CTRL + KeyEvent.VK_A);
    private Action toggleSlideShowSudokuAction = new ToggleSlideShowSudokuAction(this, "S&lide Show On/Off",
            KeyEvent.VK_L);
    private Action solveSudokuAction           = new SolveSudokuAction(this, "&Solve", KeyEvent.VK_S);
    private Action exitSudokuAction            = new ExitSudokuAction(this, "&Exit", SWT.CTRL + KeyEvent.VK_E);
    private Action freezeSudokuAction          = new FreezeSudokuAction(this, "&Freeze", SWT.CTRL + KeyEvent.VK_F);
    private Action aboutSudokuAction           = new AboutSudokuAction(this, "&About", KeyEvent.VK_A);
    private Button btnSolve                    = null;
    private Button btnFreeze                   = null;
    private Button btnSlideShow                = null;
    private Button btnAutomatic                = null;
    private Button btnManual                   = null;
    private Group  groupSlide                  = null;
    private Group  grpSudokuBlocks             = null;
    private Group  grpSudokuName               = null;
    private Text   txtName                     = null;
    private Label  lblCurrent                  = null;

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
        // MenuManager fileNewMgr = new MenuManager("New1");
        // fileNewMgr.setVisible(true);
        // fileMenuMgr.add(fileNewMgr);
        fileMenuMgr.add(newSudokuAction);
        // MenuManager fileOpenMgr = new MenuManager("Open1");
        // fileOpenMgr.setVisible(true);
        // fileMenuMgr.add(fileOpenMgr);
        fileMenuMgr.add(openSudokuAction);
        // MenuManager fileSaveMgr = new MenuManager("Save1");
        // fileSaveMgr.setVisible(true);
        // fileMenuMgr.add(fileSaveMgr);
        fileMenuMgr.add(saveSudokuAction);
        saveSudokuAction.setEnabled(false);
        // MenuManager fileSaveAsMgr = new MenuManager("Save As1");
        // fileSaveAsMgr.setVisible(true);
        // fileMenuMgr.add(fileSaveAsMgr);
        fileMenuMgr.add(saveAsSudokuAction);
        saveAsSudokuAction.setEnabled(false);
        // MenuManager fileExitMgr = new MenuManager("Exit1");
        // fileExitMgr.setVisible(true);
        // fileMenuMgr.add(fileExitMgr);
        fileMenuMgr.add(exitSudokuAction);
        exitSudokuAction.setEnabled(true);

        MenuManager actionMenuMgr = new MenuManager("Action");
        actionMenuMgr.setVisible(true);
        menuMgr.add(actionMenuMgr);
        // MenuManager freezeSolveMgr = new MenuManager("Freeze1");
        // freezeSolveMgr.setVisible(true);
        // actionMenuMgr.add(freezeSolveMgr);
        actionMenuMgr.add(freezeSudokuAction);
        setFreezeEnabled(false);

        actionMenuMgr.add(toggleSlideShowSudokuAction);
        toggleSlideShowSudokuAction.setEnabled(false);
        // MenuManager actionSolveMgr = new MenuManager("Solve1");
        // actionSolveMgr.setVisible(true);
        // actionMenuMgr.add(actionSolveMgr);
        actionMenuMgr.add(solveSudokuAction);
        setSolveEnabled(false);

        // MenuManager actionRenameMgr = new MenuManager("Rename1");
        // actionRenameMgr.setVisible(true);
        // actionMenuMgr.add(actionRenameMgr);
        actionMenuMgr.add(renameSudokuAction);
        renameSudokuAction.setEnabled(false);

        MenuManager helpMenuMgr = new MenuManager("Help");
        helpMenuMgr.setVisible(true);
        menuMgr.add(helpMenuMgr);
        // MenuManager helpAboutMgr = new MenuManager("About1");
        // helpAboutMgr.setVisible(true);
        // helpMenuMgr.add(helpAboutMgr);
        helpMenuMgr.add(aboutSudokuAction);

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

    void startRenamingGui()
    {
        grpSudokuName.setVisible(true);
        ((FormData) (grpSudokuBlocks.getLayoutData())).top = new FormAttachment(0, TOP_MARGIN + NAME_BOX_HEIGHT);
        if (mySudoku != null && mySudoku.getName() != null)
        {
            txtName.setText(mySudoku.getName());
        }
        else
        {
            txtName.setText(StringUtils.EMPTY);
        }
        txtName.setEditable(true);
        grpSudokuBlocks.getParent().layout(true, true);
        grpSudokuBlocks.redraw();
        grpSudokuBlocks.update();
        // grpSudokuBlocks.setEnabled(false);
        setSolveEnabled(false);
        setFreezeEnabled(true);
        renameSudokuAction.setEnabled(false);

        // must reset visible all solution fields because they got hidden by the line:
        // grpSudokuBlocks.getParent().layout(true, true);
        for (int row = 0; row < RECTLENGTH * RECTLENGTH; row++)
        {
            for (int col = 0; col < RECTLENGTH * RECTLENGTH; col++)
            {
                if (!uiFields.get(row).get(col).solution.getText().isEmpty())
                {
                    uiFields.get(row).get(col).solution.setVisible(true);
                }
            }
        }
    }

    void initGuiForNew()
    {
        // It is important to first relayout and then set the uiFields
        setFreezeEnabled(false);
        setSolveEnabled(false);
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
                setSolutionNInputBckgrdColor(row, col, false);
                uiField.solution.setVisible(false);
                // uiField.input.setText(StringUtils.EMPTY);
                uiField.input.setVisible(true);
                for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                {
                    Text cand = uiField.candidates.get(ind);
                    // cand.setVisible(false);
                    cand.getParent().setVisible(false);
                }
            }
        }
    }

    void freeze(boolean keepCandidatesVisibility, boolean runsInUiThread, boolean markLastSolutionFound)
    {
        // It is important to first relayout and then set the uiFields
        setFreezeEnabled(false);
        setSolveEnabled(true);
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
                    solutionUpdated(row, col, runsInUiThread, markLastSolutionFound);
                }
                else
                {
                    uiField.solution.setVisible(false);
                    uiField.input.setText(StringUtils.EMPTY);
                    uiField.solution.setText(StringUtils.EMPTY);
                    uiField.input.setVisible(false);
                    Boolean visible = true;
                    for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                    {
                        Text cand = uiField.candidates.get(ind);
                        // String candidate = cand.getText();
                        // Boolean visible = candidate != null && mySudoku.getCell(row, col).candidates
                        // .contains(LegalValues.from(Integer.parseInt(candidate)));
                        if (!keepCandidatesVisibility)
                        {
                            cand.setVisible(visible);
                            cand.getParent().setVisible(visible);
                        }
                    }
                }
            }
        }
        return;
    }

    void updateSudokuFields(boolean keepCandidatesVisibility, boolean runsInUiThread, boolean markLastSolutionFound)
    {
        if (runsInUiThread)
        {
            updateSudokuFieldsInUiThread(keepCandidatesVisibility, markLastSolutionFound);
        }
        else
        {
            myDisplay.asyncExec(() ->
            {
                updateSudokuFieldsInUiThread(keepCandidatesVisibility, markLastSolutionFound);
            });
        }
    }

    private void updateSudokuFieldsInUiThread(boolean keepCandidatesVisibility, boolean markLastSolutionFound)
    {
        grpSudokuBlocks.setText(mySudoku.getName());
        txtName.setText(mySudoku.getName());
        setStatus(mySudoku.getInputFile());
        List<List<int[]>> conflicts = mySudoku.areContentsLegal();
        freeze(keepCandidatesVisibility, true, markLastSolutionFound);
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
        setSolveEnabled(true);
        saveAsSudokuAction.setEnabled(status != AppState.CREATING);
        condEnableSaveSudokuAction(mySudoku.isSaved());
        renameSudokuAction.setEnabled(status != AppState.CREATING && status != AppState.RENAMING);
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
    @Override
    public void candidatesUpdated(int row, int col, LegalValues val, boolean runsInUiThread)
    {
        myDisplay.asyncExec(new Runnable()
        {
            public void run()
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
        });
    }

    public void candidatesReset()
    {
        for (int row = 0; row < RECTLENGTH * RECTLENGTH; row++)
        {
            for (int col = 0; col < RECTLENGTH * RECTLENGTH; col++)
            {
                if (mySudoku.getCell(row, col).getSolution() == null)
                {
                    for (Text candText : uiFields.get(row).get(col).candidates)
                    {
                        if (mySudoku.getCell(row, col).candidates
                                .contains(LegalValues.from(Integer.parseInt(candText.getText()))))
                        {
                            candText.setVisible(true);
                        }
                    }
                }
            }
        }
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
                setSolutionNInputBckgrdColor(row, col, false);
            }
        }
    }

    private void updateSolution(int row, int col, boolean markLastSolutionFound)
    {
        uiFields.get(row).get(col).input.setVisible(false);
        uiFields.get(row).get(col).solution.setVisible(true);
        uiFields.get(row).get(col).solution.setText(Integer.toString(mySudoku.getCell(row, col).getSolution().val()));
        setSolutionNInputBckgrdColor(row, col, markLastSolutionFound);
    }

    public void solutionUpdated(int row, int col, boolean runsInUiThread, boolean markLastSolutionFound)
    {
        // myDisplay.asyncExec(new Runnable()
        // {
        // public void run()
        // {
        // uiFields.get(row).get(col).input.setVisible(false);
        // uiFields.get(row).get(col).solution.setVisible(true);
        // uiFields.get(row).get(col).solution
        // .setText(Integer.toString(mySudoku.getCell(row, col).getSolution().val()));
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
            if (slideShowPause == null)
            { // Step by step
              // End thread
                Thread.currentThread().interrupt();
            }
            else if (slideShowPause > 0)
            {
                try
                {
                    // Thread.sleep(slideShowPause);
                    Thread.sleep(slideShowPause * 1000);
                }
                catch (InterruptedException ex)
                {
                    System.out.println(ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n" + ex.toString());
                }
            }
        }
    }

    private void setSolutionNInputBckgrdColor(int row, int col, boolean markLastSolutionFound)
    {
        if (mySudoku.getCell(row, col).isInput)
        {
            uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(COLOR_SOLT_FOREGRD));
        }
        else
        {
            for (Map<Integer, SolNCandTexts> currRow : uiFields.values())
            {
                for (SolNCandTexts currCell : currRow.values())
                {
                    if (!currCell.solution.getText().isEmpty() && currCell.solution.getForeground().handle == myDisplay
                            .getSystemColor(COLOR_LAST_FOREGRD).handle)
                    {
                        currCell.solution.setForeground(myDisplay.getSystemColor(COLOR_PREV_FOREGRD));
                    }
                }
            }
            int color = COLOR_PREV_FOREGRD;
            if (markLastSolutionFound)
            {
                color = COLOR_LAST_FOREGRD;
            }
            uiFields.get(row).get(col).solution.setForeground(myDisplay.getSystemColor(color));
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
            // cand.setVisible(false);
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

    void toggleSlideShow()
    {
        System.out.println("Slide Show enabled:" + groupSlide.getEnabled());
        System.out.println("ToggleSlideShowSudokuAction.run");
        try
        {
            // app.getSudokuPb().save(null);
            System.out.println("Pressed Slide Show");
            boolean newEnabledState = !getSlideShowEnabled();
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
                lblCurrent.setText(Integer.toString(slideShowPause) + " s");
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not toggle slide show. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
    }
}

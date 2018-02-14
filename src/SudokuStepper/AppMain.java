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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class AppMain extends ApplicationWindow implements SolutionListener, CandidatesListener, SavedListener
{
    private Action           action;
    private Values           mySudoku            = null;
    // remember the last candidate whose status was changed
    private Text             lastUpdatedCandText = null;
    private Font             solutionFont        = null; // SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD);
    private Font             solutionSmallFont   = null; // SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL);

    private static final int INITIAL_WIDTH       = 552;
    private static final int INITIAL_HEIGHT      = 752;

    /**
     * Create the application window.
     */
    public AppMain()
    {
        super(null);
        // addToolBar(SWT.NONE);
        addMenuBar();
        addStatusLine();
        this.myDisplay = new Display();
        this.myShell = new Shell(myDisplay);
        solutionFont = SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD);
        solutionSmallFont = SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL);
        createActions();
    }

    public void dispose()
    {
        System.out.println("AppMain.Dispose");
        if (solutionFont != null)
        {
            solutionFont.dispose();
        }
        if (solutionSmallFont != null)
        {
            solutionSmallFont.dispose();
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
    private Group                                     grpSudokublocks = null;

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

        grpSudokublocks = new Group(OverallContainer, SWT.BORDER | SWT.SHADOW_OUT);
        FormData fd_grpSudokublocks = new FormData();
        fd_grpSudokublocks.right = new FormAttachment(100, -3);
        fd_grpSudokublocks.top = new FormAttachment(0, 3);
        fd_grpSudokublocks.left = new FormAttachment(0, 3);
        grpSudokublocks.setLayoutData(fd_grpSudokublocks);
        grpSudokublocks.setText(StringUtils.EMPTY);
        grpSudokublocks.setLayout(new GridLayout(RECTLENGTH, true));
        for (int blockRow = 1; blockRow <= RECTLENGTH; blockRow++)
        {
            for (int blockCol = 1; blockCol <= RECTLENGTH; blockCol++)
            {
                Composite cellComposite = new Composite(grpSudokublocks, SWT.NONE);
                cellComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
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
                        solutionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW));
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
                                        mySudoku.getCell(totalRow, totalCol).candidates.clear();
                                        mySudoku.getCell(totalRow, totalCol).solution = LegalValues
                                                .from(Integer.parseInt(input));
                                        mySudoku.getCell(totalRow, totalCol).isInput = true;
                                    }
                                }
                                else
                                {
                                    mySudoku.resetCell(totalRow, totalCol);
                                }
                            }
                        });
                        uiFields.get(totalRow).get(totalCol).input = combo;

                        Composite composite_1110 = new Composite(composite_111, SWT.NONE);
                        composite_1110.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
                        GridLayout gl_composite_1110 = new GridLayout(RECTLENGTH, false);
                        gl_composite_1110.horizontalSpacing = 0;
                        gl_composite_1110.verticalSpacing = 0;
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
                                candidateText.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
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
        /*
         * Composite composite_11 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_11.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
         * composite_11.setLayout(new GridLayout(3, false));
         * 
         * Composite composite_111 = new Composite(composite_11, SWT.NONE);
         * composite_111.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * StackLayout layout = new StackLayout(); composite_111.setLayout(layout); Text
         * text_111 = new Text(composite_111, SWT.BORDER | SWT.CENTER);
         * text_111.setBackground(SWTResourceManager.getColor(255, 255, 0));
         * text_111.setToolTipText("ABCD");
         * text_111.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_111.setText("1"); layout.topControl = text_111; Composite composite_1110
         * = new Composite(composite_111, SWT.NONE);
         * composite_1110.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
         * GridLayout gl_composite_1110 = new GridLayout(3, false);
         * gl_composite_1110.horizontalSpacing = 0; gl_composite_1110.verticalSpacing =
         * 0; gl_composite_1110.marginWidth = 0; gl_composite_1110.marginHeight = 0;
         * composite_1110.setLayout(gl_composite_1110);
         * 
         * Text text_1111 = new Text(composite_1110, SWT.BORDER | SWT.READ_ONLY);
         * text_1111.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
         * text_1111.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
         * 1)); text_1111.setFont(SWTResourceManager.getFont("Segoe UI", 8,
         * SWT.NORMAL)); text_1111.setText("1"); text_1111.setToolTipText("01234"); Text
         * text_1112 = new Text(composite_1110, SWT.BORDER);
         * text_1112.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
         * text_1112.setText("2"); Text text_1113 = new Text(composite_1110,
         * SWT.BORDER); text_1113.setFont(SWTResourceManager.getFont("Segoe UI", 8,
         * SWT.NORMAL)); text_1113.setText("3"); Text text_1114 = new
         * Text(composite_1110, SWT.BORDER);
         * text_1114.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
         * text_1114.setText("4"); Text text_1115 = new Text(composite_1110,
         * SWT.BORDER); text_1115.setFont(SWTResourceManager.getFont("Segoe UI", 8,
         * SWT.NORMAL)); text_1115.setText("5"); Text text_1116 = new
         * Text(composite_1110, SWT.BORDER);
         * text_1116.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
         * text_1116.setText("6"); Text text_1117 = new Text(composite_1110,
         * SWT.BORDER); text_1117.setFont(SWTResourceManager.getFont("Segoe UI", 8,
         * SWT.NORMAL)); text_1117.setText("7"); Text text_1118 = new
         * Text(composite_1110, SWT.BORDER);
         * text_1118.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
         * text_1118.setText("8"); Text text_1119 = new Text(composite_1110,
         * SWT.BORDER); text_1119.setFont(SWTResourceManager.getFont("Segoe UI", 8,
         * SWT.NORMAL)); text_1119.setText("9");
         * 
         * Text text_112 = new Text(composite_11, SWT.BORDER);
         * text_112.setBackground(SWTResourceManager.getColor(255, 255, 0));
         * text_112.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_112.setText("2"); text_112.setToolTipText("EFGH"); Text text_113 = new
         * Text(composite_11, SWT.BORDER);
         * text_113.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_113.setText("3"); Text text_114 = new Text(composite_11, SWT.BORDER);
         * text_114.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_114.setText("4"); Text text_115 = new Text(composite_11, SWT.BORDER);
         * text_115.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_115.setText("5"); Text text_116 = new Text(composite_11, SWT.BORDER);
         * text_116.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_116.setText("6"); Text text_117 = new Text(composite_11, SWT.BORDER);
         * text_117.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_117.setText("7"); Text text_118 = new Text(composite_11, SWT.BORDER);
         * text_118.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_118.setText("8"); Text text_119 = new Text(composite_11, SWT.BORDER);
         * text_119.setFont(SWTResourceManager.getFont("Segoe UI", 30, SWT.BOLD));
         * text_119.setText("9");
         * 
         * Composite composite_12 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_12.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_12.setLayout(new GridLayout(3, false)); Text text_121 = new
         * Text(composite_12, SWT.BORDER); text_121.setText("1"); Text text_122 = new
         * Text(composite_12, SWT.BORDER); text_122.setText("2"); Text text_123 = new
         * Text(composite_12, SWT.BORDER); text_123.setText("3"); Text text_124 = new
         * Text(composite_12, SWT.BORDER); text_124.setText("4"); Text text_125 = new
         * Text(composite_12, SWT.BORDER); text_125.setText("5"); Text text_126 = new
         * Text(composite_12, SWT.BORDER); text_126.setText("6"); Text text_127 = new
         * Text(composite_12, SWT.BORDER); text_127.setText("7"); Text text_128 = new
         * Text(composite_12, SWT.BORDER); text_128.setText("8"); Text text_129 = new
         * Text(composite_12, SWT.BORDER); text_129.setText("9");
         * 
         * Composite composite_13 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_13.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_13.setLayout(new GridLayout(3, false)); Text text_131 = new
         * Text(composite_13, SWT.BORDER); text_131.setText("1"); Text text_132 = new
         * Text(composite_13, SWT.BORDER); text_132.setText("2"); Text text_133 = new
         * Text(composite_13, SWT.BORDER); text_133.setText("3"); Text text_134 = new
         * Text(composite_13, SWT.BORDER); text_134.setText("4"); Text text_135 = new
         * Text(composite_13, SWT.BORDER); text_135.setText("5"); Text text_136 = new
         * Text(composite_13, SWT.BORDER); text_136.setText("6"); Text text_137 = new
         * Text(composite_13, SWT.BORDER); text_137.setText("7"); Text text_138 = new
         * Text(composite_13, SWT.BORDER); text_138.setText("8"); Text text_139 = new
         * Text(composite_13, SWT.BORDER); text_139.setText("9");
         * 
         * Composite composite_21 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_21.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_21.setLayout(new GridLayout(3, false)); Text text_211 = new
         * Text(composite_21, SWT.BORDER); text_211.setText("1"); Text text_212 = new
         * Text(composite_21, SWT.BORDER); text_212.setText("2"); Text text_213 = new
         * Text(composite_21, SWT.BORDER); text_213.setText("3"); Text text_214 = new
         * Text(composite_21, SWT.BORDER); text_214.setText("4"); Text text_215 = new
         * Text(composite_21, SWT.BORDER); text_215.setText("5"); Text text_216 = new
         * Text(composite_21, SWT.BORDER); text_216.setText("6"); Text text_217 = new
         * Text(composite_21, SWT.BORDER); text_217.setText("7"); Text text_218 = new
         * Text(composite_21, SWT.BORDER); text_218.setText("8"); Text text_219 = new
         * Text(composite_21, SWT.BORDER); text_219.setText("9");
         * 
         * Composite composite_22 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_22.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_22.setLayout(new GridLayout(3, false)); Text text_221 = new
         * Text(composite_22, SWT.BORDER); text_221.setText("1"); Text text_222 = new
         * Text(composite_22, SWT.BORDER); text_222.setText("2"); Text text_223 = new
         * Text(composite_22, SWT.BORDER); text_223.setText("3"); Text text_224 = new
         * Text(composite_22, SWT.BORDER); text_224.setText("4"); Text text_225 = new
         * Text(composite_22, SWT.BORDER); text_225.setText("5"); Text text_226 = new
         * Text(composite_22, SWT.BORDER); text_226.setText("6"); Text text_227 = new
         * Text(composite_22, SWT.BORDER); text_227.setText("7"); Text text_228 = new
         * Text(composite_22, SWT.BORDER); text_228.setText("8"); Text text_229 = new
         * Text(composite_22, SWT.BORDER); text_229.setText("9");
         * 
         * Composite composite_23 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_23.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_23.setLayout(new GridLayout(3, false)); Text text_231 = new
         * Text(composite_23, SWT.BORDER); text_231.setText("1"); Text text_232 = new
         * Text(composite_23, SWT.BORDER); text_232.setText("2"); Text text_233 = new
         * Text(composite_23, SWT.BORDER); text_233.setText("3"); Text text_234 = new
         * Text(composite_23, SWT.BORDER); text_234.setText("4"); Text text_235 = new
         * Text(composite_23, SWT.BORDER); text_235.setText("5"); Text text_236 = new
         * Text(composite_23, SWT.BORDER); text_236.setText("6"); Text text_237 = new
         * Text(composite_23, SWT.BORDER); text_237.setText("7"); Text text_238 = new
         * Text(composite_23, SWT.BORDER); text_238.setText("8"); Text text_239 = new
         * Text(composite_23, SWT.BORDER); text_239.setText("9");
         * 
         * Composite composite_31 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_31.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_31.setLayout(new GridLayout(3, false)); Text text_311 = new
         * Text(composite_31, SWT.BORDER); text_311.setText("1"); Text text_312 = new
         * Text(composite_31, SWT.BORDER); text_312.setText("2"); Text text_313 = new
         * Text(composite_31, SWT.BORDER); text_313.setText("3"); Text text_314 = new
         * Text(composite_31, SWT.BORDER); text_314.setText("4"); Text text_315 = new
         * Text(composite_31, SWT.BORDER); text_315.setText("5"); Text text_316 = new
         * Text(composite_31, SWT.BORDER); text_316.setText("6"); Text text_317 = new
         * Text(composite_31, SWT.BORDER); text_317.setText("7"); Text text_318 = new
         * Text(composite_31, SWT.BORDER); text_318.setText("8"); Text text_319 = new
         * Text(composite_31, SWT.BORDER); text_319.setText("9");
         * 
         * Composite composite_32 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_32.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_32.setLayout(new GridLayout(3, false)); Text text_321 = new
         * Text(composite_32, SWT.BORDER); text_321.setText("1"); Text text_322 = new
         * Text(composite_32, SWT.BORDER); text_322.setText("2"); Text text_323 = new
         * Text(composite_32, SWT.BORDER); text_323.setText("3"); Text text_324 = new
         * Text(composite_32, SWT.BORDER); text_324.setText("4"); Text text_325 = new
         * Text(composite_32, SWT.BORDER); text_325.setText("5"); Text text_326 = new
         * Text(composite_32, SWT.BORDER); text_326.setText("6"); Text text_327 = new
         * Text(composite_32, SWT.BORDER); text_327.setText("7"); Text text_328 = new
         * Text(composite_32, SWT.BORDER); text_328.setText("8"); Text text_329 = new
         * Text(composite_32, SWT.BORDER); text_329.setText("9");
         * 
         * Composite composite_33 = new Composite(grpSudokublocks, SWT.NONE);
         * composite_33.setBackground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
         * composite_33.setLayout(new GridLayout(3, false)); Text text_331 = new
         * Text(composite_33, SWT.BORDER); text_331.setText("1"); Text text_332 = new
         * Text(composite_33, SWT.BORDER); text_332.setText("2"); Text text_333 = new
         * Text(composite_33, SWT.BORDER); text_333.setText("3"); Text text_334 = new
         * Text(composite_33, SWT.BORDER); text_334.setText("4"); Text text_335 = new
         * Text(composite_33, SWT.BORDER); text_335.setText("5"); Text text_336 = new
         * Text(composite_33, SWT.BORDER); text_336.setText("6"); Text text_337 = new
         * Text(composite_33, SWT.BORDER); text_337.setText("7"); Text text_338 = new
         * Text(composite_33, SWT.BORDER); text_338.setText("8"); Text text_339 = new
         * Text(composite_33, SWT.BORDER); text_339.setText("9"); new
         * Label(grpSudokublocks, SWT.NONE); new Label(grpSudokublocks, SWT.NONE); new
         * Label(grpSudokublocks, SWT.NONE);
         */
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

        Button btnSlideShow = new Button(grpButtons, SWT.NONE);
        btnSlideShow.setText("Slide Show");

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

    private Action renameSudokuAction = new RenameSudokuAction(this);
    private Action newSudokuAction    = new NewSudokuAction(this);
    private Action openSudokuAction   = new OpenSudokuAction(this);
    private Action saveSudokuAction   = new SaveSudokuAction(this);
    private Action saveAsSudokuAction = new SaveAsSudokuAction(this);
    private Action solveSudokuAction  = new SolveSudokuAction(this);
    private Action freezeSudokuAction = new FreezeSudokuAction(this);
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
            Display.getCurrent().dispose(); // display.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void initGuiForNew()
    {
        for (Integer row : uiFields.keySet())
        {
            for (Integer col : uiFields.get(row).keySet())
            {
                SolNCandTexts uiField = uiFields.get(row).get(col);
                uiField.solution.setVisible(false);
                uiField.input.setText(StringUtils.EMPTY);
                uiField.input.setVisible(true);
                for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                {
                    Text cand = uiField.candidates.get(ind);
                    cand.setVisible(false);
                    cand.getParent().setVisible(false);
                }
            }
        }
        freezeSudokuAction.setEnabled(true);
        btnFreeze.setEnabled(true);
    }

    void freeze()
    {
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
                    for (int ind = 0; ind < RECTLENGTH * RECTLENGTH; ind++)
                    {
                        Text cand = uiField.candidates.get(ind);
                        boolean visible = Integer.toString(mySudoku.getCell(row, col).candidates.get(ind).val())
                                .equals(cand.getText());
                        cand.setVisible(visible);
                        cand.getParent().setVisible(visible);
                    }
                }
            }
        }
        freezeSudokuAction.setEnabled(false);
        btnFreeze.setEnabled(false);

    }

    public void updateSudokuFields()
    {
        grpSudokublocks.setText(mySudoku.getName());
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
        saveAsSudokuAction.setEnabled(true);
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
        if (saved)
        {
            saveSudokuAction.setEnabled(false);
        }
        else
        {
            saveSudokuAction.setEnabled(true);
        }
        myDisplay.readAndDispatch();
    }

    public void solutionUpdated(int row, int col)
    {
        uiFields.get(row).get(col).input.setVisible(false);
        uiFields.get(row).get(col).solution.setVisible(true);
        uiFields.get(row).get(col).solution.setText(Integer.toString(mySudoku.getCell(row, col).solution.val()));
        if (mySudoku.getCell(row, col).isInput)
        {
            uiFields.get(row).get(col).solution.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        }
        else
        {
            uiFields.get(row).get(col).solution.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }
        if (mySudoku.getCell(row, col).isAConflict)
        {
            uiFields.get(row).get(col).solution.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
        }
        else
        {
            uiFields.get(row).get(col).solution.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW));
        }
        for (Text cand : uiFields.get(row).get(col).candidates)
        {
            cand.setVisible(false);
            cand.getParent().setVisible(false);
        }
    }
}

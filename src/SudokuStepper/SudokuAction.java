package SudokuStepper;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SudokuAction extends Action
{
    protected AppMain app;
    protected Shell   shell;
    protected Display display;

    public SudokuAction(AppMain appMain)
    {
        super();
        this.app = appMain;
        this.display = app.getDisplay();
        this.shell = app.getShell();
    }

    @Override
    public void run()
    {
        System.out.println("SudokuAction.run");
        app.updateSudokuFields();
    }
}

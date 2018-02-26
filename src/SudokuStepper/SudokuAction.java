package SudokuStepper;

import java.awt.event.KeyEvent;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public abstract class SudokuAction extends Action
{
    protected AppMain app;
    // protected Shell shell;
    // protected Display display;

    public SudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super();
        this.app = appMain;
        this.setText(text);
        ;
        if (acceleratorKey != null)
        {
            setAccelerator(new Integer(acceleratorKey));
        }
    }

    @Override
    public void run()
    {
        System.out.println("SudokuAction.run");
        // app.updateSudokuFields();
    }
}

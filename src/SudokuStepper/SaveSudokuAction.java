/**
 * 
 */
package SudokuStepper;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class SaveSudokuAction extends SudokuAction
{
    String fileFilterPath = "F:/jdk1.5";

    public SaveSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("Save");
    }

    @Override
    public void run()
    {
        System.out.println("SaveSudokuAction.run");
        try
        {
            app.getSudokuPb().save(null);
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not save Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n"
                    + ex.toString());
            errorBox.open();
        }
    }
}

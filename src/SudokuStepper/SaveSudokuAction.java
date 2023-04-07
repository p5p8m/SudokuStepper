/**
 * 
 */
package SudokuStepper;

import java.awt.event.KeyEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class SaveSudokuAction extends SudokuAction
{
    String fileFilterPath = "F:/jdk1.5";

    public SaveSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("SaveSudokuAction.run");
        try
        {
            app.getSudokuPb().save(null, app.getSudokuPb().getSolutionTrace());
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

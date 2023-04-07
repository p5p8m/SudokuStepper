package SudokuStepper;

/**
 * 
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class ExitSudokuAction extends SudokuAction
{
    public ExitSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("ExitSudokuAction.run");
        try
        {
            boolean reallyDo = app.canDiscardOldSudokuIfAnyExists();
            if (reallyDo)
            {
                int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.ICON_QUESTION;
                MessageBox messageBox = new MessageBox(app.getShell(), style);
                messageBox.setText("About to exit SudokuStepper");
                messageBox.setMessage("Exit the application?");
                if (messageBox.open() == SWT.YES)
                {
                    app.terminate();
                }
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not leave without errors. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
        finally
        {
        }
    }

}

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
public class AboutSudokuAction extends SudokuAction
{

    public AboutSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, "&About", acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("AboutSudokuAction.run");
        try
        {
            int style = SWT.OK | SWT.ICON_INFORMATION;
            MessageBox messageBox = new MessageBox(app.getShell(), style);
            messageBox.setText("About SudokuStepper");
            messageBox.setMessage("Version 0.1");
            messageBox.open();
        }
        catch (Exception ex)
        {
            // Just swallow
        }
        finally
        {
        }
    }
}

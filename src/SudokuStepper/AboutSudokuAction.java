/**
 * 
 */
package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * @author Pascal
 *
 */
public class AboutSudokuAction extends SudokuAction
{
    static final String VERSIONMESSAGE = "Version 0.6 on December 13, 2024";

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
            messageBox.setMessage(VERSIONMESSAGE);
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

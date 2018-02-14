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
public class SaveAsSudokuAction extends SaveSudokuAction
{
    public SaveAsSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("Save As");
    }

    @Override
    public void run()
    {
        System.out.println("SaveAsSudokuAction.run");
        try
        {
            // Display a file selection box
            FileDialog dialog = new FileDialog(new Shell(), SWT.OPEN);
            String[] filterNames = new String[]
            { "XML Files", "All Files (*)" };
            String[] filterExtensions = new String[]
            { "*.xml", "*" };
            String filterPath = "/";
            String platform = SWT.getPlatform();
            if (platform.equals("win32"))
            {
                filterNames = new String[]
                { "XML Files", "All Files (*.*)" };
                filterExtensions = new String[]
                { "*.xml", "*.*" };
                filterPath = "c:\\";
            }
            dialog.setFilterNames(filterNames);
            dialog.setFilterExtensions(filterExtensions);
            dialog.setFilterPath(filterPath);
            dialog.setFileName("myfile");
            String fileToWrite = dialog.open();
            System.out.println("Open file: " + fileToWrite);
            app.getSudokuPb().save(fileToWrite);
            app.updateSudokuFields();
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

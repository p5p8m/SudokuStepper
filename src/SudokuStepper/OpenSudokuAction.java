/**
 * 
 */
package SudokuStepper;

import java.util.List;

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
public abstract class OpenSudokuAction extends SudokuAction
{
    String fileFilterPath = "F:/jdk1.5";

    public OpenSudokuAction(AppMain appMain, String text, Integer acceleratorKey, boolean alsoGetSolution)
    {
        super(appMain, text, acceleratorKey);
        alsoReadSolution = alsoGetSolution;
    }

    private boolean alsoReadSolution = false;

    @Override
    public void run()
    {
        System.out.println("OpenSudokuAction.run");
        try
        {
            boolean reallyDo = app.canDiscardOldSudokuIfAnyExists();
            if (reallyDo)
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
                String fileToOpen = dialog.open();
                if (fileToOpen != null)
                {
                    System.out.println("Open file: " + fileToOpen);
                    // add check if previous sudoku is saved
                    app.setState(AppState.OPENING);
                    app.setSudokuPb(new Values());
                    app.getSudokuPb().read(fileToOpen, alsoReadSolution);
                    app.updateSudokuFields(false, true, false);
                }
                else
                {
                    System.out.println("Open aborted by user");
                }
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not load Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n"
                    + ex.toString());
            errorBox.open();
        }
        finally
        {
            app.setState(AppState.EMPTY);
        }
    }
}

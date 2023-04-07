/**
 * 
 */
package SudokuStepper;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class SaveAsSudokuAction extends SaveSudokuAction
{
    public SaveAsSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
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
            String proposal = "myfile";
            if (app != null && app.getSudokuPb() != null && app.getSudokuPb().getName() != null
                    && !app.getSudokuPb().getName().isEmpty())
            {
                proposal = app.getSudokuPb().getName() + ".xml";
            }
            dialog.setFileName(proposal);
            String fileToWrite = dialog.open();
            System.out.println("Save to file: " + fileToWrite);
            if (fileToWrite != null)
            {
                fileToWrite = fileToWrite.trim();
                boolean reallySave = true;
                if (Files.exists(Paths.get(fileToWrite.trim())))
                {
                    MessageBox questionBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
                    questionBox.setText("About to overwrite another sudoku file");
                    questionBox.setMessage(
                            "The file " + fileToWrite + " already exists. Do you really want to overwrite it?");
                    int response = questionBox.open();
                    switch (response)
                    {
                    case SWT.YES:
                        reallySave = true;
                        break;
                    case SWT.NO:
                    default:
                        reallySave = false;
                        break;
                    }
                }
                if (reallySave)
                {
                    app.getSudokuPb().save(fileToWrite, app.getSudokuPb().getSolutionTrace());
                    app.updateSudokuFields(true, true, false);
                }
            }
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

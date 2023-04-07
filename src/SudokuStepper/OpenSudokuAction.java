/**
 * 
 */
package SudokuStepper;

import java.io.Console;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import SudokuStepper.Values.SudokuType;

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
                    try
                    {
                        app.setState(AppState.OPENING);
                        app.setSudokuPb(new Values<LegalValuesGenClass>(SudokuType.SINGLE, LegalValues_9.class, app)); // default
                        // to
                        // be
                        // overwritten when
                        // reading
                        // in
                        SudokuType newSudokuType = app.getSudokuPb().read(fileToOpen, alsoReadSolution);
                        Class<?> newLegalValuesClass = app.getSudokuPb().getLegalValueClass();
                        app.startUpdatingNumOfFields(newLegalValuesClass, newSudokuType);
                        app.updateSudokuFields(false, true, false);
                        app.toggleSlideShow(); // Twice to make sure it is correctly reset as it was previously
                        app.toggleSlideShow();
                    }
                    catch (Exception ex)
                    {
                        MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                        errorBox.setMessage("Could not load Sudoku \"" + fileToOpen + "\"\n" + ex.getMessage() + "\n"
                                + ex.getLocalizedMessage() + "\n" + ex.toString());
                        ex.printStackTrace();
                        errorBox.open();
                    }
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

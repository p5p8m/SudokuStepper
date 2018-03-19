package SudokuStepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SolveSudokuAction extends SudokuAction
{
    public SolveSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("SolveSudokuAction.run");
        try
        {
            app.setState(AppState.SOLVING);
            SolveAlgorithm alg = new SolveAlgorithm(app, "Solution Thred", null);
            Thread solutionThread = new Thread(alg);
            solutionThread.start();
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not start solving Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
        finally
        {
            System.out.println("Leaving Solving action");
        }
    }
}

package SudokuStepper;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class NextSolutionSudokuAction extends SudokuAction
{
    public NextSolutionSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("NextSolutionSudokuAction.run");
        try
        {
            app.setState(AppState.SOLVING);
            SolveAlgorithm alg = new SolveAlgorithm(app, "Next Solution Thread", null);
            SolutionThread solutionThread = new SolutionThread(alg, app);
            solutionThread.start();
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not start looking for next solution for Sudoku. \n" + ex.getMessage() + "\n"
                    + ex.getLocalizedMessage() + "\n" + ex.toString());
            errorBox.open();
        }
        finally
        {
            System.out.println("Leaving Next Solution action");
        }
    }
}

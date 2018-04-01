/**
 * 
 */
package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Pascal
 *
 */
public class OpenSolutionSudokuAction extends OpenSudokuAction
{
    String fileFilterPath = "F:/jdk1.5";

    public OpenSolutionSudokuAction(AppMain appMain, String text, Integer acceleratorKey, boolean alsoGetSolution)
    {
        super(appMain, text, acceleratorKey, alsoGetSolution);
    }

    @Override
    public void run()
    {
        System.out.println("OpenSolutionSudokuAction.run");
        super.run();
    }
}

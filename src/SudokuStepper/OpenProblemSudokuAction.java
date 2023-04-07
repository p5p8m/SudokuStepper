/**
 * 
 */
package SudokuStepper;

/**
 * @author Pascal
 *
 */
public class OpenProblemSudokuAction extends OpenSudokuAction
{
    String fileFilterPath = "F:/jdk1.5";

    public OpenProblemSudokuAction(AppMain appMain, String text, Integer acceleratorKey, boolean alsoGetSolution)
    {
        super(appMain, text, acceleratorKey, alsoGetSolution);
    }

    @Override
    public void run()
    {
        System.out.println("OpenProblemSudokuAction.run");
        super.run();
    }
}

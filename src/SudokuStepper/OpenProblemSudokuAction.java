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

    public OpenProblemSudokuAction(AppMain appMain, String text, Integer acceleratorKey, boolean alsoGetSolution,
            boolean updateSudoku)
    {
        super(appMain, text, acceleratorKey, alsoGetSolution, updateSudoku);
    }

    @Override
    public void run()
    {
        System.out.println("OpenProblemSudokuAction.run");
        super.run();
    }
}

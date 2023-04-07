/**
 * @author Pascal
 * 
 */
package SudokuStepper;

public class ToggleSlideShowSudokuAction extends SudokuAction
{
    public ToggleSlideShowSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        app.toggleSlideShow();
    }
}

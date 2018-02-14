package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FreezeSudokuAction extends SudokuAction
{
    public FreezeSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("Freeze");
    }

    @Override
    public void run()
    {
        System.out.println("FreezeSudokuAction.run");
        try
        {
            app.freeze();
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not freeeze new Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
    }

}

package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FreezeSudokuAction<LegalValuesGen extends LegalValuesGenClass> extends SudokuAction
{
    public FreezeSudokuAction(AppMain appMain, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
    }

    @Override
    public void run()
    {
        System.out.println("FreezeSudokuAction.run");
        try
        {
            app.setState(AppState.EMPTY); // Disables the modifyListener on the combo box
            Values<LegalValuesGen> newSudoku = (Values<LegalValuesGen>) (app.getSudokuPb());
            if (newSudoku != null)
            {
                newSudoku.setSaved(false);
                newSudoku.resetCandidates(app.getLegalValClassUi());
                app.updateSudokuFields(true, true, false);
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not freeeze new Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
        }
        finally
        {
            app.setState(AppState.EMPTY);
        }
    }

}

package SudokuStepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class NewSudokuAction extends SudokuAction
{
    public NewSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("New");
    }

    @Override
    public void run()
    {
        System.out.println("NewSudokuAction.run");
        try
        {
            boolean reallyDo = app.canDiscardOldSudokuIfAnyExists();
            if (reallyDo)
            {
                app.setState(AppState.CREATING);
                app.setSudokuPb(new Values());
                app.updateSudokuFields();
                app.initGuiForNew();
            }
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not create new Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage()
                    + "\n" + ex.toString());
            errorBox.open();
            app.setState(AppState.EMPTY);
        }
        finally
        {
            // Do not reset until "Freeze" is pressed app.setState(AppState.EMPTY);
        }
    }

}
package SudokuStepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import SudokuStepper.Values.SudokuType;

public class NewSudokuAction extends SudokuAction
{
    private SudokuType newSudokuType = SudokuType.SINGLE;

    public NewSudokuAction(AppMain appMain, Values.SudokuType type, String text, Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
        newSudokuType = type;
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
                FreezeSudokuAction freezeSudokuAction = new FreezeSudokuAction(app, null, null);
                freezeSudokuAction.run();

                app.setSudokuPb(new Values(newSudokuType, app)); // Ewige Schleife beim 2. Aufruf
                app.setState(AppState.CREATING);
                app.updateSudokuFields(false, true, false);
                // app.setSlideShowMode(app.getSlideShowEnabled());
                app.disableSlideShow();
                app.initGuiForNew();
            }
        }
        // catch (Exception ex)
        // {
        // MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
        // errorBox.setMessage("Could not create new Sudoku. \n" + ex.getMessage() +
        // "\n" + ex.getLocalizedMessage()
        // + "\n" + ex.toString());
        // errorBox.open();
        // app.setState(AppState.EMPTY);
        // }
        finally
        {
            // Do not reset until "Freeze" is pressed app.setState(AppState.EMPTY);
        }
    }

}

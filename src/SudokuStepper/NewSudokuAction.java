package SudokuStepper;

import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import SudokuStepper.Values.SudokuType;

public class NewSudokuAction extends SudokuAction
{
    private SudokuType newSudokuType = SudokuType.SINGLE;
    private Class<?>   newValClass   = null;

    public NewSudokuAction(AppMain appMain, Values.SudokuType type, Class<?> newLegalValuesClass, String text,
            Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
        newSudokuType = type;
        newValClass = newLegalValuesClass;
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
                // System.out.println("UpdateNumOfValuesAction.run: " +
                // newSubAreaWidth.toString());
                try
                {
                    app.startUpdatingNumOfFields(newValClass, newSudokuType);
                    // app.setLegalValuesSwitchEnabled();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
                    errorBox.setMessage("Could not update the number of fields. \n" + ex.getMessage() + "\n"
                            + ex.getLocalizedMessage() + "\n" + ex.toString());
                    errorBox.open();
                }
                FreezeSudokuAction freezeSudokuAction = new FreezeSudokuAction(app, null, null);
                freezeSudokuAction.run();

                app.setSudokuPb(new Values<LegalValuesGenClass>(newSudokuType, app.getLegalValClassUi(), app)); // Ewige
                // Schleife
                // beim
                // 2.
                // Aufruf
                app.setState(AppState.CREATING);
                app.updateSudokuFields(false, true, false, false);
                // app.setSlideShowMode(app.getSlideShowEnabled());
                app.disableSlideShow();
                app.initGuiForNew(false);
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

package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import SudokuStepper.Values.SudokuType;

public class UpdateNumOfValuesAction extends SudokuAction
{
    Values.SubAreaWidth newVal      = Values.SubAreaWidth.THREE; // Dummy initialization
    Class               newValClass = null;

    public UpdateNumOfValuesAction(AppMain appMain, Class newLegalValuesClass, Values.SubAreaWidth val, String text,
            Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
        newVal = val;
        newValClass = newLegalValuesClass;
    }

    @Override
    public void run()
    {
        System.out.println("UpdateNumOfValuesAction.run: " + newVal.toString());
        try
        {
            Object o = null;
            Class c = o.getClass(); // Just to make sure it is never used
            Display display = app.getDisplay();
            Shell shell = new Shell(display);
            Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
            shell.setCursor(cursor);
            display.update();
            app.startUpdatingNumOfFields(newValClass, newVal, SudokuType.SINGLE);
            // app.setLegalValuesSwitchEnabled();
            cursor.dispose();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not update the number of fields. \n" + ex.getMessage() + "\n"
                    + ex.getLocalizedMessage() + "\n" + ex.toString());
            errorBox.open();
        }
    }
}
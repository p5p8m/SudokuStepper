package SudokuStepper;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class UpdateNumOfValuesAction extends SudokuAction
{
    Values.SubAreaWidth newVal      = Values.SubAreaWidth.THREE; // Dummy initialization
    Class               newValClass = null;

    public UpdateNumOfValuesAction(AppMain appMain, Class legalValuesClass, Values.SubAreaWidth val, String text,
            Integer acceleratorKey)
    {
        super(appMain, text, acceleratorKey);
        newVal = val;
        newValClass = legalValuesClass;
    }

    @Override
    public void run()
    {
        System.out.println("UpdateNumOfValuesAction.run: " + newVal.toString());
        try
        {
            Display display = app.getDisplay();
            Shell shell = new Shell(display);
            Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
            shell.setCursor(cursor);
            display.update();
            app.startUpdatingNumOfFields(newValClass, newVal);
            cursor.dispose();
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not update the number of fields. \n" + ex.getMessage() + "\n"
                    + ex.getLocalizedMessage() + "\n" + ex.toString());
            errorBox.open();
        }
    }
}
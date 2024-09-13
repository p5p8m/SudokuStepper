package SudokuStepper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandler implements ErrorHandler
{
    public XmlErrorHandler()
    {
        return;
    }

    public void warning(SAXParseException e) throws SAXException
    {
        displayMsgBox(e, SWT.ICON_WARNING);
    }

    public void error(SAXParseException e) throws SAXException
    {
        displayMsgBox(e, SWT.ICON_ERROR);
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        displayMsgBox(e, SWT.ICON_ERROR);
    }

    private void displayMsgBox(Exception ex, int style)
    {
        System.out.println(ex.getMessage());
        MessageBox errorBox = new MessageBox(new Shell(), style);
        String text;
        switch (style)
        {
        case SWT.ICON_WARNING:
            text = "Could not load Sudoku because of Xml warnings.";
            break;
        default:
            text = "Could not load Sudoku because of Xml errors.";
            break;
        }
        errorBox.setMessage(text + "\n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n" + ex.toString());
        errorBox.open();

    }
}
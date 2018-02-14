package SudokuStepper;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RenameSudokuAction extends SudokuAction
{

    public RenameSudokuAction(AppMain appMain)
    {
        super(appMain);
        this.setText("Rename");
    }

    @Override
    public void run()
    {
        System.out.println("RenameSudokuAction.run");
        try
        {
            app.getSudokuPb().eliminateCandidate(0, 2, LegalValues.FIVE);
            app.getSudokuPb().eliminateCandidate(0, 2, LegalValues.SIX);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.ONE);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.TWO);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.THREE);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.FOUR);
            // FIVE remains
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.SIX);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.SEVEN);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.EIGHT);
            app.getSudokuPb().eliminateCandidate(3, 1, LegalValues.NINE);
        }
        catch (Exception ex)
        {
            MessageBox errorBox = new MessageBox(new Shell(), SWT.ICON_ERROR);
            errorBox.setMessage("Could not rename Sudoku. \n" + ex.getMessage() + "\n" + ex.getLocalizedMessage() + "\n"
                    + ex.toString());
            errorBox.open();
        }
    }

}
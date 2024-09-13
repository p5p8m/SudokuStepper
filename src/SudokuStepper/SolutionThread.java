package SudokuStepper;

public class SolutionThread extends Thread
{
    public SolutionThread(Runnable target, AppMain app)
    {
        super(target);
        app.setSolvingThread(this);
    }
}

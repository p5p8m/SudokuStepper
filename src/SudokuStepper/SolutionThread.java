package SudokuStepper;

public class SolutionThread extends Thread
{

    // public SolutionThread()
    // {
    // // TODO Auto-generated constructor stub
    // }

    public SolutionThread(Runnable target, AppMain app)
    {
        super(target);
        // TODO Auto-generated constructor stub
        app.setSolvingThread(this);
    }

    // public void run()
    // {
    // super.run();
    // }
    // public SolutionThread(String name)
    // {
    // super(name);
    // // TODO Auto-generated constructor stub
    // }
    //
    // public SolutionThread(ThreadGroup group, Runnable target)
    // {
    // super(group, target);
    // // TODO Auto-generated constructor stub
    // }
    //
    // public SolutionThread(ThreadGroup group, String name)
    // {
    // super(group, name);
    // // TODO Auto-generated constructor stub
    // }
    //
    // public SolutionThread(Runnable target, String name)
    // {
    // super(target, name);
    // // TODO Auto-generated constructor stub
    // }
    //
    // public SolutionThread(ThreadGroup group, Runnable target, String name)
    // {
    // super(group, target, name);
    // // TODO Auto-generated constructor stub
    // }
    //
    // public SolutionThread(ThreadGroup group, Runnable target, String name, long
    // stackSize)
    // {
    // super(group, target, name, stackSize);
    // // TODO Auto-generated constructor stub
    // }

}

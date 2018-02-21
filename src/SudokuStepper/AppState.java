package SudokuStepper;
/**
 * 
 */

/**
 * @author Pascal
 *
 */

public enum AppState
{
    EMPTY(0), CREATING(1), OPENING(2), SOLVING(3), RENAMING(4);
    private final int val;

    private AppState(int value)
    {
        val = value;
    }

    private static AppState[] vals = null;

    public static AppState from(int i)
    {
        if (AppState.vals == null)
        {
            AppState.vals = AppState.values();
        }
        return AppState.vals[i - 1];
    }

    public int val()
    {
        return (val);
    }
}

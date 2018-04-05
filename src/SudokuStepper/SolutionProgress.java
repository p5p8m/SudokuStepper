/**
 * 
 */
package SudokuStepper;

/**
 * @author Pascal
 *
 */
public enum SolutionProgress
{
    NONE(0), CANDIDATES(1), SOLUTION(2);
    private final int val;

    private SolutionProgress(int value)
    {
        val = value;
    }

    private static SolutionProgress[] vals = null;

    private static SolutionProgress from(int i)
    {
        if (SolutionProgress.vals == null)
        {
            SolutionProgress.vals = SolutionProgress.values();
        }
        SolutionProgress retVal = SolutionProgress.vals[i];
        return (retVal);
    }

    // public int val()
    // {
    // return (val);
    // }
    public SolutionProgress combineWith(SolutionProgress newVal)
    {
        int newValInt = Math.max(this.val, newVal.val);
        SolutionProgress retVal = SolutionProgress.from(newValInt);
        return (retVal);
    }
}

package SudokuStepper;

import java.util.List;

public class SolutionTrace
{
    private int               row;
    private int               col;
    private LegalValues       val;
    private List<LegalValues> choices;

    public SolutionTrace(int rowIn, int colIn, LegalValues valIn, List<LegalValues> choicesIn)
    {
        row = rowIn;
        col = colIn;
        val = valIn;
        choices = choicesIn;
    }

    public int getRow()
    {
        return (row);
    }

    public int getCol()
    {
        return (col);
    }

    public LegalValues getValue()
    {
        return (val);
    }

    public List<LegalValues> getChoices()
    {
        return (choices);
    }

}
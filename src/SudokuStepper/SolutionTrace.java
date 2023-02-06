package SudokuStepper;

import java.util.List;

public class SolutionTrace<LegalValuesGen extends LegalValuesGenClass>
{
    private int                  row;
    private int                  col;
    private LegalValuesGen       val;
    private List<LegalValuesGen> choices;

    public SolutionTrace(int rowIn, int colIn, LegalValuesGen valIn, List<LegalValuesGen> choicesIn)
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

    public LegalValuesGen getValue()
    {
        return (val);
    }

    public List<LegalValuesGen> getChoices()
    {
        return (choices);
    }

}
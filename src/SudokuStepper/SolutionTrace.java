package SudokuStepper;

import java.util.List;

public class SolutionTrace<LegalValuesGen>
{
    private int                  row;
    private int                  col;
    private LegalValuesGen       val;
    private List<LegalValuesGen> choices;

    public <LegalValuesGen> SolutionTrace(int rowIn, int colIn, LegalValuesGen valIn, List<LegalValuesGen> choicesIn)
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

    public <LegalValuesGen> LegalValuesGen getValue()
    {
        return (val);
    }

    public <LegalValuesGen> List<LegalValuesGen> getChoices()
    {
        return (choices);
    }

}
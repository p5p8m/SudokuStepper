package SudokuStepper;

import java.util.ArrayList;

public class SolutionTrace<LegalValuesGen extends LegalValuesGenClass>
{
    private int                       row;
    private int                       col;
    private LegalValuesGen            val;
    private ArrayList<LegalValuesGen> choices;

    public SolutionTrace(int rowIn, int colIn, LegalValuesGen valIn, ArrayList<LegalValuesGen> choicesIn)
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

    public LegalValuesGenClass getValue()
    {
        return (val);
    }

    public ArrayList<LegalValuesGen> getChoices()
    {
        return (choices);
    }

}
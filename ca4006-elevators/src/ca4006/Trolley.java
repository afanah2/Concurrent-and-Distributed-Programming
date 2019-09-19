package ca4006;

public class Trolley 
{
    private final double weight;

    public Trolley()
    {
        this.weight = Util.getRandomDouble(10.0, 50.0);
    }

    public double getWeight()
    {
        return this.weight;
    }
}

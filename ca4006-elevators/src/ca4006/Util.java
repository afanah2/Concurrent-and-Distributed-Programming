package ca4006;

public class Util
{
    public static double getRandomDouble(double min, double max)
    {
        double range = max - min + 1.0;
        return (double) (Math.random() * range) + min;
    }

    public static int getRandomInt(int min, int max)
    {
        int range = max - min + 1;
        return (int) (Math.random() * range) + min;
    }

    public static Direction getDirection(int start, int destination)
    {
        if (start > destination)
            return Direction.DOWN;
        else if (start < destination)
            return Direction.UP;
        else
            return Direction.NOWHERE;
    }

    public static Trolley genTrolley(int trolleyRate)
    {
        if (Util.getRandomInt(0, 100) <= trolleyRate)
        {
            return new Trolley();
        }
        return null;
    }
}

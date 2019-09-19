package ca4006;

import java.util.logging.*;

public class Person implements Runnable // extends Thread
{
    private final Logger log = Logger.getLogger("ca4006");

    private final double weight;
    private Trolley trolley;

    private int currentFloor;
    private int start;
    private int destination;

    private long startTime;
    private long runtime;

    private WaitingArea waitingArea;
    private final String name;

    public Person(String name, WaitingArea waitingArea, int start, int destination)
    {
        this.name = name;
        this.waitingArea = waitingArea;
        this.destination = destination;
        this.start = start;
        this.currentFloor = start;

        this.trolley = Util.genTrolley(60);
        this.weight = Util.getRandomDouble(50.0, 100.0);

        log.info("Created Person");
        report();
    }

    public void run()
    {
        startTime = System.currentTimeMillis();
        runtime = startTime;
        while (getCurrentFloor() != getDestination())
        {
            try
            {
                Elevator ele = waitingArea.callElevator(currentFloor, this);
                if (ele.onBoard(this))
                {
                    ele.ride(this);
                }
                report();
            }
            catch (InterruptedException e)
            {
                log.warning(e.getMessage());
                log.warning(e.getStackTrace().toString());
                e.printStackTrace();
            }
        }
        runtime = System.currentTimeMillis() - startTime;
        log.info("I have arrived at the destination!");
    }

    public String getName()
    {
        return this.name;
    }

    public long getRuntime()
    {
        return runtime;
    }

    public int getStart()
    {
        return this.start;
    }

    public int getDestination()
    {
        return this.destination;
    }

    public int getCurrentFloor()
    {
        return this.currentFloor;
    }

    public void setCurrentFloor(int floor)
    {
        this.currentFloor = floor;
    }

    public Direction getDirection()
    {
        return Util.getDirection(currentFloor, destination);
    }

    public double getWeight()
    {
        double weight = this.weight;
        if (this.trolley != null)
        {
            weight += this.trolley.getWeight();
        }
        return weight;
    }

    public String report()
    {
        String msg = String.format("Currently at the [%s] floor, going to [%s]. Weighting [%.2f] kg.",
               this.currentFloor, this.destination, this.getWeight());
        log.info(msg);
        return msg;
    }
}

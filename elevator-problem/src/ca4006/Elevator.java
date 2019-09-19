package ca4006;

import java.util.logging.*;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class Elevator extends Thread
{
    private final Logger log = Logger.getLogger("ca4006");

    private LinkedList<Person> passangers;
    private WaitingArea waitingArea;
    private Direction direction;
    private ProgramState programState;

    private double maxLoad;
    private double carriedLoad;
    private double currentLoad;

    private int destination;
    private int currentFloor;
    private int floorsTravelled;

    private final ElevatorManager manager;
    private final Lock passangerLock;

    private final LinkedList<Person> allPassangers;

    public Elevator(WaitingArea waitingArea, ElevatorManager manager, double maxLoad)
    {
        this.manager = manager;
        this.passangers = new LinkedList<>();
        this.allPassangers = new LinkedList<>();
        this.direction = Direction.NOWHERE;
        this.programState = ProgramState.RUN;
        this.waitingArea = waitingArea;
        this.maxLoad = maxLoad;
        this.passangerLock = new ReentrantLock();
        this.currentFloor = 0;
        this.destination = 0;
        this.carriedLoad = 0.0;
        this.currentLoad = 0.0;
        this.floorsTravelled = 0;
        log.info("Elevator created.");
        report();
    }

    public void run()
    {
        log.info("Starting the hard work...");
        while (getProgramState() == ProgramState.RUN)
        {
            try
            {
                if (getDestination() == getCurrentFloor())
                {
                    if (passangers.isEmpty())
                    {
                        Person p = manager.getRequest(this);
                        if (p == null) break;
                        pickUp(p);
                    }
                    else
                    {
                        setDestination(passangers.peekFirst());
                    }
                }

                move(getDirection());

                if (!passangers.isEmpty())
                {
                    synchronized(passangerLock)
                    {
                        setPassangersLevel(getCurrentFloor());
                        log.info("Waking up all passangers.");
                        passangerLock.notifyAll();
                    }
                }
                waitingArea.elevatorArrived(currentFloor, this);
                report();
                Thread.sleep(200); // hardcoded requirement
            }
            catch (InterruptedException e)
            {
                log.warning(e.getMessage());
            }
        }
        log.info("I'm done here. Bye.");
    }

    private void move(Direction d)
    {
        switch (d)
        {
            case UP:
                currentFloor += 1;
                if (currentFloor >= Main.NUM_FLOORS)
                    currentFloor = Main.NUM_FLOORS - 1;
                break;
            case DOWN:
                currentFloor -= 1;
                if (currentFloor < 0)
                    currentFloor = 0;
                break;
            default:
                return;
        }
        floorsTravelled++;
        log.info("Moved to [" + currentFloor + "] floor.");
        if (getCurrentFloor() == getDestination())
        {
            log.info("I'm at the destination.");
            setDirection(Direction.NOWHERE);
        }
    }

    public void ride(Person person) throws InterruptedException
    {
        while (getCurrentFloor() != person.getDestination())
        {
            synchronized(passangerLock)
            {
                log.info("Not at the destination yet. Waiting.");
                passangerLock.wait();
            }
        }
        removePerson(person);
    }

    /* 
     * Method called by a person thread to remove itself from
     * the elevator. It needs to be synchronized as shared
     * data is being accessed.
     */
    public synchronized void removePerson(Person person)
    {
        log.info("Leaving the elevator.");
        this.passangers.remove(person);
        setCurrentLoad(getCurrentLoad() - person.getWeight());
    }

    /* 
     * Same as the above method by its purpose is to 
     * onboard the person. It only allows for it if the
     * capacity is not exceeded, otherwise it denies the entrance.
     */
    public synchronized boolean onBoard(Person person)
    {
        log.info("Trying to onboard the elevator.");
        double tmp = getCurrentLoad() + person.getWeight();
        if (tmp > maxLoad)
        {
            log.info("Elevator's capacity is maxed out.");
            return false;
        }
        this.currentLoad = tmp;
        this.carriedLoad += person.getWeight();
        this.waitingArea.remove(person);
        this.passangers.add(person);
        this.allPassangers.add(person);
        log.info("Onboarded the elevator.");
        return true;
    }

    private void computeDirection()
    {
        Direction toGo = Util.getDirection(getCurrentFloor(), getDestination());
        setDirection(toGo);
        log.info(String.format("Destination [%s] and Direction [%s]", getDestination(), getDirection()));
    }

    public ProgramState getProgramState()
    {
        return this.programState;
    }

    public void setProgramState(ProgramState programState)
    {
        this.programState = programState;
    }

    public void setDestination(Person person)
    {
        this.destination = person.getDestination();
        computeDirection();
    }

    public void pickUp(Person person)
    {
        this.destination = person.getCurrentFloor();
        computeDirection();
    }

    private void setPassangersLevel(int floor)
    {
        for (Person p : passangers)
        {
            p.setCurrentFloor(floor);
        }
    }

    public int getCurrentFloor()
    {
        return this.currentFloor;
    }

    private void setCurrentLoad(double weight)
    {
        this.currentLoad = weight;
    }

    public double getCurrentLoad()
    {
        return this.currentLoad;
    }

    private void setDirection(Direction direction)
    {
        this.direction = direction;
    }

    private int getDestination()
    {
        return this.destination;
    }

    public Direction getDirection()
    {
        return Util.getDirection(getCurrentFloor(), getDestination());
    }

    public String report()
    {
        String msg = String.format("At the floor [%s] going [%s] with [%d] passangers. Total Weight: [%.2f] kg.",
                this.currentFloor, this.direction, passangers.size(), getCurrentLoad());
        log.info(msg);
        return msg;
    }

    public String getMetrics()
    {
        return String.format("%-20s%-20d%-20d%-20.2f\n", getName(), allPassangers.size(),
                floorsTravelled, carriedLoad);
    }

    public String getAllPassangers()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------------------------------------------------\n");
        sb.append(this.getName());
        sb.append("\n---------------------------------------------------\n");
        sb.append(String.format("%-15s%-15s%-15s%-15s\n",
                    "NAME", "START", "DESTINATION", "RUNTIME"));
        for (Person p : allPassangers)
        {
            sb.append(String.format("%-15s%-15d%-15d%-15d\n",
                        p.getName(), p.getStart(), p.getDestination(), p.getRuntime()));
        }
        sb.append("\n");

        return sb.toString();
    }
}

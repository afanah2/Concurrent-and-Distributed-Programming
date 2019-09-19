package ca4006;

import java.util.logging.*;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.concurrent.locks.*;

public class WaitingArea
{
    private final Logger log = Logger.getLogger("ca4006");

    private final Hashtable<Integer, LinkedList<Person>> floors;
    private final Lock[] locks;
    private final Lock eleLock;

    private ElevatorManager manager;

    public WaitingArea(int num, ElevatorManager manager)
    {
        this.manager = manager;
        this.floors = new Hashtable<>();
        this.eleLock = new ReentrantLock();
        locks = new Lock[num];
        for (int i = 0; i < num; i++)
        {
            locks[i] = new ReentrantLock(true);
            floors.put(i, new LinkedList<Person>());
        }
        log.info("Object created.");
    }

    public synchronized void remove(Person p)
    {
        floors.get(p.getCurrentFloor()).remove(p);
    }

    /*
     * The method is called by a person thread from within their
     * run method. It queues up their request in the ElevatorManager
     * and then goes to sleep in order to be awaken once elevator arrives.
     */
    public Elevator callElevator(int floor, Person person) throws InterruptedException
    {
        Elevator elevator = null;
        synchronized(locks[floor])
        {
            while(elevator == null)
            {
                log.info("I'm in the waiting area on the " + floor + " floor.");
                floors.get(floor).add(person);
                manager.addRequest(person);
                log.info("Going to sleep.");
                locks[floor].wait();
                log.info("Got woken up!");
                elevator = manager.getElevatorAtFloor(floor, person);
                locks[floor].notify();
            }
        }
        return elevator;
    }

    public void elevatorArrived(int floor, Elevator elevator)
    {
        log.info("Arrived at the [" + floor + "] floor.");
        synchronized(locks[floor])
        {
            log.info("Waking up all people on the [" + floor + "] floor!");
            //locks[floor].notifyAll();
            // it's all right to wake up only one person since
            // only one person can get into an elevator.
            locks[floor].notifyAll();
            // TODO: Implement fairness to choose which passanger should have priority to onboard
        }
    }
}

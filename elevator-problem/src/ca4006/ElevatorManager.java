package ca4006;

import java.util.logging.*;
import java.util.LinkedList;
import java.util.concurrent.locks.*;

public class ElevatorManager extends Thread
{
    private final Logger log = Logger.getLogger("ca4006");

    private final Elevator[] elevators;
    private final LinkedList<Person> requests;
    private final Lock eleLock;
    private final Lock reqLock;
    private final double maxLoad;
    private WaitingArea waitingArea;

    private ProgramState state;

    public ElevatorManager(int numOfElevators, double maxLoad)
    {
        /*
         * eleLock is flagged as fair becuase there are multiple 
         * elevators which do work and threads are actually
         * waiting and sleeping. the reqLock doesn't require it
         * as it's only used for synchronization.
         */
        eleLock = new ReentrantLock(true);
        reqLock = new ReentrantLock();
        this.state = ProgramState.RUN;
        this.maxLoad = maxLoad;
        this.requests = new LinkedList<>();
        this.elevators = new Elevator[numOfElevators];
    }

    public void run()
    {
        while (this.state == ProgramState.RUN)
        {
            //try
            //{

            //}
            //catch (InterruptedException e)
            //{
            //    log.warning(e.getMessage());
            //}
        }
    }

    public void init(WaitingArea wa)
    {
        this.waitingArea = wa;
        for (int i = 0; i < elevators.length; i++)
        {
            elevators[i] = new Elevator(wa, this, maxLoad);
            elevators[i].setName("ELEVATOR-" + i);
            elevators[i].start();
        }
    }

    /* 
     * Method called by a person inside the waiting room.
     * Once the request is added to the list an elevator thread is woken up.
     */
    public void addRequest(Person person)
    {
        synchronized(reqLock)
        {
            synchronized(eleLock)
            {
                if (!requests.contains(person))
                    requests.add(person);
                log.info("Waking up all elevators!");
                eleLock.notify();
            }
        }
    }

    /* 
     * Method called by an elevator to obtain its next task (next person 
     * to pick up). If the program is ready to finish or when all people 
     * arrived at their destination an elevator gets an empty request.
     */
    public Person getRequest(Elevator elevator) throws InterruptedException
    {
        synchronized(eleLock)
        {
            while (requests.isEmpty())
            {
                if (state == ProgramState.STOP)
                    return null;

                log.info("No requests. Waiting.");
                eleLock.wait();
                log.info("I am awake!");
            }
            synchronized(reqLock)
            {
                return requests.pop();
            }
        }
    }

    /*
     * The method is making sure all elevators are stopped gracefully. 
     * Some elevator's might be in the standby mode still waiting
     * for new requests. 
     */
    public void stopElevators() throws InterruptedException
    {
        this.state = ProgramState.STOP;
        for (Elevator e : elevators)
        {
            if (e.getProgramState() != ProgramState.STOP)
                e.setProgramState(ProgramState.STOP);
        }
        while (!areStopped())
        {
            synchronized(eleLock)
            {
                log.info("Waking up elevators.");
                // no need to wake up all elevators and have them to fight for lock
                // one elevator us enough.
                //eleLock.notifyAll();
                eleLock.notify();
                // TODO: implement fairness to choose which elevator should pick up first.
            }
            Thread.sleep(500);
        }
    }
    
    /*
     * Checks weather elevators are actually stopped.
     */
    private boolean areStopped()
    {
        for (Elevator e : elevators)
        {
            if (e.getState() != Thread.State.TERMINATED)
                return false;
        }
        return true;
    }

    /*
     * Gets called by a person in the waiting room to find which
     * elevator had arrived. It returns only an elevator that goes the same
     * direction as the person or if it is its final destination.
     */
    public synchronized Elevator getElevatorAtFloor(int floor, Person person)
    {
        for(Elevator e : elevators)
        {
            if (e.getCurrentFloor() == floor 
                && (e.getDirection() == person.getDirection() 
                    || e.getDirection() == Direction.NOWHERE))
                return e;
        }
        return null;
    }

    public void logMetrics()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n\n%-20s%-20s%-20s%-20s\n", "ELEVATOR", "TOTAL_PASSANGERS",
                    "FLOORS_TRAVELLED", "TOTAL_LOAD_CARRIED"));
        sb.append("-----------------------------------------------------------------\n");
        StringBuilder sb2 = new StringBuilder();
        for (Elevator e : elevators)
        {
            sb.append(e.getMetrics());
            sb2.append(e.getAllPassangers());
        }
        sb.append("\n\n").append(sb2);
        log.info(sb.toString());
    }
}

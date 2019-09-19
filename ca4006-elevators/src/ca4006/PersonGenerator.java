package ca4006;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


class ThreadFactoryNameDecorator implements ThreadFactory
{
	/*
	 * Ref: https://stackoverflow.com/questions/6113746/naming-threads-and-thread-pools-of-executorservice
     */
    private final ThreadFactory defaultThreadFactory;
    private final String prefix;
    private int threadCounter;

    public ThreadFactoryNameDecorator(String prefix)
    {
        this(Executors.defaultThreadFactory(), prefix);
    }

    public ThreadFactoryNameDecorator(ThreadFactory threadFactory, String prefix)
    {
        this.defaultThreadFactory = threadFactory;
        this.prefix = prefix;
        this.threadCounter = 0;
    }

    @Override
    public Thread newThread(Runnable task)
    {
        Thread thread = defaultThreadFactory.newThread(task);
        //String tid = thread.getName();
        //tid = tid.split("-")[3];
        thread.setName(prefix + "-" + threadCounter++ );//+ "-" + tid);
        return thread;
    }
}

public class PersonGenerator extends Thread
{
    private static final Logger log = Logger.getLogger("ca4006");

    private final int numOfPeople;
    private final LinkedList<Thread> people;
    private final WaitingArea waitingArea;
    public final ScheduledExecutorService executor;
    //private final ThreadPoolExecutor executor;

    public PersonGenerator(WaitingArea waitingArea, int numOfPeople, int poolSize)
    {
        this.waitingArea = waitingArea;
        this.numOfPeople = numOfPeople;
        this.people = new LinkedList<>();
        /*
         * Custom ThreadFactory is used to name threads with 'PEROSNS' prefix.
         * The size of the thread pool normaly would be less than the number
         * of actual tasks required to run, however we use the total
         * number of people to allow the correct naming convention
         * which helps tracking the work.
         */
        this.executor = Executors.newScheduledThreadPool(numOfPeople,
                new ThreadFactoryNameDecorator("PERSON"));
    }

    /*
     * inside the run method, the executor schedules randomly new persons
     * at random times and random ammounts.
     */
    public void run()
    {
        int i = 0;
        while(i < numOfPeople)
        {
            try
            {
                int delay = Util.getRandomInt(0, 3);
                String name = "PERSON-" + i++;
                log.info("Creating " + name + " with the [" + delay + "] start delay.");
                Thread tPerson = new Thread(newPerson(name));
                tPerson.setName(name); // setting the thread name;
                people.add(tPerson);
                executor.schedule(tPerson, delay, TimeUnit.SECONDS);
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                log.warning(e.getMessage());
                throw new RuntimeException("Failed to generate a Person.");
            }
        }
        log.info("My job is done here.");
    }

    private Person newPerson(String name)
    {
        int start = Util.getRandomInt(0, Main.NUM_FLOORS-1);
        int destination = Util.getRandomInt(0, Main.NUM_FLOORS-1);
        while (start == destination)
        {
            destination = Util.getRandomInt(0, Main.NUM_FLOORS-1);
        }
        return new Person(name, waitingArea, start, destination);
    }

    public void joinPersons() throws InterruptedException
    {
        TimeUnit.SECONDS.sleep(5);
        log.info("There are currently [" + people.size() + "] people.");
        while (people.size() > 0)
        {
            Thread p = people.pop();
            log.info("Joining " + p.getName());
            p.join();
        }
        executor.shutdown();
        int c = 0;
        while (!executor.awaitTermination(2, TimeUnit.SECONDS))
        {
            log.info("People still haven't finished yet.");
            log.info("Terminated: " + executor.isTerminated() + "\tShutdown: " + executor.isShutdown());
            if (c++ > 4)
                executor.shutdownNow();
        }
    }
}

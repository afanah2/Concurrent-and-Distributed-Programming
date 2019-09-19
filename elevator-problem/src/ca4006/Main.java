package ca4006;

import java.util.logging.*;
import java.io.*;

public class Main
{
    private static final Logger log = Logger.getLogger("ca4006");

    public static final int NUM_FLOORS = 10;
    public static final int NUM_ELEVATORS = 8;
    public static final int NUM_PEOPLE = 35;
    public static final int POOL_SIZE = 5;
    public static final double WEIGHT_CAPACITY = 500.0;

    public static void main (String[] args) throws InterruptedException, IOException
    {
        initLogger();
        logConstants();
        log.info("Starting the main execution.");

        ElevatorManager manager = new ElevatorManager(NUM_ELEVATORS, WEIGHT_CAPACITY);
        WaitingArea waitingArea = new WaitingArea(NUM_FLOORS, manager);
        PersonGenerator generator = new PersonGenerator(
                waitingArea, NUM_PEOPLE, POOL_SIZE);

        manager.init(waitingArea);
        generator.setName("PeopleGen");
        generator.start();

        log.info("waiting for generator to finish.");
        generator.join();

        log.info("Waiting for all people to arrive at their destination.");
        generator.joinPersons();

        log.info("People had finished. Stopping elevators.");
        manager.stopElevators();

        log.info("Exiting the main execution.");
        manager.logMetrics();
    }

    private static void initLogger() throws IOException
    {
        FileHandler fileHandler = new FileHandler("elevators.log");
        ConsoleHandler consHandler = new ConsoleHandler();

        Formatter lf = new LogFormatter();

        fileHandler.setFormatter(lf);
        consHandler.setFormatter(lf);

        log.setUseParentHandlers(false);
        log.addHandler(fileHandler);
        log.addHandler(consHandler);
        log.setLevel(Level.INFO);
    }

    private static void logConstants()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        sb.append(String.format("\t%-20s:%10d\n", "NUMBER OF FLOORS", NUM_FLOORS));
        sb.append(String.format("\t%-20s:%10d\n", "NUMBER OF ELEVATORS", NUM_ELEVATORS));
        sb.append(String.format("\t%-20s:%10d\n", "NUMBER OF PEOPLE", NUM_PEOPLE));
        sb.append(String.format("\t%-20s:%10d\n", "SERVICE POOL SIZE", POOL_SIZE));
        sb.append(String.format("\t%-20s:%10.2f\n", "ELEVATOR CAPACITY", WEIGHT_CAPACITY));
        sb.append("\n");
        log.info(sb.toString());
    }
}

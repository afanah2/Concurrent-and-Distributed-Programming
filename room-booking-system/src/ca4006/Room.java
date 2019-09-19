package ca4006;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;
import java.time.*;

/*
 * Simple room object. There is one instance of this class created
 * for each available room. Clients call remote interface object 
 * to access the room objects. Only book() method is synchronized
 * as we dont want more than one client to be able to book a room
 * a the same time for the same date.
 */

public class Room implements Serializable
{
    private static final long serialVersionUID = 24L;
    private int capacity;
    private int floor;
    private int number;
    private String building;
    private int count = 0;

    public List<LocalDate> timetable;

    private final String name;

    public Room(int capacity, String building, int floor, int number)
    {
        this.timetable = new LinkedList<LocalDate>();
        this.capacity = capacity;
        this.floor = floor;
        this.building = building;
        this.number = number;
        this.name = String.format("%s%d.%03d", building, floor, number);
    }

    public void reset()
    {
        timetable = new LinkedList<>();
        count = 0;
    }

    public boolean isAvailable(LocalDate date)
    {
        if (this.timetable.contains(date))
            return false;
        return true;
    }

    public synchronized boolean book(LocalDate date)
    {
        System.out.printf("%10s total_requests [%d]    total_bookings [%d].\n", getName(), ++count, timetable.size());
        if (isAvailable(date))
        {
            timetable.add(date);
            return true;
        }
        return false;
    }

    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<LocalDate> getTimetable() { return timetable; }

    public String getName() { return name; }

    public int getCapacity() { return capacity; }

    public int getFloor() { return floor; }

    public int getNumber() { return number; }

    public String getBuilding() { return building; }

    public String toString()
    {
        return String.format("%10s - capacity is %d people.", name, capacity);
    }
}

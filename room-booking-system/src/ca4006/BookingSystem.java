package ca4006;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

/*
 * BookingSystem is the remote interface. It's accessible by clients
 * via RMI protocol. There is no synchronization on the interface level.
 * All clients are able to invoke the methods asynchronously, however the 
 * synchronization is implemented on the ROOM level. 
 */

public class BookingSystem extends UnicastRemoteObject implements BookingInterface
{
	private static final long serialVersionUID = 1L;

    private List<Room> roomList;
    private HashMap<String, Room> roomMap;
    private int count = 0;

    private static List<Room> generateRooms()
    {
        LinkedList<Room> list = new LinkedList<>();
        list.add(new Room(60, "L", 0, 25));
        list.add(new Room(60, "L", 0, 26));
        list.add(new Room(30, "L", 0, 27));
        list.add(new Room(80, "L", 1, 101));
        list.add(new Room(80, "L", 1, 105));
        list.add(new Room(60, "L", 1, 121));
        list.add(new Room(35, "X", 1, 14));
        list.add(new Room(55, "X", 1, 15));
        list.add(new Room(70, "X", 0, 120));
        list.add(new Room(40, "C", 0, 4));
        list.add(new Room(50, "C", 0, 5));
        list.add(new Room(80, "C", 0, 6));
        list.add(new Room(400, "T", 1, 1));
        return list;
    }

	public BookingSystem() throws RemoteException
    {
        super();
        roomList = BookingSystem.generateRooms();
        roomMap = new HashMap<>();
        for (Room r : roomList) roomMap.put(r.getName(), r);
    }

    public void resetBookings() throws RemoteException
    {
        for (Room r : roomList) r.reset();
    }

    public Room getRoom(String name) throws RemoteException
    {
        return roomMap.get(name);
    }

    public List<Room> getRooms() throws RemoteException
    {
        System.out.println("Listing rooms for " + ++count);
        return roomList;
    }

    public List<LocalDate> getTimetable(String name) throws RemoteException
    {
        return roomMap.get(name).getTimetable();
    }

    public boolean isAvailable(String name, LocalDate date) throws RemoteException
    {
        return roomMap.get(name).isAvailable(date);
    }

    public boolean makeBooking(String name, LocalDate date) throws RemoteException
    {
        System.out.printf("BookingSystem recevied booking for the %d time.\n", ++count);
        return roomMap.get(name).book(date);
    }
}

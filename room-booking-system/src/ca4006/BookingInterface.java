package ca4006;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;

public interface BookingInterface extends Remote
{
    public Room getRoom(String name) throws RemoteException;
    public List<Room> getRooms() throws RemoteException;
    public List<LocalDate> getTimetable(String name) throws RemoteException;
    public boolean isAvailable(String name, LocalDate date) throws RemoteException;
    public boolean makeBooking(String name, LocalDate date) throws RemoteException;

    // testing purposes 
    public void resetBookings() throws RemoteException;
}

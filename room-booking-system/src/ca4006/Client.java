package ca4006;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.text.ParseException;


enum State
{
    STOP, RUN
}

/*
 * Booking Client. Simple console interface. It needs a better
 * way of handling an incorrect input by the user. Also,
 * RemoteExceptions only brings you back to the main menu.
 */
public class Client
{
    private BookingInterface booking;
    private Scanner scanner;
    private State STATE;

    public static void main(String[] args) throws
        RemoteException, NotBoundException, MalformedURLException, ParseException
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        String serviceUrl = "rmi://" + host + ":" + port + "/booking";
        System.out.println(serviceUrl);

        Client client = new Client(serviceUrl);
        client.start();

        System.out.println("FINISHED");
    }

    public Client(String url) throws NotBoundException, MalformedURLException, RemoteException
    {
        this.STATE = State.RUN;
        this.booking = (BookingInterface) Naming.lookup(url);
        this.scanner = new Scanner(System.in);
    }

    private int chooseOne(List<String> options)
    {
        for (int i = 0; i < options.size(); i++)
            System.out.println(String.format("%10d. %s", i+1, options.get(i).toString()));
        System.out.print("\n\n\tCHOICE: ");
        int choice = scanner.nextInt();
        System.out.println("\n");
        return choice;
    }

    private void mainOption() throws RemoteException
    {
        List<String> options = new LinkedList<>();
        options.add("List rooms.");
        options.add("Exit.");
        
        System.out.println("\n\nChoose one of the procedures: ");
        int choice = chooseOne(options);
        switch (choice)
        {
            case 1:
                listRooms();
                break;
            case 2:
                STATE = State.STOP;
                break;
            default:
                break;
        }
    }

    private void listRooms() throws RemoteException
    {
        List<Room> rooms = booking.getRooms();
        System.out.println("Pick a room:");
        List<String> roomsNames = rooms.stream().map((room) -> 
                room.getName()).collect(Collectors.toList());
        int choice = chooseOne(roomsNames);
        roomOptions(rooms.get(choice - 1));
    }

    private void roomOptions(Room room) throws RemoteException
    {
        System.out.printf("\nROOM:\t%s\n\n", room.toString());
        List<String> options = new LinkedList<>();
        options.add("Check availability.");
        options.add("Book the room.");
        options.add("Go back.");
        boolean repeat = false;
        do
        {
            int choice = chooseOne(options);
            switch (choice)
            {
                case 1:
                    checkAvailability(room);
                    break;
                case 2:
                    repeat = !bookRoom(room);
                    roomOptions(room);
                    break;
                case 3:
                    break;
                default:
                    repeat = true;
            }
        } while(repeat);
    }

    private void checkAvailability(Room room) throws RemoteException
    {
        List<LocalDate> timetable = booking.getTimetable(room.getName());
        if (timetable.isEmpty())
        {
            System.out.println("\nRoom has no bookings currently.");
        }
        else
        {
            System.out.println("\nBOOKINGS:");
        }
        for (LocalDate date : timetable)
        {
            System.out.printf("%15s - BOOKED\n", date.toString());
        }
        System.out.println("\n\n");
        roomOptions(room);
    }

    private boolean bookRoom(Room room) throws RemoteException
    {
        LocalDate bookingDate = readDate();
        if (booking.makeBooking(room.getName(), bookingDate))
        {
            System.out.println("\n\nCongratulations! Room's booked for " + bookingDate);
            return true;
        }
        {
            System.out.println("\n\tUnfortunately, the room is already booked on " + bookingDate);
            return bookRoom(room);
        }
    }

    private LocalDate readDate()
    {
        LocalDate bookingDate = null;
        boolean repeat = false;
        do
        {
            System.out.print("\nEnter a date [yyyy-MM-dd]: ");
            try 
            {
                bookingDate = LocalDate.parse(scanner.next());
                if (bookingDate.isBefore(LocalDate.now()))
                {
                    System.out.println("\n\tDate must be in the future.");
                    repeat = true;
                }
                else
                {
                    repeat = false;
                }
            } 
            catch (DateTimeParseException e) 
            {
                System.out.println("\n\tFormat not recognized. " + e.getMessage());
                repeat = true;
            }
        } while(repeat);
        return bookingDate;
    }

    public void start()
    {
        while(STATE == State.RUN)
        {
            try
            {
                mainOption();
            }
            catch (RemoteException e)
            {
                System.out.println("\n\tI'M SORRY BUT AN ERROR OCCURED: " + e.getMessage());
            }
        }
    }
}


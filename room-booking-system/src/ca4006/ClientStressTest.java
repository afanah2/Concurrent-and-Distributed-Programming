package ca4006;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.rmi.NotBoundException;

public class ClientStressTest
{
    public static void main(String[] args) throws
        RemoteException, NotBoundException, MalformedURLException, InterruptedException
    {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int totalClients = (args.length >= 4) ? Integer.parseInt(args[2]) : 2;
        int totalDays = (args.length >= 3) ? Integer.parseInt(args[3]) : 20;

        String serviceName = "rmi://" + host + ":" + port + "/booking";
        System.out.println(serviceName);

        BookingInterface booking = (BookingInterface) Naming.lookup(serviceName);
        booking.resetBookings();

        CyclicBarrier barrier = new CyclicBarrier(totalClients);

        List<Clientello> clients = new LinkedList<>();
        List<Thread> threads = new LinkedList<>();

        for (int i = 1; i <= totalClients; i++)
        {
            Clientello client = new Clientello(barrier, serviceName, totalDays);
            Thread thread = new Thread(client);
            thread.setName(String.format("CLIENT-%02d", i));
            thread.start();
            clients.add(client);
            threads.add(thread);
        }

        for (Thread t : threads)
        {
            t.join();
        }

        printBookings(clients, threads, totalDays);

        System.out.println("\n\n\tFINISHED");
    }

    private static void printBookings(List<Clientello> clients, List<Thread> threads, int days)
    {
        System.out.println("\n\n\tBOOKING RESULTS: ");
        LocalDate beforeDate = LocalDate.now().plusDays(days);
        for (LocalDate date = LocalDate.now(); 
                date.isBefore(beforeDate); 
                date = date.plusDays(1))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-12s", date.toString()));
            String line = "------------";
            for (int i = 0; i < clients.size(); i++)
            {
                Clientello client = clients.get(i);
                line += "---";
                if (client.bookings.contains(date.toString()))
                {
                    // append "C1" instead of "CLIENT-1"
                    // => smaller size of the table
                    sb.append(String.format("%3s", "C" + 
                                threads.get(i).getName().split("-")[1]));
                }
                else
                {
                    sb.append(String.format("%3s", ""));
                }
            }
            System.out.println(line + "\n" + sb.toString());
        }
    }
}

class Clientello implements Runnable
{
    private BookingInterface booking;
    private int totalDays;
    public List<String> bookings;
    private CyclicBarrier barrier;

    public Clientello(CyclicBarrier barrier, String url, int totalDays) throws 
        NotBoundException, MalformedURLException, RemoteException
    {
        this.barrier = barrier;
        this.booking = (BookingInterface) Naming.lookup(url);
        this.totalDays = totalDays;
        this.bookings = new LinkedList<>();
    }

    public void run()
    {
        LocalDate beforeDate = LocalDate.now().plusDays(totalDays);
        List<Room> rooms = null;
        try 
        {
            rooms = booking.getRooms();
        } 
        catch (RemoteException e)
        {
            e.printStackTrace();
        }


        for (LocalDate date = LocalDate.now(); 
                date.isBefore(beforeDate); 
                date = date.plusDays(1))
        {
            try
            {
                String roomName = rooms.get(0).getName();
                System.out.printf("%-10s booking %-9s%-10s\n", Thread.currentThread().getName(),
                        roomName, date.toString());
                
                //  waiting for all clients to join up before starting booking.
                barrier.await(); 
                if (booking.makeBooking(roomName, date))
                {
                    bookings.add(date.toString());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

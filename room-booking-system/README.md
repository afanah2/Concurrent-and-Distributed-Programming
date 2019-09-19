# ca4006-elevators


## Download and build
```
git clone https://github.com/afanah2/Concurrent-and-Distributed-Programming/tree/master/room-booking-system
cd room-booking-system
chmod +x run
chmod +x test
mkdir build
javac -cp src:$CLASSPATH -d build src/ca4006/Client.java
javac -cp src:$CLASSPATH -d build src/ca4006/BookingServer.java
javac -cp src:$CLASSPATH -d build src/ca4006/ClientStressTest.java
```

## To run a server:
./run s [hostname|ip] [port]
```
./run s 0.0.0.0 9999
```

## To run a client:
./run c [hostname|ip] [port]
```
./run c afanah.com 9999
```




## Alternatively
From the root directory:
* To compile:
    ```
    javac -cp src:$CLASSPATH -d build src/ca4006/Client.java
    javac -cp src:$CLASSPATH -d build src/ca4006/BookingServer.java
    javac -cp src:$CLASSPATH -d build src/ca4006/ClientStressTest.java
    ```

* To execute:
    * Client:
    ```
    java -cp build ca4006.Client [hostname] [port]
    ```
    * Server:
    ```
    java -cp build ca4006.BookingServer -Djava.rmi.server.codebase=file:build/ -Djava.rmi.server.hostname=[DNS name | IP] [hostname] [port]
    ``` 
    

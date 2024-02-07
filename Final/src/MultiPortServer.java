import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiPortServer {

    private static final int[] PORTS = {9989};
    private static final int MAX_CLIENTS_PER_PORT = 4;
    private static final Logger logger = Logger.getLogger(MultiPortServer.class.getName());


    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        for (int port : PORTS)
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server started on port " + port + ". Waiting for clients...");

            // Accept clients on the specified port and handle them concurrently
            for (int i = 0; i < MAX_CLIENTS_PER_PORT; i++) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected. Client's IP Address: " + clientSocket.getInetAddress());

                // Handle each client connection in a separate thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        }catch (IOException e) {
            // Handle server socket errors
            handleServerSocketError(port, e);
        }
    }

    // Method to handle server socket errors
    private static void handleServerSocketError(int port, IOException e) {
        String errorMessage = "Error in server socket on port " + port;
        logger.log(Level.SEVERE, errorMessage, e);
        System.out.println("There was an error with the server socket on port " + port + ". Please try again later.");
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket; // The client's socket connection
    private final BufferedReader bufferedReader; // Reader to receive data from the client
    private final BufferedWriter bufferedWriter; // Writer to send data to the client

    // Constructor to initialize the ClientHandler with the client's socket
    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        // Initialize BufferedReader to read data sent by the client
        this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // Initialize BufferedWriter to send data back to the client
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            handleClient();  // Method to handle client communication
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close client connections when done
                bufferedReader.close(); // Close input stream from client
                bufferedWriter.close(); // Close output stream to client
                clientSocket.close();   // Close client's socket connection
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient() throws IOException {

        //ask client for their username
        bufferedWriter.write("Please enter your name: ");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        //receive the username from the client
        String username = bufferedReader.readLine();
        System.out.println("Client " + clientSocket.getInetAddress() + " username is: " + username);

        // ask client if they want to input or view data
        bufferedWriter.write("Do you want to input data (yes/no) or view existing data (view/no)");
        bufferedWriter.newLine();
        bufferedWriter.flush();

        //receive the clients response
        String response = bufferedReader.readLine();
        System.out.println("Q: Do you want to input data (yes/no) or view existing data (view/no)? " + username + "'s response: " + response);

        if ("yes".equalsIgnoreCase(response)) {
            //ask for userID
            bufferedWriter.write("Enter your User ID: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String userID = bufferedReader.readLine();
            System.out.println(username + "'s User ID: " + userID);

            //ask for postcode
            bufferedWriter.write("Enter the Postcode: ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String postcode = bufferedReader.readLine();
            System.out.println(username + "'s postcode entry: " + postcode);

            //ask for co2 emission
            bufferedWriter.write("Enter the CO2 Emission (ppm): ");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            String co2ppm = bufferedReader.readLine();
            System.out.println(username + "'s CO2 Emission entry: " + co2ppm);

            //record the local date time when finishing data
            LocalDateTime finishDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String finishTimestamp = finishDateTime.format(formatter);
            System.out.println(username + " has submitted data at: " + finishTimestamp);

            //process the received data
            List<String> data = new ArrayList<>();
            data.add(userID);
            data.add(postcode);
            data.add(co2ppm);
            data.add(finishTimestamp);

            // Writing data to CSV file directly
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("co2_data.csv", true))) {
                for (String entry : data) {
                    writer.write(entry + ",");
                }
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Continuously receive messages from the client until 'FINISHED' is received
        while (true) {
            String msgFromClient = bufferedReader.readLine();
            System.out.println(username + ": " + msgFromClient);
            bufferedWriter.write("MSG Received");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if (msgFromClient.equalsIgnoreCase("FINISHED"))
                break;
        }
    }
}




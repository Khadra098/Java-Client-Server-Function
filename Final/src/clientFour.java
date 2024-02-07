import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class clientFour {
    private static final Logger logger = Logger.getLogger(MultiPortServer.class.getName());

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 9989);
             InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
             BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome!");

            System.out.println((bufferedReader.readLine())); // Display initial message from the server

            String username = scanner.nextLine(); // Read user input for username
            bufferedWriter.write(username);  // Send username to the server
            bufferedWriter.newLine(); // Write a newline to signal the end of the username
            bufferedWriter.flush();  // Flush the stream


            System.out.println(bufferedReader.readLine());  // Receive and display the server's response

            String response = scanner.nextLine();
            bufferedWriter.write(response);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            if ("yes".equalsIgnoreCase(response)) {
                // Handling user input for data entry
                //user_id
                System.out.println(bufferedReader.readLine());  // Receive and display server's request for UserID
                String userID = scanner.nextLine();  // Read user input for UserID
                bufferedWriter.write(userID); // Send UserID to the server
                bufferedWriter.newLine(); // Write a newline to signal the end of the UserID
                bufferedWriter.flush();  // Flush the stream


                //postcode
                System.out.println(bufferedReader.readLine());
                String postcode = scanner.nextLine();
                bufferedWriter.write(postcode);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                //co2 emission
                System.out.println(bufferedReader.readLine());
                String co2ppm = scanner.nextLine();
                bufferedWriter.write(co2ppm);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Timestamping and data submission
                String finishTimestamp = getCurrentTimestamp();
                System.out.println("You have submitted your input data at: " + finishTimestamp);
                System.out.println("type 'FINISHED' to close system");
                bufferedWriter.write(finishTimestamp);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Construct and send the data record to the server
                String dataRecord = userID + "," + postcode + "," + co2ppm + "," + finishTimestamp;
                bufferedWriter.write(dataRecord); // Send data record to the server
                bufferedWriter.newLine();  // Write a newline to signal the end of the data record
                bufferedWriter.flush(); // Flush the stream
            }

            // Message sending loop
            while (true) {
                String msgToSend = scanner.nextLine();
                bufferedWriter.write(msgToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                System.out.println("Server: " + bufferedReader.readLine()); // Display server's response

                if (msgToSend.equalsIgnoreCase("FINISHED"))
                    break;  // Exit the loop when the user sends "FINISHED"
            }
        } catch (IOException e) {
            // Exception handling for client connection errors
            logger.log(Level.SEVERE, "Error in client connection: " + e.getMessage(), e);

            // Handling different types of connection errors
            if (e instanceof java.net.ConnectException) {
                // Connection Error: Server unreachable or refused connection
                System.out.println("Connection to the server failed. Please check if the server is running and try again.");
            } else if (e instanceof java.net.UnknownHostException) {
                // Unknown Host Error: Invalid server address
                System.out.println("Invalid server address. Please verify the server address and try again.");
            } else {
                // Generic IOException: Other I/O related issues
                System.out.println("There was an error with the client connection. Please try again later.");
            }
        }
    }

    // Method to get the current timestamp in the specified format
    private static String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter); // Return the formatted timestamp
    }
}
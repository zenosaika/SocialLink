import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private final ServerSocket serverSocket;
    private final int port;
    private final List<Socket> connectedClients;

    public ChatServer(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        connectedClients = new ArrayList<>(); // List to store connected clients
    }

    public void startServer() throws IOException {
        System.out.println("Server started on port " + port);
        while (true) {
            // Accept a new client connection
            Socket clientSocket = serverSocket.accept();
            connectedClients.add(clientSocket); // Add client to connected list

            // Create a thread to handle the client communication
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            new Thread(clientHandler).start();
        }
    }

    public void broadcastMessage(String message) {
        // Broadcast message to all connected clients
        synchronized (connectedClients) { // Synchronize access for thread safety
            for (Socket clientSocket : connectedClients) {
                try {
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                    output.writeUTF(message);
                } catch (IOException e) {
                    // Handle sending errors or client disconnection
                    e.printStackTrace();
                    connectedClients.remove(clientSocket); // Remove disconnected client
                }
            }
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private final ChatServer server; // Reference to the ChatServer instance
        private final DataInputStream input;

        public ClientHandler(Socket clientSocket, ChatServer server) throws IOException {
            this.clientSocket = clientSocket;
            this.server = server;
            input = new DataInputStream(clientSocket.getInputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Read message from client
                    String message = input.readUTF();
                    System.out.println("Received message from " + clientSocket.getInetAddress() + ": " + message);
                    server.broadcastMessage(message); // Broadcast received message
                }
            } catch (IOException e) {
                // Handle client disconnection or errors
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            } finally {
                try {
                    // Close resources when client disconnects
                    clientSocket.close();
                    connectedClients.remove(clientSocket); // Remove from connected list
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 1234; // Change port number as needed
        ChatServer server = new ChatServer(port);
        server.startServer();
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ChatClient extends JFrame implements ActionListener, KeyListener {

  private final JTextArea chatHistory;
  private final JTextField messageField;
  private final JButton sendButton;
  private Socket socket;
  private DataOutputStream output;
  private DataInputStream input;
  private JTextField usernameField;
  private JLabel usernameLabel;
  private String username = "Guest";

  public ChatClient() throws IOException {
    super("Social Link (Beta)");

    UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 16));

    usernameLabel = new JLabel("Username:");
    usernameField = new JTextField("Guest", 15);
    usernameField.addActionListener(this);

    // Create a JPanel to group label and field
    JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    usernamePanel.add(usernameLabel);
    usernamePanel.add(usernameField);

    // Connect to server (replace with actual server IP/port)
    socket = new Socket("localhost", 1234); // Change server IP and port as needed
    output = new DataOutputStream(socket.getOutputStream());
    input = new DataInputStream(socket.getInputStream());

    // Create chat history text area
    chatHistory = new JTextArea();
    chatHistory.setEditable(false);
    chatHistory.setBackground(Color.WHITE);
    chatHistory.setFont(new Font("Arial", Font.PLAIN, 14));
    JScrollPane chatScrollPanel = new JScrollPane(chatHistory);
    chatScrollPanel.setPreferredSize(new Dimension(400, 200));
    chatHistory.setBackground(new Color(1, 20, 38));
    chatHistory.setForeground(Color.WHITE);
    chatHistory.setMargin(new Insets(10, 10, 15, 10));

    // Create message input field
    messageField = new JTextField();
    messageField.addKeyListener(this);
    messageField.setColumns(25);

    // Create send button
    sendButton = new JButton("Send");
    sendButton.addActionListener(this);
    ImageIcon sendIcon = new ImageIcon("assets/send_button.png"); // Replace with actual path
    sendIcon = new ImageIcon(sendIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
    sendButton.setIcon(sendIcon);
    sendButton.setBorder(BorderFactory.createEmptyBorder()); // Remove button border

    // Layout components
    JPanel messagePanel = new JPanel(new FlowLayout());
    messagePanel.add(messageField);
    messagePanel.add(sendButton);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(chatScrollPanel, BorderLayout.CENTER);
    getContentPane().add(messagePanel, BorderLayout.SOUTH);

    getContentPane().add(usernamePanel, BorderLayout.NORTH);

    // Set window size and visibility
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(500, 500));
    pack();
    setVisible(true);

    // Start a thread to receive messages from server
    new Thread(new MessageReceiver()).start();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == sendButton || e.getSource() == messageField) {
      String message = messageField.getText().trim();
      if (!message.isEmpty()) {
        try {
          // Send message to server
          Date now = new Date();
          SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss"); // Adjust format as needed (e.g., HH:mm for
                                                                             // 24-hour format)
          String currentTime = timeFormatter.format(now);

          output.writeUTF("[ " + currentTime + " ~ " + username + " ] >> " + message);
          output.flush();
          messageField.setText(""); // Clear message field after sending
        } catch (IOException ex) {
          // Handle sending errors (e.g., display error message)
          ex.printStackTrace();
        }
      }
    }

    if (e.getSource() == usernameField) {
      username = usernameField.getText().trim();
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getSource() == messageField && e.getKeyCode() == KeyEvent.VK_ENTER) {
      // Handle Enter key press (same logic as actionPerformed)
      actionPerformed(new ActionEvent(messageField, ActionEvent.ACTION_PERFORMED, null));
    }
  }

  // Implement unused KeyListener methods (can be empty)
  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  // Implement updateChatHistory method to display messages
  private void updateChatHistory(String message) {
    // Append the message to the chat history text area
    chatHistory.append(message + "\n");
  }

  private class MessageReceiver implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          // Read message from server
          String message = input.readUTF();
          // Update chat history on the client side
          updateChatHistory(message);
        }
      } catch (IOException e) {
        // Handle errors or disconnection from server
        System.out.println("Disconnected from server");
      } finally {
        try {
          // Close resources when disconnected
          if (socket != null) {
            socket.close();
          }
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) {
    try {
      new ChatClient();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

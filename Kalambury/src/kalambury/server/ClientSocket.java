package kalambury.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import kalambury.sendableData.SendableData;

public class ClientSocket {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    
    public ClientSocket(Socket socket) throws IOException{
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream((socket.getOutputStream()));
    }
    
    public boolean hasDataToReceive() throws IOException{
        return inputStream.available() > 0;
    }
    
    public SendableData receive() throws IOException{
        return SendableData.receive(inputStream);
    }
    
    public void send(SendableData data) throws IOException{
        data.send(outputStream);
        outputStream.flush();
    }
}

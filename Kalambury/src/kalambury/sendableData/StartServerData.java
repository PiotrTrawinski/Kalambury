package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
;
import kalambury.mainWindow.Player;

public class StartServerData extends SendableData{
    public ObservableList<Player> players;
    public long time;
    
    public StartServerData(ObservableList<Player> players, long time){
        type = DataType.StartServerData;
        this.players = players;
        this.time = time;
    }
    
    public StartServerData(DataInputStream in){
        type = DataType.StartServerData;
        
        try {
            int numberOfPlayers = in.readInt();
            players = FXCollections.observableArrayList();
            for(int i = 0; i < numberOfPlayers; ++i){
                String nickName = in.readUTF();
                int score = in.readInt();
                int id = in.readInt();
                players.add(new Player(nickName, score,id));
            }
            time = in.readLong();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeInt(players.size());
            for(int i = 0; i < players.size(); ++i){
                out.writeUTF(players.get(i).getNickName());
                out.writeInt(players.get(i).getScore());
                out.writeInt(players.get(i).getId());
            }
            out.writeLong(time);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}

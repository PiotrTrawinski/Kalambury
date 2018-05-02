package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TurnEndedData extends SendableData{
    public ArrayList<Integer> updatedScores;
    public String winnerNickName;
    
    public TurnEndedData(ArrayList<Integer> updatedScores, String winnerNickName){
        type = DataType.TurnEndedData;
        this.updatedScores = updatedScores;
        this.winnerNickName = winnerNickName;
    }
    
    public TurnEndedData(DataInputStream in){
        type = DataType.TurnEndedData;
        try {
            int numberOfPlayers = in.readInt();
            updatedScores = new ArrayList<>();
            for(int i = 0; i < numberOfPlayers; ++i){
                updatedScores.add(in.readInt());
            }
            
            winnerNickName = in.readUTF();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            
            out.writeInt(updatedScores.size());
            for(int i = 0; i < updatedScores.size(); ++i){
                out.writeInt(updatedScores.get(i));
            }
            out.writeUTF(winnerNickName);
            
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
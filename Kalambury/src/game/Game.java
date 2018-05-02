package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import kalambury.mainWindow.Player;
import kalambury.mainWindow.TimeLabel;
import kalambury.sendableData.DataType;
import kalambury.sendableData.GamePasswordData;
import kalambury.sendableData.SendableSignal;
import kalambury.server.Server;


public class Game {
    private final int pointsForDrawing = 10;
    private final int pointsForGuess = 8;
    private final ObservableList<Player> players;
    private final List<Player> playersSequence = new ArrayList<>();
    private final int round;
    private final int maxPoints;
    private final int maxTimeSeconds;
    private String currentPassword;
    private int currentlyDrawingUserID;
    private final int NO_ID = -1;
    private final RandomFileReader randomGenerator = new RandomFileReader("slowa.txt",'\n');
    TimeLabel gameTimeLabel;
    
    long winnerTime = 0;
    int winnerId = NO_ID;
    
    public Game(int maxPlayers, int maxPoints, int maxTimeSeconds, ObservableList<Player> players){
        this.maxTimeSeconds = maxTimeSeconds;
        this.maxPoints = maxPoints;
        this.round = 0;
        this.players = players;
        this.currentlyDrawingUserID = NO_ID;
    }
    public void setTimeLabel(TimeLabel timeLabel){
        this.gameTimeLabel = timeLabel;
    }
    
    public void start(){
        System.out.println(players.size());
        for(int i = 0 ; i < players.size(); i++){
            playersSequence.add(players.get(i));
        }
        Collections.shuffle(playersSequence);

        chooseNextPlayer();
    }
    public void chooseNextPlayer(){
        
        // send stop drawing signal to last drawing player
        SendableSignal des = new SendableSignal(DataType.DrawingEndSignal);
        System.out.println(playersSequence.get(players.size()-1).getId());
        Server.sendToById(des, playersSequence.get(players.size()-1).getId());
        
        Player p = playersSequence.remove(0);
        playersSequence.add(p);
        currentlyDrawingUserID = p.getId();
        
        SendableSignal dss = new SendableSignal(DataType.DrawingStartSignal);
        Server.sendToById(dss, p.getId());

        currentPassword = randomGenerator.chooseRandom();
        
        GamePasswordData gpd = new GamePasswordData(currentPassword);
        Server.sendToById(gpd, p.getId());
        
        
        System.out.println("Current password is:"+currentPassword);
        
        System.out.println(p.getNickName() + " is now drawing.");
        
    }
    
    public boolean verifyPassword(String password, int guesserID){
        return password.equals(currentPassword) && guesserID != currentlyDrawingUserID;
    }
    
    public String endTurn(){
        players.get(Server.getPlayerIndex(currentlyDrawingUserID)).setScore(
                players.get(Server.getPlayerIndex(currentlyDrawingUserID)).getScore() + pointsForDrawing
        );
        players.get(Server.getPlayerIndex(winnerId)).setScore(
                players.get(Server.getPlayerIndex(winnerId)).getScore() + pointsForGuess
        );
        int tempWinnerId = winnerId;
        clearTurnWinner();
        //chooseNextPlayer();
        return players.get(Server.getPlayerIndex(tempWinnerId)).getNickName();
    }
    
    public void updateCurrentTurnWinner(long correctAnswerTime, int playerId){
        if(winnerId == NO_ID || correctAnswerTime < winnerTime){
            winnerId = playerId;
            winnerTime = correctAnswerTime;
        }
    }
    
    private void clearTurnWinner(){
        winnerId = NO_ID;
    }
}

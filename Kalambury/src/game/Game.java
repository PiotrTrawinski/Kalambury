package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import kalambury.mainWindow.Player;
import kalambury.mainWindow.TimeLabel;
import kalambury.server.Server;


public class Game {
    private final int pointsForDrawing = 10;
    private final int pointsForGuess = 8;
    private final ObservableList<Player> players;
    private final List<Integer> playersIdSequence = new ArrayList<>();
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
            playersIdSequence.add(players.get(i).getId());
        }
        Collections.shuffle(playersIdSequence);
    }
    public int chooseNextPlayer(){
        Integer pId = playersIdSequence.remove(0);
        playersIdSequence.add(pId);
        currentlyDrawingUserID = pId;
        return pId;
    }
    
    public boolean verifyPassword(String password, int guesserID){
        return password.equals(currentPassword) && guesserID != currentlyDrawingUserID;
    }
    
    public int getTurnTime(){
        return maxTimeSeconds;
    }
    
    public String chooseNextPassword(){
        currentPassword = randomGenerator.chooseRandom();
        return currentPassword;
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

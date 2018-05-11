package kalambury.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import kalambury.mainWindow.Player;
import kalambury.server.Server;


public class Game {
    private final int pointsForDrawing = 10;
    private final int pointsForGuess = 8;
    private final ObservableList<Player> players;
    private final List<Integer> playersIdSequence;
    private final int round;
    private final int numberOfFullTurns;
    private final int subTurnTime;
    private int currentSubTurn;
    private int currentTurn;
    private String currentPassword;
    private int currentlyDrawingUserID;
    private final int NO_ID = -1;
    private final RandomFileReader randomGenerator = new RandomFileReader("slowa.txt",'\n');
    
    long winnerTime;
    int winnerId = NO_ID;
    
    public Game(int numberOfFullTurns, int subTurnTime, ObservableList<Player> players){
        this.round = 0;
        this.numberOfFullTurns = numberOfFullTurns;
        this.subTurnTime = subTurnTime;
        this.players = players;
        currentlyDrawingUserID = NO_ID;
        playersIdSequence = new ArrayList<>();
        winnerTime = 0;
        currentSubTurn = 0;
        currentTurn = 0;
    }
    
    public void start(){
        for(int i = 0 ; i < players.size(); i++){
            playersIdSequence.add(players.get(i).getId());
        }
        Collections.shuffle(playersIdSequence);
        currentTurn = 1;
        currentSubTurn = 0;
    }
    public int chooseNextPlayer(){
        currentSubTurn++;
        if (currentSubTurn > players.size()){
            currentSubTurn = 1;
            currentTurn++;
        }
        if (currentTurn > numberOfFullTurns){
            // signal the end of game
            return -1;
        }
        
        Integer pId = playersIdSequence.remove(0);
        playersIdSequence.add(pId);
        currentlyDrawingUserID = pId;
        return pId;
    }
    public void removePlayerFromSequence(int id){
        for(int i = 0; i < playersIdSequence.size(); ++i){
            if(playersIdSequence.get(i) == id){
                playersIdSequence.remove(i);
                return;
            }
        }
    }
    
    public boolean verifyPassword(String password, int guesserID){
        return password.equals(currentPassword) && guesserID != currentlyDrawingUserID;
    }
    
    public int getTurnTime(){
        return subTurnTime;
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
    
    public int getDrawingPlayerId(){
        return currentlyDrawingUserID;
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

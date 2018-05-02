/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import kalambury.mainWindow.Player;
import kalambury.mainWindow.TimeLabel;
import kalambury.sendableData.DrawingEndSignal;
import kalambury.sendableData.DrawingStartSignal;
import kalambury.sendableData.GamePasswordData;
import kalambury.server.Server;

/**
 *
 * @author honzi
 */
public class Game {
    private final int pointsForDrawing = 10;
    private final int pointsForGuess = 8;
    private ObservableList<Player> players;
    private List<Player> playersSequence = new ArrayList<>();
    private int round;
    private int maxPoints;
    private int maxTimeSeconds;
    private int playersCount;
    private String currentPassword;
    private int currentlyDrawingUserID;
    private RandomFileReader randomGenerator = new RandomFileReader("slowa.txt",'\n');
    TimeLabel gameTimeLabel;
    
    long winnerTime = 0;
    int winnerId = -1;
    
    public Game(int maxPlayers, int maxPoints, int maxTimeSeconds, ObservableList<Player> players){

        this.maxTimeSeconds = maxTimeSeconds;
        this.maxPoints = maxPoints;
        this.round = 0;
        this.playersCount = players.size();
        this.players = players;
        this.currentlyDrawingUserID = -1;
    }
    public void setTimeLabel(TimeLabel timeLabel){
        this.gameTimeLabel = timeLabel;
    }
    
    public void start(){
        System.out.println(playersCount);
        for(int i = 0 ; i < playersCount; i++){
            playersSequence.add(players.get(i));
        }
        Collections.shuffle(playersSequence);

        chooseNextPlayer();
    }
    public void chooseNextPlayer(){
        
        // send stop drawing signal to last drawing player
        DrawingEndSignal des = new DrawingEndSignal();
        System.out.println(playersSequence.get(playersCount-1).getId());
        Server.sendTo(playersSequence.get(playersCount-1).getId(),des);
        
        Player p = playersSequence.remove(0);
        playersSequence.add(p);
        currentlyDrawingUserID = p.getId();
        
        DrawingStartSignal dss = new DrawingStartSignal();
        Server.sendTo(p.getId(), dss);

        currentPassword = randomGenerator.chooseRandom();
        
        GamePasswordData gpd = new GamePasswordData(currentPassword);
        Server.sendTo(p.getId(),gpd);
        
        
        System.out.println("Current password is:"+currentPassword);
        
        System.out.println(p.getNickName() + " is now drawing.");
        
    }
    
    public boolean verifyPassword(String password, int guesserID){
        return password.equals(currentPassword) && guesserID != currentlyDrawingUserID;
    }
    
    public String endTurn(){
        players.get(currentlyDrawingUserID).setScore(players.get(currentlyDrawingUserID).getScore() + pointsForDrawing);
        players.get(winnerId).setScore(players.get(winnerId).getScore() + pointsForGuess);
        int tempWinnerId = winnerId;
        clearTurnWinner();
        //chooseNextPlayer();
        return players.get(tempWinnerId).getNickName();
    }
    
    public void updateCurrentTurnWinner(long correctAnswerTime, int playerId){
        if(winnerId == -1 || correctAnswerTime < winnerTime){
            winnerId = playerId;
            winnerTime = correctAnswerTime;
        }
    }
    
    private void clearTurnWinner(){
        winnerId = -1;
    }
}

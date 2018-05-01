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
import kalambury.server.Server;

/**
 *
 * @author honzi
 */
public class Game {
    private ObservableList<Player> players;
    private List<Player> playersSequence = new ArrayList<>();
    private int round;
    private int maxPoints;
    private int maxTimeSeconds;
    private int playersCount;
    private String currentPassword;
    private int currentlyDrawingUserID;
    RandomFileReader randomGenerator = new RandomFileReader();
    
    public Game(int maxPlayers, int maxPoints, int maxTimeSeconds, ObservableList<Player> players){
        this.maxTimeSeconds = maxTimeSeconds;
        this.maxPoints = maxPoints;
        this.round = 0;
        this.playersCount = 0;
        this.players = players;
        this.currentlyDrawingUserID = -1;
    }
    public void start(){
        for(int i = 0 ; i < playersCount; i++){
            playersSequence.add(players.get(i));
        }
        Collections.shuffle(playersSequence);

    }
    public void chooseNextPlayer(){
        
    }
}

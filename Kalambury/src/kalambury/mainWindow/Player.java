package kalambury.mainWindow;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Player {
    private final SimpleStringProperty nickName;
    private final SimpleIntegerProperty score;
    private final int id;
    
    public Player(String nickName, int score, int id){
        this.nickName = new SimpleStringProperty(nickName);
        this.score = new SimpleIntegerProperty(score);
        this.id = id;
    }
    
    public String getNickName(){
        return nickName.get();
    }
    public int getId(){
        return id;
    }
    public void setNickName(String nickName){
        this.nickName.set(nickName);
    }
    public SimpleStringProperty getNickNameProperty(){
        return nickName;
    }
    
    public Integer getScore(){
        return score.get();
    }
    public void setScore(int score){
        this.score.set(score);
    }
    public SimpleIntegerProperty getScoreProperty(){
        return score;
    }
}

package kalambury.mainWindow;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Player {
    private final SimpleStringProperty nickName;
    private final SimpleIntegerProperty score;
    private final SimpleBooleanProperty isDrawing;
    
    public Player(String nickName, int score){
        this.nickName = new SimpleStringProperty(nickName);
        this.score = new SimpleIntegerProperty(score);
        this.isDrawing = new SimpleBooleanProperty(false);
    }
    
    public String getNickName(){
        return nickName.get();
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
    
    public Boolean getIsDrawing(){
        return isDrawing.get();
    }
    public void setIsDrawing(Boolean isDrawing){
        this.isDrawing.set(isDrawing);
    }
    public SimpleBooleanProperty getIsDrawingProperty(){
        return isDrawing;
    }
}

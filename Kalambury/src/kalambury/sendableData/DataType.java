package kalambury.sendableData;

public enum DataType {
    Unknown(-1),
    LineDraw(0),
    FloodFill(1),
    ChatMessage(2),
    StartServerData(3),
    NewPlayerData(4),
    Time(5),
    GamePassword(8),
    TurnEndedSignal(9),
    TurnEndedAcceptSignal(10),
    TurnEndedData(11),
    TurnStarted(12),
    GameStoppedSignal(13),
    GameStarted(14),
    TurnSkippedSignal(15),
    SkipRequest(16),
    PlayerQuit(17),
    TimeAcceptSignal(18),
    GameEndedSignal(19),
    GamePausedSignal(20),
    TurnTimeOut(21),
    JoinRequestSignal(22),
    JoinAcceptSignal(23),
    JoinRejectSignal(24);
   
    private final int value;
    private DataType(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static DataType fromInt(int value){
        for (DataType type : DataType.values()) {
            if(type.toInt() == value){
                return type;
            }
        }
        return DataType.Unknown;
    }
}

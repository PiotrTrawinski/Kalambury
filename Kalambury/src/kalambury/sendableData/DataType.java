package kalambury.sendableData;

public enum DataType {
    Unknown(-1),
    LineDraw(0),
    FloodFill(1),
    ChatMessage(2),
    StartServerData(3),
    NewPlayerData(4),
    Time(5),
    DrawingEndSignal(6),
    DrawingStartSignal(7),
    TurnEndedSignal(8),
    TurnEndedAcceptSignal(9),
    TurnEndedData(10);
   
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

package kalambury;

public enum DataType {
    Unknown(-1),
    LineDraw(0),
    FloodFill(1),
    ChatMessage(2);
    
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

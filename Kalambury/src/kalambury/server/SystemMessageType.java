package kalambury.server;

public enum SystemMessageType {
    Unknown(-1),
    Information(0),
    Error(1);
    
    private final int value;
    private SystemMessageType(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static SystemMessageType fromInt(int value){
        for (SystemMessageType type : SystemMessageType.values()) {
            if(type.toInt() == value){
                return type;
            }
        }
        return SystemMessageType.Unknown;
    }
}

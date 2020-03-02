package chipmunk.modules.uuid;

public class UUIDSupport {
    private static final UUIDCreateRandom create = new UUIDCreateRandom();
    private static final UUIDFromString fromStr = new UUIDFromString();
    private static final UUIDToString toStr = new UUIDToString();

    public static UUIDCreateRandom createRandomUUID(){
        return create;
    }

    public static UUIDFromString uuidFromString(){
        return fromStr;
    }

    public static UUIDToString uuidToString(){
        return toStr;
    }
}

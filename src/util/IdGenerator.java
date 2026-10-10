package util;

import java.util.UUID;

public class IdGenerator {
    public static String generateID(String prefix){
        return prefix+" "+ UUID.randomUUID().toString();
    }
}

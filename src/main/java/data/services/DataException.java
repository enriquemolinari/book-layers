package data.services;

public class DataException extends RuntimeException {
    public DataException(String msg, Exception e) {
        super(msg, e);
    }

    public DataException(String msg) {
        super(msg);
    }
}

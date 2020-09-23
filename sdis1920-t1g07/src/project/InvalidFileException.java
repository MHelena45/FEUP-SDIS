package project;

public class InvalidFileException extends Exception {

    public InvalidFileException(String error_message) {
        System.out.println(error_message);
    }
}

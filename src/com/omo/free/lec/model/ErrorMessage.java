package com.omo.free.lec.model;

/**
 * Class used to hold the error messages captured by the application.
 *
 * @author Richard Salas
 */
public class ErrorMessage {

    private String dirOrFilePath;
    private String message;

    /**
     * Constructor used to create an instance of the ErrorMessage class.
     * @param dirOrFilePath
     * @param message
     */
    public ErrorMessage(String dirOrFilePath, String message){
        this.dirOrFilePath = dirOrFilePath;
        this.message = message;
    }//end constructor

    /**
     * @return the dirOrFilePath
     */
    public String getDirOrFilePath() {
        return dirOrFilePath;
    }

    /**
     * @param dirOrFilePath the dirOrFilePath to set
     */
    public void setDirOrFilePath(String dirOrFilePath) {
        this.dirOrFilePath = dirOrFilePath;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorMessage [dirOrFilePath=");
        builder.append(dirOrFilePath);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }//end to string

}//end class

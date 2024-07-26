package gov.doc.isu.lec.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to encapsulate the exceptions found per cluster or application.
 *
 * @author Richard Salas
 */
public class ExceptionModel {

    private String type;
    private int logCount;
    private List<ErrorMessage> errors;
    private int totalExceptionCount;
    private String clusterOrApplicationName;
    private Map<String, Integer> exceptionMap;

    /**
     * Construct used to create an instance of this class
     */
    public ExceptionModel(){
        this.errors = new ArrayList<>();
        exceptionMap = new HashMap<>();
    }//end constructor

    /**
     * This method will add an error message to the list of errors.
     * @param sharedDirPath the shared path
     * @param message the message
     */
    public synchronized void addErrorMessage(String sharedDirPath, String message) {
        errors.add(new ErrorMessage(sharedDirPath, message));
    }//end method

    /**
     * @return the errors
     */
    public List<ErrorMessage> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<ErrorMessage> errors) {
        this.errors = errors;
    }

    /**
     * @return the totalExceptionCount
     */
    public int getTotalExceptionCount() {
        return totalExceptionCount;
    }

    /**
     * @return the clusterOrApplicationName
     */
    public String getClusterOrApplicationName() {
        return clusterOrApplicationName;
    }

    /**
     * @param clusterOrApplicationName the clusterOrApplicationName to set
     */
    public void setClusterOrApplicationName(String clusterOrApplicationName) {
        this.clusterOrApplicationName = clusterOrApplicationName;
    }

    /**
     * @return the exceptionMap
     */
    public Map<String, Integer> getExceptionMap() {
        return exceptionMap;
    }

    /**
     * @param exceptionMap the exceptionMap to set
     */
    public void setExceptionMap(Map<String, Integer> exceptionMap) {
        this.exceptionMap = exceptionMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExceptionModel [errors=");
        builder.append(errors);
        builder.append(", totalExceptionCount=");
        builder.append(totalExceptionCount);
        builder.append(", clusterOrApplicationName=");
        builder.append(clusterOrApplicationName);
        builder.append(", exceptionMap=");
        builder.append(exceptionMap);
        builder.append("]");
        return builder.toString();
    }//end method

    public void addException(String exceptionClass) {
        if(exceptionMap.containsKey(exceptionClass)){
            exceptionMap.put(exceptionClass, exceptionMap.get(exceptionClass) + 1);
        }else{
            exceptionMap.put(exceptionClass, 1);
        }//end if...else
        totalExceptionCount++;
    }//end method

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }//end method

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }//end method

    /**
     * @return the logCount
     */
    public int getLogCount() {
        return logCount;
    }//end method

    public void incrementLogCount(){
        this.logCount++;
    }//end method

}//end class

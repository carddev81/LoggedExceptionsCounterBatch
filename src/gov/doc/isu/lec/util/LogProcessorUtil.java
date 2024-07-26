package gov.doc.isu.lec.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.doc.isu.lec.file.DeleteFileVisitor;

/**
 * Utility class to encapsulate the utility methods used by the log processor application
 *
 * @author Richard Salas
 */
public class LogProcessorUtil {

    private static final String MY_CLASS_NAME = "gov.doc.isu.lec.file.DeleteFileVisitor";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /**
     * This method will delete a directory from the bottom up.
     *
     * @param rootDirectory
     *        the root directory to start deleting
     */
    public static void deleteDirectory(File rootDirectory) {
        myLogger.entering(MY_CLASS_NAME, "deleteDirectory", rootDirectory);
        try{
            if(rootDirectory != null && rootDirectory.exists()){
                Files.walkFileTree(rootDirectory.toPath(), new DeleteFileVisitor());
            }else{
                myLogger.warning("Directory does not exist therefore will not be attempted to be deleted.  rootDirectory=" + rootDirectory != null ? rootDirectory.getPath() : "null");
            }//end if...else
        }catch(IOException e){
            myLogger.log(Level.SEVERE, "IOException was caught while trying to close the Buffered Reader. Message is: " + e.getMessage(), e);
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "deleteDirectory");
    }// end deleteFile
}

package gov.doc.isu.lec.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to delete the contents of an entire directory.
 *
 * @author Richard Salas
 */
public class DeleteFileVisitor extends SimpleFileVisitor<Path>{

    private static final String MY_CLASS_NAME = "gov.doc.isu.lec.file.DeleteFileVisitor";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    /**
     * Creates an instance of the DeleteFileVisitor class
     */
    public DeleteFileVisitor() {
        //default constructor
    }//end constructor

    /**
     * Invoked for a file in a directory.
     *
     * <p>Visits all files within a directory and will attempt to delete the file.  A delete of all files is done here.</p>
     *
     * @param file the path
     * @param attrs the attributes to file
     *
     * @return file visit result value CONTINUE
     */
    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "visitFile", new Object[]{file, attrs});
        Files.delete(file);
        myLogger.exiting(MY_CLASS_NAME, "visitFile", FileVisitResult.CONTINUE);
        return FileVisitResult.CONTINUE;
    }//end method

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     *
     * <p>Visits a directory after all entries within it have been visited therefore a delete on the top level directory is made because all files within it should have been deleted.</p>
     *
     * @param dir the directory path
     * @param exc the exception
     *
     * @return file visit result value CONTINUE unless exception is not null
     */
    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        myLogger.entering(MY_CLASS_NAME, "visitFile", new Object[]{dir, exc});

        if(exc != null){
            myLogger.log(Level.SEVERE, "IOException occurred within the postVisitDirectory therefore there was an issue deleting a file.  Error message is: " + exc.getMessage(), exc);
            throw exc;
        }//end if

        Files.delete(dir);
        myLogger.exiting(MY_CLASS_NAME, "visitFile", FileVisitResult.CONTINUE);
        return FileVisitResult.CONTINUE;
    }//end method

}//end class

package com.omo.free.lec.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class handles filtering the logs requested by user.
 *
 * @author Richard Salas
 */
public class LogPathVisitor implements FileVisitor<Path>{

    private static final String MY_CLASS_NAME = "com.omo.free.lec.file.LogPathVisitor";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    // variables used by this file visitor
    private List<Path> logFiles = new ArrayList<>();
    private List<String> logPrefixes;
    private String date;
    private boolean currentDay;

    /**
     * Contructor used to instantiate an instance of {@code LogPathVisitor}
     *
     * @param logPrefixes the log prefixes to search for
     * @param date the date of the logs to search for
     * @param currentDay is the current day
     */
    public LogPathVisitor(List<String> logPrefixes, String date, boolean currentDay){
        this.logPrefixes = logPrefixes;
        this.date = date;
        this.currentDay = currentDay;
    }//end constructor

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a file in a directory and will add logging paths that meet the criteria into the {@code logFiles} list of paths.
     *
     * @param   file
     *          a reference path to the file
     * @param   attrs
     *          the file's basic attributes
     *
     * @return  the visit result
     *
     * @throws  IOException
     *          if an I/O error occurs
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        boolean prefix = containsPrefix(file);
        if(prefix && passedDatedFilter(file)){
            logFiles.add(file);
        }else{
            //TODO put this at a level that is higher than info once we get a good test
            myLogger.info("SKIPPING FILE:  " + String.valueOf(file));
        }//end if
        return FileVisitResult.CONTINUE;
    }//end method

    /**
     * This method is used to filter the log name by date
     * @param file the path to the file
     * @return true or false on whether or not it passes the test.
     */
    private boolean passedDatedFilter(Path file) {
        boolean pass = false;
        if(file.getFileName().toString().contains(date)){
            pass = true;
        }else if(currentDay && isProperLogName(file)){
            pass = true;
        }//end if
        return pass;
    }//end method

    /**
     * Checks to see if the file meets the proper naming standards
     * @param file the path to the file
     * @return true or false on whether or not it passes the test.
     */
    private boolean isProperLogName(Path file) {
        boolean proper = false;
        Iterator<String> prefixIt = logPrefixes.iterator();
        while(prefixIt.hasNext()){
            String prefix = prefixIt.next();
            //could use the file last modified but no need to
            if(file.getFileName().toString().startsWith(prefix) && !file.getFileName().toString().contains("_")){//if log file does not contain an underscore then the file should be an active log
                proper = true;
                break;
            }//end if
        }//end while
        return proper;
    }//end method

    /**
     * Checks to see if the method contains the log prefix.
     * @param file the path to the file
     * @return true or false on whether or not it passes the test.
     */
    private boolean containsPrefix(Path file) {
        boolean containsPrefix = false;
        Iterator<String> prefixIt = logPrefixes.iterator();
        while(prefixIt.hasNext()){
            //\\sdwmsfsp4136.state.mo.us\DOCApps\Prod\Private\Logs\WebSphere\DOCProdMOCISPrivV8Node4540\App_Logs\doc\apps\mocis\logs\Error.MOCIS-P4540.log.2017-11-01_1.log
            String prefix = prefixIt.next();
            if(file.getFileName().toString().startsWith(prefix)){
                myLogger.info("FOUND PREFIX:  " + String.valueOf(file));
                containsPrefix = true;
                break;
            }//end if
        }//end while
        return containsPrefix;
    }//end method

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.TERMINATE;
    }//end method

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }//end method

    /**
     * This method returns the logging paths
     * @return logFiles the logging paths
     */
    public List<Path> getLoggingPaths() {
        return logFiles;
    }//end method

}//end class

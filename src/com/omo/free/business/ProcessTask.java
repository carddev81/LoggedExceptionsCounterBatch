package com.omo.free.business;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.omo.free.lec.file.LogPathVisitor;
import com.omo.free.lec.model.ExceptionModel;
import com.omo.free.lec.model.LogPath;
import com.omo.free.lec.util.AppConstants;

/**
 * This class is used for processing application log files looking for exceptions.
 *
 * @author Richard Salas
 *
 */
public class ProcessTask implements Callable<ExceptionModel>{

    private static final String MY_CLASS_NAME = "com.omo.free.business.ProcessTask";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private Pattern pattern;
    private Pattern tabPattern;
    private LogPath logPath;
    private String date;
    boolean isCurrentDay;

    /**
     * Constructor used to build an instance of the ProcessTask.
     * @param logPath the logging path instance used to determine the
     * @param date the date
     * @param isCurrentDay is this the current day or not
     */
    public ProcessTask(LogPath logPath, String date, boolean isCurrentDay){
        myLogger.entering(MY_CLASS_NAME, "ProcessTask", new Object[]{logPath, date, isCurrentDay});
        this.logPath = logPath;
        this.date = date;
        this.isCurrentDay = isCurrentDay;
        //must be at least a period  ((?<!\t|at)[a-z.]+\\.[a-zA-Z]+(Exception|Error))
        //()
        tabPattern = Pattern.compile("\t");
        pattern = Pattern.compile("([a-zA-Z0-9.]+\\.[0-9a-zA-Z]+(Exception|Error))");//exception extractor
        myLogger.exiting(MY_CLASS_NAME, "ProcessTask");
    }//end method

    /**
     * This method will execute the processing logic for scanning log files for exceptions.
     * @return eModel the exception model
     */
    @Override
    public ExceptionModel call() throws Exception {
        myLogger.entering(MY_CLASS_NAME, "call");
        ExceptionModel eModel = new ExceptionModel();
        eModel.setClusterOrApplicationName(logPath.getName());
        eModel.setType(logPath.getType());
        try{
            List<Path> sharedLoggingPaths = new ArrayList<>();
            List<String> fullDirectories = logPath.getPaths();

            //LOOP THROUGH EACH INDIVIDUAL DIRECTORY AND COPY AND GATHER LOG FILES
            Iterator<String> it = fullDirectories.iterator();
            while(it.hasNext()){

                Path sharedDirPath = Paths.get(it.next());
                if(Files.exists(sharedDirPath)){
                    //start copy and
                    LogPathVisitor lpv = new LogPathVisitor(logPath.getLogPrefixes(), date, isCurrentDay);
                    Files.walkFileTree(sharedDirPath, Collections.emptySet(), 1, lpv);
                    if(lpv.getLoggingPaths().isEmpty()){
                        eModel.addErrorMessage(sharedDirPath.toString(), "No Logs found per search criteria.  LogPrefixes=" + String.valueOf(logPath.getLogPrefixes()));
                    }else{
                        sharedLoggingPaths.addAll(lpv.getLoggingPaths());
                    }//end if...else
                }else{
                    eModel.addErrorMessage(sharedDirPath.toString(), "Directory does not exist.");
                }//end if...else
            }//end while

            //COPY LOG FILES LOCALLY FOR PROCESSING
            List<Path> localLoggingPaths = new ArrayList<>();

            myLogger.info("Number of logging files that are going to be processed for " + logPath.getName() + " are: " + sharedLoggingPaths.size());
            sharedLoggingPaths.parallelStream().forEach(sourcePath -> {
                
                Path targetPath = null;
                try{
                    if("server".equals(logPath.getType())){
                        String dirName = "jccc".equals(logPath.getEnvironment()) ? logPath.getName() : sourcePath.getParent().toFile().getName();
                        targetPath = Paths.get(AppConstants.WORK_DIR, dirName, sourcePath.getFileName().toString());
                        myLogger.info("Complete local path to copy log file to is: " + String.valueOf(targetPath));
                    }else{
                        targetPath = Paths.get(AppConstants.WORK_DIR, logPath.getName(), sourcePath.getFileName().toString());
                        myLogger.info("Complete local path to copy log file to is: " + String.valueOf(targetPath));
                    }//end if...else

                    if(!Files.exists(targetPath)){//create directories here if they do not exist
                        Files.createDirectories(targetPath.getParent());
                    }//end if

                    retryAndWaitIfNeeded(sourcePath, targetPath);//added this 2022
                    localLoggingPaths.add(targetPath);
                }catch(IOException e1){
                    myLogger.log(Level.SEVERE, "Exception occurred while trying to copy. Error is " + e1.getMessage(), e1);
                    eModel.addErrorMessage(sourcePath.toString(), "Error copying log file.  Message is: " + e1.getMessage());
                }catch(Exception e){
                    eModel.addErrorMessage(sourcePath.toString(), "Error copying log file.  Message is: " + e.getMessage());
                    myLogger.log(Level.SEVERE, "Exception occurred while trying to copy. Error is " + e.getMessage(), e);
                }//end try...catch
            });

            Iterator<Path> logsToProcessIt = localLoggingPaths.iterator();
            while(logsToProcessIt.hasNext()){
                processLog(logsToProcessIt.next(), eModel);
            }//end while
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurrred somewhere in processing", e);
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "call", eModel);
        return eModel;
    }//end method

    /**
     * This method will run a retry of copying a file if it fails.  This happens because the files have locks due to copysync software being ran on state network folders.
     *  
     * @param sourcePath the source file to copy
     * @param targetPath the destination file
     * @throws Exception can occur when all retries have failed
     */
    private void retryAndWaitIfNeeded(Path sourcePath, Path targetPath) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "retryAndWaitIfNeeded", new Object[]{sourcePath, targetPath});
        int retries = 0;
        while(retries<15){
            try{
                if(retries>0){
                    TimeUnit.SECONDS.sleep(10);//retry ever 10 seconds
                }//end if
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                break;
            }catch(IOException e1){
                myLogger.log(Level.SEVERE, "Exception occurred while trying to copy. Error is " + e1.getMessage(), e1);
                if(retries==14){
                    throw e1;
                }//end if
                myLogger.warning("Number of retries: " + retries + "; copying " + String.valueOf(sourcePath));
                retries++;
            }catch(Exception e){
                myLogger.log(Level.SEVERE, "Exception occurred while trying to copy. Error is " + e.getMessage(), e);
                if(retries==14){
                    throw e;
                }//end if
                myLogger.warning("Number of retries: " + retries + "; copying " + String.valueOf(sourcePath));
                retries++;
            }//end try...catch
        }//end while
        myLogger.exiting(MY_CLASS_NAME, "retryAndWaitIfNeeded");
    }//end method

    /**
     * This method will read the contents of the file looking for the exceptions and then processing them.
     *
     * @param log the log file to read
     * @param eModel the model to add the exceptions found to
     */
    private void processLog(Path log, ExceptionModel eModel) {
        myLogger.entering(MY_CLASS_NAME, "processLog", new Object[]{log, eModel});
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(log.toFile()));
            String line = br.readLine();
            eModel.incrementLogCount();
            while(line!=null){
                Matcher matcher = tabPattern.matcher(line);//line.matches("^([^\t].*)([a-z]+\\.[a-zA-Z.]*(Exception|Error))(.*)$")
                if(!matcher.find()){
                    proccessLine(line, eModel);
                }//end if
                line = br.readLine();
            }//end while
        }catch(FileNotFoundException e){
            myLogger.log(Level.SEVERE, "FileNotFoundException occurrred while reading file.", e);
            eModel.addErrorMessage(log.toString(), "Problem reading file.  Message is: " + e.getMessage());
        }catch(IOException e){
            myLogger.log(Level.SEVERE, "IOException occurrred while reading file.", e);
            eModel.addErrorMessage(log.toString(), "Problem reading file.  Message is: " + e.getMessage());
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurrred somewhere in processing while reading file.", e);
            eModel.addErrorMessage(log.toString(), "Problem reading file.  Message is: " + e.getMessage());
        }finally{
            if(br != null){
                try{
                    br.close();
                }catch(IOException e){
                    myLogger.log(Level.SEVERE, "IOException occurrred somewhere in processing.  Error message is: " + e.getMessage(), e);
                }//end try...catch
            }//end if
        }//end try...catch....finally
        myLogger.entering(MY_CLASS_NAME, "processLog", new Object[]{log, eModel});
    }//end method

    /**
     * Process the exception line, looking for any exceptions contained on the line.
     * @param aLine the line
     * @param eModel the model used to capture the exception.
     */
    private void proccessLine(String aLine, ExceptionModel eModel) {
        Matcher matcher = pattern.matcher(aLine);
        if(matcher.find()){
            if(aLine.contains("Saving message key '.errors")){//quick shamen fix here
                return;
            }//end if
            eModel.addException(matcher.group().trim());
        }//end if
    }//end method

}//end class

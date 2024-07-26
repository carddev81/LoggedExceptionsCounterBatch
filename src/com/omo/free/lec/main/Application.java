package com.omo.free.lec.main;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.omo.free.business.LoggedExceptionController;
import com.omo.free.lec.util.AppConstants;
import com.omo.free.lec.util.LogProcessorUtil;

import gov.doc.isu.gtv.core.AbstractApplication;
import gov.doc.isu.gtv.core.UserInterface;
import gov.doc.isu.gtv.core.ifc.IEmailable;
import gov.doc.isu.gtv.email.EmailSender;
import gov.doc.isu.gtv.exception.EmailException;
import gov.doc.isu.gtv.exception.PrepareException;
import gov.doc.isu.gtv.logging.ApplicationLogger;
import gov.doc.isu.gtv.managers.LoggingMgr;
import gov.doc.isu.gtv.managers.PropertiesMgr;
import gov.doc.isu.gtv.model.CustomProperties;
import gov.doc.isu.gtv.model.ProgramDetail;
import gov.doc.isu.gtv.util.ApplicationConstants;
import gov.doc.isu.gtv.util.ApplicationUtil;
import gov.doc.isu.gtv.util.DateUtil;

/**
 * Application will scan an entire web environments logging files scanning them for exceptions that will be used to generate a report and send as attachment to email.
 *
 * @author Richard salas
 */
public class Application extends AbstractApplication implements IEmailable{

    /**
     *
     */
    private static final long serialVersionUID = -7061289873706315892L;
    private static final String MY_CLASS_NAME = "com.omo.free.lec.main.Application";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    private static ApplicationLogger emailLogger;
    private boolean error;
    private static long start;
    private static String end;

    /**
     * Overloaded constructor is the entry point for enabling the George framework.
     *
     * @param args
     *        String[] of arguments from the command line.
     * @throws PrepareException
     *         An exception thrown when the executor has included the PREPARE argument from the command line.
     */
    public Application(String[] args) throws PrepareException {
        super(args);
    }// end constructor

    /**
     * Main method initiates the logging capabilities and calls the run().
     *
     * @param args
     *        The arguments passed in from applicationStart.bat.
     */
    public static void main(String[] args) {
        try{
            start = System.currentTimeMillis();
            System.out.println("Application is initializing...");

            new Application(args);
            myLogger.info("George Framework has been enabled. Calling the controlling class for the application.");
            end = DateUtil.asTime(System.currentTimeMillis() - start);
            myLogger.info("Controlling class for the application has completed. Running time is " + end);
        }catch(Exception e){
            System.err.println("Exception caught in main! Message is: " + e.getMessage());
            System.exit(1);
        }// end try/catch
    }// end main

    /**
     * This method creates an instance of
     */
    public void run() {
        emailLogger = ApplicationLogger.getInstance();
        myLogger.entering(MY_CLASS_NAME, "run() method - makes a call to the controller class's run method for starting the batch process.");
        try{
            myLogger.log(Level.ALL, "Running LoggedExceptionsProcessorApplication...");
            LogProcessorUtil.deleteDirectory(new File(AppConstants.WORK_DIR));//attempt a delete on start
            if(getArguments().length > 0){
                String[] arguments = validateAndParseArguments();
                LoggedExceptionController.getInstance().run(arguments);
            }else{
                myLogger.info("Using default settings of yesterdays date and the production environment");
                LoggedExceptionController.getInstance().run();
            }//end else...if
            //commented this line out in case we need particular log files for the dayLogProcessorUtil.deleteDirectory(new File(AppConstants.WORK_DIR));//attempt a delete on finish

            myLogger.log(Level.ALL, "LoggedExceptionsProcessorApplication complete.");
            /* Your code should have completed by the time you are here */
            emailCompletionMsg();
        }catch(UnsupportedClassVersionError e){
            error = true;
            LoggingMgr.getInstance().setAllLoggersForOneCycle(Level.CONFIG);
            myLogger.log(Level.SEVERE, "General Exception caught in run, message is " + e.getMessage(), e);
        }catch(Exception e){
            LoggingMgr.getInstance().setAllLoggersForOneCycle(Level.CONFIG);
            error = true;
            //<p>Logged Exceptions Counter Batch Application has run successfully.
            emailLogger.info("<p>Logged Exceptions Counter Batch Application failed to run.</p> <p><font color=\"red\"><b>ERROR:</font> </b> Error occurred while trying to scan logging files for exceptions.  The exception message is: <br/><br/><font color=\"red\">" + e.getMessage() + "</font></p>");
            myLogger.log(Level.SEVERE, "General Exception caught in run, message is " + e.getMessage(), e);
        }// end try/catch
        try{
            emailSystemAdministrators(null);
            LogProcessorUtil.deleteDirectory(new File(AppConstants.WORK_DIR));//attempt a delete on start
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exeption occurred while trying to delete workspace.  No bigge!  Error is: " + e.getMessage(), e);
        }//end try..catch
        myLogger.exiting(MY_CLASS_NAME, "run() method");
    } // end run

    /**
     * This method is used to validate and parse arguments passed into this application.
     * @return a string array of parsed values used by the application
     */
    private String[] validateAndParseArguments() {
        myLogger.entering(MY_CLASS_NAME, "validateAndParseArguments() method");
        String[] parsedArgs = null;
        if(getArguments().length == 1){
            parsedArgs = new String[1];
            parsedArgs[0] = getArguments()[0].toLowerCase().trim();
        }else if(getArguments().length == 2){
            parsedArgs = new String[2];
            parsedArgs[0] = getArguments()[0].toLowerCase().trim();
            parsedArgs[1] = validateDate(getArguments()[1].trim());
        }else{
            throw new IllegalArgumentException("Invalid number of arguments passed into the application!  There must be 2:  &lt;ENVIRONMENT&gt; &lt;DATE&gt;");
        }//end if...else

        if(!parsedArgs[0].matches("(production|jccc|test)")){
            throw new IllegalArgumentException("Invalid environment specified!  Must be either production or jccc");
        }//end if

        myLogger.exiting(MY_CLASS_NAME, "validateAndParseArguments() method", parsedArgs);
        return parsedArgs;
    }//end method

    /**
     * Validates the date being passed in.
     * @param date
     * @return string date
     */
    private String validateDate(String date) {
        myLogger.entering(MY_CLASS_NAME, "validateDate() method", date);
        String returnDate = null;
        if("yesterday".equals(getArguments()[1].toLowerCase().trim())){
            returnDate = LocalDate.now().minusDays(1).toString();
        }else{
            try{
                DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
                LocalDate theDate = (LocalDate) dtf.parse(getArguments()[1].trim(), LocalDate::from);
                returnDate = theDate.toString();
            }catch(Exception e){
                myLogger.log(Level.SEVERE, "Exception trying to parse date that was passed in by the user.  Error Message is: " + e.getMessage(), e);
                throw new IllegalArgumentException("Invalid date format!  Must be in YYYY-MM-DD format");
            }//end try...catch
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "validateDate() method", returnDate);
        return returnDate;
    }//end method

    /**
     * Method called to email the system administrator at the completion of the process.
     */
    private void emailCompletionMsg() {
        myLogger.entering(MY_CLASS_NAME, "emailCompletionMsg() method");
        LoggedExceptionController controller = LoggedExceptionController.getInstance();
        
        ProgramDetail details = new ProgramDetail();
        Object[][] tableData = new Object[1][4];
        tableData[0] = new Object[]{String.valueOf(controller.getAppLogDt()), controller.getEnvironment(), controller.getTotalScannedLogs(), DateUtil.asTime(System.currentTimeMillis() - start)};
        
        StringBuilder headerMessage = new StringBuilder();
        if(controller.getTotalScannedLogs() == 0){
            headerMessage.append("<p>Logged Exceptions Counter Batch Application has ran successfully.  No log files were located.  Please view the ErrorPage within the resulting spreadsheet attached to this email for possible reasons that no log files were found.</p>");
        }else{
            headerMessage.append("<p>Logged Exceptions Counter Batch Application has ran successfully.  The resulting spreadsheet is attached to this email.</p><p>The following is a summary of events from the application: </p>");
        }//end if...else
        
        details.setEmailHeaderMessage(headerMessage.toString());
        details.setEmailTableIntroMessage("The following table provides the runtime details of the batch job.");
        details.setEmailTable(new String[]{"Date Of Logs", "Environment", "Number Of Logs Scanned", "Process Time"}, tableData, 1);
        details.setEmailFooterMessage("<p>Thank you very much and have a nice day.</p>");
        details.setEmailErrorFooterMessage("");
        emailLogger.info(details.getEmailContents());
        myLogger.exiting(MY_CLASS_NAME, "emailCompletionMsg() method");
    }// end method

    /**
     * This method was overriden so that logging files are not sent through to the email as logs may contain sensitive data.
     *
     * @param addresses {@link String} The addresses to send the e-mail to. If this value is <code>null</code> or blank then the email.to property in the external configuration file will be used.
     */
    @Override
    protected void emailSystemAdministrators(String addresses) {
        myLogger.entering(MY_CLASS_NAME, "emailSystemAdministrators");
        StringBuffer message = new StringBuffer();
        ApplicationLogger.getInstance().close();
        List<String> lines = ApplicationLogger.getInstance().getLogContents();
        for(int i = 0, j = lines.size();i < j;i++){
            if(!ApplicationUtil.isNullOrEmpty(lines.get(i))){
                message.append(lines.get(i));
            }// end if
        }// end for

        LoggedExceptionController controller = LoggedExceptionController.getInstance();

        try{
            if(error){
                EmailSender.send(message.toString());
            }else if(Boolean.valueOf(PropertiesMgr.getProperties().getProperty("sendemail")) && controller.getSpreadSheetDestFile() != null && controller.getSpreadSheetDestFile().exists()){
                EmailSender.send(new File[]{controller.getSpreadSheetDestFile()}, addresses, message.toString());
            }//end if
        }catch(EmailException e){
            myLogger.log(Level.SEVERE, "EmailException while sending system admin message. Message is: " + e.getMessage(), e);
            return;
        }// end try/catch
        myLogger.exiting(MY_CLASS_NAME, "emailSystemAdministrators");
    }// end emailSystemAdministrators

    /**
     * This method is used to execute logic after the run() method has finished to send admin email and to also clean up unused logs.
     *
     * @throws Exception
     *         thrown when something unexpected goes wrong
     */
    @Override
    protected void postprocess() throws Exception {
        myLogger.entering(MY_CLASS_NAME, "postprocess method - closing resources");
        cleanupLogs();
        myLogger.exiting(MY_CLASS_NAME, "postprocess method");
    }// end method

    /**
     * This method is overriden due to the original implementation deleting application problem loggers that may be needed. This implementation will only delete george specific loggers.
     */
    @Override
    protected void cleanupLogs() {
        myLogger.entering(MY_CLASS_NAME, "cleanupLogs");
        super.closeLoggers();
        // Handle clean up.
        String path = LoggingMgr.getProperties().getProperty("java.util.logging.FileHandler.pattern");
        path = path.substring(0, path.lastIndexOf(ApplicationConstants.SLASH));
        File directory = new File(path);
        if(directory.exists()){
            File[] logs = directory.listFiles();
            for(int i = 0, j = logs.length;i < j;i++){
                if(logs[i].getName().contains("CSV") || logs[i].getName().endsWith(".lck") || logs[i].getName().contains("Application")){
                    if(logs[i].delete()){
                        myLogger.info("DELETED logging file: " + logs[i].getAbsolutePath());
                    }else{
                        myLogger.info("UNABLE TO DELETE logging file: " + logs[i].getAbsolutePath());
                    }// end if/else
                }// end if
            }// end for
        }// end if
        myLogger.exiting(MY_CLASS_NAME, "cleanupLogs");
    }// end cleanupLogs

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.AbstractApplication#getApplicationName()
     */
    @Override
    public String getApplicationName() {
        return "LoggedExceptionsCounterBatch";
    }// end getApplicationName

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.AbstractApplication#getAdditionalApplicationProperties()
     */
    @Override
    protected CustomProperties getAdditionalApplicationProperties() {
        CustomProperties properties = new CustomProperties();
        properties.put("sendemail", "true", "flag to send email or not");
        properties.put("logPathXml", "./" +  getApplicationName() + "/resources");
        properties.put("wrkDir", "./" +  getApplicationName() + "/resources/wrk");
        properties.put("spreadsheetPath", "./" +  getApplicationName() + "/resources/ExceptionTemplate.ods");
        properties.put("spreadsheetDestPath", "./" +  getApplicationName() + "/resources/results");
        return properties;
    }// end getAdditionalApplicationProperties

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.AbstractApplication#getAdditionalLoggingProperties()
     */
    @Override
    protected CustomProperties getAdditionalLoggingProperties() {
        CustomProperties logProps = new CustomProperties();
        logProps.put("com.omo.free.lec.application.level", "INFO");
        logProps.put("com.omo.free.lec.file.level", "INFO");
        logProps.put("com.omo.free.lec.model.level", "INFO");
        logProps.put("com.omo.free.lec.util.level", "INFO");
        logProps.put("com.omo.free.lec.business.level", "INFO");
        return logProps;
    }// end getAdditionalLoggingProperties

    @Override
    protected String getEncryptionKey() {
        return "docsecretpassword";
    }//end method

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.ifc.IEmailable#getEmailBcc()
     */
    @Override
    public String getEmailBcc() {
        return null;
    }// end getEmailBcc

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.ifc.IEmailable#getEmailCc()
     */
    @Override
    public String getEmailCc() {
        return null;
    }// end getEmailCc

    /*
     * (non-Javadoc)
     * @see gov.doc.isu.gtv.core.ifc.IEmailable#getEmailFrom()
     */
    @Override
    public String getEmailFrom() {
        // The comment following the return value should be left in place. This is a marker for the build script to find and replace the value if required.
        return "LoggedExceptionsCounterBatch@docapp.mo.gov";// email from
    }// end getEmailFrom

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailHost() {
        // The comment following the return value should be left in place. This is a marker for the build script to find and replace the value if required.
        return "zimbra.isu.net";// email host
    }// end getEmailHost

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailStoreProtocol() {
        // The comment following the return value should be left in place. This is a marker for the build script to find and replace the value if required.
        return "pop3";// store protocol
    }// end getEmailStoreProtocol

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailSubject() {
        // The comment following the return value should be left in place. This is a marker for the build script to find and replace the value if required.
        return "Logged Exceptions Counter Batch";// email subject
    }// end getEmailSubject

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailTo() {
        // The comment following the return value should be left in place. This is a marker for the build script to find and replace the value if required.
        //"dwayne.walker@oa.mo.gov,Addison.Woody@oa.mo.gov,James.Moore@oa.mo.gov,amy.bell@oa.mo.gov,David.Lowe@oa.mo.gov,ITSDJCCCSHOP@doc.mo.gov";// email to
        return "rts000is@isu.net";// email to
    }// end getEmailTo

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailTransportProtocol() {
        return "smtp";
    }// end getEmailTransportProtocol

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserInterface getUserInterface() {
        return null;
    }//end method

}// end class

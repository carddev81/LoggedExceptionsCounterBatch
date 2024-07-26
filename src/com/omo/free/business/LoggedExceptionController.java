package com.omo.free.business;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.element.office.OfficeSpreadsheetElement;

import com.omo.free.lec.file.LogPathXmlParser;
import com.omo.free.lec.model.ErrorMessage;
import com.omo.free.lec.model.ExceptionModel;
import com.omo.free.lec.model.LogPath;
import com.omo.free.lec.util.AppConstants;
import com.omo.free.lec.util.LogProcessorSpreadsheetUtil;

import gov.doc.isu.gtv.managers.PropertiesMgr;
import gov.doc.isu.gtv.util.ApplicationConstants;
import gov.doc.isu.gtv.util.FileUtil;

/**
 * This class holds the main logic for the entire Log scanning process.  Logs will be scanned through for Exceptions.
 *
 * @author Richard Salas
 */
public class LoggedExceptionController {

    // class variables
    private static final String MY_CLASS_NAME = "com.omo.free.business.LoggedExceptionController";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);
    private static LoggedExceptionController controller;
    private LocalDate appLogDt;
    private String environment;
    private File spreadSheetDestFile;
    private int totalScannedLogs;

    /**
     * Private default constructor used to create an instance of the LoggedExceptionController.  This can only be called within itself.
     */
    private LoggedExceptionController() {
        myLogger.entering(MY_CLASS_NAME, "LoggedExceptionController");
        FileUtil.checkDirectories(AppConstants.WORK_DIR);
        myLogger.exiting(MY_CLASS_NAME, "LoggedExceptionController");
    }// end constructor

    /**
     * This method will return a static instance of the {@code LoggedExceptionController}.
     * @return controller the {@code LoggedExceptionController} instance
     */
    public static LoggedExceptionController getInstance() {
        myLogger.entering(MY_CLASS_NAME, "getInstance");
        if(controller == null){
            controller = new LoggedExceptionController();
        } // end if
        myLogger.exiting(MY_CLASS_NAME, "getInstance");
        return controller;
    }// end method

    /**
     * This method will run the processes for capturing the logged exceptions.
     *
     * @param arguments the arguments used by the logged exceptions processing date and environment
     * @throws Exception is thrown during processing when something bad occurrs
     */
    public void run(String... arguments) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "run", arguments);
        //SETTING UP THE THREAD POOL
        ThreadPoolExecutor myThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        if(arguments.length <= 0){
            myLogger.info("Using the default of production and today's date");
            appLogDt = LocalDate.now();
            environment = "production";
        }else if(arguments.length == 1){
            appLogDt = LocalDate.now();
            environment = arguments[0];
        }else{
            appLogDt = DateTimeFormatter.ISO_LOCAL_DATE.parse(arguments[1], LocalDate::from);
            environment = arguments[0];
        }//end if...else
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy.MM.dd");
        String serverDt = dtf.format(appLogDt);

        //add simple check here to make sure path exists before moving forward.
        if(!"ISU".equals(System.getenv("USERDOMAIN")) && !Files.exists(Paths.get("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs"))){
            throw new IllegalStateException("Contact your network administrator.  Either the shared network path doesn't exist or you do not have permission to access the shared network path:  //SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs");
        }//end if
        
        //PARSE XML HERE PER ENVIRONMENT
        List<LogPath> paths = LogPathXmlParser.parseLogPathXml(environment);

        environment = environment.matches("(production|test)") ? environment + "WAS9" : environment;
        if(myLogger.isLoggable(Level.FINE)){
            paths.forEach(log ->{
                StringBuilder sb = new StringBuilder();
                sb.append(ApplicationConstants.NEW_LINE);
                sb.append("================");
                sb.append(log.getName()).append(ApplicationConstants.NEW_LINE);
                sb.append(log.getType()).append(ApplicationConstants.NEW_LINE);
                sb.append(log.getLogPrefixes()).append(ApplicationConstants.NEW_LINE);
                sb.append(log.getEnvironment()).append(ApplicationConstants.NEW_LINE);
                sb.append(log.getPaths()).append(ApplicationConstants.NEW_LINE);
                sb.append(appLogDt).append(ApplicationConstants.NEW_LINE);
                sb.append("================").append(ApplicationConstants.NEW_LINE);
                myLogger.fine(String.valueOf(sb));
            });
        }//end if

        //SET UP THE PROCESS LIST HERE FOR
        List<Future<ExceptionModel>> processList = new ArrayList<>();

        Iterator<LogPath> it = paths.iterator();
        ProcessTask pt = null;
        boolean currentDay = LocalDate.now().isEqual(appLogDt);
        while(it.hasNext()){
            LogPath logPath = it.next();
            if("server".equals(logPath.getType())){
                pt = new ProcessTask(logPath, serverDt, currentDay);
            }else{
                pt = new ProcessTask(logPath, appLogDt.toString(), currentDay);
            }
            processList.add(myThreadPool.submit(pt));
        }//end while

        List<ExceptionModel> exceptionModels = waitForTasksToFinishAndShutdownThreadPool(myThreadPool, processList);

        //get the total number of logs scanned
        totalScannedLogs = exceptionModels.stream().mapToInt(ExceptionModel::getLogCount).sum();

        if(myLogger.isLoggable(Level.FINER)){//log the models created
            myLogger.finer(String.valueOf(exceptionModels));
        }//end if

        createSpreadSheet(exceptionModels);
        myLogger.exiting(MY_CLASS_NAME, "run");
    }// end method

    /**
     * This method creates the spreadsheet report of all exceptions found within a specific environment.
     *
     * @param exceptionModels the exception models containing the exceptions.
     */
    private void createSpreadSheet(List<ExceptionModel> exceptionModels) {
        myLogger.entering(MY_CLASS_NAME, "createSpreadSheet", exceptionModels);
        String pathToSpreadSheetTemplate = PropertiesMgr.getProperties().getProperty("spreadsheetPath");
        String spreadSheetDestination = PropertiesMgr.getProperties().getProperty("spreadsheetDestPath");
        FileUtil.checkDirectories(spreadSheetDestination);

        String pathToSpreadSheetDest = spreadSheetDestination + "/" + String.valueOf(appLogDt).replaceAll("-", "") +  "_" + environment + "_LoggedExceptionsCounts_" + DateTimeFormatter.ofPattern("yyyyMMddhhmmss").format(LocalDateTime.now()) + ".ods";
        spreadSheetDestFile = new File(pathToSpreadSheetDest);
        // Create an ods document (spreadsheet) to hold all the different issues and relevant data.
        File template = new File(pathToSpreadSheetTemplate);

        FileUtil.copyInternalFileToExternalDestination(this.getClass(),template.getParentFile().getAbsolutePath(), template.getName());
        OdfSpreadsheetDocument document = null;
        try{
            if(template.exists()){
                document = OdfSpreadsheetDocument.loadDocument(template);
                fillCoverPage(document, exceptionModels);
                createExceptionSheets(document,exceptionModels);
                fillErrorsSheet(document, exceptionModels);
                document.save(spreadSheetDestFile);
            }//end method
        }catch(Exception e){
            myLogger.log(Level.SEVERE, "Exception occurred while trying to load spreadsheet.  Error message is: " + e.getMessage(), e);
        }finally{
            if(document != null){
                document.close();
            }//end if
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "createSpreadSheet");
    }//end method

    /**
     * This method is used to fill the errors spread with errors if they exist.
     *
     * @param document the spreadsheet document
     * @param exceptionModels the models possibly containing errors.
     */
    private void fillErrorsSheet(OdfSpreadsheetDocument document, List<ExceptionModel> exceptionModels) {
        myLogger.entering(MY_CLASS_NAME, "fillErrorsSheet", new Object[]{document, exceptionModels});

        OdfTable errorTab = document.getTableByName("ErrorPage");

        List<ExceptionModel> models = exceptionModels.stream().filter(model -> model.getErrors().size() > 0).collect(Collectors.toList());
        Iterator<ExceptionModel> it = models.iterator();
        int row = 11;
        while(it.hasNext()){
            List<ErrorMessage> em = it.next().getErrors();
            for(int i = 0, j = em.size(); i < j; i++){
                ErrorMessage message = em.get(i);
                errorTab.getCellByPosition(1, row).setStringValue(message.getDirOrFilePath());
                errorTab.getCellByPosition(2, row).setStringValue(message.getMessage());
                row++;
            }//end for
        }//end while
        myLogger.exiting(MY_CLASS_NAME, "fillErrorsSheet");
    }//end method

    /**
     * This method is the main method for creating the main exception sheets.
     *
     * @param document the spreadsheet document
     * @param exceptionModels the models possibly containing errors
     * @throws Exception the exception that is thrown during the creation of spreadsheets
     */
    private void createExceptionSheets(OdfSpreadsheetDocument document, List<ExceptionModel> exceptionModels) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "createExceptionSheets", new Object[]{document, exceptionModels});

        List<ExceptionModel> serversList = exceptionModels.stream().filter(model -> "server".equals(model.getType()) && model.getTotalExceptionCount() > 0).collect(Collectors.toList());
        List<ExceptionModel> appList = exceptionModels.stream().filter(model -> !"server".equals(model.getType())  && model.getTotalExceptionCount() > 0).collect(Collectors.toList());

        createExceptionSheet(document, serversList.iterator());
        createExceptionSheet(document, appList.iterator());
        myLogger.exiting(MY_CLASS_NAME, "createExceptionSheets");
    }//end method

    /**
     * This method will create exception sheet based on the passed in parameters.
     *
     * @param document the spreadsheet document
     * @param iterator the iteratable used for filling the spreadsheet
     * @throws Exception during the filling of the sheet
     */
    private void createExceptionSheet(OdfSpreadsheetDocument document, Iterator<ExceptionModel> iterator) throws Exception {
        myLogger.entering(MY_CLASS_NAME, "createExceptionSheet", new Object[]{document, iterator});

        OdfTable exceptionSheet = null;
        OfficeSpreadsheetElement spreadSheetElement = document.getContentRoot();
        OdfContentDom contentDom = document.getContentDom();

        int row = 4;
        boolean dataCell1 = false;
        while(iterator.hasNext()){
            ExceptionModel em = iterator.next();
            exceptionSheet = LogProcessorSpreadsheetUtil.createNewSheet(document, spreadSheetElement, em.getClusterOrApplicationName());

            row = LogProcessorSpreadsheetUtil.initTable(exceptionSheet, contentDom, row);
            //add number of logs here
            //add total here to...
            exceptionSheet.getCellByPosition(1, 3).setStringValue(em.getLogCount() + " Logs Were Scanned");
            LogProcessorSpreadsheetUtil.addColumnDataFilters(contentDom, exceptionSheet.getTableName(), "C5", "C5");// make this better too clover!!
            Iterator<Entry<String, Integer>> it = em.getExceptionMap().entrySet().iterator();
            while(it.hasNext()){
                Entry<String, Integer> entry = it.next();
                OdfTableRow newRow = exceptionSheet.appendRow();
                LogProcessorSpreadsheetUtil.setDataCellColor(newRow.getCellByIndex(1), dataCell1);
                newRow.getCellByIndex(1).setStringValue(entry.getKey());

                LogProcessorSpreadsheetUtil.setDataCellColor(newRow.getCellByIndex(2), dataCell1);
                newRow.getCellByIndex(2).setDoubleValue(Double.valueOf(entry.getValue()));
                newRow.getCellByIndex(2).setHorizontalAlignment("left");

                dataCell1 = dataCell1 ? false : true;
                row++;
            }//end while
            exceptionSheet.getCellByPosition(2, 3).getOdfElement().setStyleName("FormulaCell");
            exceptionSheet.getCellByPosition(2, 3).setFormula("=\"Total Exceptions: \"&SUM(C6:C" + row + ")");
            row = 4;
            dataCell1 = false;
        }//end while
        myLogger.exiting(MY_CLASS_NAME, "createExceptionSheets");
    }//end method

    /**
     * This method fills the cover page with data based on the exception models
     *
     * @param document the spreadsheet document
     * @param exceptionModels the models possibly containing errors.
     */
    private void fillCoverPage(OdfSpreadsheetDocument document, List<ExceptionModel> exceptionModels) {
        myLogger.entering(MY_CLASS_NAME, "fillCoverPage", new Object[]{document, exceptionModels});

        List<ExceptionModel> serversList = exceptionModels.stream().filter(model -> "server".equals(model.getType())).collect(Collectors.toList());
        List<ExceptionModel> appList = exceptionModels.stream().filter(model -> !"server".equals(model.getType())).collect(Collectors.toList());

        OdfTable coverTab = document.getTableByName("CoverPage");
        coverTab.getCellByPosition(2, 4).setStringValue(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now()));
        coverTab.getCellByPosition(2, 5).setStringValue(environment);
        coverTab.getCellByPosition(2, 6).setStringValue(appLogDt.toString());
        //
        //environment production
        //date of logs processed

        //appLogDt
        Iterator<ExceptionModel> serversIt = serversList.iterator();
        int row = 25;
        while(serversIt.hasNext()){
            ExceptionModel model = serversIt.next();
            if(model.getTotalExceptionCount() > 0){
                coverTab.getCellByPosition(1, row).setStringValue(model.getClusterOrApplicationName());
                coverTab.getCellByPosition(2, row).setDoubleValue(Double.valueOf(model.getTotalExceptionCount()));
                row++;
            }
        }//end while
        //name total

        row = 50;
        Iterator<ExceptionModel> appsIt = appList.iterator();
        while(appsIt.hasNext()){
            ExceptionModel model = appsIt.next();
            if(model.getTotalExceptionCount() > 0){
                coverTab.getCellByPosition(1, row).setStringValue(model.getClusterOrApplicationName());
                coverTab.getCellByPosition(2, row).setDoubleValue(Double.valueOf(model.getTotalExceptionCount()));
                row++;
            }//end if
        }//end while
        myLogger.exiting(MY_CLASS_NAME, "fillCoverPage", new Object[]{document, exceptionModels});
    }//end method

    /**
     * This method will gather all the completed callable tasks {@code ExceptionModel} that was populated during execution.
     * @param myThreadPool the thread pool
     * @param processingList the list to process.
     * @return exceptionModelList the exception model list
     */
    private List<ExceptionModel> waitForTasksToFinishAndShutdownThreadPool(ThreadPoolExecutor myThreadPool, List<Future<ExceptionModel>> processingList) {
        myLogger.entering(MY_CLASS_NAME, "waitForTasksToFinishAndShutdownThreadPool", new Object[]{myThreadPool, processingList});
        List<ExceptionModel> exceptionModelList = new ArrayList<>();
        try{
            int finalSize = processingList.size();
            long tasksCompleted = 0;
            do{
                if(myThreadPool.getCompletedTaskCount() > tasksCompleted){
                    myLogger.info("The number of completed processing tasks is " + myThreadPool.getCompletedTaskCount() + " of " + finalSize);
                    tasksCompleted = myThreadPool.getCompletedTaskCount();
                }//end if

                try{
                    TimeUnit.SECONDS.sleep(3);//let the tasks do work.
                }catch(InterruptedException e){
                    myLogger.log(Level.SEVERE, "InterruptedException occurred while thread was sleeping.");
                    break;
                }// end try...catch
            }while(myThreadPool.getCompletedTaskCount() < finalSize);// keep checking to see if the final size matches the completed tasks count.

            //count all the records processed.
            Iterator<Future<ExceptionModel>> fIterator = processingList.iterator();
            while(fIterator.hasNext()){
                try{
                    Future<ExceptionModel> em = fIterator.next();
                    exceptionModelList.add(em.get());
                }catch(InterruptedException e){
                    myLogger.log(Level.SEVERE, "InterruptedException occurred while trying to retrieve the exception model from the processing list of Future instances.  Error is: " + e.getMessage(), e);
                }catch(ExecutionException e){
                    myLogger.log(Level.SEVERE, "ExecutionException occurred while trying to retrieve the exception model from the processing list of Future instances.  Error is: " + e.getMessage(), e);
                }//end try...catch
            }//end while
        }finally{
            myThreadPool.shutdown();
        }// end try/catch

        myLogger.exiting(MY_CLASS_NAME, "waitForTasksToFinishAndShutdownThreadPool", exceptionModelList);
        return exceptionModelList;
    }//end method

    /**
     * @return the appLogDt
     */
    public LocalDate getAppLogDt() {
        return appLogDt;
    }//end method

    /**
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }//end method

    /**
     * @return the spreadSheetDestFile
     */
    public File getSpreadSheetDestFile() {
        return spreadSheetDestFile;
    }//end method

    /**
     * @return the totalScannedLogs
     */
    public int getTotalScannedLogs() {
        return totalScannedLogs;
    }//end method

}// end class

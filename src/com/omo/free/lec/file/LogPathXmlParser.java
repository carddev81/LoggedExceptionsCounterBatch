package com.omo.free.lec.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.omo.free.lec.model.LogPath;

import gov.doc.isu.gtv.managers.PropertiesMgr;
import gov.doc.isu.gtv.util.FileUtil;

/**
 * This class is used to parse the LogPaths.xml file located internally to this application and externally during runtime.
 *
 * <p>Internal path:  /com/omo/free/lep/resource/LogPaths.xml<br>
 * External path:  ./LoggedExceptionsCounterBatch/resources/LogPaths.xml
 * @author rts000is
 *
 */
public class LogPathXmlParser {


    private static final String MY_CLASS_NAME = "com.omo.free.lec.file.LogPathXmlParser";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);

    //TODO fix this method to be more correct. exception handling needs to be beefed up.
    /**
     * This method will parse the xml file used by the application into a list of {@link LogPath}'s.
     *
     * @param environment the environment used to parse the xml
     * @return list of log paths
     */
    public static List<LogPath> parseLogPathXml(String environment){
        myLogger.entering(MY_CLASS_NAME, "parseLogPathXml", environment);
        //TODO add logic for checking version number here, this will allow user the permissions to modify the xml file at will
        List<LogPath> logPaths = new ArrayList<>();
        Path xmlPath = Paths.get(PropertiesMgr.getProperties().getProperty("logPathXml"), "LogPaths.xml");
        xmlPath.toFile().delete();
        FileUtil.copyInternalFileToExternalDestination(LogPathXmlParser.class, PropertiesMgr.getProperties().getProperty("logPathXml"), "LogPaths.xml");

        SAXBuilder sax = new SAXBuilder();
        Document doc = null;
        try{
            doc = sax.build(xmlPath.toFile());
            Element rootElement = doc.getRootElement();
            List<Element> logPathElements = rootElement.getChildren("logpath");
            Iterator<Element> it = logPathElements.iterator();
            while(it.hasNext()){
                Element logPath = it.next();
                if(environment.equals(logPath.getChild("environment").getText())){
                    logPaths.add(buildLogPath(logPath));
                }//end if
            }//end while
        }catch(JDOMException e){
            myLogger.log(Level.SEVERE, "JDOMException was caught while trying to load xml document. Error is: " + e.getMessage(), e);
        }catch(IOException e){
            myLogger.log(Level.SEVERE, "IOException was caught while trying to load xml document. Error is: " + e.getMessage(), e);
        }// end try...catch
        myLogger.exiting(MY_CLASS_NAME, "parseLogPathXml()", logPaths);
        return logPaths;
    }//end method

    /**
     * Helper method used to build a {@link LogPath}.
     * @param logPath the xml element containing the LogPath data
     * @return log the {@link LogPath} instance created using the metadata within the LogPaths.xml file
     */
    private static LogPath buildLogPath(Element logPath) {
        myLogger.entering(MY_CLASS_NAME, "buildLogPath", logPath);
        LogPath log = new LogPath();
        log.setName(logPath.getChild("name").getText());
        log.setEnvironment(logPath.getChild("environment").getText());
        log.setType(logPath.getChild("type").getText());
        log.setAccess(logPath.getChild("access").getText());

        Iterator<Element> paths = logPath.getChild("paths").getChildren().iterator();
        while(paths.hasNext()){
            log.addPath(paths.next().getText());
        }//end while

        Iterator<Element> prefixes = logPath.getChild("prefixes").getChildren().iterator();
        while(prefixes.hasNext()){
            log.addPrefix(prefixes.next().getText());
        }//end while

        myLogger.exiting(MY_CLASS_NAME, "buildLogPath", log);
        return log;
    }//end method

}//end method

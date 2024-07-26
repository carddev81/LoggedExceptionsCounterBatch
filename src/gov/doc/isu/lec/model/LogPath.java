/**
 *
 */
package gov.doc.isu.lec.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.doc.isu.lec.file.FilePath;

/**
 * This class models a logpath element within the LogPaths.xml file.
 *
 * <pre>
 *  &lt;logpath&gt;
 *      &lt;name&gt;PuppiesForParolePublic&lt;/name&gt;
 *      &lt;environment&gt;production&lt;/environment&gt;
 *      &lt;type&gt;webapp&lt;/type&gt;
 *      &lt;access&gt;Public&lt;/access&gt;
 *      &lt;paths&gt;
 *          &lt;path&gt;\App_Logs\doc\apps\PuppiesForParolePublic\logs&lt;/path&gt;
 *      &lt;/paths&gt;
 *  &lt;/logpath&gt;
 * </pre>
 * 
 * @author Richard Salas
 */
public class LogPath {

    private String name;//COMMONPRIV CLUSTER, ARB, etc.
    private String environment;//production
    private String type;//webapp, server
    private FilePath access;//private, public
    private List<String> logPrefixes;
    private List<String> paths;

    /**
     * Constructor used to create an instance of the LogPath class.
     */
    public LogPath(){
        this.paths = new ArrayList<>();
        this.logPrefixes = new ArrayList<>();
    }//end method

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return logPrefixes the prefixes
     */
    public List<String> getLogPrefixes() {
        return logPrefixes;
    }

    /**
     *
     * @param logPrefixes the prefixes to set
     */
    public void setLogPrefixes(List<String> logPrefixes) {
        this.logPrefixes = logPrefixes;
    }

    /**
     * Adds a path to the paths list
     * @param path the string path to add to the list of paths
     */
    public void addPath(String path) {
        this.paths.add(path);
    }//end method

    /**
     * Adds a prefix to the list of prefixes
     * @param prefix the prefix to add
     */
    public void addPrefix(String prefix) {
        this.logPrefixes.add(prefix);
    }//end method

    /**
     * @return the access
     */
    public FilePath getAccess() {
        return access;
    }

    /**
     * @param accessStr the access to set
     */
    public void setAccess(String accessStr) {
        switch(accessStr){
            case "Private":
                this.access = FilePath.COMMONPRIV_PROD;
                break;
            case "Public":
                this.access = FilePath.COMMONPUB_PROD;
                break;
            case "MOCISPrivate":
                this.access = FilePath.MOCISPRIV_PROD;
                break;
            case "TestPrivate":
                this.access = FilePath.COMMONPRIV_TEST;
                break;
            case "TestPublic":
                this.access = FilePath.COMMONPUB_TEST;
                break;
            case "TestMOCISPrivate":
                this.access = FilePath.MOCISPRIV_TEST;
                break;
            case "MOCISPrivateJCCC":
                this.access = FilePath.MOCISPRIV_JCCC;
                break;
            case "JCCCPrivate":
                this.access = FilePath.COMMON_JCCC;
                break;
            default:
                this.access = FilePath.DEFAULT;
                break;
        }//end
    }//end method

    /**
     * @return the paths
     */
    public List<String> getPaths() {
        if("server".equals(this.type)){
            return paths;
        }else{
            List<String> loggingPaths = new ArrayList<>();
            for(int i = 0, j = paths.size(); i < j; i++){
                loggingPaths.addAll(buildPaths(paths.get(i)));
            }//end for
            return loggingPaths;
        }//end if
    }//end method

    /**
     * This method is used to append the path to the application logging directory to the shared directory servers paths.
     * @param pathToApplicationLogs the path to the application logging directory
     * @return list of directory paths to where logs live
     */
    private List<String> buildPaths(String pathToApplicationLogs) {
        List<String> returnList = new ArrayList<>();
        List<String> urls = this.access.getUrls();
        Iterator<String> it = urls.iterator();
        while(it.hasNext()){
            returnList.add(it.next() + pathToApplicationLogs);
        }//end while
        return returnList;
    }//end method

    /**
     * @param paths the paths to set
     */
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogPath [name=");
        builder.append(name);
        builder.append(", environment=");
        builder.append(environment);
        builder.append(", type=");
        builder.append(type);
        builder.append(", access=");
        builder.append(access);
        builder.append(", logPrefixes=");
        builder.append(logPrefixes);
        builder.append(", paths=");
        builder.append(paths);
        builder.append("]");
        return builder.toString();
    }//end method

}//end class

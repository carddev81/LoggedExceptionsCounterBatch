package com.omo.free.lec.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum holds the root paths to the web server directories.
 *
 * @author Richard Salas
 */
public enum FilePath {

    MOCISPRIV_PROD("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP4272.state.mo.us/DOCMOCIS", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP4273.state.mo.us/DOCMOCIS", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP4274.state.mo.us/DOCMOCIS"),

    COMMONPUB_PROD("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5090.state.mo.us/DOCCMNPUB", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5091.state.mo.us/DOCCMNPUB",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5092.state.mo.us/DOCCMNPUB",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5093.state.mo.us/DOCCMNPUB",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5094.state.mo.us/DOCCMNPUB",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5095.state.mo.us/DOCCMNPUB"),

    COMMONPRIV_PROD("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5090.state.mo.us/DOCCMNPRIV", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5091.state.mo.us/DOCCMNPRIV",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5092.state.mo.us/DOCCMNPRIV",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5093.state.mo.us/DOCCMNPRIV",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5094.state.mo.us/DOCCMNPRIV",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWASP5095.state.mo.us/DOCCMNPRIV"),


    COMMONPUB_TEST("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5006.state.mo.us/DOCCMNPUBN", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5007.state.mo.us/DOCCMNPUBN",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5008.state.mo.us/DOCCMNPUBN"),

    COMMONPRIV_TEST("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5006.state.mo.us/DOCCMNPRIVN", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5007.state.mo.us/DOCCMNPRIVN",
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST5008.state.mo.us/DOCCMNPRIVN"),
    
    
    MOCISPRIV_TEST("//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST4205.state.mo.us/DOCMOCIS", 
            "//SDCOFILP4523.state.mo.us/SharedData/DOCWasLogs/SDWMWAST4206.state.mo.us/DOCMOCIS"),
    
    MOCISPRIV_JCCC("//isuwsphere3svr", "//isuwsphere4svr", "//isuwsphere5svr"),

    COMMON_JCCC("//isuwsphere1svr", "//isuwsphere2svr"),

    DEFAULT();

    private List<String> urls;

    /**
     * Enum constructor used to create the singleton
     * @param filePaths the files paths to different servers
     */
    FilePath(String... filePaths){
        if(filePaths == null || filePaths.length == 0){
            this.urls = Collections.emptyList();
        }else{
            this.urls = new ArrayList<>(Arrays.asList(filePaths));
        }//end if...else
    }//end constructor

    /**
     * Returns the list of uncs
     * @return server unc paths
     */
    public List<String> getUrls(){
        return this.urls;
    }//end method

}//end enum

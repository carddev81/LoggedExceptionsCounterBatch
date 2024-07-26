package com.omo.free.lec.util;

import gov.doc.isu.gtv.managers.PropertiesMgr;

/**
 * Constants class.
 * @author Richard Salas
 */
public class AppConstants {

    public static String WORK_DIR;

    static{
        WORK_DIR = PropertiesMgr.getProperties().getProperty("wrkDir");
    }
}

package com.omo.free.lec.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.OdfSettingsDom;
import org.odftoolkit.odfdom.dom.element.dc.DcCreatorElement;
import org.odftoolkit.odfdom.dom.element.dc.DcDateElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeAnnotationElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeSpreadsheetElement;
import org.odftoolkit.odfdom.dom.element.style.StyleParagraphPropertiesElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTableCellPropertiesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableColumnElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.pkg.OdfName;
import org.odftoolkit.odfdom.pkg.OdfXMLFactory;
import org.w3c.dom.NodeList;

/**
 * This class is used as a utility class for generating/manipulating spreadsheets that are specific to the Log Processor applications that insert data into spreadsheets.
 *
 * @author Richard Salas
 */
public class LogProcessorSpreadsheetUtil {

    private static final String MY_CLASS_NAME = "com.omo.free.lec.util.LogProcessorSpreadsheetUtil";
    private static Logger myLogger = Logger.getLogger(MY_CLASS_NAME);
    private static final String[] ATTRIBUTE_VALUES = {"CursorPositionX", "CursorPositionY", "HorizontalSplitMode", "VerticalSplitMode", "HorizontalSplitPosition", "VerticalSplitPosition", "ActiveSplitRange", "PositionLeft", "PositionRight", "PositionTop", "PositionBottom", "ZoomType", "ZoomValue", "PageViewZoomValue"};
    private static final String[] ATTRIBUTE_TYPE_VALUES = {"int", "int", "short", "short", "int", "int", "short", "int", "int", "int", "int", "short", "int", "int"};
    private static final String[] COLUMN_HEADERS = {"Exception Name", "# Of Exceptions"};
    private static final String[] COMMENTS = {"The fully qualified name of the exception found", "Number of instances found within the logs associated to this unit"};
    private static final long[] WIDTHS = {5L, 207L, 40L};

    /**
     * This method will initialize a new category table within the RequestsByFunctionalUnit sheet. Column styles are initially set here.
     *
     * @param categorySheet
     *        the sheet that the table will be initialized on
     * @param fileDom OdfFileDom is used for generating the pop ups.
     * @param row
     *        the row number to start inserting data into
     * @return row the row number that we left off from
     */
    public static int initTable(OdfTable categorySheet, OdfFileDom fileDom, int row) {
        myLogger.entering(MY_CLASS_NAME, "initNewCategoryTable()", new Object[]{categorySheet, fileDom, row});

        categorySheet.getCellByPosition(1, 1).getOdfElement().setStyleName("Title");
        categorySheet.getCellByPosition(1, 1).setStringValue("Exceptions Found: " + categorySheet.getTableName());

        int col = 1;

        categorySheet.getCellByPosition(1, 3).getOdfElement().setStyleName("MixedUseCell");
        categorySheet.getCellByPosition(1, 3).setHorizontalAlignment("right");

        categorySheet.getCellByPosition(0, row).getTableColumn().setWidth(WIDTHS[0]);//minimize blank column
        // category headers and data column headers
        for(int i = 0, j = COLUMN_HEADERS.length;i < j;i++){
            // headers
            categorySheet.getCellByPosition(col, row).getOdfElement().setStyleName("ColumnHeading");
            categorySheet.getCellByPosition(col, row).setStringValue(COLUMN_HEADERS[i]);
            if(i == 0){
                categorySheet.getCellByPosition(col, row).getTableColumn().setWidth(WIDTHS[1]);
            }else if(i >= 1){
                categorySheet.getCellByPosition(col, row - 2).getOdfElement().setStyleName("Default");
                categorySheet.getCellByPosition(col, row - 1).getOdfElement().setStyleName("Default");
                categorySheet.getCellByPosition(col, row).getTableColumn().setWidth(WIDTHS[2]);
            }// end if
            categorySheet.getCellByPosition(col, row).getOdfElement().appendChild(addPopOutCommentToCell(fileDom, COMMENTS[i]));
            col++;
        }// end for

        row++;// add one to the row.
        col = 1;// set col back to one
        myLogger.exiting(MY_CLASS_NAME, "initNewCategoryTable()", row);
        return row;
    }// end method()

    /**
     * This method will add a pop out comment/pop out note to a cell.
     * <p><b>Example Usage:</b>
     *  <pre>
     *     sheet1.getCellByPosition(col, row).getOdfElement().appendChild(addPopOutCommentToCell(contentDom, "This is the note"));
     *  </pre>
     *
     * @param dom the OdfFileDom which is used for creating the new pop out comment
     * @param note the note that will be used as the test
     * @return annotation the OfficeAnnotationElement element to be used.
     */
    public static OfficeAnnotationElement addPopOutCommentToCell(OdfFileDom dom, String note) {
        myLogger.entering(MY_CLASS_NAME, "addPopOutCommentToCell()", new Object[]{dom, note});
        OfficeAnnotationElement annotation = (OfficeAnnotationElement) OdfXMLFactory.newOdfElement(dom, OdfName.newName(OdfDocumentNamespace.OFFICE, "annotation"));
        TextPElement noteElement = annotation.newTextPElement();
        noteElement.setTextContent(note);
        DcCreatorElement dcCreatorElement = annotation.newDcCreatorElement();
        dcCreatorElement.setTextContent(System.getProperty("user.name"));
        String dcDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        DcDateElement dcDateElement = annotation.newDcDateElement();
        dcDateElement.setTextContent(dcDate);
        myLogger.exiting(MY_CLASS_NAME, "addPopOutCommentToCell()", annotation);
        return annotation;
    }//end set Note

    /**
     * This method will generate/build formulas for the category table based upon the parameters being passed in.
     *
     * @param categorySheet
     *        the sheet contianing the category table
     * @param formulaRow
     *        the row that will contain the formula to be set
     * @param dataEndRow
     *        the last row in the category table that contains data
     * @param isSepartedByNode true/false value - this is used to separate out by nodes.
     */
    public static void addFormulasToCategoryTable(OdfTable categorySheet, int formulaRow, int dataEndRow, boolean isSepartedByNode) {
        myLogger.entering(MY_CLASS_NAME, "addFormulasToCategoryTable()", new Object[]{categorySheet, formulaRow, dataEndRow});

        String sheetname = categorySheet.getTableName();
        categorySheet.getCellByPosition(1, 1).getOdfElement().setStyleName("Title");
        categorySheet.getCellByPosition(1, 1).setFormula("=\"HTTP Request Log Processor: " + sheetname + " module\"");

        //categorySheet.getCellByPosition(3, 1).getOdfElement().setStyleName("Hyperlink");
        //categorySheet.getCellByPosition(3, 1).;

        categorySheet.getCellByPosition(1, 1).getOdfElement().setStyleName("Title");
        if(formulaRow != 0){// if not equal to zero then add formula
            int col = 1;
            StringBuilder formula = new StringBuilder("=COUNTIF($B$6:$B$").append(dataEndRow);
            if(!isSepartedByNode){
                categorySheet.getCellByPosition(col, formulaRow).setFormula(formula.append(";\".+\") & \" ").append(categorySheet.getTableName()).append(" URL(s) Processed For Entire Day\"").toString());
                categorySheet.getCellByPosition(1, 1).setFormula("=\"HTTP Request Log Processor: " + sheetname + " module\"");
            }else{
                categorySheet.getCellByPosition(col, formulaRow).setFormula(formula.append(";\".+\") & \" URL(s) Processed For Entire Day On Server Node ").append(categorySheet.getTableName()).append("\"").toString());
                categorySheet.getCellByPosition(1, 1).setFormula("=\"HTTP Request Log Processor: " + sheetname + " node\"");
            }//end if
            col++;
            for(int i = 0;i < 3;i++){
                categorySheet.getCellByPosition(col, formulaRow).getOdfElement().setStyleName("FormulaCell");
                if(i == 0){
                    categorySheet.getCellByPosition(col, formulaRow).setFormula("=\"Total Processed: \"&SUM(C" + (formulaRow + 3) + ":C" + dataEndRow);
                }// end if

                if(i == 1){
                    categorySheet.getCellByPosition(col, formulaRow).setFormula("=\"Fastest Time: \"&MIN(D" + (formulaRow + 3) + ":D" + dataEndRow);
                }// end if

                if(i == 2){
                    categorySheet.getCellByPosition(col, formulaRow).setFormula("=\"Slowest Time: \"&MAX(E" + (formulaRow + 3) + ":E" + dataEndRow);
                }// end if
                col++;
            }// end for
        }// end if
        myLogger.exiting(MY_CLASS_NAME, "addFormulasToCategoryTable()");
    }// end method

    /**
     * This method will generate/build formulas for the request stats table based upon the parameters being passed in.
     *
     * @param requestSheet
     *        the sheet containing the request stats table
     * @param formulaRow
     *        the row that will contain the formula to be set
     */
    public static void addFormulasToRequestStatsTable(OdfTable requestSheet, int formulaRow) {
        myLogger.entering(MY_CLASS_NAME, "addFormulasToRequestStatsTable()", new Object[]{requestSheet, formulaRow});
        if(formulaRow != 0){// if not equal to zero then add formula
            requestSheet.getCellByPosition(3, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(3, formulaRow).setFormula("=($C" + (formulaRow + 1) + "/$D$15)");

            requestSheet.getCellByPosition(4, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(4, formulaRow).setFormula("=($C" + (formulaRow + 1) + "/$E$15)");

            requestSheet.getCellByPosition(6, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(6, formulaRow).setFormula("=($F" + (formulaRow + 1) + "/$G$15)");

            requestSheet.getCellByPosition(7, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(7, formulaRow).setFormula("=($F" + (formulaRow + 1) + "/$H$15)");

            requestSheet.getCellByPosition(15, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(15, formulaRow).setFormula("=($O" + (formulaRow + 1) + "/$P$15)");

            requestSheet.getCellByPosition(16, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(16, formulaRow).setFormula("=($O" + (formulaRow + 1) + "/$Q$15)");

            requestSheet.getCellByPosition(22, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(22, formulaRow).setFormula("=($V" + (formulaRow + 1) + "/$W$15)");

            requestSheet.getCellByPosition(23, formulaRow).getOdfElement().setStyleName("FormulaCell2");
            requestSheet.getCellByPosition(23, formulaRow).setFormula("=($V" + (formulaRow + 1) + "/$X$15)");
        }// end if
        myLogger.exiting(MY_CLASS_NAME, "addFormulasToRequestStatsTable()");
    }// end method

    /**
     * This method will freeze a row based upon the parameters being passed into this method.
     * <p><b>NOTE</b> This method when used will log a warning when something goes wrong due to this freeze of the row not being very important. rts000is
     * @param document the document that contains the OdfSettingsDom to change
     * @param tableName the name of the sheet
     * @param rowToFreeze the row number to freeze.
     */
    public static void freezeRow(OdfSpreadsheetDocument document, String tableName, int rowToFreeze) {
        myLogger.entering(MY_CLASS_NAME, "freezeRow()", new Object[]{document, tableName, rowToFreeze});
        try{
            myLogger.finest("Creating the element to add to the OdfSettingsDom");
            OdfSettingsDom doc = document.getSettingsDom();
            OdfElement mainElement = doc.createElementNS("config", "config:config-item-map-entry");
            mainElement.setAttributeNS("config", "config:name", tableName);//namespace test
            OdfElement childElement = null;
            myLogger.fine("Building child elements <config:config-item>'s");
            for(int i = 0, j = ATTRIBUTE_VALUES.length;i<j;i++){
                childElement = doc.createElementNS("config", "config:config-item");
                childElement.setAttributeNS("config", "config:name", ATTRIBUTE_VALUES[i]);
                childElement.setAttributeNS("config", "config:type", ATTRIBUTE_TYPE_VALUES[i]);
                if("VerticalSplitMode".equals(ATTRIBUTE_VALUES[i])){
                    childElement.setTextContent("2");
                }else if("VerticalSplitPosition".equals(ATTRIBUTE_VALUES[i])){
                    childElement.setTextContent(String.valueOf(rowToFreeze));
                }else if("ZoomValue".equals(ATTRIBUTE_VALUES[i])){
                    childElement.setTextContent("97");
                }else if("PageViewZoomValue".equals(ATTRIBUTE_VALUES[i])){
                    childElement.setTextContent("60");
                }else if("PositionBottom".equals(ATTRIBUTE_VALUES[i])){
                    childElement.setTextContent(String.valueOf(rowToFreeze));
                }else{
                    childElement.setTextContent("0");
                }//end if
                mainElement.appendChild(childElement);
            }//end for
            NodeList rootToAddTo = doc.getElementsByTagName("config:config-item-map-named");// main element list
            myLogger.fine("Appending the <config:config-item-map-entry> element to the main document.");
            for(int i = 0, j = rootToAddTo.getLength();i<j;i++){
                OdfElement root = (OdfElement) rootToAddTo.item(i);
                if("Tables".equals(root.getAttribute("config:name"))){
                    root.appendChild(mainElement);
                    break;
                }//end if
            }//end for
        }catch(Exception e){
            myLogger.log(Level.WARNING, "Could not freeze the row requested due to an error. tableName=" + tableName + ", rowToFreeze=" + rowToFreeze + " Error is: " + e.getMessage(), e);
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "freezeRow()");
    }//end method

    /**
     * This method will add an auto filter to the column headings row based upon the parameters being passed in.
     * <p><b>NOTE</b> This method when used will log a warning when something goes wrong due to this data filter not not being very important. --Richard Salas
     * @param contentDoc the document that contains the OdfContentDom to change
     * @param tableName the name of the sheet
     * @param cellAddressFrom cell address from such as C5
     * @param cellAddressTo cell address to such as I5
     */
    public static void addColumnDataFilters(OdfContentDom contentDoc, String tableName, String cellAddressFrom, String cellAddressTo) {
        myLogger.entering(MY_CLASS_NAME, "addDataFilters()", new Object[]{contentDoc, tableName, cellAddressFrom, cellAddressTo});
        try{
            myLogger.finest("Creating the element to add to the OdfContentDom");
            OdfElement parentRangeElement = contentDoc.createElementNS("table", "table:database-ranges");
            OdfElement rangeElement = contentDoc.createElementNS("table", "table:database-range");
            rangeElement.setAttributeNS("table", "table:name", "__Anonymous_Sheet_DB__" + tableName);
            rangeElement.setAttributeNS("table", "table:target-range-address", buildCellRange(tableName, cellAddressFrom, cellAddressTo));
            rangeElement.setAttributeNS("table", "table:display-filter-buttons", "true");
            parentRangeElement.appendChild(rangeElement);

            myLogger.fine("Adding child element \"<table:database-ranges><table:database-range ... /></table:database-ranges>\" to the content dom for the auto filters");
            // get the element to add the rangeElement to
            NodeList nodeList = contentDoc.getElementsByTagName("office:spreadsheet");
            if(nodeList.getLength() > 0) {
                OdfElement parentElement = (OdfElement) nodeList.item(0);
                parentElement.appendChild(parentRangeElement);
            }//end if
        }catch(Exception e){
            myLogger.log(Level.WARNING, "Could not add an auto filter to the requested column due to an error. tableName=" + tableName + ", cellAddressFrom=" + cellAddressFrom + ", cellAddressTo=" + cellAddressTo + " Error is: " + e.getMessage(), e);
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "addDataFilters()");
    }

    /**
     * This is a helper method for building a cell address string.
     * @param tableName the name of the sheet.
     * @param cellAddressFrom cell from address
     * @param cellAddressTo cell to address
     * @return cell address range
     */
    private static String buildCellRange(String tableName, String cellAddressFrom, String cellAddressTo) {
        myLogger.entering(MY_CLASS_NAME, "buildCellRange()", new Object[]{tableName, cellAddressFrom, cellAddressTo});
        myLogger.exiting(MY_CLASS_NAME, "buildCellRange()");
        return new StringBuilder("'").append(tableName).append("'.").append(cellAddressFrom).append(":'").append(tableName).append("'.").append(cellAddressTo).toString();
    }

    /**
     * This method will set the background color of a cell.
     * @param cell the cell that is to to be colored in
     * @param dataCell1 true | false value determines what the color should be (candy striping)
     */
    public static void setDataCellColor(OdfTableCell cell, boolean dataCell1) {
        myLogger.entering(MY_CLASS_NAME, "setDataCellColor()", new Object[]{cell, dataCell1});
        if(dataCell1){
            cell.getOdfElement().setStyleName("DataCell1");
        }else{
            cell.getOdfElement().setStyleName("DataCell2");
        }//end if
        myLogger.exiting(MY_CLASS_NAME, "setDataCellColor()");
    }

    /**
     * This method will set the background color of a cell.
     * @param cell the sheet that contains the cell to be colored in
     */
    public static void setFormulaCellColor(OdfTableCell cell) {
        myLogger.entering(MY_CLASS_NAME, "setFormulaCellColor()", cell);
        myLogger.exiting(MY_CLASS_NAME, "setFormulaCellColor()");
        cell.getOdfElement().setStyleName("FormulaCell");
    }

    /**
     * This method will create a new sheet based upon the method parameters being passed into this method.
     * <p>
     *  <b>Example Usage:</b>
     *  <pre>
     *      OdfSpreadsheetDocument document = OdfSpreadsheetDocument.loadDocument(template);
     *      OfficeSpreadsheetElement spreadSheetElement = document.getContentRoot();
     *      OdfTable newTable = LogProcessorSpreadsheetUtil.createNewSheet(document, spreadSheetElement, "MyNewSheetName");
     *  </pre>
     *
     * @param document the document which is used for returning the new table
     * @param spreadSheetElement the spreadsheet element which is used for adding the new table element
     * @param newSheetName the name of the sheet
     * @return odfTable that gets returned.
     */
    public static OdfTable createNewSheet(OdfSpreadsheetDocument document, OfficeSpreadsheetElement spreadSheetElement, String newSheetName) {
        myLogger.entering(MY_CLASS_NAME, "createNewSheet()", new Object[]{document, spreadSheetElement, newSheetName});
        //create new table element
        TableTableElement tableElement = spreadSheetElement.newTableTableElement();
        tableElement.setTableNameAttribute(newSheetName);
        tableElement.setTablePrintAttribute(Boolean.FALSE);
        tableElement.setTableStyleNameAttribute("ta1");

        //table:style-name="co1" table:default-cell-style-name="Default"
        //create new column element
        TableTableColumnElement col1 = tableElement.newTableTableColumnElement();
        col1.setTableStyleNameAttribute("co1");
        col1.setTableDefaultCellStyleNameAttribute("Default");
        col1.setTableNumberColumnsRepeatedAttribute(Integer.valueOf(1));
        //create new row element
        TableTableRowElement rowEl = tableElement.newTableTableRowElement();
        rowEl.setTableStyleNameAttribute("ro1");
        rowEl.setTableNumberRowsRepeatedAttribute(Integer.valueOf(1));
        //create new cell element within the row element
        TableTableCellElement cell = rowEl.newTableTableCellElement(0, "");
        cell.setTableNumberColumnsRepeatedAttribute(Integer.valueOf(1));
        cell.removeAttribute("office:value");
        cell.removeAttribute("office:value-type");
        myLogger.exiting(MY_CLASS_NAME, "createNewSheet()");
        return document.getTableByName(newSheetName);
    }

    /**
     * This method will be updated to be a universal method if need be but for now just using it as a hardcoded method to set right justification
     * @param contentDom the content dom to use for adding the office automatic styles to.
     */
    public static void addOfficeAutomaticCustomStyles(OdfContentDom contentDom) {
        myLogger.entering(MY_CLASS_NAME, "addOfficeAutomaticCustomStyles()", contentDom);
        try{
            OdfStyle rightJustify = new OdfStyle(contentDom);
            rightJustify.setStyleNameAttribute("salas1");
            rightJustify.setStyleParentStyleNameAttribute("MixedUseCell");
            rightJustify.setStyleFamilyAttribute("table-cell");
            // add the properties Element
            StyleTableCellPropertiesElement styleTableCellProps = rightJustify.newStyleTableCellPropertiesElement();
            styleTableCellProps.setStyleTextAlignSourceAttribute("fix");
            styleTableCellProps.setStyleRepeatContentAttribute(Boolean.FALSE);

            StyleParagraphPropertiesElement styleParaProps = rightJustify.newStyleParagraphPropertiesElement();
            styleParaProps.setFoTextAlignAttribute("end");
            styleParaProps.setFoMarginLeftAttribute("0in");

            //            OdfStyle center = new OdfStyle(fileDom) ;
            //            center.setStyleNameAttribute("salasDataCell1");
            //            center.setStyleParentStyleNameAttribute("DataCell1");
            //            center.setStyleFamilyAttribute("table-cell");
            //
            //            // add the properties Element
            //            StyleTableCellPropertiesElement centerTableCellProps = center.newStyleTableCellPropertiesElement();
            //            centerTableCellProps.setStyleTextAlignSourceAttribute("fix");
            //            centerTableCellProps.setStyleRepeatContentAttribute(Boolean.FALSE);
            //
            //            StyleParagraphPropertiesElement centerParaProps = center.newStyleParagraphPropertiesElement();
            //            centerParaProps.setFoTextAlignAttribute("center");
            //            centerParaProps.setFoMarginLeftAttribute("0in");
            //
            //            OdfStyle center2 = new OdfStyle(fileDom) ;
            //            center2.setStyleNameAttribute("salasDataCell2");
            //            center2.setStyleParentStyleNameAttribute("DataCell2");
            //            center2.setStyleFamilyAttribute("table-cell");
            //
            //            // add the properties Element
            //            StyleTableCellPropertiesElement center2TableCellProps = center2.newStyleTableCellPropertiesElement();
            //            center2TableCellProps.setStyleTextAlignSourceAttribute("fix");
            //            center2TableCellProps.setStyleRepeatContentAttribute(Boolean.FALSE);
            //
            //            StyleParagraphPropertiesElement center2ParaProps = center2.newStyleParagraphPropertiesElement();
            //            center2ParaProps.setFoTextAlignAttribute("center");
            //            center2ParaProps.setFoMarginLeftAttribute("0in");

            NodeList nodeList = contentDom.getElementsByTagName("office:automatic-styles");
            if(nodeList.getLength() > 0) {
                OdfElement parentElement = (OdfElement) nodeList.item(0);
                parentElement.appendChild(rightJustify);
                //                parentElement.appendChild(center);
                //                parentElement.appendChild(center2);
            }//end if
        }catch(Exception e){
            myLogger.log(Level.WARNING, "Could not add the custome style to the content DOM. Error is: " + e.getMessage(), e);
        }//end try...catch
        myLogger.exiting(MY_CLASS_NAME, "addOfficeAutomaticCustomStyles()");
    }

    /**
     * This method will unused tables from within the spreadsheet.
     * @param document the document to delete the tables/sheets from
     * @param tableNames the names of the tables to delete.
     */
    public static void removeTables(OdfSpreadsheetDocument document, String[] tableNames) {
        myLogger.entering(MY_CLASS_NAME, "removeTables()", new Object[]{document, tableNames});
        for(int i = 0, j = tableNames.length;i<j;i++){
            try{
                document.getTableByName(tableNames[i]).remove();
            }catch(Exception e) {
                myLogger.log(Level.SEVERE, "Table Name " + tableNames[i] + " does not exist");
            }//end try...catch
        }//end for
        myLogger.exiting(MY_CLASS_NAME, "removeTables()");
    }



    //createHyperLink(int row, int col, String linkName, String destination);

    //    /**
    //     * This method adds the hyperlinks to page. It uses the File Dom to append the cells.
    //     *
    //     * @param xml
    //     *        ODFFileDom file dom xml
    //     * @param categorySheet
    //     *        Table category sheet
    //     */
    //    public static void addHyperLink(OdfFileDom xml, OdfTable categorySheet) {
    //        myLogger.entering(MY_CLASS_NAME, "addHyperLink()", new Object[]{xml, categorySheet});
    //        Long localMethodTime = 0L;
    //        if(myLogger.isLoggable(Level.FINE)){
    //            localMethodTime = System.currentTimeMillis();
    //        } // end if
    //        TextPElement pElement;
    //        TextAElement aElement;
    //
    //        myLogger.fine("Setting column width");
    //        categorySheet.getCellByPosition(7, 0).getTableColumn().setWidth(39L);
    //
    //        myLogger.fine("Building Header for column");
    //        categorySheet.getCellByPosition(7, 0).setStringValue("LINKS");
    //        categorySheet.getCellByPosition(7, 0).getOdfElement().setStyleName("ce3");
    //
    //        myLogger.fine("Building <text:p><text:a> elements for Users By Node hyperlink.");
    //        pElement = new TextPElement(xml);
    //        aElement = new TextAElement(xml);
    //        aElement.setXlinkHrefAttribute("#UsersByNode");
    //        aElement.setTextContent("Users By Node");
    //        categorySheet.getCellByPosition(7, 7).getOdfElement().setStyleName("Hyperlink");
    //        categorySheet.getCellByPosition(7, 7).getOdfElement().appendChild(addPopOutCommentToCell(xml, "This link forwards the user to the \"Users by Node\" page"));
    //        pElement.appendChild(aElement);
    //        categorySheet.getCellByPosition(7, 7).getOdfElement().appendChild(pElement);
    //        if(myLogger.isLoggable(Level.FINE)){
    //            myLogger.log(Level.FINE, "Finished with method addHyperLinks processing in " + AppUtil.getTimeTookInSecMinHours(localMethodTime));
    //        } // end if
    //        myLogger.exiting(MY_CLASS_NAME, "addHyperLinks()");
    //    }


}

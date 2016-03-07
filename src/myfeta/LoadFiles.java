package myfeta;

import com.fourspaces.couchdb.Document;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.SQLException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static myfeta.Main.setMonetDB;
import static myfeta.Main.collectionName;
import static myfeta.Main.verbose;

/**
 * Class for loading i) "virtuoso.log" in Document "queryLog" and/or ii)
 * "capture.log" in Document "endpointsAnswers"
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class LoadFiles {

    CouchDBManag myDB;
    MonetDBManag myMDB;
    DeductionUtils myDeductioUtils;
    BasicUtilis myBasUtils;

    //Creation date for xml type of logs, for virtuoso log as input 
    public static String date;
    //List of Documents stored in CouchDB, for "queryLog" and "answerLog"
    private List<Document> docList;
    //Index of document in which input file will be loaded, for CouchDB
    private int indxDoc;

    public LoadFiles(List<Document> listDocument, CouchDBManag db) throws ParserConfigurationException {

        docList = listDocument;
        myDB = db;
        date = "";

        myDeductioUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
    }

    public LoadFiles(MonetDBManag db) throws ParserConfigurationException {

        myMDB = db;
        date = "";

        myDeductioUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
    }

    /**
     * This method parses a source log file and counts its total number of lines
     *
     * @param fileName path to source log file
     * @return total number of log lines
     * @throws java.io.FileNotFoundException
     */
    public int lineNumberCount(String fileName) throws FileNotFoundException {

        int linesNumber = 0;

        /* Calculate maxLineNumber and  linePercentage */
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(new File(fileName)))) {
            lnr.skip(Long.MAX_VALUE);

            linesNumber = lnr.getLineNumber();

        } catch (IOException e) {

            e.printStackTrace(System.out);
        }

        return linesNumber;
    }

    /**
     * This method loads "capture.log" into the DB for "endpointsAnswers"
     *
     * @param logPath path to capture.log
     * @throws java.io.FileNotFoundException
     * @throws java.sql.SQLException
     * @throws javax.xml.transform.TransformerException
     */
    public void loadQuersAndAns(String logPath) throws IOException, FileNotFoundException, SQLException, TransformerException {

        if (!setMonetDB) {

            myDB.createfirstDocument("endpointsAnswers" + collectionName);
        }

        loadTotalLog(logPath);
    }

    public void loadTotalLog(String logPath) throws FileNotFoundException, IOException, SQLException, TransformerException {

        //Name of CouchDB Document, where endpoints Answers will be stored
        if (!setMonetDB) {

            indxDoc = myDB.indexOfDocument("endpointsAnswers" + collectionName);
        }

        System.out.println("Document Loading...");
        // Path to "capture.log"
        String fileName = logPath;
        // Fille parser over the load file
        BufferedReader br = null;
        // Current parsed line of "capture.log"
        String sCurrentLine;
        // Current line number of apture.log
        int ligneNumber = 0;
        // Info to capture of each endpoint answer packet
        String answerMappings = "", ipClient = "172.16.8.89", portClient = "none", portEndpoint = "", time = "", queryXDec = "", queryASCII = "";
        int totalNumbers = lineNumberCount(fileName);
        int cntNumberPackets = 0;
        boolean flagPacket = false;
        boolean flagAnsw = false;
        boolean flagANAPSID = false;

        System.out.println("totalNumbers " + totalNumbers);

        try {

            br = new BufferedReader(new FileReader(fileName));

            while ((sCurrentLine = br.readLine()) != null) {

                ligneNumber++;

                // System.out.println("currentLine " + LineNumberReader);
                if ((sCurrentLine.contains("POST") || (sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) && !flagPacket) {

                    flagAnsw = false;
                    flagPacket = true;

                    System.out.println("currentLine " + ligneNumber);

                    if ((sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) {

                        flagANAPSID = true;
                    }

                    if (flagANAPSID && queryXDec.equals("")) {

                        queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("?query") + 7, sCurrentLine.indexOf("&format"));
                        queryASCII = myBasUtils.convExToASCII(queryXDec);
                    }

                } else if (flagPacket) {

                    if (sCurrentLine.contains("POST") || (sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query")) || (sCurrentLine.contains("GET"))) {
                        //          if (sCurrentLine.contains("POST")|| (sCurrentLine.contains("GET")&&sCurrentLine.contains("/sparql/?query"))) {
                        System.out.println("currentLine " + ligneNumber);

                        if ((sCurrentLine.contains("GET")) && !sCurrentLine.contains("/sparql/?query")) {

                            System.out.println("");
                            flagPacket = false;
                        }
                        if (verbose) {

                            System.out.println("--------------------------------------");
                            System.out.println("No of packet captured: " + cntNumberPackets);
                            System.out.println("portEndpoint: " + portEndpoint);
                            System.out.println("time: " + time);
                            System.out.println("ipClient: " + ipClient);
                            System.out.println("portClient: " + portClient);
                            System.out.println("answerMappings: " + answerMappings);
                            System.out.println("query XDecimal: " + queryXDec);
                            System.out.println("query ASCII: " + queryASCII);
                            System.out.println("---------------------------------------");

                            if (queryASCII.contains("micronutrient") && queryASCII.contains("SELECT")) {
                                int razraz = 0;
                            }
                        }

                        answerMappings += "\n";
                        if (setMonetDB) {

                            myMDB.saveEntryAnswers("TableEntryAnswers", ipClient, portClient + Integer.toString(cntNumberPackets), portEndpoint, answerMappings, time, queryASCII, indxDoc);

                        } else {

                            myDB.saveEntryAnswers(ipClient, portClient + Integer.toString(cntNumberPackets), portEndpoint, answerMappings, time, queryASCII, cntNumberPackets);

                        }
                        if (cntNumberPackets == 2) {

                            //   break;
                        }
                        cntNumberPackets++;
                        answerMappings = "";
                        portEndpoint = "";
                        time = "";

                        queryXDec = "";
                        queryASCII = "";

                        flagAnsw = false;

                        /**
                         * *******************BUUUUUUUUUUUUUUUUUUG************************
                         */
                        flagANAPSID = false;

                        if ((sCurrentLine.contains("GET") && sCurrentLine.contains("/sparql/?query"))) {

                            flagANAPSID = true;
                        }

                        if (flagANAPSID && queryXDec.equals("")) {

                            queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("?query") + 7, sCurrentLine.indexOf("&format"));
                            queryASCII = myBasUtils.convExToASCII(queryXDec);
                        }
                    } else {

                        if (sCurrentLine.contains("Host: ")) {

                            String tmpLine = sCurrentLine.substring(sCurrentLine.indexOf("Host: ") + 6);
                            portEndpoint = tmpLine.substring(tmpLine.indexOf(":") + 1);
                        } else if (sCurrentLine.contains("Date:")) {

                            time = sCurrentLine.substring(sCurrentLine.indexOf("GMT") - 9, sCurrentLine.indexOf("GMT") - 1);
                        } else if (sCurrentLine.contains("\"head\"") || flagAnsw) {

                            flagAnsw = true;
                            answerMappings += sCurrentLine;
                        } else if (sCurrentLine.contains("queryLn=SPARQL&query=") && !flagANAPSID) {

                            queryXDec = sCurrentLine.substring(sCurrentLine.indexOf("&query=") + 7, sCurrentLine.indexOf("&infer=false"));
                            queryASCII = myBasUtils.convExToASCII(queryXDec);
                        }
                    }

                }

            }

        } finally {
            try {
                if (br != null) {

                    br.close();
                }
            } catch (IOException ex) {

                ex.printStackTrace(System.out);
            }
        }

        System.out.println("******************************************");
        System.out.println("Total number of pacjet captured: " + cntNumberPackets);
        System.out.println("******************************************");
    }
}

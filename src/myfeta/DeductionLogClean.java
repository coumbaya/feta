package myfeta;

import com.fourspaces.couchdb.Document;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom2.JDOMException;

import static myfeta.Deduction.mapAnsIDToLogClQuery;
import static myfeta.Deduction.mapAnsIDToQueryEnts;
import static myfeta.Deduction.mapAnsIDToQueryProjVars;
import static myfeta.Deduction.mapAnsIDToTimeSecs;
import static myfeta.Deduction.mapAnsIDtoEntry;
import static myfeta.Deduction.mapLogClQueryToAllTPEnts;
import static myfeta.Deduction.mapLogClQueryToAnsEntry;
import static myfeta.Deduction.mapLogClQueryToProjVars;
import static myfeta.Deduction.mapLogClQueryToTimeSecs;
import static myfeta.Deduction.mapLogClQueryToTimestamp;
import static myfeta.Main.nameDB;
import static myfeta.Main.setMonetDB;

/**
 * Class for "LogClean" heuristic, filtering exactly the same subqueries or
 * merging same subqueries sent to the same or different endpoints
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-03-19
 */
public class DeductionLogClean {

    CouchDBManag myDB;
    MonetDBManag myMDB;
    BasicUtilis myBasUtils;
    DeductionUtils myDedUtils;

    // all distinct queries which are captured, after "Log CLean" phase
    public static List<String> queries;
    // map each LogClean query to its associated LogCLean id 
    public static HashMap<String, Integer> mapQueryToID;
    // map each LogClean query to its source endpoints
    public static HashMap<Integer, List<String>> mapQuerytoSrcEndps;
    // map each candidate triple pattern with FILTER options of UNION queries 
    public static HashMap<List<String>, List<String>> mapCTPtoFILTERwithBoundJ;
    //capture the IP Address
    public static String engineIPAddress = "";
    //capture the first timestamp of the slice DB used as input, and print it in "verbose2"
    String epochTimeCurrent = "";
    String prevTime = "";

    String startTimeQuery = "";
    String stopTimeQuery = "";

    public DeductionLogClean(List<Document> listDocument, MonetDBManag db) throws ParserConfigurationException {

        myMDB = db;
        myBasUtils = new BasicUtilis();
        myDedUtils = new DeductionUtils();
        mapQueryToID = new HashMap<>();
        mapQuerytoSrcEndps = new HashMap<>();
        mapQuerytoSrcEndps = new HashMap<>();
        mapCTPtoFILTERwithBoundJ = new HashMap<>();
        queries = new LinkedList<>();
        epochTimeCurrent = "";
    }

    public DeductionLogClean(List<Document> listDocument, CouchDBManag db) throws ParserConfigurationException {

        myDB = db;
        myBasUtils = new BasicUtilis();
        myDedUtils = new DeductionUtils();
        mapQueryToID = new HashMap<>();
        mapQuerytoSrcEndps = new HashMap<>();
        mapCTPtoFILTERwithBoundJ = new HashMap<>();
        queries = new LinkedList<>();
        epochTimeCurrent = "";
    }

    public DeductionLogClean() throws ParserConfigurationException {

        myBasUtils = new BasicUtilis();
        myDedUtils = new DeductionUtils();
        mapQueryToID = new HashMap<>();
        mapQuerytoSrcEndps = new HashMap<>();
        mapCTPtoFILTERwithBoundJ = new HashMap<>();
        queries = new LinkedList<>();
        epochTimeCurrent = "";
    }

    /**
     * Purify query log entries that will be used as input to
     * "GraphConstruction" heuristic, for different cases:
     *
     * (i) exactly the same subquery sent from the same query engine to the same
     * endpoint, two times sequentially
     *
     * (ii) exactly the same subquery sent from the same query engine to at
     * least two different endpoints for complete answers
     *
     * (iii) the same subquery sent from the same query engine to the same
     * endpoint, at least two times, but with different OFFSETs to avoid
     * reaching limit responde
     *
     * @param window deduction windowDeduction in seconds
     * @throws java.sql.SQLException
     * @throws javax.xml.transform.TransformerException
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void LogClean(int window) throws SQLException, TransformerException, JDOMException, IOException, InstantiationException, IllegalAccessException {

        long startTime = 0;
        long finishTime = 0;
        long elapsedTime = 0;
        int monetSize = -1;
        List<String> entryInfo = null;
        String epochTime = "";

        System.out.println("[START] ---> LogClean heuristic");
        System.out.println();
        startTime = System.nanoTime();

        // Scan all entries of the database traces, for MonetDB or CouchDB
        if (setMonetDB) {

            monetSize = myMDB.getTableSize("tableQrsAndAns" + nameDB);

            for (int i = 1; i < monetSize; i++) {

                entryInfo = myMDB.getEntryAnswers(i);

                if (!entryInfo.isEmpty()) {

                    //In MonetDB, Table entries start from 1
                    if (i == 1) {

                        epochTime = entryInfo.get(3);
                    }

                    //Skip some kind of queries BUUUUUUUG must skip only ?s ?p ?o qeries of anapsid
                    if (entryInfo.get(4).contains("ASK") || entryInfo.get(4).contains("?s ?p ?o")
                            || !(entryInfo.get(4).contains("SELECT") || entryInfo.get(4).contains("Select")
                            || entryInfo.get(4).contains("select"))) {

                        continue;
                    }

                    checkIfQueryExist(entryInfo, epochTime, i);
                }
            }
        } else {

            for (int key : mapAnsIDtoEntry.keySet()) {

                entryInfo = myDB.getEntryAnswerLogHashMap(mapAnsIDtoEntry, key);

                if (!entryInfo.isEmpty()) {

                    //In CouchDB, i=0 and i=1 correspond to a Documents "_id" and "_rev", respectively
                    // BUUUUUUUUUUUUUUUUUG  key ==2
                    if (key == 2) {

                        epochTime = entryInfo.get(3);
                    }

                    //Skip some kind of queries BUUUUUUUG must skip only ?s ?p ?o qeries of anapsid
                    if (entryInfo.get(2).contains("ASK") || entryInfo.get(2).contains("?s ?p ?o")
                            || !(entryInfo.get(2).contains("SELECT") || entryInfo.get(2).contains("Select")
                            || entryInfo.get(2).contains("select"))) {

                        continue;
                    }

                    checkIfQueryExist(entryInfo, epochTime, key);
                }

            }

        }

        //match each query's string converted to seconds
        for (int key : mapLogClQueryToTimestamp.keySet()) {

            String currString = mapLogClQueryToTimestamp.get(key);
            mapLogClQueryToTimeSecs.put(key, myBasUtils.getTimeInSec(currString));
        }

        System.out.println("\t================ Real time interval of DB slice, used as FETA input: [" + startTimeQuery + ", " + stopTimeQuery + "] "
                + "and duration: " + (myBasUtils.getTimeInSec(stopTimeQuery) - myBasUtils.getTimeInSec(startTimeQuery)) + " seconds ================");
        System.out.println();

        printDistinctQueries();

        finishTime = System.nanoTime();
        elapsedTime = finishTime - startTime;

        System.out.println("[FINISH] ---> LogClean heuristic (Elapsed time: " + elapsedTime / 1000000000 + " seconds)");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    /**
     * Check if the current trace, correspond to a subquery or endpoint port
     *
     * @param entryInfo answer information with all captured data
     * @param epochTime start epoche time of FETA deducion, in HH:MM:SS format
     * @param AnsEntryID answer entry id to be parsed
     */
    public void checkIfQueryExist(List<String> entryInfo, String epochTime, int AnsEntryID) {

        String Port = "", IpAddress = "", Time = "", Query = "";
        boolean flagPortExist = true;
        int indxQuery = 0;
        int timeInSec = 0;
        List<String> queryUnities = null;

        if (!setMonetDB) {

            IpAddress = entryInfo.get(0);
            Port = entryInfo.get(1);
            Query = entryInfo.get(2).replaceAll(("\r\n"), " ");
            Time = entryInfo.get(3);
        } else {

            IpAddress = entryInfo.get(2);
            Port = entryInfo.get(1);
            Query = entryInfo.get(4).replaceAll(("\r\n"), " ");
            Time = entryInfo.get(3);
        }

        // BUUUUUUUUUUUUUUUUUG 
        if (!Time.contains(":")) {
            Time = prevTime;
            Time = stopTimeQuery;
        } else {
            prevTime = Time;
        }

        if (engineIPAddress.equalsIgnoreCase("") && !IpAddress.equalsIgnoreCase("")) {

            engineIPAddress = IpAddress;
        }

        timeInSec = myBasUtils.getTimeInSec(Time);

        if (!Time.equalsIgnoreCase("") && startTimeQuery.equalsIgnoreCase("")) {

            startTimeQuery = Time;
        }

        if (!Time.equalsIgnoreCase("") && !startTimeQuery.equalsIgnoreCase("")) {

            stopTimeQuery = Time;
        }

        if (Time.equalsIgnoreCase("")) {

            Time = stopTimeQuery;
        }

        if (epochTimeCurrent.equals("")) {

            epochTimeCurrent = Time;
        }

        queryUnities = myBasUtils.getQueryEntities(Query, 4, false);

        if (Query.contains("UNION") && Query.contains("FILTER") && Query.contains("_0")) {

            myDedUtils.setCTPtoFilterBoundVals(Query, queryUnities);
        }

        mapAnsIDToTimeSecs.put(AnsEntryID, timeInSec);
        mapAnsIDToQueryEnts.put(AnsEntryID, queryUnities);
        mapAnsIDToQueryProjVars.put(AnsEntryID, myBasUtils.getProjVars(Query));

        //If its the first time we capture this query
        if (mapQueryToID.get(Query) == null) {

            int querySize = queries.size();
            queries.add(Query);
            mapLogClQueryToProjVars.put(querySize, myBasUtils.getProjVars(Query));
            myBasUtils.insertToMap(mapQuerytoSrcEndps, Port, querySize);
            mapAnsIDToLogClQuery.put(AnsEntryID, Integer.toString(queries.size() - 1));
            myBasUtils.insertToMap2(mapLogClQueryToAnsEntry, AnsEntryID, queries.size() - 1);
            mapLogClQueryToTimestamp.put(queries.size() - 1, Time);
            mapQueryToID.put(Query, querySize);
            mapLogClQueryToAllTPEnts.put(queries.size() - 1, queryUnities);
            flagPortExist = false;
        } else {

            indxQuery = mapQueryToID.get(Query);
            if (!myBasUtils.elemInListContained(mapQuerytoSrcEndps.get(indxQuery), Port)) {

                flagPortExist = false;
            } else {

                flagPortExist = true;
            }

            // else, this current entry's reception endpoint has not been identified yet 
            if (flagPortExist == false) {

                flagPortExist = true;
            }


            if (!myBasUtils.elemInListContained(mapQuerytoSrcEndps.get(indxQuery), Port)) {

                myBasUtils.insertToMap(mapQuerytoSrcEndps, Port, indxQuery);
            }

            mapAnsIDToLogClQuery.put(AnsEntryID, Integer.toString(indxQuery));
            myBasUtils.insertToMap2(mapLogClQueryToAnsEntry, AnsEntryID, indxQuery);
            mapLogClQueryToTimestamp.put(indxQuery, Time);
        }
    }

    /**
     * Print "Log Clean" queries
     */
    public void printDistinctQueries() {

        System.out.println("\t******************************************************************************************************************************************************************************************\t\t");
        System.out.println("\t******************************************************************************************************************************************************************************************\t\t");

        for (int i = 0; i < queries.size(); i++) {

            System.out.println("\t**ID " + (i + 1) + " ** " + queries.get(i) + "*\t\t\t");
            System.out.println("\t******************************************************************************************************************************************************************************************\t\t");
        }

        System.out.println("\t******************************************************************************************************************************************************************************************\t\t");
        System.out.println();
    }

}
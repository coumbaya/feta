package myfeta;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;

import static myfeta.Deduction.mapAnsIDtoEntry;
import static myfeta.Main.verbose;
import static myfeta.Deduction.docAnswers;
import static myfeta.Main.traceGen;

/**
 * Class for interacting with DB (CouchDB)
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class CouchDBManag {

    private Database db;
    private Session dbSession;

    BasicUtilis myBasUtils;
    DeductionUtils myDecUtils;
    
     FileWriter writerCapt;


    // List of Documents stored in database, ("queryLog" or "endpointsAnswers")
    public static List<Document> myListDocs;
    // Document "endpointsAnswer", called by 2 functions
    public Document docsDist;
    // Current Document of "querLogs" with log of entries in virtuoso.log
    private Document myDoc;
    // Current entry id for "myDoc" or "myDoc2": we start from 2 as we have 2
    //entries by default in a CouchDB document, "_id" and "_rev" 
    private int idEntAns = 2;
    // Current id of Document of List "myDoc"
    private int idDoc;

    public CouchDBManag() throws IOException {

        myListDocs = new LinkedList<>();
        myDecUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
        if(traceGen){
           writerCapt = new FileWriter("capture.txt");
        }
    }

    /**
     * Open a new session with the DB
     *
     * @param localhostAddr IP address host of couchdb server
     * @param localPort couchdb server port, by default "5984"
     */
    public void openSession(String localhostAddr, int localPort) {

        dbSession = new Session(localhostAddr, localPort);
    }

    public void openSession2(String localhostAddr, int localPort, String userName, String password) {

        dbSession = new Session(localhostAddr, localPort, userName, password);
    }

    /**
     * Get the already created DB
     *
     * @param dbName the name of DB to be loaded
     */
    public void getDatabase(String dbName) {

        db = dbSession.getDatabase(dbName);
    }

    /**
     * Create a new DB
     *
     * @param dbName the name of DB to be created
     */
    public void createDatabase(String dbName) {

        db = dbSession.createDatabase(dbName);
    }

    /**
     * Reset an exisiting DB
     *
     * @param dataBase DB name to be reset
     */
    public void resetDB(String dataBase) {

        if (verbose) {

            System.out.println("reCreateDataBase");
        }

        deleteDatabase(dataBase);
        createDatabase(dataBase);
    }

    /**
     * Delete an existing DB
     *
     * @param dbName DB name to reset
     */
    public void deleteDatabase(String dbName) {

        if (!dbName.equals("")) {

            dbSession.deleteDatabase(dbName);
        }

    }

    /**
     * Create the fist Document where entries will be loaded
     *
     * @param currDoc the name of first Document that will be created
     */
    public void createfirstDocument(String currDoc) {

        myDoc = new Document();
        myDoc.setId(currDoc);
        myListDocs.add(myDoc);
    }

    /**
     * Add a new Document of the same type ("querylog" or "endpointsAnswers")
     * into current list of Documents
     *
     * @param currDoc the name of current Document that will be added
     */
    public void addDocument(String currDoc) {

        myListDocs.add(db.getDocument(currDoc));
    }

    /**
     * Get the the current Docment from the list of all Documents
     *
     * @param currDoc the name of current Document that will be returned
     * @return the Document that was passed as parameter
     */
    public Document getDocument(String currDoc) {

        return myListDocs.get(indexOfDocument(currDoc));
    }

    /**
     * Get the id of the current Docment from the list of all Documents
     *
     * @param currDoc the name of current Document that will be returned
     * @return the Document that was passed as parameter
     */
    public int indexOfDocument(String currDoc) {

        for (int i = 0; i < myListDocs.size(); i++) {
            if (myListDocs.get(i).getId().equals(currDoc)) {

                return i;
            }
        }
        return -1;
    }

    /**
     * Get all created Documents
     *
     * @return list of Documents
     */
    public List<Document> getDocList() {

        return myListDocs;
    }

    /**
     * Get the id of the current Docment from the list of all Documents
     *
     * @return id of the current Doument
     */
    public int getIdDoc() {

        return idDoc;
    }

    /**
     * Save a new entry into current Document "endpointsAnswers"
     *
     * Every entry is represented as five-tuple of the form
     *
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime,
     * ClientIPAdress>
     *
     * Each entry is saved as a Document into the Document "endpointsAnswers"
     *
     * @param clientIpAddress query engine's IPAddress
     * @param ClientTCPport query engine's TCP port connexion
     * @param endpointPort virtuoso's sender endpoint port
     * @param answer endpoint's answer, in jason format for SPARQL queries
     * @param time query's reception time at virtuoso log
     * @param query query engine's request query
     * @param indexDoc index of the Document to which the entry will be stored
     */
    public void saveEntryAnswers(String clientIpAddress, String ClientTCPport,
            String endpointPort, String answer, String time, String query, int indexDoc) {

        if (verbose) {

            System.out.println("--------------------------------------------");
        }

        Document doc = new Document();

        doc.setId(Integer.toString(idEntAns));
        doc.put("ClientIpAddress", clientIpAddress);
        doc.put("ClientTCPport", ClientTCPport);
        doc.put("SPARQLEndpointPort", endpointPort);
        doc.put("Answer", answer);
        doc.put("ReceptionTime", time);
        doc.put("RequestQuery", query);

        myListDocs.get(0).put(Integer.toString(myListDocs.get(0).size() + 1), doc);
        idEntAns++;
        saveFinalDocument(myListDocs.get(0));
    }

    /**
     * Default save entry function of CouchDB: "saveDocAnswers" and
     * "saveEntryQueryLog", call this function once they the number of entries
     * has been treated
     *
     * @param currDoc name of Document into which entries will be saved
     */
    public void saveFinalDocument(Document currDoc) {

        if (verbose) {

            System.out.println("saveFinalDocument");
            System.out.println(currDoc.getId());
        }

        db.saveDocument(currDoc);
    }

    /**
     * Get all information of an answer entry, from "endpointsAnswers"
     *
     * @param idEntry the current answer entry
     * @return all specific entry's information
     */
    public List<String> getAnswerEntry(String idEntry) {

        List<String> Entry = new LinkedList<>();

        JSONObject entryClient = null;

        try {

            entryClient = (JSONObject) docAnswers.getJSONObject(String.valueOf(idEntry));

            Entry.add(0, entryClient.get("Answer").toString());
            Entry.add(1, entryClient.get("SPARQLEndpointPort").toString());
            Entry.add(2, entryClient.get("ClientIpAddress").toString());
            Entry.add(3, entryClient.get("ReceptionTime").toString());
            Entry.add(4, entryClient.get("RequestQuery").toString());
            Entry.add(5, entryClient.get("ClientTCPport").toString());

        } catch (Exception ex) {
            System.out.println(ex);
        }

        return Entry;
    }

    /**
     * Get all answer entry information, from the Answer Log hash map
     *
     * @param ansDocMap the input "endpointsAnswers" hashMap
     * @param idEntry the current answer entry
     * @return
     */
    public List<String> getEntryAnswerLogHashMap(Map<Integer, List<String>> ansDocMap, int idEntry) {

        List<String> Entry = new LinkedList<>();
        List<String> entryInformation = new LinkedList<>();

        Entry = mapAnsIDtoEntry.get(idEntry);

        //BUUUUUUUUUUUUUUUUUUUUUUUUGGGGGGGGGGGGGGGG
        if (Entry.size() == 6) {

            entryInformation.add(Entry.get(2));
            entryInformation.add(Entry.get(1));

            int indexStartLIMIT = Entry.get(4).indexOf("LIMIT");

            if (indexStartLIMIT > 0) {

                entryInformation.add(Entry.get(4).substring(0, indexStartLIMIT - 1));
            } else {

                entryInformation.add(Entry.get(4));
            }

            entryInformation.add(Entry.get(3));
        }

        return entryInformation;
    }

    /**
     * Parse every answer string, and match all answer entities (IRIs/Literals)
     * to the corresponding hashMaps
     */
    public void setAnswerStringToMaps() throws IOException, URISyntaxException {
        
       
        List<String> entryInformation = null;
        String Answer = "", ClientIpAddress = "", requestQuery = "", receptTime = "", endpoint = "";
        for (Object key : docAnswers.keySet()) {
            
            try {
                if (key.toString().contains("_")) {
                    continue;
                }

                entryInformation = getAnswerEntry(key.toString());
                 if(traceGen){
            
                     addEntryInTrace(entryInformation);
                } 
                mapAnsIDtoEntry.put(Integer.parseInt(key.toString()), entryInformation);

                if (!entryInformation.isEmpty()) {

                    entryInformation = mapAnsIDtoEntry.get(Integer.parseInt(key.toString()));
                    if (!entryInformation.isEmpty()) {

                        Answer = entryInformation.get(0);
                        endpoint = entryInformation.get(1);
                        ClientIpAddress = entryInformation.get(2);
                        receptTime = entryInformation.get(3);
                        requestQuery = entryInformation.get(4);
                        myBasUtils.setVarsToAnswEntities(Integer.parseInt(key.toString()), requestQuery, Answer);
                    }

                }

            } catch (NumberFormatException e) {

                System.out.println(e);
            }

        }
    }
    
    public void addEntryInTrace(List<String> entryInformation) throws IOException, URISyntaxException{
        
        for(int i=0; i<entryInformation.size();i++){
            
            if(i==4){
        URI uri = new URI("http", null, "localhost", 8900,
                "/sparql/", "default-graph-uri=&query=" + entryInformation.get(i) + "&format=json&timeout=0&debug=on", null);

       writerCapt.write( uri.toString());
            }
            
            else{
                
           writerCapt.write(entryInformation.get(i));
            }
        }

        System.out.print("****************");
    }

}
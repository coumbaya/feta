package myfeta;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static myfeta.Deduction.docAnswers;
import static myfeta.Deduction.mapAnsIDtoEntry;
import static myfeta.Main.nameDB;
import static myfeta.Main.traceGen;
import static myfeta.Main.verbose;

/**
 * Class for interacting with DB (MonetDB)
 *
 * @author TRISTAN Jarry, PICHAUD Thibaut, Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class MonetDBManag {

    DeductionUtils myDecUtils;
    BasicUtilis myBasUtils;

    // map each  answer entry with its associated string answer ==> saves a lot of time when searching a specific answer entry's specific answers
    public static HashMap<Integer, List<String>> mapAnswerEntryToAnswerEntities;

    // Current id of Tables
    private int IDLog;
    private int IDAns;

    ResultSet rs;
    Statement st;

    public MonetDBManag() {

        myDecUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
        mapAnswerEntryToAnswerEntities = new HashMap<>();
        IDLog = 1;
        IDAns = 1;
    }

    /**
     * Open a new session with the MonetDB
     *
     * @param dbIPAddress address of local or distant monetDB server
     * @param pass
     * @param user
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void openSession(String dbIPAddress, String user, String pass) throws SQLException, InstantiationException, IllegalAccessException {

        //make sure the ClassLoader has the monetDB JDBC driver loaded
        try {

            Class.forName("nl.cwi.monetdb.jdbc.MonetDriver").newInstance();
        } catch (ClassNotFoundException e) {

        }

        // request a Connection to a monetDB server running on 'localhost'
        Connection con = DriverManager.getConnection(dbIPAddress, user, pass);
        st = con.createStatement();
    }

    /**
     * This method saves a new entry into current table "queryLog".
     *
     * Every entry is represented as five-tuple of the form
     * <IdEntry, ClientIpAddress, SPARQLEndpoint, Query, ReceptionTime>
     *
     * @param clientIpAddress
     * @param endpointPort virtuoso's endpoint reception port
     * @param query virtuoso's endpoint recieved query
     * @param time answer's reception time in virtuoso query Log
     * @param save boolean variable to force saving values in a specific Doc
     * @throws SQLException
     */
    public void saveEntryQueryLog(String clientIpAddress, String endpointPort, String query, String time, boolean save) throws SQLException {

        if (verbose) {

            st.executeUpdate("INSERT INTO feta.tablequerylog " + "VALUES ('" + IDLog + "','" + clientIpAddress + "','" + endpointPort + "','" + query + "', '" + time + "');");
            IDLog++;
        }
    }

    /**
     * Delete a table in Monet DB
     *
     * @param table
     * @throws SQLException
     */
    public void deleteTable(String table) throws SQLException {

        System.out.println("Deleting table: " + table);

        st.executeQuery("DROP TABLE  " + table + ";\n");
        System.out.println("Document deleted: " + table);
    }

    /**
     * Delete all tables in Monet DB
     *
     * @param dbName name of DB to delete
     * @throws SQLException
     */
    public void deleteDatabase(String dbName) throws SQLException {

        if (dbName.equals("")) {

        } else {

            st.executeUpdate("DROP TABLE EntryQueryLogTable;");
            st.executeUpdate("DROP TABLE EntryAnswersTable;");
            // st.executeUpdate("DROP TABLE EntryEngineOutputTable;");
        }

    }

    /**
     * This method save a new entry into table "endpointsAnswers"
     *
     * Every entry is represented as five-tuple of the form
     * <IdEntry, Answer, CLientTCPport, SPARQLEndpoint, ReceptionTime,
     * ClientIPAdress>
     *
     * @param table table in which entries will be saved
     * @param clientIpAddress query engine's IPAddress
     * @param clientTCPport query engine's TCP port connexion
     * @param endpointPort virtuoso's sender endpoint port
     * @param answer endpoint's answer, sent in json format for SPARQL queries
     * @param time answer's reception time in query engine
     * @param query query engine's request query
     * @param indexEntry new entry to be created with above info
     * @throws SQLException
     */
    public void saveEntryAnswers(String table, String clientIpAddress, String clientTCPport,
            String endpointPort, String answer, String time, String query, int indexEntry) throws SQLException, IOException {

        String none = "";
        List<String> ansToVarList = null;
        List<String> answToVarVals = null;
        List<List<String>> answToAllVarVals = new LinkedList<>();

        if (verbose) {

            System.out.println("--------------------------------------------");
        }

        if (query.contains("\'")) {

            query = query.replace("\'", "\"");
        }
        
            if (answer.contains("\'")) {

            answer = answer.replace("\'", "\"");
        }
        
        String ansCopy=answer;
         /* try (
                    FileWriter writer = new FileWriter("000.txt")) {
                writer.write(answer);
            }*/


        //st.executeUpdate("INSERT INTO tableQrsAndAns" + " VALUES ('" + IDAns + "','" + clientIpAddress + "', '" + clientTCPport + "', '" + endpointPort + "', '" + answer + "', '" + time + "', '" + query + "');");
        st.executeUpdate("INSERT INTO tableQrsAndAns" + nameDB + "" + " VALUES ('" + IDAns + "','" + clientIpAddress + "', '" + clientTCPport + "', '" + endpointPort + "', '" + answer + "', '" + time + "', '" + query + "');");
        IDAns++;

    }

    /**
     * Delete a table in Monet DB
     *
     * @param dbname
     * @throws SQLException
     */
    public void createDB(String dbname) throws SQLException {

        System.out.println("Create " + dbname);

        try {
            Runtime.getRuntime().exec("monetdb create " + dbname + "");
            Runtime.getRuntime().exec("monetdb release " + dbname + "");
        } catch (IOException ex) {
        }

        System.out.println("Document Createed: " + dbname);
    }

    /**
     * Set each IRI/Literal with its mapping variable to the corresponding
     * table, for a specific answer entry
     *
     * @param idTable id corresponding to answer entry, used to name the table
     * @param varList lists of all different variables in the ans
     * @param answListPerVar
     * @throws SQLException
     */
    public void setTablePerAnswEntry(int idTable, List<String> varList, List<List<String>> answListPerVar) throws SQLException {

        for (int i = 0; i < answListPerVar.size(); i++) {

            for (int j = 0; j < answListPerVar.get(i).size(); j++) {

                st.executeUpdate("INSERT INTO tableAnswEntry" + idTable
                        + " VALUES ('" + j + 1 + "','" + varList.get(i) + "', '" + answListPerVar.get(i).get(j) + "');");
            }
        }

    }

    /**
     * Reset all tables (tablequerylog and tableQrsAndAns), in the existing
     * monetDB
     *
     * @throws SQLException
     */
    public void resetMDB() throws SQLException {

        System.out.println("reCreateDataBase");

        st.executeUpdate("DROP TABLE feta.tableQrsAndAns" + nameDB + ";\n");
        IDAns = 1;
        System.out.println("Table deleted: tableQrsAndAns" + nameDB);

        st.executeUpdate("CREATE TABLE tableQrsAndAns" + nameDB + " "
                + "(ID VARCHAR(1000), "
                + "ClientIPAddress VARCHAR(10000), "
                + " ClientTCPport VARCHAR(10000), "
                + " SPARQLEndpointPort VARCHAR(10000), "
                + " Answer VARCHAR(1410065408), "
                + " ReceptionTime VARCHAR(10000), "
                + " RequestQuery VARCHAR(100000))");

        System.out.println("Table added: tableQrsAndAns" + nameDB);
    }

    /**
     * Create all tables (tablequerylog and tableQrsAndAns), in the existing
     * monetDB
     *
     * @throws SQLException
     */
    public void createMDB() throws SQLException {

        IDLog = 1;
        IDAns = 1;
        
        st.executeUpdate("CREATE TABLE tableQrsAndAns" + nameDB + " "
                + "(ID VARCHAR(1000), "
                + "ClientIPAddress VARCHAR(10000), "
                + " ClientTCPport VARCHAR(10000), "
                + " SPARQLEndpointPort VARCHAR(10000), "
                + " Answer VARCHAR(1410065408), "
                + " ReceptionTime VARCHAR(10000), "
                + " RequestQuery VARCHAR(100000))");

        System.out.println("Table added: tableQrsAndAns" + nameDB);
    }

    /**
     * Get number of entries, for a specific table
     *
     * @param tableName
     * @return
     */
    public int getTableSize(String tableName) {

        int count = 0;

        try {
            rs = st.executeQuery("SELECT COUNT(*) FROM feta." + tableName + "");
            while (rs.next()) {

                count = Integer.parseInt(rs.getString("L1"));

            }

        } catch (SQLException ex) {

            Logger.getLogger(MonetDBManag.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    /**
     * Get a specific entry information, from "tablequerylog" table
     *
     * @param idEntry
     * @return
     * @throws SQLException
     */
    public List<String> getEntryQueryLog(int idEntry) throws SQLException {

        List<String> entryInformation = new LinkedList<>();

        rs = st.executeQuery("SELECT * FROM feta.tablequerylog WHERE ID = '" + idEntry + "'");

        while (rs.next()) {

            //String id = rs.getString("idEntry");
            String ip = rs.getString("ClientIPAddress");
            String ep = rs.getString("SPARQLEndpointPort");
            String qu = rs.getString("Query");
            String rt = rs.getString("ReceptionTime");

            entryInformation.add(ip);
            entryInformation.add(ep);
            entryInformation.add(qu);
            entryInformation.add(rt);
        }

        return entryInformation;
    }

    /**
     * Get a specific entry information, from "tableQrsAndAns" table
     *
     * @param idEntry
     * @return
     * @throws SQLException
     */
    public List<String> getEntryAnswers(int idEntry) throws SQLException {

        List<String> entryInformation = new LinkedList<>();

        rs = st.executeQuery("SELECT * FROM feta.tableQrsAndAns" + nameDB + " WHERE ID = '" + idEntry + "'");

        while (rs.next()) {

            //String id = rs.getString("idEntry");
            String ip = rs.getString("ClientIPAddress");
            String tcp = rs.getString("ClientTCPport");
            String ep = rs.getString("SPARQLEndpointPort");
            String ans = rs.getString("Answer");
            String rt = rs.getString("ReceptionTime");
            String rq = rs.getString("RequestQuery");

            entryInformation.add(ans);
            entryInformation.add(ep);
            entryInformation.add(ip);
            entryInformation.add(rt);
            entryInformation.add(rq);
            entryInformation.add(tcp);
        }

        return entryInformation;
    }

        /**
     * Parse every answer string, and match all answer entities (IRIs/Literals)
     * to the corresponding hashMaps
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     * @throws java.sql.SQLException
     */
    public void setAnswerStringToMaps() throws IOException, URISyntaxException, SQLException {
        
       
        List<String> entryInformation = null;
        String Answer = "", ClientIpAddress = "", requestQuery = "", receptTime = "", endpoint = "";      
        int monetSIze = getTableSize("tableQrsAndAns" + nameDB);

        for (int i = 1; i < monetSIze; i++) {
       
                entryInformation  = getEntryAnswers(i);         
                mapAnsIDtoEntry.put(i, entryInformation);

                if (!entryInformation.isEmpty()) {

                    entryInformation = mapAnsIDtoEntry.get(i);
                    if (!entryInformation.isEmpty()) {

                        Answer = entryInformation.get(0);
                        endpoint = entryInformation.get(1);
                        ClientIpAddress = entryInformation.get(2);
                        receptTime = entryInformation.get(3);
                        requestQuery = entryInformation.get(4);
                        myBasUtils.setVarsToAnswEntities(i, requestQuery, Answer);
                    }

                }

        }
    }

}
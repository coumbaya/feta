package myfeta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jdom2.JDOMException;

/**
 * Class Main for launchng program with algo options
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class Main {

    private static CouchDBManag myDB;
    private static MonetDBManag myMDB;
    private static Deduction deduction;
    private static LoadFiles load;

    //** ****FETA loading traces in DB (monetDB or couchDB)********
    //load  queries and answers in DB (for "monetDB" or "couchDB")
    private static boolean loadDB;
    //setting the DB name (for "monetDB" or "couchDB")
    private static boolean setCouchDB;
    //enabling "monetDB" storage for FETA, by default its "couchDB"
    public static boolean setMonetDB;
    //reset DB (for "monetDB" or "couchDB")
    private static boolean resetDB;
    //database name string, formed in combination with "collectionName" and "engineName"
    public static String nameDB;
    //seting  "capture.log" path
    private static String logPath;
    //count the number of different SELECTs, identified during loading
    public static int cntSELECTS;
    //count the number of different queries (ASKs and SELECTs), identified during loading
    public static int cntall;
    //** ****FETA loading traces in DB (monetDB or couchDB)********

    //** ****FETA deduction basic features and utils********
    //setEngine name string, as it is passed as argument by user, when "setEngine" is enabled
    public static String engineName;
    //collection string name (e.g. CD for Cross Domain), used to name the database
    public static String collectionName;
    //integer window value, defining the DB slice used as FETA input, 
    //from the first to last captured subquery
    public static int windowSlice;
    //integer window value, defining the maximum time interval to consider 
    //that two captured subqueries are joinable
    public static int windowJoin;
    //enabling simple execution of FETA approach, stopping just after "Common join Condition"
    public static boolean simpleExecution;
    //enabling inverse mapping from IRIs/Literals to variables,
    //necessairy for "FedX" query engine traces
    public static boolean inverseMapping;
    //** ****FETA deduction basic features and utils********

    //**FETA deduction extra features and utils********
    //enabling usage option menu
    private static boolean help;
    //verbose option for more detailed output
    public static boolean verbose;
    public static boolean verbose2;
    //disabling concurrent execution traces' statistics for precision/Recall
    public static boolean single;
    //
    public static double inverseThresh;
    //
    public static boolean traceGen;

    /**
     * This method inits all program's options, objects and paths to be used
     */
    public static void initVariables() {

        verbose = true;
        verbose2 = false;
        loadDB = false;
        resetDB = false;
        setCouchDB = false;
        setMonetDB = true;
        cntSELECTS = 0;
        cntall = 0;
        single = true;

        logPath = "/home/nassopoulos-g/capture.log";

        nameDB = "mydatabasefedxcd1";
        engineName = "ANAPSID";
        collectionName = "CD";
        collectionName = collectionName + engineName;

        inverseMapping = false;
        simpleExecution = false;
        help = false;
        windowSlice = 1000000000;
        windowJoin = 1000000;
        inverseThresh = 0.01;
        traceGen=true;
    }

    /**
     * This method prints a usage menu, when it is passed as argument or when an
     * arguments are not syntaxically correct when they are passed
     */
    public static void usage() {

        System.out.println(" Usage : --load or -l <path_to_capture>: for loading a capture into the DB");
        System.out.println("         --resetDB or -r: for reseting an existing DB");
        System.out.println("         --nameDB or -n: for setting DB name");
        System.out.println("         --systemDB or -s <systemDB_to_use>: for setting \"couchDB\" or \"monetDB\" system (by default \"couchDB\" )");
        System.out.println("         --inverseMap or -i <inverse_mapping_threshold>: for enabling inverse mapping in \"NestedLoopDetection\" heuristic, necessary for FedX, "
                + "and setting the threshold to validate a matching");
        System.out.println("         --sameConcept or -c <path_to_endpoints_addresses>: enabling \"SameConcept/SameAs\" and passing Endpoints IP Addresses as argument");
        System.out.println("         --setWinSlice or -ws <window_in_seconds>: for setting the maximum temporal distance between first and last subquery, "
                + "defining DB slice (by default 1000000 seconds)");
        System.out.println("         --setWinJoin or -wj <window_in_seconds>: for setting the maximum joinable window interval gap between two subqueries or triple patterns"
                + " (by default 1000000 seconds)");
        System.out.println("         --onlyGraphCon or -og: to stop FETA deduction just after \"Graph Construction\" heuristic");
        System.out.println("         --help or -h: for showing help");
    }

    /**
     * This method gets all the input arguments and treats every case
     *
     * @param args
     */
    public static void getParameteres(final String[] args) {

        if (args.length > 0) {

            System.out.println("User defined parameters of FETA: ");
        }
        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {
                case "--verbose":
                case "-v":
                    switch (args[i + 1]) {
                        case "1":
                            verbose = true;
                            break;
                        case "2":
                            verbose2 = true;
                            break;
                        default:
                            break;
                    }
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give the verbose mode, 1 or 2: ");
                        System.exit(-1);
                    }

                    break;
                case "--load":
                case "-l":
                    loadDB = true;
                    logPath = args[i + 1];
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give the capture to be loaded: ");
                        System.exit(-1);
                    }
                    i++;
                    break;
                case "--resetDB":
                case "-r":
                    resetDB = true;
                    break;
                case "--nameDB":
                case "-n":
                    nameDB = args[i + 1];
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give the DB's name: ");
                        System.exit(-1);
                    }
                    i++;
                    break;
                case "--systemDB":
                case "-s":
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give the storage system used as DB(\"couchDB\" or \"monetDB\"): ");
                        System.exit(-1);
                    } else if (args[i + 1].equalsIgnoreCase("couchDB")) {
                        setCouchDB = true;
                    } else if (args[i + 1].equalsIgnoreCase("monetDB")) {
                        setMonetDB = true;
                    }
                    i++;
                    break;
                case "--setWinJoin":
                case "-wj":
                    windowJoin = Integer.parseInt(args[i + 1]);
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give a valid window join (gap) value: ");
                        System.exit(-1);
                    }
                    i++;
                    break;
                case "--setWinSlice":
                case "-ws":
                    windowSlice = Integer.parseInt(args[i + 1]);
                    if (args[i + 1].startsWith("-")) {

                        System.out.println("Please give a valid window slice value: ");
                        System.exit(-1);
                    }
                    i++;
                    break;
                case "--inverseMap":
                case "-i":
                    inverseMapping = true;
                    break;
                case "--onlyGraphCon":
                case "-og":
                    simpleExecution = true;
                    break;
                case "--help":
                case "-h":
                    help = true;
                    break;
                default:
                    System.out.println("Unknow argument");
            }

        }
    }

    public static void showGeneralInfo() {

        System.out.println("************************General information********************");

        if (setCouchDB) {
            System.out.println("\t Storage DB system: \"CouchDB\"");

        } else {
            System.out.println("\t Storage DB system: \"MonetDB\"");

        }

        System.out.println("\t Database name: \"" + nameDB + "\"");

        if (loadDB) {

            System.out.println("\t Number of queries (SELECTs and ASKs): " + cntall);
            System.out.println("\t Number of SELECTs: " + cntSELECTS);
        } else {

            System.out.println("\t Window deduction, defining FETA's input DB slice: " + windowSlice + " seconds");
            System.out.println("\t Window Tjoin (gap), defining maximum joinable "
                    + "temporal distance (for subqueries and triple patterns): " + windowJoin + " seconds");
        }

        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    /**
     * This is the main method for launching the programm.
     *
     * @param args parameters passed from user to choose the code comportement
     * @exception IOException on input error.
     * @throws java.net.URISyntaxException
     *
     * @throws java.lang.InterruptedException @see IOException
     * @throws ParserConfigurationException
     * @throws FileNotFoundException
     * @throws TransformerException
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws org.jdom2.JDOMException
     */
    public static void main(String[] args) throws IOException,
            ParserConfigurationException, FileNotFoundException, TransformerException, URISyntaxException, InterruptedException, SQLException, InstantiationException, IllegalAccessException, JDOMException {

        initVariables();
        getParameteres(args);
        
        if(setMonetDB){
            try {
                 
                  String command = "monetdbd start myMONETDB/";
                  Process perkele = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                
            }
        }

        if (help) {

            usage();

        } else {

            if (setMonetDB) {

                myMDB = new MonetDBManag();
                myMDB.createDB(nameDB);
                load = new LoadFiles(myMDB);
                myMDB.openSession("jdbc:monetdb://localhost/demo", "feta", "feta");
            } else {

                myDB = new CouchDBManag();
                load = new LoadFiles(myDB.getDocList(), myDB);
                myDB.openSession("localhost", 5984);
                //myDB.openSession2("127.0.0.1", 5984, "nassopoulos", "20CouchdbLina14");
            }

            if (loadDB) {

                if (resetDB) {

                    if (setMonetDB) {

                        myMDB.resetMDB();
                    } else {

                        myDB.resetDB(nameDB);
                    }
                } else {
                    if (setMonetDB) {

                        myMDB.createMDB();
                    } else {

                        myDB.createDatabase(nameDB);
                        myDB.getDatabase(nameDB);
                    }

                }

                load.loadQuersAndAns(logPath);
                showGeneralInfo();
            } else if (!loadDB) {

                showGeneralInfo();

                if (setMonetDB) {

                    deduction = new Deduction(null, myMDB);
                } else {

                    deduction = new Deduction(myDB.getDocList(), myDB);
                }

                deduction.initDeduction(nameDB);
                deduction.deductionAlgo(windowSlice);
            }

        }

    }
}

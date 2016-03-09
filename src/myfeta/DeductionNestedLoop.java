package myfeta;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static myfeta.Deduction.cntEGTotal;
import static myfeta.Deduction.cntEGTrPo;
import static myfeta.Deduction.dedGraphSelect;
import static myfeta.Deduction.mapAnsEntryToAllSignatures;
import static myfeta.Deduction.mapAnsEntryToListValues;
import static myfeta.Deduction.mapAnsIDToLogClQuery;
import static myfeta.Deduction.mapAnsIDToTimeSecs;
import static myfeta.Deduction.mapAnsIDtoEntry;
import static myfeta.Deduction.mapAnsSingatureToAllValues;
import static myfeta.Deduction.mapDTPtoCANCELofEG;
import static myfeta.Deduction.mapGroundTruthPairs;
import static myfeta.Deduction.mapLogClQueryToAllTPEnts;
import static myfeta.Deduction.mapLogClQueryToAnsEntry;
import static myfeta.Deduction.mapLogClQueryToDedGraph;
import static myfeta.Deduction.mapLogClQueryToTimeSecs;
import static myfeta.Deduction.mapTPtoAnswersSourcesInverse;
import static myfeta.Deduction.mapTruePositivePairs;
import static myfeta.Deduction.totalPairs;
import static myfeta.Deduction.truePositivesPairs;
import static myfeta.DeductionLogClean.mapCTPtoFILTERUNION;
import static myfeta.DeductionLogClean.queries;
import static myfeta.DeductionUtils.DTPCandidates;
import static myfeta.Main.inverseMapping;
import static myfeta.Main.inverseThresh;
import static myfeta.Main.setMonetDB;
import static myfeta.Main.windowJoin;

/**
 * Class for "NestedLoopDetection" heuristic, first by extracting all Candidate
 * Triple Patterns (CTPs) and either inverse map all CTPs constant values as the
 * inner part of a nested loop implemention with a previously Deduced Triple
 * Pattern (DTP), or identify the CTP directly as a DTP.
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class DeductionNestedLoop {

    BasicUtilis myBasUtils;
    Deduction myDeduction;
    DeductionUtils myDedUtils;
    DeductionLogClean myLogClean;
    DeductionGraphConstruction myGraph;
    MonetDBManag myMonet;

    int cntEGpairs;
    private boolean flagSTOPNONPROJECTEDvars = false;
    public static List<List<Integer>> dedGraphBeforeNesLoop;
    public static List<List<List<String>>> setDTPbasedConcepts;
    public static List<List<String>> allCTPs;
    public static List<List<List<String>>> notnullJoinBGPs;
    public static List<List<List<String>>> sameConceptBGPs;
    public static HashMap<Integer, List<String>> mapEGtoCTPTmp;
    public static HashMap<Integer, Integer> mapEGtoCancel;
    public static HashMap<List<String>, Integer> mapEGtoOccurs;
    public static HashMap<Integer, Integer> mapCTPtoDedGraph;

    //******************************Hash Maps concerning Candidate Triple Patterns (CTPs)******************************//
    // concerning source queries containing it
    public static HashMap<List<String>, List<Integer>> mapCTPtoQuerySrc;
    public static HashMap<Integer, List<Integer>> mapCTPIDtoQuerySrc;
    // concerning constant values (IRIs or Litarals) to be searched in inverse mapping
    public static HashMap<List<String>, List<String>> mapCTPtoConstants;
    // concerning first query's timestamp
    public static HashMap<Integer, String> mapCTPToStartTime;
    // concerning latest query's timestamp
    public static HashMap<Integer, String> mapCTPToFinishTime;
    // concerning source queries' endpoints
    public static HashMap<List<String>, List<String>> mapCTPToEndpsSrc;
    // concerning source queries' answers, for CTP variables
    public static HashMap<List<String>, List<String>> mapCTPtoAnswTotal;
    // concerning original or inverse variables, used for Sequential Pattern Mining
    public static HashMap<List<String>, List<String>> mapCTPtoAllVars;
    // 
    public static HashMap<List<String>, List<List<String>>> mapCTPtoAllDTPs;

    //******************************Hash Maps concerning Deduced Triple Patterns (DTP)******************************//   
    // concerning to its serial deduction id 
    public static HashMap<List<String>, Integer> mapDTPToDeducedID;
    public static HashMap<List<String>, Integer> mapDTPToTimeDeduction;
    // concerning source queries containing it, possible inner nested loop part
    public static HashMap<Integer, List<Integer>> mapDTPtoInnerQuery;
    // concerning queries injecting values as constants, certified as an outer nested loop part
    public static HashMap<Integer, List<Integer>> mapDTPtoOuterQuery;
    // concerning source queries' endpoints
    public static HashMap<List<String>, List<String>> mapDTPToEndpsSrc;
    // concerning first query's timestamp
    public static HashMap<List<String>, Integer> mapDTPToStartTime;
    // concerning latest query's timestamp
    public static HashMap<List<String>, Integer> mapDTPToFinishTime;
    // concerning  answers, for both CTP and deduced variables
    public static HashMap<List<String>, List<String>> mapDTPtoAnswTotal;
    // concerning SameConcept/Same As BGP containing it 
    public static HashMap<List<String>, Integer> mapDTPtoConceptBGP;
    // concerning NotNullJoin BGP containing it 
    public static HashMap<List<String>, Integer> mapDTPtoJoinBGP;
    // concerning Exclusive Group containing it 
    public static HashMap<List<String>, Integer> mapDTPtoExclGroup;
    // concerning Exclusive Group containing it 
    public static HashMap<List<List<String>>, Integer> mapPairExclGroup;
    public static HashMap<Integer, Integer> mapDTPToAnyJoin;
    // concerning alternative DTPs, with differents vars for common values
    public static HashMap<List<String>, Integer> mapDTPtoAlternatives;
    // 
    public static HashMap<List<List<String>>, Integer> mapDTPpair;
    //
    public static HashMap<List<String>, Integer> mapDTPofEGNested;
        //
    public static HashMap<List<String>, Integer> mapSrcTPtoBoundJoin;
              //
    public static HashMap<List<String>, Integer> mapSrcTPtoSingleTPQuery;

    //******************************Hash Maps concerning relation between Deduced Triple Patterns******************************//   
    // concerning type of join: "Alternative inverese mapping vars", "exclusive groups", "nested loop" or "symmetric hash"
    public static HashMap<List<List<String>>, String> mapPairTPsToTypeJoin;
    // affecting a JOIN BGP graph to a level of confidence
    public static HashMap<Integer, Float> mapJOINBGPtoConfidence;

    public DeductionNestedLoop() throws ParserConfigurationException {

        myDedUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
        myLogClean = new DeductionLogClean();
        myGraph = new DeductionGraphConstruction();
        myMonet = new MonetDBManag();

        notnullJoinBGPs = new LinkedList<>();
        sameConceptBGPs = new LinkedList<>();
        mapPairTPsToTypeJoin = new HashMap<>();
        mapJOINBGPtoConfidence = new HashMap<>();

        mapCTPtoQuerySrc = new HashMap<>();
        mapCTPtoConstants = new HashMap<>();
        mapCTPtoAllVars = new HashMap<>();
        mapCTPtoAnswTotal = new HashMap<>();
        mapCTPIDtoQuerySrc = new HashMap();
        mapCTPToFinishTime = new HashMap<>();
        mapCTPToStartTime = new HashMap<>();
        mapCTPToEndpsSrc = new HashMap<>();
        mapCTPtoDedGraph = new HashMap<>();

        setDTPbasedConcepts = new LinkedList<>();
        mapDTPToDeducedID = new HashMap<>();
        mapDTPToFinishTime = new HashMap<>();
        mapDTPToStartTime = new HashMap<>();
        mapDTPtoJoinBGP = new HashMap<>();
        mapDTPtoConceptBGP = new HashMap<>();
        mapDTPtoInnerQuery = new HashMap<>();
        mapDTPtoOuterQuery = new HashMap<>();
        mapDTPtoExclGroup = new HashMap<>();
        mapDTPtoAlternatives = new HashMap<>();
        mapDTPToAnyJoin = new HashMap();
        mapDTPToEndpsSrc = new HashMap<>();
        mapDTPtoAnswTotal = new HashMap<>();
        mapDTPToTimeDeduction = new HashMap<>();

        mapPairExclGroup = new HashMap<>();

        mapEGtoCTPTmp = new HashMap<>();
        mapEGtoOccurs = new HashMap<>();

        mapCTPtoAllDTPs = new HashMap<>();
        mapDTPpair = new HashMap<>();
        allCTPs = null;
        cntEGpairs = 0;
        mapEGtoCancel = new HashMap<>();
        mapDTPofEGNested = new HashMap<>();
        mapSrcTPtoBoundJoin= new HashMap<>();
        mapSrcTPtoSingleTPQuery= new HashMap<>();
    }

    /**
     * After a preprocessing phase of extracting all Candidate Triple Patterns
     * (CTP) from all queries, we infer nested loops for two types of
     * implementation:
     *
     * (i) "bound Join": we match CTP constant values (IRIs/Literals) to mapping
     * variables of previously deduced TPs and identify hidden JOIN variables
     *
     * (ii) "filter options". we identify subquerys' filter options as answers
     * of a previously evaluated subquery, and their respective CTPs. The
     * difference with "bound join" is that join variables are not hidden, but
     * confirmed
     *
     * In other case, we identify directly the CTP as a DTP
     *
     * @param windowDed deduction window, defining DB slice
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void NestedLoopDetection(int windowDed) throws TransformerException, ParserConfigurationException, SQLException, InstantiationException, IllegalAccessException {

        long startTime = 0;
        long finishTime = 0;
        long elapsedTime = 0;
        List<String> allCTPconsts = null;
        List<String> tmpCTP = null;

        if (setMonetDB) {

            myMonet.openSession("jdbc:monetdb://localhost/demo", "feta", "feta");
        }

        long tmpTime = System.nanoTime();
        System.out.println("[START] ---> Triple Pattern's extraction");
        System.out.println();
        allCTPs = getPatternValues(dedGraphSelect);
        System.out.println("[FINISH] ---> Triple Pattern's extraction (Elapsed time: " + (System.nanoTime() - tmpTime) / 1000000000 + " seconds)");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();
        startTime = System.nanoTime();
        System.out.println("[START] ---> NestedLoopDetection heuristic");
        System.out.println();

        dedGraphBeforeNesLoop = myBasUtils.cloneListOfList2(dedGraphSelect);

        for (int i = 0; i < allCTPs.size(); i++) {

            allCTPconsts = myDedUtils.getValuesFromCTP(i);
            HashSet hs = new HashSet();
            hs.addAll(allCTPconsts);
            allCTPconsts.clear();
            allCTPconsts.addAll(hs);
            Collections.sort(allCTPconsts);
        }

        //search every CTP's constant value, (IRI/Literal) with inverse mapping 
        //to reveal hidden variables or directly corresponding values to hash map structures 
        //(e.g., endpoints, answers)
        for (int i = 0; i < allCTPs.size(); i++) {

            tmpCTP = myDedUtils.getCleanTP(allCTPs.get(i));

            if (!inverseMapping) {

               // System.out.println(tmpCTP);
                 //System.out.println(i);
                myDedUtils.setDTPHashInfo(tmpCTP, i);
                allCTPconsts = myDedUtils.getValuesFromCTP(i);
                myBasUtils.insertToMap1(mapCTPtoAllDTPs, tmpCTP, tmpCTP);
                directMatchingTP(i, allCTPconsts, allCTPs.get(i));
            } else {

                SearchValueInverseMap(i);
            }

        }

        //Get FILTER values, for ANAPSID trace's inner subqueres of NLFO
        checkNestedLoopFilter();
        //Get FILTER values, for FedX trace's inner subqueres of NLBJ
        cancelJoinsNLEG();
        //Search for possible double NLBJ implementation in subject and object 
        //position of each triple pattern, where the second NLBJ is implemented
        //into a FILTER option
        matchFedXFilterValsToVariable();

        finishTime = System.nanoTime();
        elapsedTime = finishTime - startTime;
        System.out.println();
        System.out.println("[FINISH] ---> NestedLoopDetection heuristic (Elapsed time: " + elapsedTime / 1000000000 + " seconds)");
    }

    /**
     * Extract all Candidate Triple Patterns (CTPs) from all deduced graphs
     *
     * @param listOfDedGraphs
     * @return
     */
    public List<List<String>> getPatternValues(List<List<Integer>> listOfDedGraphs) {

        List<String> queryTriplets = null;

        for (int i = 0; i < listOfDedGraphs.size(); i++) {

            Collections.sort(listOfDedGraphs.get(i));
            for (int j = 0; j < listOfDedGraphs.get(i).size(); j++) {

                queryTriplets = mapLogClQueryToAllTPEnts.get(listOfDedGraphs.get(i).get(j) - 1);
                if (queryTriplets.size() > 0) {

                    parseNgetCTP(queryTriplets, i, listOfDedGraphs.get(i).get(j) - 1);
                }

            }
        }

        int count = 0;
        System.out.println();
        System.out.println("\t================ Candidate Triple Patterns (CTPs)================");
        System.out.println();

        for (int i = 0; i < DTPCandidates.size(); i++) {

            count++;
            System.out.println("\t\t\tCTP no [" + count + "] " + DTPCandidates.get(i).get(0) + " "
                    + DTPCandidates.get(i).get(1) + " " + DTPCandidates.get(i).get(2) + " ");
        }

        System.out.println();
        System.out.println("\t================ Candidate Triple Patterns (CTPs)================");
        System.out.println();

        return DTPCandidates;
    }

    /**
     * Parse query triplets and identify new CTP or update exisitng one
     *
     * @param queryTriplets
     * @param dedGraphId
     * @param indxLogCleanQuery
     */
    public void parseNgetCTP(List<String> queryTriplets, int dedGraphId, int indxLogCleanQuery) {

        int indxValue = -1;
        int indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
        int indxNewTPDedGraph = -1;
        List<String> tmpTriplet = null;
        List<String> tmpTripletClean = null;
        List<Integer> allIdPats = null;
        List<Integer> deducedTPnotCoveredTimestamp = new LinkedList<>();
        String strDedQueryId = Integer.toString(dedGraphId);
        String constanValue = "";
        boolean flagTriplePatternOutOfTimeRange = false;

        for (int f = 0; f < queryTriplets.size(); f += 3) {
            tmpTriplet = new LinkedList<>();
            tmpTriplet.add(queryTriplets.get(f + 0));
            tmpTriplet.add(queryTriplets.get(f + 1));
            tmpTriplet.add(queryTriplets.get(f + 2));
            tmpTripletClean = myDedUtils.getCleanTP(tmpTriplet);

            if (tmpTriplet.get(0).contains("?Int")
                    && tmpTriplet.get(1).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1>")
                    && tmpTriplet.get(2).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00863>")) {

                int zeze = 0;
            }

            if (queryTriplets.size() > 6 && !queries.get(indxLogCleanQuery).contains("UNION") && !queries.get(indxLogCleanQuery).contains("_0")) {

                if (!tmpTriplet.get(0).contains("?") || !tmpTriplet.get(2).contains("?")) {

                    mapDTPofEGNested.put(tmpTripletClean, 1);
                }

            }
            
            //save all RAW triple patterns participating into a bound JOIN
            if(queries.get(indxLogCleanQuery).contains("UNION") && queries.get(indxLogCleanQuery).contains("_0")){
                mapSrcTPtoBoundJoin.put(tmpTripletClean, 1);
            }

            
                    if (queryTriplets.size() ==3 && !queries.get(indxLogCleanQuery).contains("UNION") && !queries.get(indxLogCleanQuery).contains("_0")) {

                if (!tmpTriplet.get(0).contains("?") || !tmpTriplet.get(2).contains("?")) {

                    mapSrcTPtoSingleTPQuery.put(tmpTripletClean, 1);
                }

            }
                    
            //If both subject and object are variables, identify directly the CTP as a whole
            if (queryTriplets.get(f).contains("?") && queryTriplets.get(f + 2).contains("?") || !inverseMapping) {

                //case (a), for its first apparition
                allIdPats = myDedUtils.getIdemCTPs(DTPCandidates, queryTriplets.get(f), queryTriplets.get(f + 1), queryTriplets.get(f + 2));
                if (allIdPats.isEmpty()) {

                    myDedUtils.setNewCTPInfo(tmpTriplet, "", indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "");
                    matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", DTPCandidates.size() - 1, queryTriplets.get(f + 0));
                } // case (b), for its second apparition then check if is covered by the Tjoin (Gap)
                // if not, concidered it as a new CTP
                else {

                    for (int l = allIdPats.size() - 1; l >= 0; l--) {

                        indxNewTPDedGraph = mapCTPtoDedGraph.get(allIdPats.get(l));
                        if (indxNewTPDedGraph != indxLogQueryDedGraph) {

                            flagTriplePatternOutOfTimeRange = true;
                            myDedUtils.setNewCTPInfo(tmpTriplet, queryTriplets.get(f + 0), indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "_" + allIdPats.size());
                            matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", DTPCandidates.size() - 1, queryTriplets.get(f + 0));
                            deducedTPnotCoveredTimestamp.add(DTPCandidates.size() - 1);
                            break;
                        } else {

                            myDedUtils.updateCTPInfo(tmpTriplet, constanValue, indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                            matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", allIdPats.get(l), constanValue);
                            if ((DTPCandidates.get(allIdPats.get(l)).get(0).contains("?") && DTPCandidates.get(allIdPats.get(l)).get(0).contains("_"))
                                    || (DTPCandidates.get(allIdPats.get(l)).get(2).contains("?") && DTPCandidates.get(allIdPats.get(l)).get(2).contains("_"))) {

                                deducedTPnotCoveredTimestamp.add(allIdPats.get(l));
                            }

                            break;
                        }

                    }

                }

                //ELse if subject or object is Literal/IRI
            } else {

                if (queryTriplets.get(f).contains("?") && (queryTriplets.get(f + 2).contains("<")
                        || queryTriplets.get(f + 2).contains("\"") || queryTriplets.get(f + 2).contains("'")) && inverseMapping) {

                    indxValue = 2;
                }
                if (queryTriplets.get(f + 2).contains("?") && (queryTriplets.get(f).contains("<")
                        || queryTriplets.get(f).contains("\"") || queryTriplets.get(f).contains("'")) && inverseMapping) {

                    indxValue = 0;
                }

                setOrUpdateCTPInfo(tmpTripletClean, f, indxValue, strDedQueryId, indxLogCleanQuery);
            }

        }

        //check for  an exclusive group relation between CTP            
        if (queryTriplets.size() >= 6 && !flagTriplePatternOutOfTimeRange) {
            if (!queries.get(indxLogCleanQuery).contains("UNION")) {

                if (checkExclusiveGroupJoin(queryTriplets)) {
                    for (int i = 0; i < deducedTPnotCoveredTimestamp.size(); i++) {
                        if (mapDTPToAnyJoin.get(deducedTPnotCoveredTimestamp.get(i)) == null) {

                            mapDTPToDeducedID.put(DTPCandidates.get(deducedTPnotCoveredTimestamp.get(i)), deducedTPnotCoveredTimestamp.get(i));
                            mapDTPToAnyJoin.put(deducedTPnotCoveredTimestamp.get(i), -1);
                        }
                    }
                }
            }
        }

    }

    /**
     * Identify new CTP or update existing info of corresponding hash maps
     *
     * @param tmpTriplet
     * @param indexTPinQuery
     * @param indxValue
     * @param strDedQueryId
     * @param indxLogCleanQuery
     */
    public void setOrUpdateCTPInfo(List<String> tmpTriplet, int indexTPinQuery, int indxValue, String strDedQueryId, int indxLogCleanQuery) {

        int indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
        int indxNewTPDedGraph = -1;
        List<Integer> allIdPats = new LinkedList<>();
        String constantVal = "";

        //CHANGE get constants
        if (!tmpTriplet.get(0).contains("?")) {

            constantVal = tmpTriplet.get(0);
        } else if (!tmpTriplet.get(2).contains("?")) {

            constantVal = tmpTriplet.get(2);
        }

        //case inverseMapping is enabled, we are looking how many CTPs with same
        //elements on two out of three entities (i.e., variable and predicate) actually match
        if (inverseMapping) {
            switch (indxValue) {

                case 0:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, tmpTriplet.get(1), 1, tmpTriplet.get(2), 2);
                    break;
                case 1:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, tmpTriplet.get(0), 0, tmpTriplet.get(2), 2);
                    break;
                case 2:

                    allIdPats = myDedUtils.getDerivedCTPs(DTPCandidates, tmpTriplet.get(0), 0, tmpTriplet.get(1), 1);
                    break;
            }
        } else {

            allIdPats = myDedUtils.getIdemCTPs(DTPCandidates, tmpTriplet.get(0), tmpTriplet.get(1), tmpTriplet.get(2));
        }

        //case (a), for CTP first apparition
        if (allIdPats.isEmpty()) {

            myDedUtils.setNewCTPInfo(tmpTriplet, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "");
            matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", DTPCandidates.size() - 1, constantVal);
        } // case (b), for already treated CTP  check if is covered by the Tjoin (Gap)
        // if not, concidered it as a new CTP
        else if (allIdPats.size() > 0) {

            for (int l = allIdPats.size() - 1; l >= 0; l--) {
                //  for (int l = 0; l < allIdPats.size(); l++) {

                indxLogQueryDedGraph = mapLogClQueryToDedGraph.get(indxLogCleanQuery);
                indxNewTPDedGraph = mapCTPtoDedGraph.get(allIdPats.get(l));
                int startTime = myBasUtils.getTimeInSec(mapCTPToStartTime.get(allIdPats.get(l)));
                int stopTime = myBasUtils.getTimeInSec(mapCTPToFinishTime.get(allIdPats.get(l)));

                if (indxNewTPDedGraph != indxLogQueryDedGraph && (!(mapLogClQueryToTimeSecs.get(indxLogCleanQuery) - startTime <= windowJoin)
                        || !(mapLogClQueryToTimeSecs.get(indxLogCleanQuery) - stopTime <= windowJoin))) {

                    myDedUtils.setNewCTPInfo(tmpTriplet, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, strDedQueryId, "_" + Integer.toString(allIdPats.size() + 1));
                    matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", DTPCandidates.size() - 1, constantVal);
                    break;
                } else {

                    //CHANGE must simplify
                    if (DTPCandidates.get(allIdPats.get(l)).get(0).contains("_")) {

                        myDedUtils.updateCTPInfo(DTPCandidates.get(allIdPats.get(l)), constantVal, indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                        matchAnswOrigVarsToCTP(DTPCandidates.get(allIdPats.get(l)), indxLogCleanQuery, "", allIdPats.get(l), constantVal);
                    } else {

                        myDedUtils.updateCTPInfo(tmpTriplet, constantVal, indxLogCleanQuery, indxLogQueryDedGraph, allIdPats.get(l));
                        matchAnswOrigVarsToCTP(tmpTriplet, indxLogCleanQuery, "", allIdPats.get(l), constantVal);
                    }

                    break;
                }

            }

        }
    }

    /**
     * Identify exclusive groups and combine respective DTPs and at the same time
     * check for possible Nested Loop with Exclusive Groups implementation, made
     * by FedX
     *
     * @param queryTriplets
     * @return
     */
    public boolean checkExclusiveGroupJoin(List<String> queryTriplets) {

        boolean flagEG = false;
        boolean foundEG = false;
        List<List<String>> newEGpair = null;
        List<List<String>> newEGpair2 = null;
        List<String> innerDTP = null;
        List<String> outerDTP = null;
        List<String> currEG = null;

        boolean flagSkip = false;

        if (queryTriplets.size() >= 6) {

            for (int key : mapEGtoCTPTmp.keySet()) {

                currEG = mapEGtoCTPTmp.get(key);

                if (currEG.size() == queryTriplets.size()) {

                    int commElems = myBasUtils.candidateTPcomElems(currEG, queryTriplets);

                    // the second condition, is used to capture Nested Loop with EG operator, made by FedX
                    if (commElems == currEG.size() || commElems == currEG.size() - 1) {

                        if (commElems == currEG.size() - 1) {
                            mapEGtoCancel.put(key, 1);
                            mapEGtoOccurs.put(currEG, 2);
                        }

                        foundEG = true;
                        break;
                    }

                }

            }

            if (!foundEG) {

                int indEG = mapEGtoCTPTmp.size();
                mapEGtoCTPTmp.put(indEG, queryTriplets);
                mapEGtoOccurs.put(currEG, 1);

                for (int i = 0; i < queryTriplets.size(); i += 3) {

                    outerDTP = new LinkedList<>();
                    outerDTP.add(queryTriplets.get(i));
                    outerDTP.add(queryTriplets.get(i + 1));
                    outerDTP.add(queryTriplets.get(i + 2));

                    for (int f = i + 3; f < queryTriplets.size(); f += 3) {

                        //CHANGE: unnessacary condition
                        if (queryTriplets.get(i).equalsIgnoreCase(queryTriplets.get(f)) || queryTriplets.get(i + 2).equalsIgnoreCase(queryTriplets.get(f + 2))
                                || queryTriplets.get(i + 2).equalsIgnoreCase(queryTriplets.get(f)) || queryTriplets.get(i).equalsIgnoreCase(queryTriplets.get(f + 2))) {

                            innerDTP = new LinkedList<>();
                            innerDTP.add(queryTriplets.get(f));
                            innerDTP.add(queryTriplets.get(f + 1));
                            innerDTP.add(queryTriplets.get(f + 2));
                            newEGpair = new LinkedList<>();
                            newEGpair2 = new LinkedList<>();
                            innerDTP = myDedUtils.getCleanTP(innerDTP);
                            outerDTP = myDedUtils.getCleanTP(outerDTP);
                            newEGpair.add(innerDTP);
                            newEGpair.add(outerDTP);
                            newEGpair2.add(outerDTP);
                            newEGpair2.add(innerDTP);

                            int currDTPsize = mapDTPToDeducedID.size();

                            if (mapDTPToDeducedID.get(outerDTP) == null) {

                                mapDTPToDeducedID.put(outerDTP, currDTPsize);
                                mapDTPToAnyJoin.put(currDTPsize, 1);
                            }

                            currDTPsize = mapDTPToDeducedID.size();
                            if (mapDTPToDeducedID.get(innerDTP) == null) {

                                mapDTPToDeducedID.put(innerDTP, currDTPsize);
                                mapDTPToAnyJoin.put(currDTPsize, 1);
                            }

                            mapDTPtoExclGroup.put(innerDTP, indEG + 1);
                            mapDTPtoExclGroup.put(outerDTP, indEG + 1);

                            int EGsize = mapPairExclGroup.size();

                            if (mapPairExclGroup.get(newEGpair) == null && mapPairExclGroup.get(newEGpair2) == null) {

                                mapPairExclGroup.put(newEGpair, EGsize);
                                mapPairExclGroup.put(newEGpair2, EGsize);
                                totalPairs++;
                                cntEGTotal++;
                            }

                            if (mapGroundTruthPairs.get(newEGpair) != null && mapTruePositivePairs.get(newEGpair) == null) {

                                mapTruePositivePairs.put(newEGpair, 1);
                                mapTruePositivePairs.put(newEGpair2, 1);
                                cntEGTrPo++;
                                truePositivesPairs++;
                            }

                            mapDTPpair.put(newEGpair, 1);
                            mapDTPpair.put(newEGpair2, 1);

                            flagEG = true;
                            myDedUtils.setNewEGInfo(outerDTP, innerDTP, newEGpair, indEG);
                        }

                    }

                }
            }

        }

        return flagEG;
    }

    /**
     * Implement inverse mapping, by matching a CTP's constant values with
     * answers of previously evaluated mapping variables of DTPs
     *
     * @param indxCTP index of candidate triple pattern
     * @throws java.sql.SQLException
     */
    public void SearchValueInverseMap(int indxCTP) throws SQLException {

        String Answer = "", SPARQLEndpointPort = "", OriginalQuery = "";
        String candVar = "", mapVar = "";
        String TimeCandTPFinsh = mapCTPToFinishTime.get(indxCTP);
        String TimeCandTPStart = mapCTPToStartTime.get(indxCTP);
        int sourceGraph = 0, logCLeanIDquery = -1;
        int TimeCandTPSecFinish = myBasUtils.getTimeInSec(TimeCandTPFinsh);
        int TimeCandTPSecStart = myBasUtils.getTimeInSec(TimeCandTPStart);
        boolean flagSkipPattern = false, flagAnswerMatch = false, flagSkipBoundJoinQuery = false;
        //maps a candidate var to CTP matched values
        HashMap<String, List<String>> mapCandVarToMatchedVals = new HashMap<>();
        //maps a candidate var to all answer values
        HashMap<String, List<String>> mapCandVarToAllAnsMaps = new HashMap<>();
        List<String> tpClean = null;
        List<String> allCTPVals = myDedUtils.getValuesFromCTP(indxCTP);
        List<String> tmpPattern = myDedUtils.getCleanTP(allCTPs.get(indxCTP));
        List<String> entryInformation = null;
        List<String> currAnsVals = null;
        List<String> currCTPValsMatched = null;
        List<String> answerEntryVars = new LinkedList<>();
        List<String> allVarAns = new LinkedList<>();
        flagSTOPNONPROJECTEDvars = false;
        
        if(indxCTP==13){
            
            int aezra=0;
        }

        if (allCTPVals == null) {

            allCTPVals = new LinkedList<>();
        }

        System.out.println("\t####### CTP No [" + (indxCTP + 1) + "] ####### ===> values to match: " + allCTPVals.size());
        System.out.print("\t\t" + DTPCandidates.get(indxCTP).get(0) + " ");
        System.out.print("" + DTPCandidates.get(indxCTP).get(1) + " ");
        System.out.println("" + DTPCandidates.get(indxCTP).get(2) + "\"");

        if (DTPCandidates.get(indxCTP).get(0).contains("?")
                && DTPCandidates.get(indxCTP).get(2).contains("?")) {

            System.out.println("\t\t\tCandidate Pattern skipped, no values to match");
            myDedUtils.setDTPHashInfo(myDedUtils.getCleanTP(DTPCandidates.get(indxCTP)), indxCTP);
            myBasUtils.insertToMap1(mapCTPtoAllDTPs, tmpPattern, tmpPattern);
            flagSkipPattern = true;
        }

        if (!flagSkipPattern) {

            for (int i = 0; i < allCTPVals.size(); i++) {

                tpClean = myDedUtils.getNewRawTP(tmpPattern, allCTPVals.get(i));
                mapDTPToFinishTime.put(tpClean, myBasUtils.getTimeInSec(mapCTPToFinishTime.get(indxCTP)));
                mapDTPToStartTime.put(tpClean, myBasUtils.getTimeInSec(mapCTPToStartTime.get(indxCTP)));
            }

            Collections.sort(allCTPVals);
        }

        if (!flagSkipPattern) {

            for (int key : mapAnsIDtoEntry.keySet()) {

                if (setMonetDB) {

                    entryInformation = myMonet.getEntryAnswers(key);
                } else {

                    entryInformation = mapAnsIDtoEntry.get(key);
                }

                flagSkipBoundJoinQuery = false;
                flagAnswerMatch = false;

                if (!entryInformation.isEmpty()) {

                    if (mapAnsIDToTimeSecs.get(key) == null) {

                        continue;
                    }

                    int TimeAnswTPSec = mapAnsIDToTimeSecs.get(key);

                    if (!(((TimeCandTPSecFinish - TimeAnswTPSec <= windowJoin) || TimeCandTPSecFinish - TimeAnswTPSec == 0)
                            || ((TimeCandTPSecStart - TimeAnswTPSec <= windowJoin) || TimeCandTPSecStart - TimeAnswTPSec == 0))) {
                        continue;
                    }

                    Answer = entryInformation.get(0);
                    OriginalQuery = entryInformation.get(4);

                    if (OriginalQuery.contains("SELECT *") || (Answer.contains("\"s\"") && Answer.contains("\"p\"") && Answer.contains("\"o\""))) {
                        continue;
                    }

                    if (!Answer.contains("boolean") || Answer.contains("results") && Answer.contains("value")) {

                        SPARQLEndpointPort = entryInformation.get(1);
                        answerEntryVars = myBasUtils.getAnswerVars(Answer);

                        for (int y = 0; y < answerEntryVars.size(); y++) {

                            if (flagSkipBoundJoinQuery) {
                                break;
                            }

                            if (answerEntryVars.get(y).contains("predicate")) {

                                continue;
                            }

                            if (answerEntryVars.get(y).contains("_")) {

                                flagSkipBoundJoinQuery = true;
                            }

                            candVar = myDedUtils.getCleanCandVar(answerEntryVars.get(y));

                            for (String currSignature : mapAnsEntryToAllSignatures.get(Integer.toString(key))) {

                                if (currSignature.contains("NoAnswersToQuery") || currSignature.contains("predicate")) {
                                    continue;
                                }

                                mapVar = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());
                                mapVar = mapVar.substring(0, mapVar.indexOf("_"));
                                mapVar = "?" + mapVar;
                                //  if(mapVar.equalsIgnoreCase(answerEntryVars.get(y)))   
                                {
                                    currAnsVals = mapAnsSingatureToAllValues.get(currSignature);

                                    if (currAnsVals != null) {

                                        currCTPValsMatched = myDedUtils.compareListsForIntersection(currAnsVals, allCTPVals);

                                        if (!currCTPValsMatched.isEmpty()) {

                                            flagAnswerMatch = true;

                                        }

                                        if (flagAnswerMatch) {

                                            String subKey = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());

                                            myBasUtils.insertToMap(mapCandVarToAllAnsMaps, currAnsVals, subKey);
                                            myBasUtils.insertToMap(mapCandVarToMatchedVals, currCTPValsMatched, subKey);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

            }
        }

        List<String> allCTPconsts = null;
        for (String matchVar : mapCandVarToMatchedVals.keySet()) {

            allCTPconsts = mapCandVarToMatchedVals.get(matchVar);
            Set<String> hs = new HashSet<>();
            hs.addAll(allCTPconsts);
            allCTPconsts.clear();
            allCTPconsts.addAll(hs);
            mapCandVarToMatchedVals.put(matchVar, allCTPconsts);

        }

        for (String matchVar : mapCandVarToAllAnsMaps.keySet()) {

            allCTPconsts = mapCandVarToAllAnsMaps.get(matchVar);
            Set<String> hs = new HashSet<>();
            hs.addAll(allCTPconsts);
            allCTPconsts.clear();
            allCTPconsts.addAll(hs);
        }

        processCandVars(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps,
                flagSkipPattern, indxCTP, sourceGraph, allCTPVals);
    }

    /**
     * Processes candidate variables, by removing false positives, combine
     * alternative mappings, and match variables to related Hash map structures
     *
     * @param mapCandVarToMatchedVals
     * @param mapCandVarToAllAnsMaps
     * @param flagSkipCTP
     * @param indxCTP
     * @param srcGraph
     * @param allCTPvals
     */
    public void processCandVars(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps,
            boolean flagSkipCTP, int indxCTP, int srcGraph, List<String> allCTPvals) {

        List<String> removeKeys = null;
        List<String> tmpPattern = null;

        //Remove false positives
        removeKeys = confirmMappings(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps, indxCTP);

        for (int k = 0; k < removeKeys.size(); k++) {

            mapCandVarToMatchedVals.remove(removeKeys.get(k));
        }

        //Combine alternative variables and match variables to corresponding hash maps
        matchDTPtoMaps(mapCandVarToMatchedVals, mapCandVarToAllAnsMaps, indxCTP, srcGraph);

        //Match a CTP directly to a DTP, when no candidate var matched 
        List<String> newCandVals = new LinkedList<>();
        if (mapCandVarToMatchedVals.isEmpty()) {

            if (allCTPvals == null) {

                tmpPattern = myDedUtils.getCleanTP(allCTPs.get(indxCTP));

                if (!flagSkipCTP) {

                    myDedUtils.setDTPHashInfo(tmpPattern, indxCTP);
                    allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
                    directMatchingTP(indxCTP, allCTPvals, tmpPattern);
                }

            } else {

                if (allCTPvals.size() <= 3) {

                    for (int i = 0; i < allCTPvals.size(); i++) {

                        tmpPattern = myDedUtils.getNewRawTP(allCTPs.get(indxCTP), allCTPvals.get(i));

                        if (!flagSkipCTP) {

                            myDedUtils.setDTPHashInfo(tmpPattern, indxCTP);
                            allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
                            newCandVals = new LinkedList<>();
                            newCandVals.add(allCTPvals.get(i));
                            directMatchingTP(indxCTP, allCTPvals, tmpPattern);
                        }
                    }
                } else {
                    tmpPattern = myDedUtils.getCleanTP(allCTPs.get(indxCTP));

                    if (!flagSkipCTP) {

                        myDedUtils.setDTPHashInfo(tmpPattern, indxCTP);
                        allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
                        directMatchingTP(indxCTP, allCTPvals, tmpPattern);
                    }

                }

            }
            if (!flagSkipCTP) {
                for (int i = 0; i < allCTPvals.size(); i++) {

                    tmpPattern = myDedUtils.getNewRawTP(allCTPs.get(indxCTP), allCTPvals.get(i));
                    myBasUtils.insertToMap1(mapCTPtoAllDTPs, tmpPattern, tmpPattern);

                }

            }

        }

        allCTPvals = myDedUtils.getValuesFromCTP(indxCTP);
        List<String> cleanCANDTP = myDedUtils.getCleanTP(DTPCandidates.get(indxCTP));

        for (int i = 0; i < allCTPvals.size(); i++) {

            tmpPattern = myDedUtils.getNewRawTP(allCTPs.get(indxCTP), allCTPvals.get(i));
            myBasUtils.insertToMap3(mapDTPToEndpsSrc, mapCTPToEndpsSrc.get(cleanCANDTP), tmpPattern);

        }

    }

    /**
     * Confirm candidate variables or cancel them
     *
     * @param mapCandVarToMatchedVals
     * @param mapCandVarToAllAnsMaps
     * @param indxCTP
     * @return
     */
    public List<String> confirmMappings(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps, int indxCTP) {

        List<String> valuesCandVar = null;
        List valuesCandVarAns = null;
        List<String> removeKeys = new LinkedList<>();

        System.out.println("\t\t\t\t\t ================ CONFIRM or REJECT matched vars ================");
        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            if (myBasUtils.elemInListEquals(removeKeys, keyOuter)) {

                continue;
            }

            valuesCandVarAns = mapCandVarToAllAnsMaps.get(keyOuter);
            valuesCandVar = mapCandVarToMatchedVals.get(keyOuter);
            double percentageMatchec = (double)valuesCandVar.size() / valuesCandVarAns.size();
            

            if(indxCTP==8){
                int ar=0;
            }
            if (valuesCandVarAns != null && valuesCandVar != null) {

                if(keyOuter.contains("drug")){
                    int azr=0;
                }
                //  if (valuesCandVarAns.size() - valuesCandVar.size() > 20 && valuesCandVar.size() < 10) {
                //                if ((percentageMatchec < invrmatchThresh)&&!(DTPCandidates.get(indxCTP).get(0).equalsIgnoreCase("?o")||DTPCandidates.get(indxCTP).get(2).equalsIgnoreCase("?o"))) {

                //BUUUUUUUUUUUUUUUUUG
                if ((percentageMatchec < inverseThresh&&!(Double.toString(percentageMatchec).contains("E")))||
                        (DTPCandidates.get(indxCTP).get(1).contains("?")&&percentageMatchec < 0.10)||
                        (valuesCandVar.size()==1&&percentageMatchec<0.04)) {

                    if (!myBasUtils.elemInListEquals(removeKeys, keyOuter)) {

                        System.out.println("\t\t\t\t\t\t CANCEL var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                        removeKeys.add(keyOuter);
                    }
                } else {

                    System.out.println("\t\t\t\t\t\t VALIDATE candidate var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                }

            } else if (valuesCandVar == null) {
                if (!myBasUtils.elemInListEquals(removeKeys, keyOuter) || keyOuter.contains("subject")) {

                    System.out.println("\t\t\t\t\t\t CANCEL var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
                    removeKeys.add(keyOuter);
                }
            } else {

                System.out.println("\t\t\t\t\t\t VALIDATE var: ?" + keyOuter.substring(0, keyOuter.indexOf("_")) + ", while actually mathced: " + (percentageMatchec * 100) + " %");
            }

        }

        System.out.println("\t\t\t\t\t ================ CONFIRM or REJECT matched vars ================");

        return removeKeys;
    }

    /**
     * Combine alternative mappings and match variables to related map
     * structures
     *
     * @param mapCandVarToMatchedVals
     * @param mapCandVarToAllAnsMaps
     * @param indxCTP
     * @param srcGraph
     */
    public void matchDTPtoMaps(HashMap<String, List<String>> mapCandVarToMatchedVals,
            HashMap<String, List<String>> mapCandVarToAllAnsMaps, int indxCTP, int srcGraph) {

        List<String> valueskeyOuter = null;
        List<String> valuesCandAnswMapOuter = null;
        List valuesCandAnswMapInner = null;
        List<String> newDedTP = null;
        List<String> valueskeyInner = null;
        List<List<String>> newPairTPs = null;
        List<String> newDedTPOuter = null;
        List<String> newDedTPInner = null;
        List<String> currEGouterTP = null;
        List<String> currEGinnerTP = null;
        List<List<String>> pairEGtoNested = new LinkedList<>();
        List<String> originalTP = myDedUtils.getCleanTP(DTPCandidates.get(indxCTP));
        HashMap<List<String>, Integer> mapMatchedConstant = new HashMap<>();
        int currentSizeDTPtoAlternatives = mapDTPtoAlternatives.size();
        List<String> allCTPVals = myDedUtils.getValuesFromCTP(indxCTP);
         List<String> cloneTP=null;
        String originalVar="";
        if(indxCTP==27){
            
            int araz=0;
        }
        
        if(DTPCandidates.get(indxCTP).get(0).contains("?")&&!DTPCandidates.get(indxCTP).get(2).contains("?")){
            originalVar=DTPCandidates.get(indxCTP).get(0);
        }
        
         if(!DTPCandidates.get(indxCTP).get(0).contains("?")&&DTPCandidates.get(indxCTP).get(2).contains("?")){
            originalVar=DTPCandidates.get(indxCTP).get(2);
        }

        for (int i = 0; i < allCTPVals.size(); i++) {

            List<String> newRAAAWTP = myDedUtils.getNewRawTP(DTPCandidates.get(indxCTP), allCTPVals.get(i));
            mapMatchedConstant.put(newRAAAWTP, -1);
        }

        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            String cleanVariable = "";
            cleanVariable = keyOuter.substring(0, keyOuter.indexOf("_"));
            cleanVariable = "?" + cleanVariable;
            newDedTP = myDedUtils.getInstanceCTP(DTPCandidates.get(indxCTP), cleanVariable);
            
            if(newDedTP.get(0).contains("?Int")&&newDedTP.get(1).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1>")){
                
                int arazr=0;
            }
             
            valueskeyOuter = mapCandVarToMatchedVals.get(keyOuter);
            valuesCandAnswMapOuter = mapCandVarToAllAnsMaps.get(keyOuter);
            List<String> tmpTP = myDedUtils.getCleanTP(newDedTP);
            List<List<String>> pair = new LinkedList<>();

            for (int i = 0; i < valuesCandAnswMapOuter.size(); i++) {

                List<String> rawTP = myDedUtils.getNewRawTP(originalTP, valuesCandAnswMapOuter.get(i));
                
                   if(rawTP.get(2).contains("http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00002")){
                
                    int arazr=0;
                 }

                for (int keyEG : mapEGtoCTPTmp.keySet()) {

                    List<String> currEG = mapEGtoCTPTmp.get(keyEG);
                    
                    if((mapEGtoOccurs.get(currEG)!=null&&mapEGtoOccurs.get(currEG)>1)||
                            (mapEGtoOccurs.get(currEG)!=null&&mapEGtoOccurs.get(currEG)==1)){
                                 for (int k = 0; k < currEG.size(); k += 3) {

                        currEGouterTP = new LinkedList<>();
                        currEGouterTP.add(currEG.get(k));
                        currEGouterTP.add(currEG.get(k + 1));
                        currEGouterTP.add(currEG.get(k + 2));

                        if (currEGouterTP.equals(rawTP)&&!currEGouterTP.contains("ontology")) {

                            for (int l = k + 3; l < currEG.size(); l += 3) {

                                currEGinnerTP = new LinkedList<>();
                                currEGinnerTP.add(currEG.get(l));
                                currEGinnerTP.add(currEG.get(l + 1));
                                currEGinnerTP.add(currEG.get(l + 2));

                                pairEGtoNested = new LinkedList<>();
                                pairEGtoNested.add(newDedTP);
                                pairEGtoNested.add(currEGinnerTP);
                                myDedUtils.pairJoinRelation(newDedTP, currEGinnerTP, pairEGtoNested, 1, "nestedLoop", true);
                            }

                        }

                    }  
                    }

         
                }

                myBasUtils.insertToMap1(mapCTPtoAllDTPs, newDedTP, rawTP);
                myBasUtils.insertToMap(mapCTPtoAllVars, cleanVariable, rawTP);
                if(mapMatchedConstant.get(rawTP)!=null)
                mapMatchedConstant.put(rawTP, 1);
            }

            for (int i = 0; i < valueskeyOuter.size(); i++) {

                List<String> rawTP = myDedUtils.getNewRawTP(originalTP, valueskeyOuter.get(i));
                myBasUtils.insertToMap1(mapCTPtoAllDTPs, newDedTP, rawTP);
                myBasUtils.insertToMap(mapCTPtoAllVars, cleanVariable, rawTP);
                    if(mapMatchedConstant.get(rawTP)!=null)
                mapMatchedConstant.put(rawTP, 1);
                    
                    if(newDedTP.size()==3){
                         cloneTP=myBasUtils.cloneListElems(newDedTP);
                        cloneTP.add(originalVar);
                           
                       if(mapCTPtoAnswTotal.get(rawTP)!=null){
                        
                        myBasUtils.insertToMap3(mapCTPtoAnswTotal, mapCTPtoAnswTotal.get(rawTP),cloneTP);
                       }
                    }
                 


            }

            myDedUtils.setDTPHashInfo(newDedTP, indxCTP);

            if (mapDTPtoJoinBGP.get(newDedTP) == null) {

                mapDTPtoJoinBGP.put(newDedTP, -1);
                mapDTPtoConceptBGP.put(newDedTP, -1);
            }

            if (valuesCandAnswMapInner != null) {

                tmpTP.add(cleanVariable);
                myBasUtils.insertToMap4(mapTPtoAnswersSourcesInverse, tmpTP, valuesCandAnswMapOuter);
                myBasUtils.insertToMap4(mapDTPtoAnswTotal, tmpTP, valuesCandAnswMapOuter);

            } else {

                tmpTP.add(cleanVariable);
                myBasUtils.insertToMap4(mapTPtoAnswersSourcesInverse, tmpTP, valueskeyOuter);
                myBasUtils.insertToMap4(mapDTPtoAnswTotal, tmpTP, valueskeyOuter);
            }

            directMatchingTP(indxCTP, valueskeyOuter, newDedTP);
            
        
                
        }

        for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

            valueskeyOuter = mapCandVarToMatchedVals.get(keyOuter);
            String cleanVariableOuter = "";
            cleanVariableOuter = keyOuter.substring(0, keyOuter.indexOf("_"));
            cleanVariableOuter = "?" + cleanVariableOuter;
            newDedTPOuter = myDedUtils.getInstanceCTP(DTPCandidates.get(indxCTP), cleanVariableOuter);

            for (String keyInner : mapCandVarToMatchedVals.keySet()) {

                if (keyInner.equalsIgnoreCase(keyOuter)) {
                    continue;
                }

                String cleanVariableInner = "";
                cleanVariableInner = keyInner.substring(0, keyInner.indexOf("_"));
                cleanVariableInner = "?" + cleanVariableInner;
                valueskeyInner = mapCandVarToMatchedVals.get(keyInner);

                if (!Collections.disjoint(valueskeyOuter, valueskeyInner)) {

                    newDedTPInner = myDedUtils.getInstanceCTP(DTPCandidates.get(indxCTP), cleanVariableInner);
                    newPairTPs = new LinkedList<>();
                    newPairTPs.add(newDedTPOuter);
                    newPairTPs.add(newDedTPInner);

                    mapDTPtoAlternatives.put(newDedTPOuter, currentSizeDTPtoAlternatives + 1);
                    mapDTPtoAlternatives.put(newDedTPInner, currentSizeDTPtoAlternatives + 1);
                    myDedUtils.addPairTPinNotNullJoinBGP(newPairTPs, (float) 1.0);
                    mapPairTPsToTypeJoin.put(newPairTPs, "alternativeMappings");
                }

            }
        }

        for (List<String> raw : mapMatchedConstant.keySet()) {

            int val = mapMatchedConstant.get(raw);

            if (val == -1) {
                
               // System.out.println("Gaaaaaaaaaaaaaaaaaaaaaaa: "+raw);
                for (String keyOuter : mapCandVarToMatchedVals.keySet()) {

                    String cleanVariable = "";
                    cleanVariable = keyOuter.substring(0, keyOuter.indexOf("_"));
                    cleanVariable = "?" + cleanVariable;
                    newDedTP = myDedUtils.getInstanceCTP(DTPCandidates.get(indxCTP), cleanVariable);
                    myBasUtils.insertToMap1(mapCTPtoAllDTPs, newDedTP, raw);

                    myBasUtils.insertToMap(mapCTPtoAllVars, keyOuter, raw);
                    mapMatchedConstant.put(raw, 1);
                }

            }
        }
        

    }

    /**
     * Match directly a Candidate Triple Pattern (CTP) or a Deduced Triple
     * Pattern (DTP) to its original variable's answers, for queries that
     * containing them
     *
     * @param indxCTP
     * @param candTPvals
     * @param deducedTP
     */
    public void directMatchingTP(int indxCTP, List<String> candTPvals, List<String> deducedTP) {

        List<String> tmpTP = null;
        List<String> answEntitiesOfTPinDTP = null;
        List<String> newDeducedTP = myDedUtils.getCleanTP(deducedTP);
        List<List<String>> newPairTPs = new LinkedList<>();
        List<List<String>> newPairTPsTmp = new LinkedList<>();
        List<String> outerTMP = new LinkedList<>();
        List<String> innerTMP = new LinkedList<>();
        String originalVAR = myDedUtils.getTPVariables(DTPCandidates.get(indxCTP)).get(0);
        newDeducedTP.add(originalVAR);

        for (int l = 0; l < candTPvals.size(); l++) {

            if (candTPvals.get(l).contains("?")) {
                continue;
            }

            tmpTP = myDedUtils.getInstanceCTP(allCTPs.get(indxCTP), candTPvals.get(l));
            tmpTP = myDedUtils.getCleanTP(tmpTP);
            answEntitiesOfTPinDTP = mapCTPtoAnswTotal.get(tmpTP);

            if (answEntitiesOfTPinDTP != null) {
                if (answEntitiesOfTPinDTP.size() > 0) {

                    myBasUtils.insertToMap4(mapDTPtoAnswTotal, newDeducedTP, answEntitiesOfTPinDTP);
                }
                if (myBasUtils.elemInListContained(answEntitiesOfTPinDTP, "noneProjected") && !flagSTOPNONPROJECTEDvars) {

                    newPairTPs.add(deducedTP);
                    newPairTPs.add(tmpTP);
                    flagSTOPNONPROJECTEDvars = true;
                    newPairTPsTmp = new LinkedList<>();
                    outerTMP = new LinkedList<>();
                    outerTMP.add(deducedTP.get(0));
                    outerTMP.add(deducedTP.get(1));
                    outerTMP.add(deducedTP.get(2));
                    innerTMP = new LinkedList<>();
                    innerTMP.add(tmpTP.get(0));
                    innerTMP.add(tmpTP.get(1));
                    innerTMP.add(tmpTP.get(2));
                    newPairTPsTmp.add(outerTMP);
                    newPairTPsTmp.add(innerTMP);

                    if (mapDTPofEGNested.get(outerTMP) == null && mapDTPofEGNested.get(innerTMP) == null) {

                        myDedUtils.addPairTPinNotNullJoinBGP(newPairTPsTmp, (float) 1.0);

                    }
                }
            }
        }

    }

    /**
     * Set original CTP's variables answer to corresponding hash maps
     *
     * @param triplePattern
     * @param indxLogCleanQuery
     * @param variableOriginal
     * @param indxPattern
     * @param constantVal
     */
    public void matchAnswOrigVarsToCTP(List<String> triplePattern, int indxLogCleanQuery,
            String variableOriginal, int indxPattern, String constantVal) {

        String Answer = "", ClientIpAddress = "", requestQuery = "", receptTime = "", endpoint = "";
        List<String> matchQueryExtrVars = null;
        List<String> entryInformation = null;
        List<String> answerEntities = null;
        List<String> tmpTP = new LinkedList<>();
        List<String> tpVars = new LinkedList<>();
        boolean flagPassed = false;
        String constantValue = "";

        if (variableOriginal.equals("")) {

            tpVars = myDedUtils.getTPVariables(triplePattern);
        } else {

            tpVars.add(variableOriginal);
        }
        
        if(indxPattern==27){
            
            int araz=0;
        }

        constantValue = myDedUtils.getTPConstants(triplePattern);
        List<Integer> key = mapLogClQueryToAnsEntry.get(indxLogCleanQuery);

        if (key != null) {

            if (key.size() > 0) {

                for (int k = 0; k < key.size(); k++) {

                    entryInformation = mapAnsIDtoEntry.get(key.get(k));
                    if (entryInformation != null) {

                        if (!entryInformation.isEmpty()) {

                            Answer = entryInformation.get(0);
                            endpoint = entryInformation.get(1);
                            ClientIpAddress = entryInformation.get(2);
                            receptTime = entryInformation.get(3);
                            requestQuery = entryInformation.get(4);

                            myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, triplePattern);

                            String indxQueryString = mapAnsIDToLogClQuery.get(key.get(k));
                            myBasUtils.insertToMap2(mapCTPIDtoQuerySrc, Integer.parseInt(indxQueryString), indxPattern);
                            List<Integer> test = new LinkedList<>();
                            test.add(Integer.parseInt(indxQueryString));
                            mapDTPtoInnerQuery.put(indxPattern, test);

                            if (!requestQuery.contains(constantValue)) {

                                break;
                            } else {

                                matchQueryExtrVars = myBasUtils.getProjVars(requestQuery);
                            }

                            if ((((Answer.contains("results"))
                                    && !Answer.contains("boolean")) || Answer.contains("value"))) {

                                for (int u = 0; u < matchQueryExtrVars.size(); u++) {

                                    if (myBasUtils.elemInListContained(tpVars, matchQueryExtrVars.get(u))) {

                                        answerEntities = mapAnsEntryToListValues.get(key.get(k) + (matchQueryExtrVars.get(u).substring(matchQueryExtrVars.get(u).indexOf("?") + 1)));
                                        if (answerEntities == null) {

                                            answerEntities = mapAnsEntryToListValues.get(key.get(k) + (matchQueryExtrVars.get(u).substring(matchQueryExtrVars.get(u).indexOf("?") + 1).toLowerCase()));
                                        }

                                        if (answerEntities == null) {

                                            answerEntities = new LinkedList<>();
                                        }

                                        tmpTP = myDedUtils.getCleanTP(triplePattern);
                                        tmpTP.add(matchQueryExtrVars.get(u));

                                        myBasUtils.insertToMap4(mapCTPtoAnswTotal, triplePattern, answerEntities);
                                        myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, triplePattern);

                                        if (tmpTP.get(0).contains("?") && tmpTP.get(2).contains("?") || !inverseMapping) {
                                            myBasUtils.insertToMap4(mapDTPtoAnswTotal, tmpTP, answerEntities);
                                        }

                                        myBasUtils.insertToMap4(mapCTPtoAnswTotal, tmpTP, answerEntities);
                                        myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, tmpTP);

                                        flagPassed = true;
                                    }

                                }
                            }
                        }
                    }

                }

                if (flagPassed == false) {

                    answerEntities = new LinkedList<>();
                    answerEntities.add("noneProjected");
                    myBasUtils.insertToMap4(mapCTPtoAnswTotal, triplePattern, answerEntities);
                    myBasUtils.insertToMap(mapCTPToEndpsSrc, endpoint, triplePattern);
                }

            }
        }
    }

    /**
     * Particular function for ANAPPSID traces. Captute filter option values, as
     * possible inverse mapping values of a NLFO
     */
    public void checkNestedLoopFilter() {

        String extractedVar = "";
        String currQuery = "";
        List<Integer> sourceQueries = null;
        List<String> tmpTP = null;
        List<String> extractedVals = null;

        for (List<String> key : mapCTPtoQuerySrc.keySet()) {

            sourceQueries = mapCTPtoQuerySrc.get(key);

            for (int i = 0; i < sourceQueries.size(); i++) {

                currQuery = queries.get(sourceQueries.get(i));

                //Buuuuuuuuuuuuuug
                if (currQuery.contains("mass")) {
                    continue;
                }

                if (currQuery.contains("filter") || currQuery.contains("FILTER")) {

                    extractedVals = myBasUtils.getFILTERvals(currQuery);
                    extractedVar = myBasUtils.getFILTERvar(currQuery);

                    if (extractedVals.size() >= 1) {
                        if (key.get(0).equalsIgnoreCase(extractedVar) || key.get(2).equalsIgnoreCase(extractedVar)) {

                            tmpTP = myDedUtils.getCleanTP(key);
                            tmpTP.add(extractedVar);
                            myBasUtils.insertToMap4(mapTPtoAnswersSourcesInverse, tmpTP, extractedVals);
                        }
                    }

                }

            }
        }

    }

    /**
     * Particular function for FedX traces. Cancle joins in which a DTP,
     * evaluated with NLEG and falsely believed as an exclusive group,
     * participated
     *
     *
     */
    public void cancelJoinsNLEG() {

        List<String> currEG = null;
        List<String> currOuterTP = null;
        List<String> currInnerTP = null;

        for (int keyOuter : mapEGtoCTPTmp.keySet()) {

            if (mapEGtoCancel.get(keyOuter) != null) {

                currEG = mapEGtoCTPTmp.get(keyOuter);

                for (int i = 0; i < currEG.size(); i += 3) {

                    currOuterTP = new LinkedList<>();
                    currOuterTP.add(currEG.get(i));
                    currOuterTP.add(currEG.get(i + 1));
                    currOuterTP.add(currEG.get(i + 2));

                    if (!currOuterTP.get(0).contains("?") && !currOuterTP.get(0).contains("<http://bio2rdf.org/ns/kegg#Enzyme>")
                            || !currOuterTP.get(2).contains("?") && !currOuterTP.get(2).contains("<http://bio2rdf.org/ns/kegg#Enzyme>")) {

                        mapDTPtoCANCELofEG.put(currOuterTP, 1);
                        int currID = mapDTPToDeducedID.get(currOuterTP);
                        mapDTPToAnyJoin.remove(currID);
                        mapDTPToDeducedID.remove(currOuterTP);
                        mapDTPToEndpsSrc.remove(currOuterTP);
                        mapDTPToFinishTime.remove(currOuterTP);
                        mapDTPToStartTime.remove(currOuterTP);
                        mapDTPToTimeDeduction.remove(currOuterTP);
                        mapDTPtoAnswTotal.remove(currOuterTP);
                        mapDTPtoConceptBGP.remove(currOuterTP);
                        mapDTPtoJoinBGP.remove(currOuterTP);

                        for (int j = i + 3; j < currEG.size(); j += 3) {

                            currInnerTP = new LinkedList<>();
                            currInnerTP.add(currEG.get(j));
                            currInnerTP.add(currEG.get(j + 1));
                            currInnerTP.add(currEG.get(j + 2));
                            totalPairs--;
                        }
                    }

                }
            }

        }

    }

    public void matchFedXFilterValsToVariable() throws SQLException, InstantiationException, IllegalAccessException {

        String Answer = "", OriginalQuery = "";
        String mapVar = "";
        boolean flagAnswerMatch = false, flagSkipBoundJoinQuery = false;
        //maps a candidate var to CTP matched values
        HashMap<String, List<String>> mapCandVarToMatchedVals = new HashMap<>();
        //maps a candidate var to all answer values
        HashMap<String, List<String>> mapCandVarToAllAnsMaps = new HashMap<>();
        List<String> allFilterVals = null;
        List<String> entryInformation = null;
        List<String> currAnsVals = null;
        List<String> currCTPValsMatched = null;
        List<String> answerEntryVars = new LinkedList<>();
        List<String> allVarAns = new LinkedList<>();
        List<List<String>> allNewDTPShort = null;
        HashMap<List<String>, List<String>> newDTPtoALtenativeTotalAns = new HashMap<>();
        HashMap<List<String>, List<String>> newDTPtoALtenativeInverseMap = new HashMap<>();

        
        if (setMonetDB) {

            myMonet.openSession("jdbc:monetdb://localhost/demo", "feta", "feta");
        }
        
        for (List<String> keyFilterVar : mapCTPtoFILTERUNION.keySet()) {

            allFilterVals = mapCTPtoFILTERUNION.get(keyFilterVar);

            //If there is only one FILTER value of BOUND JOIN, skip matching
            if (allFilterVals.size() == 1) {
                continue;
            }
            for (int key : mapAnsIDtoEntry.keySet()) {

                if (setMonetDB) {

                    entryInformation = myMonet.getEntryAnswers(key);
                } else {

                    entryInformation = mapAnsIDtoEntry.get(key);
                }

                flagSkipBoundJoinQuery = false;
                flagAnswerMatch = false;

                if (!entryInformation.isEmpty()) {

                    Answer = entryInformation.get(0);
                    OriginalQuery = entryInformation.get(4);

                    if (OriginalQuery.contains("?s ?p ?o") || (Answer.contains("\"s\"") && Answer.contains("\"p\"") && Answer.contains("\"o\""))) {
                        continue;
                    }

                    if (!Answer.contains("boolean") || Answer.contains("results") && Answer.contains("value")) {

                        answerEntryVars = myBasUtils.getAnswerVars(Answer);

                        for (int y = 0; y < answerEntryVars.size(); y++) {

                            if (flagSkipBoundJoinQuery) {
                                break;
                            }

                            if (answerEntryVars.get(y).contains("predicate")) {

                                continue;
                            }

                            if (answerEntryVars.get(y).contains("_")) {

                                flagSkipBoundJoinQuery = true;
                            }

                            for (String currSignature : mapAnsEntryToAllSignatures.get(Integer.toString(key))) {

                                if (currSignature.contains("NoAnswersToQuery") || currSignature.contains("predicate")) {
                                    continue;
                                }

                                mapVar = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());
                                mapVar = mapVar.substring(0, mapVar.indexOf("_"));
                                mapVar = "?" + mapVar;

                                String mapAnsVar = answerEntryVars.get(y);

                                if (!mapAnsVar.contains("?")) {

                                    mapAnsVar = "?" + mapAnsVar;
                                }

                                if (mapVar.equalsIgnoreCase(mapAnsVar)) {
                                    currAnsVals = mapAnsSingatureToAllValues.get(currSignature);

                                    if (currAnsVals != null) {

                                        currCTPValsMatched = myDedUtils.compareListsForIntersection(currAnsVals, allFilterVals);

                                        if (!currCTPValsMatched.isEmpty()) {

                                            flagAnswerMatch = true;

                                        }

                                        if (flagAnswerMatch) {

                                            String subKey = currSignature.substring(currSignature.indexOf("_") + 1, currSignature.length());

                                            myBasUtils.insertToMap(mapCandVarToAllAnsMaps, currAnsVals, subKey);
                                            myBasUtils.insertToMap(mapCandVarToMatchedVals, currCTPValsMatched, subKey);

                                        }
                                    }
                                }
                            }

                        }
                    }
                }

            }

            allNewDTPShort = new LinkedList<>();
            List<String> matchingValues = new LinkedList<>();

            for (String matchVar : mapCandVarToMatchedVals.keySet()) {

                if (!matchVar.contains("o_") && mapCandVarToMatchedVals.get(matchVar).size() > 0) {

                    for (List<String> keyDTP : mapDTPtoAnswTotal.keySet()) {

                        if (keyDTP.get(1).equalsIgnoreCase(keyFilterVar.get(1))) {

                            if (keyDTP.get(0).equalsIgnoreCase(keyFilterVar.get(0)) && keyDTP.get(0).equalsIgnoreCase("?o") && !keyDTP.get(3).equalsIgnoreCase("?o")) {

                                List<String> newDTP = new LinkedList<>();
                                newDTP.add("?" + matchVar.substring(0, matchVar.indexOf("_")));
                                newDTP.add(keyDTP.get(1));
                                newDTP.add(keyDTP.get(2));

                                if (mapDTPToDeducedID.get(newDTP) == null) {
                                    mapDTPToDeducedID.put(newDTP, mapDTPToDeducedID.size() + 1);

                                }

                                if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(newDTP)) == null) {

                                    mapDTPToAnyJoin.put(mapDTPToDeducedID.get(newDTP), -1);
                                }

                                allNewDTPShort.add(newDTP);
                                newDTP.add(keyDTP.get(3));
                                matchingValues.addAll(mapCandVarToMatchedVals.get(matchVar));
                                newDTPtoALtenativeTotalAns.put(newDTP, keyDTP);

                            } else if (keyDTP.get(2).equalsIgnoreCase(keyFilterVar.get(2)) && keyDTP.get(2).equalsIgnoreCase("?o") && !keyDTP.get(3).equalsIgnoreCase("?o")) {
                                List<String> newDTP = new LinkedList<>();

                                newDTP.add(keyDTP.get(0));
                                newDTP.add(keyDTP.get(1));
                                newDTP.add("?" + matchVar.substring(0, matchVar.indexOf("_")));

                                if (mapDTPToDeducedID.get(newDTP) == null) {
                                    mapDTPToDeducedID.put(newDTP, mapDTPToDeducedID.size() + 1);

                                }

                                if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(newDTP)) == null) {
                                    mapDTPToAnyJoin.put(mapDTPToDeducedID.get(newDTP), -1);

                                }

                                allNewDTPShort.add(newDTP);
                                newDTP.add(keyDTP.get(3));
                                newDTPtoALtenativeTotalAns.put(newDTP, keyDTP);
                                matchingValues.addAll(mapCandVarToMatchedVals.get(matchVar));
                            }
                        }
                    }

                }

                for (List<String> keyDTP : mapTPtoAnswersSourcesInverse.keySet()) {

                    for (List<String> newDTPfilter : allNewDTPShort) {

                        if ((keyDTP.get(0).equalsIgnoreCase("?o") && (newDTPfilter.get(2).equalsIgnoreCase(keyDTP.get(2)) && newDTPfilter.get(1).equalsIgnoreCase(keyDTP.get(1)))) || (keyDTP.get(2).equalsIgnoreCase("?o") && (newDTPfilter.get(0).equalsIgnoreCase(keyDTP.get(0)) && newDTPfilter.get(1).equalsIgnoreCase(keyDTP.get(1))))) {

                            List<String> newDTP = new LinkedList<>();
                            newDTP.add(newDTPfilter.get(0));
                            newDTP.add(newDTPfilter.get(1));
                            newDTP.add(newDTPfilter.get(2));

                            if (!myBasUtils.elemInListContained(newDTP, "?" + matchVar.substring(0, matchVar.indexOf("_")))) {
                                continue;
                            }
                            if (mapDTPToDeducedID.get(newDTP) == null) {
                                mapDTPToDeducedID.put(newDTP, mapDTPToDeducedID.size() + 1);

                            }

                            if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(newDTP)) == null) {
                                mapDTPToAnyJoin.put(mapDTPToDeducedID.get(newDTP), -1);

                            }

                            newDTP.add("?" + matchVar.substring(0, matchVar.indexOf("_")));
                            newDTPtoALtenativeInverseMap.put(newDTP, keyDTP);

                        }
                    }

                }

            }

            for (List<String> keyTotalNEW : newDTPtoALtenativeTotalAns.keySet()) {

                List<String> tripletTMP = new LinkedList<>();
                List<String> tripletNew = new LinkedList<>();

                tripletNew.add(keyTotalNEW.get(0));
                tripletNew.add(keyTotalNEW.get(1));
                tripletNew.add(keyTotalNEW.get(2));
                tripletTMP.add(newDTPtoALtenativeTotalAns.get(keyTotalNEW).get(0));
                tripletTMP.add(newDTPtoALtenativeTotalAns.get(keyTotalNEW).get(1));
                tripletTMP.add(newDTPtoALtenativeTotalAns.get(keyTotalNEW).get(2));

                mapDTPToEndpsSrc.put(tripletNew, mapDTPToEndpsSrc.get(tripletTMP));

                if (mapDTPToDeducedID.get(tripletTMP) == null) {
                    mapDTPToDeducedID.put(tripletTMP, mapDTPToDeducedID.size() + 1);

                }

                if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(tripletTMP)) == null) {
                    mapDTPToAnyJoin.put(mapDTPToDeducedID.get(tripletTMP), -1);

                }

                mapTPtoAnswersSourcesInverse.put(keyTotalNEW, mapTPtoAnswersSourcesInverse.get(newDTPtoALtenativeTotalAns.get(keyTotalNEW)));
                mapDTPtoAnswTotal.put(keyTotalNEW, mapDTPtoAnswTotal.get(newDTPtoALtenativeTotalAns.get(keyTotalNEW)));
                mapDTPToStartTime.put(tripletNew, mapDTPToStartTime.get(tripletTMP));
                mapDTPToFinishTime.put(tripletNew, mapDTPToFinishTime.get(tripletTMP));
                int currentSizeDTPtoAlternatives = mapDTPtoAlternatives.size();
                mapDTPtoAlternatives.put(tripletTMP, currentSizeDTPtoAlternatives);
                mapDTPtoAlternatives.put(tripletNew, currentSizeDTPtoAlternatives);

            }

            for (List<String> keyTotalInvMaps : newDTPtoALtenativeInverseMap.keySet()) {

                List<String> tripletTMP = new LinkedList<>();
                List<String> tripletNew = new LinkedList<>();
                tripletNew.add(keyTotalInvMaps.get(0));
                tripletNew.add(keyTotalInvMaps.get(1));
                tripletNew.add(keyTotalInvMaps.get(2));

                if (mapDTPToDeducedID.get(tripletNew) == null) {
                    mapDTPToDeducedID.put(tripletNew, mapDTPToDeducedID.size() + 1);

                }

                if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(tripletNew)) == null) {
                    mapDTPToAnyJoin.put(mapDTPToDeducedID.get(tripletNew), -1);

                }
                tripletTMP.add(newDTPtoALtenativeInverseMap.get(keyTotalInvMaps).get(0));
                tripletTMP.add(newDTPtoALtenativeInverseMap.get(keyTotalInvMaps).get(1));
                tripletTMP.add(newDTPtoALtenativeInverseMap.get(keyTotalInvMaps).get(2));

                mapDTPtoAnswTotal.put(keyTotalInvMaps, matchingValues);
                mapDTPToEndpsSrc.put(tripletNew, mapDTPToEndpsSrc.get(tripletTMP));
                mapDTPToStartTime.put(tripletNew, mapDTPToStartTime.get(tripletTMP));
                mapDTPToFinishTime.put(tripletNew, mapDTPToFinishTime.get(tripletTMP));
                int currentSizeDTPtoAlternatives = mapDTPtoAlternatives.size();
                mapDTPtoAlternatives.put(tripletTMP, currentSizeDTPtoAlternatives);
                mapDTPtoAlternatives.put(tripletNew, currentSizeDTPtoAlternatives);
            }

        }

    }

}
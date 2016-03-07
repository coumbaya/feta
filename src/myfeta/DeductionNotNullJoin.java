package myfeta;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static myfeta.Deduction.cntEGTrPo;
import static myfeta.Deduction.mapDTPtoCANCELofEG;
import static myfeta.Deduction.mapEndpointToName;
import static myfeta.Deduction.mapGroundTruthPairs;
import static myfeta.Deduction.mapLogClQueryToAllTPEnts;
import static myfeta.Deduction.mapLogClQueryToAnsEntry;
import static myfeta.Deduction.mapLogClQueryToTimestamp;
import static myfeta.Deduction.mapTPtoAnswersSourcesInverse;
import static myfeta.Deduction.mapTruePositivePairs;
import static myfeta.Deduction.truePositivesPairs;
import static myfeta.DeductionLogClean.engineIPAddress;
import static myfeta.DeductionLogClean.queries;
import static myfeta.DeductionNestedLoop.mapDTPToAnyJoin;
import static myfeta.DeductionNestedLoop.mapDTPToDeducedID;
import static myfeta.DeductionNestedLoop.mapDTPToEndpsSrc;
import static myfeta.DeductionNestedLoop.mapDTPToFinishTime;
import static myfeta.DeductionNestedLoop.mapDTPToStartTime;
import static myfeta.DeductionNestedLoop.mapDTPToTimeDeduction;
import static myfeta.DeductionNestedLoop.mapDTPofEGNested;
import static myfeta.DeductionNestedLoop.mapDTPpair;
import static myfeta.DeductionNestedLoop.mapDTPtoAlternatives;
import static myfeta.DeductionNestedLoop.mapDTPtoAnswTotal;
import static myfeta.DeductionNestedLoop.mapDTPtoInnerQuery;
import static myfeta.DeductionNestedLoop.mapDTPtoJoinBGP;
import static myfeta.DeductionNestedLoop.mapPairExclGroup;
import static myfeta.DeductionNestedLoop.notnullJoinBGPs;
import static myfeta.DeductionUtils.listBGPsremoveAnswer;
import static myfeta.Main.collectionName;
import static myfeta.Main.engineName;
import static myfeta.Main.single;
import static myfeta.Main.windowJoin;

/**
 * Class for "NotNullJoin" heuristic, for those DTPs not related in a nested
 * loop implementation, associate them based on their answers simulating a
 * symhash join operation
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class DeductionNotNullJoin {

    DeductionUtils myDedUtils;
    Deduction myDeduction;
    DeductionLogClean myLogClean;
    DeductionSameConceptOrAs myDedSameConcp;
    BasicUtilis myBasUtils;

    public static List<List<List<String>>> setDTPbasedAnswersLatest;
    public static List<List<List<String>>> notnullJoinBGPsFinal;
    // map each pair of deduced triple patterns to their associated relation: Alternative inverese mapping vars, exclusive groups, nested loop or symmetric hash
    public static HashMap<Integer, List<Integer>> mapNodeToMergeTPs;
    public static HashMap<Integer, String> mapNodeToNewString;
    public static HashMap<List<Integer>, String> mapPairNodesJoinRel;
    public static HashMap<Integer, Integer> mapOldIDToNew;
    public static HashMap<Integer, List<String>> mapBGPtoPredicates;
    public static HashMap<List<List<String>>, Integer> mapAlreadySeenJoin;
    public static HashMap<List<Integer>, String> mapPairNodesConnection;
    List<Integer> alreadySeenTPs;
    List<Integer> alreadySeenConnectedNestedTPs;
    List<Integer> alreadySeenConnectedSymhashTPs;
    List<String> allEndpoints;

    public DeductionNotNullJoin() throws ParserConfigurationException {

        myDedUtils = new DeductionUtils();
        myDeduction = new Deduction();
        myLogClean = new DeductionLogClean();
        myDedSameConcp = new DeductionSameConceptOrAs();
        myBasUtils = new BasicUtilis();

        notnullJoinBGPsFinal = new LinkedList<>();
        alreadySeenTPs = new LinkedList<>();
        allEndpoints = new LinkedList<>();
        alreadySeenConnectedNestedTPs = new LinkedList<>();
        alreadySeenConnectedSymhashTPs = new LinkedList<>();
        setDTPbasedAnswersLatest = new LinkedList<>();
        mapBGPtoPredicates = new HashMap<>();
        mapAlreadySeenJoin = new HashMap<>();
    }

    /**
     * Implement "NotNullJoin" heuristic
     *
     * @param window
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    public void notNullJoin(int window) throws TransformerException, ParserConfigurationException, URISyntaxException, InterruptedException, IOException {

        //find join relation between triple patterns (i.e., "exclusive group", 
        //"nested loop" or "symhash")
        notNullJoinComparison();

        //find which deduced triple paterns do not belong to any FETA BGP graph
        //and get the total number of BGPs (i.e., deduced by FETA + subqueries not combined)
        // totalBGPs = triplePatternsNotInDeducedBGP();
        showMissingPairs();

        // set ground truth structures, for isolated query execution
        if (single) {

            myDedUtils.setGroundTruthHashMaps();
        }

    }

    /**
     * Show content of each deduced BGP
     *
     * @throws IOException
     */
    public void showDeducedBGPs() throws IOException {

        int idTP = 0;
        int idBGP = 0;

        List<String> currTP = new LinkedList<>();

        System.out.println("\t*********Query Engine IP Address: " + engineIPAddress);
        System.out.println("\t*********Time interval of captured queries ["
                + mapLogClQueryToTimestamp.get(0)
                + "---" + mapLogClQueryToTimestamp.get(queries.size() - 1) + "]");
        System.out.println("\t\t ************************************** Deduced Graphs sets with JOIN intersection*********************** ");

        // remove "redundant" graphs, that were identified progressively as a
        // new pair of deduced triple patterns was also identiified in method "addPairTPinNotNullJoinBGP"
        for (int j = 0; j < notnullJoinBGPs.size(); j++) {

            if (!myBasUtils.elemInListEquals(listBGPsremoveAnswer, j)) {

                notnullJoinBGPsFinal.add(notnullJoinBGPs.get(j));
            }

        }

        //Print all deduced BGPs
        for (int i = 0; i < notnullJoinBGPsFinal.size(); i++) {

            idTP = 0;
            idBGP++;
            System.out.println("\t\t\t Graph no [" + idBGP + "] " + "size : " + notnullJoinBGPsFinal.get(i).size());

            for (int j = 0; j < notnullJoinBGPsFinal.get(i).size(); j++) {

                currTP = new LinkedList<>();
                currTP.add(notnullJoinBGPsFinal.get(i).get(j).get(0));
                currTP.add(notnullJoinBGPsFinal.get(i).get(j).get(1));
                currTP.add(notnullJoinBGPsFinal.get(i).get(j).get(2));

                List<String> allEndpointsPorts = mapDTPToEndpsSrc.get(currTP);
                List<String> allEndpointsNames = new LinkedList<>();

                if (allEndpointsPorts != null) {
                    HashSet hs = new HashSet();
                    hs.addAll(allEndpointsPorts);
                    allEndpointsPorts.clear();
                    allEndpointsPorts.addAll(hs);
                    Collections.sort(allEndpointsPorts);

                    for (int t = 0; t < allEndpointsPorts.size(); t++) {

                        allEndpointsNames.add(mapEndpointToName.get(allEndpointsPorts.get(t)));

                    }
                } else {
                  //  System.out.println("BUUUG no endpoints matched for: " + currTP);
                    continue;
                }

                idTP++;
                myDedUtils.updatePrecisionRecalTPinfo(currTP);

                System.out.println("\t\t\t\t TP no [" + idTP + "] " + notnullJoinBGPsFinal.get(i).get(j).get(0) + " "
                        + notnullJoinBGPsFinal.get(i).get(j).get(1) + " " + notnullJoinBGPsFinal.get(i).get(j).get(2) + " ");
                System.out.println("\t\t\t\t\t received at: " + allEndpointsNames);
            }

        }
    }

    /**
     * For every BGP, sort its DTPs according to the order they were deduced
     *
     * @param bgpList
     * @param indxGraph
     * @return
     */
    public List<List<String>> sortBGPtoTimestamp(List<List<String>> bgpList, int indxGraph) {

        int index = 0;
        boolean found = true;

        //sort list of deduced triple patterns
        //this method simulates "bubble" sort
        while (found) {

            found = false;
            for (int i = 0; i < bgpList.size() - 1; i++) {

                //get the "serial" order of its deduced tp, and store it
                //as "serial" order for the current BGP
                index = mapDTPToTimeDeduction.get(bgpList.get(i));
                mapDTPtoJoinBGP.put(bgpList.get(i), indxGraph);

                //If tps in positions "i" and "i+1" are not sorted, interchange them
                if (index > mapDTPToTimeDeduction.get(bgpList.get(i + 1))) {

                    List<String> tmpList = new LinkedList<>();
                    tmpList = myBasUtils.cloneListElems(bgpList.get(i));
                    bgpList.get(i).clear();
                    bgpList.get(i).addAll(bgpList.get(i + 1));
                    bgpList.get(i + 1).clear();
                    bgpList.get(i + 1).addAll(tmpList);
                    found = true;
                }

            }
        }

        return bgpList;
    }

    /**
     * Show BGPs which correspond to subqueries, for all triple patterns that
     * were not joined (i.e., "symhash", "constant" or "nestedLoop") with any
     * othe triple pattern
     *
     * @return
     */
    public int triplePatternsNotInDeducedBGP() {

        int count = 0;
        int count2 = 0;
        int currDTP = 0;
        int currDTPTOken = 0;
        List<Integer> sourceQueries = null;
        List<Integer> alreadySeen = new LinkedList<>();
        List<String> currTP = new LinkedList<>();
        HashMap<Integer, List<Integer>> mapQueriesToDeducedTPs = new HashMap<>();
        HashMap<List<List<String>>, Integer> alreadySeenPair = new HashMap<>();
        HashMap<List<String>, Integer> alreadySeenTP = new HashMap<>();
        List<List<String>> allTPsInQuery = new LinkedList<>();
        List<String> outerTP = new LinkedList<>();
        List<String> innerTP = new LinkedList<>();

        for (List<String> key : mapDTPToDeducedID.keySet()) {

            List<String> keyShort = new LinkedList<>();
            keyShort.add(key.get(0));
            keyShort.add(key.get(1));
            keyShort.add(key.get(2));

            if (mapDTPToDeducedID.get(key) != null) {

                currDTP = mapDTPToDeducedID.get(key);
                currDTPTOken = mapDTPToAnyJoin.get(currDTP);
            } else if (mapDTPToDeducedID.get(keyShort) != null) {

                currDTP = mapDTPToDeducedID.get(keyShort);
                currDTPTOken = mapDTPToAnyJoin.get(currDTP);
            }
            if (currDTPTOken == -1) {

                sourceQueries = mapDTPtoInnerQuery.get(currDTP);

                for (int i = 0; i < sourceQueries.size(); i++) {

                    if (!alreadySeen.contains(currDTP)) {

                        myBasUtils.insertToMap2(mapQueriesToDeducedTPs, sourceQueries.get(i), currDTP);
                        alreadySeen.add(currDTP);
                    }

                }
            }
        }

        for (Integer key : mapQueriesToDeducedTPs.keySet()) {

            count = 0;
            count2++;
            List<Integer> tmpList = mapQueriesToDeducedTPs.get(key);
            HashSet hs = new HashSet();
            hs.addAll(tmpList);
            tmpList.clear();
            tmpList.addAll(hs);
            Collections.sort(tmpList);

            for (int k = 0; k < tmpList.size(); k++) {
                List<String> tmpentites = mapLogClQueryToAllTPEnts.get(tmpList.get(k));

                if (queries.get(tmpList.get(k)).contains("UNION") || queries.get(tmpList.get(k)).contains("UNION")) {

                    for (int l = 0; l < tmpentites.size(); l += 3) {

                        count2++;

                        System.out.println("\t\t\t Graph no [" + count2 + "] " + "size : " + 1 + " because of DTP out of timestamp Tjoin of LogClean query no"
                                + tmpList + " corresponding to LogAnswer entries: " + mapLogClQueryToAnsEntry.get(tmpList.get(k)));

                        count++;
                        currTP = new LinkedList<>();
                        currTP.add(tmpentites.get(l + 0));
                        currTP.add(tmpentites.get(l + 1));
                        currTP.add(tmpentites.get(l + 2));

                        myDedUtils.updatePrecisionRecalTPinfo(currTP);

                        System.out.println("\t\t\t\t TP no [1" + "] " + mapLogClQueryToAllTPEnts.get(tmpList.get(k)).get(l + 0) + " "
                                + mapLogClQueryToAllTPEnts.get(tmpList.get(k)).get(l + 1) + " " + mapLogClQueryToAllTPEnts.get(tmpList.get(k)).get(l + 2) + " ");

                        List<String> allEndpointsPorts = mapDTPToEndpsSrc.get(currTP);

                        hs = new HashSet();
                        hs.addAll(allEndpointsPorts);
                        allEndpointsPorts.clear();
                        allEndpointsPorts.addAll(hs);
                        Collections.sort(allEndpointsPorts);

                        List<String> allEndpointsNames = new LinkedList<>();

                        for (int t = 0; t < allEndpointsPorts.size(); t++) {

                            allEndpointsNames.add(mapEndpointToName.get(allEndpointsPorts.get(t)));

                        }

                        System.out.println("\t\t\t\t\t received at: " + allEndpointsNames);
                    }
                } else {
                    System.out.println("\t\t\t Graph no [" + count2 + "] " + "size : " + 1 + " because of DTP out of timestamp Tjoin of LogClean query no"
                            + tmpList + " corresponding to LogAnswer entries: " + mapLogClQueryToAnsEntry.get(tmpList.get(k)));

                    allTPsInQuery = new LinkedList<>();
                    for (int l = 0; l < tmpentites.size(); l += 3) {

                        count++;
                        currTP = new LinkedList<>();
                        currTP.add(tmpentites.get(l + 0));
                        currTP.add(tmpentites.get(l + 1));
                        currTP.add(tmpentites.get(l + 2));

                        myDedUtils.updatePrecisionRecalTPinfo(currTP);

                        allTPsInQuery.add(currTP);
                        System.out.println("\t\t\t\t TP no [" + count + "] " + tmpentites.get(l + 0) + " "
                                + tmpentites.get(l + 1) + " " + tmpentites.get(l + 2) + " ");

                        List<String> cleanTP = myDedUtils.getCleanTP(currTP);
                        List<String> allEndpointsPorts = mapDTPToEndpsSrc.get(cleanTP);

                        hs = new HashSet();
                        hs.addAll(allEndpointsPorts);
                        allEndpointsPorts.clear();
                        allEndpointsPorts.addAll(hs);
                        Collections.sort(allEndpointsPorts);

                        List<String> allEndpointsNames = new LinkedList<>();

                        for (int t = 0; t < allEndpointsPorts.size(); t++) {

                            allEndpointsNames.add(mapEndpointToName.get(allEndpointsPorts.get(t)));

                        }

                        System.out.println("\t\t\t\t\t received at: " + allEndpointsNames);
                    }

                    List<List<String>> tmpPairHello = new LinkedList<>();
                    List<List<String>> tmpPairHello2 = new LinkedList<>();

                    for (int i = 0; i < allTPsInQuery.size(); i += 3) {
                        outerTP = new LinkedList<>();
                        outerTP.add(allTPsInQuery.get(i).get(0));
                        outerTP.add(allTPsInQuery.get(i).get(1));
                        outerTP.add(allTPsInQuery.get(i).get(2));

                        for (int j = i; j < allTPsInQuery.size(); j += 3) {
                            innerTP = new LinkedList<>();
                            innerTP.add(allTPsInQuery.get(i).get(0));
                            innerTP.add(allTPsInQuery.get(i).get(1));
                            innerTP.add(allTPsInQuery.get(i).get(2));

                            tmpPairHello.add(innerTP);
                            tmpPairHello.add(outerTP);
                            tmpPairHello2.add(outerTP);
                            tmpPairHello2.add(innerTP);

                            if (alreadySeenPair.get(tmpPairHello) == null) {

                                if (mapTruePositivePairs.get(tmpPairHello) != null) {
                                    truePositivesPairs++;
                                    cntEGTrPo++;
                                }

                                alreadySeenPair.put(tmpPairHello, 1);
                                alreadySeenPair.put(tmpPairHello2, 1);
                            } else {

                                alreadySeenPair.put(tmpPairHello, alreadySeenPair.get(tmpPairHello) + 1);
                                alreadySeenPair.put(tmpPairHello2, alreadySeenPair.get(tmpPairHello2) + 1);

                                if (alreadySeenPair.get(tmpPairHello) == 2) {

                                    truePositivesPairs--;
                                }
                            }

                        }
                    }

                }
            }

        }

        return count2;
    }

    /**
     * Check for a symhash (on common projected var or constant value) bewteen
     * two DTPs, if they are not already related with "alternativeMapping",
     * "exclusiveGroup" or "nested loop" relation
     *
     */
    public void notNullJoinComparison() {

        System.out.println();

        List<String> valueskeyInner = null;
        List<String> valueskeyOuter = null;
        List<String> valuesNestedLoopOuter = null;
        List<String> valuesNestedLoopInner = null;
        List<List<String>> tmp = null;
        List<List<String>> tmpPair2 = null;
        List<String> tmpInnerShort = new LinkedList<>();
        List<String> tmpOuterShort = new LinkedList<>();
        String outerVarClean = "";
        String innerVarClean = "";

        HashMap<List<List<String>>, Integer> alreadySeen = new HashMap<>();

        for (List<String> keyOuter : mapDTPtoAnswTotal.keySet()) {
            if ((keyOuter.get(0).contains("?chebiDrug")
                    && keyOuter.get(1).contains("<http://purl.org/dc/elements/1.1/title>")
                    && keyOuter.get(2).contains("?drugBankName"))) {
                int azeeaz = 0;

            }
            
            valueskeyOuter=mapDTPtoAnswTotal.get(keyOuter);
            if(valueskeyOuter!=null)
            myBasUtils.sortAndRemoveRedundancy(valueskeyOuter);
        }

        for (List<String> keyOuter : mapTPtoAnswersSourcesInverse.keySet()) {

            if ((keyOuter.get(0).contains("?chebiDrug")
                    && keyOuter.get(1).contains("<http://purl.org/dc/elements/1.1/title>")
                    && keyOuter.get(2).contains("?drugBankName"))) {
                int azeeaz = 0;

            }

             valueskeyOuter=mapTPtoAnswersSourcesInverse.get(keyOuter);
            if(valueskeyOuter!=null)
            myBasUtils.sortAndRemoveRedundancy(valueskeyOuter);
        }

        for (List<String> keyOuter : mapDTPtoAnswTotal.keySet()) {

            //BUUUUUUUUUG
            if (keyOuter.size() == 3) {
                continue;
            }

            valuesNestedLoopOuter = mapTPtoAnswersSourcesInverse.get(keyOuter);
            valueskeyOuter = mapDTPtoAnswTotal.get(keyOuter);
            tmpOuterShort = myDedUtils.getCleanTP(keyOuter);

            if (mapDTPofEGNested.get(tmpOuterShort) != null) {
                continue;
            }

            if (mapDTPtoCANCELofEG.get(tmpOuterShort) != null) {
                continue;
            }

            if (mapDTPtoJoinBGP.get(tmpOuterShort) == null) {

                mapDTPtoJoinBGP.put(tmpOuterShort, -1);
            }

            if ((keyOuter.get(0).contains("?Int")
                    && keyOuter.get(1).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1>")
                    && keyOuter.get(2).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00863>"))) {
                int azeeaz = 0;

                if (keyOuter.get(3).contains("Int")) {
                    int ara = 0;
                }
            }

            for (List<String> keyInner : mapDTPtoAnswTotal.keySet()) {

                //BUUUUUUUUUG
                if (keyInner.size() == 3) {
                    continue;
                }

                tmpInnerShort = myDedUtils.getCleanTP(keyInner);

                if (mapDTPtoCANCELofEG.get(tmpInnerShort) != null) {

                    continue;
                }
                if (mapDTPtoJoinBGP.get(tmpInnerShort) == null) {

                    mapDTPtoJoinBGP.put(tmpInnerShort, -1);
                }

                valuesNestedLoopInner = mapTPtoAnswersSourcesInverse.get(keyInner);

                if (keyOuter.get(0).equalsIgnoreCase(keyInner.get(0))
                        && keyOuter.get(1).equalsIgnoreCase(keyInner.get(1))
                        && keyOuter.get(2).equalsIgnoreCase(keyInner.get(2))) {
                    continue;
                }

                if ((keyInner.get(0).contains("?Int")
                        && keyInner.get(1).contains("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2>")
                        && keyInner.get(2).contains("?IntDrug"))) {
                    int azeeaz = 0;

                }

                if (keyOuter.size() == 4) {

                    outerVarClean = myDedUtils.getCleanFinalVar(keyOuter.get(3));
                } else {
                    outerVarClean = "noth1";
                }

                if (keyInner.size() == 4) {

                    innerVarClean = myDedUtils.getCleanFinalVar(keyInner.get(3));
                } else {
                    innerVarClean = "noth2";
                }
                if (mapDTPofEGNested.get(tmpInnerShort) != null) {
                    continue;
                }

                tmp = new LinkedList<>();
                tmp.add(tmpInnerShort);
                tmp.add(tmpOuterShort);

                tmpPair2 = new LinkedList<>();
                tmpPair2.add(tmpOuterShort);
                tmpPair2.add(tmpInnerShort);

                if (mapAlreadySeenJoin.get(tmp) != null) {

                    continue;
                }

                if (mapAlreadySeenJoin.get(tmpPair2) != null) {

                    continue;
                }

                if (outerVarClean.equalsIgnoreCase(innerVarClean)) {

                    if (mapPairExclGroup.get(tmpPair2) != null || mapPairExclGroup.get(tmp) != null) {

                        mapDTPpair.put(tmp, 1);
                        mapDTPpair.put(tmpPair2, 1);
                        continue;
                    }

                    if ((mapDTPtoAlternatives.get(tmpInnerShort) != null && (engineName.contains("FedX") && mapDTPtoAlternatives.get(tmpOuterShort) != null))) {

                        if (Objects.equals(mapDTPtoAlternatives.get(tmpInnerShort), mapDTPtoAlternatives.get(tmpOuterShort))) {

                            myDedUtils.updatePrecisonRecallInfo2(tmpOuterShort, tmpInnerShort);
                            mapDTPpair.put(tmp, 1);
                            mapDTPpair.put(tmpPair2, 1);
                            continue;
                        }
                    }

                    //or else its already treated
                    if (mapDTPtoJoinBGP.get(tmpInnerShort) != null && mapDTPtoJoinBGP.get(tmpOuterShort) != null) {
                        valueskeyInner = mapDTPtoAnswTotal.get(keyInner);

                        //  if (!(mapDTPtoJoinBGP.get(tmpInnerShort) != -1
                        //             && Objects.equals(mapDTPtoJoinBGP.get(tmpOuterShort), mapDTPtoJoinBGP.get(tmpInnerShort)))) 
                        {

                            if ((valuesNestedLoopOuter != null && mapDTPToStartTime.get(tmpInnerShort) <= mapDTPToStartTime.get(tmpOuterShort))) {

                                if (myDedUtils.compareListsForIntersection(valuesNestedLoopOuter, valueskeyInner).size() > 0) //  if ((valuesNestedLoopOuter.size() >= valueskeyInner.size() && !Collections.disjoint(valuesNestedLoopOuter, valueskeyInner)
                                //     || (valueskeyInner.size() >= valuesNestedLoopOuter.size() && !Collections.disjoint(valueskeyInner, valuesNestedLoopOuter))))   
                                {
                                    List<List<String>> tmp1 = new LinkedList<>();
                                    tmp1.add(tmpOuterShort);
                                    tmp1.add(tmpInnerShort);
                                    List<List<String>> tmp2 = new LinkedList<>();
                                    tmp2.add(tmpInnerShort);
                                    tmp2.add(tmpOuterShort);
                                    if (alreadySeen.get(tmp1) == null && alreadySeen.get(tmp2) == null) {

                                        if (mapDTPToFinishTime.get(tmpInnerShort) <= mapDTPToStartTime.get(tmpOuterShort)) {

                                            myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 0.65, "nestedLoop", false);

                                        } else {

                                            myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 1, "nestedLoop", false);

                                        }
                                        alreadySeen.put(tmp1, -1);
                                        alreadySeen.put(tmp2, -1);
                                    }
                                }

                            } else if ((valuesNestedLoopInner != null && mapDTPToStartTime.get(tmpOuterShort) <= mapDTPToStartTime.get(tmpOuterShort))) {

                                //  if ((valueskeyOuter.size() >= valuesNestedLoopInner.size() && !Collections.disjoint(valueskeyOuter, valuesNestedLoopInner))
                                //        || (valuesNestedLoopInner.size() >= valueskeyOuter.size() && !Collections.disjoint(valuesNestedLoopInner, valueskeyOuter)))
                                if (myDedUtils.compareListsForIntersection(valueskeyOuter, valuesNestedLoopInner).size() > 0) {
                                    List<List<String>> tmp1 = new LinkedList<>();
                                    tmp1.add(tmpOuterShort);
                                    tmp1.add(tmpInnerShort);
                                    List<List<String>> tmp2 = new LinkedList<>();
                                    tmp2.add(tmpInnerShort);
                                    tmp2.add(tmpOuterShort);
                                    if (alreadySeen.get(tmp1) == null && alreadySeen.get(tmp2) == null) {

                                        if (mapDTPToFinishTime.get(tmpOuterShort) <= mapDTPToStartTime.get(tmpInnerShort)) {
                                            myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 0.65, "nestedLoop", false);

                                        } else {

                                            myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 1, "nestedLoop", false);

                                        }

                                        alreadySeen.put(tmp1, -1);
                                        alreadySeen.put(tmp2, -1);
                                    }
                                }

                            } else {
                                // if (!Collections.disjoint(valueskeyOuter, valueskeyInner)) 
                                if (myDedUtils.compareListsForIntersection(valueskeyOuter, valueskeyInner).size() > 0) {
                                    List<List<String>> tmp1 = new LinkedList<>();
                                    tmp1.add(tmpOuterShort);
                                    tmp1.add(tmpInnerShort);
                                    List<List<String>> tmp2 = new LinkedList<>();
                                    tmp2.add(tmpInnerShort);
                                    tmp2.add(tmpOuterShort);
                                    if (alreadySeen.get(tmp1) == null && alreadySeen.get(tmp2) == null) {

                                        myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 0.6, "symhash", false);
                                        alreadySeen.put(tmp1, -1);
                                        alreadySeen.put(tmp2, -1);
                                    }
                                }
                            }

                        }

                    }

                } else if ((keyOuter.get(0).equalsIgnoreCase(keyInner.get(0)) && !keyOuter.get(0).contains("?"))
                        || (keyOuter.get(0).equalsIgnoreCase(keyInner.get(2)) && !keyOuter.get(0).contains("?"))
                        || (keyOuter.get(2).equalsIgnoreCase(keyInner.get(0)) && !keyOuter.get(2).contains("?"))
                        || (keyOuter.get(2).equalsIgnoreCase(keyInner.get(2)) && !keyOuter.get(2).contains("?"))) {

                    List<List<String>> tmp1 = new LinkedList<>();
                    tmp1.add(tmpOuterShort);
                    tmp1.add(tmpInnerShort);
                    List<List<String>> tmp2 = new LinkedList<>();
                    tmp2.add(tmpInnerShort);
                    tmp2.add(tmpOuterShort);
                    if (alreadySeen.get(tmp1) == null && alreadySeen.get(tmp2) == null) {

                        myDedUtils.pairJoinRelation(tmpOuterShort, tmpInnerShort, tmp, 0.6, "constant", false);
                        alreadySeen.put(tmp1, -1);
                        alreadySeen.put(tmp2, -1);
                    }
                }

            }

        }

    }

    public void showMissingPairs() throws IOException {

        int totalBGPs = 0;

        Map<List<List<String>>, Integer> mySeen = new HashMap<>();
        Map<List<List<String>>, Integer> mySeenClear = new HashMap<>();

        List<List<String>> tmpPair = new LinkedList<>();
        List<List<String>> tmpPair2 = new LinkedList<>();

        List<List<String>> pair = new LinkedList<>();
        List<String> outer = new LinkedList<>();
        List<String> inner = new LinkedList<>();

        if (!single) {
            if (collectionName.contains("CD")) {
                pair = new LinkedList<>();
                outer = new LinkedList<>();
                inner = new LinkedList<>();
                outer.add("?x");
                outer.add("<http://data.nytimes.com/elements/topicPage>");
                outer.add("?page");

                pair.add(outer);
                inner.add("?x");
                inner.add("<http://www.w3.org/2002/07/owl#sameAs>");
                inner.add("<http://dbpedia.org/resource/Barack_Obama>");
                pair.add(inner);
                mySeenClear.put(pair, 1);

                pair = new LinkedList<>();
                outer = new LinkedList<>();
                inner = new LinkedList<>();
                outer.add("?y");
                outer.add("<http://www.w3.org/2002/07/owl#sameAs>");
                outer.add("?location");

                pair.add(outer);
                inner.add("?y");
                inner.add("<http://data.nytimes.com/elements/topicPage>");
                inner.add("?news");
                pair.add(inner);
                mySeenClear.put(pair, 1);

                pair = new LinkedList<>();
                outer = new LinkedList<>();
                inner = new LinkedList<>();
                outer.add("?y");
                outer.add("<http://www.w3.org/2002/07/owl#sameAs>");
                outer.add("?x");
                pair.add(outer);
                inner.add("?y");
                inner.add("<http://data.nytimes.com/elements/topicPage>");
                inner.add("?news");
                pair.add(inner);
                mySeenClear.put(pair, 1);

            }

        }

        /* if(nameDB.contains("mydatabasefedxlsseq2")){
            
         pair = new LinkedList<>();
         outer = new LinkedList<>();
         inner = new LinkedList<>();
         outer.add("?chebiDrug");
         outer.add("<http://purl.org/dc/elements/1.1/title>");
         outer.add("?drugBankName");
         pair.add(outer);
         inner.add("?chebiDrug");
         inner.add("<http://bio2rdf.org/ns/bio2rdf#image>");
         inner.add("?chebiImage");
         pair.add(inner);
         mapTruePositivePairs.put(pair, 1);
                
                
         pair = new LinkedList<>();
         outer = new LinkedList<>();
         inner = new LinkedList<>();
         outer.add("?drug");
         outer.add("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory>");
         outer.add("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics>");
         pair.add(outer);
         inner.add("?drug");
         inner.add("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId>");
         inner.add("?cpd");
         pair.add(inner);
         mapTruePositivePairs.put(pair, 1);
         mySeen.put(pair, 1);
         pair = new LinkedList<>();
         outer = new LinkedList<>();
         inner = new LinkedList<>();
         outer.add("?drug");
         outer.add("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId>");
         outer.add("?keggDrug");
         pair.add(outer);
         inner.add("?keggDrug");
         inner.add("<http://bio2rdf.org/ns/bio2rdf#url>");
         inner.add("?keggUrl");
         pair.add(inner);
         mapTruePositivePairs.put(pair, 1);
         mySeen.put(pair, 1);
                
         }*/
        for (List<List<String>> keyOuter : mapGroundTruthPairs.keySet()) {

            tmpPair = new LinkedList<>();
            tmpPair.add(keyOuter.get(0));
            tmpPair.add(keyOuter.get(1));
            tmpPair2 = new LinkedList<>();
            tmpPair2.add(keyOuter.get(1));
            tmpPair2.add(keyOuter.get(0));
            if (mapTruePositivePairs.get(keyOuter) == null && mySeen.get(tmpPair) == null) {
                // 
                mySeen.put(tmpPair, 1);
                mySeen.put(tmpPair2, 1);
                mySeenClear.put(tmpPair, 1);

            }

        }
        showDeducedBGPs();

        totalBGPs = triplePatternsNotInDeducedBGP();
        System.out.println();

        if (!single) {
            System.out.println("****************************Missing Ground truth pairWise joins: *************************");

            int cnt = 0;

            for (List<List<String>> keyOuter : mySeenClear.keySet()) {

                cnt++;
                System.out.println("\t Pair join missed no[" + cnt + "]: " + keyOuter);

            }
        }

        System.out.println();
        myDedUtils.generateGNUFinal(windowJoin, totalBGPs + notnullJoinBGPsFinal.size());
    }

}
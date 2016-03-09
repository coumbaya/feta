package myfeta;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
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
    public static HashMap<Integer, List<String>> mapBGPtoPredicates;
    public static HashMap<List<List<String>>, Integer> mapAlreadySeenJoin;
    List<Integer> alreadySeenTPs;
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
        //and show the totality of deduced BGPs (i.e., deduced by FETA + subqueries not combined)
        showTotalBGPs();

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
        List<String> allEndpointsPorts = null;
        List<String> allEndpointsNames = null;

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

                currTP = new LinkedList<>(notnullJoinBGPsFinal.get(i).get(j).subList(0, 3));
                allEndpointsPorts = mapDTPToEndpsSrc.get(currTP);
                allEndpointsNames = new LinkedList<>();

                if (allEndpointsPorts != null) {

                    allEndpointsPorts = myBasUtils.sortAndRemoveRedundancy(allEndpointsPorts);

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
        List<String> cleanTP = new LinkedList<>();
        List<String> outerTP = new LinkedList<>();
        List<String> innerTP = new LinkedList<>();
        List<Integer> currSrcQuers = new LinkedList<>();
        List<String> currSrcEntities = new LinkedList<>();
        List<List<String>> allTPsInQuery = new LinkedList<>();
        List<String> allEndpointsPorts = new LinkedList<>();
        List<String> allEndpointsNames = new LinkedList<>();
        List<List<String>> currSrcJoin = new LinkedList<>();
        List<List<String>> currSrcJoin2 = new LinkedList<>();
        HashMap<Integer, List<Integer>> mapQueriesToDeducedTPs = new HashMap<>();
        HashMap<List<List<String>>, Integer> alreadySeenPair = new HashMap<>();

        //Cacth all source queries of DTPs that did not participated in any join
        for (List<String> key : mapDTPToDeducedID.keySet()) {

            List<String> keyShort = new LinkedList<>(key.subList(0, 3));

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

        //Identify, if there exists, joins of these source queries
        for (Integer key : mapQueriesToDeducedTPs.keySet()) {

            count = 0;
            count2++;
            currSrcQuers = myBasUtils.sortAndRemoveRedundancy2(mapQueriesToDeducedTPs.get(key));

            for (int k = 0; k < currSrcQuers.size(); k++) {

                currSrcEntities = mapLogClQueryToAllTPEnts.get(currSrcQuers.get(k));

                if (queries.get(currSrcQuers.get(k)).contains("UNION") || queries.get(currSrcQuers.get(k)).contains("union")) {

                    for (int l = 0; l < currSrcEntities.size(); l += 3) {

                        count2++;

                        System.out.println("\t\t\t Graph no [" + count2 + "] " + "size : " + 1 + " because of DTP out of timestamp Tjoin of LogClean query no"
                                + currSrcQuers + " corresponding to LogAnswer entries: " + mapLogClQueryToAnsEntry.get(currSrcQuers.get(k)));

                        count++;
                        currTP = new LinkedList<>(currSrcEntities.subList(l, l+3));
                        myDedUtils.updatePrecisionRecalTPinfo(currTP);

                        System.out.println("\t\t\t\t TP no [1" + "] " + mapLogClQueryToAllTPEnts.get(currSrcQuers.get(k)).get(l + 0) + " "
                                + mapLogClQueryToAllTPEnts.get(currSrcQuers.get(k)).get(l + 1) + " " + mapLogClQueryToAllTPEnts.get(currSrcQuers.get(k)).get(l + 2) + " ");

                        allEndpointsPorts = mapDTPToEndpsSrc.get(currTP);
                        if (allEndpointsPorts != null) {

                            allEndpointsPorts = myBasUtils.sortAndRemoveRedundancy(allEndpointsPorts);
                            allEndpointsNames = new LinkedList<>();

                            for (int t = 0; t < allEndpointsPorts.size(); t++) {

                                allEndpointsNames.add(mapEndpointToName.get(allEndpointsPorts.get(t)));

                            }

                            System.out.println("\t\t\t\t\t received at: " + allEndpointsNames);
                        }
                    }
                } else {
                    System.out.println("\t\t\t Graph no [" + count2 + "] " + "size : " + 1 + " because of DTP out of timestamp Tjoin of LogClean query no"
                            + currSrcQuers + " corresponding to LogAnswer entries: " + mapLogClQueryToAnsEntry.get(currSrcQuers.get(k)));

                    allTPsInQuery = new LinkedList<>();
                    for (int l = 0; l < currSrcEntities.size(); l += 3) {

                        count++;
                        currTP = new LinkedList<>(currSrcEntities.subList(l, l+3));
                        myDedUtils.updatePrecisionRecalTPinfo(currTP);
                        allTPsInQuery.add(currTP);
                        System.out.println("\t\t\t\t TP no [" + count + "] " + currSrcEntities.get(l + 0) + " "
                                + currSrcEntities.get(l + 1) + " " + currSrcEntities.get(l + 2) + " ");

                        cleanTP = myDedUtils.getCleanTP(currTP);
                        allEndpointsPorts = mapDTPToEndpsSrc.get(cleanTP);

                        if (allEndpointsPorts != null) {

                            allEndpointsPorts = myBasUtils.sortAndRemoveRedundancy(allEndpointsPorts);
                            allEndpointsNames = new LinkedList<>();

                            for (int t = 0; t < allEndpointsPorts.size(); t++) {

                                allEndpointsNames.add(mapEndpointToName.get(allEndpointsPorts.get(t)));

                            }

                            System.out.println("\t\t\t\t\t received at: " + allEndpointsNames);
                        }
                    }

                    currSrcJoin = new LinkedList<>();
                    currSrcJoin2 = new LinkedList<>();

                    for (int i = 0; i < allTPsInQuery.size(); i += 3) {

                        outerTP = new LinkedList<>(allTPsInQuery.get(i).subList(0, 3));

                        for (int j = i; j < allTPsInQuery.size(); j += 3) {

                            innerTP = new LinkedList<>(allTPsInQuery.get(j).subList(0, 3));

                            currSrcJoin.add(innerTP);
                            currSrcJoin.add(outerTP);
                            currSrcJoin2.add(outerTP);
                            currSrcJoin2.add(innerTP);

                            if (alreadySeenPair.get(currSrcJoin) == null) {

                                if (mapTruePositivePairs.get(currSrcJoin) != null) {
                                    truePositivesPairs++;
                                    cntEGTrPo++;
                                }

                                alreadySeenPair.put(currSrcJoin, 1);
                                alreadySeenPair.put(currSrcJoin2, 1);
                            } else {

                                alreadySeenPair.put(currSrcJoin, alreadySeenPair.get(currSrcJoin) + 1);
                                alreadySeenPair.put(currSrcJoin2, alreadySeenPair.get(currSrcJoin2) + 1);

                                if (alreadySeenPair.get(currSrcJoin) == 2) {

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

            valueskeyOuter = mapDTPtoAnswTotal.get(keyOuter);
            if (valueskeyOuter != null) {
                myBasUtils.sortAndRemoveRedundancy(valueskeyOuter);
            }
        }

        for (List<String> keyOuter : mapTPtoAnswersSourcesInverse.keySet()) {

            valueskeyOuter = mapTPtoAnswersSourcesInverse.get(keyOuter);
            if (valueskeyOuter != null) {
                myBasUtils.sortAndRemoveRedundancy(valueskeyOuter);
            }
        }

        for (List<String> keyOuter : mapDTPtoAnswTotal.keySet()) {

            //BUUUUUUUUUG
            if (keyOuter.size() == 3) {
                continue;
            }

            valuesNestedLoopOuter = mapTPtoAnswersSourcesInverse.get(keyOuter);
            valueskeyOuter = mapDTPtoAnswTotal.get(keyOuter);
            tmpOuterShort = myDedUtils.getCleanTP(keyOuter);

            if (mapDTPofEGNested.get(tmpOuterShort) != null || mapDTPtoCANCELofEG.get(tmpOuterShort) != null) {
                continue;
            }

            //BUUUUUUUUUG
            if (mapDTPtoJoinBGP.get(tmpOuterShort) == null) {

                mapDTPtoJoinBGP.put(tmpOuterShort, -1);
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

                //Better
                if (keyOuter.get(0).equalsIgnoreCase(keyInner.get(0))
                        && keyOuter.get(1).equalsIgnoreCase(keyInner.get(1))
                        && keyOuter.get(2).equalsIgnoreCase(keyInner.get(2))) {
                    continue;
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

                        if ((valuesNestedLoopOuter != null && mapDTPToStartTime.get(tmpInnerShort) <= mapDTPToStartTime.get(tmpOuterShort))) {

                            if (myDedUtils.compareListsForIntersection(valuesNestedLoopOuter, valueskeyInner).size() > 0) {
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

    public void showTotalBGPs() throws IOException {

        int totalBGPs = 0;
        Map<List<List<String>>, Integer> mySeen = new HashMap<>();
        Map<List<List<String>>, Integer> mySeenClear = new HashMap<>();
        List<List<String>> tmpPair = new LinkedList<>();
        List<List<String>> tmpPair2 = new LinkedList<>();

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
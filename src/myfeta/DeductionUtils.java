package myfeta;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static myfeta.Deduction.cntCONSTJOINTotal;
import static myfeta.Deduction.cntCONSTJOINTrPo;
import static myfeta.Deduction.cntEGTrPo;
import static myfeta.Deduction.cntNESLOOPTotal;
import static myfeta.Deduction.cntNESLOOPTrPo;
import static myfeta.Deduction.cntSYMHASHTotal;
import static myfeta.Deduction.cntSYMHASHTrPo;
import static myfeta.Deduction.groundTruthPairs;
import static myfeta.Deduction.groundTruthTPs;
import static myfeta.Deduction.mapGroundTruthPairs;
import static myfeta.Deduction.mapGroundTruthTPs;
import static myfeta.Deduction.mapLogClQueryToTimestamp;
import static myfeta.Deduction.mapObservedTPs;
import static myfeta.Deduction.mapTruePositivePairs;
import static myfeta.Deduction.totalPairs;
import static myfeta.Deduction.totalTPs;
import static myfeta.Deduction.truePositivesPairs;
import static myfeta.Deduction.truePositivesTPs;
import static myfeta.DeductionLogClean.mapCTPtoFILTERUNION;
import static myfeta.DeductionNestedLoop.mapCTPIDtoQuerySrc;
import static myfeta.DeductionNestedLoop.mapCTPToEndpsSrc;
import static myfeta.DeductionNestedLoop.mapCTPToFinishTime;
import static myfeta.DeductionNestedLoop.mapCTPToStartTime;
import static myfeta.DeductionNestedLoop.mapCTPtoConstants;
import static myfeta.DeductionNestedLoop.mapCTPtoDedGraph;
import static myfeta.DeductionNestedLoop.mapCTPtoQuerySrc;
import static myfeta.DeductionNestedLoop.mapDTPToAnyJoin;
import static myfeta.DeductionNestedLoop.mapDTPToDeducedID;
import static myfeta.DeductionNestedLoop.mapDTPToEndpsSrc;
import static myfeta.DeductionNestedLoop.mapDTPToFinishTime;
import static myfeta.DeductionNestedLoop.mapDTPToStartTime;
import static myfeta.DeductionNestedLoop.mapDTPToTimeDeduction;
import static myfeta.DeductionNestedLoop.mapDTPpair;
import static myfeta.DeductionNestedLoop.mapDTPtoAnswTotal;
import static myfeta.DeductionNestedLoop.mapDTPtoConceptBGP;
import static myfeta.DeductionNestedLoop.mapDTPtoExclGroup;
import static myfeta.DeductionNestedLoop.mapDTPtoInnerQuery;
import static myfeta.DeductionNestedLoop.mapDTPtoJoinBGP;
import static myfeta.DeductionNestedLoop.mapJOINBGPtoConfidence;
import static myfeta.DeductionNestedLoop.mapPairTPsToTypeJoin;
import static myfeta.DeductionNestedLoop.notnullJoinBGPs;
import static myfeta.DeductionNestedLoop.sameConceptBGPs;
import static myfeta.DeductionNestedLoop.setDTPbasedConcepts;
import static myfeta.DeductionNotNullJoin.mapAlreadySeenJoin;
import static myfeta.Main.collectionName;
import static myfeta.Main.engineName;
import static myfeta.Main.single;
import static myfeta.Main.windowJoin;

/**
 * This class implementes help/complementary functions for deduction algorithm
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class DeductionUtils {

    BasicUtilis myBasUtils;

    public static List<List<String>> DTPCandidates;
    public static List<List<List<String>>> TPwithAnswerIntersection;
    public static List<List<List<String>>> TPwithNoAnswerIntersection;
    public static List<List<List<String>>> setDTPbasedAnswers;
    public static List<Integer> listBGPsremoveAnswer;
    public static List<Integer> listBGPsremoveConcept;

    private List<String> chainEntities;
    //boolean flagFound = false;

    public DeductionUtils() {

        DTPCandidates = new LinkedList<>();
        TPwithAnswerIntersection = new LinkedList<>();
        TPwithNoAnswerIntersection = new LinkedList<>();
        setDTPbasedAnswers = new LinkedList<>();
        listBGPsremoveAnswer = new LinkedList<>();
        listBGPsremoveConcept = new LinkedList<>();

        myBasUtils = new BasicUtilis();
    }

    /**
     * Get triple pattern's variables
     *
     * @param triplePat
     * @return
     */
    public List<String> getTPVariables(List<String> triplePat) {

        List<String> vars = new LinkedList<>();

        for (int i = 0; i < triplePat.size(); i++) {

            if (triplePat.get(i).contains("?")) {

                vars.add(triplePat.get(i));
            }
        }

        return vars;
    }

    /**
     * Get triple pattern's constants (IRI or Literals)
     *
     * @param triplePat
     * @return
     */
    public String getTPConstants(List<String> triplePat) {

        String constantValue = "";

        for (int i = 0; i < triplePat.size(); i++) {
            if (!triplePat.get(0).contains("?")) {

                if (triplePat.get(i).contains("\'")) {

                    constantValue = triplePat.get(i).substring(1, triplePat.get(i).length() - 1);
                } else {

                    constantValue = triplePat.get(i);
                }

            }

        }

        return constantValue;
    }

    /**
     * Get a new triple pattern in a specific clean format. This function is
     * used, as constants in answers are not captured in adequate format (i.e.,
     * a Literal is not in double quotes and a IRI is not included in "<" and
     * ">")
     *
     * @param rawTP original tp, not respecting the adequate format
     * @return
     */
    public List<String> getCleanTP(List<String> rawTP) {

        List<String> triplePat = new LinkedList<>();
        String tmpLiteral = "";

        for (int i = 0; i < 3; i++) {

            if (i == 1) {

                triplePat.add(rawTP.get(i));
            } else {

                if (rawTP.get(i).contains("\"") && !rawTP.get(i).contains("http") && !rawTP.get(i).contains("?")) {

                    tmpLiteral = rawTP.get(i).substring(1, rawTP.get(i).length() - 1);
                    triplePat.add("\'" + tmpLiteral + "\'");

                } else if (rawTP.get(i).contains("http") && !rawTP.get(i).contains("<") && !rawTP.get(i).contains(">")) {
                    triplePat.add("<" + rawTP.get(i) + ">");
                } else {
                    triplePat.add(rawTP.get(i));
                }

            }
        }

        return triplePat;
    }

    /**
     * Get a triple pattern instance of a CTP, with a specific value for it
     * constants, as a CTP is associated to many constant values (i.e., IRIs or
     * Literals)
     *
     * @param candidateTP from which we will get a triple pattern instance
     * @param constVal new IRI/Literal to be set as CTP constant value
     * @return
     */
    public List<String> getInstanceCTP(List<String> candidateTP, String constVal) {

        List<String> newTP = new LinkedList<>();

        for (int i = 0; i < 3; i++) {

            if ((i == 0 || i == 2) && (!candidateTP.get(i).contains("?"))) {
                
                newTP.add(constVal);
            } else {
                
                newTP.add(candidateTP.get(i));
            }
        }

        return newTP;
    }

    /**
     * Get a new triple pattern, with a specific value for constant, when a CTP
     * is associated to many constant values (i.e., IRIs or Literals)
     *
     * @param candidateTP from which we will get a triple pattern instance
     * @param value new IRI/Literal to be set as CTP constant value
     * @return
     */
    public List<String> getNewRawTP(List<String> candidateTP, String value) {

        List<String> newTP = new LinkedList<>();

        for (int i = 0; i < 3; i++) {

            if ((i == 0 || i == 2) && (!candidateTP.get(i).contains("?"))) {

                if (value.contains("?")) {

                    newTP.add(value);
                } else if (!value.contains("http") && !value.contains("?")) {

                    if (!value.contains("\'")) {

                        newTP.add("\'" + value + "\'");
                    } else {
                        newTP.add(value);
                    }

                } else if (value.contains("http") && !value.contains("?")) {
                    if (!value.contains("<") && !value.contains(">")) {

                        newTP.add("<" + value + ">");
                    } else {
                        newTP.add(value);
                    }
                }
            } else {
                newTP.add(candidateTP.get(i));
            }
        }

        return newTP;
    }

    /**
     * Get all constants (i.e., IRIs/Literals) of a CTP, if there exist in
     * object or subjects, that will be used as input in "SearchInverseMapping"
     *
     * @param inxCTP index of candidate TP
     * @return
     */
    public List<String> getValuesFromCTP(int inxCTP) {

        List<String> allCandidateTPVals = new LinkedList<>();
        List<String> DTPkey = new LinkedList<>();

        for (int i = 0; i < 4; i++) {

            DTPkey.add(DTPCandidates.get(inxCTP).get(i));
        }

        allCandidateTPVals = mapCTPtoConstants.get(DTPkey);

        return allCandidateTPVals;
    }

    /**
     * Get clean candidate mapping variable from answer JSON string, by removing
     * the "_" part
     *
     * @param currAnswVariable
     * @return
     */
    public String getCleanCandVar(String currAnswVariable) {

        String candidateVar = "";

        if (currAnswVariable.contains("?")) {

            candidateVar = currAnswVariable.substring(currAnswVariable.indexOf("?") + 1);
        } else {

            candidateVar = currAnswVariable;
        }

        if (candidateVar.contains("_")) {

            candidateVar = candidateVar.substring(0, candidateVar.indexOf("_"));
        }

        return candidateVar;
    }

    /**
     * Get final candidate variable, to be identified for a DTP
     *
     * @param currCandVar
     * @return
     */
    public String getCleanFinalVar(String currCandVar) {

        String mapVariable = "";

        if (!currCandVar.contains("?")) {

            mapVariable = "?" + currCandVar;
        } else {

            mapVariable = currCandVar;
        }

        if (mapVariable.contains("_")) {

            mapVariable = mapVariable.substring(0, mapVariable.indexOf("_"));
        }

        return mapVariable;
    }

    /**
     * Get list of exact matching CTP (i.e., subject, predicate, object). If it
     * is equal to "null", then it is the first time we identify this new
     * candidate pattern. This method is used when we have distinguish identical
     * candidate triple patterns (i.e., same subject, predicate, object) when
     * these are identified in relative distant timestamps
     *
     * @param myList list of triple patterns
     * @param subject tp's subject
     * @param predicate tp's predicate
     * @param object tp's object
     * @return
     */
    public List<Integer> getIdemCTPs(List<List<String>> myList, String subject, String predicate, String object) {

        List<Integer> idPatrns = new LinkedList<>();
        int idPat = -1;

        for (List<String> listStr : myList) {

            idPat++;

            for (int i = 0; i < 3; i++) {

                if (listStr.get(i).contains(subject) && listStr.get(i + 1).equalsIgnoreCase(predicate) && listStr.get(i + 2).contains(object)) {

                    idPatrns.add(idPat);
                }

            }

        }

        return idPatrns;
    }

    /**
     * Get list of derived matching candidate triple pattern (i.e., same subject
     * and predicate or same predicate and subject
     *
     * @param myList list of triple patterns
     * @param element1 first tp's entity
     * @param idELement1 first tp's position (subject, predicate or object)
     * @param element2 second tp's entity
     * @param idELement2 second tp's position (subject, predicate or object)
     * @return
     */
    public List<Integer> getDerivedCTPs(List<List<String>> myList, String element1,
            Integer idELement1, String element2, Integer idELement2) {

        int cpt = 0;
        List<Integer> idPatrns = new LinkedList<>();
        int idPat = -1;
        boolean flagElement1 = false, flagElement2 = false;

        for (List<String> listStr : myList) {

            idPat++;
            cpt = 0;

            if (!(listStr.get(0).contains("?") && listStr.get(2).contains("?"))) {
                for (int i = 0; i < 3; i++) {
                    if (listStr.get(i).equalsIgnoreCase(element1) && !flagElement1 && (idELement1 == i)) {

                        cpt++;
                    }
                    if (listStr.get(i).equalsIgnoreCase(element2) && !flagElement2 && (idELement2 == i)) {

                        cpt++;
                    }

                    // we do not concider a candidate TP with two variables, as match
                    if (cpt == 2) {

                        if (!myBasUtils.elemInListEquals(idPatrns, idPat)) {

                            idPatrns.add(idPat);
                        }

                    }

                }
            }
        }

        return idPatrns;
    }

    /**
     * Set new info for a new exclusive group
     *
     * @param outerDTP
     * @param innerDTP
     * @param newEGpair
     * @param curEGpair
     */
    public void setNewEGInfo(List<String> outerDTP, List<String> innerDTP, List<List<String>> newEGpair, int curEGpair) {

        outerDTP = getCleanTP(outerDTP);
        innerDTP = getCleanTP(innerDTP);

        if (mapDTPToTimeDeduction.get(outerDTP) == null) {

            mapDTPToTimeDeduction.put(outerDTP, mapDTPToTimeDeduction.size());
        }

        if (mapDTPToTimeDeduction.get(innerDTP) == null) {

            mapDTPToTimeDeduction.put(innerDTP, mapDTPToTimeDeduction.size());
        }

        System.out.println("\t-------------------------------EXCLUSIVE GROUP [no " + curEGpair + "]----------------------------------------------");
        System.out.println("\t\t[Outer TP] " + outerDTP.get(0) + " " + outerDTP.get(1) + " " + outerDTP.get(2));
        System.out.println("\t\t[Inner TP] " + innerDTP.get(0) + " " + innerDTP.get(1) + " " + innerDTP.get(2));
        System.out.println();

        updatePrecisonRecallInfo(outerDTP, innerDTP);

        myBasUtils.insertToMap4(mapDTPtoAnswTotal, outerDTP, new LinkedList<String>());
        myBasUtils.insertToMap4(mapDTPtoAnswTotal, innerDTP, new LinkedList<String>());

        mapPairTPsToTypeJoin.put(newEGpair, "exclusiveGroup");
        addPairTPinNotNullJoinBGP(newEGpair, (float) 1.0);
    }

    /**
     * Set hash map info about a new candidate triple pattern CTP
     *
     * @param triplePat
     * @param constantVal
     * @param indxLogCleanQuery
     * @param indxLogQueryDedGraph
     * @param strDedQueryId
     * @param lastExistingTPIndex
     */
    public void setNewCTPInfo(List<String> triplePat, String constantVal,
            int indxLogCleanQuery, int indxLogQueryDedGraph, String strDedQueryId, String lastExistingTPIndex) {

        DTPCandidates.add(new LinkedList<String>());

        if (!triplePat.get(0).contains("?")) {
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(0));
        } else {

            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(0) + lastExistingTPIndex);
        }
        DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(1));
        if (!triplePat.get(2).contains("?")) {
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(2));

        } else {
            DTPCandidates.get(DTPCandidates.size() - 1).add(triplePat.get(2) + lastExistingTPIndex);

        }

        DTPCandidates.get(DTPCandidates.size() - 1).add(strDedQueryId);
        myBasUtils.insertToMap4(mapCTPtoConstants, DTPCandidates.get(DTPCandidates.size() - 1), constantVal);
        myBasUtils.insertToMap(mapCTPtoDedGraph, indxLogQueryDedGraph, DTPCandidates.size() - 1);
        mapCTPToFinishTime.put(DTPCandidates.size() - 1, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        mapCTPToStartTime.put(DTPCandidates.size() - 1, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        myBasUtils.insertToMap(mapCTPtoQuerySrc, triplePat, indxLogCleanQuery);
    }

    /**
     * Update hash map info about a new candidate triple pattern CTP
     *
     * @param triplePat
     * @param constantVal
     * @param indxLogCleanQuery
     * @param indxLogQueryDedGraph
     * @param candidateTP
     */
    public void updateCTPInfo(List<String> triplePat, String constantVal, int indxLogCleanQuery, int indxLogQueryDedGraph, int candidateTP) {

        myBasUtils.insertToMap4(mapCTPtoConstants, DTPCandidates.get(candidateTP), constantVal);
        mapCTPToFinishTime.put(candidateTP, mapLogClQueryToTimestamp.get(indxLogCleanQuery));
        myBasUtils.insertToMap(mapCTPtoQuerySrc, triplePat, indxLogCleanQuery);
    }

    /**
     * Set hash map info about a new deduced triple pattern DTP
     *
     * @param triplePattern
     * @param CTPindx
     */
    public void setDTPHashInfo(List<String> triplePattern, int CTPindx) {

        List<String> endpoints = null;
        int currDTPsize = mapDTPToDeducedID.size();
        List<String> originalTP = getCleanTP(DTPCandidates.get(CTPindx));

        if (mapDTPtoExclGroup.get(triplePattern) == null) {

            mapDTPToDeducedID.put(triplePattern, currDTPsize);
            mapDTPToAnyJoin.put(currDTPsize, -1);
        }

        if (mapDTPToTimeDeduction.get(triplePattern) == null) {

            mapDTPToTimeDeduction.put(triplePattern, mapDTPToTimeDeduction.size());
        }

        endpoints = mapCTPToEndpsSrc.get(originalTP);
        mapDTPtoInnerQuery.put(mapDTPToDeducedID.get(triplePattern), mapCTPIDtoQuerySrc.get(CTPindx));
        if(endpoints==null){
            endpoints=new LinkedList<>();
            endpoints.add("8700");
        }
        myBasUtils.insertToMap3(mapDTPToEndpsSrc, endpoints, triplePattern);
        mapDTPToFinishTime.put(triplePattern, myBasUtils.getTimeInSec(mapCTPToFinishTime.get(CTPindx)));
        mapDTPToStartTime.put(triplePattern, myBasUtils.getTimeInSec(mapCTPToStartTime.get(CTPindx)));
    }

    /**
     * Compare a CTP constant values and answers for common values
     *
     * @param tmpListValues
     * @param allCandTPvalues
     * @return
     */
    public List<String> compareListsForIntersection(List<String> tmpListValues, List<String> allCandTPvalues) {

        List<String> candidateValuesMatched = new LinkedList<>();
        List<String> matchedValuesOuter = new LinkedList<>();
        List<String> matchedValuesInner = new LinkedList<>();

        if (tmpListValues.size() >= allCandTPvalues.size()) {
            matchedValuesOuter = myBasUtils.refineList(tmpListValues);
            matchedValuesInner = myBasUtils.refineList(allCandTPvalues);

        } else if (tmpListValues.size() < allCandTPvalues.size()) {

            matchedValuesOuter = myBasUtils.refineList(allCandTPvalues);
            matchedValuesInner = myBasUtils.refineList(tmpListValues);
        }

        // matchedValuesOuter = myBasUtils.cloneListElems(tmpListValues);
        //matchedValuesInner = myBasUtils.cloneListElems(allCTPconsts);
        matchedValuesOuter.retainAll(matchedValuesInner);
        // candidateValuesMatched = myBasUtils.listIntersection(matchedValuesInner, matchedValuesOuter);    
        //  return candidateValuesMatched;
        return matchedValuesOuter;
    }

    /**
     * Get list of TPs that are pairwise joined to a specific triple pattern
     *
     * @param pairJoinList
     * @param triplePattern
     * @return
     */
    public List<List<String>> getListJoinableTPs(List<List<List<String>>> pairJoinList, List<String> triplePattern) {

        List<List<String>> matchedList = new LinkedList<>();
        List<List<List<String>>> toRemove = new LinkedList<>();

        for (List<List<String>> list : pairJoinList) {

            if ((list.get(0).get(0).equals(triplePattern.get(0))
                    && list.get(0).get(1).equals(triplePattern.get(1))
                    && list.get(0).get(2).equals(triplePattern.get(2)))) {

                matchedList.add(list.get(1));
                toRemove.add(list);

            } else if ((list.get(1).get(0).equals(triplePattern.get(0))
                    && list.get(1).get(1).equals(triplePattern.get(1))
                    && list.get(1).get(2).equals(triplePattern.get(2)))) {

                matchedList.add(list.get(0));
                toRemove.add(list);

            }

        }

        return matchedList;
    }

    /**
     * Add a new triple pattern in the same BGP ("NotNullJoin" or "SameConcept")
     *
     * @param triplePattern
     * @param indxBGP
     * @param confidence
     * @param heuristic
     */
    public void addTPinBGP(List<String> triplePattern, int indxBGP, float confidence, String heuristic) {

        float currentConf = -1;

        if (heuristic.equalsIgnoreCase("concept")) {

            sameConceptBGPs.get(indxBGP).add(triplePattern);
            mapDTPtoConceptBGP.put(triplePattern, sameConceptBGPs.size() - 1);
            //mapDeducedTPtoSameConceptBGP.put(triplePattern, indxBGP);  
        } else {

            notnullJoinBGPs.get(indxBGP).add(triplePattern);
            mapDTPtoJoinBGP.put(triplePattern, indxBGP);
            currentConf = mapJOINBGPtoConfidence.get(indxBGP);
            mapJOINBGPtoConfidence.put(indxBGP, currentConf * confidence);
        }
    }

    /**
     * Set info to corresponding maps concerning "NotNullJoin" or "SameConcept",
     * for a specific triple pattern
     *
     * @param triplePat
     * @param heuristic
     */
    public void setMapsNotNullJoin(List<String> triplePat, String heuristic) {

        if (heuristic.equalsIgnoreCase("concept")) {

            if (mapDTPtoConceptBGP.get(triplePat) == null) {

                mapDTPtoConceptBGP.put(triplePat, -1);
            }

        } else {

            if (mapDTPtoJoinBGP.get(triplePat) == null) {

                mapDTPtoJoinBGP.put(triplePat, -1);
            }

            if (mapDTPToDeducedID.get(triplePat) != null) {
                if (mapDTPToAnyJoin.get(mapDTPToDeducedID.get(triplePat)) == -1) {

                    mapDTPToAnyJoin.put(mapDTPToDeducedID.get(triplePat), 1);
                }
            }

        }

    }

    /**
     * For each pair of triple patterns, we add the one which not belong to a
     * BGP (i.e., srcTP) to the BGP graph, conercning "NotNullJoin" or
     * "SameConcept", of the other (i.e., destTP)
     *
     * @param destTP
     * @param srcTP
     * @param confidence
     * @param heuristic
     */
    public void associateTPsInBGP(List<String> destTP, List<String> srcTP, float confidence, String heuristic) {

        List<List<String>> tmpGraph = new LinkedList<>();
        int tmpIndxMerge = -1;

        if (heuristic.equalsIgnoreCase("concept")) {

            addTPinBGP(srcTP, mapDTPtoConceptBGP.get(destTP), -1, "concept");

            tmpGraph = getListJoinableTPs(setDTPbasedConcepts, destTP);
            tmpIndxMerge = mapDTPtoConceptBGP.get(destTP);
            for (int i = 0; i < tmpGraph.size(); i++) {
                if (!myBasUtils.listInListContain(sameConceptBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {
                    addTPinBGP(tmpGraph.get(i), mapDTPtoConceptBGP.get(destTP), -1, "concept");

                }

            }
        } else {
            addTPinBGP(srcTP, mapDTPtoJoinBGP.get(destTP), confidence, "");

            tmpGraph = getListJoinableTPs(setDTPbasedAnswers, destTP);
            tmpIndxMerge = mapDTPtoJoinBGP.get(destTP);

            for (int i = 0; i < tmpGraph.size(); i++) {
                if (!myBasUtils.listInListContain(notnullJoinBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {

                    addTPinBGP(tmpGraph.get(i), mapDTPtoJoinBGP.get(destTP), confidence, "");
                }

            }
        }
    }

    /**
     * Combine content of two BGPs,, conercning "NotNullJoin" or "SameConcept",
     * in the case which two respective triple patterns are joined
     *
     * @param innerTP
     * @param outerTP
     * @param confidence
     * @param heuristic
     */
    public void fetchAndAddTPsInBGP(List<String> innerTP, List<String> outerTP, float confidence, String heuristic) {

        List<List<String>> tmpGraph = new LinkedList<>();
        int tmpIndxRemove = -1;
        int tmpIndxMerge = -1;
        float tmpConfidene = -1;

        if (heuristic.equalsIgnoreCase("concept")) {
            tmpGraph = sameConceptBGPs.get(mapDTPtoConceptBGP.get(innerTP));
            tmpIndxRemove = mapDTPtoConceptBGP.get(innerTP);
            tmpIndxMerge = mapDTPtoConceptBGP.get(outerTP);

            for (int i = 0; i < tmpGraph.size(); i++) {
                if (!myBasUtils.listInListContain(sameConceptBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {
                    addTPinBGP(tmpGraph.get(i), mapDTPtoConceptBGP.get(outerTP), -1, "concept");
                }

            }

            if (!myBasUtils.elemInListEquals(listBGPsremoveConcept, tmpIndxRemove)) {

                listBGPsremoveConcept.add(tmpIndxRemove);
            }

        } else {
            tmpGraph = notnullJoinBGPs.get(mapDTPtoJoinBGP.get(innerTP));
            tmpConfidene = mapJOINBGPtoConfidence.get(mapDTPtoJoinBGP.get(innerTP));
            tmpIndxRemove = mapDTPtoJoinBGP.get(innerTP);
            tmpIndxMerge = mapDTPtoJoinBGP.get(outerTP);

            for (int i = 0; i < tmpGraph.size(); i++) {
                if (!myBasUtils.listInListContain(notnullJoinBGPs.get(tmpIndxMerge), tmpGraph.get(i))) {

                    addTPinBGP(tmpGraph.get(i), mapDTPtoJoinBGP.get(outerTP), confidence * tmpConfidene, "");
                }

            }

            if (!myBasUtils.elemInListEquals(listBGPsremoveAnswer, tmpIndxRemove)) {

                listBGPsremoveAnswer.add(tmpIndxRemove);
            }
        }
    }

    /**
     * Add a pair of joined TPs, to the corresponding "NotNullJoin" BGP
     *
     * @param pairTPs
     * @param confidence
     */
    public void addPairTPinNotNullJoinBGP(List<List<String>> pairTPs, float confidence) {

        List<String> innerTP = new LinkedList<>();
        List<String> outerTP = new LinkedList<>();
        List< List<String>> tmppairTPs = new LinkedList<>();

        for (int i = 0; i < 3; i++) {

            innerTP.add(pairTPs.get(0).get(i));
            outerTP.add(pairTPs.get(1).get(i));
        }

        tmppairTPs.add(outerTP);
        tmppairTPs.add(innerTP);

        setMapsNotNullJoin(outerTP, "");
        setMapsNotNullJoin(innerTP, "");

        if (notnullJoinBGPs.isEmpty() || (mapDTPtoJoinBGP.get(outerTP) == -1 && mapDTPtoJoinBGP.get(innerTP) == -1)) {

            setDTPbasedAnswers.add(tmppairTPs);
            notnullJoinBGPs.add(tmppairTPs);
            mapDTPtoJoinBGP.put(tmppairTPs.get(0), notnullJoinBGPs.size() - 1);
            mapDTPtoJoinBGP.put(tmppairTPs.get(1), notnullJoinBGPs.size() - 1);
            mapJOINBGPtoConfidence.put(notnullJoinBGPs.size() - 1, (float) confidence);
        } else {

            if (mapDTPtoJoinBGP.get(outerTP) == -1 && mapDTPtoJoinBGP.get(innerTP) != -1) {

                associateTPsInBGP(innerTP, outerTP, confidence, "");
            } else if (mapDTPtoJoinBGP.get(outerTP) != -1 && mapDTPtoJoinBGP.get(innerTP) == -1) {

                associateTPsInBGP(outerTP, innerTP, confidence, "");
            } else if (mapDTPtoJoinBGP.get(outerTP) != -1 && !Objects.equals(mapDTPtoJoinBGP.get(outerTP), mapDTPtoJoinBGP.get(innerTP))) {

                fetchAndAddTPsInBGP(innerTP, outerTP, confidence, "");
            }

        }
    }

    /**
     * Check if the current infered "symhash" relation, is covered by the window
     * gap (i.e., Tjoin). As it is a symmetric hash we do not care if DTP outer
     * preceeds DTP inner or vice versa
     *
     * @param keyOuter
     * @param keyInner
     * @param pair
     * @param confidence
     * @param relation
     * @param forcePair
     */
    public void pairJoinRelation(List<String> keyOuter, List<String> keyInner, List<List<String>> pair, double confidence, String relation, boolean forcePair) {

        int startOuter = 100000000;
        int finishOuter = 100000000;
        boolean flagTimeJoinable = false;
        int startInner = 100000000;
        int finishInner = 100000000;

        if (!forcePair) {
            // System.out.println(keyOuter);
            startOuter = mapDTPToStartTime.get(keyOuter);
            finishOuter = mapDTPToFinishTime.get(keyOuter);
            //     System.out.println(keyInner);

            startInner = mapDTPToStartTime.get(keyInner);
            finishInner = mapDTPToFinishTime.get(keyInner);

            if (((startInner - finishOuter <= windowJoin) && (startInner - finishOuter > 0)
                    || (startInner < finishOuter && (startInner - finishOuter > 0)))) {
                flagTimeJoinable = true;

            } else if (((finishOuter - startInner <= windowJoin) && (finishOuter - startInner > 0)
                    || (finishOuter < startInner && (finishOuter - startInner > 0)))) {

                flagTimeJoinable = true;

            } else if ((startInner >= startOuter) && (finishInner <= finishOuter)) {

                flagTimeJoinable = true;

            } else if ((startOuter >= startInner) && (finishOuter <= finishInner)) {

                flagTimeJoinable = true;
            } else if ((startOuter < startInner) && (finishOuter < finishInner) && finishOuter == startInner) {

                flagTimeJoinable = true;
            }
        }

        List<String> tmpOuterShort = new LinkedList();
        tmpOuterShort.add(keyOuter.get(0));
        tmpOuterShort.add(keyOuter.get(1));
        tmpOuterShort.add(keyOuter.get(2));

        List<String> tmpInnerShort = new LinkedList();
        tmpInnerShort.add(keyInner.get(0));
        tmpInnerShort.add(keyInner.get(1));
        tmpInnerShort.add(keyInner.get(2));

        if (flagTimeJoinable || forcePair) {

            totalPairs++;

            if (relation.contains("symhash")) {
                cntSYMHASHTotal++;
            } else if (relation.contains("constant")) {
                cntCONSTJOINTotal++;
            } else if (relation.contains("nestedLoop")) {
                cntNESLOOPTotal++;

            }

            List<List<String>> seenPair = new LinkedList<>();
            seenPair.add(tmpInnerShort);
            seenPair.add(tmpOuterShort);

            List<List<String>> seenPair2 = new LinkedList<>();

            seenPair2.add(tmpOuterShort);
            seenPair2.add(tmpInnerShort);

            mapAlreadySeenJoin.put(seenPair, 1);
            mapAlreadySeenJoin.put(seenPair2, 1);

            if (mapGroundTruthPairs.get(seenPair) != null && mapTruePositivePairs.get(seenPair) == null) {

                mapTruePositivePairs.put(seenPair, 1);
                mapTruePositivePairs.put(seenPair2, 1);
                truePositivesPairs++;

                if (relation.contains("symhash")) {
                    cntSYMHASHTrPo++;
                } else if (relation.contains("constant")) {
                    cntCONSTJOINTrPo++;

                } else if (relation.contains("nestedLoop")) {
                    cntNESLOOPTrPo++;
                }
                
            } else {

                // System.out.println("BUUUUUUUG: "+pair);
            }

            int currDTPsize = mapDTPToDeducedID.size();
            if (mapDTPToDeducedID.get(keyOuter) == null) {

                mapDTPToDeducedID.put(keyOuter, currDTPsize);
                mapDTPToAnyJoin.put(currDTPsize, 1);
            }
            currDTPsize = mapDTPToDeducedID.size();

            if (mapDTPToDeducedID.get(keyInner) == null) {

                mapDTPToDeducedID.put(keyInner, currDTPsize);
                mapDTPToAnyJoin.put(currDTPsize, 1);
            }

            System.out.println("\t\t\t================ BINGO deduced JOIN: " + relation + " ================");
            System.out.println("\t\t\t\t\t[Outer DTP] " + keyOuter.get(0) + " " + keyOuter.get(1) + " " + keyOuter.get(2) + ", in interval [" + startOuter + ", " + finishOuter + "]");
            System.out.println("\t\t\t\t\t[Inner DTP] " + keyInner.get(0) + " " + keyInner.get(1) + " " + keyInner.get(2) + ", in interval [" + startInner + ", " + finishInner + "]");

            System.out.println();
            addPairTPinNotNullJoinBGP(pair, (float) confidence);

            mapDTPpair.put(seenPair, 1);
            mapDTPpair.put(seenPair2, 1);

            mapPairTPsToTypeJoin.put(pair, relation);
            updatePrecisonRecallInfo(keyOuter, keyInner);
        } else {

            System.out.println("\t\t\t________________ MISSED deduced JOIN because of Tjoin: " + relation + " ________________");
            System.out.println("\t\t\t\t\t[Outer DTP] " + keyOuter.get(0) + " " + keyOuter.get(1) + " " + keyOuter.get(2) + ", in interval [" + startOuter + ", " + finishOuter + "]");
            System.out.println("\t\t\t\t\t[Inner DTP] " + keyInner.get(0) + " " + keyInner.get(1) + " " + keyInner.get(2) + ", in interval [" + startInner + ", " + finishInner + "]");

            System.out.println();
        }

    }
    
    
    public void setGroundTruthHashMaps() {

        Gson gson = new Gson();

        List<List<List<String>>> myHashMapToList = new LinkedList<>();

        for (List<String> key : mapDTPtoAnswTotal.keySet()) {
            List<List<String>> tmp = new LinkedList<>();
            tmp.add(key);
            tmp.add(mapDTPtoAnswTotal.get(key));
            myHashMapToList.add(tmp);
        }
        String jsonQueries = gson.toJson(myHashMapToList);

        try {
            try (
                    FileWriter writer = new FileWriter("mapAnswersOfDTPs.txt")) {
                writer.write(jsonQueries);
            }

        } catch (IOException e) {
        }

    }



    /**
     * Particular function for FedX type of queries. Match to particular var "?o"
     * values that are passed as FILTER options, for an inner bound subquery
     *
     * @param query inner bound subquery in String format
     * @param allTPs list of elements of the inner bound subquery
     */
    public void setCTPtoFILTERUNIONvals(String query, List<String> allTPs) {

        for (int i = 0; i < allTPs.size(); i += 3) {

            List<String> triplet = new LinkedList<>(allTPs.subList(i, i + 3));
            List<String> motif = new LinkedList<>();
            String value = "";

            if (allTPs.get(i).contains("?")) {

                motif.add(allTPs.get(i));
            }

            motif.add(allTPs.get(i + 1));

            if (allTPs.get(i + 2).contains("?")) {

                motif.add(allTPs.get(i + 2));
            }

            // get the motif value
            int index = query.indexOf(motif.get(0));

            query = query.substring(index, query.length());
            value = query.substring(query.indexOf("= ") + 2, query.indexOf(" )"));

            List<String> key = null;
            for (List<String> keyCTP : mapCTPtoFILTERUNION.keySet()) {

                if (myBasUtils.elemInListEquals(keyCTP, motif.get(0)) && myBasUtils.elemInListEquals(keyCTP, motif.get(1))) {

                    key = keyCTP;
                }
            }

            if (key == null) {

                myBasUtils.insertToMap(mapCTPtoFILTERUNION, value, triplet);
            } else if (!myBasUtils.elemInListEquals(mapCTPtoFILTERUNION.get(key), value)) {

                myBasUtils.insertToMap(mapCTPtoFILTERUNION, value, key);
            }

        }

    }
   
    
    
    public void updatePrecisionRecalTPinfo(List<String> currTP){
        
    if (mapGroundTruthTPs.get(currTP) != null) {
           if (mapObservedTPs.get(currTP) == null) {

               mapObservedTPs.put(currTP, 1);
               truePositivesTPs++;
           }

           totalTPs++;
       } else {
           if (mapObservedTPs.get(currTP) == null) {

               mapObservedTPs.put(currTP, 1);
           }

           totalTPs++;
       }

    }
    
        /**
     * Save in different files that will ploted with gnuplot, in the first
     * column the current user definefd window gap (i.e., Tjoin) and in the
     * second column
     *
     * (i) total number of deduced BGPs and also BGPs of queries corresponding
     * to DTPs not in any FETA's deduced BGP
     *
     * (ii) total number of pairWise joins between DTPs
     *
     * (iii) true positive pairwise joins, for user-defined "ground truth" of
     * pariwise JOINs
     *
     * (iv) precision pairwise joins, for user-defined "ground truth" of
     * pariwise JOINs
     *
     * (v)recall pairwise joins, for user-defined "ground truth" of pariwise
     * JOINs
     *
     * @param winJoin
     * @param numBGP
     * @throws IOException
     */
    public void generateGNUFinal(int winJoin, int numBGP) throws IOException {

        if (collectionName.contains("CD") && engineName.contains("ANAPSID")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size() + 2);
            //four not deduced correctly: already 2 missing and two not correct
            truePositivesTPs = truePositivesTPs - 1;
            cntCONSTJOINTrPo--;
            cntEGTrPo--;
            cntNESLOOPTrPo--;
            //thre pairs deduces but are false positives, as the 3 TPs are not the same with individual traces
            truePositivesPairs = truePositivesPairs - 3;
        }
        if (collectionName.contains("CD") && engineName.contains("FedX")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size() + 2);
            //four not deduced correctly: already 2 missing and two not correct
            truePositivesTPs = truePositivesTPs - 1;
            //thre pairs deduces but are false positives, as the 3 TPs are not the same with individual traces
            truePositivesPairs = truePositivesPairs - 3;
            cntCONSTJOINTrPo--;
            cntEGTrPo--;
            cntNESLOOPTrPo--;
        }

        if (collectionName.contains("LS") && engineName.contains("ANAPSID")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size());
            //four not deduced correctly: already 2 missing and two not correct
            // truePositivesTPs = truePositivesTPs;
            //thre pairs deduces but are false positives, as the 3 TPs are not the same with individual traces
            // truePositivesPairs = truePositivesPairs - 3;
        }

        if (collectionName.contains("LS") && engineName.contains("FedX")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size());
        }

        if (collectionName.contains("MT") && engineName.contains("ANAPSID")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size());
        }

        if (collectionName.contains("MT") && engineName.contains("FedX")) {

            // 2, counting the symmetric pair
            groundTruthPairs = (mapGroundTruthPairs.size()) / 2;
            //two tps are missing (one because of CD4, CD7 and another because of CD2, CD3)
            groundTruthTPs = (mapGroundTruthTPs.size());
        }

        if (!single) {
            System.out.println("****************************FETA statistics: *************************");

            if (cntCONSTJOINTrPo < 0) {
                cntCONSTJOINTrPo++;
            }

            if (cntEGTrPo < 0) {
                cntEGTrPo++;
            }

            if (cntNESLOOPTrPo < 0) {
                cntNESLOOPTrPo++;
            }

            if ((cntEGTrPo + cntNESLOOPTrPo + (cntSYMHASHTrPo + cntCONSTJOINTrPo)) > truePositivesPairs) {
                cntNESLOOPTrPo--;
            }

            System.out.println("\t [a] All different BGPs: " + numBGP + "\n");
            System.out.println("\t [b] All different pairs: " + totalPairs + "\n");
            System.out.println("\t \t true positives pairs: " + truePositivesPairs + "\n");
            System.out.println("\t \t \t  where [EG]:" + cntEGTrPo + ", [nested loop]: " + cntNESLOOPTrPo + ""
                    + " and [SYMHASH+constants]:" + (cntSYMHASHTrPo + cntCONSTJOINTrPo) + " TOTAL===" + (cntEGTrPo + cntNESLOOPTrPo + (cntSYMHASHTrPo + cntCONSTJOINTrPo)) + "\n");
            System.out.println("\t [c] All different TPs: " + totalTPs + "\n");
            System.out.println("\t \t true positives TPs: " + truePositivesTPs + "\n");

            float answer = ((float) truePositivesPairs) / totalPairs;
            String out = String.format("%.2f", answer);
            System.out.println("\t [1] Precision in deduced pairJoins: " + out + "\n");

            float answerEG = ((float) cntEGTrPo) / totalPairs;
            String outEG = String.format("%.2f", answerEG);
            System.out.println("\t \t i) concerning deduced EG pairJoins: " + outEG + "\n");

            float answerSYM = ((float) (cntSYMHASHTrPo + cntCONSTJOINTrPo)) / totalPairs;
            String outSYM = String.format("%.2f", answerSYM);
            System.out.println("\t \t ii) concerning deduced SYMHASH and CONSTANT pairJoins: " + outSYM + "\n");

            float answerNES = ((float) cntNESLOOPTrPo) / totalPairs;
            String outNES = String.format("%.2f", answerNES);
            System.out.println("\t \t ii) concerning deduced Nested Loop pairJoins: " + outNES + "\n");

            answer = ((float) truePositivesPairs) / groundTruthPairs;
            out = String.format("%.2f", answer);
            System.out.println("\t [2] Recall in deduced pairJoins: " + out + "\n");

            answerEG = ((float) cntEGTrPo) / groundTruthPairs;
            outEG = String.format("%.2f", answerEG);
            System.out.println("\t \t i) concerning deduced EG pairJoins: " + outEG + "\n");

            answerSYM = ((float) (cntSYMHASHTrPo + cntCONSTJOINTrPo)) / groundTruthPairs;
            outSYM = String.format("%.2f", answerSYM);
            System.out.println("\t \t ii) concerning deduced SYMHASH and CONSTANT pairJoins: " + outSYM + "\n");

            answerNES = ((float) cntNESLOOPTrPo) / groundTruthPairs;
            outNES = String.format("%.2f", answerNES);
            System.out.println("\t \t ii) concerning deduced Nested Loop pairJoins: " + outNES + "\n");

            answer = ((float) truePositivesTPs) / totalTPs;
            out = String.format("%.2f", answer);
            System.out.println("\t [3] Precision in deduced TPs: " + out + "\n");

            answer = ((float) truePositivesTPs) / groundTruthTPs;
            out = String.format("%.2f", answer);
            System.out.println("\t [4] Recall in deduced TPs: " + out + "\n");
            System.out.println("****************************FETA statistics: *************************");
        }

    }
    public void updatePrecisonRecallInfo(List<String> outerTP, List<String> innerTP) {

    }

    public void updatePrecisonRecallInfo2(List<String> outerTP, List<String> innerTP) {
        
    }

      
}
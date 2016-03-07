package myfeta;

import com.fourspaces.couchdb.Document;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static myfeta.Deduction.mapLogClQueryToTimeSecs;
import static myfeta.Deduction.mapLogClQueryToAllTPEnts;
import static myfeta.Deduction.mapLogClQueryToProjVars;
import static myfeta.DeductionLogClean.queries;
import static myfeta.Deduction.mapLogClQueryToDedGraph;
import static myfeta.Main.windowJoin;

/**
 * Class for Graph Construction, combining all joinable Log Clean queries
 * captured during a user defined window gap (i.e., Tjoin), in the same deduced
 * graph
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class DeductionGraphConstruction {

    Deduction myDeduction;
    DeductionLogClean myDedCLean;
    DeductionUtils myDedUtils;
    BasicUtilis myBasUtils;

    public DeductionGraphConstruction(List<Document> listDocument, MonetDBManag db) throws ParserConfigurationException {

        myDedCLean = new DeductionLogClean(listDocument, db);
        myDedUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
    }

    public DeductionGraphConstruction(List<Document> listDocument, CouchDBManag db) throws ParserConfigurationException {

        myDedCLean = new DeductionLogClean(listDocument, db);
        myDedUtils = new DeductionUtils();
        myBasUtils = new BasicUtilis();
    }

    public DeductionGraphConstruction() throws ParserConfigurationException {

        myDedCLean = new DeductionLogClean();
    }

    /**
     * Implements the "Common Join Codtition" heuristic, using as input the
     * result of "LogClean" and which output is used as input for
     * "NestedLoopDetection"
     *
     * @param dedGraph the set of deduced graphs to be returned
     * @param window deduction windowDeduction in seconds
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws java.io.IOException
     */
    public void GraphConstruction(List<List<Integer>> dedGraph, int window) throws TransformerException, ParserConfigurationException, IOException {

        long startTime = 0;
        long finishTime = 0;
        long elapsedTime = 0;

        System.out.println("[START] ---> CommonJoinCondition heuristic ");
        System.out.println();
        startTime = System.nanoTime();

        constructGraphs(dedGraph, window);

        printGraph(dedGraph);

        finishTime = System.nanoTime();
        elapsedTime = finishTime - startTime;

        System.out.println("[FINISH] ---> CommonJoinCondition heuristic (Elapsed time: " + elapsedTime / 1000000000 + " seconds)");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

    /**
     * Construct all possible deduced graphs of Log Clean queries, based on
     * their projected variables and also Literals/IRIs on their subjects or
     * objects.
     *
     * The set of graphs is viewed as a stack, i.e., a new Log Clean query is
     * added to the latest identified graph that is possible
     *
     * When option "streaming" is enabled, each time a new Log Clean query can
     * be added to more than one graphs, we merge them
     *
     * @param dedGraph list of all deduced graphs
     * @param windJoin window Join defining the DB slice
     * @throws IOException
     */
    public void constructGraphs(List<List<Integer>> dedGraph, int windJoin) throws IOException {

        int k = 0, l = 0;
        List<String> queryUnities = new LinkedList<>();

        //init maps regarding graphs and unites of each query
        for (int i = 0; i < queries.size(); i++) {

            mapLogClQueryToDedGraph.put(i, -1);
            queryUnities = myBasUtils.getQueryEntities(queries.get(i), 4, false);
            mapLogClQueryToAllTPEnts.put(i, queryUnities);
        }

        //Init the first deduced graph with the first identified SELECT query
        dedGraph.add(new LinkedList<Integer>());
        dedGraph.get(0).add(0 + 1);
        mapLogClQueryToDedGraph.put(0, dedGraph.size() - 1);

        for (k = 0; k < queries.size(); k++) {

            // Check if a query can be added in an existing graph
            checkIfCanAddInDedGraph(dedGraph, k);

            if (mapLogClQueryToDedGraph.get(k) == -1) {

                //if not, create a new graph for this query
                dedGraph.add(new LinkedList<Integer>());
                dedGraph.get(dedGraph.size() - 1).add(k + 1);
                mapLogClQueryToDedGraph.put(k, dedGraph.size() - 1);
            }

            for (l = k + 1; l < queries.size(); l++) {

                // for the rest of queries that have not been matched into a graph
                // try to see if they can be added to an existng one
                if (mapLogClQueryToDedGraph.get(l) == -1) {

                    int timeOuterSecs = mapLogClQueryToTimeSecs.get(l);
                    int timeInnerSecs = mapLogClQueryToTimeSecs.get(k);

                    if (timeOuterSecs - timeInnerSecs <= windowJoin || timeOuterSecs - timeInnerSecs == 0) {
                        if (testJoin(k, l)) {

                            dedGraph.get(mapLogClQueryToDedGraph.get(k)).add(l + 1);
                            mapLogClQueryToDedGraph.put(l, mapLogClQueryToDedGraph.get(k));
                        }

                    }
                }

            }
        }
    }

    /**
     * Check if the current LogCLean query can be added in a existing graph
     *
     * @param dedGraph list of all deduced graphs
     * @param queryID Log Clean query's id
     * @return
     */
    public boolean checkIfCanAddInDedGraph(List<List<Integer>> dedGraph, int queryID) {

        boolean flagFoundGraph = false;
        int tmpIDquery = -1;
        int timeIDquery = mapLogClQueryToTimeSecs.get(queryID);
        List<Integer> matchedGraphs = new LinkedList<>();

        //For every deduced graph
        for (int i = 0; i < dedGraph.size(); i++) {

            flagFoundGraph = false;

            if (flagFoundGraph) {
                //  break;
            }

            //From the last to the first graph's query
            for (int j = dedGraph.get(i).size() - 1; j >= 0; j--) {

                if (flagFoundGraph) {
                    break;
                }

                //If streaming option is not enabled, we add the graph 
                //to the first matching graph
                tmpIDquery = dedGraph.get(i).get(j) - 1;

                if (tmpIDquery != queryID && !Objects.equals(mapLogClQueryToDedGraph.get(queryID), mapLogClQueryToDedGraph.get(tmpIDquery))) {

                    int timeOuterInt = mapLogClQueryToTimeSecs.get(tmpIDquery);

                    //check if two queries which are relatively close (respecting Tjoin), have a common join condition
                    if (timeOuterInt - timeIDquery <= windowJoin || timeOuterInt - timeIDquery == 0) {
                        if (testJoin(queryID, tmpIDquery)) {

                            matchedGraphs.add(i);
                            flagFoundGraph = true;
                        }
                    }
                }

            }

        }

        //For all matched graphs, merged to the first all queries of all the others
        for (int h = 0; h < matchedGraphs.size(); h++) {
            if (h == 0) {

                if (!Objects.equals(mapLogClQueryToDedGraph.get(queryID), matchedGraphs.get(0))) {
                    dedGraph.get(matchedGraphs.get(h)).add(queryID + 1);
                    mapLogClQueryToDedGraph.put(queryID, matchedGraphs.get(h));
                    flagFoundGraph = true;
                }

            } else {

                int k = matchedGraphs.get(h);
                dedGraph.get(matchedGraphs.get(0)).addAll(dedGraph.get(k));
            }
        }

        //Then, for all matched graphs, delete all graphs except the first one
        if (matchedGraphs.size() > 1) {
            for (int h = 1; h < matchedGraphs.size(); h++) {

                int k = matchedGraphs.get(h);
                dedGraph.remove(k);
            }
        }

        //reset for all queries, the coresponding map concerning the deduced graph
        if (matchedGraphs.size() > 0) {
            for (int i = 0; i < dedGraph.size(); i++) {

                for (int j = 0; j < dedGraph.get(i).size(); j++) {

                    mapLogClQueryToDedGraph.put((dedGraph.get(i).get(j) - 1), i);
                }
            }
        }

        return flagFoundGraph;
    }

    /**
     * Check if two SELECT LogClean queries have any point of intersection
     * (i.e., IRI/Literal or projected variable)
     *
     * @param outerQuery outer query to be compared
     * @param innerQuery inner query to be compared
     * @return true if they do have something in common
     */
    public boolean testJoin(int outerQuery, int innerQuery) {

        List<String> query1Unities = null;
        List<String> query2Unities = null;
        int indxQuery1 = -1;
        int indxQuery2 = -1;
        boolean ret = false;

        query1Unities = mapLogClQueryToAllTPEnts.get(outerQuery);
        query2Unities = mapLogClQueryToAllTPEnts.get(innerQuery);

        for (String query1Units : query1Unities) {
            if (indxQuery1 == 2) {
                indxQuery1 = -1;
            }
            indxQuery1++;

            //a predicate is not concedered as a common join condition
            if (indxQuery1 != 1) {

                for (String query2Units : query2Unities) {

                    if (indxQuery2 == 2) {
                        indxQuery2 = -1;
                    }

                    indxQuery2++;

                    if (indxQuery2 != 1) {
                        if (query1Units.equalsIgnoreCase(query2Units)) {

                            //If their common point is a variable, this must be projected
                            if (query1Units.contains("?") && query2Units.contains("?")
                                    && myDedUtils.compareListsForIntersection(mapLogClQueryToProjVars.get(outerQuery), mapLogClQueryToProjVars.get(innerQuery)).size() > 0) {

                                return true;
                            } //else if its a constant
                            else if (!query1Units.contains("?") && !query2Units.contains("?")) {

                                return true;
                            }

                        }
                    }

                }
            }

        }

        return ret;
    }

    /**
     * Print each graph's query ids
     *
     * @param dedGraph
     */
    public void printGraph(List<List<Integer>> dedGraph) {

        int size = 0;

        for (int i = 0; i < dedGraph.size(); i++) {

            size = 0;
            Collections.sort(dedGraph.get(i));
            System.out.println("\t Deduced Graph No " + (i + 1));
            System.out.print("\t [ ");

            for (int m = 0; m < dedGraph.get(i).size(); m++) {

                if (m != dedGraph.get(i).size() - 1) {

                    System.out.print(dedGraph.get(i).get(m) + ", ");
                } else {

                    System.out.print(dedGraph.get(i).get(m));
                }

                size += 2 + dedGraph.get(i).get(m).toString().length();
                if (size > 100) {

                    size = 0;
                    System.out.println("");
                    System.out.print("\t  ");
                }
            }

            System.out.print(" ]");
            System.out.println();
        }

        System.out.println();
    }

}
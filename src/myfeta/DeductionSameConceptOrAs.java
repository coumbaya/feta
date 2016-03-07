package myfeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import static myfeta.DeductionNestedLoop.mapDTPtoAnswTotal;

/**
 * Class for "SameConcept/SameAs" heuristic, combine DTPs in the same BGP based
 * on their same concept over their projected common variables or constants
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class DeductionSameConceptOrAs {

    DeductionUtils myDedUtils;
    DeductionNestedLoop myInverse;
    BasicUtilis myBasUtils;

    public static List<List<List<String>>> setDTPbasedConceptsLatest;
    public static HashMap<List<String>, List<String>> mapTPtoConcepts;
    public static List<List<List<String>>> sameConceptBGPsFinal;

    public DeductionSameConceptOrAs() throws ParserConfigurationException {

        myDedUtils = new DeductionUtils();
        myInverse = new DeductionNestedLoop();
        myBasUtils = new BasicUtilis();
        sameConceptBGPsFinal = new LinkedList<>();
        setDTPbasedConceptsLatest = new LinkedList<>();
        mapTPtoConcepts = new HashMap<>();
    }

    /**
     * Implement "SameConcept/SameAs" heuristic
     *
     * @param windowDeduction slice of dataBase to be used as FETA input
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public void sameConceptOrAs(int windowDeduction) throws TransformerException, ParserConfigurationException, URISyntaxException, InterruptedException {

        String request = "";
        String result = "";
        String tmpAnsVal = "";
        String server = "";
        int NumberOfCalls = 0;
        boolean findConcepts = false;
        List<String> valsAnsTotal = null;
        List<String> keyDTPTemp = null;
        List<String> listConceptsReturned = null;
        List<String> valsAll;
        List<String> allEndpoints = constructFederation();
        server = "172.16.9.15";

        for (List<String> DTPkey : mapDTPtoAnswTotal.keySet()) {

            findConcepts = false;
            valsAll = new LinkedList<>();
            keyDTPTemp = new LinkedList<>();
            keyDTPTemp.add(DTPkey.get(0));
            keyDTPTemp.add(DTPkey.get(1));
            keyDTPTemp.add(DTPkey.get(2));
            keyDTPTemp.add(DTPkey.get(3));

            listConceptsReturned = new LinkedList<>();
            valsAnsTotal = mapDTPtoAnswTotal.get(DTPkey);
            valsAll.addAll(valsAnsTotal);

            for (int i = 0; i < valsAll.size(); i++) {

                tmpAnsVal = "";
                if ((valsAll.get(i).contains("http") && !valsAll.get(i).contains("<")
                        && !valsAll.get(i).contains(">")) || (valsAll.get(i).contains("<")
                        && valsAll.get(i).contains("http") && valsAll.get(i).contains(">"))) {

                    tmpAnsVal = valsAll.get(i);

                    if (!(valsAll.get(i).startsWith("<") && valsAll.get(i).endsWith(">"))) {

                        tmpAnsVal = "<" + tmpAnsVal + ">";
                    }

                    if (addKnownConcepts(keyDTPTemp, tmpAnsVal)) {

                        break;
                    }

                    if (!tmpAnsVal.contains("ontology")) {

                        if (findConcepts) {

                            break;
                        }
                    }

                    for (int j = 0; j < allEndpoints.size(); j++) {

                        if (findConcepts) {

                            break;
                        }

                        //  System.out.println("********* i "+i+"**********j "+j+"******tmpVal "+tmpVal+" vs"+setEndpoints.get(j)+"****")
                        request = convertSPARQLtoHTTP(server, Integer.parseInt(allEndpoints.get(j)),
                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                                + "    SELECT distinct ?parent ?same\n"
                                + "    WHERE { \n"
                                + "       " + tmpAnsVal + " a ?classe .\n"
                                + "        ?classe rdfs:subClassOf* ?parent .\n"
                                + "        optional {\n"
                                + "            ?parent <http://www.w3.org/2002/07/owl#sameAs> ?same\n"
                                + "        }\n"
                                + "    }");

                        result = callURL(request);
                        NumberOfCalls++;

                        //  System.out.println("Result concept json" + result);
                        if (parseAndAddValuesFromConceptAnswers(listConceptsReturned, result, tmpAnsVal)) {

                            findConcepts = true;
                        }

                        result = "";
                    }
                }

            }

            for (int k = 0; k < listConceptsReturned.size(); k++) {

                myBasUtils.insertToMap4(mapTPtoConcepts, keyDTPTemp, listConceptsReturned.get(k));
            }

            listConceptsReturned.clear();
        }

        listConceptsReturned.clear();
    }

    /**
     * Construct virtuoso endpoints federation
     *
     * @return
     */
    public List<String> constructFederation() {

        List<String> setEndpoints = new LinkedList<>();

        setEndpoints.add("8700");
        setEndpoints.add("8701");
        setEndpoints.add("8702");
        setEndpoints.add("8703");
        setEndpoints.add("8704");
        setEndpoints.add("8705");
        setEndpoints.add("8706");
        setEndpoints.add("8707");
        setEndpoints.add("8708");
        setEndpoints.add("8709");
        setEndpoints.add("8710");
        setEndpoints.add("8711");
        setEndpoints.add("8712");
        setEndpoints.add("8713");
        setEndpoints.add("8714");
        setEndpoints.add("8715");
        setEndpoints.add("8716");
        setEndpoints.add("8717");
        setEndpoints.add("8718");
        setEndpoints.add("8719");

        return setEndpoints;
    }

    /**
     * Add known concepts into concept list in order to minimize number of
     * queries sent to endpoints, to find answers's concepts
     *
     * @param triplePat
     * @param value
     * @return
     */
    public boolean addKnownConcepts(List<String> triplePat, String value) {

        boolean found = false;

        if (value.contains("geonames")) {

            myBasUtils.insertToMap4(mapTPtoConcepts, triplePat, "http://www.geonames.org/ontology#Feature");
            found = true;
        } else if (value.contains("data.nytimes")) {

            myBasUtils.insertToMap4(mapTPtoConcepts, triplePat, "http://data.nytimes.com/elements");
            found = true;
        } else if (value.contains("topics.nytimes")) {

            myBasUtils.insertToMap4(mapTPtoConcepts, triplePat, "http://topics.nytimes.com/top/reference/timestopics");
            found = true;
        } else if (value.contains("data.linkedmdb.org/resource/film")) {

            myBasUtils.insertToMap4(mapTPtoConcepts, triplePat, "http://data.linkedmdb.org/resource/film");
            myBasUtils.insertToMap4(mapTPtoConcepts, triplePat, "<http://data.linkedmdb.org/resource/movie/film>");
            found = true;
        }

        return found;
    }

    /**
     * Get values from answer JSON string, and insert it in the "listConcept"
     *
     * @param listConcepts
     * @param ConcAnswer
     * @param tmpVa
     * @return
     */
    public boolean parseAndAddValuesFromConceptAnswers(List<String> listConcepts, String ConcAnswer, String tmpVa) {

        int idx = 0;
        String currValue = "";
        int pos = 0;
        boolean foundVals = false;

        do {

            pos = 0;
            idx = ConcAnswer.indexOf("value", pos);
            if (idx > 0) {

                pos = idx + 9;
                ConcAnswer = ConcAnswer.substring(pos, ConcAnswer.length() - 1);

                if (ConcAnswer.indexOf("\"") > 0) {

                    currValue = ConcAnswer.substring(0, ConcAnswer.indexOf("\""));

                    if (currValue.contains("http") && !currValue.contains("..") && currValue.contains("/")) {
                        if (!myBasUtils.elemInListContained(listConcepts, currValue)) {

                            listConcepts.add(currValue);
                        }
                    }

                    currValue = "";
                }
            }

        } while (idx > 0);

        if (listConcepts.size() > 0) {

            foundVals = true;
        }

        return foundVals;
    }

    /**
     * Sent a SPARQL query in XDecimal format return answer String (JSON format)
     *
     * @param myURL SPARQL query in HTTP XDecimal format to be sent
     * @return
     */
    public String callURL(final String myURL) {

        // System.out.println("Requeted URL:" + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;

        try {

            final URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null) {

                urlConn.setReadTimeout(60 * 1000);
            }
            if (urlConn != null && urlConn.getInputStream() != null) {

                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                final BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {

                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {

                        sb.append((char) cp);
                    }

                    bufferedReader.close();
                }
            }

            in.close();
        } catch (IOException e) {

            throw new RuntimeException("Exception while calling URL:" + myURL, e);
        }

        return sb.toString();
    }

    /**
     * Convert a SPARQL query from ASCII to XDecimal format in order to sent it
     * to the endpoint (HTTP request format)
     *
     * @param host server IP address, hosting all virtuoso endpoints
     * @param Port server SPARQL virtuoso endpoint port to sent the query
     * @param query SPARQL query converted in HTTP XDecimal
     * @return
     * @throws URISyntaxException
     */
    public String convertSPARQLtoHTTP(final String host, final int Port, final String query) throws URISyntaxException {

        String result = "";

        URI uri = new URI("http", null, host, Port,
                "/sparql/", "default-graph-uri=&query=" + query + "&format=json&timeout=0&debug=on", null);

        result = uri.toString();

        return result;
    }

}
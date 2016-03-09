package myfeta;

import static java.lang.Math.abs;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static myfeta.Deduction.mapAnsEntryToAllSignatures;
import static myfeta.Deduction.mapAnsEntryToListValues;
import static myfeta.Deduction.mapAnsSingatureToAllValues;

/**
 * Class with basic help functions
 *
 * @author Nassopoulos Georges
 * @version 0.9
 * @since 2016-01-13
 */
public class BasicUtilis {

    /**
     * Convert timeStamp from HH:MM:SS format, into total number of seconds
     *
     * @param time input timestamp in HH:MM:SS format
     * @return time in total number of seconds
     */
    public int getTimeInSec(String time) {

        if (time.contains(".")) {

            time = time.substring(0, time.indexOf("."));
        }

        int timeInSec = 0;
        int hour = 0, min = 0, second = 0;

        hour = Integer.parseInt(time.substring(0, 2));
        min = Integer.parseInt(time.substring(3, 5));
        second = Integer.parseInt(time.substring(6, 8));

        timeInSec = hour * 3600 + min * 60 + second;

        return timeInSec;
    }

    /**
     * Convert a timestamp from total number secondes into HH:MM:SS format
     *
     * @param timeInSec input in total number of seconds
     * @return time timestamp in HH:MM:SS format
     */
    public String getTimeToString(int timeInSec) {

        String resultTime = "";
        int hour = 0, min = 0, second = 0;
        int intVal = abs(timeInSec);

        hour = intVal / 3600;
        min = (intVal % 3600) / 60;
        second = intVal % 60;

        resultTime = String.format("%02d:%02d:%02d", hour, min, second);

        return resultTime;
    }

    /**
     * Search if an integer is equal to any elemnt of a list of elements
     *
     * @param listOfElems input list of elements
     * @param searchItem item to be searched
     * @return true if it exists
     */
    public boolean elemInListEquals(List<Integer> listOfElems, int searchItem) {

        for (int item : listOfElems) {
            if (item == searchItem) {

                return true;
            }
        }

        return false;
    }

    public boolean elemInListEquals(List<String> listOfElems, String searchItem) {

        for (String item : listOfElems) {
            if (item.equalsIgnoreCase(searchItem)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Check if a string is contained into any element of a list of elements
     *
     * @param listOfElems input list of elements
     * @param searchItem element to be search to be searched
     * @return true if it exists
     */
    public boolean elemInListContained(List<String> listOfElems, String searchItem) {

        for (String str : listOfElems) {
            if (str.contains(searchItem)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Check if a list of strings is contained into a list of lists of strings
     *
     * @param listOfLists input list of lists of elements
     * @param searchList list of elements to be searched
     * @return true if searchList is contained into listOfLists
     */
    public boolean listInListContain(List<List<String>> listOfLists, List<String> searchList) {

        for (List<String> list : listOfLists) {
            if (list.get(0).equals(searchList.get(0))
                    && list.get(1).equals(searchList.get(1))
                    && list.get(2).equals(searchList.get(2))) {

                return true;
            }

            if (list.size() == 6) {
                if (list.get(3).equals(searchList.get(0))
                        && list.get(4).equals(searchList.get(1))
                        && list.get(5).equals(searchList.get(2))) {

                    return true;
                }
            }

        }

        return false;
    }

    public boolean listInListContain2(List<List<Integer>> listOfLists, List<Integer> searchList) {

        int count = 0;

        for (List<Integer> list : listOfLists) {

            if (list.size() == searchList.size()) {

                count = 0;
                for (int i = 0; i < list.size(); i++) {

                    if (list.get(i).equals(searchList.get(i))) {

                        count++;
                    }
                }

                if (count == list.size()) {

                    return true;
                }
            }

        }

        return false;
    }

    /**
     * Clone a list of strings into another one. This function is used in order
     * to solve the problem of pointers when we affect a list to another
     *
     * @param input
     * @return the cloned list
     */
    public List<String> cloneListElems(List<String> input) {

        List<String> output = new LinkedList<>();

        for (int i = 0; i < input.size(); i++) {
            output.add(input.get(i));

        }

        return output;
    }

    public List<List<String>> cloneListOfList(List<List<String>> listSrc) {

        List<List<String>> clone = new LinkedList<>();
        List<String> temp = null;

        for (int i = 0; i < listSrc.size(); i++) {

            temp = new LinkedList<>();
            for (int j = 0; j < listSrc.get(i).size(); j++) {

                temp.add(listSrc.get(i).get(j));
            }

            clone.add(temp);
        }

        return clone;
    }

    public List<List<Integer>> cloneListOfList2(List<List<Integer>> listSrc) {

        List<List<Integer>> clone = new LinkedList<>();
        List<Integer> temp = null;

        for (int i = 0; i < listSrc.size(); i++) {

            temp = new LinkedList<>();
            for (int j = 0; j < listSrc.get(i).size(); j++) {

                temp.add(listSrc.get(i).get(j));
            }

            clone.add(temp);
        }

        return clone;
    }

    /**
     * Get common elements of two lists of strings
     *
     * @param outerList outer list to be compared
     * @param innerList inner list to be compared
     * @return common elements of these two lists
     */
    public List<String> commonElements(List<String> outerList, List<String> innerList) {

        List<String> matchedValuesOuter = new LinkedList<>();
        List<String> matchedValuesInner = new LinkedList<>();

        if (outerList.size() >= innerList.size()) {
            matchedValuesOuter = outerList;
            matchedValuesInner = innerList;

        } else if (outerList.size() < innerList.size()) {

            matchedValuesOuter = innerList;
            matchedValuesInner = outerList;
        }

        matchedValuesOuter.retainAll(matchedValuesInner);
        return matchedValuesOuter;
    }

    /**
     * This is a particular function, to find commont elements of two candidate
     * triple patterns, in order to check if they can be merged to one CTP
     *
     * @param outerList outer list to be compared
     * @param innerList inner list to be compared
     * @return number of common elements
     */
    public int candidateTPcomElems(List<String> outerList, List<String> innerList) {

        int comElems = 0;

        if (outerList.size() != innerList.size()) {

            return comElems;
        }

        for (int i = 0; i < outerList.size(); i++) {

            if (outerList.get(i).equalsIgnoreCase(innerList.get(i))) {

                comElems++;
            } else if (outerList.get(i).contains("?") && innerList.get(i).contains("?")) {

                comElems--;
            }

        }

        return comElems;
    }

    /**
     * First "insertToMap" type function, that inserts into an exisitng key a
     * new value, or creates a new key and initialize it with this new value
     *
     * @param map hash map structure
     * @param valueMap input value to be added
     * @param keyMap intput key into which valueMap will be added
     */
    public void insertToMap(HashMap<String, List<String>> map, String valueMap, String keyMap) {

        List<String> valueList = null;
        List newList = null;

        if (map.get(keyMap) != null) {

            valueList = map.get(keyMap);
            valueList.add(valueMap);
        } else {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        }
    }

    public void insertToMap(HashMap<Integer, Integer> map, int valueMap, int keyMap) {

        int value = -1;

        if (map.get(keyMap) == null) {

            map.put(keyMap, valueMap);
        } else {

            value = map.get(keyMap);

            if (value == valueMap) {

                System.out.println("Problem matching key answer to query, key already used!!!");
            }
        }
    }

    public void insertToMap(HashMap<String, List<Integer>> map, int valueMap, String keyMap) {

        List<Integer> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap(HashMap<String, List<String>> map, List<String> valueMap, String keyMap) {

        List<String> valueList = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            valueList = map.get(keyMap);
            valueList.addAll(valueMap);
        }
    }

    public void insertToMap(HashMap<Integer, List<String>> map, String valueMap, int keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap(HashMap<List<String>, List<String>> map, String valueMap, List<String> keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap(HashMap<List<String>, List<Integer>> map, List<String> keyMap, int valueMap) {

        List<Integer> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap(HashMap<List<String>, Integer> map, List<String> keyMap) {

        int value = -1;

        if (map.get(keyMap) == null) {

            map.put(keyMap, 1);
        } else {

            value = map.get(keyMap);
            value++;
            map.put(keyMap, value);
        }
    }

    public void insertToMap(HashMap<List<String>, List<List<Integer>>> map, List<Integer> valueMap, List<String> keyMap) {

        List<List<Integer>> listOfLists = null;
        List newList = null;
        boolean flagSKip = false;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);

            for (int i = 0; i < listOfLists.size(); i++) {

                if ((Objects.equals(listOfLists.get(i).get(0), valueMap.get(0)))
                        && (Objects.equals(listOfLists.get(i).get(1), valueMap.get(1)))) {
                    flagSKip = true;
                }
            }
            if (!flagSKip) {

                listOfLists.add(valueMap);
            }
        }
    }

    public void insertToMap1(HashMap<List<String>, List<List<String>>> map, List<String> valueMap, List<String> keyMap) {

        List<List<String>> value = null;
        List<List<String>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap2(HashMap<Integer, List<Integer>> map, int valueMap, int keyMap) {

        List<Integer> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap2(HashMap<Integer, List<List<String>>> map, List<String> valueMap, int keyMap) {

        List<List<String>> listOfLists = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);
            listOfLists.add(valueMap);
        }
    }

    public void insertToMap2(HashMap<List<String>, List<List<Integer>>> map, List<String> keyMap, List<Integer> valueMap) {

        List<List<Integer>> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap2(HashMap<List<String>, String> map, List<String> keyMap, String valueMap) {

        if (map.get(keyMap) == null) {

            map.put(keyMap, valueMap);
        } else {

            map.put(keyMap, valueMap);
        }
    }

    public void insertToMap3(HashMap<String, List<List<String>>> map, List<String> valueMap, String keyMap) {

        List<List<String>> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap3(HashMap<Integer, List<List<Integer>>> map, List<Integer> valueMap, int keyMap) {

        List<List<Integer>> listOfLists = null;
        List newList = null;
        boolean flagSKip = false;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);
            for (int i = 0; i < listOfLists.size(); i++) {

                if ((Objects.equals(listOfLists.get(i).get(0), valueMap.get(0))) && (Objects.equals(listOfLists.get(i).get(1), valueMap.get(1)))) {

                    flagSKip = true;
                }

            }

            if (!flagSKip) {

                listOfLists.add(valueMap);
            }
        }
    }

    public void insertToMap3(HashMap<List<String>, List<String>> map, List<String> valueMap, List<String> keyMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) != null) {

            value = map.get(keyMap);
            value.addAll(valueMap);
        } else {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        }
    }

    public void insertToMap4(HashMap<Integer, List<List<String>>> map, List<String> valueMap, int keyMap) {

        List<List<String>> listOfLists = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);
            listOfLists.add(valueMap);
        }
    }

    public void insertToMap4(HashMap<List<String>, List<String>> map, List<String> keyMap, String valueMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.add(valueMap);
        }
    }

    public void insertToMap4(HashMap<List<String>, List<String>> map, List<String> keyMap, List<String> valueMap) {

        List<String> value = null;
        List newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            value = map.get(keyMap);
            value.addAll(valueMap);
        }
    }

    public void insertToMap5(HashMap<List<String>, List<List<Integer>>> map, List<Integer> valueMap, List<String> keyMap) {

        List<List<Integer>> listOfLists = null;
        List<List<Integer>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);
            listOfLists.add(valueMap);
        }
    }

    public void insertToMap6(HashMap<List<String>, List<List<List<Integer>>>> map, List<List<Integer>> valueMap, List<String> keyMap) {

        List<List<List<Integer>>> listOfListsOflists = null;
        List<List<List<Integer>>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfListsOflists = map.get(keyMap);
            listOfListsOflists.add(valueMap);
        }
    }

    public void insertToMap7(HashMap<List<String>, List<List<List<Integer>>>> map, List<List<List<Integer>>> valueMap, List<String> keyMap) {

        List<List<List<Integer>>> listOfListsOflists = null;
        List<List<List<Integer>>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.addAll(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfListsOflists = map.get(keyMap);
            listOfListsOflists.addAll(valueMap);
        }
    }

    public void insertToMap8(HashMap<List<Integer>, List<List<List<Integer>>>> map, List<List<Integer>> valueMap, List<Integer> keyMap) {

        List<List<List<Integer>>> listOfLists = null;
        List<List<List<Integer>>> newList = null;

        if (map.get(keyMap) == null) {

            newList = new LinkedList<>();
            newList.add(valueMap);
            map.put(keyMap, newList);
        } else {

            listOfLists = map.get(keyMap);
            listOfLists.add(valueMap);
        }
    }

    /**
     * Get all different predicates of a query
     *
     * @param queryEntities list of triple patterns, in form of list of list of
     * elements
     * @return all different predicates
     */
    public List<String> getQueryPredicates(List<String> queryEntities) {

        List<String> predicates = new LinkedList<>();
        int countIndex = -1;

        for (int i = 0; i < queryEntities.size(); i++) {

            countIndex++;

            if (countIndex == 1) {

                if (!predicates.contains(queryEntities.get(i))) {

                    predicates.add(queryEntities.get(i));
                }
            }

            if (countIndex == 2) {

                countIndex = -1;
            }

        }

        return predicates;
    }

    /**
     * Remove oparators (FILTER, LIMIT and UNION) from query, before extracting
     * all entities
     *
     * @param query intput query to be refined
     * @return string without any operators
     */
    public String removeQueryOperators(String query) {

        String refinedQuery = query;

        //remove new line character
        refinedQuery = refinedQuery.replaceAll("\\n", "");

        //Remove FILTER entities from query
        while (refinedQuery.contains("FILTER")) {

            //BUG
            if (refinedQuery.contains("mass")) {

                refinedQuery = refinedQuery.replace(refinedQuery.substring(refinedQuery.indexOf("FILTER"), refinedQuery.indexOf(")") + 2), "");
            } else if (!refinedQuery.contains("||")) {

                refinedQuery = refinedQuery.replace(refinedQuery.substring(refinedQuery.indexOf("FILTER"), refinedQuery.indexOf(")") + 1), "");
            } else if (refinedQuery.contains("||")) {

                refinedQuery = refinedQuery.substring(0, refinedQuery.indexOf("FILTER") - 2) + "}";
                break;
            }
        }

        // Ignore the LIMIT/OFFSET part from query
        if (refinedQuery.contains(" LIMIT 10000 OFFSET")) {

            refinedQuery = refinedQuery.substring(0, refinedQuery.indexOf(" LIMIT 10000 OFFSET"));
        }

        // Ignore UNION oparators from query       
        if (refinedQuery.contains("UNION")) {

            refinedQuery = refinedQuery.replace("UNION", " ");
        }

        return refinedQuery;
    }

    /**
     * Get, for a specific query, all triple pattern's unities (iRI, variable or
     * Literal) depending on the second argument "typeEntities":
     *
     * (i) '1' is for variables
     *
     * (ii) '2' is for IRIs
     *
     * (iii) '3' is for Literals
     *
     * (iv) '4' for all above
     *
     * Argument "details" defines if we ignore the part "_#number" of variables
     * when BoundJoin is also extracted, or not
     *
     * @param query input query
     * @param typeEntities defines the entity type(s) to be extracted
     * @param detailBound defines the detail of boundJoin variables
     * @return all query entities into a list
     */
    public List<String> getQueryEntities(String query, int typeEntities, boolean detailBound) {

        boolean flagVariable = false, flagURI = false, flagLiteral = false, flagPredicatePref = false;
        boolean flagStart = false;
        boolean flagDoublecotes = false;
        String valueURI = "";
        String valueLiteral = "";
        String valueVariable = "";
        String valuePrefix = "";
        List<String> queryUnities = new LinkedList<>();

        query = removeQueryOperators(query);

        // Ignore UNION entities from query
        for (int i = 0; i < query.length(); i++) {

            // Start parsing triple patterns
            if ((query.charAt(i) == '{' || flagStart) && (typeEntities == 1 || typeEntities == 4)) {

                flagStart = true;
                //Case it is a variable
                if (((query.charAt(i) == '?') || (query.charAt(i) == '$')) || flagVariable) {
                    if (query.charAt(i) == ' ' || query.charAt(i) == '}') {

                        flagVariable = false;
                        if (query.charAt(i) == '$') {

                            queryUnities.add("?");
                        } else {

                            if (valueVariable.indexOf(".") > 0) {
                                valueVariable = valueVariable.substring(0, valueVariable.indexOf("."));
                            }
                            queryUnities.add(valueVariable);
                        }

                        valueVariable = "";

                    } else {

                        flagVariable = true;
                        valueVariable += query.charAt(i);
                    }
                } //Case it is a IRI
                else if ((query.charAt(i) == '<' || flagURI) && (typeEntities == 2 || typeEntities == 4)) {
                    if (query.charAt(i) == '>') {

                        valueURI += query.charAt(i);
                        if (typeEntities == 4 || typeEntities == 2) {

                            if (valueURI.contains("http")) {

                                String tmp = valueURI.substring(valueURI.indexOf("http") + 4);

                                if (tmp.contains("http")) {

                                    int indx = tmp.indexOf("http");
                                    tmp = tmp.substring(indx);
                                    queryUnities.add(valueURI.substring(0, valueURI.indexOf(tmp)) + ">");
                                    queryUnities.add("<" + valueURI.substring(valueURI.indexOf(tmp), valueURI.length() - 1) + ">");
                                } else {

                                    queryUnities.add(valueURI);
                                }

                            }

                        }

                        flagURI = false;
                        valueURI = "";
                    } else {

                        flagURI = true;
                        valueURI += query.charAt(i);
                    }
                } //Case it is a literal
                else if ((((query.charAt(i) == '\"') || ((query.charAt(i) == '\'') && !flagDoublecotes)) || flagLiteral) && (typeEntities == 3 || typeEntities == 4)) {
                    if (query.charAt(i) == '\"') {
                        flagDoublecotes = true;
                    }

                    if (((query.charAt(i) == '\"') || ((query.charAt(i) == '\'') && !flagDoublecotes)) && flagLiteral) {
                        if ((query.charAt(i) == '\"') && flagDoublecotes) {
                            flagDoublecotes = false;
                        }

                        if ((query.charAt(i) == '\'') && !flagDoublecotes) {
                            flagDoublecotes = false;
                        }
                        flagLiteral = false;
                        valueLiteral += query.charAt(i);

                        if (typeEntities == 4 || typeEntities == 3) {

                            if (valueLiteral.indexOf(".") > 0) {
                                valueLiteral = valueLiteral.substring(0, valueLiteral.indexOf("."));
                            }
                            queryUnities.add(valueLiteral);
                        }

                        valueLiteral = "";

                    } else {

                        flagLiteral = true;
                        valueLiteral += query.charAt(i);
                    }
                } //Case it is a IRI but with a PREFIX
                else if (((Character.isLetter(query.charAt(i))) || flagPredicatePref) && (typeEntities == 2 || typeEntities == 4)) {

                    if ((query.charAt(i) == ' ' || query.charAt(i) == '}') && flagPredicatePref) {

                        flagPredicatePref = false;
                        queryUnities.add(valuePrefix);
                        valuePrefix = "";
                    } else {

                        flagPredicatePref = true;
                        valuePrefix += query.charAt(i);
                    }
                }
            }
        }

        //If the user want to ignore the part "_#number" of variables when BoundJoin is used
        if (!detailBound) {
            for (int i = 0; i < queryUnities.size(); i++) {

                if (queryUnities.get(i).contains("_") && !queryUnities.get(i).contains("http")) {

                    queryUnities.set(i, queryUnities.get(i).substring(0, queryUnities.get(i).indexOf("_")));
                }

            }
        }

        if (queryUnities.get(queryUnities.size() - 1).contains("LIMIT")) {
            queryUnities.remove(queryUnities.size() - 1);
        }

        return queryUnities;
    }

    /**
     * Remove special characters from list of strings. For literals, we remove
     * characters "\'" and "\"". For IRIs, we remove "<" and ">"
     *
     * @param myListSrc list of raw string elements
     * @return refined list
     */
    public List<String> refineList(List<String> myListSrc) {

        List<String> refList = new LinkedList<>();
        int startInd = -1;
        int stopInd = -1;

        for (String str : myListSrc) {

            if (str.startsWith("<") && str.endsWith(">")) {
                str = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
            }
            if (str.startsWith("\"") && str.endsWith("\"")) {
                startInd = str.indexOf("\"") + 1;
                stopInd = str.substring(startInd).indexOf("\"");
                str = str.substring(startInd, stopInd + 1);
            }
            if (str.startsWith("\'") && str.endsWith("\'")) {

                startInd = str.indexOf("\'") + 1;
                stopInd = str.substring(startInd).indexOf("\'");
                str = str.substring(startInd, stopInd + 1);
            }

            refList.add(str);
        }

        return refList;
    }

    /**
     * Get the list of the projected variables of a SELECT query
     *
     * @param query input query
     * @return list of projected variables
     */
    public List<String> getProjVars(String query) {

        String subquery = "";
        String tmpVar = "";
        int tmpIndxStart = -1;
        int tmpIndxStop = -1;
        List<String> projVars = new LinkedList<>();

        if (query.contains("SELECT")) {

            // get all variables of query's BGP
            if (query.contains("SELECT *")) {

                projVars = getQueryEntities(query, 1, true);
            } else {

                //subpart of a query from start to end of projected variables
                subquery = query.substring(query.indexOf("?"), query.indexOf("WHERE") - 1);
                subquery = subquery + " ";

                for (int k = 0; k < subquery.length(); k++) {

                    tmpIndxStart = subquery.indexOf("?");

                    //once capture the start of a variable, parce it
                    if (tmpIndxStart != -1) {

                        if (subquery.indexOf(" ") > 0) {

                            tmpVar = subquery.substring(tmpIndxStart, subquery.indexOf(" "));
                            tmpIndxStop = subquery.indexOf(" ");
                        } else {

                            tmpVar = subquery.substring(tmpIndxStart, subquery.length() - 1);
                            tmpIndxStop = subquery.length() - 1;
                        }

                        //ignore the part "_#number" of boundJoin queries 
                        if (tmpVar.contains("_")) {

                            tmpVar = tmpVar.substring(0, tmpVar.indexOf("_"));
                        }

                        //add only one variable  boundJoin queries
                        // if (!elemInListContained(projVar, tmpVar)) {
                        if (!elemInListEquals(projVars, tmpVar)) {

                            projVars.add(tmpVar);
                            tmpIndxStart = -1;
                            tmpVar = "";
                        } else {

                            break;
                        }

                        subquery = subquery.substring(tmpIndxStop + 1);
                    } else {

                        break;
                    }

                }
            }

        }

        return projVars;
    }

    /**
     * Get the list of FILTER values in a SELECT squery
     *
     * @param query query to be parsed
     * @return the list of FILTER values
     */
    public List<String> getFILTERvals(String query) {

        int indxFilter = query.indexOf("FILTER ((");
        int indxStartVal = 0;
        int indxStopVal = 0;
        int indxNxtValue = 0;
        int indxInverseVarStart = -1;
        int indxINverseVarStop = -1;
        String tmpVals = "";
        List<String> allFilterValues = new LinkedList<>();

        if (indxFilter != -1) {

            //Capture variable of FILTER option
            indxInverseVarStart = indxFilter + 9;
            if (query.contains("=")) {

                indxINverseVarStop = query.indexOf("=");
            }

            //Start of a FILTER value
            indxStartVal = indxINverseVarStop + 1;
            if (indxINverseVarStop != -1) {

                query = query.substring(indxStartVal, query.length() - 1);
                indxStartVal = 0;
                //Index to stop of all filter values
                while (!query.startsWith("))}")) {

                    if (indxStartVal != -1) {

                        //Index of end of a FILTER value
                        indxStopVal = query.indexOf(")") - 1;
                        if (indxStopVal != -1) {

                            tmpVals = query.substring(indxStartVal, indxStopVal);
                            allFilterValues.add(tmpVals);

                            //Capture a new value
                            indxNxtValue = query.indexOf("=");
                            if (indxNxtValue == -1) {

                                break;
                            }

                            if (indxNxtValue != -1) {

                                indxStartVal = indxNxtValue + 1;
                                query = query.substring(indxStartVal + 1);
                                indxStartVal = 0;
                                tmpVals = "";
                                indxNxtValue = 0;
                                indxStopVal = 0;
                            } else {

                                break;
                            }
                        }
                    }
                }
            }

        }

        if (indxInverseVarStart != -1 && indxINverseVarStop == -1) {

            allFilterValues.add("none");
        }

        return allFilterValues;
    }

    /**
     * Get the list of the filter variables of a SELECT FILTER query
     *
     * @param query input query
     * @return the list of filter variables
     */
    public String getFILTERvar(String query) {

        String var = "";
        int indxFilter = query.indexOf("FILTER ((");
        int indxInverseVarStart = -1;
        int indxINverseVarStop = -1;

        if (indxFilter != -1) {

            //Capture variable of FILTER option
            indxInverseVarStart = indxFilter + 9;
            indxINverseVarStop = query.indexOf("=");
            var = query.substring(indxInverseVarStart, indxINverseVarStop);
        }

        return var;
    }

    /**
     * Convert the query sent from an Xdecimal into an ASCII format
     *
     * @param queryEx query in Xdecimal format
     * @return query in ASCII format
     */
    public String convExToASCII(String queryEx) {

        char firstLineChar;
        String hex = "";
        StringBuilder output;
        String queryAscii = "";
        int index2 = 0;

        for (int i = index2; i < queryEx.length(); i++) {

            firstLineChar = queryEx.charAt(i);
            if (firstLineChar == '%') {
                //Ignore a new line
                if (queryEx.substring(i).startsWith("%0A+")) {

                    i = i + 4;

                } //ignore spaces
                else if (queryEx.substring(i).startsWith("%0A%0A")) {

                    i = i + 5;

                } //catch other alpharithmetic characters except alphabetic characters 
                else {

                    hex = "";
                    output = new StringBuilder();
                    hex = queryEx.substring(i + 1, i + 3);
                    output.append((char) Integer.parseInt(hex, 16));
                    queryAscii += output.toString();
                    i += 2;
                }
                // replace '+' with space
            } else if (firstLineChar == '+') {

                queryAscii += " ";
            } else if (firstLineChar == '&') {
                //http key word to ignore
                if (queryEx.substring(i + 1).startsWith("infer")) {

                    break;
                }
            } else {
                //catch alphabetic characters 
                queryAscii += Character.toString(queryEx.charAt(i));
            }
        }

        return queryAscii;
    }

    /**
     * Get all variables of a answer string, which is stored in json format
     *
     * @param answer json format string to be parsed
     * @return list of answer variables
     */
    public List<String> getAnswerVars(String answer) {

        List<String> listValues = new LinkedList<>();
        String tmpVar = "";
        String tmpAnsw = "";
        int indxStart = -1;
        int indxStop = -1;

        if (answer.contains("results")) {

            tmpAnsw = answer;
            tmpAnsw = tmpAnsw.substring(tmpAnsw.indexOf("vars") + 6);
            for (int i = 0; i < tmpAnsw.length(); i++) {

                indxStart = tmpAnsw.indexOf("\"");
                if (indxStart != -1) {

                    tmpAnsw = tmpAnsw.substring(indxStart + 1);
                    indxStop = tmpAnsw.indexOf("\"");
                    if (indxStop != -1) {

                        tmpVar = tmpAnsw.substring(0, indxStop);
                        /**
                         * BUUUUUUUUUUUUG
                         */
                        if (tmpVar.contains("_")) {
                            tmpVar = tmpVar.substring(0, tmpVar.indexOf("_"));
                        }

                        if (!elemInListEquals(listValues, tmpVar)) {

                            listValues.add(tmpVar);
                        }
                        tmpAnsw = tmpAnsw.substring(indxStop + 1);
                        indxStop++;
                    }

                    if (tmpAnsw.startsWith("]")) {

                        break;
                    }
                }

            }
        }

        return listValues;
    }
    
    /**
     * 
     * @param inputList
     * @return 
     */
    public List<String> sortAndRemoveRedundancy(List<String> inputList){
        
        //BUUUUUUUUUG create clone
     //   List<String> sortedList= new LinkedList<>();
        
          HashSet hs = new HashSet();
            hs.addAll(inputList);
            inputList.clear();
            inputList.addAll(hs);
            Collections.sort(inputList);
        return inputList;
    }
    
        /**
     * 
     * @param inputList
     * @return 
     */
    public List<Integer> sortAndRemoveRedundancy2(List<Integer> inputList){
        
        //BUUUUUUUUUG create clone
     //   List<String> sortedList= new LinkedList<>();
        
          HashSet hs = new HashSet();
            hs.addAll(inputList);
            inputList.clear();
            inputList.addAll(hs);
            Collections.sort(inputList);
        return inputList;
    }


    /**
     * Get all distinct IRIs or Literals, contained in answer of json format
     * string, for a given variable as input
     *
     * @param answer input answer of json format
     * @param variable input variable
     * @return list of answers, for variable passed as parameter
     */
    public List<String> getDistAnsPerVar(String answer, String variable) {

        int idxValue = 0;
        String ansCopy = answer;
        String strValue = "";
        List<String> listValues = new LinkedList<>();

        if (variable.contains("?")) {

            variable = variable.substring(variable.indexOf("?") + 1);
        }

        variable = "\"" + variable;
        if (ansCopy.contains("results")) {

            ansCopy = ansCopy.substring(ansCopy.indexOf("results"));
        }

        int i = ansCopy.indexOf(variable);

        while (i >= 0) {

            ansCopy = ansCopy.substring(i);
            idxValue = ansCopy.indexOf("value\"");

            if (idxValue + 9 > ansCopy.length()) {
                break;
            }

            ansCopy = ansCopy.substring(idxValue + 9, ansCopy.length());

            if (!ansCopy.contains("\"")) {
                break;
            }

            strValue = ansCopy.substring(0, ansCopy.indexOf("\""));
            listValues.add(strValue);
            ansCopy = ansCopy.substring(ansCopy.indexOf("\""));
            i = ansCopy.indexOf(variable);
        }

        return listValues;
    }

    /**
     * Match each string answer's variable to corresponding values
     *
     * @param key answer of entry log
     * @param requestQuery corresponding query of entry log
     * @param Answer answer string in jason string format
     */
    public void setVarsToAnswEntities(int key, String requestQuery, String Answer) {

        List<String> answerEntities = null;
        List<String> matchQueryExtrVars = new LinkedList<>();
        List<String> allTPs = new LinkedList<>();
        List<String> motif = null;
        HashMap<List<String>, List<String>> mapCurrQuerySignature = new HashMap<>();
        String newKey = "";
        String originalVarPosition = "";
        String newKeyDetailed = "";

        matchQueryExtrVars = getAnswerVars(Answer);

        if (matchQueryExtrVars.isEmpty()) {

            matchQueryExtrVars = new LinkedList<>();
        }

        if (matchQueryExtrVars.size() > 0) {

            if (Answer.length() > 0 && Answer.contains("value")) {

                allTPs = getQueryEntities(requestQuery, 4, false);
                for (int i = 0; i < allTPs.size(); i += 3) {

                    motif = new LinkedList<>();

                    if (allTPs.get(i).contains("?")) {
                        motif.add(allTPs.get(i));
                    }
                    motif.add(allTPs.get(i + 1));
                    if (allTPs.get(i + 2).contains("?")) {
                        motif.add(allTPs.get(i + 2));
                    }

                    if (mapCurrQuerySignature.get(motif) == null) {

                        mapCurrQuerySignature.put(motif, null);
                    }

                }

                for (int u = 0; u < matchQueryExtrVars.size(); u++) {

                    answerEntities = new LinkedList<>();

                    for (List<String> currMotiv : mapCurrQuerySignature.keySet()) {

                        if (elemInListEquals(currMotiv, "?" + matchQueryExtrVars.get(u))) {
                            newKeyDetailed = "";

                            if (currMotiv.get(0).contains("?")) {

                                String tmpVar = currMotiv.get(0).substring(1, currMotiv.get(0).length());

                                if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                    originalVarPosition = "subject";
                                    newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                                }
                            }

                            if (currMotiv.get(1).contains("?")) {

                                String tmpVar = currMotiv.get(1).substring(1, currMotiv.get(1).length());

                                if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                    originalVarPosition = "object";
                                    newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                                }
                            }

                            if (currMotiv.size() == 3 && currMotiv.get(2).contains("?")) {

                                String tmpVar = currMotiv.get(2).substring(1, currMotiv.get(2).length());

                                if (matchQueryExtrVars.get(u).equalsIgnoreCase(tmpVar)) {

                                    originalVarPosition = "object";
                                    newKeyDetailed = Integer.toString(key) + "_" + matchQueryExtrVars.get(u) + "_" + currMotiv.get(1) + "_" + originalVarPosition;
                                }

                            }

                            if (!newKeyDetailed.equalsIgnoreCase("")) {

                                insertToMap(mapAnsEntryToAllSignatures, newKeyDetailed, Integer.toString(key));
                            }

                        }
                    }

                    newKey = Integer.toString(key) + matchQueryExtrVars.get(u);

                    answerEntities = getDistAnsPerVar(Answer, matchQueryExtrVars.get(u));

                    if (newKey.contains("_")) {

                        newKey = newKey.substring(0, newKey.indexOf("_"));
                    }
                    if (newKey.contains("?")) {

                        newKey = newKey.substring(newKey.indexOf("?") + 1);
                    }

                    Collections.sort(answerEntities);
                    insertToMap(mapAnsEntryToListValues, answerEntities, newKey);
                    insertToMap(mapAnsSingatureToAllValues, answerEntities, newKeyDetailed);
                }

            } else {

                insertToMap(mapAnsEntryToAllSignatures, "NoAnswersToQuery", Integer.toString(key));
            }

        }

    }
    

}
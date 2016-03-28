# FETA: experiments with FedBench

In this Section we present, (1) execution time for all traces, in isolation and in concurrence (2) precision/recall of deduced triple patterns and joins, for queries executed in isolation and (3) precision/recall of joins, for queries executed in concurrence. Both experiments are made with the same IP Address host, using either Anapsid or FedX query engines.

**Summary**

1. **Execution time**
   * [Isolated execution](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#executiontimeisolated)
   * [Concurrent execution](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#executiontimeconcurrent)

2. **Isolated execution**
   * [Precision/recall of triple patterns](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisiontps)
   * [Precision/recall of joins](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisionjoins)

3. **Concurrent execution**
   * [Cross Domain (CD)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#crossdomain)
   * [Life Science (LS)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#lifescience)
   * [Mixed collection (MX)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#mixedcollection)

## Execution time

In order to evaluate FETA's performance we provide the execution times, for both queries executed in isolation and in concurrence. Experiments were run on Ubuntu 14.04 LTS, with 8 CPUs and 32G RAM.

### ExecutionTimeIsolated

Next we present execuiton times of queries executed in isolation, for Cross Domain (CD) and Life Science (LS) collections.

| Query         | Anapsid    | FedX  | Query         | Anapsid    | FedX  |
| ------------- |:----------:|:-----:| ------------- |:----------:|:-----:|
| CD1           |0s          |0s     | LS1           |0s          |0s     |
| CD2           |0s          |0s     | LS2           |0s          |0s     |
| CD3           |0s          |0s     | LS3           |4s          |10s    |
| CD4           |0s          |0s     | LS4           |14s         |0s     |
| CD5           |0s          |0s     | LS5           |1s          |2s     |
| CD6           |33s         |0s     | LS6           |10s         |4m31s  |
| CD7           |0s          |0s     | LS7           |1m38s       |2s     |

### ExecutionTimeConcurrent

Next we present execuiton times of queries executed in concurrence, for Cross Domain (CD), Life Science (LS) and Mixed (MX) collections.

| Query         | Anapsid    | FedX  | Query         | Anapsid    | FedX  | Query         | Anapsid    | FedX  |
| ------------- |:----------:|:-----:| ------------- |:----------:|:-----:| ------------- |:----------:|:-----:|
| CD mixage1    |0s          |0s     | LS mixage1    |0s          |0s     | MX mixage1    |0s          |0s     |
| CD mixage2    |0s          |0s     | LS mixage1    |0s          |0s     | MX mixage1    |0s          |0s     |
| CD mixage3    |0s          |0s     | LS mixage1    |0s          |0s     | MX mixage1    |0s          |0s     |
| CD mixage4    |0s          |0s     | LS mixage1    |0s          |0s     | MX mixage1    |0s          |0s     |


## Isolated execution

First, we evaluate FETA by measuring precision/recall of both deduced triple patterns and joins, for queries executed in isolation comparing to those identified in original federated queries, for Cross Domain and Life Science collections.

### RecallPrecisionTPs

Next, we present precision/recall of deduced triple patterns, for Anapsid and FedX traces.

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/precision_triple_patterns_per_query.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/recall_triple_patterns_per_query.PNG)

### RecallPrecisionJoins

Next, we present precision/recall of deduced joins, for Anapsid and FedX traces.

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/precision_joins_per_query.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/recall_joins_per_query.PNG)


## Concurrent execution

Next, we evaluate FETA by measuring precision/recall of deduced joins, for queries executed in concurence comparing to those executed in isolation, for Cross Domain (CD), Life Science (LS) and Mixed (MX) collections. This experiment was made by varying the user-defined gap threshold, i.e. the maximum temporal distance between two queries to consider them possibly joinables, namely 1%, 10%, 25%, 50%, 75% and 100% of the duration of the input capture trace.

### CrossDomain

Next, we present precision/recall of deduced joins, concerning the concurrent execution of all CD collection's queries (CD1 to CD7).

**ANAPSID traces' precision**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_precision_cd.PNG)

**ANAPSID traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_cd.PNG)


**FedX traces' precision**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_precision_cd.PNG)

**FedX traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_cd.PNG)

### LifeScience

Next, we present precision/recall of deduced joins, concerning the concurrent execution of all LS collection queries (LS1 to LS7).

**Anapsid traces' precision**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_precision_ls.PNG)

**Anapsid traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_ls.PNG)


**FedX traces' precision**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_precision_ls.PNG)

**FedX traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_ls.PNG)

### MixedCollection

Next, we present precision/recall of deduced joins, concerning the concurrent execution of a mixed collection. This set is composed of non similar queries from CD and LS collections, i.e. queries that have a null resultset for their common projected variables, namely: CD3, CD4, CD5, CD6, LS2 and LS3.

**Anapsid traces' precision**

We ommit this figure, as precision for this collection is always equal to 1.

**Anapsid traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_mx.PNG)


**FedX traces' precision**

We ommit this figure, as precision for this collection is always equal to 1.

**FedX traces' recall**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_mx.PNG)

# FETA: experiments with FedBench

In this Section we present, (1) precision/recall of deduced triple patterns and joins, for queries executed in isolation and (2) precision/recall of joins, for queries executed in concurence. Both of experiments are made with the same IP Address host, using either Anapsid or FedX query engines.

**Summary**

1. **Isolated execution**
   * [Precision/recall of triple patterns](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisiontps)
   * [Precision/recall of joins](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisionjoins)

2. **Concurent execution**
   * [Cross Domain (CD)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#crossdomain)
   * [Life Science (LS)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#lifescience)
   * [Mixed collection (MX)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#mixedcollection)


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

Next, we evaluate FETA by measuring precision/recall of deduced joins, for queries executed in concurence comparing to those executed in isolation.

### CrossDomain

Next, we present precision/recall of deduced joins, concerning the concurrent execution of all CD collection's queries (CD1 to CD7).

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_precision_cd.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_cd.PNG)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_precision_cd.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_cd.PNG)

### LifeScience

Next, we present precision/recall of deduced joins, concerning the concurrent execution of all LS collection queries (LS1 to LS7).

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_precision_ls.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_ls.PNG)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_precision_ls.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_ls.PNG)

### MixedCollection

Next, we present precision/recall of deduced joins, concerning the concurrent execution of a mixed collection. This set is composed of non similar queries from CD and LS collections, i.e. queries that have a null resultset for their common projected variables, namely: CD3, CD4, CD5, CD6, LS2 and LS3.

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_precision_mx.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/anapsid_recall_mx.PNG)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_precision_mx.PNG)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/fedx_recall_mx.PNG)

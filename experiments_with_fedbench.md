# FETA: experiments with FedBench

In this Section we present, 1) precision/recall of triple patterns and joins, for queries executed in isolation and 2) precision/recall of joins, for queries executed in concurence. Both of experiments are made with the same IP Address host, using Anapsid and FedX query engines.

**Summary**

1. **Isolated execution**
   * [Precision/recall of triple patterns](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisiontpsjaccard)
   * [Precision/recall of joins](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#recallprecisionjoins)

2. **Concurent execution**
   * [Cross Domain (CD)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#crossdomain)
   * [Life Science (LS)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#lifescience)
   * [Mixed Queries (MX)](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench.md#non-similar)


## Isolated execution

### RecallPrecisionTPs

### RecallPrecisionJoins


## CrossDomain

Next figures concern the concurrent execution of all CD collection queries (CD1 to CD7).

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_CD_pairJoins_precision_all_traces.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_CD_pairJoins_recall_all_mixages.jpeg)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_CD_pairJoins_precision_all_mixages.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_CD_pairJoins_recall_all_mixages.jpeg)

## LifeScience

Next figures concern the concurrent execution of all LS collection queries (LS1 to LS7).

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_LS_pairJoins_precision_all_traces.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_LS_pairJoins_recall_all_mixages.jpeg)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_LS_pairJoins_precision_all_mixages.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_LS_pairJoins_recall_all_mixages.jpeg)

## Non-similar

Next figures concern the concurrent execution of non similar queries from CD and LS collections, i.e. queries that have a null resultset for their common projected variables, namely: CD3, CD4, CD5, CD6, LS2 and LS3.

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_MX_pairJoins_precision_all_traces.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/ANAPSID_MX_pairJoins_recall_all_mixages.jpeg)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_MX_pairJoins_precision_all_mixages.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/experiments_with_fedbench/execution_figures/FEDX_MX_pairJoins_recall_all_mixages.jpeg)

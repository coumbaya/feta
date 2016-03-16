# FETA: precision and recall

In this Section we present FETA recall/precision of deduced joins over fedbench queries, produced with FedX and Anapsid.
Each federated query log, is produced by concurently executing a set of queries with the same query engine IP Address.

Summary

1. [Cross Domain (CD)](https://github.com/coumbaya/feta/blob/master/fedbench_precision_recall.md#crossdomain)
2. [Life Science (LS)](https://github.com/coumbaya/feta/blob/master/fedbench_precision_recall.md#lifescience)
3. [Non-similar](https://github.com/coumbaya/feta/blob/master/fedbench_precision_recall.md#non-smilar)

# Cross Domain

Next figures concern the concurrent execution of all CD collection queries (CD1 to CD7).

**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/ANAPSID_CD_pairJoins_precision_all_traces.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/ANAPSID_CD_pairJoins_recall_all_mixages.jpeg)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/FEDX_CD_pairJoins_precision_all_mixages.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/FEDX_CD_pairJoins_recall_all_mixages.jpeg)

# Life Science

Next figures concern the concurrent execution of all LS collection queries (LS1 to LS7).


**ANAPSID traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/ANAPSID_LS_pairJoins_precision_all_traces.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/ANAPSID_LS_pairJoins_recall_all_mixages.jpeg)


**FedX traces**

![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/FEDX_LS_pairJoins_precision_all_mixages.jpeg)
![GitHub Logo](https://github.com/coumbaya/feta/blob/master/execution_figures/FEDX_LS_pairJoins_recall_all_mixages.jpeg)



# Non-similar

Next figures concern the concurrent execution of non similar queries from CD and LS collections, i.e. queries that have a null resultset for their common projected variables, namely: CD3, CD4, CD5, CD6, LS2 and LS3.

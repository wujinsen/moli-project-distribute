/*/过滤title=“title”的数据 Filter[] filterList = nul; TermQuery categoryQuery=new TermQuery(new Term("title","title");

Filter categoryFilter=new QueryWraperFilter(categoryQuery); query = new FilteredQuery(query, categoryFilter); ChainedFilter c = new ChainedFilter(filterList); / TopDocs tds = searcher.search(query,categoryFilter, firstResult + maxResult);/查询出前n条数

据的中间值*/


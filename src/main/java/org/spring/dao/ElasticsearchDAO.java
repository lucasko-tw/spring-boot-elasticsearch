package org.spring.dao;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.iii.aws.dao.ElasticsearchDAO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spring.config.ElasticsearchConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

@Service
public class ElasticsearchDAO {

	private Client client = null;

	@Autowired
	ElasticsearchConfig elasticsearchconfig;

	@PostConstruct
	public void init() {
		try {
			client = elasticsearchconfig.getClient();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("elasticsearch connection fail.");
			e.printStackTrace();
		}
	}
	

	public void saveJson(String index, String type, JSONObject esJSON) {

		IndexResponse response = client.prepareIndex(index, type)
		.setSource(esJSON.toString(), XContentType.JSON).get();

	}
	
	public JSONArray aggs(String index, String type, String field) {

		JSONArray array = new JSONArray();
		SearchResponse sr = client.prepareSearch(index).setTypes(type)
				.addAggregation(AggregationBuilders.terms("terms").field(field + ".keyword")).execute().actionGet();

		Terms agg = sr.getAggregations().get("terms");

		for (Terms.Bucket entry : agg.getBuckets()) {
			String key = entry.getKeyAsString(); // bucket key
			// long docCount = entry.getDocCount();
			array.put(key);
		}

		return array;
	}
	
	public SearchHits getEventHits(String index, String type ) {

	
		SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
		
		/*
		TermQueryBuilder termquery = QueryBuilders.termQuery("eventname", eventname);
		BoolQueryBuilder bq = new BoolQueryBuilder();
		bq.must(termquery);
		search.setQuery(termquery);
		 */
		
		search.setFrom(0).setSize(10000);
		SearchResponse response = search.execute().actionGet();
		SearchHits hits = response.getHits();
		return hits;
	}

	public void close() {
		if (client != null)
			client.close();
	}

	
	public boolean isIndexExists(String indexName) {
		return client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
	}

	public boolean isTypeExists(String indexName, String typeName) {

		ArrayList<String> types = this.getTypes(indexName);
		if (types.contains(typeName))
			return true;
		else
			return false;
	}
	public ArrayList<String> getTypes(String indexName) {
		ClusterStateResponse resp = client.admin().cluster().prepareState().execute().actionGet();
		ImmutableOpenMap<String, MappingMetaData> mappings = resp.getState().metaData().index(indexName).getMappings();
		ArrayList<String> list = new ArrayList<String>();
		for (ObjectObjectCursor<String, MappingMetaData> mapping : mappings) {
			String type = mapping.key;
			list.add(type);
		}
		return list;
	}
 
	 
	public SearchHits getHistogram(String index, String type, String date, String eventName) {

		TermQueryBuilder termDate = QueryBuilders.termQuery("date", date);
		TermQueryBuilder termEventname = QueryBuilders.termQuery("eventName.keyword", eventName);
		BoolQueryBuilder bq = new BoolQueryBuilder();
		bq.must(termDate);
		bq.must(termEventname);

		DateHistogramInterval interval = DateHistogramInterval.days(1);
		DateHistogramAggregationBuilder dateagg = AggregationBuilders.dateHistogram("dateagg").field("eventTime")
				.dateHistogramInterval(interval).minDocCount(0);

		SearchResponse response = client.prepareSearch(index).setTypes(type).setQuery(bq).setSize(1000)

				.addAggregation(dateagg).execute().actionGet();

		Histogram histogram = response.getAggregations().get("dateagg");

		JSONArray array = new JSONArray();

		SearchHits hits = response.getHits();
		return hits;
 

	}
 

	public void saveJsonById(String index, String type, String id, JSONObject esJSON) {

		client.prepareIndex(index, type, id).setSource(esJSON.toString()).execute().actionGet();

	}


	public void createIndex(String index) {

		client.admin().indices().prepareCreate(index).get();

	}
 
	public void deleteIndex(String index) {
		client.admin().indices().prepareDelete(index).get();
	}
 

}

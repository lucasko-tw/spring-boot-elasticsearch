package org.spring.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.elasticsearch.index.reindex.ScrollableHitSource.Hit;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spring.dao.ElasticsearchDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchService {

	@Autowired
	ElasticsearchDAO ES_DAO;

	private String INDEX = "log";
	private String TYPE = "user";

	/*
	 * public JSONArray getDates() {
	 * 
	 * String field = logs List<String> list = ES_DAO.aggs(INDEX , TYPE);
	 * 
	 * java.util.Collections.reverse(list); JSONArray array = new JSONArray(); for
	 * (String key : list) array.put(key);
	 * 
	 * return array;
	 * 
	 * }
	 */

	public JSONObject addLog() {

		JSONObject logJSON = new JSONObject();
		Random generator = new Random();
		int i = generator.nextInt(10) + 1;
		long timestamp = generator.nextLong();
		String username = "user" + String.valueOf(i);
		String[] list = { "Create", "Update", "Delete", "Query" };
		Random r = new Random();
		String eventname = list[r.nextInt(list.length)];

		logJSON.put("username", username);
		logJSON.put("timestamp", timestamp);
		logJSON.put("eventname", eventname);

		ES_DAO.saveJson(this.INDEX, this.TYPE, logJSON);

		return new JSONObject().put("success", true);
	}

	public JSONArray   listLog() {

		JSONArray array = new JSONArray();
		SearchHits hits = ES_DAO.getEventHits(this.INDEX, this.TYPE);
		for (SearchHit hit : hits )
		{
			JSONObject logJSON = new JSONObject(   hit.getSourceAsString() );
			array.put(logJSON);
		}
		return array ;

	}

}

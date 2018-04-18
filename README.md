
### Run Elasticsearch by Docker

```
git clone https://github.com/lucasko-tw/docker-compose-elasticsearch5.6.7.git

cd docker-compose-elasticsearch5.6.7

docker-compose up -d
```

### Check Settings for Spring-Boot Project

Setting for elasticsearch in src/main/resources/application.properties

```yml
elasticsearch.clustername = elasticsearch
elasticsearch.host = 127.0.0.1
elasticsearch.port = 9300
```


### Swagger2 

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

There are 2 apis : 

	1.  [http://localhost:8080/api/log/add](http://localhost:8080/api/log/add)
	
	2.  [http://localhost:8080/api/log/list](http://localhost:8080/api/log/list)
	
	

### Add Log API

receive request in controller (APIController.java)

```java 
@GetMapping(path = "/log/add", produces = MediaType.APPLICATION_JSON_VALUE)
public String addLog() {
	return ES_SERVICE.addLog().toString();
}
```


Randomly generate log json (ElasticsearchService.java)

```java
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
```

save json into elasticsearch  (ElasticsearchDAO.java)

```java
public void saveJson(String index, String type, JSONObject esJSON) {
IndexResponse response = client.prepareIndex(index, type)
.setSource(esJSON.toString(), XContentType.JSON)
.get();
}
```


### List Log API

Receive request in controller (APIController.java)

```java
@GetMapping(path = "/log/list", produces = MediaType.APPLICATION_JSON_VALUE)
public String listLog() {
	return ES_SERVICE.listLog().toString();
}

```


Call Dao function. (ElasticsearchService.java)

```java
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

```

search for log in elasticsearch (ElasticsearchDAO.java)

```java
public SearchHits getEventHits(String index, String type ) {

	SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
	
	search.setFrom(0).setSize(10000);
	SearchResponse response = search.execute().actionGet();
	SearchHits hits = response.getHits();
	return hits;
}
```







package org.spring.config;

import java.net.InetAddress;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;



@Component
public class ElasticsearchConfig {

	@Value("${elasticsearch.clustername}")
	public String ES_CLUSTERNAME;
	
	@Value("${elasticsearch.host}")
	public String ES_HOST;

	@Value("${elasticsearch.port}")
	public String ES_PORT;
 
	@Bean
	public Client getClient() throws Exception {

        Settings esSettings = Settings.builder()
                .put("cluster.name", ES_CLUSTERNAME)
                .build();
        
        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
      
        return new PreBuiltTransportClient(esSettings)
                .addTransportAddress(
				  new InetSocketTransportAddress(InetAddress.getByName(ES_HOST),Integer.parseInt( ES_PORT)));
    }

	
	
	 
	
	
}
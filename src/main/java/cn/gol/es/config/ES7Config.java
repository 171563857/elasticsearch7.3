package cn.gol.es.config;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ES7Config {
	private String ip;
	private int port;
	private String schema;
	private int connectTimeOut;
	private int socketTimeOut;
	private int connectionRequestTimeOut;
	private int maxConnectNum;
	private int maxConnectPerRoute;

	@Bean(name = "RestHighLevelClient")
	public RestHighLevelClient client() {
		RestClientBuilder builder = RestClient.builder(new HttpHost(ip, port));
		// 异步httpclient连接延时配置
		builder.setRequestConfigCallback(requestConfigBuilder -> {
			requestConfigBuilder.setConnectTimeout(connectTimeOut);
			requestConfigBuilder.setSocketTimeout(socketTimeOut);
			requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
			return requestConfigBuilder;
		});
		// 异步httpclient连接数配置
		builder.setHttpClientConfigCallback(httpClientBuilder -> {
			httpClientBuilder.setMaxConnTotal(maxConnectNum);
			httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
			return httpClientBuilder;
		});
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}
}

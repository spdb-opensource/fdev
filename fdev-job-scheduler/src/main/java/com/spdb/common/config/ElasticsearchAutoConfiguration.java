package com.spdb.common.config;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import com.spdb.common.config.ElasticsearchProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *Elasticsearch client 配置
 */
@Configuration
public class ElasticsearchAutoConfiguration {

	@Autowired
	private ElasticsearchProperties elasticsearchProperties;

	private List<HttpHost> httpHosts = new ArrayList<>();

	@Bean
	@ConditionalOnMissingBean
	public RestHighLevelClient restHighLevelClient() {

		List<String> clusterNodes = Arrays.asList(elasticsearchProperties.getClusterNodes().split(","));
		clusterNodes.forEach(node -> {
			try {
				String[] parts = StringUtils.split(node, ":");
				Assert.notNull(parts, "Must defined");
				Assert.state(parts.length == 2, "Must be defined as 'host:port'");
				httpHosts.add(new HttpHost(parts[0], Integer.parseInt(parts[1]), elasticsearchProperties.getSchema()));
			} catch (Exception e) {
				throw new IllegalStateException("Invalid ES nodes " + "property '" + node + "'", e);
			}
		});


		RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));

		return getRestHighLevelClient(builder, elasticsearchProperties);
	}


	/**
	 * get restHistLevelClient
	 *
	 * @param builder                 RestClientBuilder
	 * @param elasticsearchProperties elasticsearch default properties
	 * @return {@link RestHighLevelClient}
	 * @author fxbin
	 */
	private static RestHighLevelClient getRestHighLevelClient(RestClientBuilder builder, ElasticsearchProperties elasticsearchProperties) {

		// Callback used the default {@link RequestConfig} being set to the {@link CloseableHttpClient}
		builder.setRequestConfigCallback(requestConfigBuilder -> {
			requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeout());
			requestConfigBuilder.setSocketTimeout(elasticsearchProperties.getSocketTimeout());
			requestConfigBuilder.setConnectionRequestTimeout(elasticsearchProperties.getConnectionRequestTimeout());
			return requestConfigBuilder;
		});

		// Callback used to customize the {@link CloseableHttpClient} instance used by a {@link RestClient} instance.
		builder.setHttpClientConfigCallback(httpClientBuilder -> {

			ConnectionKeepAliveStrategy connectStrategy  =	new ConnectionKeepAliveStrategy() {
				@Override
				public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
					HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
					while(it.hasNext()) {
						HeaderElement he = it.nextElement();
						String param = he.getName();
						String value = he.getValue();
						if (value!=null && param.equalsIgnoreCase("timeout")) {
							return Long.parseLong(value);
						}
					}
					return elasticsearchProperties.getKeepAliveTime();//set default keepalive time
				}
			};
			httpClientBuilder.setKeepAliveStrategy(connectStrategy);
			httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnectTotal());
			httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnectPerRoute());
			// Callback used the basic credential auth
			/*if (!StringUtils.isEmpty(elasticsearchProperties.getUsername()) && !StringUtils.isEmpty(elasticsearchProperties.getUsername())) {
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider
				.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword()));
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}*/
			return httpClientBuilder;
		});
		return new RestHighLevelClient(builder);
	}



}

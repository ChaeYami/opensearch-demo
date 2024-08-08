package com.example.opensearchdemo;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;

@Configuration
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient() throws Exception {
        // TrustStore 설정
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/SCY/Desktop/opensearch-demo/mytruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "mypassword");

        final HttpHost host = new HttpHost("https", "211.253.36.208", 9200);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials("admin", "Pass123Word456!!".toCharArray()));

        final SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chains, authType) -> true) // 모든 인증서 신뢰
                .build();

        /*final SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new File("C:/Users/SCY/mytruststore.jks"), "mypassword".toCharArray())
                .build();*/



        final ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            final TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslContext)
                    .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                        @Override
                        public TlsDetails create(final SSLEngine sslEngine) {
                            return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                        }
                    })
                    .build();

            final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                    .create()
                    .setTlsStrategy(tlsStrategy)
                    .build();

            return httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager);
        });

        final OpenSearchTransport transport = builder.build();
        return new OpenSearchClient(transport);
    }
}

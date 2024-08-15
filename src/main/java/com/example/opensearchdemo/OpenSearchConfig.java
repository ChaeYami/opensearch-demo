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
import java.io.FileInputStream;
import java.security.KeyStore;

@Configuration
public class OpenSearchConfig {

    // host 설정

    private static final String SCHEME = "http";

    private static final String HOST = "211.254.212.171";

//    private static final String SCHEME = "https";
//    private static final String HOST = "211.253.36.208";

    // port 설정
    private static final int PORT = 9200;

    // 인증 정보
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
/*
    // ssl/tls 설정
    private static final String TRUSTSTOREDIR = "C:/Users/SCY/mytruststore.jks";
    private static final String TRUSTSTOREPW = "mypassword";

    private static final String KEYSTOREDIR = "C:/Users/SCY/mykeystore.jks";
    private static final String KEYSTOREPW = "mypassword";
*/


    /**
     * OpenSearchClient Bean 설정
     *
     * @return OpenSearchClient
     */


    /*@Bean
    public OpenSearchClient openSearchClient() throws Exception {
        // TrustStore 설정
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTOREDIR);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTOREPW);

        // Load KeyStore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream keyStoreStream = new FileInputStream(KEYSTOREDIR)) {
            keyStore.load(keyStoreStream, KEYSTOREPW.toCharArray());
        }


        // TrustStore와 KeyStore를 사용하여 SSLContext 설정
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(new File(TRUSTSTOREDIR), TRUSTSTOREPW.toCharArray())
                .loadKeyMaterial(new File(KEYSTOREDIR), KEYSTOREPW.toCharArray(), KEYSTOREPW.toCharArray())
                .build();

       *//* final SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chains, authType) -> true) // 모든 인증서 신뢰
                .build();*//*

        final HttpHost host = new HttpHost(SCHEME, HOST, PORT);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(USERNAME, PASSWORD.toCharArray()));


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
    }*/
    @Bean
    public OpenSearchClient openSearchClient() {

        final HttpHost httpHost = new HttpHost(SCHEME, HOST, PORT);

        // 인증 정보를 설정
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(httpHost),
                new UsernamePasswordCredentials(USERNAME, PASSWORD.toCharArray()));

        // OpenSearch와 통신하기 위한 OpenSearchTransport 객체를 생성
        final OpenSearchTransport transport =
                ApacheHttpClient5TransportBuilder.builder(httpHost)
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider)).build();

        return new OpenSearchClient(transport);
    }

}

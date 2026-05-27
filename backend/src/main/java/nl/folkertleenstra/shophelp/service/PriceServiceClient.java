package nl.folkertleenstra.shophelp.service;

import nl.folkertleenstra.shophelp.dto.LiveSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Calls the Python price-service (FastAPI + SupermarktConnector) over the
 * Docker network to fetch live supermarket prices.
 */
@Service
public class PriceServiceClient {

    private final RestClient restClient;

    public PriceServiceClient(@Value("${price-service.url}") String baseUrl) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(15).toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public LiveSearchResponse search(String query, String chain, int size) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("query", query)
                        .queryParam("chain", chain)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(LiveSearchResponse.class);
    }
}

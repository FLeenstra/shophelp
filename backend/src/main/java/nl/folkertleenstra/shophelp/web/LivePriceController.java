package nl.folkertleenstra.shophelp.web;

import nl.folkertleenstra.shophelp.dto.LiveSearchResponse;
import nl.folkertleenstra.shophelp.service.PriceServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** Live price lookups proxied to the Python price-service. */
@RestController
@RequestMapping("/api/live")
public class LivePriceController {

    private final PriceServiceClient priceServiceClient;

    public LivePriceController(PriceServiceClient priceServiceClient) {
        this.priceServiceClient = priceServiceClient;
    }

    @GetMapping("/search")
    public LiveSearchResponse search(
            @RequestParam String query,
            @RequestParam(defaultValue = "all") String chain,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return priceServiceClient.search(query, chain, size);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "price-service unavailable: " + e.getMessage());
        }
    }
}

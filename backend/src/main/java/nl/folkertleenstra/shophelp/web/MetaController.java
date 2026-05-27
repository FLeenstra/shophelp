package nl.folkertleenstra.shophelp.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Frontend-facing metadata, e.g. whether displayed prices are stub/seed data. */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @Value("${shophelp.prices-stubbed:true}")
    private boolean pricesStubbed;

    @GetMapping
    public Map<String, Object> meta() {
        return Map.of("pricesStubbed", pricesStubbed);
    }
}

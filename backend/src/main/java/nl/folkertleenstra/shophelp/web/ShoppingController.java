package nl.folkertleenstra.shophelp.web;

import jakarta.validation.Valid;
import nl.folkertleenstra.shophelp.dto.CompareRequest;
import nl.folkertleenstra.shophelp.dto.RoutePlan;
import nl.folkertleenstra.shophelp.dto.RouteRequest;
import nl.folkertleenstra.shophelp.dto.StoreTotal;
import nl.folkertleenstra.shophelp.service.ComparisonService;
import nl.folkertleenstra.shophelp.service.RouteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShoppingController {

    private final ComparisonService comparisonService;
    private final RouteService routeService;

    public ShoppingController(ComparisonService comparisonService, RouteService routeService) {
        this.comparisonService = comparisonService;
        this.routeService = routeService;
    }

    /** Compare a basket's total cost across stores. */
    @PostMapping("/basket/compare")
    public List<StoreTotal> compare(@Valid @RequestBody CompareRequest request) {
        return comparisonService.compare(request);
    }

    /** Plan a map route that buys every item at its cheapest store. */
    @PostMapping("/route/plan")
    public RoutePlan plan(@Valid @RequestBody RouteRequest request) {
        return routeService.plan(request);
    }
}

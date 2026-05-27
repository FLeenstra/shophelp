package nl.folkertleenstra.shophelp.service;

import nl.folkertleenstra.shophelp.dto.BasketItem;
import nl.folkertleenstra.shophelp.dto.RoutePlan;
import nl.folkertleenstra.shophelp.dto.RouteRequest;
import nl.folkertleenstra.shophelp.dto.RouteStop;
import nl.folkertleenstra.shophelp.model.Store;
import nl.folkertleenstra.shophelp.model.StorePrice;
import nl.folkertleenstra.shophelp.repo.StorePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final StorePriceRepository storePriceRepository;

    public RouteService(StorePriceRepository storePriceRepository) {
        this.storePriceRepository = storePriceRepository;
    }

    /**
     * Buys each item at its cheapest store, then orders the resulting set of
     * stores into a visiting route using a nearest-neighbour heuristic starting
     * from the shopper's location.
     */
    @Transactional(readOnly = true)
    public RoutePlan plan(RouteRequest request) {
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();
        for (BasketItem item : request.items()) {
            quantityByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        List<StorePrice> prices = storePriceRepository.findByProductIdIn(quantityByProduct.keySet());

        // Cheapest store price per product.
        Map<Long, StorePrice> cheapestByProduct = new HashMap<>();
        for (StorePrice sp : prices) {
            cheapestByProduct.merge(sp.getProduct().getId(), sp, (a, b) ->
                    a.getPrice().compareTo(b.getPrice()) <= 0 ? a : b);
        }

        // Aggregate per store: subtotal and the items to buy there.
        Map<Long, Store> stores = new LinkedHashMap<>();
        Map<Long, BigDecimal> subtotalByStore = new HashMap<>();
        Map<Long, List<String>> itemsByStore = new HashMap<>();
        BigDecimal estimatedTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, StorePrice> entry : cheapestByProduct.entrySet()) {
            StorePrice sp = entry.getValue();
            int qty = quantityByProduct.get(entry.getKey());
            Store store = sp.getStore();
            BigDecimal lineCost = sp.getPrice().multiply(BigDecimal.valueOf(qty));

            stores.putIfAbsent(store.getId(), store);
            subtotalByStore.merge(store.getId(), lineCost, BigDecimal::add);
            itemsByStore.computeIfAbsent(store.getId(), k -> new ArrayList<>())
                    .add(qty + "x " + sp.getProduct().getName());
            estimatedTotal = estimatedTotal.add(lineCost);
        }

        // Nearest-neighbour ordering from the start location.
        List<Store> remaining = new ArrayList<>(stores.values());
        List<RouteStop> stops = new ArrayList<>();
        double currentLat = request.startLat();
        double currentLng = request.startLng();
        double totalDistanceKm = 0.0;
        int order = 1;

        while (!remaining.isEmpty()) {
            Store nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Store candidate : remaining) {
                double d = haversineKm(currentLat, currentLng, candidate.getLatitude(), candidate.getLongitude());
                if (d < nearestDistance) {
                    nearestDistance = d;
                    nearest = candidate;
                }
            }
            remaining.remove(nearest);
            totalDistanceKm += nearestDistance;
            currentLat = nearest.getLatitude();
            currentLng = nearest.getLongitude();

            stops.add(new RouteStop(
                    order++,
                    nearest.getId(),
                    nearest.getName(),
                    nearest.getLatitude(),
                    nearest.getLongitude(),
                    subtotalByStore.get(nearest.getId()),
                    itemsByStore.get(nearest.getId())));
        }

        return new RoutePlan(stops, estimatedTotal, round(totalDistanceKm));
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

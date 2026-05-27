package nl.folkertleenstra.shophelp.service;

import nl.folkertleenstra.shophelp.dto.BasketItem;
import nl.folkertleenstra.shophelp.dto.CompareRequest;
import nl.folkertleenstra.shophelp.dto.StoreTotal;
import nl.folkertleenstra.shophelp.model.Store;
import nl.folkertleenstra.shophelp.model.StorePrice;
import nl.folkertleenstra.shophelp.repo.StorePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComparisonService {

    private final StorePriceRepository storePriceRepository;

    public ComparisonService(StorePriceRepository storePriceRepository) {
        this.storePriceRepository = storePriceRepository;
    }

    /**
     * Computes the basket total at every store that carries at least one item.
     * Stores that carry the whole basket come first, then cheapest total first.
     */
    @Transactional(readOnly = true)
    public List<StoreTotal> compare(CompareRequest request) {
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();
        for (BasketItem item : request.items()) {
            quantityByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }
        int itemsRequested = quantityByProduct.size();

        List<StorePrice> prices = storePriceRepository.findByProductIdIn(quantityByProduct.keySet());

        Map<Long, BigDecimal> totalByStore = new HashMap<>();
        Map<Long, Integer> availableByStore = new HashMap<>();
        Map<Long, Store> stores = new HashMap<>();

        for (StorePrice sp : prices) {
            Integer qty = quantityByProduct.get(sp.getProduct().getId());
            if (qty == null) {
                continue;
            }
            Store store = sp.getStore();
            stores.putIfAbsent(store.getId(), store);
            BigDecimal lineCost = sp.getPrice().multiply(BigDecimal.valueOf(qty));
            totalByStore.merge(store.getId(), lineCost, BigDecimal::add);
            availableByStore.merge(store.getId(), 1, Integer::sum);
        }

        List<StoreTotal> result = new ArrayList<>();
        for (Store store : stores.values()) {
            int available = availableByStore.getOrDefault(store.getId(), 0);
            result.add(new StoreTotal(
                    store.getId(),
                    store.getName(),
                    totalByStore.get(store.getId()),
                    available,
                    itemsRequested,
                    available == itemsRequested));
        }

        result.sort(Comparator
                .comparing(StoreTotal::complete).reversed()
                .thenComparing(StoreTotal::total));
        return result;
    }
}

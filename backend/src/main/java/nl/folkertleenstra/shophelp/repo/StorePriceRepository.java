package nl.folkertleenstra.shophelp.repo;

import nl.folkertleenstra.shophelp.model.StorePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface StorePriceRepository extends JpaRepository<StorePrice, Long> {

    @Query("select sp from StorePrice sp join fetch sp.store where sp.product.id = :productId order by sp.price asc")
    List<StorePrice> findByProductIdOrderByPrice(Long productId);

    @Query("select sp from StorePrice sp join fetch sp.store join fetch sp.product where sp.product.id in :productIds")
    List<StorePrice> findByProductIdIn(Collection<Long> productIds);
}

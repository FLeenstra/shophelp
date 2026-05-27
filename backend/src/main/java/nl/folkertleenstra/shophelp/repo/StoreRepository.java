package nl.folkertleenstra.shophelp.repo;

import nl.folkertleenstra.shophelp.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}

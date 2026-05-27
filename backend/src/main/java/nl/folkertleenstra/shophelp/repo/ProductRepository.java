package nl.folkertleenstra.shophelp.repo;

import nl.folkertleenstra.shophelp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

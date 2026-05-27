package nl.folkertleenstra.shophelp.web;

import nl.folkertleenstra.shophelp.dto.ProductPriceView;
import nl.folkertleenstra.shophelp.model.Product;
import nl.folkertleenstra.shophelp.repo.ProductRepository;
import nl.folkertleenstra.shophelp.repo.StorePriceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final StorePriceRepository storePriceRepository;

    public ProductController(ProductRepository productRepository, StorePriceRepository storePriceRepository) {
        this.productRepository = productRepository;
        this.storePriceRepository = storePriceRepository;
    }

    @GetMapping
    public List<Product> all() {
        return productRepository.findAll();
    }

    /** Every store's price for one product, cheapest first. */
    @GetMapping("/{id}/prices")
    public List<ProductPriceView> prices(@PathVariable Long id) {
        return storePriceRepository.findByProductIdOrderByPrice(id).stream()
                .map(sp -> new ProductPriceView(
                        sp.getStore().getId(),
                        sp.getStore().getName(),
                        sp.getPrice(),
                        sp.getCurrency()))
                .toList();
    }
}

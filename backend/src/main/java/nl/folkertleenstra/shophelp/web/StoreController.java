package nl.folkertleenstra.shophelp.web;

import nl.folkertleenstra.shophelp.model.Store;
import nl.folkertleenstra.shophelp.repo.StoreRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreRepository storeRepository;

    public StoreController(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @GetMapping
    public List<Store> all() {
        return storeRepository.findAll();
    }
}

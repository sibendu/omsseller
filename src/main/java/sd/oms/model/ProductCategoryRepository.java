package sd.oms.model;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ProductCategoryRepository extends CrudRepository<ProductCategory, Long> {
	
	List<ProductCategory> findBySeller(Long seller);  
	
}

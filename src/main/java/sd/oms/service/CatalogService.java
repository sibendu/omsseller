package sd.oms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sd.oms.model.ProductCategory;
import sd.oms.model.ProductCategoryRepository;

@Service
public class CatalogService {

	@Autowired
	private ProductCategoryRepository categoryRepository;
	
	public void save(ProductCategory category) throws Exception {
		categoryRepository.save(category);
	}
	
	public void deleteAll() throws Exception {
		categoryRepository.deleteAll();
	}
	
	public ProductCategory findCatalog(Long seller) throws Exception {
		
		ProductCategory root = null;
		
		Iterable<ProductCategory> categories = categoryRepository.findBySeller(seller);
		
		long id = 0;
		
		for (ProductCategory productCategory : categories) {
			
			id = productCategory.getId().longValue();
			
			//nest all categories under this
			for (ProductCategory cat : categories) {
				if(cat.getParentId() != null && cat.getParentId().longValue() == id) {
					productCategory.addCategory(cat);
				}
			}
			
			if(productCategory.getParentId() == null) {
				root = productCategory;
			}
		}
		
		if(root == null) {
			throw new Exception("Root not found");
		}
		
		return root;
	}
}

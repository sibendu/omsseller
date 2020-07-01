package sd.oms;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import sd.oms.model.Customer;
import sd.oms.model.CustomerRepository;
import sd.oms.model.ProductCategory;
import sd.oms.model.ProductCategoryRepository;
import sd.oms.model.SKUItem;
import sd.oms.service.CatalogService;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/catalog")
public class CategoryController {

	@Autowired
	private CatalogService catalogService;

	@RequestMapping(value = "/{seller}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> findCatalog(@PathVariable(name = "seller", required = true) Long seller)
			throws Exception {

		ResponseEntity<String> response;

		ProductCategory root = catalogService.findCatalog(seller);

		String productJson = root.toJSON();

		response = new ResponseEntity<String>(productJson, HttpStatus.OK);
		return response;
	}

	@GetMapping(path = "/clean") //
	public @ResponseBody String cleanCatalog() throws Exception{
		catalogService.deleteAll();
		return "All catalog deleted";
	}

}
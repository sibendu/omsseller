package sd.oms;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import sd.oms.util.OMSUtil;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/customer")
public class UserController {

	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ProductCategoryRepository categoryRepository;

	@RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getMessage() {
		ResponseEntity<String> response = null;
		response = new ResponseEntity<>("UserService is live", HttpStatus.OK);
		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ArrayList<Customer>> findCustomers() {
		ResponseEntity<ArrayList<Customer>> response = null;
		ArrayList<Customer> allCustomers = new ArrayList<>();
		Iterable<Customer> customers = customerRepository.findAll();
		for (Customer cust : customers) {
			allCustomers.add(cust);
		}
		System.out.println("# of users = " + allCustomers.size());
		response = new ResponseEntity<ArrayList<Customer>>(allCustomers, HttpStatus.OK);
		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Customer> findById(@PathVariable(name = "id", required = true) Long id) {

		ResponseEntity<Customer> response = null;
		Customer result = null;
		Iterable<Customer> customers = customerRepository.findAll();
		for (Customer cust : customers) {
			if (cust.getId().longValue() == id.longValue()) {
				result = cust;
			}
		}
		response = new ResponseEntity<Customer>(result, HttpStatus.OK);
		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Customer> addCustomer(@RequestBody Customer cust) {
		ResponseEntity<Customer> response = null;
		System.out.println("Order Processed. Returning response...");
		return response;
	}

	@GetMapping(path = "/addcustomer") //
	public @ResponseBody Customer addCustomer(@RequestParam(required = true) String name,
			@RequestParam(required = true) String phone, @RequestParam(required = false) String type,
			@RequestParam(required = false) Long seller, @RequestParam(required = false) String password,
			@RequestParam(required = false) String email, @RequestParam(required = false) String address) {

		if (type != null) {
			type = "SELLER";
			seller = null;
		} else {
			type = "CUSTOMER";
		}

		password = (password == null || password.trim().equals("")) ? "password" : password;

		Customer cust = new Customer(null, type, seller, password, name, phone, email, address, new Date());

		customerRepository.save(cust);

		if (type.equals("CUSTOMER")) {
			System.out.println("Registered customer successfully");

			if (seller != null) {
				Optional<Customer> sellerUser = customerRepository.findById(seller);
				sellerUser.ifPresent(s -> {
					System.out.println("Customer user registered, and mapped to seller " + s.getName());
				});

				if (!sellerUser.isPresent()) {
					System.out
							.println("Customer user registered; but was not mapped to a seller. seller in request was: "
									+ seller);
				}
			}
		} else {
			System.out.println("Registered seller successfully");
		}

		return cust;
	}

	@GetMapping(path = "/seed") //
	public @ResponseBody String seedUser() throws Exception {
		customerRepository.deleteAll();

		//String name,String phone,String type,Long seller, String password,String email,String address
		
		Customer seller1 = addCustomer("Mana", "1111", OMSUtil.USER_TYPE_SELLER , null, null, null, null);
		Customer seller2 = addCustomer("Sibendu Das", "2222", OMSUtil.USER_TYPE_SELLER, null, null, null, null);

		Customer cust1 = addCustomer("Arghya Dutta", "3333", OMSUtil.USER_TYPE_CUSTOMER, seller2.getId(), "password", "arghyadutta.19@gmail.com" , "Gournagar, Thakurpukur, Kolkata - 63");
		Customer cust2 = addCustomer("Mehul Das", "4444", OMSUtil.USER_TYPE_CUSTOMER, seller2.getId(), "password", "sibendu.das@gmail.com", "Gournagar, Thakurpukur, Kolkata - 63"); // mapped to seller
																								// Bappa
		Customer cust3 = addCustomer("Dipanjana Naha", "5555", OMSUtil.USER_TYPE_CUSTOMER, seller2.getId(), "password", "dipanjanan@gmail.com", "Sakherbazar, Kolkata - 61"); // mapped to absent
																								// seller
		Customer cust4 = addCustomer("Nilanjan Das", "6666", OMSUtil.USER_TYPE_CUSTOMER, seller2.getId(), "password", "nilanjan.31@gmail.com", "Tollygunge, Kolkata"); // mapped to absent
		// seller
		Customer cust5 = addCustomer("Nilotpal Ghosh", "7777", OMSUtil.USER_TYPE_CUSTOMER, seller2.getId(), "password", "nilotpalghosh@gmail.com", "Oxytown, Kolkata"); // mapped to absent
		// seller

		// Seed only root catalog data
		seedRootCatalogOnly(seller1.getId());
				
		// Seed Dummy catalog data
		seedDummyCatalog(seller2.getId());

		return "OMS data seeded successfully";
	}
	
	public void seedRootCatalogOnly(Long seller) throws Exception {
		ProductCategory rootCatalog = new ProductCategory(null, seller, "Root Catalog", "Root Catalog", null, null);
		catalogService.save(rootCatalog);
		System.out.println("Seeded root catalog: " + rootCatalog.getId()+" for seller user: "+seller);
	}

	public void seedDummyCatalog(Long seller) throws Exception {

		ProductCategory cat = null;
		SKUItem item = null;

		ProductCategory rootCatalog = new ProductCategory(null, seller, "Root Catalog", "Root Catalog", null, null);
		catalogService.save(rootCatalog);
		System.out.println("Saved root catalog: " + rootCatalog.getId());

		ProductCategory cat1 = new ProductCategory(null, seller, "Super Category 10", "Super Category 10",
				rootCatalog.getId(), null);
		ProductCategory cat2 = new ProductCategory(null, seller, "Super Category 20", "Super Category 20",
				rootCatalog.getId(), null);
		ProductCategory cat3 = new ProductCategory(null, seller, "Super Category 30", "Super Category 30",
				rootCatalog.getId(), null);
		ProductCategory cat4 = new ProductCategory(null, seller, "Super Category 40", "Super Category 40",
				rootCatalog.getId(), null);

		catalogService.save(cat1);
		catalogService.save(cat2);
		catalogService.save(cat3);
		catalogService.save(cat4);
		System.out.println("Saved level 1 categories");

		Long parentId = null;

		for (int i = 0; i < 30; i++) {

			if (i < 5) {
				parentId = cat1.getId();
			} else if (i >= 5 && i < 12) {
				parentId = cat2.getId();
			} else if (i >= 12 && i < 20) {
				parentId = cat3.getId();
			} else {
				parentId = cat4.getId();
			}

			int catId = (i + 1) * 100;

			cat = new ProductCategory(null, seller, "Category-" + catId, "Category Description-" + catId, parentId,
					null);

			for (int k = 1; k < 25; k++) {

				long val = catId + 10 + k;

				item = new SKUItem(null, "sku-" + val, "Item -" + val, "Item Description :::: " + val, new Double(val),
						"kg", new Double(1), new Double(5), new Double(0.5), new Double(2), cat);

				cat.addItem(item);
			}

			catalogService.save(cat);
			System.out.println("Saved category " + cat.getId() + "for parent = " + parentId);

		}
	}

	@GetMapping(path = "/clean") //
	public @ResponseBody String cleanCustomer() {
		customerRepository.deleteAll();
		return "All users deleted";
	}

}
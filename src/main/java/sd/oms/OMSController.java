package sd.oms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sd.oms.dataobject.*;
import sd.oms.model.*;
import sd.oms.service.*;
import sd.oms.util.OMSUtil;

@Controller
public class OMSController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	CategoryRepository categoryRepository;

	@PostMapping("/login")
	public String login(@RequestParam(name = "phone", required = true) String phone, Model model,
			HttpSession httpSession) {

		System.out.println("login :: " + phone);
		List<Customer> customers = customerRepository.findByPhone(phone);

		Customer cust = null;
		if (customers != null && customers.size() > 0) {
			for (int i = 0; i < customers.size(); i++) {
				if (customers.get(i).getPhone().equals(phone)) {
					cust = customers.get(i);
				}
			}
		}

		System.out.println("Customer record :: " + cust);

		if (cust != null) {

			httpSession.setAttribute("customer", cust);
			
			String IMAGE_URL = OMSUtil.IMAGE_BASE_URL +cust.getId() + "/";  
			
			httpSession.setAttribute("IMAGE_URL", IMAGE_URL);
			httpSession.setAttribute("IMAGE_URL_SUFFIX", OMSUtil.IMAGE_URL_SUFFIX);

			if (cust.getType() != null && !OMSUtil.USER_TYPE_SELLER.equals(cust.getType())) {
				model.addAttribute("message",
						"You are not authorized to access this applicaiton. Please contact Admin.");
				return "error";
			}

			try {
				ProductCategory root = catalogService.findCatalog(cust.getId()); // getJson(); Test.getDummyCatalog();

				String productsJson = root.toJSON();

				httpSession.setAttribute("products", productsJson);

				System.out.println("Catalog saved in session: " + productsJson);

			} catch (Exception e) {
				model.addAttribute("message", "Your products could not be retreived. Please contact Admin.");
				return "error";
			}

		} else { // No customer record found
			model.addAttribute("message", "Invalid login. Please contact Admin.");
			return "error";
		}

		return showHome(model, httpSession);
	}
	
	
	@GetMapping("/search")
	public String showSearchForm(Model model, HttpSession httpSession) {
		System.out.println("Going to Search");
		
		SearchDTO search = new SearchDTO();
		
		Customer cust = (Customer) httpSession.getAttribute("customer");
		search.setSeller(cust.getId());
		
		List<Customer> myCustomers = customerRepository.findBySeller(cust.getId());
		search.setCustomers(myCustomers);
		
		model.addAttribute("search", search);
		
		return "search";
	}
	
	@PostMapping("/search")
	public String showSearchResult( @ModelAttribute SearchDTO search, Model model,
			HttpSession httpSession) {
		
		System.out.println("Searching orders for: "+ search.getSeller()+ " :: "+search.getCustomerId()+" :: "+search.getOrderNum());
		
		Customer cust = (Customer) httpSession.getAttribute("customer");
		search.setSeller(cust.getId());		
		List<Customer> myCustomers = customerRepository.findBySeller(cust.getId());
		search.setCustomers(myCustomers);
		
		model.addAttribute("search", search);		
	
		ArrayList<Order> result;
		try {
			result = orderService. search(search.getSeller(), search.getCustomerId(), search.getOrderNum()); 
			model.addAttribute("orders", result);
			
			System.out.println("Found: "+result.size());
			if(result.size() == 0) {
				model.addAttribute("message", "There are no results matching criteria.");
			}
		} catch (Exception e) {
			model.addAttribute("message", "There is some error in search: "+e.getMessage()+" . Please contact your Admin if the error persists.");
		}
		
		return "search";
	}	

	@GetMapping("/home")
	public String showHome(Model model, HttpSession httpSession) {
		System.out.println("Going to home");

		Customer cust = (Customer) httpSession.getAttribute("customer");
		System.out.println("Cust from session = " + cust.getId());

		ArrayList[] orders = orderService.findOrders(cust.getId());

		model.addAttribute("orders", orders[0]);
		model.addAttribute("otherOrders", orders[1]);

		System.out.println("Found all orders for : " + cust.getId());

		return "home";
	}

	@GetMapping("/customer")
	public String showCustomer(Model model, HttpSession httpSession) {
		System.out.println("Going to customers");

		Customer cust = (Customer) httpSession.getAttribute("customer");

		List<Customer> myCustomers = customerRepository.findBySeller(cust.getId());

		model.addAttribute("customers", myCustomers);

		System.out.println("Found customers for seller id " + cust.getId() + " : " + myCustomers.size());

		return "customer";
	}

	@GetMapping("/product")
	public String showProducts(Model model, HttpSession httpSession) {
		System.out.println("Going to products");
		return "product";
	}

	@GetMapping("/order")
	public String showNewOrder(Model model, HttpSession httpSession) {
		System.out.println("Going to create order");
		return "order";
	}

	@GetMapping("/cart")
	public String showCart(Model model, HttpSession httpSession) {
		System.out.println("Going to cart");

		Customer cust = (Customer) httpSession.getAttribute("customer");

		List<Customer> myCustomers = customerService.findCustomer(cust.getId());
		model.addAttribute("mycustomers", myCustomers);
		System.out.println("No of customers found for seller = " + myCustomers.size());

		return "cart";
	}

	@GetMapping("/seller")
	public String showSeller(Model model, HttpSession httpSession) {

		System.out.println("Going to seller - my profile");

		Customer cust = (Customer) httpSession.getAttribute("customer");
		model.addAttribute("seller", cust);

		return "seller";
	}

	@PostMapping("/order")
	public String submitOrder(@RequestParam(name = "order", required = true) String orderPayload, Model model,
			HttpSession httpSession) {

		System.out.println("Received order :: " + orderPayload);

		Customer cust = (Customer) httpSession.getAttribute("customer");

		final GsonBuilder gsonBuilder = new GsonBuilder();
		final Gson gson = gsonBuilder.create();

		// ItemDTO[] items = gson.fromJson(orderPayload, ItemDTO[].class);
		OrderDTO inputOrder = gson.fromJson(orderPayload, OrderDTO.class);
		ItemDTO[] items = inputOrder.getItems();
		System.out.println("Order has = " + items.length + " items; customer: " + inputOrder.getCustomer() + " inst = "
				+ inputOrder.getInstruction());
		for (int i = 0; i < items.length; i++) {
			System.out.println(items[i].getDescription());
		}

		Optional<Customer> buyer = customerRepository.findById(inputOrder.getCustomer());

		double totalPrice = 0;

		Order order = new Order(null, inputOrder.getInstruction(), OMSUtil.ORDER_STATUS_NEW, null,
				inputOrder.getCustomer(), cust.getId(), null, buyer.get().getName());

		for (ItemDTO item : items) {
			totalPrice = totalPrice + item.getPrice() * item.getQuantity();
			order.getItems()
					.add(new Item(null, item.getCode(), item.getName(), item.getDescription(), item.getQuantity(),
							item.getPrice(), item.getUnit(), null, null, OMSUtil.ITEM_STATUS_ORDERED, order));
		}

		order.setTotalPrice(new Double(totalPrice));

		orderRepository.save(order);
		System.out.println("Order created successfully :: " + order.getId());

		// Send order email - this is to seller himself/herself
		try {
			if (cust.getEmail() != null && cust.getEmail().indexOf("@") != -1) {
				catalogService.sendOrderEmail(cust.getEmail(), order, buyer.get());
				System.out.println("Order emailed successfully.");
			}
		} catch (Exception e) {
			System.out.println("Order email failure: " + e.getMessage());
			e.printStackTrace();
		}

		model.addAttribute("clearCart", true);

		model.addAttribute("message",
				"Order# " + order.getId() + " created sucessfully for customer id = " + inputOrder.getCustomer());

		return showHome(model, httpSession);
	}

	@GetMapping("/logout")
	public String logout(Model model, HttpSession httpSession) {
		httpSession.removeAttribute("customer");
		httpSession.invalidate();
		System.out.println("Logging off.");
		return "redirect:/";
	}

	@GetMapping("/order/view/{id}")
	public String viewOrder(@PathVariable(name = "id", required = true) String id, Order order, Model model,
			HttpSession httpSession) {

		System.out.println("viewOrder :: order_id = " + id);
		Optional<Order> thisOrder = orderRepository.findById(new Long(id));

		if (thisOrder.isPresent()) {
			System.out.println("Order retrieved");
			order = thisOrder.get();
			model.addAttribute("order", order);

			Optional<Customer> cust = customerRepository.findById(order.getCustomerId());
			if (cust.isPresent()) {
				model.addAttribute("customer", cust.get());
			}
		}

		return "orderdetails";
	}

	@PostMapping("/processorder")
	public String processOrder(@ModelAttribute Order inputOrder, Model model, HttpSession httpSession) {

		System.out.println("Updating order: " + inputOrder + "  " + inputOrder.getId() + "  " + inputOrder.getStatus());

		Optional<Order> ord = orderRepository.findById(inputOrder.getId());

		if (ord.isPresent()) {
			Order order = ord.get();

			order.setRemarks(inputOrder.getRemarks());
			order.setStatus(inputOrder.getStatus());

			orderRepository.save(order);
			System.out.println("Order updated successfully :: " + order.getId());

			Optional<Customer> cust = customerRepository.findById(order.getCustomerId());
			if (cust.isPresent()) {
				model.addAttribute("customer", cust.get());
			}

			model.addAttribute("order", order);
			model.addAttribute("message", "Order updated successfully.");

		} else {
			System.out.println("Order could not be found !!! id = " + inputOrder.getId());
			showHome(model, httpSession);
		}

		return "orderdetails";
	}

	@GetMapping("/customer/view/{id}")
	public String viewCustomer(@PathVariable(name = "id", required = true) Long id, Order order, Model model,
			HttpSession httpSession) {

		System.out.println("viewCustomer :: customer_id = " + id);
		Optional<Customer> thisCust = customerRepository.findById(id);

		if (thisCust.isPresent()) {
			System.out.println("Customer retrieved");
			Customer customer = thisCust.get();
			model.addAttribute("customer", customer);
		}

		return "customerdetails";
	}

	@GetMapping("/customer/delete/{id}")
	public String deleteCustomer(@PathVariable(name = "id", required = true) Long id, Model model,
			HttpSession httpSession) {

		System.out.println("deleteCustomer :: customer_id = " + id);

		try {
			customerRepository.deleteById(id);
			System.out.println("Customer record deleted successfully" + id);
		} catch (Exception e) {
			System.out.println("Error in deleting customer: " + id + " :: " + e.getMessage());
			e.printStackTrace();
		}

		model.addAttribute("customer", new Customer());
		return "customerdetails";
	}

	@GetMapping("/addcustomer")
	public String addCustomer(Model model, HttpSession httpSession) {

		System.out.println("Going to add new Customer");
		model.addAttribute("customer", new Customer());

		return "customerdetails";
	}

	@PostMapping("/customer")
	public String processCustomer(@ModelAttribute Customer customer, Model model, HttpSession httpSession) {

		System.out.println(
				"processCustomer :: customer_id = " + customer.getId() + "  " + model.getAttribute("operation"));

		try {
			if (customer.getId() == null) { // Adding a new customer

				Customer me = (Customer) httpSession.getAttribute("customer");

				customer.setSeller(me.getId());
				customer.setCreated(new Date());
				customer.setType(OMSUtil.USER_TYPE_CUSTOMER);

				customerRepository.save(customer);

				model.addAttribute("customer", customer);

				model.addAttribute("message", "New customer record created successfuly.");

			} else {

				Optional<Customer> thisCust = customerRepository.findById(customer.getId());
				if (thisCust.isPresent()) {
					System.out.println("Customer retrieved");
					Customer thisCustomer = thisCust.get();

					thisCustomer.setName(customer.getName());
					thisCustomer.setPassword(customer.getPassword());
					thisCustomer.setPhone(customer.getPhone());
					thisCustomer.setEmail(customer.getEmail());
					thisCustomer.setAddress(customer.getAddress());

					customerRepository.save(thisCustomer);

					model.addAttribute("customer", thisCustomer);
					model.addAttribute("message", "Customer record updated successfuly.");
				} else {
					model.addAttribute("message", "No customer record found with id# " + customer.getId() + " !!");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println(e);
			if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
				model.addAttribute("message", "Phone number already in use. Please use a different number.");
			} else {
				model.addAttribute("message",
						"Error in processing customer. Please contact admin. Error message: " + e.getMessage());
			}

			customer.setId(null);
			model.addAttribute("customer", customer);
		}

		return "customerdetails";
	}

	@PostMapping("/image")
	public String addCustomer(
			@RequestParam(name = "itemId", required = false) Long itemId,
			@RequestParam(name = "categoryId", required = false) Long categoryId, 
			Model model,
			HttpSession httpSession) {

		System.out.println("Going to add/edit image: " + categoryId + " - " + itemId);
		
		model.addAttribute("itemId", itemId);
		model.addAttribute("categoryId", categoryId);
		
		return "image";
	}
	
	@PostMapping("/upload")
	public String uploadImage(
			@RequestParam(name = "itemId", required = false) Long itemId,
			@RequestParam(name = "categoryId", required = false) Long categoryId, 
			@RequestParam(name = "file", required = true) MultipartFile file,
			Model model,
			HttpSession httpSession) {

		System.out.println("Going to upload image: " + categoryId + " - " + itemId);
		//System.out.println(file);
		Customer seller = (Customer) httpSession.getAttribute("customer");
		
		long MAX_FILE_SIZE = 1024000; //1MB alloed
		
		if(file.getSize() > MAX_FILE_SIZE) {
			model.addAttribute("message", "Image size must be less than 1 MB.");
		}else {
			try {
				
				String fileName = file.getOriginalFilename();
				System.out.println(fileName);
				String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
				
				String objectName = "";
				
				if(categoryId != null && categoryId.longValue() != 0) {
					objectName = categoryId + "." + extension;
				}else {
					objectName = itemId + "." + extension;
				}
				
				OMSUtil.uploadObject(seller.getId(), objectName, file);
				
				/*
				String destination = "C:\\Temp\\omsimages\\" + seller.getId() + "\\" ;
				
				destination = destination + (categoryId == null? itemId : categoryId ) + "." + extension;
				
				System.out.println("Saving to "+destination);
				File imagefile = new File(destination);
				file.transferTo(imagefile);
				
				
				OMSUtil.uploadObject(null,null,null,null);
				*/
				
				model.addAttribute("message", "Image uploaded successfully");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				model.addAttribute("message", "Error in saving image. Please try again, contact admin if problem persists.");
			}
		}
		
		return "image";
	}

	@PostMapping("/item")
	public String viewItem(@RequestParam(name = "itemId", required = false) Long itemId,
			@RequestParam(name = "categoryId", required = false) Long categoryId, Model model,
			HttpSession httpSession) {

		System.out.println("viewItem :: itemId = " + itemId + " & categoryId = " + categoryId);

		SKUItem item = null;

		if (itemId == null) {
			
			item = new SKUItem();
			
			item.setMin(new Double(1));
			item.setMax(new Double(10));
			item.setStep(new Double(1));
			item.setDefaultValue(new Double(1));
			
			Optional<ProductCategory> category = categoryRepository.findById(categoryId);
			item.setCategory(category.get());
			
		} else if (itemId != null || itemId.longValue() != 0) { // Existing item being updated
			item = catalogService.findItem(itemId);
			catalogService.saveItem(item);
		} else {
			model.addAttribute("message", "Please contact your admin for this operation.");
		}

		model.addAttribute("item", item);

		return "item";
	}

	@PostMapping("/category")
	public String addCategory(@RequestParam(name = "parentId", required = true) Long parentId,
			// @RequestParam(name = "categoryId", required = false) Long categoryId,
			// @RequestParam(name = "categoryName", required = false) String categoryName,
			// @RequestParam(name = "categoryDescription", required = false) String
			// categoryDescription,
			Model model, HttpSession httpSession) {

		System.out.println(" saveCategory :: parentId = " + parentId);

		Customer seller = (Customer) httpSession.getAttribute("customer");

		Optional<ProductCategory> parent = categoryRepository.findById(parentId);

		ProductCategory category = new ProductCategory(null, seller.getId(), null, null, parentId, null);

		model.addAttribute("category", category);

		return "category";
	}

	@PostMapping("/category/save")
	public String saveCategory(@ModelAttribute ProductCategory formCategory, Model model, HttpSession httpSession) {

		Long catId = formCategory.getId();
		Long parentId = formCategory.getParentId();
		System.out.println("saveCategory :: catId = " + catId + "; parent_id = " + parentId);

		try {
			if (parentId != null && parentId.longValue() == 0) { // This is under root category
				Customer seller = (Customer) httpSession.getAttribute("customer");
				ProductCategory root = catalogService.findRootCategory(seller.getId());
				formCategory.setParentId(root.getId());
			}

			if (catId == null) { // New category

				categoryRepository.save(formCategory);
				model.addAttribute("category", formCategory);

				// Update product catalog saved in session
				try {
					refreshProductCatalog(httpSession);
					model.addAttribute("message", "Category added successfully.");
				} catch (Exception e) {
					model.addAttribute(
							"Category added successfully, but product catalog could not be updated. Please log in again.");
				}

			} else if (catId.longValue() != 0) { // Exiting category to be updated

				Optional<ProductCategory> thisCategory = categoryRepository.findById(catId);
				ProductCategory category = thisCategory.get();
				category.setName(formCategory.getName());
				category.setSeller(formCategory.getSeller());
				category.setDescription(formCategory.getDescription());
				category.setParentId(formCategory.getParentId());

				categoryRepository.save(category);

				model.addAttribute("category", category);

				// Update product catalog saved in session
				try {
					refreshProductCatalog(httpSession);
					model.addAttribute("message", "Category updated successfully.");
				} catch (Exception e) {
					model.addAttribute(
							"Category updated successfully, but product catalog could not be updated. Please log in again.");
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			model.addAttribute("message", "Error in processing.   Please contact admin.");
		}
		return "category";
	}

	@PostMapping("/category/delete")
	public String deleteCategory(@RequestParam(name = "categoryId", required = true) Long categoryId, Model model,
			HttpSession httpSession) {

		System.out.println("deleteCategory :: categoryId = " + categoryId);

		// Optional<ProductCategory> thisCategory = categoryRepository.findById(catId);
		// ProductCategory category = thisCategory.get();

		categoryRepository.deleteById(categoryId);

		// Update product catalog saved in session
		try {
			refreshProductCatalog(httpSession);
			model.addAttribute("message", "Category deleted successfully.");
		} catch (Exception e) {
			model.addAttribute(
					"Category deleted successfully, but product catalog could not be updated. Please log in again.");
		}

		return showProducts(model, httpSession);
	}

	@PostMapping("/item/save")
	public String saveItem(@ModelAttribute SKUItem formItem, Model model, HttpSession httpSession) {

		Long itemId = formItem.getId();
		System.out.println("saveItem :: item_id = " + itemId + "; category_id = " + formItem.getCategory().getId());

		if (itemId != null && itemId.longValue() != 0) {

			SKUItem item = catalogService.findItem(itemId);

			item.setName(formItem.getName());
			item.setDescription(formItem.getDescription());
			item.setPrice(formItem.getPrice());
			item.setMin(formItem.getMin());
			item.setMax(formItem.getMax());
			item.setStep(formItem.getStep());
			item.setDefaultValue(formItem.getDefaultValue());
			item.setUnit(formItem.getUnit());

			catalogService.saveItem(item);

			model.addAttribute("item", item);

			// Update product catalog saved in session
			try {
				refreshProductCatalog(httpSession);
				model.addAttribute("message", "Item updated successfully.");
			} catch (Exception e) {
				model.addAttribute(
						"Item updated successfully, but product catalog could not be updated. Please log in again.");
			}
		} else {
			// New item being added
			catalogService.saveItem(formItem);
			
			formItem.setCode("sku-"+formItem.getId());
			catalogService.saveItem(formItem);
			
			
			model.addAttribute("item", formItem);

			// Update product catalog saved in session
			try {
				refreshProductCatalog(httpSession);
				model.addAttribute("message", "Item added successfully.");
			} catch (Exception e) {
				model.addAttribute(
						"Item added successfully, but product catalog could not be updated. Please log in again.");
			}
		}
		return "item";
	}

	@PostMapping("/item/delete")
	public String deleteItem(@ModelAttribute SKUItem skuItem, Model model, HttpSession httpSession) {

		Long itemId = skuItem.getId();
		System.out.println("deleteItem :: item_id = " + itemId);

		catalogService.removeItem(skuItem.getId());
		System.out.println("Item deleted. id = " + skuItem.getId());

		// model.addAttribute("item", new SKUItem());

		// Update product catalog saved in session
		try {
			refreshProductCatalog(httpSession);
			model.addAttribute("message", "Item removed successfully.");
		} catch (Exception e) {
			model.addAttribute(
					"Item removed successfully, but product catalog could not be updated. Please log in again.");
		}

		return showProducts(model, httpSession);
	}

	public void refreshProductCatalog(HttpSession httpSession) throws Exception {
		Customer cust = (Customer) httpSession.getAttribute("customer");
		ProductCategory root = catalogService.findCatalog(cust.getId());
		String productsJson = root.toJSON();
		httpSession.setAttribute("products", productsJson);
		System.out.println("Catalog updated in session");
	}

	/*
	 * @GetMapping("/order/delete/{id}") public String deleteOrder(
	 * 
	 * @PathVariable(name = "id", required = true) String id, Model model,
	 * HttpSession httpSession) {
	 * 
	 * System.out.println("deleteOrder :: order_id = "+id); Customer customer =
	 * (Customer)httpSession.getAttribute("customer"); ArrayList<Order> orders =
	 * null; Optional<Order> order = orderRepository.findById(new Long(id));
	 * 
	 * if(order.isPresent()) { Order ord = order.get(); if(ord.getStatus() != null
	 * && !ord.getStatus().equals(OMSUtil.ORDER_STATUS_NEW)) { //Only order in NEW
	 * state can be cancelled i.e. seller has not started processing
	 * model.addAttribute("message",
	 * "Order is already under processing or processed; please contact your seller for any change."
	 * );
	 * 
	 * orders = getOrders(customer.getId()); model.addAttribute("orders", orders);
	 * 
	 * return "home"; } orderRepository.delete(ord); }
	 * System.out.println("Order deleted");
	 * 
	 * model.addAttribute("message", "Order deleted successfully.");
	 * 
	 * 
	 * orders = getOrders(customer.getId()); model.addAttribute("orders", orders);
	 * 
	 * return "orders"; }
	 */
}

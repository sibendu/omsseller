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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sd.oms.dataobject.*;
import sd.oms.model.*;
import sd.oms.service.CatalogService;
import sd.oms.service.CustomerService;
import sd.oms.service.OrderService;
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
				e.printStackTrace();
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
		
		//Send order email - this is to seller himself/herself
		try {
			if(cust.getEmail() != null && cust.getEmail().indexOf("@") != -1) {	
				catalogService.sendOrderEmail(cust.getEmail(), order, buyer.get());
				System.out.println("Order emailed successfully.");
			}
		}catch(Exception e) {
			System.out.println("Order email failure: "+e.getMessage());
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

		return "customerdetails";
	}

	@PostMapping("/item")
	public String viewItem(@RequestParam(name = "itemId", required = true) Long itemId,  Model model,
			HttpSession httpSession) {
		
		System.out.println("viewItem :: item_id = " + itemId);
		
		if(itemId != null || itemId.longValue() != 0) {
			SKUItem item = catalogService.findItem(itemId);
			catalogService.saveItem(item);
			model.addAttribute("item", item);
		}else {
			model.addAttribute("message", "Please contact your admin for adding items.");
		}
		return "item";
	}
	
	@PostMapping("/item/save")
	public String saveItem(@ModelAttribute SKUItem formItem, Model model,
			HttpSession httpSession) {
		
		Long itemId = formItem.getId();
		System.out.println("saveItem :: item_id = " + itemId);
		
		if(itemId != null || itemId.longValue() != 0) {
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
			
			//Update product catalog saved in session
			try {
				Customer cust = (Customer)httpSession.getAttribute("customer");
				ProductCategory root = catalogService.findCatalog(cust.getId());
				String productsJson = root.toJSON();
				httpSession.setAttribute("products", productsJson);
				System.out.println("Catalog updated in session");
				
				model.addAttribute("message", "Item updated successfully.");
				
			} catch (Exception e) {
				model.addAttribute("Item updated successfully, but product catalog could not be updated. Please log in again.");
			}				
			
		}else {
			model.addAttribute("message", "Please contact your admin for adding items.");
		}
		return "item";
	}

	@PostMapping("/item/delete")
	public String deleteItem(@ModelAttribute SKUItem skuItem, Model model,
			HttpSession httpSession) {

		Long itemId = skuItem.getId();
		System.out.println("deleteItem :: item_id = " + itemId);

		catalogService.removeItem(skuItem.getId());
		System.out.println("Item deleted. id = "+skuItem.getId());

		model.addAttribute("item", new SKUItem());
		
		//Update product catalog saved in session
		try {
			Customer cust = (Customer)httpSession.getAttribute("customer");
			ProductCategory root = catalogService.findCatalog(cust.getId());
			String productsJson = root.toJSON();
			httpSession.setAttribute("products", productsJson);
			System.out.println("Catalog updated in session");
			
			model.addAttribute("message", "Item removed successfully.");
			
		} catch (Exception e) {
			model.addAttribute("Item removed successfully, but product catalog could not be updated. Please log in again.");
		}	
	
		return "item";
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

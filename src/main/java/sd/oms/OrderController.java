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

import sd.oms.model.Order;
import sd.oms.model.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;

	@RequestMapping(value = "/health", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getMessage() {
		ResponseEntity<String> response = null;
		response = new ResponseEntity<>("OrderService is live", HttpStatus.OK);
		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ArrayList<Order>> findCustomers() {
		ResponseEntity<ArrayList<Order>> response = null;
		ArrayList<Order> allOrders = new ArrayList<>();
		Iterable<Order> orders = orderRepository.findAll();
		for (Order order : orders) {
			allOrders.add(order);
		}
		System.out.println("# of orders = " + allOrders.size());
		response = new ResponseEntity<ArrayList<Order>>(allOrders, HttpStatus.OK);
		return response;
	}
	
	@GetMapping(path = "/clean") //
	public @ResponseBody String cleanCustomer() {
		orderRepository.deleteAll();
		return "All orders deleted";
	}
}
package sd.oms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sd.oms.model.Customer;
import sd.oms.model.CustomerRepository;
import sd.oms.model.Order;
import sd.oms.model.OrderRepository;
import sd.oms.model.ProductCategory;
import sd.oms.model.ProductCategoryRepository;
import sd.oms.util.OMSUtil;

@Service
public class CustomerService {
	
	@Autowired
	private CustomerRepository customerRepository;
	
	public List<Customer> findCustomer(Long seller){		
		return customerRepository.findBySeller(seller); 
	}

}

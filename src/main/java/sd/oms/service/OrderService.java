package sd.oms.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sd.oms.model.Order;
import sd.oms.model.OrderRepository;
import sd.oms.model.ProductCategory;
import sd.oms.model.ProductCategoryRepository;
import sd.oms.util.OMSUtil;

@Service
public class OrderService {
	
	@Autowired
	private OrderRepository orderRepository;
	
	public ArrayList[] findOrders(Long sellerId){  
		
		ArrayList[] orders = new ArrayList[2];
		
		ArrayList<Order> newOrders = new ArrayList<>();
		ArrayList<Order> otherOrders = new ArrayList<>();		
		
		
		List<Order>	allOrders = orderRepository.findBySellerId(sellerId); 
		
		long now = new Date().getTime();
		
		for (Order order : allOrders) {
			//Order status new or processing  
			if(OMSUtil.ORDER_STATUS_NEW.equals(order.getStatus()) || OMSUtil.ORDER_STATUS_PROCESSING.equals(order.getStatus())) {
				newOrders.add(order);
			}else {
				
				long age = TimeUnit.DAYS.convert(Math.abs(now - order.getCreated().getTime()), TimeUnit.MILLISECONDS);
				if(age < 180) { // Only within last 180 days
					otherOrders.add(order);
				}
			}
		}
		
		orders[0] = newOrders;
		orders[1] = otherOrders;
		
		return orders;
	}
}

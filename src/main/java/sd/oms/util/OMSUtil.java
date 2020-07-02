package sd.oms.util;

import java.util.Properties;
 
import sd.oms.model.Order;

public class OMSUtil {
	
	public static String USER_TYPE_SELLER = "SELLER";
	public static String USER_TYPE_CUSTOMER = "";
	public static String USER_TYPE_ADMIN = "ADMIN";
	
	
	public static String ORDER_STATUS_NEW = "New";
	public static String ORDER_STATUS_PROCESSING = "Processing";
	public static String ORDER_STATUS_CANCELLED = "Cancelled";
	public static String ORDER_STATUS_COMPLETED = "Completed";
	public static String ORDER_STATUS_PAID = "Paid";

	public static String ITEM_STATUS_ORDERED = "Ordered";
	public static String ITEM_STATUS_CANCELLED = "Cancelled";
	public static String ITEM_STATUS_NOT_AVAILABLE = "Not Available";
	
	public static void main(String[] args) {

		// Recipient's email ID needs to be mentioned.
        String to = "fromaddress@gmail.com";
		
	}
}

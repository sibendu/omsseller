package sd.oms.service;

import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sd.oms.model.*;

@Service
public class CatalogService {

	@Autowired
	private ProductCategoryRepository categoryRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	public SKUItem findItem(Long id) {
		Optional<SKUItem> item = itemRepository.findById(id);
		if(item.isPresent()) {
			return item.get();
		}
		return null;
	}
	
	public void saveItem(SKUItem item) {
		itemRepository.save(item);
	}
	
	public void removeItem(Long id) {
		itemRepository.deleteById(id);
	}
	
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
					//System.out.println(id + " has child "+ cat.getParentId());
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
	
	public ProductCategory findRootCategory(Long seller) throws Exception {
		
		ProductCategory root = null;	
		Iterable<ProductCategory> categories = categoryRepository.findBySeller(seller);
		
		for (ProductCategory productCategory : categories) {			
			if(productCategory.getParentId() == null ) {
				root = productCategory; 
			}	
		}
		return root;
	}
	
	public void sendOrderEmail(String toEmail, Order order, Customer customer) {
		//System.out.println("Here 1");
		
		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("enterprisecloudarch@gmail.com", "Mehul@2019");
				}
			});
			
			//System.out.println("Here 2");
			
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("enterprisecloudarch@gmail.com", false));

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); 
			msg.setSubject("Order# "+order.getId()+" - "+customer.getName()+"("+customer.getPhone()+")");
			
			String BR = "<br/>";
			String html = "<html>";
			
			html = html + "Order# "+order.getId() + BR;
			html = html + "Customer: "+ customer.getName() + BR;
			html = html + "Total Price: "+ order.getTotalPrice() + BR;
			html = html + "Instruction: "+order.getInstruction() + BR + BR;
			
			html = html + "<table border=1>";
			html = html + "<tr>";
			html = html + "<td>Item Code</td><td>Name</td><td>Description</td><td>Price</td><td>Quantity</td>";
			html = html + "</tr>";
			
			for (int i = 0; i < order.getItems().size(); i++) {
				Item item = order.getItems().get(i);
				
				html = html + "<tr>";
				html = html + "<td>"+item.getCode()+"</td>";
				html = html + "<td>"+item.getName()+"</td>";
				html = html + "<td>"+item.getDescription()+"</td>";
				html = html + "<td>"+item.getPrice()+ "/" + item.getUnit() + "</td>";
				html = html + "<td>"+item.getQuantity()+"</td>";
				html = html + "</tr>";
			}
			html = html + "</table>";
			
			html = html + "</html>";
			
			html = html + "</html>";
			
			msg.setContent(html, "text/html");
			msg.setSentDate(new Date());

			//MimeBodyPart messageBodyPart = new MimeBodyPart();
			//messageBodyPart.setContent("Tutorials point email", "text/html");

			//Multipart multipart = new MimeMultipart();
			//multipart.addBodyPart(messageBodyPart);
			
			//MimeBodyPart attachPart = new MimeBodyPart();
			//attachPart.attachFile("/var/tmp/image19.png");
			//multipart.addBodyPart(attachPart);
			
			//msg.setContent(multipart);
			
			//System.out.println("Here 3");
			
			Transport.send(msg);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("Here 4");
	}
}

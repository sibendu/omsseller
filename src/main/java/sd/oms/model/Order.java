package sd.oms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

@javax.persistence.TableGenerator(
	    name="SEQ_ORDERS",
	    table="OMS_SEQ",
	    pkColumnName = "keyname",
	    valueColumnName = "keyvalue",
	    pkColumnValue="ORDERS",
	    initialValue = 1000,
	    allocationSize=1
)

@Entity(name = "ORDERS")
public class Order implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SEQ_ORDERS") 
	private Long id;
	private Date created;
	private String instruction;
	private String status;
	private String remarks;
	private Double totalPrice;
	
	private Long customerId;
	private Long sellerId;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<Item> items = new ArrayList<>();
	
	public Order() {	
		this.created = new Date();
	}	
	
	public Order(Long id, String instruction, String status, String remarks, Long customerId, Long sellerId, Double totalPrice) {
		super();
		this.id = id;
		this.created = new Date();
		this.instruction = instruction;
		this.status = status;
		this.remarks = remarks;
		this.customerId=customerId;
		this.sellerId=sellerId;
		this.totalPrice = totalPrice;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getSellerId() {
		return sellerId;
	}

	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	
}

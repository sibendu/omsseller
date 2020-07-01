package sd.oms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.TableGenerator(
	    name="SEQ_ITEMS",
	    table="OMS_SEQ",
	    pkColumnName = "keyname",
	    valueColumnName = "keyvalue",
	    pkColumnValue="ITEMS",
	    initialValue = 1000,
	    allocationSize=1
)

@Entity(name = "ITEMS")
public class Item implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SEQ_ITEMS") 
	private Long id;
	
	private String code;
	private String name;
	private String description;	
	private Double quantity;	
	private Double price;	
	private String unit;	
	private String instruction;
	private String status;
	private String remarks;
	
	@JsonIgnore 
	@ManyToOne
    @JoinColumn(name ="order_id", nullable = false)
    private Order order;
	
	public Item() {	
	}

	public Item(Long id, String code, String name, String description, Double quantity, Double price, String unit, String instruction,
			String remarks, String status, sd.oms.model.Order order) {
		super();
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
		this.price = price;
		this.unit=unit;
		this.instruction = instruction;
		this.remarks = remarks;
		this.status=status;
		this.order = order;
	}
	
	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	


}

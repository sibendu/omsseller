package sd.oms.dataobject;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDTO implements Serializable {
	
	private Long customer;
	
	private String instruction;
	
	private ItemDTO[] items;
	
	public OrderDTO() {
		super();
	}

	public OrderDTO(Long customer, String instruction, ItemDTO[] items) {
		super();
		this.customer=customer;
		this.instruction = instruction;
		this.items = items;
	}
	
	

	public Long getCustomer() {
		return customer;
	}

	public void setCustomer(Long customer) {
		this.customer = customer;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public ItemDTO[] getItems() {
		return items;
	}

	public void setItems(ItemDTO[] items) {
		this.items = items;
	}
	
}

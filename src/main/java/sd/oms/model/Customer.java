package sd.oms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

@javax.persistence.TableGenerator(
	    name="SEQ_CUSTOMER",
	    table="OMS_SEQ",
	    pkColumnName = "keyname",
	    valueColumnName = "keyvalue",
	    pkColumnValue="CUSTOMER",
	    initialValue = 1000,
	    allocationSize=1
)

@Entity(name = "CUSTOMER")
public class Customer implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SEQ_CUSTOMER") 
	private Long id;

	private String type;
	private String password;
	private String name;
	
	@Column(unique=true) 
	private String phone;
	
	private String email;
	private String address;
	private Date created;
	private Long seller;
	
	public Customer() {	
	}
	
	public Customer(Long id, String type, Long seller, String password, String name, String phone, String email, String address, Date created) {
		super();
		this.id = id;
		this.type=type;
		this.seller = seller;
		this.password = password;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.created = created;
	}
	

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getPassword() {
		return password;
	}
	
	public Long getSeller() {
		return seller;
	}

	public void setSeller(Long seller) {
		this.seller = seller;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	

}

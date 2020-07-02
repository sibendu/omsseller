package sd.oms.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.TableGenerator(
    name="SEQ_SKUITEM",
    table="OMS_SEQ",
    pkColumnName = "keyname",
    valueColumnName = "keyvalue",
    pkColumnValue="SKUITEM",
    initialValue = 1000001,
    allocationSize=1
)

@Entity(name = "SKUITEM")
public class SKUItem implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SEQ_SKUITEM") 
	private Long id;
	
	private String code;
	
	//@NotNull
	private String name;
	
	//@NotNull
	private String description;
	
	//@NotNull
	private Double price;
	
	//@NotNull
	private String unit;
	
	//@NotNull
	private Double min;
	
	//@NotNull
	private Double max;
	
	//@NotNull
	private Double step;
	
	//@NotNull
	private Double defaultValue;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private ProductCategory category;

	public SKUItem() {
		super();
	}

	public SKUItem(Long id, String code, String name, String description, Double price, String unit, Double min,
			Double max, Double step, Double defaultValue, ProductCategory category) {
		super();
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.price = price;
		this.unit = unit;
		this.min = min;
		this.max = max;
		this.step = step;
		this.defaultValue = defaultValue;
		this.category = category;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public Double getStep() {
		return step;
	}

	public void setStep(Double step) {
		this.step = step;
	}

	public Double getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Double defaultValue) {
		this.defaultValue = defaultValue;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

	public String toJSON() {

		String json = "{\"id\": " + id + ",\"parent\":" + category.getId()
				+ ",\"code\":\""+ code +"\",\"name\":\""+name+"\",\"description\":\""+ description +"\",\"price\":"+price+",\"unit\":\""+unit+"\",\"min\":"+min+",\"max\":"+max+",\"step\":"+step+",\"defaultValue\":"+defaultValue+"}";
		return json;
	}
}

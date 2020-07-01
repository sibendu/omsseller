package sd.oms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@javax.persistence.TableGenerator(
    name="SEQ_CATEGORY",
    table="OMS_SEQ",
    pkColumnName = "keyname",
    valueColumnName = "keyvalue",
    pkColumnValue="CATEGORY",
    initialValue = 100,
    allocationSize=1
)

@Entity(name = "CATEGORY")
public class ProductCategory implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "SEQ_CATEGORY") 
	private Long id;
	private Long seller;
	private String name;
	private String description;
	private Long parentId;
	
	@Transient
	private List<ProductCategory> subCategories =  new ArrayList<ProductCategory>();
	
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<SKUItem> skuItems =  new ArrayList<SKUItem>();
	
	public ProductCategory() {
		super();
	}
	
	public ProductCategory(Long id, Long seller, String name, String description, Long parentId, List<SKUItem> skuItems) {
		super();
		this.seller = seller;
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentId = parentId;
		if(skuItems != null) {
			this.skuItems = skuItems;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public Long getSeller() {
		return seller;
	}

	public void setSeller(Long seller) {
		this.seller = seller;
	}

	public List<ProductCategory> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<ProductCategory> subCategories) {
		this.subCategories = subCategories;
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

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<SKUItem> getSkuItems() {
		return skuItems;
	}

	public void setSkuItems(List<SKUItem> skuItems) {
		this.skuItems = skuItems;
	}
	
	public void addItem(SKUItem item) {
		this.skuItems.add(item);
	}
	
	public void addCategory(ProductCategory cat) {
		cat.setParentId(this.id);
		this.subCategories.add(cat);
	}
	
	public String toJSON() {
		
		String json = null;
		
		if(this.subCategories != null && this.subCategories.size() > 0) {
			
			if(this.parentId == null || this.id.longValue() == 0) {
				//This is root category
			
				String subCateJson = "";
				for(int m =0 ; m < subCategories.size(); m++) {
					subCateJson = subCateJson + subCategories.get(m).toJSON() + ",";
				}
				if(subCateJson.length() > 0) {
					subCateJson = subCateJson.substring(0, subCateJson.length()-1);
				}
					
				json = "[ " + subCateJson +" ] ";
				
			}else {
			
				json =  "{\"id\":"+id+",\"name\":\""+name+"\",\"parent\":"+parentId;
						                                           
				String subCateJson = "";
				for(int m =0 ; m < subCategories.size(); m++) {
					subCateJson = subCateJson + subCategories.get(m).toJSON() + ",";
				}
				if(subCateJson.length() > 0) {
					subCateJson = subCateJson.substring(0, subCateJson.length()-1);
				}
				
				json = json + ", \"subcategories\": [ "+ subCateJson +"] } ";
			} 
			
		}else {

			json = "{\"id\": " + id + ",\"name\":\""+name+"\",\"description\":\""+ description +"\",\"parent\":"+parentId;
		
			String items =  "";
			for(int m =0 ; m < skuItems.size(); m++) {
					items = items + skuItems.get(m).toJSON() + ",";
			}
			if(items.length() > 0) {
				items = items.substring(0, items.length()-1);
			}
			
			//System.out.println(items);
			
			json = json + ", \"items\": [ "+ items +"] }";
		
		}
		
		return json;
	}
	
}

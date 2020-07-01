package sd.oms.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sd.oms.dataobject.ItemDTO;
import sd.oms.model.*;

public class TestCatalog {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		/*
[
 {"id":"1002","code":"abc1002","name":"Deradun","description":"Deradun Rice","price":"70","unit":"kg","quantity":4},
 {"id":"2001","code":"abc2001","name":"Fortune","description":"Fortune","price":"125.75","unit":"pack","quantity":2}]
		
		
		String x = "[{\"id\":\"1002\",\"code\":\"abc1002\",\"name\":\"Deradun\",\"description\":\"Deradun Rice\",\"price\":\",,70\",\"unit\":\"kg\",\"quantity\":4},{\"id\":\"2001\",\"code\":\"abc2001\",\"name\":\"Fortune\",\"description\":\"Fortune\",\"price\":\",,125.75\",\"unit\":\"pack\",\"quantity\":2}]";
		
		final GsonBuilder gsonBuilder = new GsonBuilder();
		final Gson gson = gsonBuilder.create();

		ItemDTO[] order = gson.fromJson(x, ItemDTO[].class);
		for (int i = 0; i < order.length; i++) {
			System.out.println(order[i].getDescription());
		}
		
		//OrderDTO order = gson.fromJson(x, OrderDTO.class);
		
		System.out.println(order);
		*/
		ProductCategory root = getDummyCatalog();
		System.out.println(root.toJSON());
		
	}
	
	public String ReadProductJsonFromFile() {
		String products = null;
		try {
			InputStream fis = this.getClass().getClassLoader()
                    .getResourceAsStream("products.json");
			
			//FileInputStream fis = new FileInputStream(in); 
			byte[] b = new  byte[10000];
			fis.read(b);
			fis.close();
			products = new String(b).trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return products;
	}
	
	public static ProductCategory getDummyCatalog() {
		 
		Long seller = new Long(2222);
		
		ProductCategory cat = null;
		SKUItem item = null;
		
		ProductCategory[] catalog = new ProductCategory[10];
				
		for (int i = 0; i < catalog.length; i++) {
			
			int catId = (i + 1) * 100;
			
			cat = new ProductCategory(new Long(catId),seller, "Category-"+catId, "Category Description-"+catId, new Long(0), null);
			
			for (int k = 1; k < 3; k++) {
				
				long val = catId + 10 + k;
				
				item = new SKUItem(new Long(val), "sku-"+val, "Item -"+val,  "Item Description :::: "+val, new Double(val), "kg", new Double(1), new Double(5), new Double(0.5),
						new Double(2), cat);
				
				cat.addItem(item); 
			}
			catalog[i] = cat;
			
		}
		
		ProductCategory rootCatalog = new ProductCategory(new Long(0),seller, "Root Catalog", "Root Catalog", null, null);
		
		/*
		for (int i = 0; i < catalog.length; i++) {
			rootCatalog.addCategory(catalog[i]);
		}*/
		
		ProductCategory cat1 = new ProductCategory(new Long(10),seller, "Super Category 10", "Super Category 10", new Long(0), null);
		cat1.addCategory(catalog[0]);
		cat1.addCategory(catalog[1]);
		
		
		ProductCategory cat2 = new ProductCategory(new Long(20),seller,  "Super Category 20", "Super Category 20", new Long(0), null);
		cat2.addCategory(catalog[2]);
		cat2.addCategory(catalog[3]);
		cat2.addCategory(catalog[4]);
		
		ProductCategory cat3 = new ProductCategory(new Long(30),seller, "Super Category 30", "Super Category 30", new Long(0), null);
		cat3.addCategory(catalog[5]);
		cat3.addCategory(catalog[6]);
		cat3.addCategory(catalog[7]);
		cat3.addCategory(catalog[8]);
		cat3.addCategory(catalog[9]);
		
		rootCatalog.addCategory(cat1);
		rootCatalog.addCategory(cat2);
		rootCatalog.addCategory(cat3);
		
		//System.out.println(rootCatalog.toJSON());
		return rootCatalog;
		
	}
	

}

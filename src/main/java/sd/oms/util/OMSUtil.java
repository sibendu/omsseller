package sd.oms.util;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.google.auth.oauth2.*;
import com.google.cloud.storage.*;


import org.springframework.web.multipart.MultipartFile;

public class OMSUtil {

	public static String IMAGE_BASE_URL = "https://storage.cloud.google.com/oms_pictures/";
	public static String IMAGE_URL_SUFFIX = ".jpg?authuser=3";
	public static String GCP_BUCKET = "oms_pictures";
	public static String GCP_AUTH_FILE = "base-project-f69ff8d47af1.json";
	
	public static String USER_TYPE_SELLER = "SELLER";
	public static String USER_TYPE_CUSTOMER = "CUSTOMER";
	public static String USER_TYPE_ADMIN = "ADMIN";

	public static String ORDER_STATUS_NEW = "New";
	public static String ORDER_STATUS_PROCESSING = "Processing";
	public static String ORDER_STATUS_CANCELLED = "Cancelled";
	public static String ORDER_STATUS_COMPLETED = "Completed";
	public static String ORDER_STATUS_PAID = "Paid";

	public static String ITEM_STATUS_ORDERED = "Ordered";
	public static String ITEM_STATUS_CANCELLED = "Cancelled";
	public static String ITEM_STATUS_NOT_AVAILABLE = "Not Available";

	public static void main(String[] args) throws Exception {
		
		String path = "C:\\tmp\\140.jpg";
		FileInputStream fis = new FileInputStream(new File(path));
		byte[] b = new byte[10000];
		fis.read(b);
		fis.close();
		
		uploadObject(new Long(1001), "150.jpg", null);
	}
	
	
	public static void uploadObject(Long sellerId, String objectName, MultipartFile file)
			throws IOException {
		
		objectName = sellerId + "/" + objectName;		
		
		try {

			InputStream in = OMSUtil.class.getClassLoader().getResourceAsStream(GCP_AUTH_FILE);
			GoogleCredentials credentials = GoogleCredentials.fromStream(in)
					.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
			Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
			
//			System.out.println("Buckets:");
//			Storage.Buckets.List buckets = store.buckets().list();
//			for (Bucket bucket : buckets.iterateAll()) {d:
//				System.out.println(bucket.toString());
//			}

			BlobId blobId = BlobId.of(GCP_BUCKET, objectName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
			//storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
			storage.create(blobInfo,  file.getBytes());
			
			System.out.println("File upload :: " + objectName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
		}
		
	}
}

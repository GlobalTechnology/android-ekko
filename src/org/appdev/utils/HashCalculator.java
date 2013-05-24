package org.appdev.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.util.Log;

public class HashCalculator {
	
	private String fileName;
	
	HashCalculator(String fileName) {
		this.fileName = fileName;
	}
	
	public String getCourseSha1() throws OutOfMemoryError, IOException {
		File file=new File(fileName);  
		FileInputStream in = new FileInputStream(file);  
	    MessageDigest messagedigest;  
	    try {  
	    	messagedigest = MessageDigest.getInstance("SHA-1");  
	  
	    	byte[] buffer = new byte[1024 * 1024 ];  
	    	int len = 0;  
	      
		    while ((len = in.read(buffer)) >0) {  
		    	messagedigest.update(buffer, 0, len);  
		    }       
			 return ByteArrayToHexString(messagedigest.digest());  
		}   catch (NoSuchAlgorithmException e) {  
			    Log.e("getFileSha1->NoSuchAlgorithmException###", e.toString());  
			        e.printStackTrace();  
	    }  catch (OutOfMemoryError e) {  
	      
			Log.e("getFileSha1->OutOfMemoryError###", e.toString());  
	        e.printStackTrace();  
	        throw e;  
	    }  
		finally {  
		     in.close();  
		}  
		    return null;  	
		
	}
	
	  public String ByteArrayToHexString(byte[] b){
		    if (b==null) return null;
		    
		    StringBuffer sb = new StringBuffer(b.length * 2);
		    for (int i = 0; i < b.length; i++){
		      int v = b[i] & 0xff;
		      if (v < 16) {
		        sb.append('0');
		      }
		      sb.append(Integer.toHexString(v));
		    }
		    return sb.toString().toUpperCase();
		  }

}

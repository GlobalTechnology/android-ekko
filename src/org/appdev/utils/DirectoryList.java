package org.appdev.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.appdev.app.AppContext;

public class DirectoryList {
	
	private String path;	
	
	public DirectoryList(String path) {	
		
		this.path = path;
	}
	
	public DirectoryList(String path, boolean bCheck) {	
		
		if(bCheck == true) {
			File f = new File(path);
			if (f.isDirectory()){
				f.mkdirs();
			}
		}
		
		this.path = path;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}
	
	public ArrayList<File> getDirectoryList(){		
		ArrayList<File> dirFileList = new ArrayList<File>();
		File f = new File(path);
		File[] files = f.listFiles();

		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
			       if (arg0.lastModified() > arg1.lastModified()) {
			            return -1;
			        } else if (arg0.lastModified() < arg1.lastModified()) {
			            return 1;
			        } else {
			            return 0;
			        }
			}
			
		});
		for (File inFile : files) {
		    if (inFile.isDirectory()) {
		        // is directory
		    	dirFileList.add(inFile);		    	
		    	
		    }
		}
		

		return dirFileList;		
	}
	
	public List<String> getDirFileNameList() {
		List<String> dirFileNameList = new ArrayList<String>();
		File f = new File(path);
		File[] files = f.listFiles();
		for (File inFile : files) {
		    if (inFile.isDirectory()) {
		        // is directory		    
		    	dirFileNameList.add(inFile.getAbsolutePath());
		    	
		    }
		}
		return dirFileNameList;
	}
	
	
	public List<String> getCourseConfigFileList() {
		List<String> dirFileNameList = new ArrayList<String>();
		File f = new File(path);
		
		File[] files = f.listFiles();
		
		if (files == null )
			return null;
		else {
			for (File inFile : files) {
			    if (inFile.isDirectory() && !inFile.getPath().endsWith("downloads") ){
			        // is directory		    
			    	dirFileNameList.add(inFile.getAbsolutePath() + "/" + AppContext.COURSE_MANIFEST_FILE);
			    	
			    }
			}
			return dirFileNameList;
		}
	}


}

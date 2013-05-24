package org.appdev.utils;

import android.util.Log; 

import java.io.ByteArrayOutputStream;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 


public class Unzip { 
  private String zipFile;  //zipped file
  private String pathDir;  //unzip location or directory

  public Unzip(String zipFile, String location) { 
    this.zipFile = zipFile; 
    this.pathDir = location; 

    DirChecker(""); 
  } 

  public void unzip() { 
    try  { 
      FileInputStream fin = new FileInputStream(zipFile); 
      ZipInputStream zin = new ZipInputStream(fin); 
      ZipEntry ze = null; 
      int count;
      while ((ze = zin.getNextEntry()) != null) { 
        Log.v("Decompress", "Unzipping " + ze.getName()); 

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        
        if(ze.isDirectory()) { 
          DirChecker(ze.getName()); 
        } else { 
          FileOutputStream fout = new FileOutputStream(pathDir + ze.getName()); 
          while ((count = zin.read(buffer)) != -1) { 
            fout.write(buffer, 0, count); 
          } 

          fout.close(); 
          zin.closeEntry(); 
          
        } 

      } 
      zin.close(); 
    } catch(Exception e) { 
      Log.e("Unzip", "unzip", e); 
    } 

  } 

  private void DirChecker(String dir) { 
    File f = new File(this.pathDir + dir); 
 
    if(!f.exists()) {
    	f.mkdirs(); 
    }

  } 
} 
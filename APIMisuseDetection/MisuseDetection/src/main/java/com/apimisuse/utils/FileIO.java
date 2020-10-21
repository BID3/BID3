package com.apimisuse.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import spoon.reflect.cu.SourcePosition;


public class FileIO {
	public static final String finalResult = "Results.json"; // store all potential bugs
	

	
	public static void traverseFolder(String directoryName, ArrayList<File> files) {		
	    File directory = new File(directoryName);
	    // Get all files from a directory.
	    File[] fList = directory.listFiles();
	    if(fList != null){
	        for (File file : fList) {      
	            if (file.isFile()) {
	            	String fileName =  file.getAbsolutePath();
	            	if(!fileName.contains("/test/") && fileName.endsWith(".java")){
	            		files.add(file);
	            	}
	            } else if (file.isDirectory()) {
	            	traverseFolder(file.getAbsolutePath(), files);
	            }
	        }
	    }
	}
	
	public static void writeToJSON(String pos, String writeFile, String type) {
		JSONObject obj = new JSONObject();

		obj.put("Bug Position", pos);
		obj.put("Bug Pattern",type);
		
  	  	try {
  	  		FileWriter fw = new FileWriter(writeFile,true);
  	  		BufferedWriter bw = new BufferedWriter(fw);
			bw.write(obj.toJSONString());
			bw.write("\n");
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	  	
	}
}

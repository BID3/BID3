package com.apimisuse.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.apimisuse.detection.DetectBug;
import com.apimisuse.utils.FileIO;

public class Main {	
	public static void main(String[] args) throws IOException, NoSuchMethodException {
		String projectPath = args[0];
		ArrayList<File> files = new ArrayList<File>();
		FileIO.traverseFolder(projectPath,files);
		
		for (File f : files){
			String fileName = f.getAbsolutePath();
			if (fileName.endsWith(".java") && !fileName.toLowerCase().contains("test")){
				DetectBug.detectBug(fileName); // extract by each pattern
			}
		}
	}	
}

package com.apimisuse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FileTraverse
{	
	public static final String OLD = "buggy-version";
	public static final String NEW = "fixed-version";
	public static final String filterPath = "FilterJavaFiles.txt";
	public static final int JAVAFILENUMBER = 10000;
	public static final int CHANGLINENUMBER = 7;

	public static List<FilePair> getAllFiles(String pairFileDir) throws IOException {
		List<FilePair> list = new ArrayList<FilePair>();
		File subjectDir = new File(pairFileDir + File.separator);		
		list.addAll(getAllFilesPerSubject(subjectDir));		
		return list;
	}

	private static HashMap<String, Integer> getChangeLineNumber(String path) throws IOException {
		HashMap<String, Integer> commitLine = new HashMap<String, Integer>();
		File f = new File(path);
		String[] pathnames = f.list();
		for (String pathname : pathnames) {
            // Print the names of files and directories
            BufferedReader br = new BufferedReader(new FileReader(path + pathname));
            String line = br.readLine();
            while(line!=null) {
            	String commit = line.split(":")[0];
            	int number = Integer.parseInt(line.split(":")[1]);
            	if (number < CHANGLINENUMBER && number > 0) {
            		commitLine.put(commit,number);
            	}
            	line = br.readLine();
            }
        }
		
		return commitLine;
	}
	
	public static List<FilePair> getAllFilesPerSubject(File folder) throws IOException {

		HashSet<String> filteredFiles = readFileter(filterPath);
		
		List<FilePair> list = new ArrayList<FilePair>();
		String path = folder.getAbsolutePath() + File.separator;
		File PartationDir = new File(path);
		if (!PartationDir.exists())
			return list;
		File[] versions = PartationDir.listFiles();
		for (File version : versions) {
			String commitInforPath = version + "/archiveInfo.txt";
			if (!new File(commitInforPath).exists() ) {
				continue;
			}
			String commitNumber = getCommitNumber(commitInforPath);


			if(isFiltered(version))
				continue;
			File oldDir = new File(version.getAbsoluteFile() + File.separator + OLD);
			if(oldDir.exists()){
				int fileCount = 0;
				for(File file:oldDir.listFiles()){
					if(file.toString().endsWith(".java"))
						fileCount++;
				}
				if (fileCount <= JAVAFILENUMBER){
					for(File file:oldDir.listFiles()){
						if(!file.getAbsolutePath().contains(".test.")){
							if(isFiltered(file))
								continue;						
							String name=file.getName();
							if(filteredFiles.contains(name)) {							
								continue;
							}
							if(name.endsWith(".java")) {
								list.add(new FilePair(file, new File(version.getAbsoluteFile() + File.separator + NEW+File.separator+name)));
							}
						}
					}
				}else{
					System.out.println(oldDir.toString() + " Have more than 5 java files");
				}
			}
			
		}
		System.out.println("========================");
		System.out.println("There are " + list.size() + " pair files including java files");
		System.out.println("========================");
		return list;
	}
	private static String getCommitNumber(String string) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(string));
		String line = reader.readLine();
		String fixedCommit = "";
		while(line!=null) {
			if (line.startsWith("Fixed-version")) {
				fixedCommit = line.split("Fixed-version:")[1].trim();
			}
			line = reader.readLine();
		}
		return fixedCommit;
	}

	public static HashSet<String> readFileter(String path){
		HashSet<String> FFList = new HashSet<String>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(path));
			String line = bf.readLine();
			while(line != null) {
				int lastIndex = line.lastIndexOf("/");
				String FFiles = line.substring(lastIndex + 1);
				FFList.add(FFiles);
				line = bf.readLine();
			}
			bf.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return FFList;		
	}
	
	public static boolean isFiltered(File file){
		return file.getName().startsWith(".");
	}

}

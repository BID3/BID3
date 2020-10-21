package com.apimisuse.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gumtree.spoon.AstComparator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

public class Utils {
	public static String extractInvoc(CtElement element) {

		CtInvocationImpl invocation = (CtInvocationImpl) element;	
		if (invocation == null)
			return "";
		String all = invocation.toString();
		String fullQualifiedName = "";
		String fullQualifiedNameType = "";
		if (invocation.getTarget() != null){
			fullQualifiedName = invocation.getTarget().toString();
			fullQualifiedNameType = fullQualifiedName;
			if (invocation.getTarget().getType() != null)
				fullQualifiedNameType = invocation.getTarget().getType().toString();  //such as variable name to "java.lang.String"
		}
		
		ArrayList<CtTypedElement> arguments = new ArrayList<CtTypedElement>();
		StringBuilder argus = new StringBuilder();
		if(invocation.getArguments().size() > 0) {
			arguments = (ArrayList<CtTypedElement>) invocation.getArguments();		
			int argCount = 1;
			for (CtTypedElement ar : arguments) {
				String argType = ar.toString();
				if(ar.getType() != null)
					argType = ar.getType().toString();				
				argus.append(argType);
				if(argCount < arguments.size())
					argus.append(",");
				argCount ++;
			}
		}
		
		String argumes = "(" + argus.toString() + ")";
		String onlyMethodName = "";
		String returnString = "";
					
		if (!fullQualifiedName.equals("")) {	
			if(all.contains(fullQualifiedName)){
				int firstIndex = all.indexOf(fullQualifiedName) + fullQualifiedName.length() + 1;			
				int lastIndex = all.substring(firstIndex).indexOf("(");			
				onlyMethodName = all.substring(firstIndex).substring(0, lastIndex);
				if(fullQualifiedNameType.equals("void")) {
					returnString = fullQualifiedName + "." + onlyMethodName + argumes;
				}else {
					returnString = fullQualifiedNameType + "." + onlyMethodName + argumes;
				}
			}
		}else {
			if(all.startsWith("(")) {
				all = all.replaceFirst("\\(", "");
			}
			if(all.contains("(")){
				onlyMethodName = all.substring(0,all.indexOf("("));
				returnString = onlyMethodName + argumes;
			}
		}
		String nameAndargus = onlyMethodName + argumes;
		String[] cases = {"equals(java.lang.String)","equalsIgnoreCase(java.lang.String)","contains(java.lang.String)",
						  "replace(java.lang.String,java.lang.String)","replaceAll(java.lang.String,java.lang.String)",
						  "startsWith(java.lang.String)","indexOf(java.lang.String)","lastIndexOf(java.lang.String)",
						  "toLowerCase()","replaceFirst(java.lang.String,java.lang.String)","endsWith(java.lang.String)",
						  "matches(java.lang.String)","indexOf(char)","lastIndexOf(char)","toLowerCase()","toUpperCase()",
						  "trim()","split(java.lang.String)","substring(int,int)"};
		List<String> caseList = Arrays.asList(cases); 
		/*
		if(caseList.contains(nameAndargus)){
			returnString = "*." + onlyMethodName + argumes;
		}
		*/
		if(returnString.startsWith("[")){
			int idx = returnString.indexOf("] ");
			returnString = returnString.substring(idx + 2);
		}
		
		return returnString;
	}
	public static CtTypeImpl prepareParse(String filename) throws NoSuchMethodException {
		AstComparator astc = new AstComparator();
		Method getCtTypeMethod = AstComparator.class.getDeclaredMethod("getCtType", File.class);
		getCtTypeMethod.setAccessible(true);
		CtType ctt = null;
		try{
			ctt = (CtType)getCtTypeMethod.invoke(astc, new File(filename));
		}catch(InvocationTargetException e){			
		}catch(IllegalArgumentException e){			
		}catch(IllegalAccessException e){			
		}
		
		CtTypeImpl ctti = (CtTypeImpl) ctt;
		return ctti;
	}
	public static String cleanAPI(CtElement ct) {
		String cleanedInvoc;
		CtInvocationImpl cti = (CtInvocationImpl)ct;
		cleanedInvoc = Utils.extractInvoc(cti);	
		return cleanedInvoc;
	}
	public static String onlyMethodName(String cleanedInvoc) {
		int firstIndex = cleanedInvoc.indexOf("(");
		if(firstIndex > -1){
			cleanedInvoc = cleanedInvoc.substring(0,firstIndex);
		}
		return cleanedInvoc;
	}

}

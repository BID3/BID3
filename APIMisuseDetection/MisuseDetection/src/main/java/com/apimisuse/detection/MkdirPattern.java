package com.apimisuse.detection;

import java.util.ArrayList;

import com.apimisuse.utils.FileIO;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtVariableWriteImpl;

/*
 * 
 * java.io.File.mkdir()
 * 
 * 
 */
public class MkdirPattern extends BugPattern{
	ArrayList<String> dangerVariable = new ArrayList<String>();
	
	
	MkdirPattern(String pName){
		super(pName);
	}
	
	public static void getDanerVariable(ArrayList<String> dangerVariable, CtElement ct) {
		String dangerVar = null, checkString = null; 
		String dangerType = "";
		if (ct instanceof CtLocalVariableImpl) {
			CtLocalVariableImpl ctlv = (CtLocalVariableImpl) ct;
			checkString = ctlv.toString();   // "File b "
			if(ctlv.getType() != null)
				dangerType = ctlv.getType().toString();
			dangerVar = ctlv.getSimpleName();
		}
		if (ct instanceof CtVariableWriteImpl) {
			CtVariableWriteImpl ctv = (CtVariableWriteImpl)ct;
			checkString = ctv.getParent().toString();
			if(ctv.getType() != null)
				dangerType = ctv.getType().toString();
			dangerVar = ctv.toString();
		}
		if(dangerType!= null && dangerType.contains("io.File")) {							
			boolean ifDir = checkPath(checkString);
			if(ifDir) {
				dangerVariable.add(dangerVar);
			}
		}		
	}
	
	public static boolean checkPath(String pathString) {
		String checkPath = pathString.toLowerCase();
		return checkPath.matches(".*(eparator|/|\\\\).*");
	}
	
	public void invoc(CtElement ct, SourcePosition srcPos, String cleanedInvoc) {
		CtInvocationImpl cti = (CtInvocationImpl) ct;
		if(cleanedInvoc.contains(bugPatternName)) {									
			if(cti.getTarget() != null) {
				String receiver = cti.getTarget().toString();
				
				if(checkPath(receiver) || dangerVariable.contains(receiver)){
					FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);
				}										
			}									
		}
	}
	
	
}

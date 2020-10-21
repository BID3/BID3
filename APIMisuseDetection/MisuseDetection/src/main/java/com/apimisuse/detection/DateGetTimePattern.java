package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.apimisuse.utils.FileIO;

import spoon.reflect.code.CtExpression;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtVariableReadImpl;

public class DateGetTimePattern extends BugPattern{
	boolean dateAsArgument;
	HashMap<String,ArrayList<String>> dateAndCallAPI = new HashMap<>();
	
	DateGetTimePattern(String pName){
		super(pName);
	}
	
	//new java.util.Date....
	public void invoc(CtElement ct, SourcePosition srcPos, String cleanedInvoc) {
		CtInvocationImpl cti = (CtInvocationImpl) ct;
		if(cleanedInvoc.contains(bugPatternName)){
			CtElement ctTarget = cti.getTarget();
			if(ctTarget != null) {
				if( !(ctTarget instanceof CtVariableReadImpl)){
					CtElement target = cti.getTarget();
					if(target != null && target.toString().contains("new java.util")) {
						FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);	
					}
				}
			}
		}
	}
	public static boolean hasDateArg(CtMethod m) {
		List<CtElement> paras = m.getParameters();
		for(CtElement p : paras) {
			if(p.toString().contains("java.util.Date")) {
				return true;
			}			
		}
		return false;
	}
	
	
	public void preDetect(CtMethod m, ArrayList<CtElement> allElements) {
		dateAsArgument = hasDateArg(m);
		if(!dateAsArgument) {
			getMapVariable(allElements,"Date");  //  get "d" like "Date d = new Date()" , then put "d" into "dateAndCallAPI" map
		}
	}

	private void getMapVariable(ArrayList<CtElement> allElements,
			String tag) {
		for(CtElement ct:allElements) {
			if(ct instanceof CtLocalVariableImpl) {
				CtLocalVariableImpl ctLocalV = (CtLocalVariableImpl) ct;
				CtExpression CtAssign = ctLocalV.getAssignment();
				if(CtAssign != null && CtAssign.toString().contains("new java.util.Date")) {
					String name = ctLocalV.getSimpleName();
					dateAndCallAPI.put(name, new ArrayList<String>());
				}										
			}
		}
		
	}

	public void getObjAndUse(CtElement ct) {
		if(ct instanceof CtInvocationImpl) { 
			CtInvocationImpl cti = (CtInvocationImpl) ct;
			CtElement target = cti.getTarget();
			if(target != null) {
				if (dateAndCallAPI.containsKey(target.toString())) {
					dateAndCallAPI.get(target.toString()).add(cti.toString());
				}
			}
		}
		
	}

	public void detect(CtMethod m) {
		for(String key : dateAndCallAPI.keySet()) {
			ArrayList<String> values = dateAndCallAPI.get(key);
			if(values.size() == 1) {
				if(values.get(0).equals(key+ ".getTime()")) {					
					FileIO.writeToJSON(m.getPosition().toString() + " " + key, FileIO.finalResult, bugPatternName);	
				}				
			}
		}
		
	}
	
	
	
	

}

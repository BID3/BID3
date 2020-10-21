package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.Set;

import com.apimisuse.utils.FileIO;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.code.CtCatchImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtTryImpl;

/*
 * org.json.JSONObject.getString(java.lang.String)
 * org.json.JSONObject.getJSONArray(java.lang.String)
 * org.json.JSONObject.getJSONObject(java.lang.String)
 * 
 */
public class JSONObjectPattern extends BugPattern{

	JSONObjectPattern(String pName){
		super(pName);
	}
	
	public void invoc(CtMethod m, CtElement ct, SourcePosition srcPos, String cleanedInvoc) {		
		if(cleanedInvoc.contains(bugPatternName)){				
			CtInvocationImpl cti = (CtInvocationImpl) ct;
			ArrayList<String> throwsString = new ArrayList<String>();
			Set<CtElement> allThrows = m.getThrownTypes();
			for (CtElement t : allThrows) {
				throwsString.add(t.toString());
			}
			CtElement parent = cti.getParent();
			boolean ifTry = false;
			while(parent != null) {
				if(parent instanceof CtTryImpl || parent instanceof CtCatchImpl) {
					ifTry = true;
					break;
				}
				parent = parent.getParent();
			}
			if(ifTry == false && !throwsString.toString().toLowerCase().contains("jsonexception") 
					&& !throwsString.contains("java.lang.Exception") 
					&& !throwsString.contains("java.lang.RuntimeException")) {
				FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);
			}
		}		
	}
}

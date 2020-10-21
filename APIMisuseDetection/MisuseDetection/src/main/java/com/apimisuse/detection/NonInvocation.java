package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.HashMap;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLoopImpl;

/*
 * prepare required elements for detection of API invocation 
 * and other tokens like "Loop".
 * 
 */

public class NonInvocation {
	
	public static boolean ifInLoop(String var, String tag, ArrayList<String> invocInLoop) {

		
		if (tag.equals("java.sql.Connection.createStatement()")) {
			for(String invoc : invocInLoop) {
				if((!var.equals("") && invoc.contains(var + ".execute"))){
					return true;
				}
			}
		}
		
		if (tag.equals("java.util.concurrent.Executors.newCachedThreadPool()")) {
			for (String invoc: invocInLoop) {
				if((!var.equals("") && invoc.contains(var + ".submit"))){
					return true;
				}
			}
		}
		
				
		//if(!bugCaller.equals("") && loopBody.contains(bugCaller)){
		//	hasLoop = true;
		
		return false;
	}
	
	public static boolean ifLoopAsParent(CtInvocationImpl cti) {
		CtElement ctiParent = cti.getParent();
		while (ctiParent != null) {
			if(ctiParent instanceof CtLoopImpl) {											
				return true;
			}											
			ctiParent = ctiParent.getParent();
		}
		return false;	
	}
	

}

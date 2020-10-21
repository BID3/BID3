package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.HashMap;

import com.apimisuse.tokens.TokenUtils;
import com.apimisuse.utils.FileIO;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtInvocationImpl;

/*
 * 
 * (1) "java.sql.Connection.createStatement()":
 * if createStatement in a  loop or object of crateStatement call "execute.." in a loop
 * (2) "java.util.concurrent.Executors.newCachedThreadPool()":
 * object of crateStatement call "execute.." in a loop
 * 
 */
public class CrtStatePattern extends BugPattern {	
	
	
	HashMap<String,SourcePosition> VarAndPos = new HashMap<>();// varName:position
	ArrayList<String> invocInLoop = new ArrayList<String>(); 
		
	/*
	 * 
	 */
	CrtStatePattern(String pName){
		super(pName);
	}
	
	public void invoc(CtElement ct, SourcePosition srcPos, String cleanedInvoc) {
		if(cleanedInvoc.contains(bugPatternName)){
			CtInvocationImpl cti = (CtInvocationImpl) ct;
			String varName = TokenUtils.simpleName(ct);
			VarAndPos.put(varName, srcPos);
		}
	}
	public void detect() {
		if(VarAndPos.keySet().size() > 0){
			for(String varName : VarAndPos.keySet()) {								
				boolean inLoop = NonInvocation.ifInLoop(varName,bugPatternName,invocInLoop);
				if (inLoop) {
					String pos = VarAndPos.get(varName).toString();
					FileIO.writeToJSON(pos,FileIO.finalResult,bugPatternName);
				}
			}
		}					
	}
	public void updateLoop(CtElement ct) {
		ArrayList<CtInvocationImpl> allInvocs = (ArrayList<CtInvocationImpl>) ct.getElements(new TypeFilter(CtInvocationImpl.class));
		for (CtInvocationImpl invoc : allInvocs) {
			String name = invoc.getExecutable().getSimpleName();
			if (invoc.getTarget() != null) {
				String receiver = invoc.getTarget().toString();
				invocInLoop.add(receiver + "." + name);
			}			
		}
	}

}

package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.HashMap;

import com.apimisuse.utils.FileIO;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtAssignmentImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtVariableReadImpl;

public class StringEquals extends BugPattern{
	HashMap<String,ArrayList<CtElement>> StringMap = new HashMap<>();  // String a: {a.equals,a.subset... }
	HashMap<String,CtExpression> localVInforMap = new HashMap<>();
	
	StringEquals(String pName){
		super(pName);
	}
	
	public void preDetect(ArrayList<CtElement> allElements) {
		getMapVariable(allElements);
	}
		
	private void getMapVariable(ArrayList<CtElement> allElements) {
		for(CtElement ct:allElements) {
			if(ct instanceof CtLocalVariableImpl) {
				CtLocalVariableImpl ctLocalV = (CtLocalVariableImpl) ct;
				CtExpression CtAssign = ctLocalV.getAssignment();
				if(CtAssign != null) {
					boolean con = !(CtAssign instanceof CtLiteralImpl)&& !CtAssign.toString().equals("null")&&
								ctLocalV.getType().toString().equals("java.lang.String");
				
					if(con) {
						String name = ctLocalV.getSimpleName();
						StringMap.put(name, new ArrayList<CtElement>());
						localVInforMap.put(name, CtAssign);
					}							
				}
			}
		}
	}
	public void getLocalV(CtElement ct) {
		if(ct instanceof CtVariableReadImpl) { 
			CtVariableReadImpl ctv = (CtVariableReadImpl) ct;
			String v = ctv.toString();
			if (StringMap.containsKey(v)) {
				StringMap.get(v).add(ctv.getParent());
			}
		}
		if(ct instanceof CtAssignmentImpl) { 
			CtAssignmentImpl ctV = (CtAssignmentImpl) ct;	
			String ctvString = ctV.getAssigned().toString();
			if (StringMap.containsKey(ctvString)) {
				StringMap.get(ctvString).add(ctV);
			}
		}
	}

	public void detect() {
		for(String key : StringMap.keySet()) {
			ArrayList<CtElement> values = StringMap.get(key);
			if(values.size() > 0) {
				CtElement firstE = values.get(0);
				if (firstE instanceof CtInvocationImpl) {
					CtInvocationImpl cti = (CtInvocationImpl) firstE;
					if(cti.getTarget() != null) {
						if(getAllCtLiteral(localVInforMap.get(key)).size() == 0 && cti.getExecutable().getSimpleName().equals("equals") && cti.getTarget().toString().equals(key)) {
							FileIO.writeToJSON(firstE.getPosition().toString(), FileIO.finalResult, bugPatternName);
						}
					}
				}
			}
		}		
	}
	private static ArrayList<CtLiteralImpl> getAllCtLiteral(CtExpression CtAssign) {
		ArrayList<CtLiteralImpl> alle = (ArrayList<CtLiteralImpl>) CtAssign.getElements(new TypeFilter(CtLiteralImpl.class));
		return alle;
	}
	
}

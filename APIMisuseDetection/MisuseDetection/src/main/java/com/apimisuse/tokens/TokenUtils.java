package com.apimisuse.tokens;

import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;

public class TokenUtils {
	
	//if a method call "ct" return a value, this function returns the simple name of the value
	// For exmaple, a b = c.ct()  this function returns "b" 
	public static String simpleName(CtElement ct) {
		String varName = "";
		CtElement parent = ((CtInvocationImpl) ct).getParent();
		if (parent instanceof CtLocalVariableImpl ){
			CtLocalVariableImpl varParent = (CtLocalVariableImpl) parent;
			varName = varParent.getSimpleName();										
		}			
		return varName;
	}
}

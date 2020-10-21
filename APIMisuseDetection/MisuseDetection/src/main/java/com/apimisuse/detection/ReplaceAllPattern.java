package com.apimisuse.detection;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.apimisuse.utils.FileIO;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtInvocationImpl;

/*
 * java.lang.String.replaceAll(java.lang.String,java.lang.String)
 */
public class ReplaceAllPattern extends BugPattern {
	
	ReplaceAllPattern(String pName){
		super(pName);
	}
	
	public void invoc(CtElement ct, SourcePosition srcPos, String cleanedInvoc) {
		if(cleanedInvoc.contains(bugPatternName)){
			CtInvocationImpl cti = (CtInvocationImpl) ct;
			boolean ifReg = false;
			boolean rightReg;
			Pattern p = null;
			CtElement arg1 = (CtElement)cti.getArguments().get(0);
			if(arg1 instanceof CtLiteral) {							
				try {
					p = Pattern.compile(arg1.toString());
					rightReg = true;
				}catch (PatternSyntaxException e) {
					rightReg = false;
					FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);
				}
				if(rightReg) {
					ifReg = p.matcher(arg1.toString()).matches();
					if(ifReg) {
						FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);
					}
				}
			}
		}
	}
}

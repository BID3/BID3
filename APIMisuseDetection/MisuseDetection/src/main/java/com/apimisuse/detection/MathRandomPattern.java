package com.apimisuse.detection;

import com.apimisuse.utils.FileIO;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

/*
 * Pattern is buggy if used, for example:
 * 
 * java.lang.Math.random()
 * 
 * 
 */
public class MathRandomPattern extends BugPattern {
	
	MathRandomPattern(String pName){
		super(pName);
	}
	
	public void invoc(SourcePosition srcPos, String cleanedInvoc) {
		if(cleanedInvoc.contains(bugPatternName)) {
			FileIO.writeToJSON(srcPos.toString(), FileIO.finalResult, bugPatternName);
		}
	}

}

package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.HashMap;

import com.apimisuse.utils.FileIO;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLoopImpl;

public class FileWriterPattern extends BugPattern{
	boolean noBuffer;
	
	FileWriterPattern(String pName){
		super(pName);
	}
	
	public void hasBuffer(CtMethod m) {
		String bodyString = m.toString();
		if(bodyString.contains("FileWriter")) {
			if(!bodyString.contains("BufferedWriter") && !bodyString.contains("PrintWriter")) {
				noBuffer = true;
			}
		}
	}
		
	public void invoc(CtMethod m, CtElement ct, SourcePosition srcPos, String cleanedInvoc) {
		if(cleanedInvoc.contains(bugPatternName) && noBuffer){
			CtInvocationImpl cti = (CtInvocationImpl) ct;
			boolean inLoop = NonInvocation.ifLoopAsParent(cti);
			if (inLoop) {
				if(cti.getTarget()!=null) {					
					String obj = cti.getTarget().toString();
					String mLoc = m.getPosition().toString() + "--" + obj;
					String lineNumber = Integer.toString(cti.getPosition().getLine());
					FileIO.writeToJSON(mLoc + "--" + lineNumber, FileIO.finalResult, bugPatternName);
				}
			}
		}
	}
}

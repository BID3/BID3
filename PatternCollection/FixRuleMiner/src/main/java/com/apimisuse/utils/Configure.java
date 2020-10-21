package com.apimisuse.utils;

import com.apimisuse.rule.node.NodeType;

public class Configure {

	public static String[] rulesForPrint = {"", 
			NodeType.MethodName, 
			NodeType.MethodArg, 
			NodeType.Method,
			NodeType.FieldRead, 
			NodeType.FieldWrite};
	
	public static final String TAG_CONTEXT = ":";
	public static final String TAG_TO = "=>";
	public static final String TAG_SPLITARGS = "FORSPLITARGS";
	
}

package com.apimisuse.rule;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.apimisuse.rule.node.API;
import com.apimisuse.rule.node.ChangeTags;
import com.apimisuse.utils.Configure;

import gumtree.spoon.diff.operations.OperationKind;

public class ChangeRule
{
	private OperationKind type;
	private String pattern;
	private String content;
	private String fileName;
	private String[] archiveInfor;
	public final static String SEP = " ------> ";
	public boolean parseTypeFailed;
	Set<ChangeTags> categories = null;
	private Set<API> retAPIs;
	private Set<API> apisInBody;

	public ChangeRule(OperationKind type, String pattern, Set<ChangeTags> categories, boolean parseTypeFailed, String content, String fileName,String[] archiveInfor) {
		this.type = type;
		this.pattern = pattern;
		this.categories = categories;
		this.parseTypeFailed= parseTypeFailed;
		this.content = content.replaceAll("\\s+", " ");
		this.fileName = fileName;
		this.archiveInfor = archiveInfor;
		this.retAPIs = new HashSet<>();
		this.apisInBody = new HashSet<>();
	}
	
	public void setRetApis(Set<API> apis) {
		this.retAPIs = apis;
	}
	
	public void setBodyUseAPI(Set<API> apis) {
		this.apisInBody = apis;
	}

	public OperationKind getType() {
		return type;
	}

	public String getPattern() {
		return pattern;
	}

	public String getContent() {
		return content;
	}

	public String toString() {
		return (type + SEP + pattern + SEP + new ArrayList<>(categories) + SEP + content + SEP + fileName).replace(Configure.TAG_SPLITARGS, "");
	}
	
	public void toJSON(String path) throws IOException{
		JSONObject obj = new JSONObject();
		String cont = content.split(SEP)[0];
		String line = content.split(SEP)[1];
//		String tag = content.split(SEP)[2];
		obj.put("Type", type.toString());
	    obj.put("Pattern", pattern);
	    obj.put("Content", cont);
	    obj.put("FileName", fileName);
	    obj.put("Line", line);
	    obj.put("Url",archiveInfor[0]);
	    obj.put("Fixed commit",archiveInfor[1]);
	    obj.put("Date", archiveInfor[2]);
	    //JSONArray array = new JSONArray();
	    //array.addAll(categories);
	    obj.put("BugDetectionTag", categories.toString());
	    JSONArray jsonArray = new JSONArray();
	    for(API api : retAPIs) {
	    		jsonArray.add(api.toString());
	    }
	    obj.put("RetCheckAPI", jsonArray);
	    StringBuffer buffer = new StringBuffer();
	    jsonArray = new JSONArray();
	    for(API api : apisInBody) {
	    		jsonArray.add(api.toString());
	    }
	    obj.put("BodyUseAPI", jsonArray);
	    obj.put("parseTypeFail", this.parseTypeFailed ? "fail" : "sucess");
	    FileWriter file = new FileWriter(path,true);
	    file.write(obj.toJSONString().replace(Configure.TAG_SPLITARGS, ""));	
	    file.write("\n");	
	    file.flush();
	    file.close();
	}

}

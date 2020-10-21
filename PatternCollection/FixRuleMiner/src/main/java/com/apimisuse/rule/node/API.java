package com.apimisuse.rule.node;

import java.util.List;

import com.apimisuse.rule.node.utils.NodeUtils;
import com.github.gumtreediff.utils.Pair;

public class API {
	
	private Pair<String, String> ret;
	private Pair<String, String> receiver;
	private String name;
	private List<Pair<String, String>> args;
	
	public API(Pair<String, String> ret, Pair<String, String> clazz, String name, List<Pair<String, String>> args) {
		this.ret = ret;
		this.receiver = clazz;
		this.name = name;
		this.args = args;
	}
	
	public void setRetValue(String type, String expr) {
		ret = new Pair<String, String>(type, expr);
	}
	
	public Pair<String, String> getClazz() {
		return this.receiver;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<Pair<String, String>> getArgs() {
		return this.args;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		String type = "?", name = "?";
//		if (this.ret != null) {
//			if (this.ret.getFirst() != null) {
//				type = this.ret.getFirst();
//			}
//			if (this.ret.getSecond() != null) {
//				name = this.ret.getSecond();
//			}
//		}
//		//buffer.append(type + "::" + name + "=");
//		
//		buffer.append(name).append("=");
		type = name = null;
		if (this.receiver != null) {
			if (this.receiver.getFirst() != null) {
				type = this.receiver.getFirst();
			}
			if (this.receiver.getSecond() != null) {
				name = this.receiver.getSecond();
			}	
		}
		if (!NodeUtils.showType(type)) {
			type = name;
		}
//		buffer.append(type + "::" + name).append(".")
		buffer.append(type == null ? "" : type + ".")
		.append(this.name).append("(");
		for (int i = 0; i < this.args.size(); i++) {
			type = name = "?";
			if (this.args.get(i).getFirst() != null) {
				type = this.args.get(i).getFirst();
			}
			if (this.args.get(i).getSecond() != null) {
				name = this.args.get(i).getSecond();
			}
			if (!NodeUtils.showType(type)) {
				type = name;
			}
//			buffer.append(i == 0 ? "" : ",").append(type + "::" + name);
			buffer.append(i == 0 ? "" : ",").append(type);
		}
		buffer.append(")");
		return buffer.toString();
	}
	

}

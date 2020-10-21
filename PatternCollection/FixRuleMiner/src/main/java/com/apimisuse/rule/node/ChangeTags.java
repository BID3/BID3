package com.apimisuse.rule.node;

public enum ChangeTags {
	IF_NULL("IfNull"),
	IF_RET("IfRet"),
	IF_DEPEND("IfDep"),
	IF_OTHER("IfOther"),
	
	INV_CHG_ARG("ArgChange"),
	INV_CHG_METHOD("MethodChange"),
	INV_CHG_NAME("NameChange"),
	INV_RECEIVER("Reciever"),
	
	TRY("Try"),
	CATCH("Catch"),
	FINALLY("Finally"),
	SYNC("Synchronized");
	
	private String value;
	private ChangeTags(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
	
}

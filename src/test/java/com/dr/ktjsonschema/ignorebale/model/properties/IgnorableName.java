package com.dr.ktjsonschema.ignorebale.model.properties;

import com.dr.ktjsonschema.annotations.JsonSchemaInputModelIgnore;
import com.dr.ktjsonschema.annotations.JsonSchemaOutputModelIgnore;

public class IgnorableName {
	@JsonSchemaInputModelIgnore
	@JsonSchemaOutputModelIgnore
	private String first;

	@JsonSchemaOutputModelIgnore
	private String last;

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

}

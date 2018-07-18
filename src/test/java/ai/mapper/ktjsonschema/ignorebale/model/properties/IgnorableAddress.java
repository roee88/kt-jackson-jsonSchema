package ai.mapper.ktjsonschema.ignorebale.model.properties;

import ai.mapper.ktjsonschema.annotations.JsonSchemaInputModelIgnore;
import ai.mapper.ktjsonschema.annotations.JsonSchemaOutputModelIgnore;

public class IgnorableAddress {
	private Integer number;
	@JsonSchemaInputModelIgnore
	private String Street;
	@JsonSchemaOutputModelIgnore
	private Integer zip;

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getStreet() {
		return Street;
	}

	public void setStreet(String street) {
		Street = street;
	}

	public Integer getZip() {
		return zip;
	}

	public void setZip(Integer zip) {
		this.zip = zip;
	}

}
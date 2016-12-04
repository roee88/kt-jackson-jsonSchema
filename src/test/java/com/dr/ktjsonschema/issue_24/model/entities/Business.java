package com.dr.ktjsonschema.issue_24.model.entities;

import com.dr.ktjsonschema.issue_24.model.AbstractObject;
import com.dr.ktjsonschema.issue_24.model.properties.Address;
import com.dr.ktjsonschema.issue_24.model.properties.Name;

public class Business extends AbstractObject {
	private Address employment;
	private Name name;

	public Address getEmployment() {
		return employment;
	}

	public void setEmployment(Address employment) {
		this.employment = employment;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

}

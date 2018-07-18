package ai.mapper.ktjsonschema.issue_24.model.entities;

import ai.mapper.ktjsonschema.issue_24.model.AbstractObject;
import ai.mapper.ktjsonschema.issue_24.model.properties.Address;
import ai.mapper.ktjsonschema.issue_24.model.properties.Name;

public class Person extends AbstractObject {
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

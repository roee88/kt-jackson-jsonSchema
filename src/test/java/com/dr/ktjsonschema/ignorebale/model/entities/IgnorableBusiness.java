package com.dr.ktjsonschema.ignorebale.model.entities;

import com.dr.ktjsonschema.annotations.JsonSchemaOutputModelIgnore;
import com.dr.ktjsonschema.ignorebale.model.AbstractObject;
import com.dr.ktjsonschema.ignorebale.model.properties.IgnorableAddress;
import com.dr.ktjsonschema.ignorebale.model.properties.IgnorableName;

public class IgnorableBusiness extends AbstractObject {
	@JsonSchemaOutputModelIgnore
  private IgnorableAddress employment;

	private IgnorableName ignorableName;

	public IgnorableAddress getEmployment() {
		return employment;
	}

	public void setEmployment(IgnorableAddress employment) {
		this.employment = employment;
	}

	public IgnorableName getIgnorableName() {
		return ignorableName;
	}

	public void setIgnorableName(IgnorableName ignorableName) {
		this.ignorableName = ignorableName;
	}

}

package ai.mapper.ktjsonschema.ignorebale.model.entities;

import ai.mapper.ktjsonschema.ignorebale.model.AbstractObject;
import ai.mapper.ktjsonschema.ignorebale.model.properties.IgnorableAddress;
import ai.mapper.ktjsonschema.ignorebale.model.properties.IgnorableName;

public class IgnorablePerson extends AbstractObject {
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

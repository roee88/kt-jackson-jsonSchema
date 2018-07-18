package ai.mapper.ktjsonschema.ignorebale.model.entities;

import ai.mapper.ktjsonschema.annotations.JsonSchemaOutputModelIgnore;
import ai.mapper.ktjsonschema.ignorebale.model.AbstractObject;
import ai.mapper.ktjsonschema.ignorebale.model.properties.IgnorableAddress;
import ai.mapper.ktjsonschema.ignorebale.model.properties.IgnorableName;

import java.util.List;

public class IgnorableBusiness extends AbstractObject {
	@JsonSchemaOutputModelIgnore
  private IgnorableAddress employment;

	private IgnorableName ignorableName;

  private List<IgnorableName> ignorableNameList;

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

  public List<IgnorableName> getIgnorableNameList() {
    return ignorableNameList;
  }

  public void setIgnorableNameList(List<IgnorableName> ignorableNameList) {
    this.ignorableNameList = ignorableNameList;
  }

}

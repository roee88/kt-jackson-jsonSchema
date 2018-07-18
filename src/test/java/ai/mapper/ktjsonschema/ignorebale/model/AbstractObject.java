package ai.mapper.ktjsonschema.ignorebale.model;

import ai.mapper.ktjsonschema.ignorebale.model.entities.IgnorableBusiness;
import ai.mapper.ktjsonschema.ignorebale.model.entities.IgnorablePerson;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = IgnorablePerson.class, name = "IgnorablePerson"),
		@JsonSubTypes.Type(value = IgnorableBusiness.class, name = "IgnorableBusiness") })
public abstract class AbstractObject {

}

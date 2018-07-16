package com.dr.ktjsonschema.ignorebale.model;

import com.dr.ktjsonschema.ignorebale.model.entities.IgnorableBusiness;
import com.dr.ktjsonschema.ignorebale.model.entities.IgnorablePerson;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = IgnorablePerson.class, name = "IgnorablePerson"),
		@JsonSubTypes.Type(value = IgnorableBusiness.class, name = "IgnorableBusiness") })
public abstract class AbstractObject {

}

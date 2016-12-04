package com.dr.ktjsonschema.issue_24.model;

import com.dr.ktjsonschema.issue_24.model.entities.Business;
import com.dr.ktjsonschema.issue_24.model.entities.Person;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = Person.class, name = "Person"),
		@JsonSubTypes.Type(value = Business.class, name = "Business") })
public abstract class AbstractObject {

}

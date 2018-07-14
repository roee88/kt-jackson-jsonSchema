package com.dr.ktjsonschema.Issue23.model;

import com.dr.ktjsonschema.annotations.JsonSchemaInputModelIgnore;

import java.util.Map;

public class Dummy {
    //@JsonSchemaInputModelIgnore
    public int x;
    public Map<String, Color> colors;
}

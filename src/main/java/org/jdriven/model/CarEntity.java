package org.jdriven.model;

import org.jdriven.annotation.Entity;

@Entity
public record CarEntity(Integer id, String make, String model, int year) {}

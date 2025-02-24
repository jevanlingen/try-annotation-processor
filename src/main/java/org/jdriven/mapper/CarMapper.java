package org.jdriven.mapper;

import org.jdriven.Mapper;
import org.jdriven.model.Car;
import org.jdriven.model.CarEntity;

@Mapper
public interface CarMapper {
    Car map(CarEntity car);
    CarEntity map(Car car);
}

package org.jdriven.mapper;

import org.jdriven.Mapper;
import org.jdriven.Mappers;
import org.jdriven.model.Car;
import org.jdriven.model.CarEntity;

@Mapper
public interface CarMapper {
    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);

    Car map(CarEntity car);
    CarEntity map(Car car);
}

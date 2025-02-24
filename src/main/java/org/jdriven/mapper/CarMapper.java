package org.jdriven.mapper;

import org.jdriven.Mapper;
import org.jdriven.model.Car;
import org.jdriven.model.CarEntity;

import static org.jdriven.MapperUtil.use;

@Mapper
public interface CarMapper {
    CarMapper INSTANCE = use(CarMapper.class);

    Car map(CarEntity car);
    CarEntity map(Car car);
}

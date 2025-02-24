package org.jdriven;

import org.jdriven.mapper.CarMapper;
import org.jdriven.model.CarEntity;

import static org.jdriven.MapperUtil.use;

public class Starter {
    public static void main(String[] args) {
        var carEntity = new CarEntity(3, "BMW", "X2 M35i xDrive", 2025);
        var car = use(CarMapper.class).map(carEntity);

        System.out.println(car.toString());
    }
}

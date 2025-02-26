package org.jdriven;

import org.jdriven.mapper.CarMapper;
import org.jdriven.model.CarEntity;
import org.jdriven.model.Dog;

public class Starter {
    public static void main(String[] args) {
        var carEntity = new CarEntity(3, "BMW", "X2 M35i xDrive", 2025);
        var car = CarMapper.INSTANCE.map(carEntity);

        System.out.println(car.toString());

        // Enable for compile error:
        //List.of("first").get(0);

        System.out.println(new Dog("Max", 3));
    }
}

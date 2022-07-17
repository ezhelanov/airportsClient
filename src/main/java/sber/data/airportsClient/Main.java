package sber.data.airportsClient;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        AirportsClient airportsClient = new AirportsClient();

        Set<String> params = new HashSet<>();
        Collections.addAll(params, args);

        params.stream()
                .filter(NumberUtils::isParsable)
                .map(NumberUtils::toInt)
                .filter(param -> param >= 0)
                .collect(Collectors.toList())
                .parallelStream()
                .forEach(airportsClient::doPost);
    }

}

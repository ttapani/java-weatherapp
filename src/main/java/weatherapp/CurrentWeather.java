package weatherapp;

import java.util.List;

// Class for GSon to decode JSON downloaded from openweathermap.com
public class CurrentWeather {

    // Weather phenomena
    static class Weather {
        String description;
        String icon;
    }

    static class Main {
        double temp;
    }

    // Cloud coverage
    static class Clouds {
        String all;
    }

    // Wind
    static class Wind {
        double speed;
        double deg;
    }

    // Internal parameters for openweathermap, but also city's country code
    // which can help distinguish the correct one
    static class Sys {
        String country;
    }
    String dt;
    Main main;
    List<Weather> weather;
    Clouds clouds;
    Wind wind;
    String name;
    Sys sys;
}
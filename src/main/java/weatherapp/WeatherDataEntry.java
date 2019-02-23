package weatherapp;

import java.util.Calendar;

// Data structure for moving data from worker thread to UI thread
public class WeatherDataEntry {
    public String cityName;
    public String country;
    public Calendar time;
    public String weather;
    public String weatherIcon;
    public double temperature;
    public double windDirection;
    public double windSpeed;
}

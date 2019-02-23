package weatherapp;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.google.gson.*;

import javax.swing.*;

class GetCurrentWeatherTask extends SwingWorker<WeatherDataEntry, Integer> {
    private final String cityName;
    private final JFrame parent;

    GetCurrentWeatherTask(String city, JFrame parent) {
        this.cityName = city;
        this.parent = parent;
    }

    @Override
    public WeatherDataEntry doInBackground() {
        // Hardcoded app key for testing..
        final String address = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&APPID=eb4836697d2cdb9246701fb7a78ba3a5";
        try {
            URL url = new URL(address);
            try
            {
                // Read the http response and pass it to Gson for decoding
                Reader reader = new InputStreamReader(url.openStream());
                CurrentWeather result = new Gson().fromJson(reader, CurrentWeather.class);

                WeatherDataEntry data = new WeatherDataEntry();
                data.cityName = result.name;
                data.country = result.sys.country;
                Calendar newTime = new GregorianCalendar();
                TimeZone tz = TimeZone.getTimeZone("Europe/Helsinki");
                newTime.setTimeZone(tz);
                newTime.setTimeInMillis((long)Integer.parseInt((result.dt))*1000);
                data.time = newTime;
                // We naively assume that this list will only have one item, or that the first item is the one we are
                // interested in, as in, the major weather phenomena
                data.weather = result.weather.get(0).description;
                data.weatherIcon = result.weather.get(0).icon;
                data.temperature = result.main.temp - 273.15;
                data.windDirection = result.wind.deg;
                data.windSpeed = result.wind.speed;
                return data;
            }
            catch (UnknownHostException e) {
                System.out.println("Error occured: " + e.getMessage());
                JOptionPane.showMessageDialog(this.parent, "No connection", "Warning", JOptionPane.WARNING_MESSAGE);
                System.err.println(e);
            }
        }
        catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
            JOptionPane.showMessageDialog(this.parent, "Malformed city name","Warning", JOptionPane.WARNING_MESSAGE);
            System.err.println(e);
        }
        return null;
    }
}

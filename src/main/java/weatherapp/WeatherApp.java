package weatherapp;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class WeatherApp {
    // UI Components
    private JFrame frame;
    private JLabel cityNameLabel;
    private JLabel weatherTimeLabel;
    private JLabel weatherIconLabel;
    private JLabel temperatureLabel;
    private JLabel windDirectionLabel;
    private JLabel windSpeedLabel;
    private JLabel timeUpdatedLabel;

    // Variables that control data retrieval
    private String weatherLocation;
    private int checkWeatherPeriod;
    private Timer weatherTimer;

    // Main that initializes the app and GUI
    public static void main(String[] args) {
        WeatherApp app = new WeatherApp();
        app.createAndShowGUI();
    }

    // Creates GUI, assigns actionlisteners, timers and shows GUI
    private void createAndShowGUI() {
        // Create main window
        frame = new JFrame("WeatherApp");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create panel that holds the controls and general information
        JPanel header = new JPanel();
        header.setLayout(new GridLayout(0,3));

        cityNameLabel = new JLabel("Tampere");
        cityNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeUpdatedLabel = new JLabel("Päivitetty");
        timeUpdatedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JButton selectCityButton = new JButton("Valitse kaupunki");
        ActionListener selectCityClicked = new OnChangeCityClicked();
        selectCityButton.addActionListener(selectCityClicked);
        header.add(cityNameLabel);
        header.add(timeUpdatedLabel);
        header.add(selectCityButton);
        frame.getContentPane().add(header, BorderLayout.NORTH);

        // Create lower panel that holds the actual presented data, fill with blanks or placeholders
        JPanel weatherDataRow = new JPanel();
        Border blackline = BorderFactory.createLineBorder(Color.black);
        weatherDataRow.setBorder(blackline);
        weatherDataRow.setLayout(new GridLayout(0, 5));

        weatherTimeLabel = new JLabel("--:--");
        weatherTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        weatherIconLabel = new JLabel();
        Dimension iconDimension = new Dimension(50, 50);
        weatherIconLabel.setSize(50, 50);
        weatherIconLabel.setMinimumSize(iconDimension);
        weatherIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        temperatureLabel = new JLabel("-");
        temperatureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        windDirectionLabel = new JLabel();
        windDirectionLabel.setSize(50, 50);
        windDirectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        windSpeedLabel = new JLabel("- m/s");
        windSpeedLabel.setHorizontalAlignment(SwingConstants.CENTER);

        weatherDataRow.add(weatherTimeLabel);
        weatherDataRow.add(weatherIconLabel);
        weatherDataRow.add(temperatureLabel);
        weatherDataRow.add(windDirectionLabel);
        weatherDataRow.add(windSpeedLabel);

        frame.getContentPane().add(weatherDataRow);

        // By default, we check for new weather every 10 minutes, which is the suggested rate by openweathermap
        checkWeatherPeriod = 60 * 10 * 1000;

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Method that is called after setting a city and starts the timer that reads the data every 10 minutes
    // first retrieval is done 1 second after confirming
    private void initiateTimedRetrieval() {
        TimerTask getCurrentWeather = new TimerTask() {
            @Override
            public void run() {
                getCurrentWeather();
            }
        };
        weatherTimer = new Timer();
        weatherTimer.scheduleAtFixedRate(getCurrentWeather, 1000, checkWeatherPeriod);
    }

    // Method that initiates data retrieval
    private void getCurrentWeather() {
        // Spawn a swing worker thread to get the data safely
        GetCurrentWeatherTask task = new GetCurrentWeatherTask(weatherLocation, this.frame);
        try {
            task.execute();
        }
        catch (Exception e) {
            System.err.println(e);
        }

        WeatherDataEntry current;
        try {
            current = task.get();
            updateCurrentWeather(current);
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    // Updates the GUI with data received from internet
    private void updateCurrentWeather(WeatherDataEntry data) {
        cityNameLabel.setText(data.cityName + ", " + data.country);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        weatherTimeLabel.setText(sdf.format(data.time.getTime()));

        // Get the icon supplied by openweathermap, add description for mouseover
        String iconPath = "http://openweathermap.org/img/w/" + data.weatherIcon + ".png";
        ImageIcon icon = createImageIcon(iconPath, data.weather);
        weatherIconLabel.setIcon(icon);
        weatherIconLabel.setToolTipText(data.weather);

        // Format temperature into accuracy of 0.1 m/s before presenting
        String temperatureString = String.format("%.1f", data.temperature);
        temperatureLabel.setText(temperatureString  + "°C");

        // This was supposed to be a cool, java2d drawn arrow that was translated with affinetransform to point
        // where the wind is blowing, but alas, the maker was too ambitious with too little time :(
        String windDirectionString = String.format("%.1f", data.windDirection);
        windDirectionLabel.setText(windDirectionString + "°");

        // Format wind speed into accuracy of 0.1 m/s before presenting
        String windSpeedString = String.format("%.1f", data.windSpeed);
        windSpeedLabel.setText(windSpeedString + " m/s");

        // Finally, also update the "last time downloaded"-label with current time
        Calendar newTime = new GregorianCalendar();
        TimeZone tz = TimeZone.getTimeZone("Europe/Helsinki");
        newTime.setTimeZone(tz);
        timeUpdatedLabel.setText("Päivitetty: " + sdf.format(newTime.getTime()));

        // Repack the gui to make room for the data
        frame.pack();
    }

    // Creates input dialog that lets user change the target city
    public class OnChangeCityClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            weatherLocation = JOptionPane.showInputDialog(frame, "Type city name");
            initiateTimedRetrieval();
            //getCurrentWeather();
        }
    }

    // Helper method that retrieves images from openweathermap and returns images for UI use
    // Inspired by stackoverflow
    private static ImageIcon createImageIcon(String path,
                                        String description) {
        URL imgURL = null;
        try {
            imgURL = new URL(path);
        }
        catch (MalformedURLException e) {
            System.err.println("Exception happened: " + e.toString());
        }

        Image image;
        ImageIcon icon;
        try {
            image = ImageIO.read(imgURL);
            if (image != null) {
                icon = new ImageIcon(image, description);
            } else {
                System.err.println("Couldn't find file: " + path);
                icon = null;
            }
        }
        catch (IOException e) {
            System.err.println("Exception happened: " + e.toString());
            icon = null;
        }
        return icon;
    }
}
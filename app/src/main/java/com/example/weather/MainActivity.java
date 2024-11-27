package com.example.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tempDisplay, cityInfo, errorText, dayStatusID;
    private TextView dayOneTemp, dayTwoTemp, dayThreeTemp, dayFourTemp, dayFiveTemp;
    private TextView dayOneDay, dayTwoDay, dayThreeDay, dayFourDay, dayFiveDay;
    private ImageView dayOneStat, dayTwoStat, dayThreeStat, dayFourStat, dayFiveStat;
    private ProgressBar loader;
    private RequestQueue requestQueue;

    private ImageView airQualityImage;
    private TextView airQualityDetails;

    private final String API_KEY = "1fc995c0d7111798a72d0acd159e5d19";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        airQualityImage = findViewById(R.id.airpolDisplay);

        airQualityDetails = findViewById(R.id.airQualityDetails);

        tempDisplay = findViewById(R.id.tempDisplayID);
        cityInfo = findViewById(R.id.cityInfoID);
        errorText = findViewById(R.id.errorText);
        loader = findViewById(R.id.loader);

        dayOneTemp = findViewById(R.id.dayOneTempDisplay);
        dayTwoTemp = findViewById(R.id.dayTwoTempDisplay);
        dayThreeTemp = findViewById(R.id.dayThreeTempDisplay);
        dayFourTemp = findViewById(R.id.dayFourTempDisplay);
        dayFiveTemp = findViewById(R.id.dayFiveTempDisplay);

        dayOneDay = findViewById(R.id.dayOneDayDisplay);
        dayTwoDay = findViewById(R.id.dayTwoDayDisplay);
        dayThreeDay = findViewById(R.id.dayThreeDayDisplay);
        dayFourDay = findViewById(R.id.dayFourDayDisplay);
        dayFiveDay = findViewById(R.id.dayFiveDayDisplay);

        dayOneStat = findViewById(R.id.dayOneStatDisplay);
        dayTwoStat = findViewById(R.id.dayTwoStatDisplay);
        dayThreeStat = findViewById(R.id.dayThreeStatDisplay);
        dayFourStat = findViewById(R.id.dayFourStatDisplay);
        dayFiveStat = findViewById(R.id.dayFiveStatDisplay);


        requestQueue = Volley.newRequestQueue(this);


        Button cityOne = findViewById(R.id.cityOne);
        Button cityTwo = findViewById(R.id.cityTwo);
        Button cityThree = findViewById(R.id.cityThree);

        cityOne.setOnClickListener(v -> fetchWeatherAndAirQuality("Riyadh"));
        cityTwo.setOnClickListener(v -> fetchWeatherAndAirQuality("New York"));
        cityThree.setOnClickListener(v -> fetchWeatherAndAirQuality("Dhaka"));
    }

    private void fetchWeatherAndAirQuality(String cityName) {
        fetchWeatherData(cityName);
        fetchAirQuality(cityName);

    }

    private void fetchAirQuality(String cityName) {
        // Geocoding API to get lat/lon of the city
        String geoUrl = BASE_URL + "weather?q=" + cityName + "&appid=" + API_KEY;

        JsonObjectRequest geoRequest = new JsonObjectRequest(Request.Method.GET, geoUrl, null,
                response -> {
                    try {
                        JSONObject coord = response.getJSONObject("coord");
                        double lat = coord.getDouble("lat");
                        double lon = coord.getDouble("lon");

                        // Fetch air pollution using coordinates
                        fetchAirPollutionData(lat, lon);
                    } catch (Exception e) {
                        airQualityDetails.setText("Error fetching air quality: Location parsing failed.");
                    }
                },
                error -> airQualityDetails.setText("Error fetching air quality: Unable to fetch location.")
        );

        requestQueue.add(geoRequest);
    }


    private void fetchAirPollutionData(double lat, double lon) {
        String pollutionUrl = BASE_URL + "air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;

        JsonObjectRequest pollutionRequest = new JsonObjectRequest(Request.Method.GET, pollutionUrl, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONArray("list").getJSONObject(0).getJSONObject("main");
                        int aqi = main.getInt("aqi");

                        // Convert AQI to descriptive text
                        String airQuality = getAirQualityDescription(aqi);
                        airQualityDetails.setText("Air Quality: " + airQuality);

                        airQualityImage.setImageResource(getAirQualityImage(aqi));
                    } catch (Exception e) {
                        airQualityDetails.setText("Error parsing air quality data.");
                    }
                },
                error -> airQualityDetails.setText("Error fetching air quality data.")
        );

        requestQueue.add(pollutionRequest);
    }


    private void fetchWeatherData(String cityName) {
        loader.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        String url = BASE_URL + "forecast?q=" + cityName + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse city information
                        JSONObject city = response.getJSONObject("city");
                        cityInfo.setText(city.getString("name") + ", " + city.getString("country"));

                        JSONArray list = response.getJSONArray("list");

                        // Current weather
                        JSONObject currentWeather = list.getJSONObject(0);
                        JSONObject main = currentWeather.getJSONObject("main");
                        tempDisplay.setText(String.format("%s°C", main.getString("temp")));

                        // Display day and date for current weather
                        String dateTime = currentWeather.getString("dt_txt");
                        String formattedDayDate = formatDate(dateTime);
                        TextView dayStatus = findViewById(R.id.dayStatusID);
                        dayStatus.setText(formattedDayDate);

                        // Forecast data for the next 5 days
                        for (int i = 0; i < 5; i++) {
                            int index = i * 8; // 8 intervals per day (3-hour gaps)

                            JSONObject dayWeather = list.getJSONObject(index);
                            JSONObject dayMain = dayWeather.getJSONObject("main");
                            JSONArray weatherArray = dayWeather.getJSONArray("weather");
                            JSONObject weather = weatherArray.getJSONObject(0);

                            String dayTemp = String.format("%s°C", dayMain.getString("temp"));
                            String dayDesc = weather.getString("main");
                            String dayDateTime = dayWeather.getString("dt_txt");
                            String formattedDay = formatDay(dayDateTime); // Format to display day name (e.g., "Tuesday")

                            // Update UI based on index
                            switch (i) {
                                case 0:
                                    dayOneTemp.setText(dayTemp);
                                    dayOneDay.setText(formattedDay);
                                    dayOneStat.setImageResource(getIcon(dayDesc));
                                    break;
                                case 1:
                                    dayTwoTemp.setText(dayTemp);
                                    dayTwoDay.setText(formattedDay);
                                    dayTwoStat.setImageResource(getIcon(dayDesc));
                                    break;
                                case 2:
                                    dayThreeTemp.setText(dayTemp);
                                    dayThreeDay.setText(formattedDay);
                                    dayThreeStat.setImageResource(getIcon(dayDesc));
                                    break;
                                case 3:
                                    dayFourTemp.setText(dayTemp);
                                    dayFourDay.setText(formattedDay);
                                    dayFourStat.setImageResource(getIcon(dayDesc));
                                    break;
                                case 4:
                                    dayFiveTemp.setText(dayTemp);
                                    dayFiveDay.setText(formattedDay);
                                    dayFiveStat.setImageResource(getIcon(dayDesc));
                                    break;
                            }
                        }

                        loader.setVisibility(View.GONE);
                    } catch (Exception e) {
                        loader.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Error parsing weather data.");
                    }
                },
                error -> {
                    loader.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Error fetching weather data.");
                });

        requestQueue.add(jsonObjectRequest);
    }



    private int getIcon(String weather) {
        // Map weather conditions to drawable resources
        switch (weather.toLowerCase()) {
            case "clear":
                return R.drawable.clear_sky;
            case "clouds":
                return R.drawable.cloudyy;
            case "rain":
                return R.drawable.rainy;
            case "snow":
                return R.drawable.snowy;
            case "thunderstorm":
                return R.drawable.thunderstorm;
            default:
                return R.drawable.unknown;
        }
    }
    private String formatDate(String dateTime) {
        try {
            // Input date format from API
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);

            // Desired output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown Date";
        }
    }

    private String formatDay(String dateTime) {
        try {
            // Input date format from API
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);

            // Output format: Day of the week
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private String getAirQualityDescription(int aqi) {
        switch (aqi) {
            case 1: return "Good (1)";
            case 2: return "Fair (2)";
            case 3: return "Moderate (3)";
            case 4: return "Poor (4)";
            case 5: return "Very Poor (5)";
            default: return "Unknown";
        }
    }

    private int getAirQualityImage(int aqi) {
        switch (aqi) {
            case 1:
                return R.drawable.good;
            case 2:
                return R.drawable.fair;
            case 3:
                return R.drawable.moderate;
            case 4:
                return R.drawable.poor;
            case 5:
                return R.drawable.very_poor;
            default:
                return R.drawable.unknown;
        }

    }
}

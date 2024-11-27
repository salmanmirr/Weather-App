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

    private TextView tempDisplay, cityInfo, errorText;
    private TextView dayOneTemp, dayTwoTemp, dayThreeTemp, dayFourTemp, dayFiveTemp;
    private TextView dayOneDay, dayTwoDay, dayThreeDay, dayFourDay, dayFiveDay;
    private ImageView dayOneStat, dayTwoStat, dayThreeStat, dayFourStat, dayFiveStat;
    private ProgressBar loader;
    private RequestQueue requestQueue;
    private TextView airQualityDetails;
    private ImageView airQualityImage;

    private final String API_KEY = "1fc995c0d7111798a72d0acd159e5d19";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        airQualityDetails = findViewById(R.id.airQualityDetails);
        airQualityImage = findViewById(R.id.qualityStatDisplay);

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

    private void fetchWeatherData(String cityName) {
        loader.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        String url = BASE_URL + "forecast?q=" + cityName + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {

                        JSONObject city = response.getJSONObject("city");
                        cityInfo.setText(city.getString("name") + ", " + city.getString("country"));

                        JSONArray list = response.getJSONArray("list");


                        JSONObject currentWeather = list.getJSONObject(0);
                        JSONObject main = currentWeather.getJSONObject("main");
                        tempDisplay.setText(String.format("%s°C", main.getString("temp")));


                        String dateTime = currentWeather.getString("dt_txt");
                        String formattedDayDate = formatDate(dateTime);
                        TextView dayStatus = findViewById(R.id.dayStatusID);
                        dayStatus.setText(formattedDayDate);


                        for (int i = 0; i < 5; i++) {
                            int index = i * 8;

                            JSONObject dayWeather = list.getJSONObject(index);
                            JSONObject dayMain = dayWeather.getJSONObject("main");
                            JSONArray weatherArray = dayWeather.getJSONArray("weather");
                            JSONObject weather = weatherArray.getJSONObject(0);

                            String dayTemp = String.format("%s°C", dayMain.getString("temp"));
                            String dayDesc = weather.getString("main");
                            String dayDateTime = dayWeather.getString("dt_txt");
                            String formattedDay = formatDate(dayDateTime);


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


    private void fetchAirQuality(String cityName) {
        String geoUrl = BASE_URL + "weather?q=" + cityName + "&appid=" + API_KEY;

        JsonObjectRequest geoRequest = new JsonObjectRequest(Request.Method.GET, geoUrl, null,
                response -> {
                    try {
                        JSONObject coord = response.getJSONObject("coord");
                        double lat = coord.getDouble("lat");
                        double lon = coord.getDouble("lon");

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

                        String airQuality = getAirQualityDescription(aqi);
                        airQualityDetails.setText("Air Quality: " + airQuality);
                        airQualityImage.setImageResource(getAirQualityImage(aqi));
                    } catch (Exception e) {
                        airQualityDetails.setText("Error parsing air quality data.");
                        airQualityImage.setImageResource(R.drawable.unknownn);
                    }
                },
                error -> {
                    airQualityDetails.setText("Error fetching air quality data.");
                    airQualityImage.setImageResource(R.drawable.unknownn);
                });

        requestQueue.add(pollutionRequest);
    }

    private int getAirQualityImage(int aqi) {
        switch (aqi) {
            case 1: return R.drawable.good;
            case 2: return R.drawable.fair;
            case 3: return R.drawable.moderate;
            case 4: return R.drawable.poor;
            case 5: return R.drawable.very_poor;
            default: return R.drawable.unknownn;
        }
    }


    private String getAirQualityDescription(int aqi) {
        switch (aqi) {
            case 1: return "Good";
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Unknown";
        }
    }



    private String formatDate(String dateTime) {
        try {

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);

            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    private int getIcon(String weather) {

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
}

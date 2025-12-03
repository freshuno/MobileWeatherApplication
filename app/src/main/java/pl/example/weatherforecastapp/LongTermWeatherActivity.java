package pl.example.weatherforecastapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearSmoothScroller;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LongTermWeatherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private ArrayList<DailyWeather> weatherList;
    private TextView tvCity;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    // Twój klucz API
    private final String appid = BuildConfig.OPEN_WEATHER_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_term_weather);

        tvCity = findViewById(R.id.tvCityHeader);
        recyclerView = findViewById(R.id.recyclerViewForecast);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherList = new ArrayList<>();

        // Inicjalizacja adaptera z listenerem kliknięcia
        adapter = new ForecastAdapter(weatherList, new ForecastAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DailyWeather item) {
                showDetailsDialog(item);
            }
        });

        recyclerView.setAdapter(adapter);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lon = getIntent().getDoubleExtra("lon", 0);
        String city = getIntent().getStringExtra("city");

        tvCity.setText(city);

        get16DayForecast(lat, lon);
    }

    private void showDetailsDialog(DailyWeather weather) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_weather_details);

        // Ustawienie tła na przezroczyste (żeby zaokrąglone rogi CardView działały)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Znalezienie widoków w dialogu
        TextView date = dialog.findViewById(R.id.dialogDate);
        TextView desc = dialog.findViewById(R.id.dialogDescription);
        TextView temp = dialog.findViewById(R.id.dialogTemp);
        TextView pressure = dialog.findViewById(R.id.dialogPressure);
        TextView humidity = dialog.findViewById(R.id.dialogHumidity);
        TextView wind = dialog.findViewById(R.id.dialogWind);
        TextView clouds = dialog.findViewById(R.id.dialogClouds);
        ImageButton close = dialog.findViewById(R.id.btnCloseDialog);

        // Ustawienie danych
        date.setText(weather.date);
        desc.setText(weather.description);
        temp.setText(weather.tempMax + " / " + weather.tempMin.replace("/ ", ""));
        pressure.setText(weather.pressure);
        humidity.setText(weather.humidity);
        wind.setText(weather.windSpeed);
        clouds.setText(weather.clouds);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void get16DayForecast(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/forecast/daily?" +
                "lat=" + lat + "&lon=" + lon +
                "&cnt=16" +
                "&units=metric" +
                "&lang=pl" +
                "&appid=" + appid;

        Log.d("API_URL", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray list = jsonResponse.getJSONArray("list");

                            weatherList.clear();

                            for (int i = 0; i < list.length(); i++) {
                                JSONObject dayObj = list.getJSONObject(i);

                                long dt = dayObj.getLong("dt");
                                Date date = new Date(dt * 1000L);
                                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", new Locale("pl", "PL"));
                                String dateString = sdf.format(date);
                                dateString = dateString.substring(0, 1).toUpperCase() + dateString.substring(1);

                                JSONObject tempObj = dayObj.getJSONObject("temp");
                                double max = tempObj.getDouble("max");
                                double min = tempObj.getDouble("min");

                                JSONArray weatherArr = dayObj.getJSONArray("weather");
                                JSONObject weatherObj = weatherArr.getJSONObject(0);
                                String description = weatherObj.getString("description");
                                String main = weatherObj.getString("main");

                                // Pobieranie dodatkowych szczegółów
                                int pressure = dayObj.getInt("pressure");
                                int humidity = dayObj.getInt("humidity");
                                double speed = dayObj.getDouble("speed");
                                int clouds = dayObj.getInt("clouds");

                                weatherList.add(new DailyWeather(
                                        dateString,
                                        translateWeatherDescription(description),
                                        String.format(Locale.getDefault(), "%.0f°", max),
                                        String.format(Locale.getDefault(), "/ %.0f°", min),
                                        getIconResource(description, main),
                                        pressure + " hPa",
                                        humidity + "%",
                                        String.format(Locale.getDefault(), "%.1f m/s", speed),
                                        clouds + "%"
                                ));
                            }

                            adapter.notifyDataSetChanged();

                            // --- NOWY KOD DO PRZEWIJANIA ---
                            int dayToScroll = getIntent().getIntExtra("scroll_to_day", 0);

                            if (dayToScroll > 0 && dayToScroll < weatherList.size()) {
                                // Przewijamy listę do konkretnej pozycji (dnia)
                                // Używamy post(), żeby upewnić się, że lista się załadowała
                                recyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //recyclerView.scrollToPosition(dayToScroll);
                                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                                        if (layoutManager != null) {
                                            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(LongTermWeatherActivity.this) {
                                                @Override
                                                public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                                                    // Ta matematyka wylicza środek ekranu i środek elementu, a potem zwraca różnicę
                                                    return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
                                                }
                                            };
                                            smoothScroller.setTargetPosition(dayToScroll);
                                            layoutManager.startSmoothScroll(smoothScroller);
                                        }

                                        // Opcjonalnie: podświetlenie lub komunikat
                                        Toast.makeText(LongTermWeatherActivity.this,
                                                "Prognoza na dzień: " + weatherList.get(dayToScroll).date,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LongTermWeatherActivity.this, "Błąd przetwarzania danych", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            Log.e("Volley", "Error code: " + error.networkResponse.statusCode);
                        }
                        Toast.makeText(LongTermWeatherActivity.this, "Błąd pobierania danych (16 dni)", Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String translateWeatherDescription(String description) {
        String polishDescription;
        switch (description) {
            case "overcast clouds": polishDescription = "Zachmurzenie całkowite"; break;
            case "light rain": polishDescription = "Lekki deszcz"; break;
            case "scattered clouds": polishDescription = "Rozproszone chmury"; break;
            case "light snow": polishDescription = "Lekki śnieg"; break;
            case "snow": polishDescription = "Śnieg"; break;
            case "sunrise": polishDescription = "Wschód słońca"; break;
            case "thunderstorm": polishDescription = "Burza"; break;
            case "few clouds": polishDescription = "Pochmurno"; break;
            case "broken clouds": polishDescription = "Rozbite chmury"; break;
            case "rain": polishDescription = "Deszcz"; break;
            case "clear sky": polishDescription = "Czyste Niebo"; break;
            case "light intensity shower rain": polishDescription = "Lekki deszcz"; break;
            case "moderate rain": polishDescription = "Lekki deszcz"; break;
            case "haze": polishDescription = "Mgła"; break;
            case "mist": polishDescription = "Mgła"; break;
            case "heavy intensity rain": polishDescription = "Ulewa"; break;
            case "very heavy rain": polishDescription = "Ulewa"; break;
            case "rain and snow": polishDescription = "Deszcz ze śniegiem"; break;
            case "sky is clear": polishDescription = "Bezchmurnie"; break;
            default: polishDescription = description;
        }
        return polishDescription;
    }

    private int getIconResource(String description, String main) {
        switch (description) {
            case "overcast clouds": return R.drawable.clouds;
            case "light rain": return R.drawable.smallrain;
            case "moderate rain": return R.drawable.smallrain;
            case "rain": return R.drawable.smallrain;
            case "scattered clouds": return R.drawable.shatteredclouds;
            case "few clouds": return R.drawable.shatteredclouds;
            case "haze": return R.drawable.clouds;
            case "mist": return R.drawable.clouds;
            case "broken clouds": return R.drawable.shatteredclouds;
            case "light snow": return R.drawable.snow;
            case "snow": return R.drawable.snow;
            case "clear sky": return R.drawable.sun;
            case "sky is clear": return R.drawable.sun;
            case "sunrise": return R.drawable.sun;
            case "light intensity shower rain": return R.drawable.smallrain;
            case "very heavy rain": return R.drawable.heavyrain;
            case "heavy intensity rain": return R.drawable.heavyrain;
            case "thunderstorm": return R.drawable.thunderstorm;
            case "rain and snow": return R.drawable.snow;
            default:
                if (main.equals("Rain")) return R.drawable.smallrain;
                if (main.equals("Snow")) return R.drawable.snow;
                if (main.equals("Clouds")) return R.drawable.clouds;
                if (main.equals("Clear")) return R.drawable.sun;
                return R.drawable.unknown_weather;
        }
    }

    // Klasa modelu danych - zaktualizowana o nowe pola
    public static class DailyWeather {
        String date;
        String description;
        String tempMax;
        String tempMin;
        int iconResId;

        // Nowe pola
        String pressure;
        String humidity;
        String windSpeed;
        String clouds;

        public DailyWeather(String date, String description, String tempMax, String tempMin, int iconResId,
                            String pressure, String humidity, String windSpeed, String clouds) {
            this.date = date;
            this.description = description;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.iconResId = iconResId;
            this.pressure = pressure;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.clouds = clouds;
        }
    }
}
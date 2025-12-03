package pl.example.weatherforecastapp;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.content.Context;

import android.app.ProgressDialog;

import android.animation.ObjectAnimator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import java.util.ArrayList;
import com.google.mlkit.nl.entityextraction.Entity;
import com.google.mlkit.nl.entityextraction.EntityAnnotation;
import com.google.mlkit.nl.entityextraction.EntityExtractor;
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions;
import com.google.mlkit.nl.entityextraction.EntityExtraction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int REQUEST_SPEECH = 100;
    private TextToSpeech tts;
    EditText textInputLayout;
    TextView textView,longTermWeather,weatherInfoCity,weatherInfoTemp, weatherInfoDescr, futureTempOne, futureTempTwo, futureTempThree, futureTempFour,
            futureHourOne, futureHourTwo, futureHourThree, futureHourFour,weatherInfoPressure,weatherInfoFeelsLike,weatherInfoHumidity,weatherInfoWindSpeed;
    ImageView futureImgOne, futureImgTwo, futureImgThree, futureImgFour, button_location;
    Button button_searchView, btnLongTermForecast;
    ConstraintLayout weatherInfo,weatherInfoDetails;
    LocationManager locationManager;
    private final String url = "https://api.openweathermap.org/geo/1.0/direct?";

    // TUTAJ ZAKTUALIZOWANO KLUCZ
    private final String appid = BuildConfig.OPEN_WEATHER_API_KEY;

    double lat, lon;
    DecimalFormat df = new DecimalFormat("#.##");
    private Double currentLat = null;
    private Double currentLon = null;
    private int targetDayOffset = 0; // 0 = dzisiaj, 1 = jutro, 5 = za 5 dni...

    // mała klasa na dane miasta z geo API
    class CityOption {
        String name;
        String country;
        String state;   // np. województwo / region
        double lat;
        double lon;

        CityOption(String name, String country, String state, double lat, double lon) {
            this.name = name;
            this.country = country;
            this.state = state;
            this.lat = lat;
            this.lon = lon;
        }

        // podstawowa nazwa: Miasto, PL (Małopolskie)
        String getDisplayName() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (country != null && !country.isEmpty()) {
                sb.append(", ").append(country);
            }
            if (state != null && !state.isEmpty()) {
                sb.append(" (").append(state).append(")");
            }
            return sb.toString();
        }

        // PROSTA nazwa – do nagłówka na ekranie głównym
        String getHeaderName() {
            return name;
        }

        // nazwa + opcjonalny dystans: "Krzyszkowice, PL (Małopolskie) – 12 km od Ciebie"
        String getDisplayNameWithDistance(Double refLat, Double refLon) {
            String base = getDisplayName();
            if (refLat == null || refLon == null) {
                return base;
            }
            double dist = distanceKm(refLat, refLon, lat, lon);
            return base + String.format(Locale.getDefault(), " – %.0f km od Ciebie", dist);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView voiceButton = findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognition();
            }
        });

        // TTS – bez lambdy
        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("pl", "PL"));
                }
            }
        });

        textInputLayout = findViewById(R.id.editText1);
        textView = findViewById(R.id.textView);
        weatherInfoCity = findViewById(R.id.weatherInfoCity);
        weatherInfoTemp = findViewById(R.id.weatherInfoTemp);
        weatherInfoDescr = findViewById(R.id.weatherInfoDescr);
        weatherInfo = findViewById(R.id.weatherInfo);
        weatherInfoDetails = findViewById(R.id.weatherInfoDetails);
        longTermWeather = findViewById(R.id.longTermForecast);
        futureImgOne = findViewById(R.id.futureImgOne);
        futureImgTwo = findViewById(R.id.futureImgTwo);
        futureImgThree = findViewById(R.id.futureImgThree);
        futureImgFour = findViewById(R.id.futureImgFour);
        futureTempOne = findViewById(R.id.futureTempOne);
        futureTempTwo = findViewById(R.id.futureTempTwo);
        futureTempThree = findViewById(R.id.futureTempThree);
        futureTempFour = findViewById(R.id.futureTempFour);
        futureHourOne = findViewById(R.id.futureHourOne);
        futureHourTwo = findViewById(R.id.futureHourTwo);
        futureHourThree = findViewById(R.id.futureHourThree);
        futureHourFour = findViewById(R.id.futureHourFour);
        button_location = findViewById(R.id.currentLocation);
        button_searchView = findViewById(R.id.searchView2);
        weatherInfoPressure = findViewById(R.id.weatherInfoPressure);
        weatherInfoFeelsLike = findViewById(R.id.weatherInfoFeelsLike);
        weatherInfoHumidity = findViewById(R.id.weatherInfoHumidity);
        weatherInfoWindSpeed = findViewById(R.id.weatherInfoWindSpeed);

        // NOWY PRZYCISK
        btnLongTermForecast = findViewById(R.id.btnLongTermForecast);
        btnLongTermForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LongTermWeatherActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("city", weatherInfoCity.getText().toString());
                startActivity(intent);
            }
        });

        //Asking for location permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(button_location, "alpha", 1f, 0f, 1f);
                animator.setDuration(500);

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        getLocation();
                    }
                });
                animator.start();
            }
        });

        button_searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(button_searchView, "alpha", 1f, 0f, 1f);
                animator.setDuration(500);

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        getWeather(v);
                    }
                });
                animator.start();
            }
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Słucham…");

        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "Brak wsparcia dla rozpoznawania mowy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String voiceQuery = result.get(0).toLowerCase();

            handleVoiceCommandWithNER(voiceQuery);
        }
    }

    // Wklej to w MainActivity.java

    private void handleVoiceCommandWithNER(String query) {
        String capitalizedQuery = capitalizeText(query);

        // 1. Najpierw wyliczamy dzień z CAŁEGO zapytania (to naprawi problem "za 20 dni")
        int days = parseDaysFromText(query);

        if (days > 16) {
            speak("Przykro mi, prognoza sięga tylko szesnastu dni.");
            return; // <--- To przerywa działanie, nie otworzy się lista miast
        }

        // Zapisujemy to globalnie, żeby przetrwało wybieranie miasta z listy
        targetDayOffset = days;

        EntityExtractorOptions options =
                new EntityExtractorOptions.Builder(EntityExtractorOptions.POLISH).build();
        EntityExtractor extractor = EntityExtraction.getClient(options);

        extractor.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    extractor.annotate(capitalizedQuery)
                            .addOnSuccessListener(annotations -> {
                                String foundCity = null;

                                for (EntityAnnotation annotation : annotations) {
                                    for (Entity entity : annotation.getEntities()) {
                                        if (entity.getType() == Entity.TYPE_ADDRESS) {
                                            foundCity = annotation.getAnnotatedText();
                                            break;
                                        }
                                    }
                                }

                                if (foundCity != null) {
                                    String normalizedCity = getNormalizedCityName(foundCity);
                                    textInputLayout.setText(normalizedCity);

                                    // Komunikat dla użytkownika
                                    if (targetDayOffset > 0) {
                                        speak("Szukam prognozy na dzień za " + targetDayOffset + " dni dla miasta " + normalizedCity);
                                    } else {
                                        speak("Sprawdzam pogodę dla: " + normalizedCity);
                                    }

                                    getWeather(null);
                                } else {
                                    runManualCleanup(query);
                                }
                            })
                            .addOnFailureListener(e -> runManualCleanup(query));
                })
                .addOnFailureListener(e -> runManualCleanup(query));
    }

    // Metoda pomocnicza do powiększania liter (dla modelu NER)
    private String capitalizeText(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Metoda pomocnicza do ręcznego czyszczenia (Fallback)
    private void runManualCleanup(String query) {
        String cleanQuery = query.toLowerCase().trim();
        // Lista słów do wycięcia
        String[] trashWords = {
                "jaka", "będzie", "pogoda", "w", "we", "dla", "jutro",
                "pojutrze", "teraz", "proszę", "sprawdź", "miasto", "mieście"
        };

        for (String word : trashWords) {
            // Usuwamy całe słowa
            cleanQuery = cleanQuery.replaceAll("\\b" + word + "\\b", "");
        }

        // Usuwamy zbędne spacje i kropki (czasami voice to text dodaje kropkę na końcu)
        String rawCity = cleanQuery.replaceAll("[^a-zA-ZąęćłńóśźżĄĘĆŁŃÓŚŹŻ ]", "").trim().replaceAll("\\s+", " ");

        if (!rawCity.isEmpty()) {
            Log.d("NER_DEBUG", "Surowa nazwa z text-to-speech: " + rawCity);

            // --- TU JEST MAGIA: NAPRAWIAMY NAZWĘ ---
            // Wywołujemy naszą nową funkcję, która zamieni "krakowie" na "Kraków"
            String normalizedCity = getNormalizedCityName(rawCity);
            Log.d("NER_DEBUG", "Nazwa po normalizacji Geocoderem: " + normalizedCity);

            // Ustawiamy w polu tekstowym ładną nazwę "Kraków"
            textInputLayout.setText(normalizedCity);

            // Pobieramy pogodę
            getWeather(null);

            // Aplikacja mówi teraz poprawnie
            speak("Sprawdzam pogodę dla: " + normalizedCity);
        } else {
            speak("Nie zrozumiałem nazwy miasta.");
        }
    }

    private void handleVoiceCommand(String query) {
        if (query.contains("pogoda w")) {
            String miasto = query.replace("pogoda w", "").trim();
            EditText edt = findViewById(R.id.editText1);
            edt.setText(miasto);
            getWeather(null);
            speak("Sprawdzam pogodę w " + miasto);
            return;
        }
        if (query.contains("jutro")) {
            speak("Prognoza na jutro pojawi się w przyszłej aktualizacji.");
            return;
        }
        if (query.contains("wycieczk") || query.contains("rower")) {
            speak("Sprawdzam pogodę na Twoją wycieczkę.");
            getWeather(null);
            return;
        }
        EditText edt = findViewById(R.id.editText1);
        edt.setText(query);
        getWeather(null);
        speak("Szukam informacji pogodowych dla " + query);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        }
    }

    public void getWeather(View view) {
        String tempUrl = "";
        String city = textInputLayout.getText().toString().trim();
        if (city.equals("")) {
            Toast.makeText(getApplicationContext(), "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show();
            weatherInfo.setVisibility(View.INVISIBLE);
            weatherInfoDetails.setVisibility(View.INVISIBLE);
            btnLongTermForecast.setVisibility(View.INVISIBLE);
        } else {
            tempUrl = url + "q=" + city + "&limit=5&appid=" + appid;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("response", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() == 0) {
                            Toast.makeText(getApplicationContext(), "Miasto nie zostało znalezione", Toast.LENGTH_SHORT).show();
                            weatherInfo.setVisibility(View.INVISIBLE);
                            weatherInfoDetails.setVisibility(View.INVISIBLE);
                            btnLongTermForecast.setVisibility(View.INVISIBLE);
                            return;
                        }

                        ArrayList<CityOption> options = new ArrayList<>();
                        final double MERGE_DISTANCE_KM = 10.0;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            double lat = obj.getDouble("lat");
                            double lon = obj.getDouble("lon");
                            String name = obj.getString("name");
                            String country = obj.optString("country", "");
                            String state = obj.optString("state", "");

                            boolean tooClose = false;
                            for (CityOption existing : options) {
                                double dist = distanceKm(existing.lat, existing.lon, lat, lon);
                                if (dist < MERGE_DISTANCE_KM) {
                                    tooClose = true;
                                    break;
                                }
                            }

                            if (!tooClose) {
                                options.add(new CityOption(name, country, state, lat, lon));
                            }
                        }

                        if (options.size() == 1) {
                            CityOption only = options.get(0);
                            weatherInfoCity.setText(only.getHeaderName());
                            getWeatherDetails(only.lat, only.lon);
                        } else {
                            showCityChoiceDialog(options);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Problem z parsowaniem odpowiedzi", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Toast.makeText(getApplicationContext(), "Błąd podczas pobierania danych", Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
    }

    private void showCityChoiceDialog(ArrayList<CityOption> options) {
        String[] items = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            items[i] = options.get(i).getDisplayNameWithDistance(currentLat, currentLon);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Wybierz miejscowość");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CityOption chosen = options.get(which);
                weatherInfoCity.setText(chosen.getHeaderName());
                getWeatherDetails(chosen.lat, chosen.lon);
            }
        });
        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void getWeatherDetails(double lat, double lon) {
        // Aktualizacja zmiennych klasowych
        this.lat = lat;
        this.lon = lon;

        String tempUrlLoc = "https://api.openweathermap.org/data/2.5/weather?";
        tempUrlLoc += "lat=" + lat + "&lon=" + lon + "&appid=" + appid;
        System.out.println(tempUrlLoc);
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, tempUrlLoc, new Response.Listener<String>() {
            @Override
            public void onResponse(String response2) {
                Log.d("response", response2);
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response2);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    String polishDescription = translateWeatherDescription(description);
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                    float pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    float speedWind = jsonObjectWind.getInt("speed");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    int cloudy = jsonObjectClouds.getInt("all");
                    String city = jsonResponse.getString("name");
                    output = "description=" + description + "\n"
                            + "temperature=" + temp + "\n "
                            + "feels like=" + feelsLike + "\n "
                            + "Pressure=" + pressure + "\n"
                            + "Humidity=" + humidity + "\n"
                            + "Speed wind=" + speedWind + "\n"
                            + "Cloudy=" + cloudy + "\n";
                    textView.setText(output);
                    weatherInfo.setVisibility(View.VISIBLE);
                    weatherInfoDetails.setVisibility(View.VISIBLE);
                    // Pokaż przycisk długoterminowy
                    btnLongTermForecast.setVisibility(View.VISIBLE);

                    // JEŚLI użytkownik pytał o przyszłość (np. za 5 dni)
                    if (targetDayOffset > 0) {
                        Intent intent = new Intent(MainActivity.this, LongTermWeatherActivity.class);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lon", lon);
                        intent.putExtra("city", weatherInfoCity.getText().toString());
                        // PRZEKAZUJEMY DZIEŃ DO PRZEWINIĘCIA
                        intent.putExtra("scroll_to_day", targetDayOffset);

                        startActivity(intent);

                        // Resetujemy, żeby kolejne ręczne kliknięcia działały normalnie
                        targetDayOffset = 0;
                    }

                    weatherInfoTemp.setText(String.format("%.2f", temp) + "\u00B0");
                    weatherInfoDescr.setText(polishDescription);
                    weatherInfoPressure.setText(String.valueOf(pressure)+" hPa");
                    weatherInfoFeelsLike.setText(String.format("%.2f", feelsLike) + "\u00B0");
                    weatherInfoHumidity.setText(String.valueOf(humidity)+" %");
                    weatherInfoWindSpeed.setText(String.valueOf(speedWind)+" m/s");
                    hideKeyboard();
                    textInputLayout.setText("");


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), volleyError.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });

        // LONG-TERM-WEATHER-FORECAST (3-hour / 5 days)
        String longTermUrlLoc = "https://api.openweathermap.org/data/2.5/forecast?";
        longTermUrlLoc += "lat=" + lat + "&lon=" + lon + "&appid=" + appid;
        System.out.println(longTermUrlLoc);
        StringRequest stringRequest3 = new StringRequest(Request.Method.GET, longTermUrlLoc, new Response.Listener<String>() {
            @Override
            public void onResponse(String response3) {
                Log.d("response", response3);
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response3);
                    JSONArray jsonArray = jsonResponse.getJSONArray("list");
                    for(int i=1;i<=4;i++) {
                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(i);
                        JSONArray jsonArray2 = jsonObjectWeather.getJSONArray("weather");
                        JSONObject jsonObjectWeather2 = jsonArray2.getJSONObject(0);
                        String description = jsonObjectWeather2.getString("description");
                        JSONObject jsonObjectMain = jsonObjectWeather.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        String dataTime = jsonObjectWeather.getString("dt_txt");
                        String hour = dataTime.substring(dataTime.indexOf(' ') + 1, dataTime.lastIndexOf(':'));
                        int imageResource;
                        switch (description) {
                            case "overcast clouds": imageResource = R.drawable.clouds; break;
                            case "light rain": imageResource = R.drawable.smallrain; break;
                            case "moderate rain": imageResource = R.drawable.smallrain; break;
                            case "rain": imageResource = R.drawable.smallrain; break;
                            case "scattered clouds": imageResource = R.drawable.shatteredclouds; break;
                            case "few clouds": imageResource = R.drawable.shatteredclouds; break;
                            case "haze": imageResource = R.drawable.clouds; break;
                            case "mist": imageResource = R.drawable.clouds; break;
                            case "broken clouds": imageResource = R.drawable.shatteredclouds; break;
                            case "light snow": imageResource = R.drawable.snow; break;
                            case "snow": imageResource = R.drawable.snow; break;
                            case "clear sky": imageResource = R.drawable.sun; break;
                            case "sunrise": imageResource = R.drawable.sun; break;
                            case "light intensity shower rain": imageResource = R.drawable.smallrain; break;
                            case "very heavy rain": imageResource = R.drawable.heavyrain; break;
                            case "heavy intensity rain": imageResource = R.drawable.heavyrain; break;
                            case "thunderstorm": imageResource = R.drawable.thunderstorm; break;
                            default: imageResource = R.drawable.unknown_weather; break;
                        }
                        switch (i) {
                            case 1:
                                futureTempOne.setText(String.format("%.2f", temp) + "\u00B0");
                                futureHourOne.setText(hour);
                                futureImgOne.setImageResource(imageResource);
                                break;
                            case 2:
                                futureTempTwo.setText(String.format("%.2f", temp) + "\u00B0");
                                futureHourTwo.setText(hour);
                                futureImgTwo.setImageResource(imageResource);
                                break;
                            case 3:
                                futureTempThree.setText(String.format("%.2f", temp) + "\u00B0");
                                futureHourThree.setText(hour);
                                futureImgThree.setImageResource(imageResource);
                                break;
                            case 4:
                                futureTempFour.setText(String.format("%.2f", temp) + "\u00B0");
                                futureHourFour.setText(hour);
                                futureImgFour.setImageResource(imageResource);
                                break;
                        }
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), volleyError.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue2 = Volley.newRequestQueue(getApplicationContext());
        requestQueue2.add(stringRequest2);
        RequestQueue requestQueue3 = Volley.newRequestQueue(getApplicationContext());
        requestQueue3.add(stringRequest3);
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
            default: polishDescription = "brak tłumaczenia";
        }
        return polishDescription;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);
            lat = addresses.get(0).getLatitude();
            lon = addresses.get(0).getLongitude();
            longTermWeather.setText(address);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            ProgressDialog progressDialog = ProgressDialog.show(this, "Ładowanie danych", "Proszę czekać...", true);

            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Address addr = addresses.get(0);

                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();

                        getWeatherDetails(lat, lon);

                        currentLat = lat;
                        currentLon = lon;

                        String cityName = addr.getLocality();
                        if (cityName == null || cityName.isEmpty()) {
                            cityName = addr.getSubAdminArea();
                        }
                        if (cityName == null || cityName.isEmpty()) {
                            cityName = addr.getAddressLine(0);
                        }

                        weatherInfoCity.setText(cityName);
                        progressDialog.dismiss();

                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metoda do zamiany "krakowie" -> "Kraków" przy użyciu Geocodera
    private String getNormalizedCityName(String inputName) {
        try {
            Geocoder geocoder = new Geocoder(this, new Locale("pl", "PL"));
            // Pytamy o 1 najlepszy wynik dla danej nazwy
            List<Address> addresses = geocoder.getFromLocationName(inputName, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Pobieramy oficjalną nazwę miasta (Locality)
                String city = address.getLocality();
                // Czasami miasto jest w subAdminArea (np. dla małych wsi), więc zabezpieczenie:
                if (city == null) city = address.getSubAdminArea();
                if (city == null) city = address.getFeatureName();

                if (city != null) return city;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Jak się nie uda naprawić, zwracamy oryginał (np. "krakowie")
        return inputName;
    }

    private int parseDaysFromText(String text) {
        if (text == null) return 0;
        String t = text.toLowerCase().trim();

        if (t.contains("jutro")) return 1;
        if (t.contains("pojutrze")) return 2;
        if (t.contains("tydzień")) return 7;

        // Szukamy konkretnych liczb, np. "za 5 dni", "za 20 dni"
        Pattern p = Pattern.compile("za (\\d+) dni");
        Matcher m = p.matcher(t);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // Fallback: szukamy samej liczby jeśli powyższe nie zadziała
        p = Pattern.compile("(\\d+)");
        m = p.matcher(t);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) { return 0; }
        }

        return 0;
    }
}
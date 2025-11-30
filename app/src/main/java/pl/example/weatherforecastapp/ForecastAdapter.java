package pl.example.weatherforecastapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private ArrayList<LongTermWeatherActivity.DailyWeather> weatherList;
    private OnItemClickListener listener;

    // Interfejs do obsługi kliknięć
    public interface OnItemClickListener {
        void onItemClick(LongTermWeatherActivity.DailyWeather item);
    }

    public ForecastAdapter(ArrayList<LongTermWeatherActivity.DailyWeather> weatherList, OnItemClickListener listener) {
        this.weatherList = weatherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LongTermWeatherActivity.DailyWeather weather = weatherList.get(position);
        holder.tvDate.setText(weather.date);
        holder.tvDescription.setText(weather.description);
        holder.tvTempMax.setText(weather.tempMax);
        holder.tvTempMin.setText(weather.tempMin);
        holder.imgIcon.setImageResource(weather.iconResId);

        // Obsługa kliknięcia w cały wiersz
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(weather);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDescription, tvTempMax, tvTempMin;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTempMax = itemView.findViewById(R.id.tvTempMax);
            tvTempMin = itemView.findViewById(R.id.tvTempMin);
            imgIcon = itemView.findViewById(R.id.imgWeatherIcon);
        }
    }
}
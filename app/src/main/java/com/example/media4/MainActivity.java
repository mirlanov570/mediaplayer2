package com.example.media4;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playButton, prevButton, nextButton;
    private SeekBar songSeekBar, volumeSeekBar;
    private TextView songTitle, currentTimeText, totalTimeText;
    private ImageView albumCover;
    private Handler handler = new Handler();

    // Массив песен и соответствующих обложек
    private int[] songResources = {R.raw.sample_audio, R.raw.tusinbedin};
    private int[] albumCovers = {R.drawable.cover1, R.drawable.cover2};
    private String[] songNames = {"Umyttynba", "Tusinbedin"}; // Названия песен
    private int currentSongIndex = 0;

    // Обновление позиции SeekBar и времени
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                songSeekBar.setProgress(currentPosition);
                currentTimeText.setText(formatTime(currentPosition));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация компонентов
        playButton = findViewById(R.id.playButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        songSeekBar = findViewById(R.id.songSeekBar);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        songTitle = findViewById(R.id.songTitle);
        albumCover = findViewById(R.id.albumCover);
        currentTimeText = findViewById(R.id.currenttime);
        totalTimeText = findViewById(R.id.totaltime);
        SearchView searchView = findViewById(R.id.searchView); // Инициализация SearchView

        // Установка начальной песни
        loadSong(currentSongIndex);

        // Play/Pause функционал
        playButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                handler.postDelayed(updateSeekBar, 1000);  // Начинаем обновление SeekBar и времени
            }
        });

        // Кнопка "Предыдущая"
        prevButton.setOnClickListener(v -> {
            currentSongIndex = (currentSongIndex - 1 + songResources.length) % songResources.length; // Переход к предыдущей песне
            loadSong(currentSongIndex); // Загружаем предыдущую песню
        });

        // Кнопка "Следующая"
        nextButton.setOnClickListener(v -> {
            currentSongIndex = (currentSongIndex + 1) % songResources.length; // Переход к следующей песне
            loadSong(currentSongIndex); // Загружаем следующую песню
        });

        // SeekBar для песни
        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    currentTimeText.setText(formatTime(progress));  // Обновляем текущее время
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar); // Останавливаем обновление при взаимодействии
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateSeekBar, 1000); // Возобновляем обновление после взаимодействия
            }
        });

        // SeekBar для громкости
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                mediaPlayer.setVolume(volume, volume); // Установка громкости
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Поиск песен
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText); // Фильтруем песни по введенному тексту
                return true;
            }
        });
    }

    // Метод для загрузки песни
    private void loadSong(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Освобождаем предыдущую песню
        }

        mediaPlayer = MediaPlayer.create(this, songResources[index]);
        songSeekBar.setMax(mediaPlayer.getDuration());
        totalTimeText.setText(formatTime(mediaPlayer.getDuration()));
        songTitle.setText(songNames[index]); // Устанавливаем название текущей песни
        albumCover.setImageResource(albumCovers[index]); // Устанавливаем обложку альбома

        // Установка слушателя на окончание песни
        mediaPlayer.setOnCompletionListener(mp -> {
            // Переход к следующей песне сразу после завершения
            currentSongIndex = (currentSongIndex + 1) % songResources.length; // Переход к следующей песне
            loadSong(currentSongIndex); // Загружаем следующую песню
            mediaPlayer.start(); // Запускаем воспроизведение следующей песни
        });

        // Запуск текущей песни
        mediaPlayer.start();
        playButton.setImageResource(android.R.drawable.ic_media_pause); // Обновляем кнопку воспроизведения на паузу
        handler.postDelayed(updateSeekBar, 1000); // Начинаем обновление SeekBar
    }

    // Метод для форматирования времени
    private String formatTime(int milliseconds) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    // Метод для фильтрации песен
    private void filterSongs(String query) {
        // Логика фильтрации по названиям песен
        for (int i = 0; i < songNames.length; i++) {
            if (songNames[i].toLowerCase().contains(query.toLowerCase())) {
                currentSongIndex = i; // Устанавливаем индекс найденной песни
                loadSong(currentSongIndex); // Загружаем найденную песню
                break; // Выход из цикла после нахождения первой подходящей песни
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Освобождаем ресурсы при уничтожении
        }
    }
}

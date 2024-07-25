package com.example.apocalypseescape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPlayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        editTextPlayerName = findViewById(R.id.editTextPlayerName);

        SharedPreferences sharedPreferences = getSharedPreferences("TempoMaximo", MODE_PRIVATE);
        int tempo = sharedPreferences.getInt("Tempo", 0);
        String jogador = sharedPreferences.getString("Jogador", "N/A");

        TextView highScoreView = findViewById(R.id.tvHighScore);
        highScoreView.setText("Melhor Tempo: " + jogador + " - " + formatTime(tempo));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void play(View v){
        if(editTextPlayerName.getVisibility() == View.GONE){
            editTextPlayerName.setVisibility(View.VISIBLE);
        } else {
            String playerName = editTextPlayerName.getText().toString();
            if(playerName.isEmpty()){
                Toast.makeText(this, "Por favor, insira seu nome", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, Game.class);
                intent.putExtra("PLAYER_NAME", playerName);
                startActivity(intent);
                finish();
            }
        }
    }
}

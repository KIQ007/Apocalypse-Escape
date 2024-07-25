    package com.example.apocalypseescape;

    import android.animation.ValueAnimator;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.ActivityInfo;
    import android.graphics.Point;
    import android.graphics.Rect;
    import android.hardware.Sensor;
    import android.hardware.SensorEvent;
    import android.hardware.SensorEventListener;
    import android.hardware.SensorManager;
    import android.media.MediaPlayer;
    import android.os.Bundle;
    import android.os.Handler;
    import android.view.Display;
    import android.view.View;
    import android.view.animation.LinearInterpolator;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.constraintlayout.widget.ConstraintLayout;

    import java.util.Locale;

    public class Game extends AppCompatActivity {

        MediaPlayer playerTiro;
        MediaPlayer playerMorte;
        MediaPlayer playerFundo;
        MediaPlayer playerGrito;

        SensorManager sensorManager;
        Sensor sensor;
        SensorEventListener sensorEventListener;


        ImageView jogador;
        ConstraintLayout constraintLayout;


        Handler handler = new Handler();
        Handler cronometroHandler = new Handler();

        int duracaoQuedaObjeto = 4000;
        int duracaoInicialQuedaObjeto = 3000;
        int intervaloCriacao = 2000;
        int incrementoZumbis = 1;
        int numeroZumbis = 1;
        int segundosPassados = 0;

        private int balas = 3;
        private final int MAX_BALAS = 3;
        private TextView tvMunicao;
        TextView tvCronometro;

        private int[] framesPassos = {R.drawable.passo1, R.drawable.passo2, R.drawable.passo3, R.drawable.passo4,
                R.drawable.passo5, R.drawable.passo6, R.drawable.passo7, R.drawable.passo8};
        private int[] framesBala = {R.drawable.bala1, R.drawable.bala2, R.drawable.bala3, R.drawable.bala4};

        private int[] framesInimigo = {R.drawable.passo1esqueleto, R.drawable.passo2esqueleto, R.drawable.passo3esqueleto,
                R.drawable.passo4esqueleto};

        private int[] framesCorpoCaindo = {R.drawable.morte1, R.drawable.morte2, R.drawable.morte3, R.drawable.morte4};

        private int indiceFrameAtual = 0;
        private int indiceFrameBala = 0;
        private int indiceFrameInimigo = 0;



        private Handler animacaoHandler = new Handler();
        private Handler animacaoBalaHandler = new Handler();
        private Handler animacaoInimigoHandler = new Handler();

        private Runnable animacaoRunnable = new Runnable() {
            @Override
            public void run() {
                indiceFrameAtual = (indiceFrameAtual + 1) % framesPassos.length;
                jogador.setImageResource(framesPassos[indiceFrameAtual]);
                animacaoHandler.postDelayed(this, 100);
            }
        };
        private Runnable animacaoBalaRunnable = new Runnable() {
            @Override
            public void run() {
                indiceFrameBala = (indiceFrameBala + 1) % framesBala.length;
                for (int i = 0; i < constraintLayout.getChildCount(); i++) {
                    View child = constraintLayout.getChildAt(i);
                    if (child instanceof ImageView && "bala".equals(child.getTag())) {
                        ((ImageView) child).setImageResource(framesBala[indiceFrameBala]);
                    }
                }
                animacaoBalaHandler.postDelayed(this, 100);
            }
        };
        private Runnable animacaoInimigoRunnable = new Runnable() {
            @Override
            public void run() {
                indiceFrameInimigo = (indiceFrameInimigo + 1) % framesInimigo.length;
                for (int i = 0; i < constraintLayout.getChildCount(); i++) {
                    View child = constraintLayout.getChildAt(i);
                    if (child instanceof ImageView && "zumbi".equals(child.getTag())) {
                        ((ImageView) child).setImageResource(framesInimigo[indiceFrameInimigo]);
                    }
                }
                animacaoInimigoHandler.postDelayed(this, 100);
            }
        };


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < numeroZumbis; i++) {
                    criarObjetoCaindo(duracaoInicialQuedaObjeto);
                }
                numeroZumbis += incrementoZumbis;
                handler.postDelayed(this, intervaloCriacao);
            }
        };

        private Runnable cronometroRunnable = new Runnable() {
            @Override
            public void run() {
                segundosPassados++;
                int minutos = segundosPassados / 60;
                int segundos = segundosPassados % 60;
                String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
                tvCronometro.setText(tempoFormatado);
                cronometroHandler.postDelayed(this, 1000);
            }
        };


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            playerTiro = MediaPlayer.create(this, R.raw.somfogo);
            playerMorte = MediaPlayer.create(this, R.raw.morteesqueleto);
            playerFundo = MediaPlayer.create(this, R.raw.fundo);
            playerGrito = MediaPlayer.create(this, R.raw.gritomorte);

            playerFundo.setLooping(true);
            playerFundo.start();

            // Inicialização dos componentes de interface
            tvMunicao = findViewById(R.id.tvMunicao);
            atualizarMunicao(); // Método para atualizar a munição na tela

            tvCronometro = findViewById(R.id.tvCronometro);
            cronometroHandler.postDelayed(cronometroRunnable, 1000); // Inicia o cronômetro

            constraintLayout = findViewById(R.id.constraintLayout);
            jogador = findViewById(R.id.player);

            // Configuração do sensor de movimento
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            // Verifica se o sensor está disponível
            if (sensor == null) {
                finish(); // Fecha a atividade se o sensor não estiver disponível
            }

            // Implementação do SensorEventListener
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    // Lógica para atualizar a posição do jogador com base nos dados do sensor
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];

                    float posXAtual = jogador.getX();
                    float posYAtual = jogador.getY();
                    float novaPosX = posXAtual - x * 3; // Ajuste na sensibilidade
                    float novaPosY = posYAtual + y * 3; // Ajuste na sensibilidade

                    // Obtém as dimensões da tela
                    Display display = getWindowManager().getDefaultDisplay();
                    Point tamanho = new Point();
                    display.getSize(tamanho);
                    int larguraTela = tamanho.x;
                    int alturaTela = tamanho.y;

                    // Obtém as dimensões da imagem do jogador
                    int larguraImagem = jogador.getWidth();
                    int alturaImagem = jogador.getHeight();

                    int MetadeInferior = alturaTela / 2;

                    // Ajuste das posições para garantir que a imagem não saia da tela
                    novaPosX = Math.max(0, Math.min(novaPosX, larguraTela - larguraImagem));
                    novaPosY = Math.max(MetadeInferior, Math.min(novaPosY, alturaTela - alturaImagem));

                    jogador.setX(novaPosX);
                    jogador.setY(novaPosY);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int precisao) {
                    // Método não utilizado
                }
            };

            iniciarSensor(); // Método para iniciar o sensor
            handler.post(runnable); // Inicia a lógica de criação de objetos (zumbis)
        }


        private void iniciarSensor() {
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

        @Override
        protected void onResume() {
            super.onResume();
            iniciarSensor(); // Reinicia o sensor
            numeroZumbis = 1; // Reseta a contagem de zumbis
            // Reinicia os handlers e runnables para o cronômetro e animações
            handler.removeCallbacks(runnable);
            handler.post(runnable);
            cronometroHandler.removeCallbacks(cronometroRunnable);
            cronometroHandler.postDelayed(cronometroRunnable, 1000);
            animacaoHandler.post(animacaoRunnable);
            animacaoInimigoHandler.post(animacaoInimigoRunnable);
            animacaoBalaHandler.post(animacaoBalaRunnable);
        }

        @Override
        protected void onPause() {
            super.onPause();
            pararSensor(); // Pausa o sensor
            // Remove callbacks para evitar execuções indesejadas
            cronometroHandler.removeCallbacks(cronometroRunnable);
            handler.removeCallbacks(runnable);
            animacaoHandler.removeCallbacks(animacaoRunnable);
            animacaoInimigoHandler.removeCallbacks(animacaoInimigoRunnable);
            animacaoBalaHandler.removeCallbacks(animacaoBalaRunnable);
        }

        private void pararSensor() {
            sensorManager.unregisterListener(sensorEventListener);
        }

        private boolean verificarColisao(ImageView zumbi, ImageView jogador) {
            Rect rectZumbi = new Rect();
            zumbi.getHitRect(rectZumbi);
            Rect rectJogador = new Rect();
            jogador.getHitRect(rectJogador);
            return Rect.intersects(rectZumbi, rectJogador);
        }

        private void criarObjetoCaindo(int duracaoInicialQuedaObjeto) {
            ImageView objetoCaindo = new ImageView(this);
            objetoCaindo.setImageResource(R.drawable.passo1esqueleto);
            objetoCaindo.setTag("zumbi");
            int larguraZumbi = 75;
            int alturaZumbi = 75;
            objetoCaindo.setLayoutParams(new ConstraintLayout.LayoutParams(larguraZumbi, alturaZumbi));
            Display display = getWindowManager().getDefaultDisplay();
            Point tamanho = new Point();
            display.getSize(tamanho);
            int larguraTela = tamanho.x;
            int posicaoX = (int) (Math.random() * (larguraTela - larguraZumbi));
            objetoCaindo.setX(posicaoX);
            constraintLayout.addView(objetoCaindo);

            ValueAnimator animator = ValueAnimator.ofFloat(0, tamanho.y);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    objetoCaindo.setTranslationY(value);
                    if (verificarColisao(objetoCaindo, jogador)) {
                        animator.cancel();
                        playerGrito.start();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mostrarGameOver();
                            }
                        }, 150);
                    }
                }
            });
            animator.setDuration(duracaoQuedaObjeto);
            animator.start();
        }

        public void atirar(View v) {
            if (balas > 0) {
                balas--;
                playerTiro.start();
                ImageView bala = new ImageView(this);
                bala.setImageResource(R.drawable.bala1); // Inicie com o primeiro frame da animação da bala
                bala.setTag("bala");

                int larguraBala = 300; // Ajuste a largura da bala conforme necessário
                int alturaBala = 300;  // Ajuste a altura da bala conforme necessário
                bala.setLayoutParams(new ConstraintLayout.LayoutParams(larguraBala, alturaBala));

                // Ajustando a posição X da bala para centralizá-la em relação ao jogador
                float posXJogador = jogador.getX();
                int larguraJogador = jogador.getWidth();
                float posXInicialBala = posXJogador + larguraJogador / 2 - larguraBala / 2;
                bala.setX(posXInicialBala);

                // Ajustando a posição Y da bala
                float posYJogador = jogador.getY();
                int alturaJogador = jogador.getHeight();
                float posYInicialBala = posYJogador + alturaJogador / 10 - alturaBala / 2;
                bala.setY(posYInicialBala);

                constraintLayout.addView(bala);

                ValueAnimator animator = ValueAnimator.ofFloat(bala.getY(), 0); // Assumindo que a bala se move para cima
                animator.addUpdateListener(animation -> {
                    float value = (Float) animation.getAnimatedValue();
                    bala.setTranslationY(value);

                    for (int i = 0; i < constraintLayout.getChildCount(); i++) {
                        View child = constraintLayout.getChildAt(i);
                        if (child instanceof ImageView && child.getTag() != null && child.getTag().equals("zumbi")) {
                            if (verificarColisaoBala((ImageView) child, bala)) {
                                playerMorte.start();
                                animator.cancel();
                                constraintLayout.removeView(bala);
                                iniciarAnimacaoCorpoCaindo((ImageView) child);
                                break;
                            }
                        }
                    }

                    // Verifica se a bala chegou ao topo da tela
                    if (bala.getTranslationY() <= 0) {
                        animator.cancel();
                        constraintLayout.removeView(bala);
                    }
                });
                animator.setDuration(1000);
                animator.start();

                new Handler().postDelayed(() -> {
                    if (balas < MAX_BALAS) {
                        balas++;
                        atualizarMunicao();
                    }
                }, 5000); //tempo para recarregar cada bala

                atualizarMunicao();
            } else {
                Toast.makeText(getApplicationContext(), "Sem munição!", Toast.LENGTH_SHORT).show();
            }
        }


        private void iniciarAnimacaoCorpoCaindo(ImageView zumbi) {
            ImageView corpoCaindo = new ImageView(this);
            corpoCaindo.setLayoutParams(zumbi.getLayoutParams());
            corpoCaindo.setX(zumbi.getX());
            corpoCaindo.setY(zumbi.getY());
            constraintLayout.addView(corpoCaindo);
            constraintLayout.removeView(zumbi);

            // Exemplo usando um Handler para mudar os frames
            new Handler().postDelayed(new Runnable() {
                int frameIndex = 0;
                @Override
                public void run() {
                    if (frameIndex < framesCorpoCaindo.length) {
                        corpoCaindo.setImageResource(framesCorpoCaindo[frameIndex++]);
                        new Handler().postDelayed(this, 100);
                    } else {
                        constraintLayout.removeView(corpoCaindo);
                    }
                }
            }, 100);
        }

        private boolean verificarColisaoBala(ImageView obj1, ImageView obj2) {
            Rect rect1 = new Rect();
            obj1.getHitRect(rect1);

            Rect rect2 = new Rect();
            obj2.getHitRect(rect2);

            return Rect.intersects(rect1, rect2);
        }
        private void atualizarMunicao() {
            tvMunicao.setText("Tiros: " + balas);
        }

        private void mostrarGameOver() {

            playerFundo.stop();



            View gameOverView = getLayoutInflater().inflate(R.layout.gameover, null);
            setContentView(gameOverView);

            TextView temp = gameOverView.findViewById(R.id.tempo);

            int minutos = segundosPassados / 60;
            int segundos = segundosPassados % 60;
            String tempoFormatado = String.format(Locale.getDefault(), "Seu Tempo é: %02d:%02d.", minutos, segundos);
            temp.setText(tempoFormatado);

            // Encontrar botões e definir eventos de clique
            ImageButton btnReiniciar = gameOverView.findViewById(R.id.btnReiniciar);
            ImageButton btnVoltarMenu = gameOverView.findViewById(R.id.btnVoltarMenu);

            btnReiniciar.setOnClickListener(v -> reiniciarJogo());
            btnVoltarMenu.setOnClickListener(v -> voltarAoMenu());
        }

        private void reiniciarJogo() {

            // Redefinir o layout
            setContentView(R.layout.activity_game);

            // Reinicializar componentes da UI
            tvCronometro = findViewById(R.id.tvCronometro);
            constraintLayout = findViewById(R.id.constraintLayout);
            jogador = findViewById(R.id.player);

            // Redefinir as variáveis de estado do jogo
            segundosPassados = 0;
            numeroZumbis = 1;
            duracaoQuedaObjeto = 4000;
            duracaoInicialQuedaObjeto = 3000;
            intervaloCriacao = 2000;
            incrementoZumbis = 1;

            // Atualizar o cronômetro na tela
            tvCronometro.setText("00:00");

            // Reiniciar o sensor
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            iniciarSensor();

            // Limpar e reiniciar os Handlers
            handler.removeCallbacks(runnable);
            cronometroHandler.removeCallbacks(cronometroRunnable);

            handler.post(runnable);
            cronometroHandler.postDelayed(cronometroRunnable, 1000);

            // Adicionar o jogador novamente ao layout, se necessário
            if(jogador.getParent() == null) {
                constraintLayout.addView(jogador);
            }

            // Libera o MediaPlayer se já estiver em uso
            if (playerFundo != null) {
                playerFundo.release();
            }

            // Reinicializa e inicia a música de fundo
            playerFundo = MediaPlayer.create(this, R.raw.fundo);
            playerFundo.setLooping(true);
            playerFundo.start();
        }

        private void voltarAoMenu() {
            SharedPreferences sharedPreferences = getSharedPreferences("TempoMaximo", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            int TempoDePontos = sharedPreferences.getInt("TempoDePontos", 0);
            if(segundosPassados > TempoDePontos){
                editor.putInt("Tempo", segundosPassados);
                editor.putString("Jogador", getIntent().getStringExtra("PLAYER_NAME"));
                editor.apply();
            }

            Intent intent = new Intent(Game.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        public void voltar(View v) {
            Intent intent = new Intent(Game.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

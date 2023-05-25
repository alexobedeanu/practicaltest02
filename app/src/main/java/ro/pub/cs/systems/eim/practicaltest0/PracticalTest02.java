package ro.pub.cs.systems.eim.practicaltest0;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class PracticalTest02 extends AppCompatActivity {

    private MyServer server;
    private EditText portEditText;
    private Spinner currencySpinner;
    private Button startButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);

        portEditText = findViewById(R.id.editText_port);
        currencySpinner = findViewById(R.id.spinner_currency);
        startButton = findViewById(R.id.button_start);
        resultTextView = findViewById(R.id.textView_result);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int port = Integer.parseInt(portEditText.getText().toString());
                String currency = currencySpinner.getSelectedItem().toString();

                if (server == null) {
                    server = new MyServer(port);
                    server.start();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try (Socket socket = new Socket("localhost", port);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            out.println(currency);
                            final String response = in.readLine();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultTextView.setText(response);
                                }
                            });
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}

/*
se cere sa se implementeze o aplicatie client-server, ambele fiind  integrate in aceeasi aplicatie android.
serverul va pune la dispozitia clientilor cursul curent obtinut print api-ul coindesk.com.
spre exemplu, apelul urmator intoarce cursul bitcoin-usd-euro, in format json.
https://api.coindesk.com/v1/bpi/currentprice/BTC.json
serverul va mentine un cache actualizat la un minut, in care va stoca timpul ultime actualizari (hint: tagul "updated") si
valoarea curenta a 1 bitcoin in usd si eur (hint tag-urile rate din cadrul USD si EUR).
clientul va trimite o cerere la server folosindparametrii deconectare adresa si port.
o cerere a clientului consta in unul din mesajele "usd" sau "eur", iar raspunsul serverului va fi in moneda ceruta.
Clientul va afisa rezultatele serverul intr-un camp text.

1. Sa se implementeze o interfata grafica minima pentru aplicatia client-server - minimum un camp pentru specificarea
portului, un buton pentru speficiarea unei valute, un buton de initiere a cererii si un camp text pentru afisarea rezultatului.
2. sa se implementeza functionalitatea specifica serverului.
a) primirea informatiilor de la serviciul web
b) parsarea raspunsului
c) trimitere valorilor catre clienti, in functie de metoda solicitata, prin conectarea pe un port ,indicat de utilizator in interfata grafica.
3. sa se implementeze functionalitatea specifica clientului.
 */
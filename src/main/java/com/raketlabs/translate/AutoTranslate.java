package com.raketlabs.translate;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoTranslate extends JFrame {

    private final JFXPanel jfxPanel = new JFXPanel();
    private String text;

    public AutoTranslate(String text) {
        this.text = text;
        initComponents();
    }

    private void initComponents() {
        loadWebView();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jfxPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void loadWebView() {
        javafx.application.Platform.runLater(this::run);
    }

    private void run() {
        try {
            // make a scene
            WebView webView = new WebView();
            jfxPanel.setScene(new Scene(webView));

            // load script
            String script = Files.readString(Path.of("script.js"));

            // compose url
            String param = URLEncoder.encode(text, "UTF-8");
            String url = "https://translate.google.com/?sl=auto&tl=en&text=${param}&op=translate"
                    .replace("${param}", param);

            // load url
            WebEngine engine = webView.getEngine();
            engine.load(url);
            engine.getLoadWorker().stateProperty().addListener(
                    (ObservableValue<? extends Worker.State> observable,
                     Worker.State oldValue,
                     Worker.State newValue) -> {
                        if (newValue != Worker.State.SUCCEEDED) {
                            return;
                        }

                        Timer timer = new Timer(1000, e -> {
                            Platform.runLater(() -> {
                                try {
                                    String result = engine.executeScript(script).toString();
                                    System.out.printf("%s -> %s\n", text, result);
                                    System.exit(0);
                                } catch (Exception exception) {
                                    System.out.println(exception.getMessage());
                                }
                            });
                        });
                        timer.start();
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Source text not passed as argument");
            System.exit(-1);
        }

        SwingUtilities.invokeLater(() -> {
            AutoTranslate browser = new AutoTranslate(args[0]);
        });
    }
}

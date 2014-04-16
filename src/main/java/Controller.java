import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import utils.ExecuteShellCommand;
import utils.HistogramHelper;
import utils.MedianFilter;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.print.DocFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public static final String SAMPLE = "Вхідне зображення";
    public static final String STEGO = "Стего зображення";
    public static final String STEGO_AFTER_FILTRATION = "Стего зображення після фільтрації";

    //    private static String EMBED_DATA = "outguess -d data/some_text.txt ";
    private static String EMBED_DATA = "steghide embed -f -p \"passphrase\" -ef data/some_text.txt ";
    private static String SAMPLE_PATH;
    private static String STEGO_PATH;

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    private static ArrayList<int[]> sampleHisto = new ArrayList<int[]>(3);

    private static ArrayList<int[]> stegoHisto = new ArrayList<int[]>(3);

    private BufferedImage stegoImage;
    private BufferedImage filteredStegoImage;

    @FXML
    private LineChart inputChart;
    @FXML
    private LineChart filterChart;

    @FXML
    private ImageView inputImg;
    @FXML
    private ImageView stegoImg;

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton redBtn;
    @FXML
    private RadioButton greenBtn;
    @FXML
    private RadioButton blueBtn;

    @FXML
    private TextField thresholdField;
    @FXML
    private Text chiSquareText;

    @FXML
    private void handleOpenAction(){
        //Clear chart
        inputChart.getData().retainAll();
        filterChart.getData().retainAll();

        //Choose file
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);

        inputImg.setImage(new Image(file.toURI().toString()));

        //Build sample histogram
        XYChart.Series inputSeries = getHistoSeries(file, SAMPLE, RED);
        inputChart.getData().addAll(inputSeries);


        //Get stego file
        SAMPLE_PATH = "-cf " + file.getAbsolutePath();
        STEGO_PATH = " -sf data/" + file.getName().replaceAll(".jpg", "e.jpg");
        System.out.println(ExecuteShellCommand.executeCommand(EMBED_DATA + SAMPLE_PATH + STEGO_PATH));
        File stegoFile = new File("data/" + file.getName().replaceAll(".jpg", "e.jpg"));

        stegoImg.setImage(new Image(file.toURI().toString()));

        //Build stego histogram
        XYChart.Series stegoSeries = getHistoSeries(stegoFile, STEGO, RED);
        inputChart.getData().addAll(stegoSeries);



        XYChart.Series inputSeries1 = getSeries(sampleHisto.get(0), SAMPLE);
        filterChart.getData().addAll(inputSeries1);

//        MedianFilter filter = new MedianFilter(6);
//        filteredStegoImage = filter.filter(stegoImage);
//        XYChart.Series filteredSeries = getSeries(HistogramHelper.imageHistogram(filteredStegoImage).get(0),"Stego after filtration");
        XYChart.Series filteredSeries = getSampleWithDeviations(Controller.STEGO_AFTER_FILTRATION, RED);
        filterChart.getData().addAll(filteredSeries);

        chiSquareText.setText("" + calculateChiSquare(RED));

        setRadioGroupVisibility(isGrayScale());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setRadioGroupVisibility(false);
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle toggle2) {
                inputChart.getData().retainAll();
                filterChart.getData().retainAll();
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if(redBtn.equals(observableValue.getValue())){
                    inputChart.getData().addAll(getColorHistoSeries(SAMPLE, RED));
                    inputChart.getData().addAll(getColorHistoSeries(STEGO, RED));

                    filterChart.getData().addAll(getColorHistoSeries(SAMPLE, RED));
                    filterChart.getData().addAll(getSampleWithDeviations(STEGO_AFTER_FILTRATION, RED));

                    chiSquareText.setText("" + calculateChiSquare(RED));
                } else if(greenBtn.equals(observableValue.getValue())){
                    inputChart.getData().addAll(getColorHistoSeries(SAMPLE, GREEN));
                    inputChart.getData().addAll(getColorHistoSeries(STEGO, GREEN));

                    filterChart.getData().addAll(getColorHistoSeries(SAMPLE, GREEN));
                    filterChart.getData().addAll(getSampleWithDeviations(Controller.STEGO_AFTER_FILTRATION, GREEN));

                    chiSquareText.setText("" + calculateChiSquare(GREEN));
                } else if(blueBtn.equals(observableValue.getValue())){
                    inputChart.getData().addAll(getColorHistoSeries(SAMPLE, BLUE));
                    inputChart.getData().addAll(getColorHistoSeries(STEGO, BLUE));

                    filterChart.getData().addAll(getColorHistoSeries(SAMPLE, BLUE));
                    filterChart.getData().addAll(getSampleWithDeviations(Controller.STEGO_AFTER_FILTRATION, BLUE));

                    chiSquareText.setText("" + calculateChiSquare(BLUE));
                }
            }
        });

        thresholdField.setFocusTraversable(false);
        thresholdField.textProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.matches("\\d+")) {
                    int value = Integer.parseInt(newValue);
                } else {
                    thresholdField.setText(oldValue);
                }
            }
        });
    }

    private void setRadioGroupVisibility(boolean isVisible){
        redBtn.setVisible(isVisible);
        greenBtn.setVisible(isVisible);
        blueBtn.setVisible(isVisible);

        redBtn.setSelected(true);
    }

    private boolean isGrayScale(){
        int[] redHist = sampleHisto.get(RED);
        int[] greenHist = sampleHisto.get(GREEN);
        int[] blueHist = sampleHisto.get(BLUE);

        for(int i = 0; ++i < 5;){
            if(redHist[i] != blueHist[i] || redHist[i] != greenHist[i])
                return true;
        }
        return false;
    }




    /******************Util methods****************/
    private XYChart.Series getSeries(int[] histogram, String name){
        XYChart.Series series = new XYChart.Series();
        for(int i = 0; i < histogram.length; i++){
            series.getData().add(new XYChart.Data(i, histogram[i]));
        }
        series.setName(name);
        return series;
    }

    private int[] getHistogram(File file){
        PlanarImage image = JAI.create("fileload",file.getAbsolutePath());
        return HistogramHelper.imageHistogram(image.getAsBufferedImage()).get(0);//HistogramDemo.getHistogram(image);
    }

    private XYChart.Series getHistoSeries(File file, String name, int colHist){
        PlanarImage image = JAI.create("fileload",file.getAbsolutePath());
        ArrayList<int[]> histo = HistogramHelper.imageHistogram(image.getAsBufferedImage());

        if(STEGO.equals(name)){
            stegoImage = image.getAsBufferedImage();
        }

        switch(name){
            case SAMPLE:
                sampleHisto = histo;
            case STEGO:
                stegoHisto = histo;
        }

        int[] hist = histo.get(colHist);//HistogramDemo.getHistogram(image);

        return getSeries(hist, name);
    }

    private XYChart.Series getColorHistoSeries(String name, int colHist){
        ArrayList<int[]> histo = null;
        switch(name){
            case SAMPLE:
                histo = (ArrayList<int[]>) sampleHisto.clone();
                break;
            case STEGO:
                histo = (ArrayList<int[]>) stegoHisto.clone();
                break;
        }

        int[] hist = histo.get(colHist);
        return getSeries(hist, name);
    }

    private XYChart.Series getSampleWithDeviations(String name, int colHist){
        int[] histo = sampleHisto.get(colHist).clone();
        Random random = new Random();
        for(int i=0; ++i < histo.length;) {
            boolean sign = (random.nextInt(10) + 1) % 2 == 0;
            int deviationValue = random.nextInt(Integer.parseInt(thresholdField.getText()));

            histo[i] = histo[i] + (sign ? deviationValue : -deviationValue);

            if(histo[i] < 0) histo[i] = 0;
        }
        return getSeries(histo, name);
    }

    private float calculateChiSquare(int colHist){
        int[] sampleHist = sampleHisto.get(colHist).clone();
        int[] stegoHist = stegoHisto.get(colHist).clone();

        float returnValue = 0;

        for(int i = 0; ++i < sampleHist.length;)
            returnValue += (Math.pow((stegoHist[i] - sampleHist[i]), 2) / sampleHist[i]);

        return returnValue;
    }

}

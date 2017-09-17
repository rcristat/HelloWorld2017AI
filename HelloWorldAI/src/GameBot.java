import com.google.common.primitives.Ints;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.lang.reflect.Array;
import java.util.*;

public class GameBot {
    int[] nodes = {17, 12, 7, 4};
    //NeuralNetwork neuralNetwork = new NeuralNetwork(nodes);;

//    public static boolean checkForStopCondition(WebDriver driver){
//        String gameMessage = driver.findElement(By.className("game-message")).getAttribute("class");
//        gameMessage = gameMessage.substring(gameMessage.indexOf(" "));
//        if (gameMessage != null){
//            stopCondition = true;
//        } else {
//            stopCondition = false;
//        }
//        return stopCondition;
//    }


    public static ArrayList<Double> convertTileToInput(WebDriver driver){
        List<WebElement> tiles = driver.findElements(By.className("tile"));
        ArrayList<Tile> tileList = new ArrayList<>();
        int[][] values = new int[4][4];
        boolean hasModifier;
        ArrayList<Double> inputs = new ArrayList<>();
        //int score = Integer.parseInt(driver.findElement(By.className("score-container")).getAttribute("value"));
            for (int i = 0; i < tiles.size(); i ++){
                String elementPath = tiles.get(i).getAttribute("class");
                String [] tileData = elementPath.split(" ");
                int value = Integer.parseInt(tileData[1].substring(tileData[1].indexOf("-")+1));
                int x = Integer.parseInt(tileData[2].substring(tileData[2].lastIndexOf("-")-1, tileData[2].lastIndexOf("-")));
                int y =Integer.parseInt(tileData[2].substring(tileData[2].length()-1));
                int[] coords = {x,y};
                boolean shouldKeep;
                if (tileData.length == 4){
                    shouldKeep = true;
                } else {
                    shouldKeep = false;
                }
                Tile tile = new Tile(value, coords, shouldKeep);
                System.out.println(value + "  " + coords[0] + "," + coords[1] + "  " + shouldKeep);
                tileList.add(tile);
            }
        //gets rid of double instances and just takes the merged version of the tile
        for (int i = 0; i < tileList.size(); i++){
            for (int j = 0; j < tileList.size(); j++) {
                if (i != j){
                    if (tileList.get(i).coords.equals(tileList.get(j).coords)){
                        if (tileList.get(i).shouldKeep == true){
                            tileList.remove(j);
                        } else {
                            tileList.remove(i);
                        }
                    }
                }
            }
        }
        //Create list of all possible coordinates
        for (int i = 1; i < 5; i++){
            for (int j = 1; j < 5; j++){
                int[] testCoords = {i,j};
                //if there is a Tile at coordinates, the value is the value of the tile, otherwise is 0
                for (int k = 0; k < tileList.size(); k++){
                     if (testCoords.equals(tileList.get(k).coords)){
                         values[i-1][j-1] = tileList.get(k).value;
                     } else {
                         values[i-1][j-1] = 0;
                     }
                }
            }
        }
        //Convert 2D matrix to a 1D matrix
        ArrayList<Double> inputsList = new ArrayList<Double>();
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                inputsList.add((double)values[i][j]);
            }
        }

        //inputsList.add(0);
        //inputs[17] =
        tiles = null;
        double max = 0;
        for (int i = 0; i < inputsList.size(); i++){
            if (inputsList.get(i) > max) max = inputsList.get(i);
        }
        for (int i = 0; i < inputsList.size(); i++){
            inputs.add(inputsList.get(i)/max);
        }

        return inputsList;

    }
    public static ArrayList<NeuralNetwork> generateFirstGeneration(int[] networkDimensions){
        int populationSize = 20;
        ArrayList<NeuralNetwork> neuralNetworkArrayList = new ArrayList<>();
        for (int i = 0; i < populationSize; i++){
            neuralNetworkArrayList.add(new NeuralNetwork(networkDimensions));
        }
        return neuralNetworkArrayList;
    }

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Reece\\Downloads\\selenium-java-3.5.3\\chromedriver.exe");
        ChromeOptions o = new ChromeOptions();
        o.addArguments("webdriver.chrome.driver", "C:\\Users\\Reece\\Downloads\\selenium-java-3.5.3\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get("https://gabrielecirulli.github.io/2048/");
        WebElement body = driver.findElement(By.tagName("body"));
        int[] networkDimensions = {16, 12, 7, 4};
        ArrayList<NeuralNetwork> neuralNetworkList = new ArrayList<>();
        ArrayList<NeuralNetwork> neuralNetworksRanked = new ArrayList<>();
        ArrayList<NeuralNetwork> neuralNetworksMutated = new ArrayList<>();
        ArrayList<Double> oldInputs = new ArrayList<>();
        int illegalMovesCounter = 0;
        neuralNetworkList = generateFirstGeneration(networkDimensions);
        boolean exitCondition = false;
        for (int i = 0; i < neuralNetworkList.size(); i++) {
            do {
                if (illegalMovesCounter == 10) exitCondition = true;
                int score = Integer.parseInt(driver.findElement(By.className("score-container")).getText());
                neuralNetworkList.get(i).setFitness(score);
                ArrayList<Double> inputs = convertTileToInput(driver);
                double[] output = neuralNetworkList.get(i).feedForward(inputs);
                if (output[0] >= output[1] && output[0] >= output[2] && output[0] >= output[3]) {
                    body.sendKeys(Keys.ARROW_UP);
                }
                if (output[1] >= output[0] && output[1] >= output[2] && output[1] >= output[3]) {
                    body.sendKeys(Keys.ARROW_DOWN);
                }
                if (output[2] >= output[0] && output[2] >= output[1] && output[2] >= output[3]) {
                    body.sendKeys(Keys.ARROW_LEFT);
                }
                if (output[3] >= output[0] && output[3] >= output[1] && output[3] >= output[2]) {
                    body.sendKeys(Keys.ARROW_RIGHT);
                }
                if (oldInputs.equals(inputs)){
                    illegalMovesCounter++;
                }
                oldInputs = inputs;
                //check for exitCondition status
                WebElement gameMessage = driver.findElement(By.className("game-message"));
                String gameStatus = gameMessage.getAttribute("class");
                if (gameStatus.length() > 12) {
                    exitCondition = true;
                }
            } while (!exitCondition);

            //once the exit condition is true exit while loop
            if (exitCondition) {
                    //for finished neuralNet get the score and set as final fitness
                    if (neuralNetworkList.size() == 0){
                        neuralNetworksRanked.add(neuralNetworkList.get(i));
                    }
                        Collections.sort(neuralNetworkList, new Comparator<NeuralNetwork>() {
                            @Override
                            public int compare(NeuralNetwork o1, NeuralNetwork o2) {
                                return o2.fitness - o1.fitness;
                            }
                        });
                    while (neuralNetworksRanked.size() > 10){
                        neuralNetworksRanked.remove(10);
                    }
                    for (int j = 0; j < neuralNetworksRanked.size(); j++){
                        neuralNetworksMutated.add(neuralNetworksRanked.get(j).mutate(neuralNetworksRanked.get(j)));
                        neuralNetworksMutated.add(neuralNetworksRanked.get(j).mutate(neuralNetworksRanked.get(j)));
                    }
                    neuralNetworkList = neuralNetworksMutated;
            }
        }
    }
}
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork{
    public int fitness;
    private Random r = new Random();
    private int[] layers;
    private double[][] neurons;
    private double[][][] weights;
    //Constructor
   public NeuralNetwork(int[] layers){
        //Create layers containing neuron counts
        this.layers = new int[layers.length];
        for (int i = 0; i < layers.length; i++){
            this.layers[i] = layers[i];
        }
        //Initialize neurons
        initNeurons();
       //Initialize synapse weights
        initWeights();
    }

    //Creates 2D Neuron Array
    public void initNeurons(){
      ArrayList<double[]> neuronList = new ArrayList<double[]>();
      //create a list of arrays of length [NEURON COUNT]
      for (int i = 0; i < layers.length; i++) {
        neuronList.add(new double[layers[i]]);
      }
      //convert list of arrays into a 2D Neuron array
      neurons = neuronList.toArray(new double[neuronList.size()][]);
    }

    //Adds synapses and their respective weights
    public void initWeights(){
        ArrayList<double[][]> weightsList = new ArrayList<double[][]>();

        for (int i = 1; i < neurons.length; i++){
            //Creates an array of synapses of length [NEURONS IN PREVIOUS LAYER]
            ArrayList<double[]> layerWeightList = new ArrayList<double[]>();
            int synapses = layers[i-1];
            for (int j = 0; j < neurons[i].length; j++){ //TODO: THIS IS THE PROBLEM
                System.out.println(neurons[i].length);
                double[] synapseWeights = new double[synapses];
                //Assigns weight values to each synapses
                for (int k = 0; k < synapses; k++){
                    synapseWeights[k] = ThreadLocalRandom.current().nextDouble(-1,1);
                }
                layerWeightList.add(synapseWeights);
            }
            weightsList.add(layerWeightList.toArray(new double[layerWeightList.size()][]));
        }
        weights = weightsList.toArray(new double[weightsList.size()][][]);
    }

    public double[] feedForward(ArrayList<Double> inputs){
        for (int i = 0; i < inputs.size(); i++){
            neurons[0][i] = inputs.get(i);
        }
        for (int i = 1; i < layers.length; i ++){
            for (int j = 0; j < neurons[i].length; j++){
                double value = 0.25;
                for (int k = 0;  k < neurons[i -1].length; k++){
                    value += weights[i - 1][j][k] * neurons[i - 1][k];
                }
                neurons[i][j] = (float) Math.tanh(value);
            }
        }
        return neurons[neurons.length-1];
    }

    public NeuralNetwork mutate(NeuralNetwork network){
        for (int i = 0; i < network.weights.length; i++) {
            for (int j = 0; j < network.weights[i].length; j++) {
                for (int k = 0; k < network.weights[i][j].length; k++) {
                    double weight = weights[i][j][k];
                    double mutationValue = ThreadLocalRandom.current().nextDouble(.95, 1.05);
                    weight *= mutationValue;
                    weights[i][j][k] = weight;
                }
            }
        } return network;
    }

    public void setFitness(int fitness){
        this.fitness = fitness;
    }

    public int compareNeuralNet(NeuralNetwork other){
        if (other == null) return 1;

        if (fitness > other.fitness)
            return 1;
        else if (fitness < other.fitness)
            return -1;
        else
            return 0;
    }
}
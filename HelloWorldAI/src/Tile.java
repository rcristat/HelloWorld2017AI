public class Tile {
    int value;
    int [] coords = new int[2];
    boolean shouldKeep;
     public Tile(int value, int[] coords, boolean modifier){
         this.value = value;
         this.coords = coords;
         this.shouldKeep = modifier;
     }
}

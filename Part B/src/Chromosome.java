public class Chromosome {
    private int id;
    private int[][] Population;
    private int score;

    public Chromosome(int[][] population,int id) {
        this.Population = population;
        this.score = 0;
        this.id = id;
    }

    public int getId() { return id; }

    public int[][] getPopulation() { return Population; }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }
}

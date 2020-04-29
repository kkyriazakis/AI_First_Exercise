import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Main {

    private static final int[][]  hConst = {{10,10,5},{10,10,5},{5,10,5},{5,5,5},{5,10,5},{5,5,5},{5,5,5}};
    private static int ChromosomeID = 1;
    private static Writer wr;
    private static final int populationSize = 1000;
    private static final int numOfWorkers = 30;
    private static final int numOfDays = 14;

    private static final double selectionFactor = 0.75;
    private static final double mutationChance = 0.2;
    private static final double crossChance = 1;

    private static final int maxIterationsWithoutChange = 1000;
    private static final int maxIterations = 10*maxIterationsWithoutChange;


    public static void main(String[] args) {
        int noChangeCtr = 0;
        int bestscore = Integer.MAX_VALUE;
        List<Chromosome> population = new LinkedList<>();
        List<Chromosome> selected, tmp, new_popul;

        try {
            wr = new FileWriter("PopSize" + populationSize + "-Psel" + selectionFactor + "-Pcross" + crossChance + "-Pmut" + mutationChance + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*----- CREATE INITIAL POPULATION -----*/
        while (population.size() < populationSize){
            Chromosome chr = randomizeChromosome( generateBaseChromosome() );
            if (checkFeasibility(chr)){ //FEASIBILITY CHECK
                chr.updateScore();      //CHROMOSOME RATING
                population.add(chr);
            }
        }

        int iteration_ctr = 1;
        /*----- START GENETIC ALGORITHM PROCCESS -----*/
        while ( noChangeCtr < maxIterationsWithoutChange && iteration_ctr < maxIterations) {
            int[] generation_score = new int[populationSize];   //Used to calculate score mean

            /* ================== SELECTION ================== */
            selected = rouletteWheelSelection(population);

            /* ================== CROSSOVER ================== */
            tmp = onePointSquareCrossover(selected);
            //tmp = twoPointHorizontal(selected);
            //tmp = twoPointVertical(selected);

            /* ==================  MUTATION  ================== */
            swapMutation(tmp);
            //flipMutate(tmp);
            //boundaryMutation(tmp);
            //inversionMutation(tmp);
            //swapColumnMutation(tmp);
            //squareSwapMutation(tmp);

            /* Feasibility Check, Score Update, fill Population with old chromosomes */
            new_popul = new LinkedList<>();
            Chromosome chr;
            for (int j = 0; j< populationSize; j++){
                if ( j < tmp.size() ){
                    chr = tmp.get(j);
                    if (checkFeasibility( chr )){ //FEASIBILITY CHECK
                        chr.updateScore();  //CHROMOSOME RATING
                        generation_score[j] = chr.getScore();
                        new_popul.add(chr);
                    }
                    else
                        tmp.remove(j--);
                }
                else {
                    chr = population.get(j - tmp.size());
                    new_popul.add( chr );
                    generation_score[j] = chr.getScore();
                }
            }
            sortByScore( new_popul );
            population = new LinkedList<>( new_popul );

            double mean = Arrays.stream(generation_score).average().orElse(Double.NaN);
            try {
                wr.write(iteration_ctr + ":" + mean + ":" + population.get(0).getScore() + "\n");
            } catch (IOException e) { e.printStackTrace(); }

            /* ---- PRINT DEBUG INFO ---- */
            System.out.printf("%-4d -- FeasibleSize( %-4d ) -- Best cost = %-6d -- Generation[%d]\n",noChangeCtr,tmp.size(),population.get(0).getScore(),iteration_ctr++);

            if (population.get(0).getScore() < bestscore){  //Check and update best score
                bestscore = population.get(0).getScore();
                noChangeCtr = 0;
            }
            else {
                noChangeCtr++;
            }
        }

        try { wr.close(); }
        catch (IOException e) { e.printStackTrace(); }

        //Get best Chromosome
        Chromosome solution = population.get(0);

        System.out.println("\nBest Solution found:");
        System.out.println("Chromosome ID   = " + solution.getId());
        System.out.println("Chromosome Cost = " + solution.getScore());
    }

    public static void swapMutation(List<Chromosome> population){
        int i,j;
        for (Chromosome c : population) {
            int col = new Random().nextInt(numOfDays); //Random column
            int[][] init = c.getState();

            i = new Random().nextInt(numOfWorkers);
            j = new Random().nextInt(numOfWorkers);
            double mut = new Random().nextDouble();

            if( i!=j && mut <= mutationChance){
                int a = init[i][col];
                int b = init[j][col];
                init[i][col] = b;
                init[j][col] = a;

                c.setState(init);
            }
        }
    }

    public static void swapColumnMutation(List<Chromosome> selected) {
        for (Chromosome c : selected) {
            int[][] init = c.getState();

            int i = new Random().nextInt(numOfDays);
            int j = new Random().nextInt(numOfDays);
            double mut = new Random().nextDouble();

            if (i != j && mut <= mutationChance) {
                int[] tmp = new int[numOfWorkers];
                for (int x=0; x<numOfWorkers; x++){
                    tmp[x]=init[x][i];
                    init[x][i] = init[x][j];
                    init[x][j] = tmp[x];
                }
                c.setState(init);
            }
        }
    }

    public static void squareSwapMutation(List<Chromosome> selected){
        for (Chromosome c : selected) {
            double mut = new Random().nextDouble();
            if (mut > mutationChance)
                continue;

            int rndx = numOfWorkers/2;
            int rndy = numOfDays/2;

            int[][] temp = c.getState();
            boolean coin = new Random().nextBoolean();
            for (int i=0; i<numOfWorkers; i++) {
                for (int j=0; j<numOfDays; j++){
                    if(coin){
                        if ((i < rndx) && (j < rndy)){
                            temp[i][j] = c.getState()[i+rndx][j+rndy];
                        }
                        if ((i >= rndx) && (j >= rndy)){
                            temp[i][j] = c.getState()[i-rndx][j-rndy];
                        }
                    }
                    else{
                        if ((i < rndx) && (j >= rndy)){
                            temp[i][j] = c.getState()[i+rndx][j-rndy];
                        }
                        if ((i >= rndx) && (j < rndy)){
                            temp[i][j] = c.getState()[i-rndx][j+rndy];
                        }
                    }
                }
            }
            c.setState(temp);
        }
    }

    public static void inversionMutation(List<Chromosome> population){
        for (Chromosome c : population) {
            double mut = new Random().nextDouble();
            if(mut > mutationChance)
                continue;

            int x = new Random().nextInt(numOfDays); //column
            int i = new Random().nextInt(numOfWorkers);
            int j = new Random().nextInt(numOfWorkers);
            int[][] tmp = c.getState().clone();

            if (i < j) {
                int b = j;
                for (int y=i ; y<j; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            }
            else {
                int b = i;
                for (int y=j; y<i; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            }
            c.setState(tmp);
        }
    }


    public static void flipMutate(List<Chromosome> population) {
        int point1, point2;
        int [][] state = new int[numOfWorkers][numOfDays];

        for (Chromosome chr : population) {
            double mut = new Random().nextDouble();
            if(mut > mutationChance)
                continue;

            for (int j = 0; j < 3; j++) {   //THREE RANDOM LOCATIONS
                point1 = new Random().nextInt(numOfWorkers);
                point2 = new Random().nextInt(numOfDays);
                state = chr.getState();

                if (state[point1][point2] == 0)
                    state[point1][point2] = 1;
                else if (state[point1][point2] == 1)
                    state[point1][point2] = 0;
                else if (state[point1][point2] == 2)
                    state[point1][point2] = 3;
                else if (state[point1][point2] == 3)
                    state[point1][point2] = 2;
            }
            chr.setState(state);
        }
    }

    public static void boundaryMutation(List<Chromosome> population){
        for (Chromosome chr : population) {
            double mut = new Random().nextDouble();
            if(mut > mutationChance)
                continue;

            for (int j = 0; j < 2; j++) { //Maybe get the upper bound of 1% online
                int x = new Random().nextInt(numOfWorkers);
                int y = new Random().nextInt(numOfDays);
                boolean coin = new Random().nextBoolean();
                int[][] tmp = chr.getState();
                if (coin) {
                    tmp[x][y] = 3;
                } else {
                    tmp[x][y] = 0;
                }
                chr.setState(tmp);
            }
        }
    }


    public static List<Chromosome> onePointSquareCrossover(List<Chromosome> selected){
        List<Chromosome> newPopulation = new ArrayList<>();

        for(int i = 1; i < selected.size(); i = i + 2){
            double mut = new Random().nextDouble();
            if(mut > crossChance) {
                newPopulation.add(selected.get(i));
                newPopulation.add(selected.get(i - 1));
                continue;
            }

            int rndx = new Random().nextInt(selected.get(i).getState().length);
            int rndy = new Random().nextInt(selected.get(i).getState()[0].length);

            int[][] temp1 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];
            int[][] temp2 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];

            for (int x = 0; x<selected.get(i).getState().length; x++) {
                for (int y=0; y<selected.get(i).getState()[0].length; y++){
                    temp1[x][y] = (((x < rndx) && (y < rndy)) || ((x >= rndx) && (y >= rndy)) ? 1 : 0)*selected.get(i-1).getState()[x][y]
                            + (((x < rndx) && (y >= rndy)) || ((x >= rndx) && (y < rndy)) ? 1 : 0)*selected.get(i).getState()[x][y];

                    temp2[x][y] = (((x < rndx) && (y < rndy)) || ((x >= rndx) && (y >= rndy)) ? 1 : 0)*selected.get(i).getState()[x][y]
                            + (((x < rndx) && (y >= rndy)) || ((x >= rndx) && (y < rndy)) ? 1 : 0)*selected.get(i-1).getState()[x][y];
                }
            }
            Chromosome c1 = new Chromosome(temp1, ChromosomeID++);
            Chromosome c2 = new Chromosome(temp2, ChromosomeID++);

            newPopulation.add(c1);
            newPopulation.add(c2);
        }
        return newPopulation;
    }


    public static List<Chromosome> twoPointVertical(List<Chromosome> iPopulation) {
        Chromosome c1,c2;
        List<Chromosome> fPopulation = new LinkedList<>();
        int point1, point2;
        int [][] init1, init2, fin1, fin2;

        fin1 = new int[numOfWorkers][numOfDays];
        fin2 = new int[numOfWorkers][numOfDays];

        for (int i=0; i<iPopulation.size()/2; i++ ){
            point1 = new Random().nextInt(numOfDays);
            point2 = new Random().nextInt(numOfDays) + numOfDays;

            init1 = iPopulation.get(2*i).getState();
            try {
                init2 = iPopulation.get(2*i + 1).getState();
            }catch ( ArrayIndexOutOfBoundsException e ) { break; }

            double mut = new Random().nextDouble();
            if(mut > crossChance) {
                fPopulation.add(iPopulation.get(2*i));
                fPopulation.add(iPopulation.get(2*i + 1));
                continue;
            }

            for (int x=0; x<numOfWorkers; x++){
                for (int y=0; y<numOfDays; y++){
                    if ( y <= point1 ){
                        fin1[x][y] = init1[x][y];
                        fin2[x][y] = init2[x][y];
                    }
                    else if ( y <= point2 ){
                        fin1[x][y] = init2[x][y];
                        fin2[x][y] = init1[x][y];
                    }
                    else {
                        fin1[x][y] = init1[x][y];
                        fin2[x][y] = init2[x][y];
                    }
                }
            }
            c1 = new Chromosome(fin1,ChromosomeID++);
            c2 = new Chromosome(fin2,ChromosomeID++);
            fPopulation.add(c1);
            fPopulation.add(c2);
        }

        return fPopulation;
    }


    public static List<Chromosome> twoPointHorizontal(List<Chromosome> iPopulation){
        List<Chromosome> newPopulation = new ArrayList<>();

        for (int i=1; i<iPopulation.size(); i+=2){
            int rnd1 = new Random().nextInt(numOfWorkers/2);
            int rnd2 = new Random().nextInt(numOfWorkers/2) + numOfWorkers/2;

            int[][] temp1 = new int[numOfWorkers][numOfDays];
            int[][] temp2 = new int[numOfWorkers][numOfDays];

            for(int j=0; j<numOfWorkers; j++){
                double mut = new Random().nextDouble();
                if(mut > crossChance) {
                    newPopulation.add(iPopulation.get(i));
                    newPopulation.add(iPopulation.get(i-1));
                    continue;
                }

                for (int x=0; x<numOfDays; x++) {
                    if (j < rnd1) {
                        temp1[j][x] = iPopulation.get(i).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i - 1).getState()[j][x];
                    }
                    else if (j >= rnd1 && j < rnd2) {
                        temp1[j][x] = iPopulation.get(i - 1).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i).getState()[j][x];
                    }
                    else if (j >= rnd2) {
                        temp1[j][x] = iPopulation.get(i).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i - 1).getState()[j][x];
                    }
                }
            }
            Chromosome c1 = new Chromosome(temp1,ChromosomeID++);
            Chromosome c2 = new Chromosome(temp2,ChromosomeID++);
            newPopulation.add(c1);
            newPopulation.add(c2);
        }
        return newPopulation;
    }

    public static List<Chromosome> rouletteWheelSelection(List<Chromosome> iPopulation){
        int selectionSize = (int) (iPopulation.size()*selectionFactor);  //Keep half of the original

        double fitness;
        double[] probFitness = new double[iPopulation.size()];
        double[] sumFit = new double[iPopulation.size()];

        double score = iPopulation.get(0).getScore();
        sumFit[0] = 1/score;
        for (int i=1; i<iPopulation.size(); i++){
            score = iPopulation.get(i).getScore();
            fitness = 1/score;
            sumFit[i] = sumFit[i-1] + fitness;
        }

        Random rand = new Random();
        List<Chromosome> newPopulation = new ArrayList<>();
        for (int i=0; i<selectionSize; i++){
            double randomFitness = rand.nextDouble() * sumFit[probFitness.length-1];
            int index = Arrays.binarySearch(sumFit,randomFitness);
            if (index < 0){
                index = Math.abs(index + 1);
            }
            if (iPopulation.size() == index) {index--;}
            newPopulation.add(iPopulation.get(index));
        }
        return newPopulation;
    }


    public static int[] getColumn(int[][] x, int col){
        int[] y = new int[x.length];
        for(int i=0; i<x.length; i++){
            y[i] = x[i][col];
        }
        return y;
    }

    public static Chromosome randomizeChromosome(Chromosome chr){
        int [][] pop = chr.getState();
        int [] col;

        for(int i=0; i<numOfDays; i++){
            col = getColumn(pop, i);    //EXTRACT COLUMN NUM i 

            List<Integer> tmp = new ArrayList<>();
            for (int w=0; w<numOfWorkers; w++){
                tmp.add(col[w]);
            }
            Collections.shuffle(tmp, new Random());
            col = tmp.stream().mapToInt(x->x).toArray();

            for(int j=0; j<col.length; j++){    //SET RANDOMIZED COLUMN BACK TO POPULATION
                pop[j][i] = col[j];
            }
        }
        Chromosome new_chr = new Chromosome(pop, ChromosomeID);
        ChromosomeID++;
        return new_chr;
    }


    public static boolean checkFeasibility(Chromosome chromosome){
        int[][] array = chromosome.getState();
        int morningW1, morningW2;
        int afternoonW1, afternoonW2;
        int nightW1, nightW2;

        for(int j=0; j<numOfDays; j++){     //CHECKING HARD CONSTRAINTS
            morningW1=0; afternoonW1=0; nightW1=0;
            morningW2=0; afternoonW2=0; nightW2=0;
            for (int i=0; i<numOfWorkers; i++){
                switch (array[i][j]){       //CHECKING FIRST WEEK
                    case 0: break;
                    case 1: morningW1++; break;
                    case 2: afternoonW1++; break;
                    case 3: nightW1++; break;
                    default:System.out.println(array[i][j]+" is not an option!");
                            return false;
                }

                switch (array[i][j+7]){     //CHECKING SECOND WEEK
                    case 0: break;
                    case 1: morningW2++; break;
                    case 2: afternoonW2++; break;
                    case 3: nightW2++; break;
                    default:System.out.println(array[i][j+7]+" is not an option!");
                            return false;
                }
            }
            if(!(morningW1==hConst[j][0] && afternoonW1==hConst[j][1] && nightW1==hConst[j][2])){
                return false;
            }
            if(!(morningW2==hConst[j][0] && afternoonW2==hConst[j][1] && nightW2==hConst[j][2])){
                return false;
            }
        }
        return true;
    }


    public static Chromosome generateBaseChromosome(){
        int[][] pop = new int[numOfWorkers][numOfDays];

        for (int d=0; d<numOfDays; d++){    //FOR EACH DAY
            for (int e=0; e<numOfWorkers; e++){   //FOR EACH EMPLOYEE
                if( e >= hConst[d][0] + hConst[d][1] + hConst[d][2]){   //SET DAY OFF
                    pop[e][d] = 0;
                    pop[e][d+7] = 0;
                }
                else if( e >= hConst[d][0] + hConst[d][1]){ //SET NIGHT SHIFT
                    pop[e][d] = 3;
                    pop[e][d+7] = 3;
                }
                else if( e >= hConst[d][0]){    //SET AFTERNOON SHIFT
                    pop[e][d] = 2;
                    pop[e][d+7] = 2;
                }
                else if( e < hConst[d][0] ){    //SET MORNING SHIFT
                    pop[e][d] = 1;
                    pop[e][d+7] = 1;
                }
            }
        }
        return new Chromosome(pop, ChromosomeID++);
    }


    public static void sortByScore(List<Chromosome> list){
        list.sort((o1, o2) -> {
            double i = o1.getScore() - o2.getScore();
            return (int) i;
        });
    }
}

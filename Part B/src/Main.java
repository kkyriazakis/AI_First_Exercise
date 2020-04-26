import java.util.*;

public class Main {

    private static final int[][]  hConst = {{10,10,5},{10,10,5},{5,10,5},{5,5,5},{5,10,5},{5,5,5},{5,5,5}};
    private static int ChromosomeID = 1;
    private static final double selectionFactor = 0.6;
    private static final int PopulationSize = 5000;
    private static final int maxIterationsWithoutChange = 2000;
    private static final double mutationChance = 0.5;

    public static void main(String[] args) {
        int noChangeCtr = 0;
        int bestscore = Integer.MAX_VALUE;
        List<Chromosome> population = new LinkedList<>();
        List<Chromosome> selected, tmp, new_popul;

        /*----- CREATE INITIAL POPULATION -----*/
        while (population.size() < PopulationSize){
            Chromosome chr = randomizeChromosome( generateBaseChromosome() );
            if (checkFeasibility(chr)){ //FEASIBILITY CHECK
                chr.updateScore();  //CHROMOSOME RATING
                population.add(chr);
            }
        }

        int i = 1;
        /*----- START GENETIC ALGORITHM PROCCESS -----*/
        while ( noChangeCtr < maxIterationsWithoutChange) {
            selected = rouletteWheelSelection(population); //SELECTION

            /* ---- CROSSOVER ---- */
            //tmp = twoPointHorizontal(selected);
            //tmp = twoPointVertical(selected);
            tmp = onePointSquareCrossover(selected);

            /* ---- MUTATION ---- */
            swapMutation(tmp);
            //flipMutate(tmp);
            //boundaryMutation(tmp);
            //inversionMutation(tmp);


            //FILL THE BLANKS
            new_popul = new LinkedList<>();
            Chromosome chr;

            for (int j=0; j<PopulationSize; j++){
                if ( j < tmp.size() ){
                    chr = tmp.get(j);
                    if (checkFeasibility( chr )){ //FEASIBILITY CHECK
                        chr.updateScore();  //CHROMOSOME RATING
                        new_popul.add(chr);
                    }
                    else
                        tmp.remove(j--);
                }
                else
                    new_popul.add( population.get(j - tmp.size()) );
            }

            sortByScore( new_popul );
            population = new LinkedList<>(new_popul);

            /* ---- PRINT DEBUG INFO ---- */
            System.out.printf("%-4d -- FeasibleSize( %-4d ) -- Best cost = %-6d -- Generation[%d]\n", noChangeCtr, tmp.size(), population.get(0).getScore(),i++);

            if (population.get(0).getScore() >= bestscore){
                noChangeCtr++;
            }
            else {
                bestscore = population.get(0).getScore();
                noChangeCtr = 0;
            }
        }

        Chromosome solution = population.get(0);

        System.out.println("\nBest Solution found:");
        System.out.println("Chromosome ID = " + solution.getId());
        System.out.println("Chromosome Cost = " + solution.getScore());
    }

    public static void swapMutation(List<Chromosome> population){
        int i,j;
        for (Chromosome c : population) {
            int col = new Random().nextInt(14); //Random column
            int[][] init = c.getState();

            i = new Random().nextInt(30);
            j = new Random().nextInt(30);
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
            //int line = new Random().nextInt(30);   //static line
            int[][] init = c.getState();

            int i = new Random().nextInt(14);
            int j = new Random().nextInt(14);
            double mut = new Random().nextDouble();

            if (i != j && mut <= mutationChance) {
                int[] tmp = new int[30];
                for (int x=0; x<30; x++){
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

            int rndx = c.getState().length/2;
            int rndy = c.getState()[0].length/2;

            //int[][] temp = new int[c.getState().length][c.getState()[0].length];
            int[][] temp=c.getState();
            boolean coin = new Random().nextBoolean();
            for (int x = 0; x < c.getState().length; x++) {
                //System.out.println("X " + x);
                for (int y=0;y<c.getState()[0].length;y++){
                    //System.out.println("Y " + y);
                    if(coin){
                        if((x < rndx) && (y < rndy)){
                            temp[x][y]=c.getState()[x+rndx][y+rndy];
                        }
                        if ((x >= rndx) && (y >= rndy)){
                            temp[x][y]=c.getState()[x-rndx][y-rndy];
                        }
                    }else{
                        if((x < rndx) && (y >= rndy)){
                            temp[x][y]=c.getState()[x+rndx][y-rndy];
                        }
                        if ((x >= rndx) && (y < rndy)){
                            temp[x][y]=c.getState()[x-rndx][y+rndy];
                        }
                    }
                }
                c.setState(temp);
            }
        }
    }

    public static void inversionMutation(List<Chromosome> population){
        for (Chromosome c : population) {
            double mut = new Random().nextDouble();
            if(mut > mutationChance)
                continue;

            int x = new Random().nextInt(c.getState()[0].length); //column
            int i = new Random().nextInt(c.getState().length);
            int j = new Random().nextInt(c.getState().length);
            int[][] tmp = c.getState().clone();

            if (i < j) {
                int b = j;
                for (int y = i; y < j; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            }
            else {
                int b = i;
                for (int y = j; y < i; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            }
            c.setState(tmp);
        }
    }


    public static void flipMutate(List<Chromosome> population) {
        int point1, point2;
        int [][] state = new int[0][0];

        for (Chromosome chr : population) {
            double mut = new Random().nextDouble();
            if(mut > mutationChance)
                continue;

            for (int j = 0; j < 3; j++) {   //THREE RANDOM LOCATIONS
                point1 = new Random().nextInt(30);
                point2 = new Random().nextInt(14);
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
                int x = new Random().nextInt(chr.getState().length);
                int y = new Random().nextInt(chr.getState()[0].length);
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
            int rndx = new Random().nextInt(selected.get(i).getState().length);
            int rndy = new Random().nextInt(selected.get(i).getState()[0].length);

            int[][] temp1 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];
            int[][] temp2 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];
            for (int x = 0; x < selected.get(i).getState().length; x++) {
                for (int y=0;y<selected.get(i).getState()[0].length;y++){
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

        fin1 = new int[30][14];
        fin2 = new int[30][14];

        for (int i=0; i<iPopulation.size()/2; i++ ){
            point1 = new Random().nextInt(7);
            point2 = new Random().nextInt(7) + 7;

            init1 = iPopulation.get(2*i).getState();
            try {
                init2 = iPopulation.get(2*i + 1).getState();
            }catch ( ArrayIndexOutOfBoundsException e ) { break; }


            for (int x=0; x<30; x++){
                for (int y=0; y<14; y++){
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
            int rnd1 = new Random().nextInt(iPopulation.get(i).getState().length/2);
            int rnd2 = new Random().nextInt(iPopulation.get(i).getState().length/2) + iPopulation.get(i).getState().length/2;

            int[][] temp1 = new int[iPopulation.get(i).getState().length][iPopulation.get(i).getState()[0].length];
            int[][] temp2 = new int[iPopulation.get(i).getState().length][iPopulation.get(i).getState()[0].length];

            for(int j=0;j<iPopulation.get(i).getState().length;j++){
                if( j < rnd1){
                    for (int x=0;x<iPopulation.get(i).getState()[0].length;x++){
                        temp1[j][x] = iPopulation.get(i).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i-1).getState()[j][x];
                    }
                }
                if( j >= rnd1 && j < rnd2){
                    for (int x=0;x<iPopulation.get(i).getState()[0].length;x++){
                        temp1[j][x] = iPopulation.get(i-1).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i).getState()[j][x];
                    }
                }
                if( j>= rnd2){
                    for (int x=0;x<iPopulation.get(i).getState()[0].length;x++){
                        temp1[j][x] = iPopulation.get(i).getState()[j][x];
                        temp2[j][x] = iPopulation.get(i-1).getState()[j][x];
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

        for(int i=0; i<14; i++){
            col = getColumn(pop, i);    //EXTRACT COLUMN NUM i 

            List<Integer> tmp = new ArrayList<>();
            for (int w=0; w<30; w++){
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
        //int[][] array = testArray;
        int morningW1, morningW2;
        int afternoonW1, afternoonW2;
        int nightW1, nightW2;

        for(int j=0; j<7; j++){     //CHECKING HARD CONSTRAINTS
            morningW1=0; afternoonW1=0; nightW1=0;
            morningW2=0; afternoonW2=0; nightW2=0;
            for (int i=0; i<30; i++){
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
        int[][] pop = new int[30][14];
        int NumOfDays = hConst.length;
        int NumOfEmployees = 30;

        for (int d=0; d<NumOfDays; d++){    //FOR EACH DAY
            for (int e=0; e<NumOfEmployees; e++){   //FOR EACH EMPLOYEE
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

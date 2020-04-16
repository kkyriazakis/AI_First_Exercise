import java.util.*;

public class Main {

    private static final int[][]  hConst = {{10,10,5},{10,10,5},{5,10,5},{5,5,5},{5,10,5},{5,5,5},{5,5,5}};
    private static int ChromosomeID = 1;
    private static final int selectionFactor = 2;
    private static final int PopulationSize = 500;
    private static final int maxIterations = 1000;
    private static final int maxIterationsWithoutChange = 70;

    public static void main(String[] args) {
        int noChangeCtr,bestscore;
        List<Chromosome> population = new LinkedList<>();

        for (int i=0; i<PopulationSize; i++){   //CREATE INITIAL POPULATION
            Chromosome chr = randomizeChromosome(generateChromosome());
            if (checkFeasibility(chr)){ //FEASIBILITY CHECK
                chr.updateScore();  //CHROMOSOME RATING
                population.add(chr);
            }
            else { i--; }
        }

        List<Chromosome> selected, tmp1, tmp_pop;
        bestscore = 9000;
        noChangeCtr = 0;
        for(int i=0; i<maxIterations; i++){
            if (noChangeCtr >= maxIterationsWithoutChange)
                break;

            selected = rouletteWheelSelection(population); //SELECTION

            /* -- CROSSOVER -- */
            tmp1 = twoPointVertical(selected);
            //tmp1 = twoPointHorizontal(selected);

            /* -- MUTATION -- */
            flipMutate(tmp1);
            //boundaryMutation(tmp1);
            //inversionMutation(tmp1);


            //FILL THE BLANKS
            tmp_pop = new LinkedList<>();
            for (int j=0; j<PopulationSize; j++){
                if ( j<tmp1.size() ){
                    if (checkFeasibility( tmp1.get(j) )){ //FEASIBILITY CHECK
                        tmp_pop.get(j).updateScore();  //CHROMOSOME RATING
                        population.add(tmp1.get(j));
                    }
                    else { j--; }
                }
                else {
                    tmp_pop.add( population.get(j - tmp1.size()) );
                }
            }
            population = tmp_pop;
            sortByScore( population );

            if (population.get(0).getScore() >= bestscore){
                noChangeCtr++;
            }
            else {
                bestscore = population.get(0).getScore();
                noChangeCtr = 0;
            }
        }
        Chromosome solution = population.get(0);

        System.out.println("Solution found");
        System.out.println("cost = " + solution.getScore());

    }

    public static void inversionMutation(List<Chromosome> selected){
        for (int z=0; z<selected.size(); z++) {
            Chromosome c = selected.get(z);
            int x = new Random().nextInt(c.getState()[0].length);
            int i = new Random().nextInt(c.getState().length);
            int j = new Random().nextInt(c.getState().length);

            int[][] tmp = c.getState().clone();

            if (i < j) {
                int b=j;
                for (int y = i; y < j; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            } else {
                int b=i;
                for (int y = j; y < i; y++) {
                    tmp[b][x] = c.getState()[y][x];
                    b--;
                }
            }
            c.setState(tmp);
        }
    }


    public static void sortByScore(List<Chromosome> list){
        list.sort((o1, o2) -> {
            double i = o1.getScore() - o2.getScore();
            return (int) i;
        });
    }


    public static void flipMutate(List<Chromosome> iPopulation) {
        Chromosome chr;
        int point1, point2;
        int [][] state = new int[0][0];

        for (Chromosome chromosome : iPopulation) {
            chr = chromosome;
            for (int j = 0; j < 5; j++) {
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

    public static void boundaryMutation(List<Chromosome> selected){
        for (Chromosome c : selected) {
            //Maybe get the upper bound of 1% online
            for (int j = 0; j < 5; j++) {
                int x = new Random().nextInt(c.getState().length);
                int y = new Random().nextInt(c.getState()[0].length);
                boolean coin = new Random().nextBoolean();
                int[][] tmp = c.getState();
                if (coin) {
                    tmp[x][y] = 3;
                } else {
                    tmp[x][y] = 0;
                }
                c.setState(tmp);
            }
        }
    }

    public static List<Chromosome> twoPointVertical(List<Chromosome> iPopulation) {
        Chromosome c1,c2;
        List<Chromosome> fPopulation = new LinkedList<>();
        int point1, point2;
        int [][] init1, init2, fin1, fin2;

        fin1 = new int[30][14];
        fin2 = new int[30][14];

        for (int i=0; i<iPopulation.size()/2; i++ ){
            point1 = new Random().nextInt(14);
            point2 = new Random().nextInt(14 - point1) + point1;    //System.out.println(point1 + " " + point2);

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
            c1 = generateChromosome();
            c2 = generateChromosome();
            c1.setState(fin1);
            c2.setState(fin2);

            fPopulation.add(c1);
            fPopulation.add(c2);
        }

        return fPopulation;
    }


    public static List<Chromosome> twoPointHorizontal(List<Chromosome> selected){
        List<Chromosome> newPopulation = new ArrayList<>();

        for (int i=1; i<selected.size(); i=i+2){
            int rnd1 = new Random().nextInt(selected.get(i).getState().length/2);
            int rnd2 = new Random().nextInt(selected.get(i).getState().length/2)+selected.get(i).getState().length/2;

            int[][] temp4 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];
            int[][] temp40 = new int[selected.get(i).getState().length][selected.get(i).getState()[0].length];

            for(int j=0;j<selected.get(i).getState().length;j++){
                if(j<rnd1){
                    for (int x=0;x<selected.get(i).getState()[0].length;x++){
                        temp4[j][x]=selected.get(i).getState()[j][x];
                        temp40[j][x]=selected.get(i-1).getState()[j][x];
                    }
                }
                if(j>=rnd1 && j<rnd2){
                    for (int x=0;x<selected.get(i).getState()[0].length;x++){
                        temp4[j][x]=selected.get(i-1).getState()[j][x];
                        temp40[j][x]=selected.get(i).getState()[j][x];
                    }
                }
                if(j>=rnd2){
                    for (int x=0;x<selected.get(i).getState()[0].length;x++){
                        temp4[j][x]=selected.get(i).getState()[j][x];
                        temp40[j][x]=selected.get(i-1).getState()[j][x];
                    }
                }
                Chromosome c1 = new Chromosome(temp4,ChromosomeID++);
                Chromosome c2 = new Chromosome(temp40,ChromosomeID++);

                newPopulation.add(c1);
                newPopulation.add(c2);
            }
        }
        return newPopulation;
    }

    public static List<Chromosome> rouletteWheelSelection(List<Chromosome> iPopulation){
        int selectionSize = iPopulation.size()/selectionFactor;  //Keep half of the original

        double fitness;
        double[] probFitness = new double[iPopulation.size()];
        double[] sumFit = new double[iPopulation.size()];

        double score = iPopulation.get(0).getScore()/1000;  //TODO CHECK IF NEED
        sumFit[0] = 1/score;
        for (int i=1; i<iPopulation.size(); i++){
            score = iPopulation.get(i).getScore()/1000; //TODO CHECK IF NEED
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
                    default:
                        System.out.println(array[i][j]+" is not an option!");
                        return false;
                }

                switch (array[i][j+7]){     //CHECKING SECOND WEEK
                    case 0: break;
                    case 1: morningW2++; break;
                    case 2: afternoonW2++; break;
                    case 3: nightW2++; break;
                    default:
                        System.out.println(array[i][j+7]+" is not an option!");
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


    public static Chromosome generateChromosome(){
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
        Chromosome chr = new Chromosome(pop, ChromosomeID);
        ChromosomeID++;

        return chr;
    }

}

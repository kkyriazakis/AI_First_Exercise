public class Main {

    private static int[][]  hConst = {{10,10,5},{10,10,5},{5,10,5},{5,5,5},{5,10,5},{5,5,5},{5,5,5}};
    private static int ChromosomeID = 1;

    public static void main(String[] args) {

        Chromosome t = GenerateChromosome();
        boolean x = checkFeasibility(t);

        System.out.println( x );
    }

    public static boolean checkFeasibility(Chromosome chromosome){
        int[][] array = chromosome.getPopulation();
        //int[][] array = testArray;
        int morningW1, morningW2;
        int afternoonW1, afternoonW2;
        int nightW1, nightW2;

        for(int j=0; j<7; j++){
            morningW1=0; afternoonW1=0; nightW1=0;
            morningW2=0; afternoonW2=0; nightW2=0;
            for (int i=0; i<30; i++){
                switch (array[i][j]){
                    case 0:
                        break;
                    case 1:
                        morningW1++;
                        break;
                    case 2:
                        afternoonW1++;
                        break;
                    case 3:
                        nightW1++;
                        break;
                    default:
                        System.out.println(array[i][j]+" is not an option!");
                        return false;
                }
            }
            if(!(morningW1==hConst[j][0] && afternoonW1==hConst[j][1] && nightW1==hConst[j][2])){
                return false;
            }
            for (int i=0; i<30; i++){
                switch (array[i][j+7]){
                    case 0:
                        break;
                    case 1:
                        morningW2++;
                        break;
                    case 2:
                        afternoonW2++;
                        break;
                    case 3:
                        nightW2++;
                        break;
                    default:
                        System.out.println(array[i][j+7]+" is not an option!");
                        return false;
                }
            }
            if(!(morningW2==hConst[j][0] && afternoonW2==hConst[j][1] && nightW2==hConst[j][2])){
                return false;
            }
        }
        return true;
    }

    public static Chromosome GenerateChromosome(){
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

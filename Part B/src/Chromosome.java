public class Chromosome {
    private int id;
    private int[][] population;
    private int score;

    public Chromosome(int[][] population,int id) {
        this.population = population;
        this.score = 0;
        this.id = id;
    }

    public int getId() { return id; }

    public int[][] getPopulation() { return population; }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }

    public void updateScore(){
        int[] worker;
        int tot_hours, cont_days, cont_nightShifts, prevShift;
        int nShift,dShift,leave,weekends,prevOne,nextOne,cLeave;
        boolean isWeekend;

        for (int w=0; w<30; w++){    //FOR EACH WORKER
            worker = this.population[w];
            tot_hours = cont_days = cont_nightShifts = prevShift = 0;
            isWeekend=false;
            nShift=0; dShift=0; leave=0; weekends=0; prevOne=-1;nextOne=-1;cLeave=0;
            for (int d=0; d<14; d++) {   //FOR EACH DAY

                //UPDATE TOTAL HOURS
                switch (worker[d]){
                    case 1: tot_hours+=8; break;
                    case 2: tot_hours+=8; break;
                    case 3: tot_hours+=10;break;
                    case 0: break;
                }

                //UPDATE AND CHECK CONT DAYS
                if ( worker[d] == 0 ){
                    if( cont_days > 7 ){
                        this.score += 1000;
                    }
                    cont_days = 0;
                }
                else {
                    cont_days++;
                }

                //UPDATE AND CHECK CONT NIGHT SHIFTS
                if ( worker[d] != 3 ){
                    if( cont_nightShifts > 4 ){
                        this.score += 1000;
                    }
                    cont_nightShifts = 0;
                }
                else {
                    cont_nightShifts++;
                }

                //CHECK LAST SHIFT
                if (prevShift == 3){
                    if(worker[d] == 1){ //NIGHT->MORNING
                        this.score += 1000;
                    }
                    else if(worker[d] == 2){ //NIGHT->AFTERNOON
                        this.score += 800;
                    }
                }
                else if (prevShift == 2 && worker[d] == 1){ //AFTERNOON->MORNING
                    this.score += 800;
                }

                //UPDATE CURR SHIFT
                prevShift = worker[d];

                if(d>=1) prevOne=worker[d-1];
                if(d<=12) {
                    nextOne = worker[d + 1];
                }else{
                    nextOne=-1;
                }

                switch (worker[d]){
                    case 0:
                        cLeave++;
                        break;
                    case 1:
                        cLeave=0;
                        dShift++;
                        if(isWeekend) weekends++;
                        break;
                    case 2:
                        cLeave=0;
                        dShift++;
                        if(isWeekend) weekends++;
                        break;
                    case 3:
                        cLeave=0;
                        nShift++;
                        if(isWeekend) weekends++;
                        break;
                    default:
                }
                //Scoring
                if (nextOne != -1 && prevOne != -1){
                    if (worker[d]==0 && prevOne!=0 && nextOne!=0) score++;  // work-leave-work
                    if (worker[d]!=0 && prevOne==0 && nextOne==0) score++;  // leave-work-leave
                }
                if(cont_nightShifts == 4){      // At least 2 days of leave after 4 days of night shift in a row
                    try{
                        if(worker[d+1]!=0 || worker[d+2]!=0){
                            score = score+100;
                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        score = score+100;
                    }
                }
                if(cont_days == 7){    // At least 2 days of leave after 7 days of work in a row
                    try{
                        if(worker[d+1]!=0 || worker[d+2]!=0){
                            score = score+100;
                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        score = score+100;
                    }
                }


            }
            //CHECK TOTAL HOURS
            if(tot_hours > 70) {
                this.score += 1000;
            }

            //Check for score
            if(worker[5]!=0 || worker[6]!=0){
                if (worker[12]!=0 || worker[13]!=0) score++; // At most one weekend (sat OR sun) of work
            }

        }
    }

    private int[] getColumn(int col){
        int[][] x = this.population;
        int[] y = new int[x.length];
        for(int i=0; i<30; i++){
            y[i] = x[i][col];
        }
        return y;
    }
}

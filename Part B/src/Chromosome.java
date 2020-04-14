public class Chromosome {
    private int id;
    private int[][] state;
    private int score;

    public Chromosome(int[][] population,int id) {
        this.state = population;
        this.score = 0;
        this.id = id;
    }

    public int getId() { return id; }

    public int[][] getState() { return state; }

    public int getScore() { return score; }

    public void updateScore(){
        int[] worker;
        int tot_hours, cont_days, cont_nightShifts, prevShift;
        int nShift,dShift,leave,weekends,prevOne,nextOne,cLeave;
        boolean isWeekend, max_time, max_days, max_night, n2m , a2m, n2a, lwl, wlw, twoLafterfourN, twoLaftersevN, weekend_work ;

        max_time = max_days = max_night = n2m = a2m = n2a = lwl = wlw = twoLafterfourN = twoLaftersevN = weekend_work = false;

        for (int w=0; w<30; w++){    //FOR EACH WORKER
            worker = this.state[w];
            tot_hours = cont_days = cont_nightShifts = prevShift = 0;
            nShift = dShift = leave = weekends = cLeave = 0; prevOne = nextOne = -1;
            isWeekend = false;

            for (int d=0; d<14; d++) {   //FOR EACH DAY

                switch (worker[d]){ //UPDATE TOTAL HOURS
                    case 1:
                    case 2: tot_hours+=8; break;
                    case 3: tot_hours+=10;break;
                    case 0: break;
                }

                if ( worker[d] == 0 ){ //UPDATE AND CHECK CONT DAYS
                    if( cont_days > 7 ){
                        max_days = true;
                    }
                    cont_days = 0;
                }
                else
                    cont_days++;

                if ( worker[d] != 3 ){ //UPDATE AND CHECK CONT NIGHT SHIFTS
                    if( cont_nightShifts > 4 ){
                        max_night = true;
                    }
                    cont_nightShifts = 0;
                }
                else
                    cont_nightShifts++;


                if (prevShift == 3){ //CHECK LAST SHIFT
                    if(worker[d] == 1){ n2m = true; } //NIGHT->MORNING
                    else if(worker[d] == 2){ n2a = true; } //NIGHT->AFTERNOON
                }
                else if (prevShift == 2 && worker[d] == 1){ a2m = true; } //AFTERNOON->MORNING

                prevShift = worker[d]; //UPDATE CURR SHIFT

                if(d>=1) { prevOne=worker[d-1]; }
                if(d<=12) { nextOne = worker[d + 1]; }
                else{ nextOne=-1; }
                if(d==5||d==6||d==12||d==13) { isWeekend=true; }

                switch (worker[d]){
                    case 0: cLeave++; break;
                    case 1:
                    case 2:cLeave=0; dShift++;
                        if(isWeekend) { weekends++; }
                        break;
                    case 3: cLeave=0; nShift++;
                        if(isWeekend) { weekends++; }
                        break;
                }

                //Scoring
                if (nextOne != -1 && prevOne != -1){
                    if (worker[d]==0 && prevOne!=0 && nextOne!=0) { wlw = true; }  // work-leave-work
                    if (worker[d]!=0 && prevOne==0 && nextOne==0) { lwl = true; }  // leave-work-leave
                }

                if(cont_nightShifts == 4){      // At least 2 days of leave after 4 days of night shift in a row
                    try{
                        if(worker[d+1]!=0 || worker[d+2]!=0){ twoLafterfourN = true; }
                    }catch (ArrayIndexOutOfBoundsException e){ twoLafterfourN = true; }
                }
                if(cont_days == 7){    // At least 2 days of leave after 7 days of work in a row
                    try{
                        if(worker[d+1]!=0 || worker[d+2]!=0){ twoLaftersevN = true; }
                    }catch (ArrayIndexOutOfBoundsException e){ twoLaftersevN = true; }
                }
            }
            //CHECK TOTAL HOURS
            if(tot_hours > 70) { max_time = true; }

            //Check for score
            if(worker[5] != 0 || worker[6] != 0){ // At most one weekend (sat OR sun) of work
                if (worker[12]!=0 || worker[13]!=0) { weekend_work = true; }
            }
            if (this.score == 5803) { break; }  //ALL PENALTIES RECIEVED
        }
        if (max_time) { this.score += 1000; }
        if (max_days) { this.score += 1000; }
        if (max_night) { this.score += 1000; }
        if (n2m) { this.score += 1000; }
        if (a2m) { this.score += 800; }
        if (n2a) { this.score += 800; }
        if (wlw) { this.score ++; }
        if (lwl) { this.score ++; }
        if (weekend_work) { this.score ++; }
        if (twoLafterfourN) { this.score += 100; }
        if (twoLaftersevN) { this.score += 100; }

    }
}

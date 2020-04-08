import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Search {
    private Grid myGrid;

    Search(Grid myGrid){
        this.myGrid = myGrid;
    }

    public int coord(int i, int j){
        return (i*myGrid.getNumOfColumns())+j;
    }

    public int[] coord(int a){
        int[] toReturn=new int[2];
        toReturn[0]=a/myGrid.getNumOfColumns();
        toReturn[1]=a%myGrid.getNumOfColumns();
        //Returns [row,collumn]
        return toReturn;
    }

    public int[] BreadthFirstSearch(){
        LinkedList<Integer> explored = new LinkedList<Integer>(); //FIFO
        LinkedList<Node> fringe = new LinkedList<Node>();   //FIFO
        List<Integer> path = new ArrayList<>();

        boolean found = false;
        Node tmp = new Node(-1,null);
        Node state = new Node(myGrid.getStartidx(),tmp);
        fringe.addLast(state);

        while(!fringe.isEmpty()){
            tmp = fringe.pop();
            state = new Node(tmp.getId(),tmp.getParent());

            if (state.getId() == myGrid.getTerminalidx()){  //FOUND GOAL
                found = true;
                break;
            }
            explored.add(state.getId());

            int[] children = getNeighbors(state.getId());

            for(int i=0; i<children.length; i++){   //EXPLORE CHILDREN
                Node child = new Node(children[i],state);
                if( !explored.contains(child.getId()) && !fringe.contains(child)){
                    fringe.addLast(child);  //APPEND CURR CHILD
                }
            }
        }

        int[] toReturn;
        if(found){    //IF FOUND
            path.add(state.getId());
            while (state.getParent() != null){
                path.add(state.getParent().getId());
                state = state.getParent();
            }
            toReturn = path.stream().mapToInt(x -> x).toArray();
        }
        else{
            toReturn = new int[]{};
        }
        return toReturn;
    }




    public int[] getNeighbors(int state) {
        int i = coord(state)[0];  // row
        int j = coord(state)[1];  // collumn
        List<Integer> children = new ArrayList<>();

        if((i+1) >= 0 && (i+1) < myGrid.getNumOfRows()){ //Down
            if (!myGrid.getCell(i + 1, j).isWall()) {
                children.add(coord(i+1,j));
            }
        }
        if((i-1) >= 0 && (i-1) < myGrid.getNumOfRows()) { //Up
            if (!myGrid.getCell(i - 1, j).isWall()) {
                children.add(coord(i-1,j));
            }
        }
        if((j+1) >= 0 && (j+1) < myGrid.getNumOfColumns()) { //Right
            if (!myGrid.getCell(i, j + 1).isWall()) {
                children.add(coord(i,j+1));
            }
        }
        if((j-1) >= 0 && (j-1) < myGrid.getNumOfColumns()) { //Left
            if (!myGrid.getCell(i,j-1).isWall()) {
                children.add(coord(i,j-1));
            }
        }
        return children.stream().mapToInt(x -> x).toArray();
    }

}

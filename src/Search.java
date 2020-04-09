import java.util.*;
import java.lang.Math.*;

public class Search {

    private Grid myGrid;

    Search(Grid myGrid){
        this.myGrid = myGrid;
    }

    public int coord(int i, int j){return (i*myGrid.getNumOfColumns())+j;}

    public int[] coord(int a){
        int[] toReturn = new int[2];
        toReturn[0] = a / myGrid.getNumOfColumns();
        toReturn[1] = a % myGrid.getNumOfColumns();
        return toReturn; //Returns [i,j]
    }

    public boolean searchList(LinkedList<Node> list, int id){
        for (int i=0; i<list.size(); i++){
            if(list.get(i).getId()==id){
                return true;
            }
        }
        return false;
    }

    public void sortByCost(LinkedList<Node> list){
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getf() - o2.getf();
            }
        });
    }

    public double heuristic(Node n){
        int[] x = coord(n.getId());
        int[] y = coord(myGrid.getTerminalidx());

        return Math.sqrt( (x[0]-y[0])*(x[0]-y[0]) + (x[1]-y[1])*(x[1]-y[1]) );
    }

    public int[] dfs(){
        int i = myGrid.getStart()[0];
        int j = myGrid.getStart()[1];

        Boolean found = false;
        //Node node = new Node(myGrid.getStartidx(),null);
        List<Integer> path = new ArrayList<>();
        Node tmp   = new Node(-1,null);
        Node state = new Node(myGrid.getStartidx(),tmp);
        Cell s = myGrid.getCell(i,j);

        if (s.isTerminal()){
            System.out.println("First Cell Goal");
        }
        LinkedList<Node> stack = new LinkedList<Node>();

        //Node n = new Node(start,null);
        //n.setNeighbours(findNeighbours(start));
        //Stack<Integer> stack = new Stack<>();
        int start = coord(i,j);
        stack.add(state);
        LinkedList<Integer> checked = new LinkedList<Integer>();

        while(!stack.isEmpty()){
            tmp = stack.pop();
            state = new Node(tmp.getId(),tmp.getParent());

            if (state.getId() == myGrid.getTerminalidx()){  //FOUND GOAL
                System.out.println("Finish found");
                System.out.println("At: " + state.getId() + " -> (" + coord(state.getId())[0] + " , "+ coord(state.getId())[1] +")" );
                found = true;
                break;
            }
            checked.add(state.getId());

            int[] neighbors = getNeighbors(state.getId());
            //setNodes(newSquare,neighbors);
            System.out.println("I am expanding: " + state.getId() + " -> (" + coord(state.getId())[0] + " , "+ coord(state.getId())[1]+ " )" );
            System.out.println("this /n");
            for(int var=0; var<neighbors.length; var++) {
                Node neighbor = new Node(neighbors[var], state);
                System.out.println("Adding neighbor: " + neighbor.getId() + " -> (" + coord(neighbor.getId())[0] + " , "+ coord(neighbor.getId())[1] +")" );
                if( !((checked.contains(neighbor.getId())) || (searchList(stack,neighbor.getId()))) ){
                    System.out.println("Added" );
                    stack.push(neighbor);
                }else{
                    System.out.println("Not added");
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

    public int[] bfs(){
        LinkedList<Node> explored = new LinkedList<Node>(); //FIFO
        LinkedList<Node> fringe   = new LinkedList<Node>(); //FIFO
        List<Integer> path        = new ArrayList<>();

        boolean found = false;
        Node tmp   = new Node(-1,null);
        Node state = new Node(myGrid.getStartidx(),tmp);
        fringe.add(state);

        while( !fringe.isEmpty() ){
            tmp = fringe.pop();
            state = new Node(tmp.getId(),tmp.getParent());

            if (state.getId() == myGrid.getTerminalidx()){  //FOUND GOAL
                found = true;
                break;
            }
            explored.add(state);

            int[] children = getNeighbors(state.getId());
            for(int i=0; i<children.length; i++){   //EXPLORE CHILDREN
                Node child = new Node(children[i],state);
                if( !explored.contains(child) && !(searchList(fringe,child.getId())) ){
                    fringe.add(child);  //APPEND CURRENT CHILD
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
        int j = coord(state)[1];  // column
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
        return children.stream().mapToInt(x->x).toArray();
        //return children;
    }




}

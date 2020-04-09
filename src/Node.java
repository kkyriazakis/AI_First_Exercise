public class Node {
    public int id;
    public Node parent;
    private double g,h;

    public Node(int id, Node parent) {
        this.id = id;
        this.parent = parent;
        this.g = 0;
        this.h = 0;
    }

    public int getId(){ return id;}
    public void setId(int id){ this.id = id;}

    public void setg(double g){ this.g = g; }
    public void seth(double h){ this.h = h; }

    public double getg() {return g; }
    public double geth() {return h; }
    public double getf() {return g + h; }

    public Node getParent() {return parent; }
    public void setParent(Node parent) {this.parent = parent; }
}
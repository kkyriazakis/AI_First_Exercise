public class Node {
    public int id;
    public Node parent;
    private int g,h;

    public Node(int id, Node parent) {
        this.id = id;
        this.parent = parent;
    }

    public int getId(){ return id;}
    public void setId(int id){ this.id = id;}

    public void setg(int g){ this.g = g; }
    public void seth(int h){ this.h = h; }

    public int getg() {return g; }
    public int geth() {return h; }
    public int getf() {return g+h; }

    public Node getParent() {return parent; }
    public void setParent(Node parent) {this.parent = parent; }
}
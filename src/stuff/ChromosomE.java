package stuff;

import java.util.ArrayList;

public class ChromosomE{
    public ArrayList<String> genes;
    int fitness;
    ChromosomE(){
        genes = new ArrayList<>();
    }
    ChromosomE copy() {
        ChromosomE c = new ChromosomE();
        c.genes = new ArrayList<>(this.genes);
        c.fitness = this.fitness;
        return c;
    }
}
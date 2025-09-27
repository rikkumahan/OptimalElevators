package stuff;
import java.lang.*;
import java.util.*;

public class Gene {
    Random rand;
    int NF;
    int M;
    int size = 50, Generations = 100;
    double Pc = 0.7, Pm = 0.01;
    double Tf = 2, Ts = 4; //interfloor time , stop time.
    ArrayList<ChromosomE> population;
    public ArrayList<HallCall> calls;
    public ArrayList<Car> cars;

    public Gene(int NF, int M) {
        rand = new Random();
        this.NF = NF;
        this.M = M;
        population = new ArrayList<>(size);
        calls = new ArrayList<>();
        cars = new ArrayList<>();
    }

    public void Generate() {
        population.clear();
        for (int i = 0; i < size; i++) {
            ChromosomE chromo = new ChromosomE();
            for (int j=0; j<calls.size(); j++) {
                int r = rand.nextInt(M) + 1;
                String CarId = "car" + r;
                chromo.genes.add(CarId);
            }
            population.add(chromo);
        }
    }

    int calc_wait(Car CAR, HallCall HCi) {
        int time = 0, count = 0;
        String carDir = CAR.c_state.replaceAll("[^A-Za-z]", "");
        String hallDir = HCi.HC.replaceAll("[^A-Za-z]", "");
        int F = Integer.parseInt(CAR.c_state.replaceAll("[^0-9]", ""));
        int H = Integer.parseInt(HCi.HC.replaceAll("[^0-9]", ""));

        boolean feasible = false;
        if (carDir.equals("I")) feasible = true;
        else if (carDir.equals("U") && H >= F && hallDir.equals("U"))
            feasible = true;
        else if (carDir.equals("D") && H <= F && hallDir.equals("D"))
            feasible = true;

        if (!feasible) {
            return Integer.MAX_VALUE;
        }
        time += (int) (Math.abs(F - H) * Tf); //travel time.
        for (int stop : CAR.stops) {
            if (carDir.equals("U") && stop > F && stop < H) {
                count++;
            } else if (carDir.equals("D") && stop < F && stop > H) {
                count++;
                count++;
            }
        }
        time += (int) (count * Ts);
        time += (int) (System.currentTimeMillis() - HCi.requestTime); //(current - request)time.
        return time;
    }

    int Fitness_fnc(ChromosomE chromo, ArrayList<HallCall> hc, ArrayList<Car> cars) {
        int TWT = 0;
        if (chromo.genes.size() > hc.size()) return 9999;
        for (int j = 0; j < chromo.genes.size(); j++) {
            int i = Integer.parseInt(chromo.genes.get(j).replaceAll("[^0-9]", ""));
            if (i - 1 < 0 || i - 1 >= cars.size()) return 9999;
            Car c = cars.get(i - 1);
            HallCall h = hc.get(j);
            if (h == null) return 9999;
            TWT += calc_wait(c, h);
        }
        return TWT;
    }

    ChromosomE SelectParent() {
        Random rand = new Random();
        ChromosomE i = population.get(rand.nextInt(population.size()));
        ChromosomE j = population.get(rand.nextInt(population.size()));
        if (i.fitness > j.fitness){
            return j;
        }
        return i;
    }

    ChromosomE[] crossover(ChromosomE p1, ChromosomE p2) {
        int n = p1.genes.size();
        ChromosomE child1 = new ChromosomE();
        ChromosomE child2 = new ChromosomE();

        int cut = (int)(Math.random() * n);

        for (int i = 0; i < n; i++) {
            if (i < cut) {
                child1.genes.add(p1.genes.get(i));
                child2.genes.add(p2.genes.get(i));
            } else {
                child1.genes.add(p2.genes.get(i));
                child2.genes.add(p1.genes.get(i));
            }
        }

        return new ChromosomE[]{child1, child2};
    }

    ChromosomE Mutate(ChromosomE chromo){
        ChromosomE mutate = chromo.copy();
        for(int i=0;i<mutate.genes.size();i++){
            if(Math.random() < Pm){
                mutate.genes.set(i,"car"+(rand.nextInt(M)+1));
            }
        }
        return mutate;
    }

    ArrayList<ChromosomE> replace(ArrayList<ChromosomE> oldPop,ArrayList<ChromosomE> newPop){
        oldPop.sort(Comparator.comparingInt(c->c.fitness));
        newPop.sort(Comparator.comparingInt(c->c.fitness));

        ArrayList<ChromosomE> nextGeneration = new ArrayList<>();
        for (int i = 0; i < 2 && i < oldPop.size(); i++) {
            nextGeneration.add(oldPop.get(i));
        }
        int remaining = oldPop.size() - nextGeneration.size();
        for (int i = 0; i < remaining && i < newPop.size(); i++) {
            nextGeneration.add(newPop.get(i));
        }
        nextGeneration.sort(Comparator.comparingInt(c->c.fitness));
        return nextGeneration;
    }

}


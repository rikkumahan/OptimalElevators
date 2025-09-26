package stuff;

import java.lang.*;
import java.util.*;

public class GA extends Gene{

    public GA(int NF, int M) {
        super(NF, M);
    }
    public ChromosomE Genetic_Algo() {
        //Scanner sc = new Scanner(System.in);
        //int NF = sc.nextInt();
        //int M = sc.nextInt();
        //Gene g = new Gene(NF, M);

        //Initialize.
        Generate();

        //fitness.
        for (ChromosomE chromo : population) {
            chromo.fitness = Fitness_fnc(chromo,calls,cars);
        }

        ArrayList<ChromosomE> nextGen = new ArrayList<>();
        for (int i = 0; i < Generations; i++) {
            ArrayList<ChromosomE> new_population = new ArrayList<>();
            while (new_population.size() < population.size()) {
                ChromosomE p1 = SelectParent();
                ChromosomE p2 = SelectParent();

                ChromosomE c1,c2;
                //CROSSOVER.
                if (Math.random() < Pc) {
                    ChromosomE[] ch = crossover(p1, p2);
                    c1 = ch[0];
                    c2 = ch[1];
                } else {
                    c1 = p1.copy();
                    c2 = p2.copy();
                }
                //MUTATION.
                if (Math.random() < Pm) {
                    c1 = Mutate(c1);
                }
                if (Math.random() < Pm) {
                    c2 = Mutate(c2);
                }
                c1.fitness = Fitness_fnc(c1, calls, cars);
                c2.fitness = Fitness_fnc(c2, calls, cars);
                new_population.add(c1);
                new_population.add(c2);
            }
            nextGen = replace(population, new_population);
            population = nextGen;
        }
        return !nextGen.isEmpty() ? nextGen.get(0) : new ChromosomE();
    }

}

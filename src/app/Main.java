package app;
import stuff.*;

import java.lang.*;
import java.util.*;

public class Main {

    static ArrayList<Car> initcars(int M, int NF){
        ArrayList<Car> cars = new ArrayList<>();
        for(int i=1;i<=M;i++){
            cars.add(new Car("car"+i,(i-1)*(NF/M)+"I"));
        }
        return cars;
    }

    static volatile boolean running = true;

    static void updateCarPositions(List<Car> cars,ArrayList<HallCall> calls,int NF,long start) {
        long TIME_PER_FLOOR = 2000; // 2 sec travel per floor
        long STOP_DELAY = 4000;     // 4 sec wait at floor
        for (Car car : cars) {

            if (!car.stops.isEmpty()) {
                int targetFloor = car.stops.get(0);
                int currentFloor = Integer.parseInt(car.c_state.replaceAll("[^0-9]",""));
                String car_dir = car.c_state.replaceAll("[^A-Z]","");
                long t = System.currentTimeMillis();

                if (currentFloor < targetFloor){
                    car.c_state = currentFloor + "U";
                    if((t - car.time)>=TIME_PER_FLOOR){
                        currentFloor++;
                        car.time = t;
                        car.c_state = currentFloor + "U";

                    }
                }
                else if (currentFloor > targetFloor){
                    car.c_state = currentFloor + "D";
                    if((t-car.time)>=TIME_PER_FLOOR) {
                        currentFloor--;
                        car.time = t;
                        car.c_state = currentFloor + "D";

                    }
                }
                else {
                    if((t-car.time)>=STOP_DELAY) {
                        car.stops.remove(0);
                        car.time = t;
                        System.out.println("Elevator "+car.name+" reached "+targetFloor+" at "+(int)((t - start) / 1000)+" sec (passenger waited "+(int)(t-car.time)/1000+" sec)");
                        int randomTarget = (int)(Math.random() * NF); // say 15 floors
                        car.stops.add(randomTarget);
                        System.out.println("Passenger inside Elevator " + car.name + " pressed " + randomTarget);
                    }
                }
            }
            else{
                car.c_state = car.c_state.replaceAll("[A-Z]", "I");
            }
        }
    }



    static void applyAssignments(ChromosomE chrom, List<Car> cars, ArrayList<HallCall> calls) {
        int i = 0;
        for (HallCall call : calls) {
            if(i<chrom.genes.size()) {
                String carIdStr = chrom.genes.get(i);
                int carId = Integer.parseInt(carIdStr.replaceAll("[^0-9]", "")) - 1; // convert to index
                call.CarId = carIdStr;
                int floor = Integer.parseInt(call.HC.replaceAll("[^0-9]", ""));
                cars.get(carId).stops.add(floor);
                System.out.println(carIdStr + " assigned to floor " + floor);
                i++;
            }
        }


    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt(),NF=sc.nextInt();
        GA g = new GA(M,NF);
        g.cars = initcars(M,NF);
        long s_time = System.currentTimeMillis();
        Thread inputThread = new Thread(()->{
            while(running){
               if(sc.hasNextLine()){
                   String input = sc.nextLine().trim();
                   if(!input.isEmpty()) {
                       try {
                           HallCall hc = new HallCall(input, System.currentTimeMillis());
                           g.calls.add(hc);
                           long time = (System.currentTimeMillis() - s_time) / 1000;
                           System.out.println("Hall call received at floor " + input + " (time -> " + time + " s)");
                       }
                       catch(Exception e){
                           e.printStackTrace();
                       }
                   }
               }
           }
           sc.close();
        });
        inputThread.setDaemon(true);
        inputThread.start();
        while(running){
            if(g.calls != null && !g.calls.isEmpty()) {
                ChromosomE best = g.Genetic_Algo();
                applyAssignments(best, g.cars, g.calls);
                updateCarPositions(g.cars, g.calls, NF, s_time);
            }
            try {
                Thread.sleep(100); // Small delay to prevent excessive CPU usage
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }






}

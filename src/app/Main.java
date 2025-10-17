package app;
import stuff.*;
import java.lang.*;
import java.util.*;

public class Main {
    static int sum=0,cnt=0;
    static ArrayList<Car> initcars(int M, int NF){
        ArrayList<Car> cars = new ArrayList<>();
        for(int i=1;i<=M;i++){
            cars.add(new Car("car"+i,(i-1)*(NF/M)+"I"));
        }
        return cars;
    }
    static volatile boolean running = true;
    static void updateCarPositions(List<Car> cars,int NF,long start) {
        long TIME_PER_FLOOR = 1000; // 1 sec travel per floor
        long STOP_DELAY = 4000;     // 4 sec wait at floor
        for (Car car : cars) {
            if (!car.stops.isEmpty()) {
                int targetFloor = car.stops.get(0);
                int currentFloor = Integer.parseInt(car.c_state.replaceAll("[^0-9]",""));
                long t = System.currentTimeMillis();
                if (currentFloor < targetFloor){
                    if((t - car.time)>=TIME_PER_FLOOR){
                        currentFloor++;
                        car.time = t;
                        car.c_state = currentFloor+"U";
                    }
                }
                else if (currentFloor > targetFloor){
                    if((t-car.time)>=TIME_PER_FLOOR) {
                        currentFloor--;
                        car.time = t;
                        car.c_state = currentFloor + "D";

                    }
                }
                else {
                    if((t-car.time)>=STOP_DELAY) {
                        car.stops.removeFirst();
                        int wait = (int)((t-start)-car.req_times.get(0))/1000 ;
                        System.out.println("Elevator "+car.name+" reached "+targetFloor+" at "+(int)(t-start)/1000+" sec."+"[Waited"+" for "+wait+" sec]");
                        sum+=wait;
                        car.req_times.removeFirst();
                        car.time =t;
                        if(car.pick_call){
                            int randomTarget;
                            boolean isUp = car.call_dir.equalsIgnoreCase("U");
                            if(isUp){
                                if(targetFloor < NF-1){
                                    randomTarget = targetFloor + 1 + (int)(Math.random() * (NF - targetFloor - 1));
                                } else {
                                    randomTarget = NF-1;
                                }
                            } else {
                                if (targetFloor > 0) {
                                    randomTarget = (int) (Math.random() * targetFloor);
                                } else {
                                    randomTarget = 0;
                                }
                            }
                            car.stops.add(randomTarget);
                            car.req_times.add(System.currentTimeMillis() - start);
                            System.out.println("Passenger inside Elevator " + car.name + " pressed " + randomTarget);
                            cnt++;
                            car.pick_call = false;
                        }
                    }
                }
            }
            else{
                car.c_state = car.c_state.replaceAll("[A-Z]","I");
            }
        }
    }
    static void applyAssignments(ChromosomE chrom, List<Car> cars, ArrayList<HallCall> calls) {
        int i = 0;
        ArrayList<HallCall> callsToRemove = new ArrayList<>();
        for (HallCall call : calls) {
            if(i<chrom.genes.size()) {
                String carIdStr = chrom.genes.get(i);
                int carId = Integer.parseInt(carIdStr.replaceAll("[^0-9]", "")) - 1; // convert to index
                if(carId>=0 && carId < cars.size()){
                    call.CarId = carIdStr;
                    int floor = Integer.parseInt(call.HC.replaceAll("[^0-9]", ""));
                    cars.get(carId).stops.add(floor);
                    cars.get(carId).req_times.add(call.requestTime);
                    cars.get(carId).pick_call = true;
                    cars.get(carId).call_dir = call.HC.replaceAll("[a-zA-Z]","");
                    System.out.println(carIdStr + " assigned to floor " + floor);
                    cnt++;
                    callsToRemove.add(call);
                }
                i++;
            }
        }
        calls.removeAll(callsToRemove);
    }
    static void Status(ArrayList<Car> cars,int sum,int cnt){
        System.out.print("-----------------------------------------------------------\n"+
                         "|   Elevators   |        Stops      |    Current State    |\n"+
                "-----------------------------------------------------------\n");
        for(Car car : cars){
            if(car.stops != null && !car.stops.isEmpty()) {
                System.out.printf("|  %-12s |  %-16s |  %-18s |\n", car.name, String.join(", ",car.stops.stream().map(Object::toString).toList()), car.c_state);
            }
            else{
                System.out.printf("|  %-12s |  %-16s |  %-18s |\n", car.name,"none", car.c_state);
            }
        }
        System.out.print("-----------------------------------------------------------\n");
        System.out.printf("Average Waiting Time : %.2f sec\n\n",(float)sum/cnt);
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the no.of Floors : ");
        int NF = sc.nextInt();
        System.out.print("Enter the no.of Elevators : ");
        int M=sc.nextInt();
        System.out.println("Input Hall calls here in the format (6U / 6D).");
        GA g = new GA(NF,M);
        g.cars = initcars(M,NF);
        long s_time = System.currentTimeMillis();
        Thread inputThread = new Thread(()->{
            while(running){
               if(sc.hasNextLine()){
                   String input = sc.nextLine().trim();
                   if(!input.isEmpty()) {
                       try {
                           if(input.equalsIgnoreCase("status")){
                               /*for(Car car: g.cars){
                                   System.out.println("Current state of "+car.name+" is "+car.c_state);
                               }*/
                               Status(g.cars,sum,cnt);
                           }

                           else if(input.equalsIgnoreCase("exit"))running = false;
                           else {
                               long time = (System.currentTimeMillis() - s_time);//check
                               HallCall hc = new HallCall(input, time);
                               g.calls.add(hc);
                               System.out.println("Hall call received at floor " + input + " (time -> " + (int) time / 1000 + " s)");
                           }
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
            boolean hasActiveCalls = g.calls !=null && !g.calls.isEmpty();
            if(hasActiveCalls) {
                ChromosomE best = g.Genetic_Algo();
                if(!best.genes.isEmpty()){
                    applyAssignments(best,g.cars,g.calls);
                }
            }
            updateCarPositions(g.cars,NF,s_time);
            try{
                Thread.sleep(200);
            }
            catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

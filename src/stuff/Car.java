package stuff;

import java.util.ArrayList;
public class Car {
    public String name;//car1 , car2.
    public String c_state;// 6I,7U,7D
    public ArrayList<Integer> stops; // floors where it must stop
    public ArrayList<Long> req_times;
    public long time =0; //last stop time.
    public String call_dir;
    public boolean pick_call=false;
    public Car(String name, String c_state){
        this.name = name;
        this.c_state = c_state;
        this.stops = new ArrayList<>();
        this.req_times = new ArrayList<>();
    }
}

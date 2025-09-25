package stuff;

public class HallCall{
    public String HC; //6U,7D.
    public String CarId; // chosen by GA
    public long requestTime;   // timestamp when call was made
    public HallCall(String call, long requestTime){
        this.HC = call;
        this.requestTime = requestTime;
    }

}
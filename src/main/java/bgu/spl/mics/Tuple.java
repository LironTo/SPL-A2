package bgu.spl.mics;

public class Tuple<X, Y> {
    public X first;
    public Y second;

    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {return first;}
    public Y getSecond() {return second;}

    public void setFirst(X first) {this.first = first;}
    public void setSecond(Y second) {this.second = second;}

    
}
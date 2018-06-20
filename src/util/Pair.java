package util;

/**
 * 用于绑定两个对象的辅助类,被绑定的两个对象存在某种语义关系,因此成员first 和 second是有先序关系的两个对象
 * @param <E>
 * @param <F>
 */
public class Pair<E extends Object,F extends Object> {

    private E first;
    private F second;

    public Pair(E first,F second){
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public F getSecond() {
        return second;
    }

    public void setSecond(F second) {
        this.second = second;
    }

}

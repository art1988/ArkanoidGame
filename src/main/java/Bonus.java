import javax.swing.ImageIcon;

abstract class Bonus extends MovableElement {
    protected ImageIcon img;

    abstract public void execute();

    public void move() {
        y += 1;
    }
}
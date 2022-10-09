import java.awt.Color;
import java.util.Random;

public class Brick {
    private Color color;
    private Bonus bouns;
    private int type;

    private static Random rnd = new Random();

    Brick() {
        color = new Color((int)(Math.random()*255),
                          (int)(Math.random()*255),
                          (int)(Math.random()*255));
    }

    public void setBonus(Bonus bonus) {
        this.bouns = bonus;
    }

    public Bonus getBonus() {
        return (bouns != null) ? bouns : null;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
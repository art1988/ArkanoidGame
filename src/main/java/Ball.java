public class Ball extends MovableElement {
    private int radius;
    private int countOfKnock;
    private int speed;

    private boolean stickStatus;

    private double angleOfDirection;

    Ball() {
        setDx(2);
        setDy(5);
        setRadius(10); // min = 6  middle = 10  max = 14
        setSpeed(4);   // min = 4   max = 12
        placeOnStick(true);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int rad) {
        radius = rad;
    }

    public int getKnockCount() {
        return countOfKnock;
    }

    public void setDefaultKnockCount() {
        countOfKnock = 0;
    }

    public void incKnockCount() {
        countOfKnock++;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int sp) {
        speed = sp;
    }

    public boolean isAtStick() {
        return stickStatus ? true : false;
    }

    public void placeOnStick(boolean stat) {
        stickStatus = stat;
    }

    public void move() {
        angleOfDirection = Math.atan(dy/dx);
        
        if(dx > 0 && dy > 0) {
            x = x + (speed * Math.cos(angleOfDirection));
            y = y - (speed * Math.sin(angleOfDirection));
        }
        if(dx < 0 && dy > 0) {
            x = x - (speed * Math.cos(-angleOfDirection));
            y = y - (speed * Math.sin(-angleOfDirection));
        }
        if(dx < 0 && dy < 0) {
            x = x - (speed * Math.cos(angleOfDirection));
            y = y + (speed * Math.sin(angleOfDirection));
        }
        if(dx > 0 && dy < 0) {
            x = x + (speed * Math.cos(-angleOfDirection));
            y = y + (speed * Math.sin(-angleOfDirection));
        }
    }
}
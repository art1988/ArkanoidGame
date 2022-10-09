public class Cell {
    private boolean isBrick; 
    private Brick brick;    

    Cell() {
        setCellAsBrick(false);
    }

    public boolean hasBrick() {
        return isBrick;
    }

    public Brick getBrick() {
        return isBrick ? brick : null;
    }
    
    public void setCellAsBrick(boolean state) {
        isBrick = state;
        if(state == true) {
            brick = new Brick();
        } else {
            brick = null;
        }
    }
}
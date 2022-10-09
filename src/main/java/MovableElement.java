abstract class MovableElement {
   protected double x, y;
   protected double dx, dy;

   abstract public void move();

   public static void moveElement(MovableElement me) {
       me.move();
   }

   public double getX() {
       return x;
   }

   public void setX(double x) {
       this.x = x;
   }

   public double getY() {
       return y;
   }

   public void setY(double y) {
       this.y = y;
   }

   public double getDx() {
       return dx;
   }

   public void setDx(double dx) {
       this.dx = dx;
   }

   public double getDy() {
       return dy;
   }

   public void setDy(double dy) {
       this.dy = dy;
   }
}
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Arkanoid implements ArkanoidConstants {
    private static JFrame arkanoidWindow = new JFrame("Arkanoid");

    private static BufferedImage stickImg = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB),
                                 ballImg = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB),
                                 mapBricksImg = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);

    private static DrawerPanel panel = new DrawerPanel();

    private static int xStickCoord = BORDER_INDENTATION,
                       yStickCoord = IMG_HEIGHT - STICK_STATIC_HEIGHT - BORDER_INDENTATION;

    private static int STICK_DEFAULT_SIZE = 80;


    private static Graphics stickGr = stickImg.getGraphics(),
                            ballGr = ballImg.getGraphics(),
                            mapGr = mapBricksImg.getGraphics();

    static ArrayList bonuses = new ArrayList(1);

    private static Ball ball = new Ball();

    static ArrayList balls = new ArrayList(3);

    static java.util.Timer timer;

    private static Cell[][] arkanoidMap = new Cell[24][15];

    private static JLabel scoreShow,
                          liveShow,
                          levelShow;

    static int scoreQuantity = 0,
               liveQuantity = 3,
               levelQuantity = 1;

    private static JButton newGame, options, exit;

    private ArkanoidMouseMotion amm = new ArkanoidMouseMotion();

    private Map lev;

    private static int currentLevelId;

    private static int levelNum = 1;

    private void initArkanoidWindow() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        arkanoidWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        arkanoidWindow.setLocation((int)dim.getHeight() / 3, (int)dim.getWidth() / 6);
        panel.setBorder(new TitledBorder(""));
        arkanoidWindow.getContentPane().setLayout(new BorderLayout());
        arkanoidWindow.getContentPane().add(panel, BorderLayout.CENTER);
        arkanoidWindow.getContentPane().add(initInfoPanel(), BorderLayout.SOUTH);

        timer = new java.util.Timer();
        balls.add(ball);

        initArkanoidMap();

        panel.addMouseMotionListener(amm);
        panel.addMouseListener(new ArkanoidMouseClickListener());
        panel.addMouseMotionListener(new ArkanoidMouseMotionListener());

        arkanoidWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        arkanoidWindow.setResizable(false);
        arkanoidWindow.setVisible(true);
    }

    private void initArkanoidMap() {
        for(int y = 0; y < 24; y++)
            for(int x = 0; x < 15; x++)
                arkanoidMap[y][x] = new Cell();
    }

    private static boolean checkLevelForEnd() {
        for(int y = 0; y < 24; y++)
            for(int x = 0; x < 15; x++)
                if(arkanoidMap[y][x].hasBrick()) return false;
        
        return true;
    }

    private void drawLevelName() {
        String name = String.valueOf(lev.get(new Integer(levelNum)));

        Font font = new Font("SansSerif", Font.PLAIN, 50);

        stickGr.setColor(Color.WHITE);
        stickGr.setFont(font);

        stickGr.drawString(name, IMG_WIDTH / 3, IMG_HEIGHT / 2);
    }

    private JPanel initInfoPanel() {
        JPanel infoPanel = new JPanel();
        BoxLayout box = new BoxLayout(infoPanel, BoxLayout.X_AXIS);
        infoPanel.setLayout(box);

        infoPanel.setBorder(new TitledBorder(""));

        JLabel score = new JLabel("Score : "),
               lives = new JLabel("Lives : "),
               level = new JLabel("Level : ");

        scoreShow = new JLabel(String.valueOf(scoreQuantity));
        liveShow = new JLabel(String.valueOf(liveQuantity));
        levelShow = new JLabel(String.valueOf(levelQuantity));

        JPanel firstColumn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)),
               secondColumn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)),
               thirdRowColumn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        firstColumn.add(score);
        firstColumn.add(scoreShow);

        secondColumn.add(lives);
        secondColumn.add(liveShow);

        thirdRowColumn.add(level);
        thirdRowColumn.add(levelShow);

        infoPanel.add(firstColumn);
        infoPanel.add(secondColumn);
        infoPanel.add(thirdRowColumn);

        infoPanel.add(Box.createHorizontalStrut(170));

        infoPanel.add(newGame = new JButton("New game"));
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(options = new JButton("Options"));
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(exit = new JButton("Exit"));
        exit.addActionListener(new ArkanoidButtonListener());

        return infoPanel;
    }

    static void updateInfo() {
        scoreShow.setText(String.valueOf(scoreQuantity));
        liveShow.setText(String.valueOf(liveQuantity));
        levelShow.setText(String.valueOf(levelQuantity));
    }

    private void initRandomBonus() {
        Random r = new Random();
        byte count = 0;

        while(count != 10) {
            int y = r.nextInt(4) + 2,
                x = r.nextInt(12) + 1;
            if(arkanoidMap[y][x].hasBrick() == true) {
                switch((int)(Math.random()*9)) {
                    case 0: arkanoidMap[y][x].getBrick().setBonus(new AddLife()); break;
                    case 1: arkanoidMap[y][x].getBrick().setBonus(new ReduceLife()); break;
                    case 2: arkanoidMap[y][x].getBrick().setBonus(new IncreaseRadius()); break;
                    case 3: arkanoidMap[y][x].getBrick().setBonus(new AddBall()); break;
                    case 4: arkanoidMap[y][x].getBrick().setBonus(new WorsenRadius()); break;
                    case 5: arkanoidMap[y][x].getBrick().setBonus(new SetSlowSpeed()); break;
                    case 6: arkanoidMap[y][x].getBrick().setBonus(new SetPromptSpeed()); break;
                    case 7: arkanoidMap[y][x].getBrick().setBonus(new IncreaseStick()); break;
                    case 8: arkanoidMap[y][x].getBrick().setBonus(new WorsenStick()); break;
                }
                count++;
            } else {
                continue;
            }
        }
    }

    private static void drawArkanoidMap() {
        for(int yMark = 0; yMark < (CELL_HEIGHT * 24); yMark += CELL_HEIGHT) {
            for(int xMark = 0; xMark < (CELL_WIDTH * 15); xMark += CELL_WIDTH) {

                int xCor = xMark / CELL_WIDTH,
                    yCor = yMark / CELL_HEIGHT;

                if(arkanoidMap[yCor][xCor].hasBrick() == true) {
                    mapGr.setColor(arkanoidMap[yCor][xCor].getBrick().getColor());
                    mapGr.fillRect(xMark, yMark, CELL_WIDTH, CELL_HEIGHT);
                }
            }
        }
    }

    private Arkanoid() {
        initArkanoidWindow();
        loadLevels();
        loadBricks();
        drawArkanoidMap();
        //drawLevelName();
        panel.repaint();
    }

    private void loadLevels() {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(levelsXMLFile);

            doc.getDocumentElement().normalize();

            NodeList listOfLevels = doc.getElementsByTagName("level");
            lev = new HashMap(listOfLevels.getLength());

            for(int i = 0; i < listOfLevels.getLength(); i++) {
                Node level = listOfLevels.item(i);
                NamedNodeMap attributes = level.getAttributes();

                Node id   = attributes.getNamedItem("id"),
                     name = attributes.getNamedItem("name");

                lev.put(new Integer(id.getNodeValue()), name.getNodeValue());
            }

            currentLevelId = 1;
        } catch(Exception e) {}
    }

    private static void loadBricks() {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(levelsXMLFile);

            NodeList listOfBrick = doc.getElementsByTagName("brick");

            for(int i = 0; i < listOfBrick.getLength(); i++) {
                Node brick = listOfBrick.item(i);

                Node n = brick.getParentNode().getAttributes().getNamedItem("id");

                if(brick.getNodeType() == Node.ELEMENT_NODE && n.getNodeValue().equals(String.valueOf(currentLevelId))) {
                    NamedNodeMap attributes = brick.getAttributes();
                    Node y     = attributes.getNamedItem("y"),
                         x     = attributes.getNamedItem("x"),
                         // TODO:
                         color = attributes.getNamedItem("color"),
                         type  = attributes.getNamedItem("type");

                    arkanoidMap[Integer.parseInt(y.getNodeValue())][Integer.parseInt(x.getNodeValue())].setCellAsBrick(true);
                }
            }
        } catch(Exception e) {}
        currentLevelId++;
    }

    static class DrawerPanel extends JComponent {
        public void paintComponent(Graphics g) {
            updateImage();

            drawStick();

            drawBall();

            drawArkanoidMap();

            drawFallingBonus();

            if(liveQuantity == 0) {
               Font font = new Font("SansSerif", Font.PLAIN, 50);

               Kernel blurKernel = new Kernel(3, 3, new float[] {
                                              0.1111f, 0.1111f, 0.1111f,
                                              0.1111f, 0.1111f, 0.1111f,
                                              0.1111f, 0.1111f, 0.1111f,
                                              });

               ConvolveOp convolveOp = new ConvolveOp(blurKernel, ConvolveOp.EDGE_NO_OP, null);

               BufferedImage output = new BufferedImage(IMG_WIDTH, IMG_HEIGHT,
                                                        BufferedImage.TYPE_INT_RGB);

               convolveOp.filter(mapBricksImg, output);

               Graphics gr = output.getGraphics();

               gr.setColor(Color.WHITE);
               gr.setFont(font);

               gr.drawString("Game over", IMG_WIDTH / 3, IMG_HEIGHT / 2);
               g.drawImage(output, 0, 0, null);
               return;
            }

            g.drawImage(mapBricksImg, 0,0, null);
        }
    }

    class BallMotion extends TimerTask {
        public void run() {
            for(int i = 0; i < balls.size(); i++) {
                ball = (Ball) balls.get(i);
                checkBallBorderLines();
                checkBrickKnock();
                updateInfo();
                MovableElement.moveElement(ball);
                catchBonus();
                panel.repaint();
            }
        }
    }

    private static void updateImage() {
        stickImg = ballImg;
        mapBricksImg = stickImg;

        stickGr = ballGr;
        mapGr = stickGr;
    }

    private static void increaseSpeed() {
        ball.incKnockCount();
        if((ball.getKnockCount() % 10) == 0) {
            ball.setSpeed(ball.getSpeed() + 1);
        }
    }

    private static void catchBonus() {
        for(int i = 0; i < bonuses.size(); i++) {
            Bonus bonus = (Bonus) bonuses.get(i);

            int by      = (int) bonus.getY() + bonus.img.getIconHeight(),
                bxLeft  = (int) bonus.getX() - (bonus.img.getIconWidth() / 2),
                bxRight = (int) bonus.getX() + (bonus.img.getIconWidth() / 2);

            if(by >= yStickCoord && bxRight >= xStickCoord &&
               bxLeft <= xStickCoord + STICK_DEFAULT_SIZE) {
                bonus.execute();
                bonuses.remove(bonus);
            }
            if(bonus.getY() > IMG_HEIGHT - STICK_STATIC_HEIGHT) {
                bonuses.remove(bonus);
            }
        }
        bonuses.trimToSize();
    }

    class AddLife extends Bonus {
        AddLife() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusLife+.GIF");
        }

        public void execute() {
            liveQuantity++;
        }
    }

    class ReduceLife extends Bonus {
        ReduceLife() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusLife-.GIF");
        }

        public void execute() {
            liveQuantity--;
            updateInfo();
            newRound();
        }

        public void move() {
            y += 2;
        }
    }

    class IncreaseRadius extends Bonus {
        IncreaseRadius() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusRad+.GIF");
        }

        public void execute() {
            Iterator iter = balls.iterator();
            while(iter.hasNext()) {
                Ball b = (Ball) iter.next();
                if(b.getRadius() == 6) { b.setRadius(10); continue; }
                if(b.getRadius() == 10) { b.setRadius(14); continue; }
            }
        }
    }

    class WorsenRadius extends Bonus {
        WorsenRadius() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusRad-.GIF");
        }

        public void execute() {
            Iterator iter = balls.iterator();
            while(iter.hasNext()) {
                Ball b = (Ball) iter.next();
                if(b.getRadius() == 14) { b.setRadius(10); continue; }
                if(b.getRadius() == 10) { b.setRadius(6); continue; }
            }
        }
    }

    class AddBall extends Bonus {
        AddBall() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusX2.GIF");
        }

        public void execute() {
            Ball newBall = new Ball();
            newBall.setX(xStickCoord + STICK_DEFAULT_SIZE / 2);
            newBall.setY(yStickCoord - STICK_STATIC_HEIGHT);
            newBall.placeOnStick(false);
            balls.add(newBall);
        }
    }

    class SetSlowSpeed extends Bonus {
        SetSlowSpeed() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusSpeed-.GIF");
        }

        public void execute() {
            Iterator iter = balls.iterator();
            while(iter.hasNext()) {
                Ball b = (Ball) iter.next();
                b.setSpeed(4);
                b.setDefaultKnockCount();
            }
        }
    }

    class SetPromptSpeed extends Bonus {
        SetPromptSpeed() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusSpeed+.GIF");
        }

        public void execute() {
            Iterator iter = balls.iterator();
            while(iter.hasNext()) {
                Ball b = (Ball) iter.next();
                b.setSpeed(b.getSpeed() + 2);
            }
        }
    }

    class IncreaseStick extends Bonus {
        IncreaseStick() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusStick+.GIF");
        }

        public void execute() {
            if(STICK_DEFAULT_SIZE < MAX_STICK_SIZE) STICK_DEFAULT_SIZE += 20;
        }
    }

    class WorsenStick extends Bonus {
        WorsenStick() {
            img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/bonusStick-.GIF");
        }

        public void execute() {
            if(STICK_DEFAULT_SIZE > MIN_STICK_SIZE) STICK_DEFAULT_SIZE -= 20;
        }
    }

    private static void checkStickKnock() {
        int xBeginLeft = xStickCoord,
            xEndLeft = (2 * xStickCoord + STICK_DEFAULT_SIZE) / 2,
            xBeginRight = xEndLeft,
            xEndRight = xStickCoord + STICK_DEFAULT_SIZE;

        if(ball.getX() + ball.getRadius() >= xBeginLeft && ball.getX() <= xEndLeft) {
            double newDx = (xEndLeft - ball.getX()) / 10;

            ball.setDy(-ball.getDy());
            ball.setDx(-newDx);

            return;
        }

        if(ball.getX() >= xBeginRight && ball.getX() <= xEndRight) {
            double newDx = -(xBeginRight - ball.getX()) / 10;

            ball.setDy(-ball.getDy());
            ball.setDx(newDx);

            return;
        }
    }

    private static void newRound() {
        balls.clear();
        ball = new Ball();
        balls.add(ball);

        ball.setX(xStickCoord + (STICK_DEFAULT_SIZE / 2) - BORDER_INDENTATION);
        ball.setY(yStickCoord - ball.getRadius() - 3);

        bonuses.clear();

        STICK_DEFAULT_SIZE = 80;
        timer.cancel();
        timer = new java.util.Timer();
    }

    private static void checkBallBorderLines() {
        // нА ОПЮБШИ АНПДЧП
        if(ball.getX() + ball.getRadius() > IMG_WIDTH) ball.setDx(-ball.getDx());
        // нА БЕПУМХИ АНПДЧП
        if(ball.getY() < 0) ball.setDy(-ball.getDy());
        // нА КЕБШИ АНПДЧП
        if(ball.getX() < 0) ball.setDx(-ball.getDx());

        // нА ОЮКНВЙС
        if( (ball.getX() + ball.getRadius() >= xStickCoord) &&
            (ball.getX() <= xStickCoord + STICK_DEFAULT_SIZE) &&
            (ball.getY() + ball.getRadius() > yStickCoord) ) {
            checkStickKnock();
            increaseSpeed();
        } else if(ball.getY() + ball.getRadius() > yStickCoord) { // ХМЮВЕ ЛЪВ ОНРЕПЪМ
            if(balls.size() == 1) { // еЯКХ БЯЕЦН ЛЪВЕИ АШКН 1, РН ЛХМСЯ ФХГМЭ
                liveQuantity--;

                if(liveQuantity == 0) {
                    timer.cancel();
                    return;
                }
                newRound();
            }
            if(balls.size() > 1) { // ХМЮВЕ САХПЮЕЛ РЕЙСЫХИ ЛЪВ
                balls.remove(ball);
                balls.trimToSize();
            }
        }
    }

    private static void parseBonusBrick(int yCoord, int xCoord) {
        Bonus bonus;
        if(arkanoidMap[yCoord][xCoord].getBrick() != null) {
            bonus = arkanoidMap[yCoord][xCoord].getBrick().getBonus();
            if(bonus != null) {
                bonus.setX(xCoord * CELL_WIDTH);
                bonus.setY(yCoord * CELL_HEIGHT);
                bonuses.add(bonus);
            }
        }
    }

    private static void drawFallingBonus() {
        for(int i = 0; i < bonuses.size(); i++) {
            Bonus b = (Bonus) bonuses.get(i);

            mapGr.drawImage(b.img.getImage(), (int) b.getX(), (int) b.getY(), null, null);
            MovableElement.moveElement(b);
            panel.repaint();
        }
    }

    // оПНБЕПЙЮ МЮ СДЮП НА АКНЙ
    private static void checkBrickKnock() {
        if(ball.getX() > IMG_WIDTH - CELL_WIDTH) return;
        if((ball.getY() + ball.getRadius() > IMG_HEIGHT - STICK_STATIC_HEIGHT - 5) ||
           (ball.getY() + ball.getRadius() > IMG_HEIGHT - CELL_HEIGHT - STICK_STATIC_HEIGHT)) return;
        if(ball.getY() - ball.getRadius() < 0) return;

        // localXMiddle localYMiddle - ЙННПДХМЮРШ РНВЙХ (БЕПЬХМШ ЙБЮДПЮРЮ,
        // НОХЯЮММНЦН БНЙПСЦ ЛЪВЮ). б ГЮБХЯХЛНЯРХ НР dx Х dy НМЮ ПЮГМЮЪ:
        // БЕПУМЪЪ ОПЮБЮЪ БЕПЬХМЮ (СЦНК ЩРНЦН ЙБЮДПЮРЮ), БЕПУМЪЪ КЕБЮЪ БЕПЬХМЮ,
        // МХФМЪЪ КЕБЮЪ БЕПЬХМЮ Х МХФМЪЪ ОПЮБЮЪ БЕПЬХМЮ ЙБЮДПЮРЮ.

        // localXLeft localYLeft - БЕПЬХМЮ РЮ ВРН ЯКЕБЮ Б ГЮБХЯХЛНЯРХ НР dx Х dy

        // localXRight localYRight - БЕПЬХМЮ РЮ ВРН ЯОПЮБЮ Б ГЮБХЯХЛНЯРХ НР dx Х dy

        if(checkLevelForEnd()) {
            newRound();
            loadBricks();
            return;
        }

        if(ball.getDx() > 0 && ball.getDy() > 0) {
            int localXMiddle = (int) (ball.getX() + ball.getRadius()),
                localYMiddle = (int) (ball.getY() - ball.getRadius()),

                localXLeft = (int) (ball.getX() - ball.getRadius()),
                localYLeft = (int) (ball.getY() - ball.getRadius()),

                localXRight = (int) (ball.getX() + ball.getRadius()),
                localYRight = (int) (ball.getY() + ball.getRadius());

            if((arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                arkanoidMap[(localYMiddle + ball.getRadius()) / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == false) ||
               (arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == true &&
                arkanoidMap[(localYLeft + ball.getRadius()) / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == false)) {

                ball.setDy(-ball.getDy());

                parseBonusBrick(localYLeft / CELL_HEIGHT, localXLeft / CELL_WIDTH);
                arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].setCellAsBrick(false);
                parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);

                scoreQuantity += 20;
                return;
            }

            if((arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                arkanoidMap[(localYMiddle + ball.getRadius()) / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == false) ||
               (arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == true &&
                arkanoidMap[localYRight / CELL_HEIGHT][(localXRight - ball.getRadius()) / CELL_WIDTH].hasBrick() == false)) {

                ball.setDx(-ball.getDx());

                parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                parseBonusBrick(localYRight / CELL_HEIGHT,localXRight / CELL_WIDTH);
                arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].setCellAsBrick(false);

                scoreQuantity += 20;
                return;
            }
        }

        if(ball.getDx() < 0 && ball.getDy() > 0) {
            int localXMiddle = (int) (ball.getX() - ball.getRadius()),
                localYMiddle = (int) (ball.getY() - ball.getRadius()),

                localXRight = (int) (ball.getX() + ball.getRadius()),
                localYRight = (int) (ball.getY() - ball.getRadius()),

                localXLeft = (int) (ball.getX() - ball.getRadius()),
                localYLeft = (int) (ball.getY() + ball.getRadius());

              if( (arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYMiddle + ball.getRadius()) / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == false) ||
                  (arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYRight + ball.getRadius()) / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == false)) {

                  ball.setDy(-ball.getDy());

                  parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                  arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                  parseBonusBrick(localYRight / CELL_HEIGHT, localXRight / CELL_WIDTH);
                  arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].setCellAsBrick(false);

                  scoreQuantity += 20;
                  return;
              }

              if((arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                  arkanoidMap[localYMiddle / CELL_HEIGHT][(localXMiddle + ball.getRadius()) / CELL_WIDTH].hasBrick() == false) ||
                  arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == true &&
                  arkanoidMap[localYLeft / CELL_HEIGHT][(localXLeft + ball.getRadius()) / CELL_WIDTH].hasBrick() == false) {

                  ball.setDx(-ball.getDx());

                  parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                  arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                  parseBonusBrick(localYLeft / CELL_HEIGHT, localXLeft / CELL_WIDTH);
                  arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].setCellAsBrick(false);

                  scoreQuantity += 20;
                  return;
               }
        }

        if(ball.getDx() < 0 && ball.getDy() < 0) {
            int localXMiddle = (int) (ball.getX() - ball.getRadius()),
                localYMiddle = (int) (ball.getY() + ball.getRadius()),

                localXLeft = (int) (ball.getX() - ball.getRadius()),
                localYLeft = (int) (ball.getY() - ball.getRadius()),

                localXRight = (int) (ball.getX() + ball.getRadius()),
                localYRight = (int) (ball.getY() + ball.getRadius());

// РСР КЮЦЮЕР, ОНЯЛНРПЕРЭ МЮДН
             if(  (arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYMiddle - ball.getRadius()) / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == false) ||
                   arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYRight - ball.getRadius()) / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == false) {

                   ball.setDy(-ball.getDy());

                   parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                   arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                   parseBonusBrick(localYRight / CELL_HEIGHT, localXRight / CELL_WIDTH);
                   arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].setCellAsBrick(false);

                   scoreQuantity += 20;
                   return;
              }

              if( (arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[localYMiddle / CELL_HEIGHT][(localXMiddle + ball.getRadius()) / CELL_WIDTH].hasBrick() == false) ||
                   arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[localYLeft / CELL_HEIGHT][(localXLeft + ball.getRadius()) / CELL_WIDTH].hasBrick() == false) {

                   ball.setDx(-ball.getDx());

                   parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                   arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                   parseBonusBrick(localYLeft / CELL_HEIGHT, localXLeft / CELL_WIDTH);
                   arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].setCellAsBrick(false);

                   scoreQuantity += 20;
                   return;
               }
        }

        if(ball.getDx() > 0 && ball.getDy() < 0) {
            int localXMiddle = (int) (ball.getX() + ball.getRadius()),
                localYMiddle = (int) (ball.getY() + ball.getRadius()),

                localXLeft = (int) (ball.getX() - ball.getRadius()),
                localYLeft = (int) (ball.getY() + ball.getRadius()),

                localXRight = (int) (ball.getX() + ball.getRadius()),
                localYRight = (int) (ball.getY() - ball.getRadius());

             if(  (arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYMiddle - ball.getRadius()) / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == false) ||
                   arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == true &&
                   arkanoidMap[(localYLeft - ball.getRadius()) / CELL_HEIGHT][localXLeft / CELL_WIDTH].hasBrick() == false ) {

                  ball.setDy(-ball.getDy());

                  parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                  arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                  parseBonusBrick(localYLeft / CELL_HEIGHT, localXLeft / CELL_WIDTH);
                  arkanoidMap[localYLeft / CELL_HEIGHT][localXLeft / CELL_WIDTH].setCellAsBrick(false);

                  scoreQuantity += 20;
                  return;
              }

              if(  (arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].hasBrick() == true &&
                    arkanoidMap[localYMiddle / CELL_HEIGHT][(localXMiddle - ball.getRadius()) / CELL_WIDTH].hasBrick() == false) ||
                    arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].hasBrick() == true &&
                    arkanoidMap[localYRight / CELL_HEIGHT][(localXRight - ball.getRadius()) / CELL_WIDTH].hasBrick() == false) {

                   ball.setDx(-ball.getDx());

                   parseBonusBrick(localYMiddle / CELL_HEIGHT, localXMiddle / CELL_WIDTH);
                   arkanoidMap[localYMiddle / CELL_HEIGHT][localXMiddle / CELL_WIDTH].setCellAsBrick(false);
                   parseBonusBrick(localYRight / CELL_HEIGHT, localXRight / CELL_WIDTH);
                   arkanoidMap[localYRight / CELL_HEIGHT][localXRight / CELL_WIDTH].setCellAsBrick(false);

                   scoreQuantity += 20;
                   return;
               }
        }
    }

    private static void updateBallImg() {
        ballGr.setColor(Color.BLACK);
        ballGr.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT - STICK_STATIC_HEIGHT - BORDER_INDENTATION);
    }

    private static void drawBall() {
        updateBallImg();

        for(int i = 0; i < balls.size(); i++) {
            ball = (Ball) balls.get(i);

            ballGr.setColor(Color.WHITE);
            ballGr.fillOval((int) ball.getX(), (int) ball.getY(), ball.getRadius(), ball.getRadius());
        }
    }

    private static void drawStick() {
        stickGr.setColor(Color.BLACK);
        stickGr.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);

        stickGr.setColor(Color.WHITE);

        stickGr.fillRect(xStickCoord, yStickCoord, STICK_DEFAULT_SIZE, STICK_STATIC_HEIGHT);
    }

    class ArkanoidMouseMotion extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            xStickCoord = e.getX() - (STICK_DEFAULT_SIZE / 2);
            if(xStickCoord < BORDER_INDENTATION || (xStickCoord + STICK_DEFAULT_SIZE > (IMG_WIDTH - BORDER_INDENTATION))) {
                return;
            }

            if(ball.isAtStick()) {
                ball.setX(xStickCoord + (STICK_DEFAULT_SIZE / 2) - BORDER_INDENTATION);
                ball.setY(yStickCoord - ball.getRadius() - 3);
            }

            panel.repaint();
        }
    }

    class ArkanoidMouseClickListener extends MouseAdapter {
        private boolean flag = false;

        public void mousePressed(MouseEvent e) {
            for(int i = 0; i < balls.size(); i++) {
                ball = (Ball) balls.get(i);
                if(!ball.isAtStick()) continue;
                ball.placeOnStick(false);
                flag = true;
            }
            if(flag) {
                timer.schedule(new BallMotion(), 0, 10);
                flag = false;
            }
        }
    }

    class ArkanoidMouseMotionListener extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent e) {
            amm.mouseMoved(e);
        }
    }

    class ArkanoidButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == exit) System.exit(0);
        }
    }

    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {}
        new Arkanoid();
    }
}
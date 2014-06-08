import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import sun.audio.*;

class Tetris extends javax.swing.JPanel 
implements java.awt.event.KeyListener
{
	
	Font font = new Font("Tahoma", Font.BOLD,16);
	Font fontTwo = new Font("Impact", Font.PLAIN,20);
	Font fontThree = new Font("Impact", Font.PLAIN, 35);
	
	/*   
	 *  occupied matrix holds 1s and 0s for every cell of the play field
	 *  1s for filled cells and 0s for empty cells
	 */
	int[][] occupied = new int[10][20];
	
	/*
	 * colorsCoords holds numbers from 0 to 6 representing the 
	 * color that should be filled in the current cell
	 */
	int[][] colorsCoords = new int[10][20];
	
	/*
	 *  In those arrays are hold the border and inner colors
	 *  of all diffrent tokens, every token have distinct color
	 */
	Color[] tokenBorderColors = {
		new Color(2,88,108),  // I
		new Color(1,36,118),    // J
		new Color(153,51,0),  // L
		new Color(153,102,0),  // O
		new Color(2,92,1),    // S
  		new Color(102,0,102),  // T  		
  		new Color(158,12,41)};   // Z  		  		
	Color[] tokenInnerColors = {
		    new Color(0,210,240),  // I
			new Color(71,134,226),    // J
			new Color(255,100,0),  // L
			new Color(255,182,24),  // O
			new Color(0,216,0),    // S
			new Color(197,41,166),  // T  		
	  		new Color(247,32,57)};   // Z  	
  
    // [seven tokens] [ four rotations ] [ four cells]
	/*
	 * The following 3 dimensional arrays hold all beginning coordinates
	 *  for every token and all of its rotations. Every token takes 
	 *  exactly 4 cells.
	 */
    static int[][][] xRotationArray = {
       //rotation 0, roatain 1, rotattion 2, rotation 3
       { {-1,0,1,2}, {1,1,1,1}, {-1,0,1,2}, {0,0,0,0} },    // I
       { {-1,-1,0,1}, {0,1,0,0}, {-1,0,1,1}, {0,0,-1,0} },  // J       
       { {1,-1,0,1}, {0,0,0,1}, {-1,0,1,-1}, {-1,0,0,0} },  // L
       { {0,1,0,1}, {0,1,0,1}, {0,1,0,1}, {0,1,0,1} }, 	    // O
       { {0,1,-1,0}, {0,0,1,1}, {0,1,-1,0}, {-1,-1,0,0} },  // S
       { {0,-1,0,1}, {0,0,1,0}, {-1,0,1,0}, {0,-1,0,0} },   // T	       
       { {-1,0,0,1}, {1,0,1,0}, {-1,0,0,1}, {0,-1,0,-1} },  // Z
       
    };
    static int[][][] yRotationArray = {
       { {0,0,0,0}, {-1,0,1,2}, {1,1,1,1}, {-2,-1,0,1} },   // I
       { {0,1,1,1}, {0,0,1,2}, {1,1,1,2}, {0,1,2,2} },  	// J
       { {0,1,1,1}, {0,1,2,2}, {1,1,1,2}, {0,0,1,2} },      // L
       { {0,0,1,1}, {0,0,1,1}, {0,0,1,1}, {0,0,1,1} },	    // O
       { {0,0,1,1}, {0,1,1,2}, {0,0,1,1}, {-1,0,0,1} },     // S
       { {0,1,1,1}, {0,1,1,2}, {1,1,1,2}, {0,1,1,2} },      // T
       { {0,0,1,1}, {0,1,1,2}, {1,1,2,2}, {0,1,1,2} },      // Z
       
    };
 
 
  int score=0;  // score
  int lineCompleted = 0;   // number of lines completed
  int level=1;  //start level
  int goal = 4; // lines completed till the next level
  int currentGoal = goal; 
  int leveldelay = 25; 
  int dropDelay = 10; // delay for fast drop down (pressing down button)
  boolean gameOver=false;
  Clip clip; 
  
  javax.swing.JLabel scoreLabel = new javax.swing.JLabel("SCORE : 0");
  javax.swing.JLabel levelLabel = new javax.swing.JLabel("LEVEL : 0");
  javax.swing.JLabel goalLabel = new javax.swing.JLabel("Goal : 4");
  javax.swing.JLabel tetrisLabael = new javax.swing.JLabel("Team ANSUROER");
  
  public void init()
  {
    this.setPreferredSize(new java.awt.Dimension(600,600));
    this.setBackground(java.awt.Color.LIGHT_GRAY);          
 
    this.setLayout(null);    // absolute coordinate system
    
    tetrisLabael.setBounds(400, 25, 200, 100);
    tetrisLabael.setFont(fontTwo);
    this.add(tetrisLabael);
    
    scoreLabel.setBounds(400,100,150,30);  // x,y,w,h (in pixels)
    scoreLabel.setFont(font);
    this.add(scoreLabel);
    
    levelLabel.setBounds(400,125,100,30);
    levelLabel.setFont(font);
    this.add(levelLabel);
    
    goalLabel.setBounds(400,150,250,30);
    goalLabel.setFont(font);
    this.add(goalLabel);
 
  }
  
  /*
   * following 2 methods fill the occupied array with 0s or 1s
   * representative of occupied or empty cells
   * drawCell fill the color matrix with the current color of the
   * filled cell
   */
  public void drawCell(int x,int y, int color)
  {
    occupied[x][y] = 1;
    colorsCoords[x][y]= color; 
  }
 
  public void eraseCell(int x,int y)
  {
    occupied[x][y] = 0;
  }
  
  /*
   *  following 2 methods loop over the  4 cells of each tokens and call
   *  the filling/erasing method for every single cell 
   *  int[] xArray and int[] yArray are filled with the current x y coordinates
   *  of each token. This happens in the addFallingToken method. 
   */
  public void drawToken(int x, int y, int[] xArray, int[] yArray, int color)
  {
    for (int i=0;i<4;i++)
    {
      drawCell(x+xArray[i],y+yArray[i], color);
    }
  }
 
  public void eraseToken(int x, int y, int[] xArray, int[] yArray)
  {
    for (int i=0;i<4;i++)
    {
      eraseCell(x+xArray[i],y+yArray[i]);
    }
  }
 
  /*
   * (non-Javadoc)
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   *  paint method is always called when the repaint() method is called
   *   first we set the color of the border and fill the entire cell with
   *   the method fill.rect then we get the inner cell color and fill a 
   *   smaller rectangle inside the big one. This is the design of all 
   *   cells in the play field.
   */
  public void paint(java.awt.Graphics gr)
  {
    super.paint(gr);
    for (int x=0;x<occupied.length;x++)
      for (int y=0;y<occupied[0].length;y++)
        if (occupied[x][y]==1)
        {
          // draw cell
          gr.setColor(tokenBorderColors[colorsCoords[x][y]]);
          gr.fillRect(x*30,y*30,30,30);
          gr.setColor(tokenInnerColors[colorsCoords[x][y]]);
          gr.fillRect(x*30+4,y*30+4,24,24);         
        }
        else
        {
          // erase cell
          Color gridInnerColor = new Color(47,47,47);	
          gr.setColor(Color.BLACK);
          gr.fillRect(x*30,y*30,30,30);
          gr.setColor(gridInnerColor);
          gr.fillRect(x*30+2,y*30+2,28,28);
        }
  }
 
  // this method checks if the position of the token is not going out the play field
  // or the token has reached occupied cell
  public boolean isValidPosition(int x,int y, int tokenNumber, int rotationNumber)
  {
    int[] xArray = xRotationArray[tokenNumber][rotationNumber];
    int[] yArray = yRotationArray[tokenNumber][rotationNumber];
    
    // loops over the 4 cells of the token
    for (int i=0;i<4;i++)  // loop over the four cells 
    {
      int xCell = x+xArray[i];
      int yCell = y+yArray[i];
 
      // range check
      if (xCell<0) return false; // going out from the side left
      if (xCell>=10) return false; // going out from the right right
      if (yCell<0) return false; // reached empty floor
      if (yCell>=20) return false; // 
 
      // reached occupid cell
      if (occupied[xCell][yCell]==1) return false; 
    }
    // if all  checks above are false token can continue its way down
    return true;
  }
 
  public void clearCompleteRow(int[] completed)
  {
    // must loop for odd number of times.
    // toggle sequence : 0,1,0,1,0
    for (int blinking=0;blinking<5;blinking++)
    {
      for (int i=0;i<completed.length;i++)
      {
        if (completed[i]==1)
        {
          for (int x=0;x<10;x++)
          {
            // toggle the occupancy array
            occupied[x][i]= 0;
          }
        }
      }
      repaint();
      try { Thread.sleep(100); } catch (Exception ignore) {}
    }
  }
 
  public void shiftDown(int[] completed)
  {
    for (int row=0;row<completed.length;row++)
    {
      if (completed[row]==1)
      {
        for (int y=row;y>=1;y--)
        {
          for (int x=0;x<10;x++)
          {
            occupied[x][y] = occupied[x][y-1];
            colorsCoords[x][y] = colorsCoords[x][y-1]; 
          }
        }
      }
    }
  }
 
  public void checkRowCompletion()
  {
    int[] complete = new int[20];
    for (int y=0;y<20;y++)  // 20 rows
    {
      int filledCell = 0;
      for (int x=0;x<10;x++)  // 10 columns
      {
        if (occupied[x][y]==1) filledCell++;
        if (filledCell==10) // row completed 
        {
          complete[y]=1;
        }
      }
    }
 
    clearCompleteRow(complete);
 
    shiftDown(complete);   
 
    addScore(complete);
  }
 
  void addScore(int[] complete)
  {
	int completeCounter = 0;  
    for (int row=0;row<complete.length;row++)
    {
      if (complete[row]==1)
      {
        completeCounter++;
        lineCompleted++;
        if (lineCompleted == goal) {
        	level++;
        	goal += 4;
        	currentGoal = goal + 1;
        	lineCompleted = 0;
        	dropDelay--;
        	leveldelay--;
    	}
        currentGoal--;
      }
    }
	
    if (completeCounter == 1) {
    	score += 100 * level;
	}
    else if (completeCounter == 2) {
    	score += 300 * level;
	}
    else if (completeCounter == 3) {
    	score += 500 * level;
	}
    else if (completeCounter == 4) {
    	score += 800 * level;
	}

   
    if (level>20) { lineCompleted=0; level=0; }  // MAX LEVEL
 
    scoreLabel.setText("SCORE : "+score);
    levelLabel.setText("LEVEL : "+level);
    goalLabel.setText("Goal : " +currentGoal);
  }
 
  /*
   * This is the begging method that changes the coordinates of the 
   * falling tokens and calls all other methods recursively.
   */
  public void addFallingToken()
  {
    int x=4,y=0;
    int tokenNumber = (int) (7*Math.random());
    int rotationNumber = 0; 
    int color = tokenNumber;  
    
    // the starting coordinates of each token
    int[] xArray = xRotationArray[tokenNumber][rotationNumber];
    int[] yArray = yRotationArray[tokenNumber][rotationNumber];
    
    // This block checks if the token can begin falling down. If
    // this return false it mean that field is empty and ends the game
    if (!isValidPosition(x,y,tokenNumber,rotationNumber)) 
    {
      gameOver=true;
      drawToken(x,y,xArray,yArray, color);
      repaint();
      return;
    }
    
    // the token is printed on the field for teh first time
    drawToken(x,y,xArray,yArray, color);
    repaint();
    
    int frame=0;
    boolean reachFloor=false;
    while (!reachFloor)
    {
      try { Thread.sleep(leveldelay); } catch (Exception ignore) {}
      eraseToken(x,y,xArray,yArray);
      // add keyboard control
      // by pressing the controls the coordinates of each token are being changed
      if (leftPressed && isValidPosition(x-1,y,tokenNumber,rotationNumber)) {
    	  try { Thread.sleep(85); } catch (Exception ignore) {} x -= 1; }
      if (rightPressed && isValidPosition(x+1,y,tokenNumber,rotationNumber)) {
    	  try { Thread.sleep(85); } catch (Exception ignore) {}    	  x += 1;}
      if (downPressed && isValidPosition(x,y+1,tokenNumber,rotationNumber)) {
    	  try { Thread.sleep(dropDelay); } catch (Exception ignore) {} y += 1;}   
      	score++;
      if (upPressed && isValidPosition(x,y,tokenNumber,(rotationNumber+1)%4)) {
        rotationNumber = (rotationNumber+1)%4;
        xArray = xRotationArray[tokenNumber][rotationNumber];
        yArray = yRotationArray[tokenNumber][rotationNumber];
        upPressed=false;  
      }
 
      int f=31-level;   // fall for every 31 frames, this value is decreased when level up
      if (frame % f==0) y += 1;  
      if (!isValidPosition(x,y,tokenNumber,rotationNumber)) {  // reached floor
        reachFloor=true;
        y -= 1;  // restore position
      }
      drawToken(x,y,xArray,yArray, color);
      repaint();
      frame++;
    }
 
  }
 
  public void printGameOver()
  {
	clip.stop();
	startMusic("gg.wav");
    javax.swing.JLabel gameOverLabel = new javax.swing.JLabel("GOOD GAME!!!");
    gameOverLabel.setBounds(380,300,250,30);
    gameOverLabel.setFont(fontThree);
    gameOverLabel.setForeground(Color.GREEN);
    add(gameOverLabel);
    repaint();
  }
 
  boolean leftPressed=false;
  boolean rightPressed=false;
  boolean downPressed=false;
  boolean upPressed=false;
 
  // must implements this method for KeyListener
  public void keyPressed(java.awt.event.KeyEvent event)
  {
    if (event.getKeyCode()==37) // left arrow
    {
      leftPressed=true;
    }
    if (event.getKeyCode()==39) // right arrow
    {
      rightPressed=true;
    }
    if (event.getKeyCode()==40) // down arrow
    {
      downPressed=true;
    }
    if (event.getKeyCode()==38) // space
    {
      upPressed=true;
    }
 
  }
 
  public void keyReleased(java.awt.event.KeyEvent event)
  {
    if (event.getKeyCode()==37) // left arrow
    {
      leftPressed=false;
    }
    if (event.getKeyCode()==39) // right arrow
    {
      rightPressed=false;
    }
    if (event.getKeyCode()==40) // down arrow
    {
      downPressed=false;
    }
    if (event.getKeyCode()==38) // space
    {
      upPressed=false;
    }
 
  }
 
  public void keyTyped(java.awt.event.KeyEvent event)
  {
//    System.out.println(event);
  }
 
  public void startMusic(String filePath) {
	  try {
	        File soundFile = new File(filePath);
	        AudioInputStream stream;
	        AudioFormat format;
	        DataLine.Info info;

	        stream = AudioSystem.getAudioInputStream(soundFile);
	        format = stream.getFormat();
	        info = new DataLine.Info(Clip.class, format);
	        clip = (Clip) AudioSystem.getLine(info);
	        clip.open(stream);
	        if (filePath.equals("tetris.wav")) {
		        clip.loop(Integer.MAX_VALUE);
			}
	        else {
				clip.start();
			}
	        
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
  }
  
  public static void main(String[] args) throws Exception
  {
    javax.swing.JFrame window = new javax.swing.JFrame("Team ANSUROER");
    window.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    
    Tetris tetris = new Tetris();
    tetris.init();
    window.add(tetris);
    window.pack();
    window.setVisible(true);
    try { Thread.sleep(1000); } catch (Exception ignore) {}
 
    window.addKeyListener(tetris);  // listen to keyboard event
 
    tetris.startMusic("tetris.wav");
    tetris.gameOver=false;
    while (!tetris.gameOver)
    {
      tetris.addFallingToken();
      tetris.checkRowCompletion();
    }
 
    tetris.printGameOver();
 
  } 
 
}

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import g4p_controls.G4P;
import g4p_controls.GButton;
import g4p_controls.GCScheme;
import g4p_controls.GCheckbox;
import g4p_controls.GCustomSlider;
import g4p_controls.GDropList;
import g4p_controls.GEvent;
import g4p_controls.GImageButton;
import g4p_controls.GLabel;
import g4p_controls.GTextField;
import processing.core.PApplet;
import processing.core.PImage;

public class GUI extends PApplet {
	PImage robot, dirt, bug;
	int x, y;
	Room start;
	float k, kold = -1, koldR = -1;
	int pWidth, pHeight; // size of window
	@SuppressWarnings("rawtypes")
	ArrayList results = new ArrayList<>();
	boolean Run = false, idle = false;
	boolean wallPressed = false, dirtPressed = false, agentPressed = false;
	boolean first = true;
	int delayOriginal = 0;
	int delay = delayOriginal;
	ArrayList<Agent> Agents = new ArrayList<>();

	int appTime = -1, fc = -1;
	Timer t;
	
	

	public void setup() {
		Agents.add(new Agent(0, 0, 100000));
		

		frameRate(30);
		pWidth = 0;
		pHeight = 0;
		x = 6; // default height and width of the room
		y = 5;

		robot = loadImage("robot.png");
		dirt = loadImage("dirt.png");
		bug = loadImage("bug.png");
		registerMethod("pre", this);
		surface.setResizable(true);
		surface.setTitle("Clean Dirt");
		surface.setIcon(loadImage("icon.png"));
		start = new Room(x, y);
		Agents.add(new AgentBad(1, 0, 100000));
		createGUI();
		startTimer();
	}

	public void startTimer() {
		if (t != null)
			t.cancel();
		t = new Timer();

		t.schedule(new TimerTask() {
			@Override
			public void run() {
				for (Agent agent : Agents) {
					if (agent.battery > 0 && startBtn != null && startBtn.getText() == "Stop Agent" && Run == false)
						start_click(null, GEvent.PRESSED);
					if (Run) {
						if (!idle) {
							agent.performance--;
							agent.battery--;
							agent.timeOn++;
						}
						if (agent.battery <= 0) {
							agent.battery = 0;
							Run = false;
						}
					}
				}
				surface.setTitle("Clean Dirt "
						+ nf(appTime / 60, 2) + ":" + nf(appTime == -1 ? 0 : appTime % 60, 2));
				appTime++;
			}
		}, 0, (long) (125 * (1 + delayOriginal)));
	}
	// Draw Borad
	public void draw() {
		
		try {
			x = start.getGridWidth();
			y = start.getGridHeight();

			background(209);
			k = (float) Math.min(((height - 1.0) / y), (width - 150.0) / x);
			// System.out.println(y);
			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					fill(255);
					rect(i * k, j * k, k, k);
					if (!start.grid[i][j].isClean()) {
						if (k < 5) {
							fill(200, 200, 0);
							rect(i * k, j * k, k, k);
						} else {
							// Choosing the picture size based on the number of grids
							if (kold != k) {
								if (x * y < 41)
									dirt = loadImage("dirt.png");
								else if (x * y < 81)
									dirt = loadImage("dirt50%c.png");
								else if (x * y < 401)
									dirt = loadImage("dirt16%c.png");
								else
									dirt = loadImage("dirt10%c.png");

								dirt.resize((int) k, (int) k);
							}
							kold = k;
							image(dirt, i * k, j * k);
						}

					}

					if (start.grid[i][j].hasWalls()) {
						fill(0, 0, 0);
						float wallWidth = (float) (k / 26.0);
						if (start.grid[i][j].HasWallUp())
							rect(i * k, j * k, k, wallWidth);
						if (start.grid[i][j].HasWallDown())
							rect(i * k, (j + 1) * k - wallWidth, k, wallWidth);
						if (start.grid[i][j].HasWallLeft())
							rect(i * k, j * k, wallWidth, k);
						if (start.grid[i][j].HasWallRight())
							rect((i + 1) * k - wallWidth, j * k, wallWidth, k);
					}

				}
			}

			// fill(0, 0, 150);
			// arc((o + 0.5f) * k, (u + 0.5f) * k, k * 0.9f, k * 0.9f, QUARTER_PI / 2, PI +
			// 7 * QUARTER_PI / 2);
			if (koldR != k) {
				robot = loadImage("robot.png");
				robot.resize((int) k, (int) k);
				bug = loadImage("bug.png");
				bug.resize((int) k, (int) k);
			}
			koldR = k;
			for (Agent agent : Agents) {
//			System.out.println("\t"+agent);
				if(agent.getIdentitity())
					image(robot, agent.x * k, agent.y * k);
				else
					image(bug, agent.x * k, agent.y * k);
				textSize(max(k/7,7));
				if(Agents.indexOf(agent)+1<10)
					text(Agents.indexOf(agent)+1, (float)(agent.x+0.451)*(float)k, (float)(agent.y +0.4)* (float)k);
				else
					text(Agents.indexOf(agent)+1, (float)(agent.x+0.407)*(float)k, (float)(agent.y +0.4)* (float)k);
				if (Run) {
					fill(0, 220, 0);
					if (agent.battery < 30)
						fill(210 - 5 * agent.battery, 7 * agent.battery, 0);
				} else {
					fill(155, 155, 0);
					if (agent.battery < 30)
						fill(210 - 5 * agent.battery, 7 * agent.battery, 0);
				}
				float val = (float) startBtn.getY() + 83 + (105*Agents.indexOf(agent));
				rect((float) (width - 147), val-18, 144, 103, 7);
				fill(0);
				textSize(14.5f);

				
				textSize(9f);
				textLeading(10);
				text("   hold   \n to  reset", (float) (width - 48), val -5);
				textSize(14.5f);
				text("Agent "+nf((Agents.indexOf(agent)+1),2)+": " + (Run ? idle ? "Idle" : "On" : "Off"), (float) (width - 142), val);
				text("Performance:" + agent.performance, (float) (width - 142), val + 20);
				text("Steps: " + agent.steps, (float) (width - 142), val + 40);
				text("Battery: " + agent.battery, (float) (width - 142), val + 60);
				text("Time On: " + nf(agent.timeOn / 60, 2) + ":" + nf(agent.timeOn == -1 ? 0 : agent.timeOn % 60, 2),
						(float) (width - 142), val + 80);

				if (fc != -1 && (frameCount - fc) / frameRate >= 1) {
//				reset();
					fc = -1;
				}
				
				
			}
				if (Run) {
					try {
						if (delay-- == 0) {
							for(Agent agent:Agents) {
//						thread("Run");
							Run(agent);
							delay = delayOriginal;
							}
						}
					} catch (Exception e) {
						System.out.println(e);
					}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			println("e");
		}
			

		
	}
// Hàm chạy
	public void Run(Agent agent) {
		if (start.getNbOfReachableDirt(agent) > 0) {
			idle = false;
			if (start.isDirt(agent.x, agent.y)) {
				start.removeDirt(agent.x, agent.y);
				agent.performance += 100;
				agent.battery -= 20;
				if (agent.battery <= 0) {
					agent.battery = 0;
					Run = false;
				}
			} else {
					try {
						Tile tile = (Tile)  BFS.compute(start, agent, false).remove(1);
					
						if(isEmpty(tile,Agents)) {
							int xold = agent.x, yold = agent.y;
							agent.x = tile.getX();
							agent.y = tile.getY();
						
						if (agent.x != xold || agent.y != yold) {
							agent.steps++;
							agent.performance -= 10;
							agent.battery -= 10;
							if (agent.battery <= 0) {
								agent.battery = 0;
								Run = false;
							}

						}
						}
					} catch (Exception e) {}
				}
			}
		}
// duyệt từ robot đầu tiên => cuối cùng 
	public boolean isEmpty(Tile a,ArrayList<Agent> arl) {
		x = a.getX();
		y = a.getY();
		for(Agent agent :Agents) 
			if(x==agent.x && y == agent.y)
				return false;
		return true;
	}
	
	// xóa tường
	public void addRemoveWall(boolean addRemove, int mouseX, int mouseY, boolean clicked) {
		results = new ArrayList<>();
		double a = mouseX / (double) k, b = mouseY / (double) k;
		int x = (int) Math.round(a), y = (int) Math.round(b);
		if (clicked || (Math.abs(a - x) > 0.2 || Math.abs(b - y) > 0.2)) {
			if (Math.abs(a - x) <= Math.abs(b - y)) {
				try {
					if (addRemove)
						start.putWallLeft(x, (int) Math.floor(b));
					else
						start.removeWallLeft(x, (int) Math.floor(b));
				} catch (Exception e) {
					if (x > 0) {
						if (addRemove)
							start.putWallRight(x - 1, (int) Math.floor(b));
						else
							start.removeWallRight(x - 1, (int) Math.floor(b));
					}
				}
				;
			} else {
				try {
					if (addRemove)
						start.putWallUp((int) Math.floor(a), y);
					else
						start.removeWallUp((int) Math.floor(a), y);
				} catch (Exception e) {
					if (y > 0) {
						if (addRemove)
							start.putWallDown((int) Math.floor(a), y - 1);
						else
							start.removeWallDown((int) Math.floor(a), y - 1);
					}
				}
				;
			}
		}
	}

	public void mouseReleased() {
		fc = -1;
	}

	public void mousePressed() {
		if (mouseX > (float) (width - 147) && mouseY > (float) startBtn.getY() + 35 && mouseX < (float) (width - 3)
				&& mouseY < (float) startBtn.getY() + 118) {
			fc = frameCount;
		} else
			fc = -1;
		int mxk = (int) (mouseX / k), myk = (int) (mouseY / k);
		if (myk < y && mxk < x && myk >= 0 && mxk >= 0) {

			if (wallPressed) {
				if (mouseButton == LEFT) {
					addRemoveWall(true, mouseX, mouseY, true);
				} else {
					if (mouseButton == RIGHT) {
						addRemoveWall(false, mouseX, mouseY, true);
					}
				}
			} else {
				if (dirtPressed) {
					if (mouseButton == LEFT)
						start.addDirt(mxk, myk);
					else {
						if (mouseButton == RIGHT) {
							start.removeDirt(mxk, myk);
						}
					}
				}

			}
			if (agentPressed) {
//				Agents.get(dropBox.getSelectedIndex()).setPosition(mxk, myk);
				updateAgent((int)mxk,(int)myk,Agents.get(dropBox.getSelectedIndex()));
				if (mouseButton == RIGHT)
					start.removeDirt(mxk, myk);

			}
		}
	}

	public void mouseDragged() {
		double mxk = mouseX / (double) k, myk = mouseY / (double) k;
		if (myk < y && mxk < x && myk >= 0 && mxk >= 0) {

			if (wallPressed) {
				if (mouseButton == LEFT)
					addRemoveWall(true, mouseX, mouseY, false);
				else {
					addRemoveWall(false, mouseX, mouseY, false);
				}
			} else {
				if (dirtPressed) {
					if (mouseButton == LEFT)
						start.addDirt((int) mxk, (int) myk);
					else {
						if (mouseButton == RIGHT)
							start.removeDirt((int) mxk, (int) myk);
					}
				}
			}
			if (agentPressed) {
//				Agents.get(dropBox.getSelectedIndex()).setPosition((int)mxk, (int)myk);
				
				updateAgent((int)mxk,(int)myk,Agents.get(dropBox.getSelectedIndex()));
			}

		}
	}
// duyệt từng robot, xem tồn tại bao nhiêu robot, ưu tiên robot 1 quét bẩn khi tới cùng 1 mục tiêu
	public boolean hasAgent(int x, int y) {
		for(Agent a : Agents)
			if(a.getX()==x && a.getY() == y)
				return true;
		return false;
	}

	public void updateAgent(int x, int y,Agent agent) {
		if(!hasAgent(x, y))
			agent.setPosition(x, y);
	}

	public void pre() {
		if (pWidth != width || pHeight != height) {
			float nx, ny; // used to calculate new control positions
			// Window has been resized so
			nx = width - wallBtn.getWidth() - 1;
			ny = (float) (8);

			nx = width - wallBtn.getWidth() - 1;
			loadBtn.moveTo(nx, ny);
			nx = nx - dirtBtn.getWidth() - 5;
			saveBtn.moveTo(nx, ny);

			ny = ny + loadBtn.getHeight() + 5;
			xField.moveTo(nx + 2, ny+1);

//			
			yField.moveTo(nx + xField.getWidth()+3, ny+1);

			nx = width - submitBtn.getWidth() - 1;
			submitBtn.moveTo(nx, ny);
			ny += submitBtn.getHeight() + 3;
			


			nx = width - generateBtn.getWidth() - 1;
			generateBtn.moveTo(nx, ny);
			nx =  xField.getX()-2;
			roomBox.moveTo(nx, ny - 6);
			dirtBox.moveTo(nx, ny + 10);
			wallsBox.moveTo(nx, ny + 26);

			nx = width - wallBtn.getWidth() - 1;
			ny += generateBtn.getHeight() + 3;
			dirtBtn.moveTo(nx, ny);

			nx = nx - dirtBtn.getWidth() - 5;
			wallBtn.moveTo(nx, ny);
			// helpText.moveTo(nx, ny - wallBtn.getHeight() - 8);

			ny += wallBtn.getHeight() + 5;
			
			batteryField.moveTo(nx, ny);
			nx = width - wallBtn.getWidth() - 1;
			submitB.moveTo(nx, ny);
			ny = ny + saveBtn.getHeight() + 5;
			nx = width - agentLoc.getWidth() - 1;
			startBtn.moveTo(nx, ny);
			nx = nx - agentLoc.getWidth() - 5;
			agentLoc.moveTo(nx, ny);

			ny += submitBtn.getHeight() + 2;
			dropBox.moveTo(agentLoc.getX()+4, ny+6);
			GDBox.moveTo(dropBox.getX()+dropBox.getWidth()+6, ny+6);
			add.moveTo(width-add.getWidth()-4, ny);
			remove.moveTo(width-2*add.getWidth()-4, ny);
//			add.

			speedSlider.moveTo(nx, height - speedSlider.getHeight() - 1);
			speed.moveTo(nx + 46, speedSlider.getCY() - speedSlider.getHeight() + 2);
			// // save current widow size
			pWidth = width;
			pHeight = height;
		}
	}

	public void updateBtnColor() {
		if (wallPressed) {
			wallBtn.setLocalColorScheme(GCScheme.GREEN_SCHEME);
			dirtBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
			agentLoc.setLocalColorScheme(GCScheme.BLUE_SCHEME);
		}
		if (dirtPressed) {
			wallBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
			dirtBtn.setLocalColorScheme(GCScheme.GREEN_SCHEME);
			agentLoc.setLocalColorScheme(GCScheme.BLUE_SCHEME);
		}
		if (agentPressed) {
			wallBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
			dirtBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
			agentLoc.setLocalColorScheme(GCScheme.GREEN_SCHEME);
		}
		else {
			agentLoc.setLocalColorScheme(GCScheme.BLUE_SCHEME);
		}

	}

	public void start_click(GButton source, GEvent event) {
		if (event == GEvent.PRESSED) {
			results = new ArrayList<>();
			Run = !Run;
			if (Run) {
				agentPressed = false;
				updateBtnColor();
				startBtn.setText("Stop Simulation");
				startBtn.setTextBold();

			} else {
				startBtn.setText("Start Simulation");
				startBtn.setTextBold();
			}
		}
	}

	public void reset(Agent agent) {
		noLoop();
		agent.performance = 0;
		agent.battery = 100000;
		agent.steps = 0;
		appTime = -1;
		loop();
	}

	public void wall_click(GButton source, GEvent event) {
		wallPressed = true;
		dirtPressed = false;
		agentPressed = false;
		updateBtnColor();
	}

	public void dirt_click(GButton source, GEvent event) {
		dirtPressed = true;
		wallPressed = false;
		agentPressed = false;
		updateBtnColor();
	}

	public void agent_click(GButton source, GEvent event) {
		agentPressed = true;
		wallPressed = false;
		dirtPressed = false;
		updateBtnColor();
		ArrayList<String> arr = new ArrayList<>();
//		for(int i =0;i<Agents.size();i++)
////		dropBox.insertItem(i+1, 1+i+"");
////		dropBox.addItem(1+i+"");
//		arr.add(1+i+"");
//	dropBox.setItems(arr,arr.size());
	}

	public void xField_change(GTextField source, GEvent event) {
	}

	public void yField_change(GTextField source, GEvent event) {
	}

	public void submit_click(GButton source, GEvent event) {
		try {
			x = Integer.parseInt(xField.getText());
			y = Integer.parseInt(yField.getText());
			background(209);
			Agents = new ArrayList<>();
			Agents.add(new Agent(0, 0, 100000));
			Agents.add(new AgentBad(1, 0, 100000));
			updateBox();
			results = new ArrayList<>();
			start = new Room(x, y);
//			reset();
		} catch (Exception e) {
			println("Wrong input");
		}
	}

	public void save_click(GButton source, GEvent event) {
		if (event == GEvent.PRESSED)
			selectOutput("Select a file name:", "outputFileSelected");
	}

	public void outputFileSelected(File selection) {
		if (selection == null) {
			println("Window was closed or the user hit cancel.");
		} else {
			println("User selected " + selection.getAbsolutePath());
			start.save(selection.getAbsolutePath() + ".room");
		}
	}

	public void load_click(GButton source, GEvent event) {
		noLoop();
		if (event == GEvent.PRESSED)
			selectInput("Select a room:", "inputFileSelected");
		updateBox();
		loop();
		
	}

	public void inputFileSelected(File selection) {
		if (selection == null) {
			println("Window was closed or the user hit cancel.");
		} else {
			println("User selected " + selection.getAbsolutePath());
			try {
				Room r = Room.load(selection.getAbsolutePath());
				if (r != null) {
//					noLoop();
					start = r;
//					agent = new Agent(0, 0, 100000);
					Agents = new ArrayList<>();
					Agents.add(new Agent(0, 0, 100000));
					Agents.add(new AgentBad(1, 0, 100000));
					x = start.getGridWidth();
					y = start.getGridHeight();
					results = new ArrayList<>();
//					reset();
					// agent.x = 0;
					// agent.y = 0;
//					loop();
				} else
					println("Wrong Input");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void speedSlider_changed(GCustomSlider source, GEvent event) {
		if ((source.getValueF() == (int) source.getValueF())) {
			int newDelay = source.getValueI();
			;
			if (newDelay == 4)
				newDelay = 0;
			else {
				if (newDelay == 3)
					newDelay = 1;
				else {
					if (newDelay == 2)
						newDelay = 3;
					else {
						if (newDelay == 1)
							newDelay = 7;
						else
							newDelay = 15;
					}
				}
			}
			if (newDelay != delayOriginal) {
				delayOriginal = newDelay;
				startTimer();
			}
		}
	}

	public void setBattery(GButton source, GEvent event) {
		try {
			int a = Integer.parseInt(batteryField.getText());
			Agents.get(dropBox.getSelectedIndex()).battery = a;
		} catch (NumberFormatException e) {
			println("Wrong battery value");
		}
	}
// hàm random
	public void generate(GButton source, GEvent event) {
		noLoop();
		boolean restart = Run;
		Run = false;
		if (!roomBox.isSelected() && !dirtBox.isSelected() && !wallsBox.isSelected())
			start.clean();
		else {
			if (roomBox.isSelected()) {
				Random random = new Random();
				start = new Room(random.nextInt(20) + 2, random.nextInt(20) + 2);
//				reset();
					Agents = new ArrayList<>();
					Agents.add(new Agent(0, 0, 100000));
					Agents.add(new AgentBad(1,0, 100000));
					updateBox();
				
			}
			if (wallsBox.isSelected()) {
				Agent a = Agents.get(0);
				if(a==null)
					a = new Agent(0,0,0);
				start.generateWalls(a);
			}
			if (dirtBox.isSelected())
				start.generateDirt();

		}
		loop();

		results = new ArrayList<>();
//		reset();
		Run = restart;
	}
	void updateBox() {
		ArrayList<String> arr = new ArrayList<>();
		for(int i =0;i<Agents.size();i++)
			arr.add(1+i+"");
		dropBox.setItems(arr,arr.size());
	}
// Xóa Agent
	public void removeAgent(GImageButton imagebutton, GEvent event) {
		int index = dropBox.getSelectedIndex();
		Agents.remove(index);
		updateBox();		
	}
// thêm Agent	
	public void addAgent(GImageButton imagebutton, GEvent event) { 
		boolean good = GDBox.getSelectedIndex()==0;
		for(int i =0;i<start.h;i++)
			for(int j=0;j<start.w;j++)
				if(!hasAgent(j, i)) {
					if(good) {
						Agents.add(new Agent(j, i, 100000));
						updateBox();
						return;
					}
					else {
						Agents.add(new AgentBad(j, i, 100000));
						updateBox();
						return;
					}
				}
	}

	// Create all the GUI controls.
	public void createGUI() {
		G4P.messagesEnabled(false);
		G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
		G4P.setMouseOverEnabled(false);
		wallBtn = new GButton(this, 311, 27, 70, 26);
		wallBtn.setText("Wall");
		wallBtn.setTextBold();
		wallBtn.fireAllEvents(true);
		wallBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
		wallBtn.addEventHandler(this, "wall_click");
		dirtBtn = new GButton(this, 391, 27, 70, 26);
		dirtBtn.setText("Dirt");
		dirtBtn.setTextBold();
		dirtBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
		dirtBtn.fireAllEvents(true);
		dirtBtn.addEventHandler(this, "dirt_click");
		agentLoc = new GButton(this, 309, 67, 70, 30);
		agentLoc.setText("Location");
		agentLoc.setTextBold();
		agentLoc.addEventHandler(this, "agent_click");
		agentLoc.fireAllEvents(true);
		xField = new GTextField(this, 308, 107, 32, 23, G4P.SCROLLBARS_NONE);
		xField.setPromptText("  W");
		xField.setOpaque(false);

		xField.addEventHandler(this, "xField_change");
		yField = new GTextField(this, 401, 107, 32, 23, G4P.SCROLLBARS_NONE);
		yField.setPromptText("  H");
		yField.setOpaque(true);
		yField.addEventHandler(this, "yField_change");
		submitBtn = new GButton(this, 356, 136, 70, 30);
		submitBtn.setText("Create new Room");
		submitBtn.setTextBold();
		submitBtn.setLocalColorScheme(GCScheme.GOLD_SCHEME);
		submitBtn.addEventHandler(this, "submit_click");
		// submitBtn.mouseEvent();
		startBtn = new GButton(this, 343, 176, 70, 30);
		submitBtn.fireAllEvents(true);
		startBtn.fireAllEvents(true);
		startBtn.setText("Start");
		startBtn.setTextBold();
		startBtn.setLocalColorScheme(GCScheme.ORANGE_SCHEME);
		startBtn.addEventHandler(this, "start_click");
		saveBtn = new GButton(this, 356, 136, 70, 30);
		saveBtn.setText("Save Room");
		saveBtn.setTextBold();
		saveBtn.setLocalColorScheme(GCScheme.GREEN_SCHEME);
		saveBtn.addEventHandler(this, "save_click");
		saveBtn.fireAllEvents(true);
		loadBtn = new GButton(this, 356, 136, 70, 30);
		loadBtn.setText("Load Room");
		loadBtn.setTextBold();
		loadBtn.setLocalColorScheme(GCScheme.RED_SCHEME);
		loadBtn.addEventHandler(this, "load_click");
		loadBtn.fireAllEvents(true);
		// helpText = new GLabel(this, 391, 10, 140, 30, "Left Click to add \nRight
		// Click to Remove");
		// System.out.print("\033[H\033[2J");
		// System.out.flush();
		// println("Starts here: ");

		speedSlider = new GCustomSlider(this, 343, 176, 140, 60, "grey_blue");
		speedSlider.setLimits(0.5f, 1, 4);
		String[] tickLabels = { "0.5x", "1x", "2x", "4x", "8x" };
		speedSlider.setTickLabels(tickLabels);
		speedSlider.addEventHandler(this, "speedSlider_changed");
		speedSlider.setValue(1.0f);
		speed = new GLabel(this, 343, 176, 140, 60, "Speed");
		speed.setFont(new Font("Segoe UI", Font.PLAIN, 16));

		batteryField = new GTextField(this, 308, 107, 70, 30, G4P.SCROLLBARS_NONE);
		batteryField.setPromptText("Set Battery");
		batteryField.setOpaque(false);
		submitB = new GButton(this, 356, 136, 70, 31);
		submitB.setText("Recharge Agent");
		submitB.setTextBold();
		submitB.fireAllEvents(true);
		submitB.addEventHandler(this, "setBattery");
		roomBox = new GCheckbox(this, 356, 136, 70, 30, "Room");
		dirtBox = new GCheckbox(this, 356, 136, 70, 30, "Dirt");
		wallsBox = new GCheckbox(this, 356, 136, 70, 30, "Walls");
		generateBtn = new GButton(this, 356, 136, 70, 50, "Generate Selection or Clean");
		generateBtn.setTextBold();
		generateBtn.setLocalColorScheme(GCScheme.PURPLE_SCHEME);
		generateBtn.addEventHandler(this, "generate");
		
		dropBox = new GDropList(this, 706, 136, 28, 200,10);
//		dropBox.setLocalColor(2,color(255,0,0));
		dropBox.setLocalColorScheme(GCScheme.RED_SCHEME);
		ArrayList<String> arr = new ArrayList<>();
		for(int i =0;i<Agents.size();i++)
			arr.add(1+i+"");
		dropBox.setItems(arr,arr.size());
		
		GDBox = new GDropList(this, 706, 136, 28, 800,40);
		GDBox.setLocalColorScheme(GCScheme.GREEN_SCHEME);
		
		ArrayList<String> arr2 = new ArrayList<>();
		arr2.add("G");
		arr2.add("B");
		GDBox.setItems(arr2,arr2.size());

		
		String[] imga = {"img/remove1.png","img/remove2.png","img/remove3.png"};
		remove = new GImageButton(this, 2, 136,32,31,imga);
		remove.addEventHandler(this, "removeAgent");
		String[] imgr = {"img/add1.png","img/add2.png","img/add3.png"};
		add = new GImageButton(this, 250, 136,32,31,imgr);
		add.addEventHandler(this, "addAgent");

	}

	// Variable declarations
	GCustomSlider speedSlider;
	GLabel speed, helpText;
	GButton wallBtn, dirtBtn, agentLoc, submitBtn, startBtn, saveBtn, loadBtn, submitB, generateBtn;
	GTextField xField, yField, batteryField;
	GCheckbox roomBox, dirtBox, wallsBox;
	GDropList dropBox, GDBox;
	GImageButton add, remove;
	public void settings() {
		size(800, 550);
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "GUI" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}

}
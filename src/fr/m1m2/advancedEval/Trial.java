package fr.m1m2.advancedEval;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import fr.lri.swingstates.canvas.*;
import fr.lri.swingstates.canvas.Canvas;

public class Trial {

	protected boolean practice = false;
	protected int block;
	protected int trial;
	protected String visualVariable;
	protected int objectCount;
	
	protected Experiment experiment;

	protected CExtensionalTag instructions = new CExtensionalTag() { };
	protected CExtensionalTag visualMarks = new CExtensionalTag() { };
	protected CExtensionalTag ghosts = new CExtensionalTag() { };
	protected CExtensionalTag target = new CExtensionalTag() { };

    protected Date timeSpaceHit;
    protected Date timeImageShown;

	
	public Trial(Experiment experiment, boolean practice, int block, int trial, String visualVariable, int objectCount) {
		this.practice = practice;
		this.block = block;
		this.trial = trial;
		this.visualVariable = visualVariable;
		this.objectCount = objectCount;
		this.experiment = experiment;
	}

	protected KeyAdapter enterListener = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			super.keyReleased(e);
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_ENTER) {
				displayShapes();
			}
		}
	};

	protected KeyAdapter spacebarListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			int key = e.getKeyCode();
			if(key == KeyEvent.VK_SPACE) {

			    // Log time that the space bar was hit. This is the moment they noticed the correct mark.
                timeSpaceHit = new Date();

                // Display the ghost marks to the user
				displayGhosts();
			}
		}
	};

	protected MouseAdapter clickListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Canvas canvas = experiment.getCanvas();

            // Get the targetMark so we can check its properties
            CShape targetMark = canvas.getFirstHavingTag(target);

            // Check if the (now transparent) target has been clicked
            if(targetMark.getMinX() < e.getX() &&
                    e.getX() < targetMark.getMaxX() &&
                    e.getY() > targetMark.getMinY() &&
                    e.getY() < targetMark.getMaxY()) {

                // 1. Log the time taken to hit the space-bar after the image has been displayed (difference in ms)
                long reactionTime = timeImageShown.getTime() - timeSpaceHit.getTime();

                // 2. Remove the transparent target from the screen
                canvas.removeShapes(target);

                // Remove this listener
                canvas.removeMouseListener(clickListener);

                // Remove the ghost shapes
                canvas.removeShapes(ghosts);

                // Continue to next trial
                experiment.nextTrial();

            } else {

            }


		}
	};

	protected void displayGhosts() {

	    // grab the canvas and remove the lingering listeners
		Canvas canvas = experiment.getCanvas();
        canvas.removeKeyListener(spacebarListener);

        // Remove the visual distraction marks
		canvas.removeShapes(visualMarks);

		// Set the target market to transparent
		CShape targetMark = canvas.getFirstHavingTag(target);
		targetMark.setFilled(false);

		// Add mouse listener
		canvas.addMouseListener(clickListener);
	};

	protected void displayShapes() {

	    // Record time that the user gets to start looking at images
	    timeImageShown = new Date();

	    Canvas canvas = experiment.getCanvas();

        canvas.removeKeyListener(enterListener);
	    canvas.removeShapes(instructions);
	    canvas.addKeyListener(spacebarListener);

		// Whether to take the invert the variables or not
		boolean invert = (Math.random() > 0.5);

		// Variable defaults
		int targetSize = 40;
		int markSize = 40;
		Color targetColor = Color.DARK_GRAY;
        Color markColor = Color.DARK_GRAY;

		// List of shapes
		ArrayList<CShape> shapeList = new ArrayList<>();

		if(invert) {
			markSize = 20;
			markColor = Color.BLUE;
		} else {
			targetSize = 20;
			targetColor = Color.BLUE;
		}

        // Initialise how many marks we will add
        int numberOfMarks = 0;

		CRectangle targetMark = new CRectangle();

		switch(visualVariable) {
			case "VV1":

			    // Add target
                targetMark = createShape(targetSize, markColor);
                shapeList.add(targetMark);

                // how many marks are we adding
                numberOfMarks = 1;
                shapeList.add(createShape(markSize, markColor));

				break;

			case "VV2":

                // Add target
                targetMark = createShape(markSize, targetColor);
                shapeList.add(targetMark);

                // how many marks are we adding
                numberOfMarks = 1;
                shapeList.add(createShape(markSize, markColor));

				break;

			case "VV1VV2":

				// Target
                targetMark = createShape(targetSize, targetColor);
                shapeList.add(targetMark);

                // how many marks are we adding
                numberOfMarks = 5;
				shapeList.add(createShape(markSize, targetColor));
				shapeList.add(createShape(markSize, targetColor));

				shapeList.add(createShape(targetSize, markColor));
				shapeList.add(createShape(targetSize, markColor));

				shapeList.add(createShape(markSize, markColor));
				shapeList.add(createShape(markSize, markColor));

				break;
		}

		System.out.println(visualVariable);

        // Finish the rest of the list as required with random copies from the marks
        // How many more do we need to add?
        int remaining = objectCount - shapeList.size();
        for(int i=0; i < remaining; i++) {

            // Find which one to copy and add it
            int copyIndex = 1 + (int)(Math.random() * ((numberOfMarks - 1) + 1));
            shapeList.add(createShape((int)shapeList.get(copyIndex).getWidth(), (Color)shapeList.get(copyIndex).getFillPaint()));

        }

        // Randomize the order of the shape list
        Collections.shuffle(shapeList);

        // Paint them on the canvas
        double sqrtObjectCount = Math.sqrt(objectCount);
        int gridWidth = (int) (canvas.getWidth() / (sqrtObjectCount+1));
        int gridHeight = (int) (canvas.getHeight() / (sqrtObjectCount+1));

        // For each of the required shapes, run through the list and draw them in a grid
        for(int x = gridWidth, y = gridHeight, i = 0; i < shapeList.size(); i++, x += gridWidth) {

            // if end of row then go to next row and reset to column 1
            if (x >= canvas.getWidth() - 20) {
                x = gridWidth;
                y += gridHeight;
            }

            // Draw ghost behind the place where the mark will appear
            CRectangle ghost = canvas.newRectangle(x, y, (10), (10));
            ghost.setFillPaint(Color.lightGray);
            ghost.setOutlined(false);
            ghost.addTag(ghosts);

            // Move to the correct location and add to canvas
            shapeList.get(i).translateTo(x, y);
            canvas.addShape(shapeList.get(i));

            // Add tag
            CRectangle currentMark = (CRectangle) shapeList.get(i);
            currentMark.addTag(visualMarks);
            currentMark.setOutlined(false);


        }

        // Always set the tag of the target
        targetMark.addTag(target);
        targetMark.removeTag(visualMarks);

	}

	// Create shapes for adding to list
	protected CRectangle createShape(int size, Color colour) {

		CRectangle shape = new CRectangle(0, 0, size, size);
		shape.setFillPaint(colour);
		shape.addTag(visualMarks);

		return shape;
	}
	
	public void displayInstructions() {
		Canvas canvas = experiment.getCanvas();
		CText text1 = canvas.newText(0, 0, "A scene with multiple shapes will get displayed", Experiment.FONT);
		CText text2 = canvas.newText(0, 50, "Identify the shape that is different from all other shapes", Experiment.FONT);
		CText text3 = canvas.newText(0, 100, "    1. Press Space bar", Experiment.FONT);
		CText text4 = canvas.newText(0, 150, "    2. Click on the identified shape", Experiment.FONT);
		CText text5 = canvas.newText(0, 200, "Do it AS FAST AND AS ACCURATELY AS POSSIBLE", Experiment.FONT);
		CText text6 = canvas.newText(0, 350, "--> Press Enter key when ready", Experiment.FONT.deriveFont(Font.PLAIN, 15));
		text1.addTag(instructions);
		text2.addTag(instructions);
		text3.addTag(instructions);
		text4.addTag(instructions);
		text5.addTag(instructions);
		text6.addTag(instructions);
		double textCenterX = instructions.getCenterX();
		double textCenterY = instructions.getCenterY();
		double canvasCenterX = canvas.getWidth()/2;
		double canvasCenterY = canvas.getHeight()/2;
		double dx = canvasCenterX - textCenterX;
		double dy = canvasCenterY - textCenterY;
		instructions.translateBy(dx, dy);
		canvas.setAntialiased(true);

		canvas.requestFocus();
        canvas.addKeyListener(enterListener);
	}
}

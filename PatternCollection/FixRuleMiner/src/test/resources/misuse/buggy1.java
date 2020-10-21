package com.retroMachines.util.lambda;

import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;
import com.retroMachines.game.gameelements.GameElement;
import com.retroMachines.util.Constants;
import com.retroMachines.util.Constants.RetroStrings;

/**
 * This class is part of the model of RetroMachines. 
 * Vertices of the Graph of Gameelements extends this class
 * 
 * @author RetroFactory
 * 
 */
public abstract class Vertex {
	
	/**
	 * Reference to next input.
	 */
	private Vertex next;
	
	/**
	 * Reference to the output tree.
	 */
	private Vertex family;
	
	/**
	 * unique id of the color of Variable.
	 */
	private int color;
	
	/**
	 * unique identifier of this very vertex
	 */
	private int id;
	
	private Vector2 pos;
	
	private boolean isInDepot;
	
	protected GameElement gameElement;
	
	/**
	 * the width of this vertex as number of Vertex in his family
	 */
	private int width;
	
	/**
	 * Width of all Vertex next to this
	 */
	private int nextWidth;
	
	private static HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
	
	/**
	 * List of all color's of vertices corresponding to this abstraction.
	 * color's are sorted after their size small to big 
	 */
	private LinkedList<Integer> familyColorList;
	
	/**
	 * List of all color's of vertices corresponding to this abstraction.
	 * color's are sorted after their size small to big 
	 */
	private LinkedList<Integer> nextColorList;
	
	// --------------------------
	// --------Constructor-------
	// --------------------------
	
	/**
	 * Default Public Constructor for Dummy Element
	 */
	public Vertex() {
		isInDepot = false;
	}
	
	/**
	 * Creates a new instance of the Vertex class.
	 * 
	 * @param color
	 *            color to set.
	 */
	public Vertex(int id, int color) {
		this.id = id;
		this.color = color;
		this.familyColorList = new LinkedList<Integer>();
		this.nextColorList = new LinkedList<Integer>();
		this.familyColorList.add(color);
		this.width = 1;
		this.nextWidth = 0;
		updateMap(color, color); //vertex is not mapped yet
	}	
	
	// --------------------------
	// ---------Methods----------
	// --------------------------
	
	/**
	 * Replaces the oldId with the newId in the IdList.
	 * It  is needed when a parent Abstraction performs a Beta-reduction or an Alpha-conversion.
	 * @param oldId ID which should be replaced.
	 * @param newId ID which should take place of the former ID.
	 * @return true if the IdList contained the oldId and it was changed successfully.
	 */
	public boolean updateIdList(int oldId, int newId) {
		boolean updated = false;
		for(Integer id : familyColorList) {
			if (id == oldId) {
				id = newId;
				updated = true;
				//break;
			}
		}
		return updated;
	}
	
	/**
	 * method to set new color for an vertex (e.g after alphaConversion)
	 * @param vertexColor vertex, which color has changed
	 * @param mappedColor new color of vertex
	 */
	protected static void updateMap(int vertexColor, int mappedColor) {
		colorMap.put(vertexColor, mappedColor);
	}
	
	/**
	 * returns mapped color of vertex
	 * @param vertexColor original Color of vertex
	 * @return mapped color of vertex
	 */
	protected static int getMappedColor(int vertexColor) {
		return colorMap.get(vertexColor);
	}
	
	/**
	 * Add element to IdList
	 * @param addId id which should be added
	 */
	public void addFamilyIdList(int addId){
		if (familyColorList.contains(addId)) {
			return; //list already contains id
		}
		int index = 0;
		for (int id : familyColorList) {
			if (id > addId) {
				index = familyColorList.indexOf(id);
				break;
			}
		}
		if (index == 0) {
			familyColorList.addFirst(addId);
		} else {
			familyColorList.add(index - 1, addId);
		}		
	}
	
	
	/**
	 * updates start vertex. This vertex if family is null.
	 * @return
	 */
	public Vertex updateStart() {
		if (family == null) {
			return this;
		} else {
			return family;
		}
	}

	//---------------------------------------------------
	//-------- Beta Reduction and Alpha Conversion ------
	//------------------Help Methods---------------------
	//---------------------------------------------------
	
	/**
	 * replace OldColor with newColor in Hole Family
	 * @param oldColor Color which should be replaced
	 * @param newColor Color which should take place of OldColor
	 * @return true if renamed family successful, false otherwise
	 */
	protected boolean renameFamily(int oldColor, int newColor) {
		System.out.println("rename" + oldColor + " " + newColor);
		int index = 0;
		// Get Index of oldColor 
		while(this.familyColorList.get(index) < oldColor) {
			index++;
		}
		
		// Replace Color 
		if (this.familyColorList.get(index) == oldColor) {
			
			// Replace Color in family Color List
			this.familyColorList.set(index, newColor);
			
			// Replace own Color if needed
			if (this.color == oldColor){
				this.color = newColor;
				int offset = (Integer) this.getGameElement().getTileSet().getProperties().get("firstgid") - 1;
				this.getGameElement().setTileId(newColor + offset);
			}
			
			// Rename Family
			if (this.family != null) {
				
				// Rename First in Family
				if (this.family.renameFamily(oldColor, newColor)) {
					//Error
					return false;
				}
				
				// Rename the Others if they are no imaginary Friends  
				Vertex renamePointer = new Dummy();
				renamePointer.setnext(this.getfamily());
				while(renamePointer.getnext() != null) {
					if (!renamePointer.getnext().renameFamily(oldColor, newColor)) {
						//Error
						return false;
					}
					// Set 
					renamePointer.setnext(renamePointer.getnext().getnext());
				}
			}
		}
		return true;
	}
	
	/**
	 * returns GameElement according to this vertex
	 * @return
	 */
	abstract public GameElement getGameElement();
	
	/**
	 * replaces all Elements of a specific color in family of start Vertex
	 * @param start vertex which is parent of this Vertex and starts the beta Reduction
	 * @return
	 */
	protected LinkedList<Vertex> replaceInFamily(Vertex start) {
		
		LinkedList<Vertex> listOfNewVertex = new LinkedList<Vertex>();
		
		// if family contains color, search and replace it
		if (this.getFamilyColorList().contains(start.getColor())) {

			if (this.getfamily() != null) {
				// Check Family Vertexes before you check to replace the first in Family
				listOfNewVertex = this.getfamily().replaceInFamily(start);
				
				
				//Replace Family Vertex if Color and Type are ok
				if (this.getfamily().getType().equals("Variable") && this.getfamily().getColor() == start.getColor()) {
					Vertex replaced = start.getnext().cloneMe();
					
					//Update listOfNewVertex
					LinkedList<Vertex> cloneList = replaced.getVertexList();
					for(Vertex v : cloneList) {
						listOfNewVertex.add(v);
					}
					
					// Insert clone in Family
					Vector2 position = new Vector2(start.getGameElement().getPosition().x, start.getGameElement().getPosition().y);
					position.x += Constants.ABSTRACTION_OUTPUT;
					position.y += Constants.GAMEELEMENT_ANIMATION_WIDTH;
					
					//Animation
					EvaluationOptimizer.MoveAndScaleAnimationWithoutDelay(position, this.getfamily().getGameElement(), false);
					
					if(this.getfamily().getnext() != null) {
						replaced.setnext(this.getfamily().getnext());
					}
					this.setfamily(replaced);
				}
			}
		}
		
		// if next Vertex contains color, search and replace it 
		if (this.getnext() != null) {
			
			// Check all Vertexes next to you, before you check to replace the Next Vertex
			LinkedList<Vertex> listOfNextVertex = this.getnext().replaceInFamily(start);
			for(Vertex v : listOfNextVertex) {
				listOfNewVertex.add(v);
			}
			
			//Replace Next Vertex if Color and Type are Ok
			if (this.getnext().getType().equals("Variable") && this.getnext().getColor() == start.getColor()) {
				Vertex replaced = start.getnext().cloneMe();
				
				//Update listOfNewVertex
				LinkedList<Vertex> cloneList = replaced.getVertexList();
				for(Vertex v : cloneList) {
					listOfNewVertex.add(v);
				}
				
				// insert clone as Next
				Vector2 position = new Vector2(start.getGameElement().getPosition().x, start.getGameElement().getPosition().y);
				position.x += Constants.ABSTRACTION_OUTPUT;
				position.y += Constants.GAMEELEMENT_ANIMATION_WIDTH;
				
				//Animation
				EvaluationOptimizer.MoveAndScaleAnimationWithoutDelay(pos, this.getnext().getGameElement(), false);
				
				if(this.getnext().getnext() != null) {
					replaced.setnext(this.getnext().getnext());
				}
				
				this.setnext(replaced);
			}
		}
		// At the End return the ColorList, if something is replaced;
		return listOfNewVertex;
	}
	
	
	/**
	 * Creates a clone of this Vertex without Next and his hole Family
	 * @return deep copy of next
	 */
	abstract public Vertex cloneMe();
	
	/**
	 * Creates a clone of this Vertex and his hole Family
	 * @return clone of Vertex with hole family and next coned
	 */
	abstract public Vertex cloneFamily();
	
	abstract public String getType();
	
	
	
	/**
	 * Removes Gameelement of this vertex and all family vertex from stage 
	 * @return true = success
	 */
	public boolean removeGameelements() {
		boolean check = true;
		// remove Family
		if(this.getfamily() != null) {
			if(!this.getfamily().removeAllGameelements()) {
				check = false;
			}
		}
					
		// remove yourself
		if (this.getGameElement().remove()) {
			check = false;
		}
		return check;
	}
	
	private boolean removeAllGameelements() {
		boolean check = true;
			// Remove all next
			if(this.getnext() != null) {
				if (!this.getnext().removeAllGameelements()) {
					check = false;
				}
			}
			
			// remove Family
			if(this.getfamily() != null) {
				if(!this.getfamily().removeAllGameelements()) {
					check = false;
				}
			}
			
			// remove yourself
			if (this.getGameElement().remove()) {
				check = false;
			}
			return check;
	}
	
	/**
	 * Removes all Gameelemnts with type Variable and color of this vertex in family of this vertex
	 * @return true = success
	 */
	public boolean removeVariableGameelements() {
		return this.getfamily().removeVariableFamily(this.getColor());
	}
	
	private boolean removeVariableFamily(int color) {
		
		boolean result = true;
		if(this.getnext() != null) {
			if(!this.getnext().removeVariableFamily(color)) {
				result = false;
			}
		}
		if(this.getfamily() != null) {
			if(!this.getfamily().removeVariableFamily(color)) {
				result = false;
			}
		}
		if(this.getType().equals(RetroStrings.VARIABLE_TYPE) && this.getColor() == color) {
			if(!this.getGameElement().remove()) {
				result = false;
			}
		}
		
		return result;
		
	}
	
	/**
	 * returns List of Vertex and his hole Family Vertex
	 * @return List of Vertex
	 */
	protected LinkedList<Vertex> getVertexList() {
		LinkedList<Vertex> returnList = new LinkedList<Vertex>();
		
		// Add family
		if(this.getfamily() != null) {
			returnList = this.getfamily().getFamilyVertexList();
		}
		returnList.add(this);
		return returnList;
	}
	
	private LinkedList<Vertex> getFamilyVertexList() {
		LinkedList<Vertex> returnList = new LinkedList<Vertex>();
		if(this.getnext() != null) {
			returnList = this.getnext().getFamilyVertexList();
		}
		
		// Add family
		if(this.getfamily() != null) {
			if(returnList.isEmpty()) {
				returnList = this.getfamily().getFamilyVertexList();
			} else {
				LinkedList<Vertex> familyList = this.getfamily().getFamilyVertexList();
				for(Vertex v : familyList) {
					returnList.add(v);
				}
			}

		}
		returnList.add(this);
		return returnList;
	}
	
	/**
	 * Updates ColorList of this Vertex and family
	 */
	protected void updateColorList(LinkedList<Integer> cloneList, int color) {
		// Update Color List in vertex
		if(this.getFamilyColorList().contains(new Integer(color))) {
			this.getFamilyColorList().remove(new Integer(color));
			for(Integer i : cloneList) {
				this.getFamilyColorList().add(i);
			}
			if(this.getfamily() != null) {
				this.getfamily().updateFamilyColorList(cloneList, color);
			}
		}
	}
	
	private void updateFamilyColorList(LinkedList<Integer> cloneList, int color) {
		// Update Color List in vertex
		if(this.getFamilyColorList().contains(new Integer(color))) {
			this.getFamilyColorList().remove(new Integer(color));
			for(Integer i : cloneList) {
				this.getFamilyColorList().add(i);
			}
			if(this.getfamily() != null) {
				this.getfamily().updateFamilyColorList(cloneList, color);
			}
		}
		
		// Update Color List in next
		if(this.getNextColorList().contains(new Integer(color))) {
			if(this.getnext() != null) {
				this.getnext().updateFamilyColorList(cloneList, color);
			}
			this.setNextColorlist(this.getnext().getCopyOfNextColorList());
			if(!this.getNextColorList().contains(new Integer(this.getnext().getColor()))) {
				this.getNextColorList().add(this.getnext().getColor());
			}
		}
	}
	
	public void updateWidth() {
		//Update width
		if(this.getfamily() != null) {
			this.getfamily().updateFamilyWidth();
			this.setWidth(this.getfamily().getWidth() + this.getfamily().getNextWidth());
		} else {
			this.setWidth(0);
		}
	}
	
	private void updateFamilyWidth() {
		//Update width
		if(this.getfamily() != null) {
			this.getfamily().updateFamilyWidth();
			this.setWidth(this.getfamily().getWidth() + this.getfamily().getNextWidth());
		} else {
			this.setWidth(1);
		}
		
		// Update next width
		if(this.getnext() != null) {
			this.getnext().updateFamilyWidth();
			this.setNextWidth(this.getnext().getWidth() + this.getnext().getNextWidth());
		} else {
			this.setNextWidth(0);
		}
		
		//Update self
	}
	
	/**
	 * Updates Pointer after Beta Redction and returns new Worker
	 * @return new Worker
	 */
	abstract public Vertex updatePointerAfterBetaReduction();
	
	/**
	 * Returns Verex with should be added to the ResultTree
	 * @return Returns null if ther is no Vertex wich should be added
	 */
	abstract public Vertex getEvaluationResult();
	
	/**
	 * Update Position of Family if Worker was deleted in BetaReduction
	 */
	abstract public void UpdatePositionsAfterBetaReduction();
	//---------------------------------------------------
	//-------- Beta Reduction and Alpha Conversion ------
	//---------------------------------------------------
	
	/**
	 * Fulfills one step of beta-reduction.
	 * 
	 * @return True if this abstraction has changed, false when an error appeared.
	 */
	abstract public LinkedList<Vertex> betaReduction();
	
	/**
	 * Fulfills alpha conversion. Makes sure that all vertices have unique ID's.
	 * 
	 * @return True if at least one ID has changed, false if no ID has changed.
	 */
	abstract public boolean alphaConversion();
	
	//----------------------------------------
	//-------- Animation Helper Methods ------
	//----------------------------------------
	
	/**
	 * return vertex witch will be replaced in beta Reduction
	 * @return return null if no vertex should be read in
	 */
	abstract public Vertex getReadIn();
	
	/**
	 * Read In Animation for Vertex and his family
	 * @param pos Position of Worker
	 * @param e EvaluationController where next step should be called
	 */
	public void readInAnimation(Vector2 pos) {
		
		//Update Position Input
		pos.x += Constants.GAMEELEMENT_ANIMATION_WIDTH;
		pos.y += Constants.ABSTRACTION_INPUT;
		
		if(this.getfamily() != null) {
			this.getfamily().readInFamilyAnimation(pos);
		}
		//Animation
		EvaluationOptimizer.MoveAndScaleAnimation(pos, this.getGameElement(), true);
		
	}
	
	protected void readInFamilyAnimation(Vector2 pos) {
		if(this.getnext() != null) {
			this.getnext().readInFamilyAnimation(pos);
		}
		if(this.getfamily() != null) {
			this.getfamily().readInFamilyAnimation(pos);
		}
		
		EvaluationOptimizer.MoveAndScaleAnimation(pos, this.getGameElement(), false);
	}
	
	/**
	 * update coordinate of gameelement with given difference(Num of Gameelemnts)
	 * @param difX dif on x axis
	 * @param difY dif on y axis
	 */
	public void updateGameelementPosition(int difX, int difY) {
		if(this.getnext() != null) {
			this.getnext().updateOtherGameelementPosition(difX, difY);
		}
		
		if (this.getfamily() != null) {
			this.getfamily().updateOtherGameelementPosition(difX, difY);
		}
		
		// Move
		Vector2 actPosition = this.getGameElement().getPosition();
		int newX = (int)actPosition.x + (Constants.GAMEELEMENT_WIDTH * difX);
		int newY = (int)actPosition.y + (Constants.GAMEELEMENT_WIDTH * difY);
		// Start next evaluationStep
		
		EvaluationOptimizer.MoveAnimation(new Vector2(newX, newY), this.getGameElement(), true);
	}
	
	private void updateOtherGameelementPosition(int difX, int difY) {
		if(this.getnext() != null) {
			this.getnext().updateOtherGameelementPosition(difX, difY);
		}
		
		
		// Move
		Vector2 actPosition = this.getGameElement().getPosition();
		int newX = (int)actPosition.x + (Constants.GAMEELEMENT_WIDTH * difX);
		int newY = (int)actPosition.y + (Constants.GAMEELEMENT_WIDTH * difY);
		
		EvaluationOptimizer.MoveAnimation(new Vector2(newX, newY), this.getGameElement(), false);
		
		if (this.getfamily() != null) {
			this.getfamily().updateOtherGameelementPosition(difX, difY);
		}
	}
	
	/**
	 * Reorganizese Position of Vertex if needed
	 * @param start  Offset of positon perhaps Padding to border ...
	 * @param newPos new Position of this Vertex in num Of GameelementWidths
	 * @param e instance of evaluationController for next steps
	 */
	abstract public void reorganizePositions(Vector2 start,Vector2 newPos);
	
	/**
	 * Set Gameelement and family to given 
	 * @param newPos as Number of GameelementWidths
	 */
	protected void setGameelementPosition(Vector2 start,Vector2 newPos) {
		
		int centerVertex = (Constants.GAMEELEMENT_WIDTH * (this.getWidth() - 1)) / 2;
		int x = Constants.GAMEELEMENT_WIDTH * (int)newPos.x + (int)start.x; 
		int y =	Constants.GAMEELEMENT_WIDTH * (int)newPos.y + (int)start.y;
		
		Vector2 startPos = new Vector2(x + centerVertex, y);
		
		if(this.getnext() != null) {
			this.getnext().setOtherGameelementPosition(new Vector2(newPos.x + this.getWidth(), newPos.y), startPos);
		}
		
		if (this.getfamily() != null) {
			this.getfamily().setOtherGameelementPosition(new Vector2(newPos.x, newPos.y + 1), startPos);
		}
		
		// Move	
		EvaluationOptimizer.MoveAnimation(startPos, this.getGameElement(), true);
	}
	
	/**
	 * Set Gameelement and family to given 
	 * @param newPos as Number of GameelementWidths
	 */
	private void setOtherGameelementPosition(Vector2 newPos, Vector2 startPos) {
		
		if(this.getnext() != null) {
			this.getnext().setOtherGameelementPosition(new Vector2(newPos.x + this.getWidth(), newPos.y), startPos);
		}
		
		// Clone Pre Set
		if(this.getGameElement().getPosition().equals(new Vector2(0,0))) {
			this.getGameElement().setPosition(startPos);
		}
		
		// Move
		int centerVertex = (Constants.GAMEELEMENT_WIDTH * (this.getWidth() - 1)) / 2;
		int x = Constants.GAMEELEMENT_WIDTH * (int)newPos.x + Constants.EVALUATIONSCREEN_PADDING; 
		int y =	Constants.GAMEELEMENT_WIDTH * (int)newPos.y + Constants.EVALUATIONSCREEN_PADDING;
		
		// Move	
		EvaluationOptimizer.MoveAnimation(new Vector2(x + centerVertex, y), this.getGameElement(), false);
		
		if (this.getfamily() != null) {
			this.getfamily().setOtherGameelementPosition(new Vector2(newPos.x, newPos.y + 1), startPos);
		}
	}
	
	/**
	 * compares this vertex with given one
	 * @param v vertex to be compared with this
	 * @return returns true if and only if this vertex and parameter have same color and same type 
	 */
	public boolean equals(Vertex v) {
		if (v == null) {
			return false;
		}
		if (this.getType().equals(v.getType()) && this.getColor() == v.getColor()) {
			if (this.family != null) {
				return this.equals(v.family);
			} else {
				return v.family == null;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Removes Gameelement from screen if this type of Vertex needs it
	 * @param e Instance of Evaluation Controller for next Steps
	 */
	abstract public void DeleteAfterBetaReduction();
	
	
	// --------------------------
	// ------Setter-------
	// --------------------------

	
	/**
	 * Setter for next Vertex in the lambda-tree.
	 * 
	 * @param next
	 * 				Next vertex that is to set.
	 */
	public void setnext(Vertex next) {
		this.next = next;
	}
	
	/**
	 * Setter for the family tree of this vertex.
	 * 
	 * @param family 
	 * 				The start vertex for the family that is to set.
	 * @return
	 * 				false if type of Vertex is Variable , true otherwise
	 */
	public boolean setfamily(Vertex family) {
		this.family = family;
		return true;
	}

	/**
	 * Setter for the Color.
	 * 
	 * @param color
	 *          	Color that is to set.
	 */
	public void setColor(int color) {
		this.color = color;
	}
	
	/**
	 * Setter for the familyColorList
	 * 
	 * @param familyColorList
	 * 				FamilyColorList that is to set
	 */
	public void setFamilyColorlist(LinkedList<Integer> familyColorList) {
		this.familyColorList = familyColorList;
	}
	
	/**
	 * Setter for the nextColorList
	 * 
	 * @param nextColorList
	 * 				NextColorList that is to set
	 */
	public void setNextColorlist(LinkedList<Integer> nextColorList) {
		this.nextColorList = nextColorList;
	}
	
	public void setPosition(Vector2 p) {
		pos = p;
	}
	
	public void setIsInDepot(boolean i) {
		isInDepot = i;
	}
	
	/**
	 * Getter for the familyColorList
	 * 
	 * @return The familyColorList of this Vertex
	 */
	public LinkedList<Integer> getFamilyColorList(){
		return familyColorList;
	}
	
	/**
	 * Getter for the familyColorList
	 * 
	 * @return The familyColorList of this Vertex
	 */
	public LinkedList<Integer> getCopyOfFamilyColorList(){
		LinkedList<Integer> copyList = new LinkedList<Integer>();
		for(Integer i : familyColorList) {
			copyList.add(i);
		}
		return copyList;
	}
	
	/**
	 * Getter for the nextColorList
	 * 
	 * @return The nextColorList of this Vertex
	 */
	public LinkedList<Integer> getNextColorList(){
		return nextColorList;
	}
	
	/**
	 * Getter for the nextColorList
	 * 
	 * @return The nextColorList of this Vertex
	 */
	public LinkedList<Integer> getCopyOfNextColorList(){
		LinkedList<Integer> copyList = new LinkedList<Integer>();
		for(Integer i : nextColorList) {
			copyList.add(i);
		}
		return copyList;
	}
	
	public boolean isInDepot() {
		return isInDepot;
	}
	
	/**
	 * Getter for the Color.
	 * 
	 * @return The current Color of the vertex.
	 */
	public int getColor() {
		return getMappedColor(color);
	}
	
	/**
	 * Getter for the family tree of this vertex.
	 * 
	 * @return The family tree of this vertex.
	 */
	public Vertex getfamily() {
		return family;
	}
	
	/**
	 * Getter for next Vertex in lambda-tree.
	 * 
	 * @return The next Vertex in the lambda-tree.
	 */
	public Vertex getnext() {
		return next;
	}
	
	public Vector2 getPosition() {
		return pos;
	}

	public int getId() {
		return id;
	}
	
	public void setWidth(int w) {
		width = w;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setNextWidth(int w) {
		nextWidth = w;
	}
	
	public int getNextWidth() {
		return nextWidth;
	}
}


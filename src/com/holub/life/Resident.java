package com.holub.life;

import java.awt.*;

import com.holub.ui.Colors;	// Contains constants specifying various
							// colors not defined in java.awt.Color.
import com.holub.visitor.CellVisitor;

/*** ****************************************************************
 * The Resident class implements a single cell---a "resident" of a
 * block.
 *
 * @include /etc/license.txt
 */

public final class Resident implements Cell
{
	private static final Color BORDER_COLOR = Colors.DARK_YELLOW;
	private static final Color LIVE_COLOR 	= Color.RED;
	private static final Color DEAD_COLOR   = Colors.LIGHT_YELLOW;
	// Color for History
	private static final Color HISTORY_COLOR = Colors.MEDIUM_ORANGE;

	private boolean amAlive 	= false;
	private boolean willBeAlive	= false;
	// History 상태 (자리 지나갔는지)

	private boolean hasPassed = false;

	private boolean isStable(){return amAlive == willBeAlive; }

	/** figure the next state.
	 *  @return true if the cell is not stable (will change state on the
	 *  next transition().
	 */
	public boolean figureNextState(
							Cell north, 	Cell south,
							Cell east, 		Cell west,
							Cell northeast, Cell northwest,
							Cell southeast, Cell southwest )
	{
		verify( north, 		"north"		);
		verify( south, 		"south"		);
		verify( east, 		"east"		);
		verify( west, 		"west"		);
		verify( northeast,	"northeast"	);
		verify( northwest,	"northwest" );
		verify( southeast,	"southeast" );
		verify( southwest,	"southwest" );

		int neighbors = 0;

		if( north.	  isAlive()) ++neighbors;
		if( south.	  isAlive()) ++neighbors;
		if( east. 	  isAlive()) ++neighbors;
		if( west. 	  isAlive()) ++neighbors;
		if( northeast.isAlive()) ++neighbors;
		if( northwest.isAlive()) ++neighbors;
		if( southeast.isAlive()) ++neighbors;
		if( southwest.isAlive()) ++neighbors;

		willBeAlive = (neighbors==3 || (amAlive && neighbors==2));
		//if(!hasPassed)
		//	hasPassed = amAlive;
		return !isStable();
	}

	private void verify( Cell c, String direction )
	{	assert (c instanceof Resident) || (c == Cell.DUMMY)
				: "incorrect type for " + direction +  ": " +
				   c.getClass().getName();
	}

	/** This cell is monetary, so it's at every edge of itself. It's
	 *  an internal error for any position except for (0,0) to be
	 *  requsted since the width is 1.
	 */
	public Cell	edge(int row, int column)
	{	assert row==0 && column==0;
		return this;
	}

	public boolean transition()
	{	boolean changed = isStable();
		if(!hasPassed)
			hasPassed = amAlive;
		amAlive = willBeAlive;
		return changed;
	}

	public void redraw(Graphics g, Rectangle here, boolean drawAll)
    {   g = g.create();
		g.setColor(hasPassed ? HISTORY_COLOR : DEAD_COLOR);
		g.fillRect(here.x+1, here.y+1, here.width-1, here.height-1);
		g.setColor(amAlive ? LIVE_COLOR : (hasPassed ? HISTORY_COLOR : DEAD_COLOR) );
		g.fillRect(here.x+1, here.y+1, here.width-1, here.height-1);

		// Doesn't draw a line on the far right and bottom of the
		// grid, but that's life, so to speak. It's not worth the
		// code for the special case.

		g.setColor( BORDER_COLOR );
		g.drawLine( here.x, here.y, here.x, here.y + here.height );
		g.drawLine( here.x, here.y, here.x + here.width, here.y  );
		g.dispose();
	}

	public void userClicked(Point here, Rectangle surface){
		this.reverse();
	}

	public void reverse(){
		amAlive = !amAlive;
	}

	public int getPixelsPerResident(Rectangle surface){
		int pixelsPerCell = surface.width;
		return pixelsPerCell;
	};

	public void accept(CellVisitor visitor){
		visitor.visit(this);
	}

	public void	   clear()			{amAlive = willBeAlive = hasPassed = false; }
	public boolean isAlive()		{return amAlive;			    }
	public Cell    create()			{return new Resident();			}
	public int 	   widthInCells()	{return 1;}

	public Direction isDisruptiveTo()
	{	return isStable() ? Direction.NONE : Direction.ALL ;
	}

	public boolean transfer(Storable blob,Point upperLeft,boolean doLoad)
	{
		Memento memento = (Memento)blob;
		if( doLoad )
		{	if( amAlive = willBeAlive = memento.isAlive(upperLeft) )
				return true;
		}
		else if( amAlive )  					// store only live cells
			memento.markAsAlive( upperLeft );


		return false;
	}

	@Override
	public Cell[][] getGrid() {
		return getGrid();
	}
	@Override
	public boolean isActive(int row, int col){
		return amAlive;
	}

	@Override
	public void makeActive(int row, int col){
		amAlive = true;
	}

	@Override
	public boolean isHasPassed(int row, int col) {return hasPassed;}



	/** Mementos must be created by Neighborhood objects. Throw an
	 *  exception if anybody tries to do it here.
	 */
	public Storable createMemento()
	{	throw new UnsupportedOperationException(
					"May not create memento of a unitary cell");
	}
}

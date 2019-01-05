package core.common.ui;

import java.util.ArrayList;

import core.scenegraph.RenderList;

public abstract class GUI {

	private ArrayList<UIScreen> screens = new ArrayList<UIScreen>();
	
	
	public void update(){
		
		screens.forEach(screen -> screen.update());
	}
	
	public void render(){
		
		screens.forEach(screen -> screen.render());
	}
	
	public void record(RenderList renderList){

		screens.forEach(screen -> screen.record(renderList));
	}
	
	public void shutdown(){

		screens.forEach(screen -> screen.shutdown());
	}

	public ArrayList<UIScreen> getScreens() {
		return screens;
	}
	
}

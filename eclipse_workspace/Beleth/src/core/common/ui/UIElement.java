package core.common.ui;

import core.math.Matrix4f;
import core.scenegraph.Renderable;

public abstract class UIElement extends Renderable{

	protected Matrix4f orthographicMatrix;
	
	public UIElement(int xPos, int yPos, int xScaling, int yScaling){
		super();
		setOrthographicMatrix(new Matrix4f().Orthographic2D());
		getWorldTransform().setTranslation(xPos, yPos, 0);
		getWorldTransform().setScaling(xScaling, yScaling, 0);
		setOrthographicMatrix(getOrthographicMatrix().mul(getWorldTransform().getWorldMatrix()));
	}
	
	@Override
	public void update(){};
	
	public void update(String text){}

	public Matrix4f getOrthographicMatrix() {
		return orthographicMatrix;
	}

	public void setOrthographicMatrix(Matrix4f orthographicMatrix) {
		this.orthographicMatrix = orthographicMatrix;
	};
	
}

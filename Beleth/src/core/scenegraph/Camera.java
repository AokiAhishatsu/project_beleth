package core.scenegraph;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;

import core.context.BaseContext;
import core.math.Matrix4f;
import core.math.Vec3f;
import core.math.Vec4f;
import core.platform.Input;
import core.util.BufferUtil;
import core.util.Constants;
import core.util.Util;

public abstract class Camera {
	
	private final Vec3f yAxis = new Vec3f(0,1,0);
	
	private Input input;
	private Vec3f position;
	private Vec3f previousPosition;
	private Vec3f forward;
	private Vec3f previousForward;
	private Vec3f up;
	private float movAmt = 0.1f;
	private float rotAmt = 0.4f;
	private Matrix4f viewMatrix;
	private Matrix4f projectionMatrix;
	private Matrix4f viewProjectionMatrix;
	private Matrix4f previousViewMatrix;
	private Matrix4f previousViewProjectionMatrix;
	private boolean isCameraMoved;
	private boolean isCameraRotated;
	
	private float width;
	private float height;
	private float fovY;

	private float rotYstride;
	private float rotYamt = 0;
	private float rotXstride;
	private float rotXamt = 0;
	private float mouseSensitivity = 0.008f;
	private boolean isUpRotation;
	private boolean isDownRotation;	
	private boolean isLeftRotation;	
	private boolean isRightRotation;	
	
	private Vec4f[] frustumPlanes = new Vec4f[6];
	private Vec3f[] frustumCorners = new Vec3f[8];
	
	protected FloatBuffer floatBuffer;
	protected final int bufferSize = Float.BYTES * (4+16+16+(6*4));
	
	protected Camera(Vec3f position, Vec3f forward, Vec3f up)
	{
		setPosition(position);
		setForward(forward.normalize());
		setUp(up.normalize());
		setProjection(70, BaseContext.getConfig().getX_ScreenResolution(), BaseContext.getConfig().getY_ScreenResolution());
		setViewMatrix(new Matrix4f().View(this.getForward(), this.getUp()).mul(new Matrix4f().Translation(this.getPosition().mul(-1))));
		initfrustumPlanes();
		previousViewMatrix = new Matrix4f().Zero();
		previousViewProjectionMatrix = new Matrix4f().Zero();
		floatBuffer = BufferUtil.createFloatBuffer(bufferSize);
		setViewProjectionMatrix(getProjectionMatrix().mul(getViewMatrix()));
		
		input = BaseContext.getInput();
	}
	
	public abstract void init();
	
	public abstract void shutdown();
	
	public void update(){
		
		setPreviousPosition(new Vec3f(getPosition()));
		setPreviousForward(new Vec3f(getForward()));
		setCameraMoved(false);
		setCameraRotated(false);
		
		setMovAmt(getMovAmt() + (0.04f * input.getScrollOffset()));
		setMovAmt(Math.max(0.02f, getMovAmt()));
		
		if(input.isKeyHolding(GLFW_KEY_W))
			move(getForward(), getMovAmt());
		if(input.isKeyHolding(GLFW_KEY_S))
			move(getForward(), -getMovAmt());
		if(input.isKeyHolding(GLFW_KEY_A))
			move(getLeft(), getMovAmt());
		if(input.isKeyHolding(GLFW_KEY_D))
			move(getRight(), getMovAmt());
				
		if(input.isKeyHolding(GLFW_KEY_UP))
			rotateX(-getRotAmt()/8f);
		if(input.isKeyHolding(GLFW_KEY_DOWN))
			rotateX(getRotAmt()/8f);
		if(input.isKeyHolding(GLFW_KEY_LEFT))
			rotateY(-getRotAmt()/8f);
		if(input.isKeyHolding(GLFW_KEY_RIGHT))
			rotateY(getRotAmt()/8f);
		
		// free mouse rotation
//		if(true){
//			
//			float dy = input.getLockedCursorPosition().getY() - input.getCursorPosition().getY();
//			float dx = input.getLockedCursorPosition().getX() - input.getCursorPosition().getX();
//			
//			// y-axxis rotation
//			if (dy != 0){
//				setRotYamt(getRotYamt() - dy);
//				setRotYstride(Math.abs(getRotYamt() * 0.1f));
//			}
//			
//			if (getRotYamt() != 0 || getRotYstride() != 0){
//				
//				// up-rotation
//				if (getRotYamt() < 0){
//					setUpRotation(true);
//					setDownRotation(false);
//					rotateX(-getRotYstride() * getMouseSensitivity());
//					setRotYamt(getRotYamt() + getRotYstride());
//					if (getRotYamt() > 0)
//						setRotYamt(0);
//				}
//				// down-rotation
//				if (getRotYamt() > 0){
//					setUpRotation(false);
//					setDownRotation(true);
//					rotateX(getRotYstride() * getMouseSensitivity());
//					setRotYamt(getRotYamt() - getRotYstride());
//					if (getRotYamt() < 0)
//						setRotYamt(0);
//				}
//				// smooth-stop
//				if (getRotYamt() == 0){
//					setRotYstride(getRotYstride() * 0.95f);
//					if (isUpRotation())
//						rotateX(-getRotYstride() * getMouseSensitivity());
//					if (isDownRotation())
//						rotateX(getRotYstride() * getMouseSensitivity());
//					if (getRotYstride() < 0.001f)
//						setRotYstride(0);
//				}
//			}
//			
//			// x-axxis rotation
//			if (dx != 0){
//				setRotXamt(getRotXamt() + dx);
//				setRotXstride(Math.abs(getRotXamt() * 0.1f));
//			}
//			
//			if (getRotXamt() != 0 || getRotXstride() != 0){
//				
//				// right-rotation
//				if (getRotXamt() < 0){
//					setRightRotation(true);
//					setLeftRotation(false);
//					rotateY(getRotXstride() * getMouseSensitivity());
//					setRotXamt(getRotXamt() + getRotXstride());
//					if (getRotXamt() > 0)
//						setRotXamt(0);
//				}
//				// left-rotation
//				if (getRotXamt() > 0){
//					setRightRotation(false);
//					setLeftRotation(true);
//					rotateY(-getRotXstride() * getMouseSensitivity());
//					setRotXamt(getRotXamt() - getRotXstride());
//					if (getRotXamt() < 0)
//						setRotXamt(0);
//				}
//				// smooth-stop
//				if (getRotXamt() == 0){
//					setRotXstride(getRotXstride() * 0.95f);
//					if (isRightRotation())
//						rotateY(getRotXstride() * getMouseSensitivity());
//					if (isLeftRotation())
//						rotateY(-getRotXstride() * getMouseSensitivity());
//					if (getRotXstride() < 0.001f)
//						setRotXstride(0);
//				}
//			}
//			
//			glfwSetCursorPos(BaseContext.getWindow().getId(),
//					input.getLockedCursorPosition().getX(),
//					input.getLockedCursorPosition().getY());
//		}
		
		if (!getPosition().equals(getPreviousPosition())){
			setCameraMoved(true);
		}
		
		if (!getForward().equals(getPreviousForward())){
			setCameraRotated(true);
		}
		
		setPreviousViewMatrix(getViewMatrix());
		setPreviousViewProjectionMatrix(getViewProjectionMatrix());
		setViewMatrix(new Matrix4f().View(this.getForward(), this.getUp()).mul(
				new Matrix4f().Translation(this.getPosition().mul(-1))));
		setViewProjectionMatrix(getProjectionMatrix().mul(getViewMatrix()));
		
		floatBuffer.clear();
		floatBuffer.put(BufferUtil.createFlippedBuffer(getPosition()));
		floatBuffer.put(0);
		floatBuffer.put(BufferUtil.createFlippedBuffer(getViewMatrix()));
		floatBuffer.put(BufferUtil.createFlippedBuffer(getViewProjectionMatrix()));
		floatBuffer.put(BufferUtil.createFlippedBuffer(getFrustumPlanes()));
		floatBuffer.flip();
	}
	
	public void move(Vec3f dir, float amount)
	{
		Vec3f newPos = position.add(dir.mul(amount));	
		setPosition(newPos);
	}
	
	private void initfrustumPlanes()
	{
		// ax * bx * cx +  d = 0; store a,b,c,d
		
		//left plane
		Vec4f leftPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) + this.projectionMatrix.get(0, 0)
					* (float) ((Math.tan(Math.toRadians(this.fovY/2))
					* ((double) BaseContext.getConfig().getX_ScreenResolution()
					/ (double) BaseContext.getConfig().getY_ScreenResolution()))),
				this.projectionMatrix.get(3, 1) + this.projectionMatrix.get(0, 1),
				this.projectionMatrix.get(3, 2) + this.projectionMatrix.get(0, 2),
				this.projectionMatrix.get(3, 3) + this.projectionMatrix.get(0, 3));
		
				this.frustumPlanes[0] = Util.normalizePlane(leftPlane);
		
		//right plane
		Vec4f rightPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) - this.projectionMatrix.get(0, 0)
					* (float) ((Math.tan(Math.toRadians(this.fovY/2))
					* ((double) BaseContext.getConfig().getX_ScreenResolution()
					/ (double) BaseContext.getConfig().getY_ScreenResolution()))),
				this.projectionMatrix.get(3, 1) - this.projectionMatrix.get(0, 1),
				this.projectionMatrix.get(3, 2) - this.projectionMatrix.get(0, 2),
				this.projectionMatrix.get(3, 3) - this.projectionMatrix.get(0, 3));
		
				this.frustumPlanes[1] = Util.normalizePlane(rightPlane);
		
		//bot plane
		Vec4f botPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) + this.projectionMatrix.get(1, 0),
				this.projectionMatrix.get(3, 1) + this.projectionMatrix.get(1, 1)
					* (float) Math.tan(Math.toRadians(this.fovY/2)),
				this.projectionMatrix.get(3, 2) + this.projectionMatrix.get(1, 2),
				this.projectionMatrix.get(3, 3) + this.projectionMatrix.get(1, 3));
		
				this.frustumPlanes[2] = Util.normalizePlane(botPlane);
		
		//top plane
		Vec4f topPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) - this.projectionMatrix.get(1, 0),
				this.projectionMatrix.get(3, 1) - this.projectionMatrix.get(1, 1)
					* (float) Math.tan(Math.toRadians(this.fovY/2)),
				this.projectionMatrix.get(3, 2) - this.projectionMatrix.get(1, 2),
				this.projectionMatrix.get(3, 3) - this.projectionMatrix.get(1, 3));
		
				this.frustumPlanes[3] = Util.normalizePlane(topPlane);
		
		//near plane
		Vec4f nearPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) + this.projectionMatrix.get(2, 0),
				this.projectionMatrix.get(3, 1) + this.projectionMatrix.get(2, 1),
				this.projectionMatrix.get(3, 2) + this.projectionMatrix.get(2, 2),
				this.projectionMatrix.get(3, 3) + this.projectionMatrix.get(2, 3));
		
				this.frustumPlanes[4] = Util.normalizePlane(nearPlane);
		
		//far plane
				Vec4f farPlane = new Vec4f(
				this.projectionMatrix.get(3, 0) - this.projectionMatrix.get(2, 0),
				this.projectionMatrix.get(3, 1) - this.projectionMatrix.get(2, 1),
				this.projectionMatrix.get(3, 2) - this.projectionMatrix.get(2, 2),
				this.projectionMatrix.get(3, 3) - this.projectionMatrix.get(2, 3));
				
				this.frustumPlanes[5] = Util.normalizePlane(farPlane);
	}
	
	public void rotateY(float angle)
	{
		Vec3f hAxis = yAxis.cross(forward).normalize();
		
		forward.rotate(angle, yAxis).normalize();
		
		up = forward.cross(hAxis).normalize();
	}
	
	public void rotateX(float angle)
	{
		Vec3f hAxis = yAxis.cross(forward).normalize();

		forward.rotate(angle, hAxis).normalize();
		
		up = forward.cross(hAxis).normalize();
	}
	
	public Vec3f getLeft()
	{
		Vec3f left = forward.cross(up);
		left.normalize();
		return left;
	}
	
	public Vec3f getRight()
	{
		Vec3f right = up.cross(forward);
		right.normalize();
		return right;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}
	
	public  void setProjection(float fovY, float width, float height)
	{
		this.fovY = fovY;
		this.width = width;
		this.height = height;
		
		this.projectionMatrix = new Matrix4f().PerspectiveProjection(
				fovY, width, height, Constants.ZNEAR, Constants.ZFAR);
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public void setViewMatrix(Matrix4f viewMatrix) {
		this.viewMatrix = viewMatrix;
	}

	public Vec3f getPosition() {
		return position;
	}

	public void setPosition(Vec3f position) {
		this.position = position;
	}

	public Vec3f getForward() {
		return forward;
	}

	public void setForward(Vec3f forward) {
		this.forward = forward;
	}

	public Vec3f getUp() {
		return up;
	}

	public void setUp(Vec3f up) {
		this.up = up;
	}

	public Vec4f[] getFrustumPlanes() {
		return frustumPlanes;
	}
	
	public float getFovY(){
		return this.fovY;
	}
	
	public float getWidth(){
		return this.width;
	}

	public float getHeight(){
		return this.height;
	}
	
	public void setViewProjectionMatrix(Matrix4f viewProjectionMatrix) {
		this.viewProjectionMatrix = viewProjectionMatrix;
	}
	
	public Matrix4f getViewProjectionMatrix() {
		return viewProjectionMatrix;
	}

	public Matrix4f getPreviousViewProjectionMatrix() {
		return previousViewProjectionMatrix;
	}

	public void setPreviousViewProjectionMatrix(
			Matrix4f previousViewProjectionMatrix) {
		this.previousViewProjectionMatrix = previousViewProjectionMatrix;
	}

	public Matrix4f getPreviousViewMatrix() {
		return previousViewMatrix;
	}

	public void setPreviousViewMatrix(Matrix4f previousViewMatrix) {
		this.previousViewMatrix = previousViewMatrix;
	}

	public Vec3f[] getFrustumCorners() {
		return frustumCorners;
	}

	public boolean isCameraMoved() {
		return isCameraMoved;
	}

	public void setCameraMoved(boolean isCameraMoved) {
		this.isCameraMoved = isCameraMoved;
	}

	public boolean isCameraRotated() {
		return isCameraRotated;
	}
	
	public void setCameraRotated(boolean isCameraRotated) {
		this.isCameraRotated = isCameraRotated;
	}

	public Vec3f getPreviousPosition() {
		return previousPosition;
	}

	public void setPreviousPosition(Vec3f previousPosition) {
		this.previousPosition = previousPosition;
	}
	
	public Vec3f getPreviousForward() {
		return previousForward;
	}

	public void setPreviousForward(Vec3f previousForward) {
		this.previousForward = previousForward;
	}

	public float getMovAmt() {
		return movAmt;
	}

	public void setMovAmt(float movAmt) {
		this.movAmt = movAmt;
	}

	public float getRotAmt() {
		return rotAmt;
	}

	public void setRotAmt(float rotAmt) {
		this.rotAmt = rotAmt;
	}

	public float getRotYstride() {
		return rotYstride;
	}

	public void setRotYstride(float rotYstride) {
		this.rotYstride = rotYstride;
	}

	public float getRotYamt() {
		return rotYamt;
	}

	public void setRotYamt(float rotYamt) {
		this.rotYamt = rotYamt;
	}

	public float getRotXstride() {
		return rotXstride;
	}

	public void setRotXstride(float rotXstride) {
		this.rotXstride = rotXstride;
	}

	public float getRotXamt() {
		return rotXamt;
	}

	public void setRotXamt(float rotXamt) {
		this.rotXamt = rotXamt;
	}

	public float getMouseSensitivity() {
		return mouseSensitivity;
	}

	public void setMouseSensitivity(float mouseSensitivity) {
		this.mouseSensitivity = mouseSensitivity;
	}

	public boolean isUpRotation() {
		return isUpRotation;
	}

	public void setUpRotation(boolean isUpRotation) {
		this.isUpRotation = isUpRotation;
	}

	public boolean isDownRotation() {
		return isDownRotation;
	}

	public void setDownRotation(boolean isDownRotation) {
		this.isDownRotation = isDownRotation;
	}

	public boolean isLeftRotation() {
		return isLeftRotation;
	}

	public void setLeftRotation(boolean isLeftRotation) {
		this.isLeftRotation = isLeftRotation;
	}

	public boolean isRightRotation() {
		return isRightRotation;
	}

	public void setRightRotation(boolean isRightRotation) {
		this.isRightRotation = isRightRotation;
	}
}
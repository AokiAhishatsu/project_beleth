package core.light;

import core.query.OcclusionQuery;
import core.scenegraph.NodeComponent;
import core.context.BaseContext;
import core.math.Vec4f;
import core.math.Vec2f;
import core.math.Vec3f;

public class Light extends NodeComponent{
	
	protected Vec3f color;
	protected float intensity;
	private OcclusionQuery occlusionQuery;
	
	public Light(Vec3f color, float intensity)
	{
		this.color = color;
		this.intensity = intensity;
	}
	
	public Light() {
	}
	
	public Vec2f getScreenSpacePosition(){
		
		Vec4f clipSpacePos = getTransform().getModelViewProjectionMatrix().mul(new Vec4f(0,0,0,1));
		Vec3f ndcSpacePos = new Vec3f(clipSpacePos.getX()/clipSpacePos.getW(),clipSpacePos.getY()/clipSpacePos.getW(),clipSpacePos.getZ()/clipSpacePos.getW());
		
		if (ndcSpacePos.getX() < -1 || ndcSpacePos.getX() > 1 || ndcSpacePos.getY() < -1 || ndcSpacePos.getY() > 1){
			return null;
		}
		
		Vec2f windowSpacePos = (new Vec2f(ndcSpacePos.getX(), ndcSpacePos.getY()).add(1.0f)).div(2.0f).mul(
									new Vec2f(BaseContext.getWindow().getWidth(),BaseContext.getWindow().getHeight()));
		
		return windowSpacePos;
	}
	
	public void update(){
		super.update();
	}
	
	public void occlusionQuery(){
		occlusionQuery.doQuery(this);
	}
	
	public Vec3f getColor() {
		return color;
	}
	public void setColor(Vec3f color) {
		this.color = color;
	}

	public float getIntensity() {
		return intensity;
	}
	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public OcclusionQuery getOcclusionQuery() {
		return occlusionQuery;
	}

	public void setOcclusionQuery(OcclusionQuery occlusionQuery) {
		this.occlusionQuery = occlusionQuery;
	}
}
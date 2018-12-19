package models;
 
public class RawModel {
	
	private int vaoID;//vertex array object id
	private int vertexCount;
     
    public RawModel(int vaoID, int vertexCount){
    	this.vaoID = vaoID;
        this.vertexCount = vertexCount;
    }
 
    public int getVaoID() {
    	return vaoID;
	}
 
	public int getVertexCount() {
		return vertexCount;
	}
 
}
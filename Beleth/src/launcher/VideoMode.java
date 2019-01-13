package launcher;

public class VideoMode{
	public final int Width; 
	public final int Height; 

	public VideoMode(int Width, int Height) { 
		this.Width = Width; 
		this.Height = Height;
	} 
	
	@Override
	public boolean equals(Object o) {
		VideoMode vm = (VideoMode) o;
		return this.Width == vm.Width && this.Height == vm.Height;  
	}
	
	@Override
	public String toString() {
		return this.Width + "x" + this.Height;
	}
} 

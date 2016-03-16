import java.util.concurrent.ThreadLocalRandom;

public class Particles{
	double x;
	double y;
	double weight;
	int heading;
	
	//Generate a random particle at around location (X,Y)
	Particles(int X, int Y, int radius_x, int radius_y){
		this.x = X + ThreadLocalRandom.current().nextInt(0, 2*radius_x + 1) - radius_x;
		this.y = Y + ThreadLocalRandom.current().nextInt(0, 2*radius_y + 1) - radius_y;
		this.weight = 1;
		this.heading = ThreadLocalRandom.current().nextInt(0, 361);
	}
}
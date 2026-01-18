package com.tsg.hitbox;

import com.gd.j2me.Vec2;

public class Hitbox {
	public void hitboxtriangle(Vec2 a, Vec2 b, Vec2 c, Vec2 offset, Direction dir) {
		
	}
	
	// used on j2i.net
	public static class Point {
	    public float x;
	    public float y;

	    public float getCoordinate(int index) {
	        if (index == 0) return x;
	        if (index == 1) return y;
	        throw new IndexOutOfBoundsException("index must be 0 or 1");
	    }

	    public void setCoordinate(int index, float value) {
	        if (index == 0) x = value;
	        else if (index == 1) y = value;
	        else throw new IndexOutOfBoundsException("index must be 0 or 1");
	    }
	    
	    public Point(float x, float y) {
	    	this.x = x;
	    	this.y = y;
	    }
	}
	 
	public static class Triangle {
	    public Point p1;
	    public Point p2;
	    public Point p3;

	    public Point[] pointList;

	    public Triangle(Point p1, Point p2, Point p3) {
	        this.p1 = p1;
	        this.p2 = p2;
	        this.p3 = p3;
	        this.pointList = new Point[]{p1, p2, p3};
	    }
	}
	
    static double Det2D(final Triangle triangle) {
        return +triangle.p1.x * (triangle.p2.y - triangle.p3.y) 
               + triangle.p2.x * (triangle.p3.y - triangle.p1.y) 
               + triangle.p3.x * (triangle.p1.y - triangle.p2.y);
    }
    
    public static void CheckTriWinding(final Triangle t, boolean allowReversed)
    {
        double detTri = Hitbox.Det2D(t);
        if (detTri < 0.0)
        {
            if (allowReversed)
            {
                Triangle tReverse = t;
                tReverse.p1 = t.p3;
                tReverse.p3 = t.p1;
                tReverse.p2 = t.p2;
            }
            else throw new IndexOutOfBoundsException("triangle has wrong winding direction");
        }
    }
    
    static boolean BoundaryCollideChk(final Triangle t, double eps)
    {
        return Det2D(t) < eps;
    }
     
    static boolean BoundaryCollideChk(final Point p1, final Point p2, final Point p3, double eps)
    {
        Triangle t = new Triangle(p1, p2, p3);
        return BoundaryCollideChk(t, eps);
    }
     
    static boolean BoundaryDoesntCollideChk(final Triangle t, double eps)
    {
        return Det2D(t) <= eps;
    }
     
    static boolean BoundaryDoesntCollideChk(final Point p1, final Point p2, final Point p3, double eps)
    {
    	Triangle t = new Triangle(p1, p2, p3);
        return BoundaryDoesntCollideChk(t, eps);
    }
    
    static boolean TriangleTriangleCollision(final Triangle triangle1,
    	    final Triangle triangle2,
    	    double eps, boolean allowReversed, boolean onBoundary)
    	{
    	    //Trangles must be expressed anti-clockwise
    	    CheckTriWinding(triangle1, allowReversed);
    	    CheckTriWinding(triangle2, allowReversed);  
    	 
    	    //For edge E of trangle 1,
    	    for (int i = 0; i < 3; i++)
    	    {
    	        int j = (i + 1) % 3;
    	        if (onBoundary)
    	        {
    	 
    	            //Check all points of trangle 2 lay on the external side of the edge E. If
    	            //they do, the triangles do not collide.
    	            if (BoundaryCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[0], eps) &&
    	                BoundaryCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[1], eps) &&
    	                BoundaryCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[2], eps))
    	                return false;
    	        }
    	        else
    	        {
    	            if (BoundaryDoesntCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[0], eps) &&
    	                BoundaryDoesntCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[1], eps) &&
    	                BoundaryDoesntCollideChk(triangle1.pointList[i], triangle1.pointList[j], triangle2.pointList[2], eps))
    	                return false;
    	        }
    	 
    	        if (onBoundary)
    	        {
    	 
    	            //Check all points of trangle 2 lay on the external side of the edge E. If
    	            //they do, the triangles do not collide.
    	            if (BoundaryCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[0], eps) &&
    	                BoundaryCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[1], eps) &&
    	                BoundaryCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[2], eps))
    	                return false;
    	        }
    	        else
    	        {
    	            if (BoundaryDoesntCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[0], eps) &&
    	                BoundaryDoesntCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[1], eps) &&
    	                BoundaryDoesntCollideChk(triangle2.pointList[i], triangle2.pointList[j], triangle1.pointList[2], eps))
    	                return false;
    	        }
    	    }
    	    //The triangles collide
    	    return true;
    	}
    //
    
    public static class Rectangle {
    	float x;
    	float y;
    	float width;
    	float height;
    	
    	Triangle t1 = new Triangle(new Point(0,0), new Point(0,0), new Point(0,0));
    	Triangle t2 = new Triangle(new Point(0,0), new Point(0,0), new Point(0,0));
    	
    	public Rectangle(float width, float height, float xoff, float yoff) {
    		this.x = xoff;
    		this.y = yoff;
    		this.width = width;
    		this.height = height;
    		Triangle t1 = new Triangle(new Point(x, y), new Point(x+width, y), new Point(x, y+height));
    		Triangle t2 = new Triangle(new Point(x+width, y), new Point(x, y+height), new Point(x+width, y+height));
    	}
    }
    
    public static boolean RectRectCollision(final Rectangle rect1,
    		final Rectangle rect2,
    		double eps, boolean allowReversed, boolean onBoundary) {
    	return TriangleTriangleCollision(rect1.t1, rect2.t1, eps, allowReversed, onBoundary) ||
    	TriangleTriangleCollision(rect1.t1, rect2.t2, eps, allowReversed, onBoundary) ||
    	TriangleTriangleCollision(rect1.t2, rect2.t1, eps, allowReversed, onBoundary) ||
    	TriangleTriangleCollision(rect1.t2, rect2.t2, eps, allowReversed, onBoundary);
    }
    
    public static boolean RectTriCollision(final Rectangle rect1,
    		final Triangle triangle1,
    		double eps, boolean allowReversed, boolean onBoundary) {
    	return TriangleTriangleCollision(rect1.t1, triangle1, eps, allowReversed, onBoundary) ||
    	TriangleTriangleCollision(rect1.t2, triangle1, eps, allowReversed, onBoundary);
    }
    
    public static boolean TriRectCollision(final Triangle triangle1,
    		final Rectangle rect1,
    		double eps, boolean allowReversed, boolean onBoundary) {
    	return TriangleTriangleCollision(rect1.t1, triangle1, eps, allowReversed, onBoundary) ||
    	TriangleTriangleCollision(rect1.t2, triangle1, eps, allowReversed, onBoundary);
    }
}
 

package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class SharkUtilities {

	public static class Hitbox {
		
		private float x = 0;
		private float y = 0;
		private float width = 0;
		private float height = 0;
		private double anchorx = 0;
		private double anchory = 0;
		
		private Hitbox(float f, float g, float h, float i, double d, double e) {
			// TODO Auto-generated constructor stub
			this.x = f;
	        this.y = g;
	        this.width = h;
	        this.height = i;
	        this.anchorx = d;
	        this.anchory = e;
		}

		public static boolean isTouching(Hitbox hitbox1, Hitbox hitbox2) {
			hitbox1 = Hitbox.rect(hitbox1.getX() + (hitbox1.getWidth() * (float) hitbox1.getAnchorX()), hitbox1.getY() + (hitbox1.getHeight() * (float) hitbox1.getAnchorY()), hitbox1.getWidth(), hitbox1.getHeight(), hitbox1.getAnchorX(), hitbox1.getAnchorY());
			hitbox2 = Hitbox.rect(hitbox2.getX() + (hitbox2.getWidth() * (float) hitbox2.getAnchorX()), hitbox2.getY() + (hitbox2.getHeight() * (float) hitbox2.getAnchorY()), hitbox2.getWidth(), hitbox2.getHeight(), hitbox2.getAnchorX(), hitbox2.getAnchorY());
			
			// TODO Auto-generated method stub
			if ((((hitbox1.getX()+hitbox1.getWidth())+1)>hitbox2.getX())
					&& (((hitbox2.getX()+hitbox2.getWidth())+1)>hitbox1.getX())
					&& (((hitbox2.getY()+hitbox2.getHeight())+1)>hitbox1.getY())
					&& (((hitbox1.getY()+hitbox1.getHeight())+1)>hitbox2.getY())) {
				return true;
				//return hitbox2.getX() + ", " + (hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() <= hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() + hitbox2.getWidth() >= hitbox1.getX()-1) + ", " + (hitbox2.getY() <= hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() + hitbox2.getHeight() >= hitbox1.getY()-1);
			}
			return false;
		}

		public static Hitbox rect(float f, float g, float h, float i, double d, double e) {
			// TODO Auto-generated method stub
			return new Hitbox(f, g, h, i, d, e);
		}
		
		public float getX() {
			// TODO Auto-generated method stub
			SharkUtilities.Hitbox object = SharkUtilities.Hitbox();
			return x;
		}

		public float getY() {
			// TODO Auto-generated method stub
			return y;
		}

		public float getWidth() {
			// TODO Auto-generated method stub
			return width;
		}

		public float getHeight() {
			// TODO Auto-generated method stub
			return height;
		}
		
		public double getAnchorX() {
			return anchorx;
		}
		
		public double getAnchorY() {
			return anchory;
		}
		
		public static boolean isTouchingH(Hitbox hitbox1, Hitbox hitbox2) {
			hitbox1 = Hitbox.rect(hitbox1.getX() + (hitbox1.getWidth() * (float) hitbox1.getAnchorX()), hitbox1.getY() + (hitbox1.getHeight() * (float) hitbox1.getAnchorY()), hitbox1.getWidth(), hitbox1.getHeight(), hitbox1.getAnchorX(), hitbox1.getAnchorY());
			hitbox2 = Hitbox.rect(hitbox2.getX() + (hitbox2.getWidth() * (float) hitbox2.getAnchorX()), hitbox2.getY() + (hitbox2.getHeight() * (float) hitbox2.getAnchorY()), hitbox2.getWidth(), hitbox2.getHeight(), hitbox2.getAnchorX(), hitbox2.getAnchorY());
			// TODO Auto-generated method stub
			if ((((hitbox2.getY()+hitbox2.getHeight())+1)>hitbox1.getY())
				&& (((hitbox1.getY()+hitbox1.getHeight())+1)>hitbox2.getY())) {
				return true;
				//return hitbox2.getX() + ", " + (hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() <= hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() + hitbox2.getWidth() >= hitbox1.getX()-1);
			}
			return false;
			//return "shit";
		}
		
		public static boolean isTouchingD(Hitbox hitbox1, Hitbox hitbox2) {
			hitbox1 = Hitbox.rect(hitbox1.getX() + (hitbox1.getWidth() * (float) hitbox1.getAnchorX()), hitbox1.getY() + (hitbox1.getHeight() * (float) hitbox1.getAnchorY()), hitbox1.getWidth(), hitbox1.getHeight(), hitbox1.getAnchorX(), hitbox1.getAnchorY());
			hitbox2 = Hitbox.rect(hitbox2.getX() + (hitbox2.getWidth() * (float) hitbox2.getAnchorX()), hitbox2.getY() + (hitbox2.getHeight() * (float) hitbox2.getAnchorY()), hitbox2.getWidth(), hitbox2.getHeight(), hitbox2.getAnchorX(), hitbox2.getAnchorY());
			// TODO Auto-generated method stub
			if ((((hitbox1.getY()+hitbox1.getHeight())+1)>hitbox2.getY())) {
				return true;
				//return hitbox2.getX() + ", " + (hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() <= hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() + hitbox2.getWidth() >= hitbox1.getX()-1);
			}
			return false;
			//return "shit";
		}
		
		public static boolean isTouchingU(Hitbox hitbox1, Hitbox hitbox2) {
			hitbox1 = Hitbox.rect(hitbox1.getX() + (hitbox1.getWidth() * (float) hitbox1.getAnchorX()), hitbox1.getY() + (hitbox1.getHeight() * (float) hitbox1.getAnchorY()), hitbox1.getWidth(), hitbox1.getHeight(), hitbox1.getAnchorX(), hitbox1.getAnchorY());
			hitbox2 = Hitbox.rect(hitbox2.getX() + (hitbox2.getWidth() * (float) hitbox2.getAnchorX()), hitbox2.getY() + (hitbox2.getHeight() * (float) hitbox2.getAnchorY()), hitbox2.getWidth(), hitbox2.getHeight(), hitbox2.getAnchorX(), hitbox2.getAnchorY());
			// TODO Auto-generated method stub
			if ((((hitbox2.getY()+hitbox2.getHeight())+1)>hitbox1.getY())) {
				return true;
				//return hitbox2.getX() + ", " + (hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() <= hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() + hitbox2.getWidth() >= hitbox1.getX()-1);
			}
			return false;
			//return "shit";
		}

		public static boolean isTouchingV(Hitbox hitbox1, Hitbox hitbox2) {
			hitbox1 = Hitbox.rect(hitbox1.getX() + (hitbox1.getWidth() * (float) hitbox1.getAnchorX()), hitbox1.getY() + (hitbox1.getHeight() * (float) hitbox1.getAnchorY()), hitbox1.getWidth(), hitbox1.getHeight(), hitbox1.getAnchorX(), hitbox1.getAnchorY());
			hitbox2 = Hitbox.rect(hitbox2.getX() + (hitbox2.getWidth() * (float) hitbox2.getAnchorX()), hitbox2.getY() + (hitbox2.getHeight() * (float) hitbox2.getAnchorY()), hitbox2.getWidth(), hitbox2.getHeight(), hitbox2.getAnchorX(), hitbox2.getAnchorY());
			// TODO Auto-generated method stub
			if ((((hitbox1.getX()+hitbox1.getWidth())+1)>hitbox2.getX())
					&& (((hitbox2.getX()+hitbox2.getWidth())+1)>hitbox1.getX())) {
				return true;
				//return hitbox2.getY() + ", " + (hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() <= hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() + hitbox2.getHeight() >= hitbox1.getY()-1);
			}
			return false;
			//return hitbox2.getY() + ", " + (hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() <= hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() + hitbox2.getHeight() >= hitbox1.getY()-1);
		}

		public Hitbox expand(float i, float j, float k, float l, double d, double e) {
			// TODO Auto-generated method stub
			return new Hitbox(x+i, y+i, width+k, height+l, anchorx+d, anchory+e);
		}
	}
	public static class HitboxPoly {
		// idk how it works now
		private float x = 0;
		private float y = 0;
		private float lt = 0;
		private float lb = 0;
		private float rt = 0;
		private float rb = 0;
		
		private HitboxPoly(float f, float g, float h, float i, float j, float k) {
			// TODO Auto-generated constructor stub
			this.x = f;
	        this.y = g;
	        this.lt = h;
	        this.lb = i;
	        this.rt = j;
	        this.rb = k;
		}
	}
	
	public static class Clickable {
		public int x= 0;
		public int y=0;
		public int w=0;
		public int h=0;
		
		public Clickable(int x, int y, int w, int h) {
			// TODO Auto-generated constructor stub
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		public static boolean isTouching(Clickable clickable, int x, int y) {
			// TODO Auto-generated method stub
	    	if ((x >= clickable.x && x <= clickable.x + clickable.w && y >= clickable.y && y <= clickable.y + clickable.h)) {
	    		return true;
	    	}
	    	return false;
		}
		
	}
	//public static Hitbox[] Hitbox;

	public static void drawHitbox(float i, float j, float k, float l, Graphics g,
			int m, int n) {
		// TODO Auto-generated method stub
		g.setColor(m);
		
		i = (int) i;
		j = (int) j;
		k = (int) k;
		l = (int) l;
		
		switch (n) {
			case (0): {
				g.drawLine((int) i, (int) j, (int) i+(int) k, (int) j);
				g.drawLine((int) i+(int) k, (int) j, (int) i+(int) k, (int) j+(int) l);
				g.drawLine((int) i+(int) k, (int) j+(int) l, (int) i, (int) j+(int) l);
				g.drawLine((int) i, (int) j+(int) l, (int) i, (int) j);
			}
		}
		
	}

	public static Hitbox Hitbox() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void drawHitboxWithRect(Hitbox hitbox, Graphics g, int m, int n) {
		// TODO Auto-generated method stub
		g.setColor(m);
		
		float i = (float) (hitbox.getX()+(hitbox.getWidth() * hitbox.getAnchorX()));
		float j = (float) (hitbox.getY()+(hitbox.getHeight() * hitbox.getAnchorY()));
		float k = hitbox.getWidth();
		float l = hitbox.getHeight();
		
		switch (n) {
			case (0): {
				g.drawLine((int) i, (int) j, (int) i+(int) k, (int) j);
				g.drawLine((int) i+(int) k, (int) j, (int) i+(int) k, (int) j+(int) l);
				g.drawLine((int) i+(int) k, (int) j+(int) l, (int) i, (int) j+(int) l);
				g.drawLine((int) i, (int) j+(int) l, (int) i, (int) j);
			}
		}
		
	}

	public static void drawImageWithAnchor(Image image, int i, int j, int k,
			double d, double e, Graphics g) {
		// TODO Auto-generated method stub
		int x = (int) (i+(image.getWidth()*d));
		int y = (int) (j-(image.getHeight()*e)+(40*e));
		g.drawImage(image, x, y, k);
		
	}
	
	public static void drawImageWithAnchorOld(Image image, int i, int j, int k,
			double d, double e, Graphics g) {
		// TODO Auto-generated method stub
		int x = (int) (i+(image.getWidth()*d));
		int y = (int) (j+(image.getHeight()*e));
		g.drawImage(image, x, y, k);
		
	}
	
	public static Image tintImage(Image source, int rgb) {
        int[] rgbData = new int[source.getWidth() * source.getHeight()];

        source.getRGB(rgbData, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());

        int tintR = (rgb >> 16) & 0xFF;
        int tintG = (rgb >> 8) & 0xFF;
        int tintB = rgb & 0xFF;

        for (int i = 0; i < rgbData.length; i++) {
            int pixel = rgbData[i];

            int alpha = (pixel >> 24) & 0xFF;
            int origR = (pixel >> 16) & 0xFF;
            int origG = (pixel >> 8) & 0xFF;
            int origB = pixel & 0xFF;

            int newR = (origR * tintR) / 255;
            int newG = (origG * tintG) / 255;
            int newB = (origB * tintB) / 255;

            rgbData[i] = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
        }
        return Image.createRGBImage(rgbData, source.getWidth(), source.getHeight(), true);
	}
	
	private static Image transformImage(Image image, int type) {
		Image transformedImage = Image.createImage(image.getWidth(), image.getHeight());
		Graphics g = transformedImage.getGraphics();
		
		g.drawRegion(image, 0, 0, image.getWidth(), image.getHeight(), type, 0, 0, Graphics.TOP | Graphics.LEFT);
		
		return transformedImage;
	}

	public static Image flipImage(Image image, int type) {
		// TODO Auto-generated method stub
		int[] imageData = new int[image.getWidth() * image.getHeight()];
		image.getRGB(imageData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
		
		int[] transformedImageData = new int[image.getWidth() * image.getHeight()];
		
		switch (type) {
			case (0):{
				for (int y = 0; y < image.getHeight(); y++) {
				    for (int x = 0; x < image.getWidth(); x++) {
				        int originalIndex = y * image.getWidth() + x;
				        int flippedIndex = y * image.getWidth() + (image.getWidth() - 1 - x);
				        transformedImageData[flippedIndex] = imageData[originalIndex];
				    }
				}

			}
			case (1):{
				for (int y = 0; y < image.getHeight(); y++) {
				    for (int x = 0; x < image.getWidth(); x++) {
				        int originalIndex = y * image.getWidth() + x;
				        int flippedIndex = (image.getHeight() - 1 - y) * image.getWidth() + x;
				        transformedImageData[flippedIndex] = imageData[originalIndex];
				    }
				}
			}
		}
		
		return Image.createRGBImage(transformedImageData, image.getWidth(), image.getHeight(), true);
	}
	
	
	public static Image flipImageHorizontal(Image image) {
	    
	    int[] argbData = new int[image.getWidth() * image.getHeight()];
	    image.getRGB(argbData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
	    
	    int[] flippedData = new int[image.getWidth() * image.getHeight()];
	    
	    for (int y = 0; y < image.getHeight(); y++) {
	        for (int x = 0; x < image.getWidth(); x++) {
	            flippedData[y * image.getWidth() + (image.getWidth() - 1 - x)] = argbData[y * image.getWidth() + x];
	        }
	    }
	    
	    Image flippedImage = Image.createRGBImage(flippedData, image.getWidth(), image.getHeight(), true);
	    return flippedImage;
	}
	
	public static Image flipImageVertical(Image image) {
	    
	    int[] argbData = new int[image.getWidth() * image.getHeight()];
	    image.getRGB(argbData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
	    
	    int[] flippedData = new int[image.getWidth() * image.getHeight()];
	    
	    for (int y = 0; y < image.getHeight(); y++) {
	        for (int x = 0; x < image.getWidth(); x++) {
	            flippedData[y * image.getWidth() + x] = argbData[(image.getHeight() - 1 - y) * image.getWidth() + x];
	        }
	    }
	    
	    Image flippedImage = Image.createRGBImage(flippedData, image.getWidth(), image.getHeight(), true);
	    return flippedImage;
	}
	
	// Source - https://stackoverflow.com/a
	// Posted by mr_lou
	// Retrieved 2025-12-14, License - CC BY-SA 3.0

	public static Image scale(Image original, int newWidth, int newHeight) {

	 int[] rawInput = new int[original.getHeight() * original.getWidth()];
	 original.getRGB(rawInput, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());

	 int[] rawOutput = new int[newWidth * newHeight];

	 // YD compensates for the x loop by subtracting the width back out
	 int YD = (original.getHeight() / newHeight) * original.getWidth() - original.getWidth();
	 int YR = original.getHeight() % newHeight;
	 int XD = original.getWidth() / newWidth;
	 int XR = original.getWidth() % newWidth;
	 int outOffset = 0;
	 int inOffset = 0;

	 for (int y = newHeight, YE = 0; y > 0; y--) {
		   for (int x = newWidth, XE = 0; x > 0; x--) {
			     rawOutput[outOffset++] = rawInput[inOffset];
			     inOffset += XD;
			     XE += XR;
			     if (XE >= newWidth) {
				       XE -= newWidth;
				       inOffset++;
			     }
		   }
		   inOffset += YD;
		   YE += YR;
		   if (YE >= newHeight) {
		     YE -= newHeight;
		     inOffset += original.getWidth();
		   }
		 }
		 rawInput = null;
		 return Image.createRGBImage(rawOutput, newWidth, newHeight, true);
	}
	
    public static float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }
    
    public static Image rotateImage(Image image, float angle) {
        int[] srcData = new int[image.getWidth() * image.getHeight()];
        image.getRGB(srcData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        
        double radians = Math.toRadians(angle);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);

        int newWidth = (int) Math.floor(Math.abs(image.getWidth() * cos) + Math.abs(image.getHeight() * sin));
        int newHeight = (int) Math.floor(Math.abs(image.getHeight() * cos) + Math.abs(image.getWidth() * sin));

        int[] dstData = new int[newWidth * newHeight];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int srcX = (int) ((x - newWidth / 2) * cos - (y - newHeight / 2) * sin + image.getWidth() / 2);
                int srcY = (int) ((x - newWidth / 2) * sin + (y - newHeight / 2) * cos + image.getHeight() / 2);
                
                if (srcX >= 0 && srcX < image.getWidth() && srcY >= 0 && srcY < image.getHeight()) {
                    dstData[x + y * newWidth] = srcData[srcX + srcY * image.getWidth()];
                } else {
                    dstData[x + y * newWidth] = 0x00000000;
                }
            }
        }

        return Image.createRGBImage(dstData, newWidth, newHeight, true);
    }
    
    public static float rotateImageToReturnWidth(Image image, float angle) {
        double radians = Math.toRadians(angle);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);

        int newWidth = (int) Math.floor(Math.abs(image.getWidth() * cos) + Math.abs(image.getHeight() * sin));

        return newWidth;
    }
    
    public static float rotateImageToReturnHeight(Image image, float angle) {
        double radians = Math.toRadians(angle);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);
        
        int newHeight = (int) Math.floor(Math.abs(image.getHeight() * cos) + Math.abs(image.getWidth() * sin));

        return newHeight;
    }
    
    public static void fillRectWithTransp(int x, int y, int width, int height, int color, Graphics g) {
        try {
            int[] rawData = new int[width * height];
            
            int targetColor = color; 

            for (int i = 0; i < rawData.length; i++) {
                rawData[i] = targetColor;;
            }

            g.drawImage(Image.createRGBImage(rawData, width, height, true), x, y, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Image splitImg(Image image, int x1, int y1, int width, int height) {
    	int[] rgbData = new int[image.getWidth() * image.getHeight()];
    	image.getRGB(rgbData, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
    	
    	int[] subRgbData = new int[width * height];
    	
    	for (int y = 0; y < height; y++) {
    	    for (int x = 0; x < width; x++) {
    	        int sourceIndex = (y1 + y) * image.getWidth() + (x1 + x);
    	        int subIndex = y * width + x;
    	        subRgbData[subIndex] = rgbData[sourceIndex];
    	    }
    	}
    	
    	
    	return Image.createRGBImage(subRgbData, width, height, true);
    }
    
    public static double atan(double x) {
        if (Double.isNaN(x)) return Double.NaN;
        if (x == Double.POSITIVE_INFINITY) return Math.PI / 2;
        if (x == Double.NEGATIVE_INFINITY) return -Math.PI / 2;
        
        boolean flip = false;
        if (Math.abs(x) > 1) {
            x = 1 / x;
            flip = true;
        }

        double result = 0;
        double term = x;
        double sign = 1.0;
        int iterations = 100; 

        for (int i = 0; i < iterations; i++) {
            result += sign * term / (2 * i + 1);
            term *= x * x;
            sign *= -1;
        }

        if (flip) {
            return (x > 0 ? Math.PI / 2 : -Math.PI / 2) - result;
        }

        return result;
    }
    
    public static double atan2(double y, double x) {
        if (x > 0) {
            return atan(y / x);
        } else if (x < 0 && y >= 0) {
            return atan(y / x) + Math.PI;
        } else if (x < 0 && y < 0) {
            return atan(y / x) - Math.PI;
        } else if (x == 0 && y > 0) {
            return Math.PI / 2;
        } else if (x == 0 && y < 0) {
            return -Math.PI / 2;
        } else {
            return 0.0; 
        }
    }
}

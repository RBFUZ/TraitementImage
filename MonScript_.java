import ij.*;
import ij.process.*;
import ij.plugin.filter.*;
public class MonScript_ implements PlugInFilter {
 public void run(ImageProcessor ip) {
  byte[] pixels = (byte[]) ip.getPixels(); // Notez le cast en byte ()
  int width = ip.getWidth();
  int height = ip.getHeight();
  int ndg;
  for (int y = 0; y < height; y++)
   for (int x = 0; x < width; x++) { // pas completement optimal mais pedagogique…
    ndg = pixels[y * width + x] & 0xff;
    if (ndg < 120)
     pixels[y * width + x] = (byte) 0;
    else
     pixels[y * width + x] = (byte) 255;
   }
 }
 public int setup(String arg, ImagePlus imp) {
  if (arg.equals("about")) {
   IJ.showMessage("Traitement de l'image");
   return DONE;
  }
  return DOES_8G;
 }
}
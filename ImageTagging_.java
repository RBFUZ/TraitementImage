import ij.*;
import ij.process.*;
import ij.plugin.filter.*;
import java.io.File;
import java.text.DecimalFormat;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.awt.Color;
import ij.process.ImageStatistics;
import java.io.FilenameFilter;
import java.util.ArrayList;
import ij.plugin.ContrastEnhancer;

public class ImageTagging_ implements PlugInFilter {

	public void run(ImageProcessor ownIp) {
		ArrayList<Color> listColors = new ArrayList<Color>();
		listColors.add(Color.black);
		listColors.add(Color.blue);
		listColors.add(Color.green);
		listColors.add(Color.orange);
		listColors.add(Color.pink);
		listColors.add(Color.red);
		listColors.add(Color.white);
		listColors.add(Color.yellow);

		String path = "C:\\Program Files\\ImageJ\\Images";
	  	File[] files = listFiles(path);

   		if (files.length != 0) {
   			try {
	    		for (int i = 0; i < files.length; i++) {

					// Remise à zéro des fichiers textes si existe, sinon création
			    	PrintWriter writer = new PrintWriter("Images/"+files[i].getName()+".txt", "UTF-8");
			    	writer.close();

			    	// Création d’une image temporaire
				    ImagePlus tempImg = new ImagePlus(files[i].getAbsolutePath());
				    ImageProcessor ip = tempImg.getProcessor();

			    	int width = ip.getWidth(), height = ip.getHeight();
					int[] rgb = new int[3];
					double meilleurDistance = 0, distance = 0;
					int[] counter = new int[10];
					
					for (int j = 0; j < 10; j++)
						counter[j] = 0;
					
					int indexTemp = 0;

					// Parcours chacun des pixels
					for (int y = 0; y < height; y++)
					{
						for (int x = 0; x < width; x++)
						{
							ip.getPixel(x,y,rgb); // Récupération d'un pixel à la position x et y

							// Recherche la couleur la plus similaire à celle calculée
							int index = 0;
							for(Color c : listColors)
							{
								// Calcul de la distance entre la couleur calculée et une couleur de référence (voir l'arraylist)
								Color color = new Color(rgb[0], rgb[1], rgb[2]);

								distance = distanceEuclidienne(color, c);

								if (index == 0) {
									meilleurDistance = distance;
									indexTemp = index;
								}
								else
									if (distance < meilleurDistance) {
										meilleurDistance = distance;
										indexTemp = index;
									}
									
								index++;
							}
							counter[indexTemp] += 1;
						}
					}					
		   		}

		    } catch (IOException e) {
			  	IJ.showMessage("Impossible de créer les fichiers textes, annulation du tagging");
		    }
   		}
	}

	public double distance(Color c1, Color c2)
	{
		int red1 = c1.getRed();
	    int red2 = c2.getRed();
	    int rmean = (red1 + red2) >> 1;
	    int r = red1 - red2;
	    int g = c1.getGreen() - c2.getGreen();
	    int b = c1.getBlue() - c2.getBlue();
	    return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
	}

	public double distanceEuclidienne(Color c1, Color c2)
	{
		return Math.sqrt(Math.pow(c2.getRed() - c1.getRed(), 2) + Math.pow(c2.getGreen() - c1.getGreen(), 2) + Math.pow(c2.getBlue() - c1.getBlue(), 2));
	}


	// TODO

   	public File[] listFiles(String directoryPath) {
			File[] files = null;
			File directoryToScan = new File(directoryPath);
			files = directoryToScan.listFiles(new FilenameFilter() {
 
 				// Permet de ne récupérer que les images et non les fichiers textes créés
    			public boolean accept(File dir, String name) {
        			return name.toLowerCase().endsWith(".jpg");
    			}
			});

			return files;
   	}

   public int setup(String arg, ImagePlus imp) {
	  	if (arg.equals("about")) {
	   		IJ.showMessage("Traitement de l'image");
	   		return DONE;
	  	}
	  	return DOES_ALL; // Acception de toutes les images
 	}
}
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

public class ImageTagging_ implements PlugInFilter {

	public void run(ImageProcessor ownIp) {
		ArrayList<Color> listColors = new ArrayList<Color>();
		listColors.add(Color.black);
		listColors.add(Color.blue);
		listColors.add(Color.cyan);
		listColors.add(Color.gray);
		listColors.add(Color.green);
		listColors.add(Color.orange);
		listColors.add(Color.pink);
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
					long rouge = 0, vert = 0, bleu = 0, size = width * height, meilleurDistance = 0, distance = 0;
					long rougeMoyenne = 0, vertMoyenne = 0, bleuMoyenne = 0;
					Color color = new Color(100,100,100);

					// Parcours chacun des pixels
					for (int y = 0; y < height; y++)
					{
						for (int x = 0; x < width; x++)
						{
							ip.getPixel(x,y,rgb); // Récupération d'un pixel à la position x et y

							rouge += rgb[0];
							vert += rgb[1];
							bleu += rgb[2];
						}
					}

					// Calcul des moyennes
					rougeMoyenne = rouge / size;
					vertMoyenne = vert / size;
					bleuMoyenne = bleu / size;


					// Recherche la couleur la plus similaire à celle calculée
					int index = 0;
					for(Color c : listColors)
					{
						// Calcul de la distance entre la couleur calculée et une couleur de référence (voir l'arraylist)
						distance = Math.abs(rougeMoyenne - c.getRed()) + Math.abs(vertMoyenne - c.getGreen()) + Math.abs(bleuMoyenne - c.getBlue());

						//IJ.showMessage(Long.toString(rougeMoyenne));
						//IJ.showMessage(Long.toString(vertMoyenne));
						//IJ.showMessage(Long.toString(bleuMoyenne));

						if (index == 0)
							meilleurDistance = distance;
						else
							if (distance < meilleurDistance) {
								meilleurDistance = distance;
								color = new Color(c.getRed(), c.getGreen(), c.getBlue());
							}
							
						index++;
					}
					IJ.showMessage(color.toString());
		   		}

		    } catch (IOException e) {
			  	IJ.showMessage("Impossible de créer les fichiers textes, annulation du tagging");
		    }
   		}
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
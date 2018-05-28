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
import java.util.Collections;

public class ImageTagging_ implements PlugInFilter {

	private ArrayList<Color> listColors;
	private ArrayList<String> listColorsName;
	private ArrayList<Integer> counter;
	private static final String path = "C:\\Program Files\\ImageJ\\Images";

	public ImageTagging_()
	{
		listColors = new ArrayList<Color>();
		listColors.add(Color.black);
		listColors.add(Color.blue);
		listColors.add(Color.cyan);
		listColors.add(Color.gray);
		listColors.add(Color.green);
		listColors.add(Color.orange);
		listColors.add(Color.pink);
		listColors.add(Color.red);
		listColors.add(Color.white);
		listColors.add(Color.yellow);

		listColorsName = new ArrayList<String>();
		listColorsName.add("black, ");
		listColorsName.add("blue, ");
		listColorsName.add("cyan, ");
		listColorsName.add("gray, ");
		listColorsName.add("green, ");
		listColorsName.add("orange, ");
		listColorsName.add("pink, ");
		listColorsName.add("red, ");
		listColorsName.add("white, ");
		listColorsName.add("yellow, ");

		counter = new ArrayList<Integer>();
		for (int j = 0; j < listColors.size(); j++)
			counter.add(0);
	}

	public void run(ImageProcessor ownIp) {
	  	File[] files = listFiles(path);
	  	int width, height;
	  	int[] rgb = new int[3]; // Permet de stoker un pixel sous forme RGB

   		if (files.length != 0) {
   			try {
   				// Parcours de l'enembles des images du dossier avec l'extension .jpg
	    		for (int i = 0; i < files.length; i++) {

					// Remise à zéro des fichiers textes si existe, sinon création
			    	PrintWriter writer = new PrintWriter("Images/"+files[i].getName()+".txt", "UTF-8");
			    	
			    	// Création d’une image temporaire
				    ImagePlus tempImg = new ImagePlus(files[i].getAbsolutePath());
				    ImageProcessor ip = tempImg.getProcessor();

				    // Récupération des dimensions de l'image
			    	width = ip.getWidth();
			    	height = ip.getHeight();
					
					// Remise à zéro des conteurs
					resetCounter();

					// Permet d'écrire dans le fichier texte le mot "light" ou "dark" en fonction de la teinte de l'image
					sombreOuClair(writer, tempImg);

					int indexTemp = 0;

					// Parcours chacun des pixels
					for (int y = 0; y < height; y++)
					{
						for (int x = 0; x < width; x++)
						{
							// Récupération d'un pixel à la position x et y
							ip.getPixel(x,y,rgb);

							// Récupération de l'indice de la couleur qui se rapproche le plus du pixel fourni en paramètre
							indexTemp = distance(new Color(rgb[0], rgb[1], rgb[2]));

							// Incrémente la couleur qui se rapprochait le plus du pixel
							counter.set(indexTemp, counter.get(indexTemp) + 1);
						}
					}

					// Insertion des deux couleurs les plus présentes dans le fichier txt
					affichage(writer);

					// Analyse l'image pour détecter la présence ou non du ciel
					detecterCiel(ip, writer);

					writer.close();
		   		}
		    } catch (IOException e) {
			  	IJ.showMessage("Impossible de créer les fichiers textes, annulation du tagging");
		    }
   		}
	}

	// Analyse l'image pour détecter la présence ou non du ciel dans une image
	public void detecterCiel(ImageProcessor ip, PrintWriter writer)
	{
		int[] rgb = new int[3]; // Permet de stoker un pixel sous forme RGB
		int width = ip.getWidth();

		// La hauteur de l'image est divisé par 4 de manière à analyser que le haut de l'image car c'est la position ou se trouve le ciel dans l'image
		int height = ip.getHeight() / 4;

		resetCounter(); // Remise à zéro des conteurs

		// Augmentation du contrast de 75% pour permettre de mieux faire ressortir la couleur bleu du ciel
		ContrastEnhancer ceh = new ContrastEnhancer();
		ceh.stretchHistogram(ip, 75.0);

		int indexTemp;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				ip.getPixel(x,y,rgb); // Récupération d'un pixel (RGB)

				// Récupération de l'indice de la couleur qui se rapproche le plus du pixel fourni en paramètre
				indexTemp = distance(new Color(rgb[0], rgb[1], rgb[2]));

				// Incrémente la couleur qui se rapprochait le plus du pixel
				counter.set(indexTemp, counter.get(indexTemp) + 1);
			}
		}

		// Récupération de la couleur dominante de l'image
		Color c = listColors.get(counter.indexOf(Collections.max(counter)));

		// Si la couleur est bleu foncé ou bleu clair, alors ciel détécté donc ajout de "sky" et "outside" dans le fichier txt
		if (c.equals(Color.blue) || c.equals(Color.cyan)) {
			writer.print("sky, ");
			writer.print("outside, ");
		}
	}

	// Permet de savoir quelle couleur se rapproche le plus du pixel fourni en paramètre
	public int distance(Color c1)
	{
		// Récupération des couleurs du pixel
		int rouge = c1.getRed(), vert = c1.getGreen(), bleu = c1.getBlue();

		// Traitement de la couleur grise
		if (rouge > 85 && rouge < 160)
			if (vert > 85 && vert < 160)
				if (bleu > 85 && bleu < 160)
					return listColors.indexOf(Color.gray); // (128, 128, 128)

		// Traitement des autres couleurs
		if (rouge < 128)
		{
			if (vert < 128)
			{
				if (bleu < 128)
					return listColors.indexOf(Color.black); // (0, 0, 0)
				else
					return listColors.indexOf(Color.blue); // (0, 0, 255)
			}
			else
			{
				if (bleu < 128)
					return listColors.indexOf(Color.green); // (0, 255, 0)
				else
					return listColors.indexOf(Color.cyan); // (0, 255, 255)
			}
		}
		else
		{
			if (vert < 128 && bleu < 128)
				return listColors.indexOf(Color.red); // (255, 0, 0)
			else
			{
				if (bleu < 100)
				{
					if (vert > 225)
						return listColors.indexOf(Color.yellow); // (255, 255, 0)
					else
						return listColors.indexOf(Color.orange); // (255, 200, 0)
				}
				else
				{
					if (vert > 200 || bleu > 200)
						return listColors.indexOf(Color.white); // (255, 255, 255)
					else
						return listColors.indexOf(Color.pink); // (255, 175, 175)
				}
			}
		}
	}

	// Ajout le nom des deux couleurs dans le fichier texte (les deux plus grandes valeurs de l'arraylist)
	public void affichage(PrintWriter writer)
	{
		// Récupère l'indice de la valeur maximum (donc de la couleur dominante)
		int index = counter.indexOf(Collections.max(counter));

		// Une fois l'indice récupéré, on peut retrouver le nom de la couleur correspondante (qui est ajouté au fichier txt)
		writer.print(listColorsName.get(index));

		// Mise à zéro de la plus grande pour permettre de trouver la deuxième plus grande
		counter.set(index, 0);

		// Même principe pour la deuxième couleur
		writer.print(listColorsName.get(counter.indexOf(Collections.max(counter))));
	}

	// Permet de déterminer si l'image est considérée comme sombre ou clair
	public void sombreOuClair(PrintWriter writer, ImagePlus tempImg)
	{
		new ImageConverter(tempImg).convertToGray8();
		ImageProcessor ip = tempImg.getProcessor();

		if (AverageNdg(ip) > 127.5)
			writer.print("light, ");
		else
			writer.print("dark, ");
	}

	// Retourne la moyenne des NdG d’une image en NdG
	public double AverageNdg(ImageProcessor ip) {
		byte[] pixels = (byte[]) ip.getPixels();
		int width = ip.getWidth();
		int height = ip.getHeight();
		double total = 0.0;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
		   	total += pixels[y * width + x] & 0xff; // Ajout de la valeur de chacun des pixels dans la variable total

		  return total / (width * height);
   }

	// Remet à zéro les valeurs du compteur
	public void resetCounter()
	{
		for (int i = 0; i < listColors.size(); i++)
			counter.set(i, 0);
	}

	// Retourne un tableau d'objet de Type file contenant l'ensemble des images du dossier fourni en paramètre
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

   // Démarrage du plugin
   public int setup(String arg, ImagePlus imp) {
	  	if (arg.equals("about")) {
	   		IJ.showMessage("Traitement de l'image");
	   		return DONE;
	  	}
	  	return DOES_ALL; // Acception de toutes les images
 	}
}
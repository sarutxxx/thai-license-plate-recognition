package system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import plate.detection.Band;
import plate.detection.Car;
import plate.detection.Plate;
import plate.text.segmentation.TextSegment;
import utils.Utils;

public class Core {
	public static String logtag = "";
	private static int foundCount = 0;
	private static int notFoundCOunt = 0;
	private static int isPlateCount = 0;

	public static void detectPlate() {
		System.loadLibrary("opencv_java248");
		FileReader fileReader;
		BufferedReader bufferedReader;
		List<String> lines;
		String line;
		try {
			fileReader = new FileReader("sourcedata/CAR/car_name.txt");
			bufferedReader = new BufferedReader(fileReader);
			lines = new ArrayList<String>();
			line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();

			// remove old file
			File folder = new File("platelocalize/");
			folder.mkdir();
			File[] listOfFiles = folder.listFiles();
			for (int n = 0; n < listOfFiles.length; n++) {
				if (listOfFiles[n].isDirectory()) {
					File file = new File("platelocalize/"
							+ listOfFiles[n].getName());
					file.delete();
				}
				if (listOfFiles[n].isFile()) {
					File file = new File("platelocalize/"
							+ listOfFiles[n].getName());
					file.delete();
				}
			}

			String[] filename = lines.toArray(new String[lines.size()]);
			for (int i = 0; i < filename.length; i++) {

				folder = new File("log/");
				folder.mkdir();
				listOfFiles = folder.listFiles();
				for (int n = 0; n < listOfFiles.length; n++) {
					if (listOfFiles[n].isDirectory()) {
						(new File("log/" + listOfFiles[n].getName())).delete();
					}
				}
				logtag = filename[i].split(".j")[0].split(".p")[0];
				File file = new File("log/" + logtag);
				file.mkdir();
				System.out.println(filename[i]);

				Car car = new Car("sourcedata/CAR/" + filename[i]);
				List<Plate> plates = new ArrayList<Plate>();
				plates = car.clipPlatesMaxBandLimit(7);

				if (plates.size() <= 0) {
					notFoundCOunt++;
					System.out.println("image " + filename[i] + " not found");
					continue;
				} else {
					foundCount++;
					System.out
							.println("Foune candidate plate " + plates.size());
				}
				int p = 1;
				Mat showBounding = car.toMat().clone();
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

				for (Plate plate : plates) {
					Point p1 = new Point(plate.getBoundingRect().x,
							plate.getBoundingRect().y);
					Point p2 = new Point(plate.getBoundingRect().x
							+ plate.getBoundingRect().width,
							plate.getBoundingRect().y);
					Point p3 = new Point(plate.getBoundingRect().x
							+ plate.getBoundingRect().width,
							plate.getBoundingRect().y
									+ plate.getBoundingRect().height);
					Point p4 = new Point(plate.getBoundingRect().x,
							plate.getBoundingRect().y
									+ plate.getBoundingRect().height);
					contours.add(new MatOfPoint(p1, p2, p3, p4));
					Highgui.imwrite("platelocalize/" + logtag + "_PLATE_"
							+ (p++) + ".jpg", plate.toMat());
					List<Mat> charImageList = TextSegment
							.getListMatOfCharImage(plate.toMat());
					if (charImageList.size() >= 1) {
						file = new File("platelocalize/" + logtag + "_CHAR/");
						file.mkdir();
						Highgui.imwrite("platelocalize/" + logtag + "_CHAR/"
								+ logtag + "_PLATE_" + (p++) + ".jpg",
								plate.toMat());
						isPlateCount++;
					}
					int c = 0;
					for (Mat mat : charImageList) {
						Highgui.imwrite("platelocalize/" + logtag + "_CHAR/"
								+ logtag + "_CHAR_" + (c++) + ".jpg", mat);
					}
				}
				Imgproc.drawContours(showBounding, contours, -1, new Scalar(0,
						255, 0),3);
				Highgui.imwrite("platelocalize/" + logtag + "_LOCATE_" + (p++)
						+ ".jpg", showBounding);
				
				List<Band> bands = car.clipBands(7);
				List<MatOfPoint> bandBound = new ArrayList<MatOfPoint>();
				Band band = bands.get(bands.size()-1);
				Point b1 = new Point(band.getBoundingRect().x,
						band.getBoundingRect().y);
				Point b2 = new Point(band.getBoundingRect().x
						+ band.getBoundingRect().width,
						band.getBoundingRect().y);
				Point b3 = new Point(band.getBoundingRect().x
						+ band.getBoundingRect().width,
						band.getBoundingRect().y
								+ band.getBoundingRect().height);
				Point b4 = new Point(band.getBoundingRect().x,
						band.getBoundingRect().y
								+ band.getBoundingRect().height);
				bandBound.add(new MatOfPoint(b1, b2, b3, b4));
				showBounding = car.toMat().clone();
				Imgproc.drawContours(showBounding, bandBound, -1, new Scalar(0,
						255, 0),3);
				Highgui.imwrite("platelocalize/" + logtag + "_BAND.jpg", showBounding);
				
				Highgui.imwrite("platelocalize/" + logtag + "_VERTICALLINE.jpg", Utils.verticalLine(car.toMat()));
				
				Mat histogram = new Mat();
				histogram = Utils.histoGraph(Utils.verticalLine(car.toMat()), false, true);
				Highgui.imwrite("platelocalize/" + logtag + "_HISTLINE.jpg", histogram);
			}

			System.out.println("Found rect " + foundCount + " Not found "
					+ notFoundCOunt + " is Plate " + isPlateCount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readPlate(Car car) {
		return "unfinish";
	}

	public static void main(String[] args) {
		System.loadLibrary("opencv_java248");
		detectPlate();
	}
}

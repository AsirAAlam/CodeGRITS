package utils;

import java.io.IOException;

public class Heatmap {
    public static void genHeatmap(String pythonInterpreter, String pathToXML) throws IOException, InterruptedException {
        String pythonScript = """
                import sys
                import matplotlib.pyplot as plt
                import math
                from screeninfo import get_monitors
                import xml.etree.ElementTree as ET
                
                tree = ET.parse(sys.argv[1] + '/eye_tracking.xml')
                root = tree.getroot()
                
                width, height = get_monitors()[0].width, get_monitors()[0].height
                x = []
                y = []
                
                for gaze in root[1]:
                    left_eye = gaze.find('left_eye')
                    right_eye = gaze.find('right_eye')
                    
                    left_eye_x = float(left_eye.attrib.get('gaze_point_x'))
                    left_eye_y = float(left_eye.attrib.get('gaze_point_y'))
                    
                    right_eye_x = float(right_eye.attrib.get('gaze_point_x'))
                    right_eye_y = float(right_eye.attrib.get('gaze_point_y'))
                    
                    xcoord = (left_eye_x + right_eye_x) / 2
                    ycoord = (left_eye_y + right_eye_y) / 2
                    
                    x.append(xcoord * width)
                    y.append(ycoord * height)
                
                gcd = math.gcd(width, height)
                
                # Create the heatmap
                plt.figure(figsize=(width // gcd, height // gcd))
                ax = plt.gca()
                
                # Reduce number of bins so points are more visible
                xbins = 80
                
                # Scale down ybins to keep aspect ratio
                scale_down_factor = 80 / width
                ybins = int(height * scale_down_factor)
                
                plt.hist2d(x, y, bins=[xbins, ybins], range=[[0, width], [0, height]], cmap='inferno')
                ax.invert_yaxis()
                plt.tick_params(axis='x', top=True, labeltop=True, bottom=False, labelbottom=False)
                plt.colorbar()
                plt.title('Tracking Heatmap')
                plt.xlabel('X')
                plt.ylabel('Y')
                
                ax.xaxis.set_label_position('top')
                
                plt.savefig(sys.argv[1] + '/heatmap.png')
                """;

        ProcessBuilder pb = new ProcessBuilder(pythonInterpreter, "-c", pythonScript, pathToXML);

        pb.redirectErrorStream(true);

        Process process = pb.start();
        process.waitFor();
    }
}

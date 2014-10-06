import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.CRS;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import uk.ac.rdg.resc.edal.coverage.CoverageMetadata;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.ncwms.graphics.ImageProducer;

class CdmUtilsTest {

    public static void main(String[] args) throws Exception
    {
        NetcdfDataset nc = NetcdfDataset.openDataset("C:\\Godiva2_data\\Nancy DeLosa\\20120930_v_195359_l_0000000.nc");
        GridDataset gds = getGridDataset(nc);
        Collection<CoverageMetadata> cms = readCoverageMetadata(gds);
        for (CoverageMetadata cm : cms) {
            System.out.printf("%s (%s)%n", cm.getTitle(), cm.getId());
        }
        
        int width = 512;
        int height = 256;
        
        HorizontalGrid targetGrid = new RegularGridImpl(DefaultGeographicBoundingBox.WORLD, width, height);
        HorizontalGrid easeGrid = new RegularGridImpl(-2560000, -1760000, 2560000, 1760000,
                CRS.decode("EPSG:53408"), width, height);
        HorizontalGrid npsGrid = new RegularGridImpl(-10700000, -10700000, 14700000, 14700000,
                CRS.decode("EPSG:32661"), width, height);
        
        
        long start = System.nanoTime();
        List<Float> data = readHorizontalPoints(nc, "RemappedSatellite", 0, 0, npsGrid);
        long finish = System.nanoTime();
        System.out.printf("Read data in %f milliseconds%n", (finish - start) / 1e6);
        
        ImageProducer im = new ImageProducer.Builder()
            .width(width)
            .height(height)
            .build();
        
        im.addFrame(data, null);
        BufferedImage bim = im.getRenderedFrames().get(0);
        ImageIO.write(bim, "png", new File("C:\\Users\\Jon\\Desktop\\sat.png"));
        
        nc.close();
    
    }

}
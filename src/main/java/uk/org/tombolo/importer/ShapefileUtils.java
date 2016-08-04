package uk.org.tombolo.importer;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.projection.ProjectionException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ShapefileUtils extends AbstractImporter implements Importer{
    private static Logger log = LoggerFactory.getLogger(ShapefileUtils.class);

    public static FeatureReader getFeatureReader(ShapefileDataStore store, int index) throws IOException {
        DefaultQuery query = new DefaultQuery(store.getTypeNames()[index]);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    private static MathTransform makeCrsTransform(String inputFormat) throws FactoryException {
        CoordinateReferenceSystem sourceCrs = CRS.decode(inputFormat);
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:"+Subject.SRID, true);

        return CRS.findMathTransform(sourceCrs, targetCrs);
    }

    public static List<Subject> convertFeaturesToSubjects(FeatureReader<SimpleFeatureType, SimpleFeature> featureReader, SubjectType subjectType, ShapefileImporter importer) throws FactoryException, IOException, TransformException {
        MathTransform crsTransform = makeCrsTransform(importer.getEncoding());

        List<Subject> subjects = new ArrayList<>();
        while (featureReader.hasNext()) {
            SimpleFeature feature = featureReader.next();
            String label = importer.getFeatureSubjectLabel(feature, subjectType);
            String name = importer.getFeatureSubjectName(feature, subjectType);
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            try {
                Geometry transformedGeom = JTS.transform(geom, crsTransform);
                transformedGeom.setSRID(Subject.SRID);
                subjects.add(new Subject(subjectType, label, name, transformedGeom));
            } catch (ProjectionException e) {
                log.warn("Rejecting {}. You will see this if you have assertions enabled (e.g. " +
                        "you run with `-ea`) as GeoTools runs asserts. See source of ShapefileUtils for details on this. " +
                        "To fix this, replace `-ea` with `-ea -da:org.geotools...` in your test VM options (probably in" +
                        "your IDE) to disable assertions in GeoTools.", label);
            }
        }
        return subjects;
    }
}
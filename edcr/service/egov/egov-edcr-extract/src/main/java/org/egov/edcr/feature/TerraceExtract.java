package org.egov.edcr.feature;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Terrace;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TerraceExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(TerraceExtract.class);
   
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail extract(PlanDetail pl) {

        if (LOG.isInfoEnabled())
            LOG.info("Starting of terrace Extract......");

        for (Block block : pl.getBlocks()) {

            // Layer name pattern like BLK_1_TERRACE, BLK_2_TERRACE etc.
            String terraceLayerName = String.format(
                    layerNames.getLayerName("LAYER_NAME_TERRACE"),
                    block.getNumber()
            );

            if (!pl.getDoc().containsDXFLayer(terraceLayerName))
                continue;

            List<DXFLWPolyline> terracePolyline = Util.getPolyLinesByLayer(
                    pl.getDoc(),
                    terraceLayerName
            );

            if (terracePolyline == null || terracePolyline.isEmpty())
                continue;

            for (DXFLWPolyline pline : terracePolyline) {

                Measurement measurement = new MeasurementDetail(pline, true);

                Terrace terrace = new Terrace();
                terrace.setArea(measurement.getArea());
                terrace.setHeight(measurement.getHeight());
                terrace.setWidth(measurement.getWidth());
                terrace.setLength(measurement.getLength());
                terrace.setInvalidReason(measurement.getInvalidReason());
                terrace.setPresentInDxf(true);
                block.setTerrace(terrace);
            }
        }

        if (LOG.isInfoEnabled())
            LOG.info("End of terrace Extract......");

        return pl;
    }

}

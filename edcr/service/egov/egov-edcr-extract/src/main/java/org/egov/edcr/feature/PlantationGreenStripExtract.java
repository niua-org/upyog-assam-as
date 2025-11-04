package org.egov.edcr.feature;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Measurement;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlantationGreenStripExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(PlantationGreenStripExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        for (Block block : planDetail.getBlocks()) {
            String plantationGreenStrip = String.format(layerNames.getLayerName("LAYER_NAME_PLANTATION_GREENSTRIP"),
                    block.getNumber());
            List<DXFLWPolyline> plantationGreenStripes = Util.getPolyLinesByLayer(planDetail.getDoc(),
                    plantationGreenStrip);
            List<Measurement> plantationGreenStripeMeasurements = plantationGreenStripes.stream()
                    .map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());

            block.setPlantationGreenStripes(plantationGreenStripeMeasurements);
            
            extractLandscapingArea(planDetail, block);
            
            extractUnpavedArea(planDetail, block);
        }

        return planDetail;
    }
    
    /**
     * Extracts and sets the landscaping area for the given block.
     *
     * @param planDetail the PlanDetail object containing the DXF document
     * @param block the Block for which the landscaping area is to be extracted
     * @param layerNames the LayerNames utility for retrieving the correct layer name
     */
    public void extractLandscapingArea(PlanDetail planDetail, Block block) {
        LOG.info("Starting landscaping area extraction for Block {}", block.getNumber());
        try {
            String landscapingArea = String.format(layerNames.getLayerName("LAYER_NAME_LANDSCAPING_AREA"),
                    block.getNumber());
            List<DXFLWPolyline> landscapePolylines = Util.getPolyLinesByLayer(planDetail.getDoc(), landscapingArea);

            List<Measurement> landscapingMeasurements = landscapePolylines.stream()
                    .map(polyline -> new MeasurementDetail(polyline, true))
                    .collect(Collectors.toList());

            block.setLandscapingArea(landscapingMeasurements);
            LOG.info("Successfully extracted {} landscaping measurements for Block {}", 
                     landscapingMeasurements.size(), block.getNumber());
        } catch (Exception e) {
            LOG.error("Error while extracting landscaping area for Block {}: {}", block.getNumber(), e.getMessage());
        }
    }

    
    /**
     * Extracts and sets the unpaved area for the given block.
     *
     * @param planDetail the PlanDetail object containing the DXF document
     * @param block the Block for which the unpaved area is to be extracted
     * @param layerNames the LayerNames utility for retrieving the correct layer name
     */
    public void extractUnpavedArea(PlanDetail planDetail, Block block) {
        LOG.info("Starting unpaved area extraction for Block {}", block.getNumber());
        try {
            String unpavedArea = String.format(layerNames.getLayerName("LAYER_NAME_UNPAVED_AREA"),
                    block.getNumber());
            List<DXFLWPolyline> unpavedPolylines = Util.getPolyLinesByLayer(planDetail.getDoc(), unpavedArea);

            List<Measurement> unpavedMeasurements = unpavedPolylines.stream()
                    .map(polyline -> new MeasurementDetail(polyline, true))
                    .collect(Collectors.toList());

            block.setUnpavedArea(unpavedMeasurements);
            LOG.info("Successfully extracted {} unpaved measurements for Block {}", 
                     unpavedMeasurements.size(), block.getNumber());
        } catch (Exception e) {
            LOG.error("Error while extracting unpaved area for Block {}: {}", block.getNumber(), e.getMessage());
        }
    }

}

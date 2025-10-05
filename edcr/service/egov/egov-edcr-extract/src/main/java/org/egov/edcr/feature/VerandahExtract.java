package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerandahExtract extends FeatureExtract {

	private static final Logger LOG = LogManager.getLogger(VerandahExtract.class);
	@Autowired
	private LayerNames layerNames;

	@Override
	public PlanDetail extract(PlanDetail pl) {
		for (Block b : pl.getBlocks()) {
			if (b.getBuilding() != null && b.getBuilding().getFloors() != null
					&& !b.getBuilding().getFloors().isEmpty()) {
				for (Floor f : b.getBuilding().getFloors()) {
                    if(f.getUnits() != null && !f.getUnits().isEmpty())
                        for(FloorUnit floorUnit : f.getUnits()) {

                            List<DXFLWPolyline> verandahs = Util.getPolyLinesByLayer(pl.getDoc(),
                                    String.format(layerNames.getLayerName("LAYER_NAME_UNIT_VERANDAH"), b.getNumber(), f.getNumber(), floorUnit.getUnitNumber()));
                            if (!verandahs.isEmpty()) {
                                List<Measurement> verandahMeasurements = verandahs.stream()
                                        .map(polyline -> new MeasurementDetail(polyline, true)).collect(Collectors.toList());
                                f.getVerandah().setMeasurements(verandahMeasurements);

//						f.getVerandah()
//								.setHeightOrDepth((Util.getListOfDimensionValueByLayer(pl,
//										String.format(layerNames.getLayerName("LAYER_NAME_VERANDAH"),
//												b.getNumber(), f.getNumber()))));

                                String verandahLayer = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_VERANDAH"),
                                        b.getNumber(), f.getNumber(), floorUnit.getUnitNumber());
                                // Verandah Height from dimension
                                List<BigDecimal> verandahHeight =
                                        Util.getListOfDimensionByColourCode(pl, verandahLayer,
                                                DxfFileConstants.INDEX_COLOR_ONE);

                                // Verandah Width from dimension
                                List<BigDecimal> verandahWidth =
                                        Util.getListOfDimensionByColourCode(pl, verandahLayer,
                                                DxfFileConstants.INDEX_COLOR_TWO);

                                floorUnit.getVerandah().setHeightOrDepth(verandahHeight);
                                floorUnit.getVerandah().setVerandahWidth(verandahWidth);

                            }
                        }
				}
			}
		}

		return pl;
	}

	@Override
	public PlanDetail validate(PlanDetail pl) {
		return pl;
	}

}

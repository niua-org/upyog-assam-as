package org.egov.bpa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NOCEvaluator {

	private static final Map<String, Function<Map<String, String>, Boolean>> conditionChecks = new HashMap<>();

	static {
		conditionChecks.put("G_PLUS_3_BUILDINGS", edcr -> "G+3".equalsIgnoreCase(edcr.get("BUILDING_CATEGORY")));
		conditionChecks.put("SPECIAL_STRUCTURES", edcr -> "SPECIAL".equalsIgnoreCase(edcr.get("BUILDING_TYPE")));
		conditionChecks.put("BUILDING_HEIGHT_GREATER_THAN_12M", edcr -> getDouble(edcr, "BUILDING_HEIGHT") > 12);
		conditionChecks.put("BUILDING_HEIGHT_GREATER_THAN_15.8M", edcr -> getDouble(edcr, "BUILDING_HEIGHT") > 15);
		conditionChecks.put("HAZARDOUS_INDUSTRIES", edcr -> "HAZARDOUS".equalsIgnoreCase(edcr.get("INDUSTRY_TYPE")));
		conditionChecks.put("LARGE_COMMERCIAL_OR_HOSPITALS_OR_HOTELS", edcr -> {
			String occupancy = edcr.get("OCCUPANCY");
			return occupancy != null && (occupancy.equalsIgnoreCase("COMMERCIAL")
					|| occupancy.equalsIgnoreCase("HOSPITAL") || occupancy.equalsIgnoreCase("HOTEL"));
		});
		conditionChecks.put("BUILT_UP_AREA_GREATER_THAN_OR_EQUAL_TO_2_LAKH_SQ_FT",
				edcr -> getDouble(edcr, "TOTAL_BUILTUP_AREA") >= 200000);
		conditionChecks.put("BUILDINGS_WITH_LIFTS", edcr -> getDouble(edcr, "NUMBER_OF_LIFTS") > 0);
		conditionChecks.put("ALL_NON_RESIDENTIAL_BUILDINGS", edcr -> "RESIDENTIAL".equalsIgnoreCase(edcr.get("OCCUPANCY"))); //TODO: Change to !=
	}

	private static double getDouble(Map<String, String> edcr, String key) {
		try {
			return Double.parseDouble(edcr.getOrDefault(key, "0"));
		} catch (Exception e) {
			return 0.0;
		}
	}

	public static boolean isAllConditionsTrue(Map<String, String> edcrResponse, List<String> conditions) {
		log.info("Evaluating conditions: {} on EDCR response: {}", conditions, edcrResponse);
		return conditions.stream().allMatch(cond -> conditionChecks.getOrDefault(cond, e -> false).apply(edcrResponse));
	}

	public List<String> getApplicableNOCList(Map<String, List<String>> nocTypeConditionsMap,
			Map<String, String> edcrResponse) {

		List<String> applicable = new ArrayList<>();

		for (Map.Entry<String, List<String>> entry : nocTypeConditionsMap.entrySet()) {
			String type = entry.getKey();
			List<String> conditions = entry.getValue();
			if (isAllConditionsTrue(edcrResponse, conditions)) {
				log.info("NOC Type: {} is applicable as all conditions: {} are met.", type, conditions);
				applicable.add(type);
			}
		}
		
		return applicable;
	}

}

/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.common.entity.edcr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Room {

    private String number;

    private List<RoomHeight> heightOfRooms = new ArrayList<>();
    
    private Boolean closed = false;

    private List<Measurement> rooms = new ArrayList<>();

    private MeasurementWithHeight lightAndVentilation = new MeasurementWithHeight();
   
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Measurement> bathVentilation = new ArrayList<>();

    private List<Occupancy> mezzanineAreas = new ArrayList<>();

    private List<Measurement> waterClosetVentialtion = new ArrayList<>();
    
    private List<Measurement> commonRoomVentialtion = new ArrayList<>();
    
    public List<Measurement> getCommonRoomVentialtion() {
		return commonRoomVentialtion;
	}

	public void setCommonRoomVentialtion(List<Measurement> commonRoomVentialtion) {
		this.commonRoomVentialtion = commonRoomVentialtion;
	}

	private List<Window> windows = new ArrayList<>();
    
    private List<Door> doors = new ArrayList<>();

    private BigDecimal hillyAreaRoomHeight;
    
    private BigDecimal kitchenWindowHeight;

    private List<Projections> roomProjections =  new ArrayList<>();
    
    private List<BigDecimal> kitchenWidth;
    
    private List<BigDecimal> kitchenDoorWidth;
    
    public List<BigDecimal> getKitchenDoorWidth() {
		return kitchenDoorWidth;
	}

	public void setKitchenDoorWidth(List<BigDecimal> kitchenDoorWidth) {
		this.kitchenDoorWidth = kitchenDoorWidth;
	}

	public List<BigDecimal> getKitchenDoorHeight() {
		return kitchenDoorHeight;
	}

	public void setKitchenDoorHeight(List<BigDecimal> kitchenDoorHeight) {
		this.kitchenDoorHeight = kitchenDoorHeight;
	}

	private List<BigDecimal> kitchenDoorHeight;
    
    private List<BigDecimal> kitchenWindowWidth;
    
    public BigDecimal getKitchenWindowHeight() {
		return kitchenWindowHeight;
	}

	public void setKitchenWindowHeight(BigDecimal kitchenWindowHeight) {
		this.kitchenWindowHeight = kitchenWindowHeight;
	}

	public List<BigDecimal> getKitchenWindowWidth() {
		return kitchenWindowWidth;
	}

	public void setKitchenWindowWidth(List<BigDecimal> kitchenWindowWidth) {
		this.kitchenWindowWidth = kitchenWindowWidth;
	}

	private List<BigDecimal> roomWidth;

    public List<BigDecimal> getRoomWidth() {
		return roomWidth;
	}

	public void setRoomWidth(List<BigDecimal> roomWidth) {
		this.roomWidth = roomWidth;
	}

	public List<BigDecimal> getKitchenWidth() {
		return kitchenWidth;
	}

	public void setKitchenWidth(List<BigDecimal> kitchenWidth) {
		this.kitchenWidth = kitchenWidth;
	}

	public BigDecimal getHillyAreaRoomHeight() {
		return hillyAreaRoomHeight;
	}

	public void setHillyAreaRoomHeight(BigDecimal hillyAreaRoomHeight) {
		this.hillyAreaRoomHeight = hillyAreaRoomHeight;
	}

	public List<RoomHeight> getHeights() {
        return heightOfRooms;
    }

    public void setHeights(List<RoomHeight> heights) {
        this.heightOfRooms = heights;
    }

    /**
     * @return the closed
     */
    public Boolean getClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    /**
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * @return the lightAndVentilation
     */
    public MeasurementWithHeight getLightAndVentilation() {
        return lightAndVentilation;
    }

    /**
     * @param lightAndVentilation the lightAndVentilation to set
     */
    public void setLightAndVentilation(MeasurementWithHeight lightAndVentilation) {
        this.lightAndVentilation = lightAndVentilation;
    }
    
  
    public List<RoomHeight> getHeightOfRooms() {
		return heightOfRooms;
	}

	public void setHeightOfRooms(List<RoomHeight> heightOfRooms) {
		this.heightOfRooms = heightOfRooms;
	}

	public List<Measurement> getBathVentilation() {
	    return bathVentilation == null ? Collections.emptyList() : bathVentilation;
	}


	public void setBathVentilation(List<Measurement> bathVentilation) {
		this.bathVentilation = bathVentilation;
	}

	public List<Measurement> getWaterClosetVentialtion() {
		return waterClosetVentialtion;
	}

	public void setWaterClosetVentialtion(List<Measurement> waterClosetVentialtion) {
		this.waterClosetVentialtion = waterClosetVentialtion;
	}

	public void setWaterClosetVentilation(List<Measurement> waterClosetVentialtion) {
		this.waterClosetVentialtion = waterClosetVentialtion;
	}
    public List<Measurement> getRooms() {
        return rooms;
    }

    public void setRooms(List<Measurement> rooms) {
        this.rooms = rooms;
    }

    public List<Occupancy> getMezzanineAreas() {
        return mezzanineAreas;
    }

    public void setMezzanineAreas(List<Occupancy> mezzanineAreas) {
        this.mezzanineAreas = mezzanineAreas;
    }
    
    public List<Window> getWindows() {
  		return windows;
  	}

  	public void setWindows(List<Window> windows) {
  		this.windows = windows;
  	}
  	
  	public void addWindow(Window window) {
          this.windows.add(window);
      }
  	
  	 public List<Door> getDoors() {
   		return doors;
   	}

   	public void setDoors(List<Door> doors) {
   		this.doors = doors;
   	}
   	
   	public void addDoors(Door doors) {
           this.doors.add(doors);
       }

    public List<Projections> getRoomProjections() {
        return roomProjections;
    }

    public void setRoomProjections(List<Projections> roomProjections) {
        this.roomProjections = roomProjections;
    }
}

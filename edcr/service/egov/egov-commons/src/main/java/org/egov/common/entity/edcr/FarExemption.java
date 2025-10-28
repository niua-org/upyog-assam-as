package org.egov.common.entity.edcr;

import java.io.Serializable;
import java.math.BigDecimal;

public class FarExemption implements Serializable {
    private BigDecimal basementParking = BigDecimal.ZERO;
    private BigDecimal basementServiceFloor = BigDecimal.ZERO;
    private BigDecimal entranceLobby = BigDecimal.ZERO;
    private BigDecimal balcony = BigDecimal.ZERO;
    private BigDecimal corridor = BigDecimal.ZERO;
    private BigDecimal projection = BigDecimal.ZERO;
    private BigDecimal guardRoom = BigDecimal.ZERO;
    private BigDecimal careTakerRoom = BigDecimal.ZERO;

    public BigDecimal getBasementParking() {
        return basementParking;
    }

    public void setBasementParking(BigDecimal basementParking) {
        this.basementParking = basementParking;
    }

    public void addBasementParking(BigDecimal basementParking) {
        this.basementParking = this.basementParking.add(basementParking);
    }

    public BigDecimal getBasementServiceFloor() {
        return basementServiceFloor;
    }

    public void setBasementServiceFloor(BigDecimal basementServiceFloor) {
        this.basementServiceFloor = basementServiceFloor;
    }

    public void addBasementServiceFloor(BigDecimal serviceFloor){
        this.basementServiceFloor = this.basementServiceFloor.add(serviceFloor);
    }

    public BigDecimal getEntranceLobby() {
        return entranceLobby;
    }

    public void setEntranceLobby(BigDecimal entranceLobby) {
        this.entranceLobby = entranceLobby;
    }

    public void addEntranceLobby(BigDecimal entranceLobby){
        this.entranceLobby = this.entranceLobby.add(entranceLobby);
    }

    public BigDecimal getBalcony() {
        return balcony;
    }

    public void setBalcony(BigDecimal balcony) {
        this.balcony = balcony;
    }

    public void addBalcony(BigDecimal balcony){
        this.balcony = this.balcony.add(balcony);
    }

    public BigDecimal getCorridor() {
        return corridor;
    }

    public void setCorridor(BigDecimal corridor) {
        this.corridor = corridor;
    }

    public BigDecimal getProjection() {
        return projection;
    }

    public void setProjection(BigDecimal projection) {
        this.projection = projection;
    }

    public void addCorridor(BigDecimal corridor){
        this.corridor = this.corridor.add(corridor);
    }

    public void addProjection(BigDecimal projection){
        this.projection = this.projection.add(projection);
    }

    public BigDecimal getGuardRoom() {
        return guardRoom;
    }

    public void setGuardRoom(BigDecimal guardRoom) {
        this.guardRoom = guardRoom;
    }

    public BigDecimal getCareTakerRoom() {
        return careTakerRoom;
    }

    public void setCareTakerRoom(BigDecimal careTakerRoom) {
        this.careTakerRoom = careTakerRoom;
    }

    public void addGuardRoom(BigDecimal guardRoom){
        this.guardRoom = this.guardRoom.add(guardRoom);
    }

    public void addCareTakerRoom(BigDecimal careTakerRoom){
        this.careTakerRoom = this.careTakerRoom.add(careTakerRoom);
    }
}

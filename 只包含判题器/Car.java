package com.huawei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Car implements Comparable<Car>{
	private int carId;
	private int fromId;
	private int toId;
	private Cross from;
	private Cross to;
	private int speed;
	private int planTime;
	private int priority;
	private int preset;
	private LinkedList<Road> planRoads;
	private LinkedList<Road> pickedRoads;
	public boolean flagChanged=false;

	Car(int carId , int fromId , int toId , int speed , int planTime,int priority,int preset){
		this.setCarId(carId);
		this.setFromId(fromId);
		this.setToId(toId);
		this.setSpeed(speed);
		this.setPlanTime(planTime);
		this.setPriority(priority);
		this.setPreset(preset);
		planRoads=new LinkedList<>();
		pickedRoads=new LinkedList<>();
	}

	public void updatePickedRoads(){
		pickedRoads.add(planRoads.getFirst());
		planRoads.remove(0);
	}

	public void setPlanRoads(LinkedList<Road> planRoads){
		this.planRoads=new LinkedList<>(planRoads);
		//planRoads.add(null);
	}

	public void updateFromAndTo(HashMap<Integer,Cross> crosses){
		this.setFrom(crosses.get(this.fromId));
		this.setTo(crosses.get(this.toId));
	}

	public int getCarId() {
		return carId;
	}

	public void setCarId(int carId) {
		this.carId = carId;
	}

	public int getFromId() {
		return fromId;
	}

	public void setFromId(int fromId) {
		this.fromId = fromId;
	}

	public int getToId() {
		return toId;
	}

	public void setToId(int toId) {
		this.toId = toId;
	}

	public void setFrom(Cross from) {
		this.from = from;
	}

	public void setTo(Cross to) {
		this.to = to;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPlanTime() {
		return planTime;
	}

	public void setPlanTime(int planTime) {
		this.planTime = planTime;
	}

	public boolean getPriority() {
		if(priority==1)
			return true;
		else
			return false;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPreset() {
		return preset;
	}

	public void setPreset(int preset) {
		this.preset = preset;
	}

	public Cross getFrom() {
		return from;
	}

	public Cross getTo() {
		return to;
	}

	@Override
	public int compareTo(Car o) {
		if(this.carId<o.getCarId())
			return -1;
		else if(this.carId>o.getCarId())
			return 1;
		else
			return 0;
	}

	public LinkedList<Road> getPickedRoads() {
		return pickedRoads;
	}

	public void setPickedRoads(LinkedList<Road> pickedRoads) {
		this.pickedRoads = pickedRoads;
	}

	public LinkedList<Road> getPlanRoads() {
		return planRoads;
	}

	public String getNextDirection(){
		Road currentRoad = this.pickedRoads.getLast();
		if(this.planRoads.size()==0){
			return "终";
		}
		Road nextRoad = this.planRoads.getFirst();
		Cross nextCross = currentRoad.getCrossByNextRoad(nextRoad);
		int d = nextCross.getDirection(currentRoad,nextRoad);
		switch (d){
			case 0:
				return "异常";
			case 1:
				return  "直";
			case 2:
				return  "左";
			case 3:
				return  "右";
		}
		return "异常";
	}
}

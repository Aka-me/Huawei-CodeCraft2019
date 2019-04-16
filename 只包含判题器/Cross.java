package com.huawei;

import java.util.*;

public class Cross implements Comparable<Cross>{
	private int crossId;
	private int roadId_North;
	private int roadId_East;
	private int roadId_South;
	private int roadId_West;
	private Road[] nextRoads;
	private LinkedList<Car[][]> toChannels;
	private LinkedList<Car[][]> fromChannels;
	private Road road_North;
	private Road road_East;
	private Road road_South;
	private Road road_West;
	private HashMap<Cross,LinkedList<Road>> planRoads;
	public int usedCount;


	Cross (int crossId , int roadId_North , int roadId_East , int roadId_South , int roadId_West){
		this.setCrossId(crossId);
		this.setRoadId_North(roadId_North);
		this.setRoadId_East(roadId_East);
		this.setRoadId_South(roadId_South);
		this.setRoadId_West(roadId_West);
	}

	public void setPlanRoads(HashMap<Cross,LinkedList<Road>> planRoads){
		this.planRoads=planRoads;
		//这里的路径是当前路口到所有其他路口的路径
		//注意对象引用,在外面修改后这里也会被改变.
	}

	public Road getOppoRoad(Road currentRoad){
		int currentIdex=getIndexOfCurrentRoad(currentRoad);
		if(currentIdex==-1)
			System.out.println("???????");
		int oppoIdex=(currentIdex+2)%4;
		return nextRoads[oppoIdex];
	}

	public Road getRightRoad(Road currentRoad){
		int currentIdex=getIndexOfCurrentRoad(currentRoad);
		if(currentIdex==-1)
			System.out.println("???????");
		int rightIdex=(currentIdex+3)%4;
		return nextRoads[rightIdex];
	}

	public Road getLeftRoad(Road currentRoad){
		int currentIdex=getIndexOfCurrentRoad(currentRoad);
		if(currentIdex==-1)
			System.out.println("???????");
		int leftIdex=(currentIdex+1)%4;
		return nextRoads[leftIdex];
	}

	private int getIndexOfCurrentRoad(Road currentRoad){
		for (int i = 0; i < 4; i++) {
			if(currentRoad==nextRoads[i])
				return i;
		}
		return -1;
	}

	public int getDirection(Road currentRoad,Road nextRoad){
		int currentIndex=-1;
		int nextIndex=-1;
		for (int i = 0; i < 4; i++) {
			if(nextRoads[i]==currentRoad)
				currentIndex=i;
			if(nextRoads[i]==nextRoad)
				nextIndex=i;
		}
		if(nextIndex==-1){//如果该车辆即将到达终点,即planroads.getfirst()==null,将该车看做直行
			return 1;
		}

		int flag=nextIndex-currentIndex;
		if(flag==1||flag==-3)
			return 2;//左转
		else if(flag==3||flag==-1)
			return 3;//右转
		else if(flag==2||flag==-2)
			return 1;//直行
		else
			return 0;
	}

	public LinkedList<Car[][]> getAllToChannels(){
		//需要测试一下顺序
		return toChannels;
	}

	public LinkedList<Road> getAllValidRoads(){
		//只返回从当前路口出去的道路，考虑单行道。
		LinkedList<Road> allValidRoads=new LinkedList<>();
		for (int i = 0; i < nextRoads.length; i++) {
			if(nextRoads[i]!=null){
				if(nextRoads[i].getIsDuplex()==1)
					allValidRoads.add(nextRoads[i]);
				else if(nextRoads[i].getFrom()==this)
					allValidRoads.add(nextRoads[i]);
			}
		}
		if (allValidRoads.size()==0)
			return null;
		return allValidRoads;
	}

	public void updateEachRoad(HashMap<Integer,Road> roads){
		nextRoads=new Road[4];
		road_North=nextRoads[0]=roads.get(roadId_North);
		road_East=nextRoads[1]=roads.get(roadId_East);
		road_South=nextRoads[2]=roads.get(roadId_South);
		road_West=nextRoads[3]=roads.get(roadId_West);

		LinkedList<Road> orderedNextRoads=new LinkedList<>();
		for (int i = 0; i < 4; i++) {
			if(nextRoads[i]!=null)
				orderedNextRoads.add(nextRoads[i]);
		}
		Collections.sort(orderedNextRoads);

		toChannels=new LinkedList<>();
		for (int i = 0; i < orderedNextRoads.size(); i++) {
			if(orderedNextRoads.get(i).getTo()==this)
				toChannels.add(orderedNextRoads.get(i).getFromTo());
			else if(orderedNextRoads.get(i).getIsDuplex()==1)
				toChannels.add(orderedNextRoads.get(i).getToFrom());
		}

	}

	public void setCrossId(int crossId) {
		this.crossId = crossId;
	}

	public int getCrossId() {
		return crossId;
	}

	public int getRoadId_North() {
		return roadId_North;
	}

	public void setRoadId_North(int roadId_North) {
		this.roadId_North = roadId_North;
	}

	public int getRoadId_East() {
		return roadId_East;
	}

	public void setRoadId_East(int roadId_East) {
		this.roadId_East = roadId_East;
	}

	public int getRoadId_South() {
		return roadId_South;
	}

	public void setRoadId_South(int roadId_South) {
		this.roadId_South = roadId_South;
	}

	public int getRoadId_West() {
		return roadId_West;
	}

	public void setRoadId_West(int roadId_West) {
		this.roadId_West = roadId_West;
	}

	public Road getRoad_North() {
		return road_North;
	}

	public void setRoad_North(Road road_North) {
		this.road_North = road_North;
	}

	public Road getRoad_East() {
		return road_East;
	}

	public void setRoad_East(Road road_East) {
		this.road_East = road_East;
	}

	public Road getRoad_South() {
		return road_South;
	}

	public void setRoad_South(Road road_South) {
		this.road_South = road_South;
	}

	public Road getRoad_West() {
		return road_West;
	}

	public void setRoad_West(Road road_West) {
		this.road_West = road_West;
	}

	//打印该路口所有的道路信息
	public void printRoad(){
		for(int i=0;i<4;i++){
			if(nextRoads[i]!=null){
				System.out.print("   **"+i+"**  ");
				nextRoads[i].printRoad(this);
			}
		}
	}

	@Override
	public int compareTo(Cross o) {
		if(this.crossId<o.getCrossId())
			return -1;
		else if(this.crossId>o.getCrossId())
			return 1;
		else
			return 0;
	}
}

package com.huawei;

import java.util.Collection;
import java.util.HashMap;

public class Road implements Comparable<Road> {
	private int roadId;
	private int length;
	private int oriLength;
	private int speed;
	private int channel;
	private Cross from;
	private int fromId;
	private Cross to;
	private int toId;
	private int isDuplex;
	private Car[][] fromTo;
	private Car[][] toFrom;
	public int fromToLoad=0;//道路负载
	public int toFromLoad=0;
	public int planFromToLoad=0;//道路负载
	public int planToFromLoad=0;
	public int fromToLength=0;
	public int toFromLength=0;

	Road(int roadId , int length , int speed , int channel , int fromId , int toId , int isDuplex){
		this.setRoadId(roadId);
		this.setLength(length);
		this.setSpeed(speed);
		this.setChannel(channel);
		this.setFromId(fromId);
		this.setToId(toId);
		this.setIsDuplex(isDuplex);
		this.setFromTo();
		this.setToFrom();
	}

	public void setLoadFator(double factor,boolean dir){
		if(dir)
			this.fromToLength*=factor;
		else
			this.toFromLength*=factor;
	}

	public int getFromToLoad(){
		return fromToLoad;
	}

	public int getToFromLoad(){
		return toFromLoad;
	}

	public int getLoadByNextCross(Cross nextCross){
		if(nextCross==to)
			return fromToLoad;
		else
			return toFromLoad;
	}

	public void addLoad(Car[][] roadInfo){
		if(roadInfo==fromTo)
			fromToLoad++;
		else
			toFromLoad++;
	}

	public void minLoad(Car[][] roadInfo){
		if(roadInfo==fromTo)
			fromToLoad--;
		else
			toFromLoad--;
	}

	public void addPlanLoad(Cross lastCross){
		if(lastCross==from)
			planFromToLoad++;
		else
			planToFromLoad++;
	}

	public void minPlanLoad(Cross lastCross){
		if(lastCross==from)
			planFromToLoad--;
		else
			planToFromLoad--;
	}

	public Car[][] getIntoFromCross(Cross currentCross){
		if(currentCross==to)
			return fromTo;
		else {
//            if(this.isDuplex==0)
//                System.out.println("出发车辆道路错误，不存在该方向道路。");
			return toFrom;
		}
	}

	public Car[][] getOutFromCross(Cross currentCross){
		if(currentCross==from)
			return fromTo;
		else {
//            if(this.isDuplex==0)
//                System.out.println("出发车辆道路错误，不存在该方向道路。");
			return toFrom;
		}
	}

	public Cross getNextCross(Cross currentCross){
		if(currentCross==this.from)
			return to;
		else
			return from;
	}

	public Cross getCrossByNextRoad(Road nextRoad){
		if(this.to==nextRoad.from||this.to==nextRoad.to)
			return to;
		else
			return from;
	}

	public void updateFromAndTo(HashMap<Integer,Cross> crosses){
		setFrom(crosses.get(this.fromId));
		setTo((crosses.get(this.toId)));
	}

	public int getRoadId() {
		return roadId;
	}

	public void setRoadId(int roadId) {
		this.roadId = roadId;
	}

	public int getLength() {
		return length;
	}

	public int getLoadLength(Cross currentCross){
		if(currentCross==from)
			return fromToLength;
		else
			return toFromLength;
	}

	public void setLength(int length) {
		this.length = length;
		this.oriLength=length;
		this.fromToLength=length;
		this.toFromLength=length;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
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

	public int getIsDuplex() {
		return isDuplex;
	}

	public void setIsDuplex(int isDuplex) {
		this.isDuplex = isDuplex;
	}

	public void setFrom(Cross from) {
		this.from = from;
	}

	public Cross getFrom(){
		return this.from;
	}

	public void setTo(Cross to) {
		this.to = to;
	}

	public Cross getTo(){
		return this.to;
	}

	public Car[][] getFromTo() {
		return fromTo;
	}

	public void setFromTo() {
		this.fromTo = new Car[this.channel][this.length];
	}

	public Car[][] getToFrom() {
		return toFrom;
	}

	public void setToFrom() {
		if(this.isDuplex==1)
			this.toFrom=new Car[this.channel][this.length];
		else
			this.toFrom=null;
	}

	//输出道路负载，n：道路负载最高为n米内一辆车
	public void printLoad(double n){
		System.out.println("道路："+roadId+"    负载要求："+length*channel/n + "当前道路（正）负载："+fromToLoad + "当前道路（反）负载："+toFromLoad);
	}

	//通过当前路口获取道路负载
	public int getLoadByCurrentCross(Cross currentCross){
		if(currentCross==from)
			return fromToLoad;
		else
			return toFromLoad;
	}

	/*打印车道上车辆信息*/
	public void printRoad(Cross currentCross){
		if(currentCross==from && isDuplex==1){//第一种情况
			System.out.println(" fromTo:       道路："+roadId);
			for(int j=0;j<channel;j++){/*打印fromTo*/
				for(int i=length-1;i>=0;i--){
					if(fromTo[j][i]==null){
						System.out.print("00000<--");
					}
					else{
						if(fromTo[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(fromTo[j][i].getCarId()+"<--");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
			System.out.println(" toFrom:");
			for(int j=0;j<channel;j++){/*打印toFrom*/
				for(int i=0;i<length;i++){
					if(toFrom[j][i]==null){
						System.out.print("00000-->");
					}
					else{
						if(toFrom[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(toFrom[j][i].getCarId()+"-->");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
		}
		else if(currentCross==from && isDuplex==0){/*打印toFrom*/
			System.out.println(" fromTo:       道路："+roadId);
			for(int j=0;j<channel;j++){/*打印fromTo*/
				for(int i=length-1;i>=0;i--){
					if(fromTo[j][i]==null){
						System.out.print("000000<--");
					}
					else{
						if(fromTo[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(fromTo[j][i].getCarId()+"<--");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
		}
		if(currentCross==to && isDuplex==1){//第二种情况
			System.out.println(" toFrom:       道路："+roadId);
			for(int j=0;j<channel;j++){/*打印toFrom*/
				for(int i=length-1;i>=0;i--){
					if(toFrom[j][i]==null){
						System.out.print("000000<--");
					}
					else{
						if(toFrom[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(toFrom[j][i].getCarId()+"<--");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
			System.out.println(" fromTo:");
			for(int j=0;j<channel;j++){/*打印fromTo*/
				for(int i=0;i<length;i++){
					if(fromTo[j][i]==null){
						System.out.print("000000-->");
					}
					else{
						if(fromTo[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(fromTo[j][i].getCarId()+"-->");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
		}
		else if(currentCross==to && isDuplex==0){/*打印toFrom*/
			System.out.println(" fromTo:       道路："+roadId);
			for(int j=0;j<channel;j++){/*打印fromTo*/
				for(int i=0;i<length;i++){
					if(fromTo[j][i]==null){
						System.out.print("000000-->");
					}
					else{
						if(fromTo[j][i].getPriority())
							System.out.print("*");
						else
							System.out.print("~");
						System.out.print(fromTo[j][i].getCarId()+"-->");
					}
				}
				System.out.println("  ("+j+")   路口："+currentCross.getCrossId());
			}
		}
	}

	@Override
	public int compareTo(Road o) {
		if(this.roadId<o.getRoadId())
			return -1;
		else if(this.roadId>o.getRoadId())
			return 1;
		else
			return 0;
	}
}

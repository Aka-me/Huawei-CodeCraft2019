package com.huawei;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class FindShortestPath {
    FindShortestPath(){

    }
//
//    public static HashMap<Cross,LinkedList<Road>> getShortestPathWithLimit(HashMap<Integer,Road> roads,HashMap<Integer,Cross> crosses,Cross from,Road notUseRoad) {
//        notUseRoad.setLength(notUseRoad.getLength()*100);
//        HashMap<Cross,LinkedList<Road>> shortestPath=new LinkedHashMap<>(getShortestPath(roads,crosses,from));
//        notUseRoad.setLength(notUseRoad.getLength()/100);
//        return shortestPath;
//    }

    public static HashMap<Integer,LinkedList<Road>> getShortestPath(HashMap<Integer,Road> roads,HashMap<Integer,Cross> crosses,Cross from){
        //获得从指定路口到到所有其他路口的最短路径
        HashMap<Integer,LinkedList<Road>> shortestPath=new HashMap<>();
        HashMap<Integer,Integer> shortestDist=new HashMap<>();
        HashMap<Integer,Boolean> visited=new HashMap<>();
        int numCross=crosses.size();

        //初始化
        for (Cross nextCross:
                crosses.values()) {
            shortestDist.put(nextCross.getCrossId(),Integer.MAX_VALUE);
            shortestPath.put(nextCross.getCrossId(),new LinkedList<>());
            visited.put(nextCross.getCrossId(),false);
        }

        //初始化起点
        Cross currentCross=from;
        //visited.put(currentCross,true);
        shortestDist.put(currentCross.getCrossId(),0);
        int count=1;
        while(count!=numCross){
            Cross nextCross=null;
            int min=Integer.MAX_VALUE;
            //找出从当前节点到所有为访问的节点的最短路口
            for (HashMap.Entry<Integer,Integer> entry:
                    shortestDist.entrySet()){
                if((!visited.get(entry.getKey()))&&entry.getValue()<min){
                    nextCross=crosses.get(entry.getKey());
                    min=entry.getValue();
                }
            }
            count++;
            visited.put(nextCross.getCrossId(),true);
            currentCross=nextCross;
//            if(nextCross==null)
//                continue;

            //根据连通图条件，不可能返回空值
            LinkedList<Road> nextValidRoads=currentCross.getAllValidRoads();
            for (Road road:
                    nextValidRoads) {
                nextCross=road.getNextCross(currentCross);
//                if(visited.get(nextCross))
//                    continue;
                int loadFactor;
//                if(road.getLoadByNextCross(nextCross)>=10)
//                    loadFactor=50;
//                else
//                    loadFactor=5;
                int dist=shortestDist.get(currentCross.getCrossId())+road.getLoadLength(currentCross)+
                        road.getLoadByNextCross(nextCross)*40/road.getChannel();
                if(dist<shortestDist.get(nextCross.getCrossId())) {
                    shortestDist.put(nextCross.getCrossId() , dist);
                    LinkedList<Road> newPath = new LinkedList<>(shortestPath.get(currentCross.getCrossId()));
                    newPath.add(road);
                    shortestPath.put(nextCross.getCrossId(), newPath);
                }
            }
        }
        return shortestPath;
    }

    public static void minLoadPath(HashMap<Integer,Road> roads,HashMap<Integer,Cross> crosses,HashMap<Integer,Car> cars,int m){
        //统计最短路径中各道路出现次数(注意单双行道)
        //为高负载道路增加权重,然后重新计算最短路径中各道路出现次数
        //目标为均衡道路负载
        int min=9999999;
        for (int a = 0; a < 150;a++) {
            HashMap<Integer,HashMap<Integer,LinkedList<Road>>> tempPath=new HashMap<>();
            for (Cross cross:
                    crosses.values()) {
                tempPath.put(cross.getCrossId(),FindShortestPath.getShortestPath(roads,crosses,cross));
            }

            for (Car currentCar:
                    cars.values()) {
                LinkedList<Road> currentPath=tempPath.get(currentCar.getFromId()).get(currentCar.getToId());
                for (int i = 0; i < currentPath.size(); i++) {
                    if(i==0)
                        currentPath.get(i).addPlanLoad(currentCar.getFrom());
                    else
                        currentPath.get(i).addPlanLoad(currentPath.get(i-1).getCrossByNextRoad(currentPath.get(i)));
                }
            }

            int maxLoad = 0;
            int maxidex = 0;
            int sumLoad = 0;
            int minLoad=9999;
            boolean dir=false;
            for ( Road currentRoad:
                    roads.values()) {
                if (maxLoad < currentRoad.planFromToLoad) {
                    maxidex = currentRoad.getRoadId();
                    maxLoad = currentRoad.planFromToLoad;
                    dir=true;
                }else if(minLoad>currentRoad.planFromToLoad){
                    //minidex = currentRoad.getRoadId();
                    minLoad = currentRoad.planFromToLoad;
                }

                if(currentRoad.getIsDuplex()==1)
                    if (maxLoad < currentRoad.planToFromLoad) {
                        maxidex = currentRoad.getRoadId();
                        maxLoad = currentRoad.planToFromLoad;
                        dir=false;
                    }else if(minLoad>currentRoad.planToFromLoad){
                        //minidex = currentRoad.getRoadId();
                        minLoad = currentRoad.planToFromLoad;
                    }
                //System.out.println(roads[i].getCount());
            }//找出sum和max

            double averLoad = (double) sumLoad / roads.size();     //平均负载
            min=maxLoad>min?min:maxLoad;
            System.out.println(minLoad+"    "+maxLoad + "    " + sumLoad + "     " + averLoad);

            if(min==m)
                return;

            for (Road currentRoad:
                    roads.values()) {
                currentRoad.planFromToLoad=0;
                currentRoad.planToFromLoad=0;
            }


            roads.get(maxidex).setLoadFator(1.1,dir);
        }
        System.out.println("first end");
        for (Road currentRoad :
                roads.values()) {
            currentRoad.fromToLength=currentRoad.getLength();
            currentRoad.toFromLength=currentRoad.getLength();
        }

        FindShortestPath.minLoadPath(roads,crosses,cars,min);
    }

}

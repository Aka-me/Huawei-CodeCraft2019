package com.huawei;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args)
    {

        if (args.length != 5) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

        logger.info("Start...");

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " presetAnswerPath = " + presetAnswerPath + " and answerPath = " + answerPath);

        // TODO:read input files
        logger.info("start read input files");
        HashMap<Integer,Road> roads=writeRoad(readInputFiles(roadPath));
        HashMap<Integer,Cross> crosses=writeCross(readInputFiles(crossPath));
        HashMap<Integer,Car> cars=writeCar(readInputFiles(carPath));

        for (Road road
                :roads.values()) {
            road.updateFromAndTo(crosses);
        }
        for (Cross cross
                :crosses.values()) {
            cross.updateEachRoad(roads);
        }
        for (Car car:
                cars.values()) {
            car.updateFromAndTo(crosses);
        }

        //setPresetCar(readInputFiles(presetAnswerPath),cars,roads);


        //****************************************************************************************
        //测试论坛小数据集
        readAnswers(readInputFiles(answerPath),cars,roads);
        SchedulSim ss=new SchedulSim(roads,crosses,cars);
        ss.startSchedule();

        //****************************************************************************************

        //constructTree(getMostFromCross(cars,crosses),roads,crosses,cars);


//        // TODO: calc
//        //FindShortestPath.getShortestPath(roads,crosses,crosses.get(6));
//        // 函数可以中出从选定路口到其他所有路口的路径
//        FindShortestPath.minLoadPath(roads,crosses,cars,99999);
//
//        HashMap<Integer,HashMap<Integer,LinkedList<Road>>> shortestPath=new HashMap<>();
//        for (Cross cross:
//                crosses.values()) {
//            shortestPath.put(cross.getCrossId(),FindShortestPath.getShortestPath(roads,crosses,cross));
//        }
//        //将上一步计算出来的最短路径作为车辆的初始计划路径添加进车辆中。
//        for (Car car:
//                cars.values()) {
//            if (car.getPreset()==0)
//                car.setPlanRoads(shortestPath.get(car.getFrom().getCrossId()).get(car.getTo().getCrossId()));
//        }
////        for (Cross cross:
////             crosses.values()) {
////            cross.setPlanRoads(shortestPath.get(cross));
////        }
//
//
//
//
//
//        SchedulSim ss=new SchedulSim(roads,crosses,cars);
//        if( !ss.startSchedule())
//            return;
//
//
//        // TODO: write answer.txt
//        logger.info("Start write output file");
//        String anwsers;
//        try {
//            FileWriter fw=new FileWriter(answerPath,false);
//            BufferedWriter bf = new BufferedWriter(fw);
//            // 按行写入字符串
//            for (Car car:
//                    cars.values()) {
//                if(car.getPreset()==1)
//                    continue;
//                anwsers="("+car.getCarId()+","+car.getPlanTime()+",";
//                List<Road> path=car.getPickedRoads();
//                for (int j = 0; j < path.size(); j++) {
//                    if(j==path.size()-1){
//                        anwsers+=path.get(j).getRoadId()+")\n";
//                    }else
//                        anwsers+=path.get(j).getRoadId()+",";
//                }
////                System.out.println(anwsers[i]);
//                bf.append(anwsers);
//            }
//            bf.close();
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        int allCarSize=cars.size();
        int priCarSize=0;
        int allCarMaxSpeed=0;
        int allCarMinSpeed=Integer.MAX_VALUE;
        int priCarMaxSpeed=0;
        int priCarMinSpeed=Integer.MAX_VALUE;
        int allCarLatestTime=0;
        int allCarEarlyestTime=Integer.MAX_VALUE;
        int priCarLatestTime=0;
        int priCarEarlyestTime=Integer.MAX_VALUE;
        HashSet<Integer> allCarStart=new HashSet<>();
        HashSet<Integer> allCarEnd=new HashSet<>();
        HashSet<Integer> priCarStart=new HashSet<>();
        HashSet<Integer> priCarEnd=new HashSet<>();
        int priCarReachedTIme=SchedulSim.priCarLastestReachTime;

        for (Car currentCar:
                cars.values()) {
            if(currentCar.getSpeed()>allCarMaxSpeed)
                allCarMaxSpeed=currentCar.getSpeed();
            if(currentCar.getSpeed()<allCarMinSpeed)
                allCarMinSpeed=currentCar.getSpeed();
            if(currentCar.getPlanTime()>allCarLatestTime)
                allCarLatestTime=currentCar.getPlanTime();
            if(currentCar.getPlanTime()<allCarEarlyestTime)
                allCarEarlyestTime=currentCar.getPlanTime();
            allCarStart.add(currentCar.getFromId());
            allCarEnd.add(currentCar.getToId());


            if(currentCar.getPriority()) {
                priCarSize++;
                if(currentCar.getSpeed()>priCarMaxSpeed)
                    priCarMaxSpeed=currentCar.getSpeed();
                if(currentCar.getSpeed()<priCarMinSpeed)
                    priCarMinSpeed=currentCar.getSpeed();
                if(currentCar.getPlanTime()>priCarLatestTime)
                    priCarLatestTime=currentCar.getPlanTime();
                if(currentCar.getPlanTime()<priCarEarlyestTime)
                    priCarEarlyestTime=currentCar.getPlanTime();
                priCarStart.add(currentCar.getFromId());
                priCarEnd.add(currentCar.getToId());
            }
        }

        DecimalFormat de=new java.text.DecimalFormat("#.00000");
        double res1=Double.valueOf(de.format((double)allCarSize/priCarSize));
        double res2a=Double.valueOf(de.format((double)allCarMaxSpeed/allCarMinSpeed));
        double res2b=Double.valueOf(de.format((double)priCarMaxSpeed/priCarMinSpeed));
        double res2=Double.valueOf(de.format((double)res2a/res2b));
        double res3a=Double.valueOf(de.format((double)allCarLatestTime/allCarEarlyestTime));
        double res3b=Double.valueOf(de.format((double)priCarLatestTime/priCarEarlyestTime));
        double res3=Double.valueOf(de.format((double)res3a/res3b));
        double res4=Double.valueOf(de.format((double)allCarStart.size()/priCarStart.size()));
        double res5=Double.valueOf(de.format((double)allCarEnd.size()/priCarEnd.size()));

        double a=(res1*0.05+res2*0.2375+res3*0.2375+res4*0.2375+res5*0.2375);
        double res=a*(priCarReachedTIme-priCarEarlyestTime)+SchedulSim.timeCount;

        System.out.println("a="+a);
        System.out.println("优先车辆到达时间"+priCarReachedTIme);
        System.out.println("优先车辆最晚出发时间"+priCarLatestTime);
        System.out.println("优先车辆出发时间"+priCarEarlyestTime);
        System.out.println("优先车间隔时间"+(priCarReachedTIme-priCarEarlyestTime));
        System.out.println("所有车辆出发分布"+allCarStart.size());
        System.out.println("优先车辆出发地分布"+priCarStart.size());
        System.out.println("所有车辆终点分布"+allCarEnd.size());
        System.out.println("优先车辆终点地分布"+priCarEnd.size());
        System.out.println("最终结果"+res);

        logger.info("End...");
    }

    public static ArrayList<String> readInputFiles(String path) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }


    private static HashMap<Integer,Road> writeRoad(ArrayList<String> arrayList){
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        HashMap<Integer,Road> roads=new HashMap<>();
        for (int i = 1; i < length; i++) {
            String str = arrayList.get(i);
            str = str.substring(1, str.length()-1);
            //截取获得字符串数组
            String[] strArray = str.split(", ");

            roads.put(Integer.parseInt(strArray[0]),
                    new Road(Integer.parseInt(strArray[0]),
                            Integer.parseInt(strArray[1]),
                            Integer.parseInt(strArray[2]),
                            Integer.parseInt(strArray[3]),
                            Integer.parseInt(strArray[4]),//from
                            Integer.parseInt(strArray[5]),//to
                            Integer.parseInt(strArray[6]))
            );
        }
        return roads;
    }

    private static HashMap<Integer,Cross> writeCross(ArrayList<String> arrayList){
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        HashMap<Integer,Cross> crosses=new HashMap<>();
        for (int i = 1; i < length; i++) {
            String str = arrayList.get(i);
            str = str.substring(1, str.length()-1);
            //logger.info(str);
            //截取获得字符串数组
            String[] strArray = str.split(", ");
            crosses.put(Integer.parseInt(strArray[0]),
                    new Cross(Integer.parseInt(strArray[0]),
                            Integer.parseInt(strArray[1]),
                            Integer.parseInt(strArray[2]),
                            Integer.parseInt(strArray[3]),
                            Integer.parseInt(strArray[4])));
        }
        return crosses;
    }

    private static HashMap<Integer,Car> writeCar(ArrayList<String> arrayList){
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        HashMap<Integer,Car> cars=new HashMap<>();
        for (int i = 1; i < length; i++) {
            String str = arrayList.get(i);
            str = str.substring(1, str.length()-1);
            //截取获得字符串数组
            String[] strArray = str.split(", ");
            cars.put(Integer.parseInt(strArray[0]),
                    new Car(Integer.parseInt(strArray[0]),
                            Integer.parseInt(strArray[1]),
                            Integer.parseInt(strArray[2]),
                            Integer.parseInt(strArray[3]),
                            Integer.parseInt(strArray[4]),
                            Integer.parseInt(strArray[5]),
                            Integer.parseInt(strArray[6])));
        }
        return cars;
    }

    private static void setPresetCar(ArrayList<String> arrayList,HashMap<Integer,Car> cars,HashMap<Integer,Road> roads){
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        for (int i = 1; i < length; i++) {
            String str = arrayList.get(i);
            str = str.substring(1, str.length() - 1);
            //截取获得字符串数组
            String[] strArray = str.split(",");
//            if(strArray[0].charAt(0)==' ')
//                strArray[0]=strArray[0].substring(1);
//            if(strArray[1].charAt(0)==' ')
//                strArray[1]=strArray[1].substring(1);
//            if(strArray[2].charAt(0)==' ')
//                strArray[2]=strArray[2].substring(1);
            Car currentCar = cars.get(Integer.parseInt(strArray[0]));
            currentCar.setPlanTime(Integer.parseInt(strArray[1]));
            LinkedList<Road> path = new LinkedList<>();
            for (int j = 2; j < strArray.length; j++) {
                path.add(roads.get(Integer.valueOf(strArray[j])));
            }
            currentCar.setPlanRoads(path);
        }
    }

    private static void readAnswers(ArrayList<String> arrayList,HashMap<Integer,Car> cars,HashMap<Integer,Road> roads){
        for (String str:
                arrayList) {
            str=str.substring(1,str.length()-1);
            String[] ans=str.split(",");
            int carId=Integer.parseInt(ans[0]);
            Car currentCar=cars.get(carId);
            LinkedList<Road> planRoad=new LinkedList<>();
            currentCar.setPlanTime(Integer.parseInt(ans[1]));
            for (int i = 2; i < ans.length; i++) {
                planRoad.add(roads.get(Integer.parseInt(ans[i])));
            }
            currentCar.setPlanRoads(planRoad);
        }
    }


    public static Cross getMostFromCross(HashMap<Integer,Car> cars,HashMap<Integer,Cross> crosses){
        for (Car currentCar:
                cars.values()) {
            crosses.get(currentCar.getFromId()).usedCount++;
        }
        int used=0;
        Cross mostFromCross=null;
        for (Cross currentCross:
                crosses.values()) {
            if(currentCross.usedCount>used){
                used=currentCross.usedCount;
                mostFromCross=currentCross;
            }
        }
        return mostFromCross;
    }

//    public static void constructTree(Cross root,HashMap<Integer,Road> roads,HashMap<Integer,Cross> crosses,HashMap<Integer,Car> cars){
//        HashMap<Cross,LinkedList<Road>> sPath=FindShortestPath.getShortestPath(roads,crosses,root);
//
//        ArrayList<Road> usedRoads=new ArrayList<>();
//        HashMap<Integer,Road> hashUsedRoads=new HashMap<>();
//        for (LinkedList<Road> usedPath:
//             sPath.values()) {
//            for (Road currentRoad:
//                 usedPath) {
//                if (!usedRoads.contains(currentRoad)){
//                    usedRoads.add(currentRoad);
//                    hashUsedRoads.put(currentRoad.getRoadId(),currentRoad);
//                }
//            }
//        }
//
//        //此时useRoads里包含了从root路口到所有其他路口的路径
//        System.out.println(usedRoads.size());
//        System.out.println(roads.size());
////        for (Road currentRoad:
////             usedRoads) {
////            System.out.println(currentRoad.getRoadId());
////        }
//
//        for (Cross currentCross:
//             crosses.values()) {
//            LinkedList<Road> allValidRoads=currentCross.getAllValidRoads();
//            for (int i = 0; i < allValidRoads.size(); i++) {
//                if (!usedRoads.contains(allValidRoads.get(i)))
//                    allValidRoads.set(i,null);
//            }
//        }
//
//
//        int crossSize=crosses.size();
//        int count=0;
//        HashMap<Cross,HashMap<Cross,LinkedList<Road>>> shorestPaths=new HashMap<>();
//        for (Cross currentCross:
//             crosses.values()) {
//            //System.out.print("从路口"+currentCross.getCrossId()+"到其他路口数");
//            shorestPaths.put(currentCross,FindShortestPath.getShortestPath(hashUsedRoads,crosses,currentCross));
//        }
//
//
//        for (Cross currentCross:
//             crosses.values()) {
//            HashMap<Cross,LinkedList<Road>> currentPaths= shorestPaths.get(currentCross);
//            for (LinkedList<Road> path:
//                 currentPaths.values()) {
//                for (int i = 0; i < path.size(); i++) {
//                    if(usedRoads.contains(path.get(i))) {
//                        count++;
//                        break;
//                    }
//                }
//            }
//        }
//        System.out.println(count);
//
//        for (Car currentCar:
//             cars.values()) {
//            if (currentCar.getPreset()==0){
//                currentCar.setPlanRoads(shorestPaths.get(currentCar.getFrom()).get(currentCar.getTo()));
//            }
//        }
//
//    }
}
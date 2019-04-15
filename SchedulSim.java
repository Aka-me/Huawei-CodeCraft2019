package com.huawei;

import java.util.*;

public class SchedulSim {
    private HashMap<Integer,Road> roads;
    private HashMap<Integer,Cross> crosses;
    private HashMap<Integer,Car> cars;
    private LinkedList<Cross> orderedCrosses;
    private HashMap<Integer,HashMap<Integer,LinkedList<Road>>> shortestPath;

    private List<Car> waitForStartCars;
    private List<Car> priWaitForStartCars=new ArrayList<>();
    private List<Car> norWaitForStartCars=new ArrayList<>();
    private List<Car> preWaitForStartCars=new ArrayList<>();
    private List<Car> notReachedCars;
    private List<Car> waitingCars;
    private HashSet<Road> preCarCross;

    public static int timeCount;
    private final int LIMIT=1000;
    private int LIMIT_NUM_PER_TIME=1000;
    private int limitCount=0;
    private int preCount=0;
    private boolean flagPre=false;

    public static int priCarLastestReachTime=0;

    public SchedulSim(HashMap<Integer,Road> roads,HashMap<Integer,Cross> crosses,HashMap<Integer,Car> cars){
        this.roads=roads;
        this.crosses=crosses;
        this.cars=cars;
        orderedCrosses=new LinkedList<>(crosses.values());
        Collections.sort(orderedCrosses);
        waitingCars=new LinkedList<>();
    }


    public boolean startSchedule(){
        waitForStartCars=new ArrayList<>();
        notReachedCars=new LinkedList<>();
        waitForStartCars.addAll(cars.values());
        Collections.sort(waitForStartCars);

        //把等待发车的队列分成三个队列
        for (Car currentCar:
                waitForStartCars) {
            if(currentCar.getPreset()==0&&currentCar.getPriority())
                priWaitForStartCars.add(currentCar);
            else if(currentCar.getPreset()==0)
                norWaitForStartCars.add(currentCar);
            else
                preWaitForStartCars.add(currentCar);
        }

        timeCount=0;
        while(true){
            timeCount++;
            limitCount=0;
            flagPre=false;
            System.out.print("时间片："+timeCount);
            System.out.print("    路上车辆："+notReachedCars.size());
            System.out.println("    等待发车: "+waitForStartCars.size());

            //对preset车辆进行排序，将计划时间比当前时间小的车发到开头的按顺序排列的位置
            preCarCross=new HashSet<>();
            for (int i = preCount; i < preWaitForStartCars.size(); i++) {
                Car currentCar=preWaitForStartCars.get(i);
                if(currentCar.getPlanTime()==timeCount){
                    preWaitForStartCars.add(preCount,currentCar);
                    preWaitForStartCars.remove(i+1);
                    preCount++;
                    preCarCross.add(currentCar.getPlanRoads().getFirst());
                }
            }
            if(preWaitForStartCars.size()!=0&&preWaitForStartCars.get(0).getPlanTime()<=timeCount)
                flagPre=true;


            //在每个时间片开始时将所有还没有到达终点的车设置为等待状态.
            waitingCars.addAll(notReachedCars);
            LinkedList<Cross> waitingCrosses = new LinkedList<>(orderedCrosses);

            //每个时间片开始时根据当前路径负载改变车辆的planRoads
            shortestPath=new HashMap<>();
            for (Cross cross:
                    crosses.values()) {
                shortestPath.put(cross.getCrossId(),FindShortestPath.getShortestPath(roads,crosses,cross));
            }
            for (Car currentCar:
                    waitingCars) {
                if(currentCar.getPreset()==0&&currentCar.getPlanRoads().size()>=2&&currentCar.getPlanRoads().size()!=0){
                    //该车在当前道路还未到达终点,改变该车路径
                    Road nextRoad=currentCar.getPlanRoads().getFirst();
                    Cross currentCross=currentCar.getPickedRoads().getLast().getCrossByNextRoad(nextRoad);

                    //如果这辆车的最短路径要走回头路，就重新给它选择一条路。
                    if(!currentCar.flagChanged) {
                        LinkedList<Road> newPath = shortestPath.get(currentCross.getCrossId()).get(currentCar.getTo().getCrossId());

                        if (newPath.getFirst() != currentCar.getPickedRoads().getLast()) {
                            currentCar.setPlanRoads(newPath);
                        } else {
                            //currentCar.setPlanRoads(FindShortestPath.getShortestPathWithLimit(roads,crosses,currentCross,currentCar.getPickedRoads().getLast()).get(currentCar.getTo()));
                        }
                    }
                }
            }


            if(timeCount==1){
                //第一个时间片只有发车操作
                preSetCarStart(true,null,null);
                preSetCarStart(false,null,null);
                carStart(timeCount,true);
                carStart(timeCount,false);
                continue;
            }

            //第一步对所有道路上的车辆进行独立调度
            updateAllRoads();
            //第二步发出所有能发的优先车辆
            preSetCarStart(true,null,null);
            carStart(timeCount,true);

            while(waitingCars.size()!=0){
                //一次调度直到所有车辆都进入停止状态.
                int last=waitingCars.size();//用来判断死锁状态

                for (Cross currentCross:
                        waitingCrosses) {
                    //按顺序遍历路口
                    scheduleEachCross(currentCross);
                }

                //移除所有不需要调度的cross,节约时间
                for (int i = 0; i < waitingCrosses.size(); i++) {
                    LinkedList<Car[][]> scheduleRoads= waitingCrosses.get(i).getAllToChannels();
                    int m=0;
                    for (;m<scheduleRoads.size();m++) {
                        Car[][] roadInfo=scheduleRoads.get(m);
                        int j = 0;
                        for (; j < roadInfo.length; j++) {
                            int k = 0;
                            for (; k < roadInfo[j].length; k++) {
                                if(roadInfo[j][k]!=null){
                                    if(waitingCars.contains(roadInfo[j][k]))
                                        break;
                                }
                            }
                            if(k!=roadInfo[j].length)
                                break;
                        }
                        if(j!=roadInfo.length)
                            break;
                    }
                    if(m==scheduleRoads.size()) {
                        waitingCrosses.remove(i);
                        i--;
                    }
                }

                if(waitingCars.size()==last){
                    //死锁
                    System.out.println("调度出现死锁,死锁车辆: 数"+waitingCars.size());
                    System.out.println("道路上车辆："+notReachedCars.size());
                    return false;
                }
            }

            //路口调度完成，开始发车
            preSetCarStart(true,null,null);
            preSetCarStart(false,null,null);
            //carStart(timeCount,true);
            carStart(timeCount,false);

            //判断是否完成调度
            if (notReachedCars.size()==0&&waitForStartCars.size()==0){
                int n=0;
                int l=0;
                for(Car car:cars.values()){
                    l=0;
                    if(n%5000==0){
                        LinkedList<Road> pickedRoads = car.getPickedRoads();
                        for(int i=0;i<pickedRoads.size();i++){
                            l+=pickedRoads.get(i).getLength();
                        }
                        System.out.println("车辆："+car.getCarId()+"   行驶距离： "+l);
                    }
                    n++;
                }
                System.out.print("----------END---------");
                System.out.println("时间片: "+timeCount);
                break;
            }
        }
        return true;
    }

    public boolean scheduleEachRoads(Car[][] roadInfo,Cross currentCross){
        //这里并不需要获得当前道路的对象，因为对于roadInfo数组中的每一辆车，pickedRoad中都可以知道当前道路
        boolean flagMove=false;

        int[] position=getFirstRunningCar(roadInfo);
        if(position[1]==-1)//该道路没有可调度车辆
            return false;

        Car currentCar=roadInfo[position[0]][position[1]];//该车是等待中车辆已经检验过了
        Road currentRoad=currentCar.getPickedRoads().getLast();
        if(currentCar.getPlanRoads().size()==0){
            //用直行优先级判定该车是否能进入终点车库
            return reachEndCross(currentCar,currentRoad,roadInfo,position,currentCross);
        }

        Road nextRoad=currentCar.getPlanRoads().getFirst();
        Car[] currentChannel=currentRoad.getIntoFromCross(currentCross)[position[0]];
        int nextLength=0;
        try {
            nextLength= Math.min(nextRoad.getSpeed(), currentCar.getSpeed()) - (roadInfo[0].length - 1 - position[1]);
        }catch (Exception e){
            System.out.println("asdas");
        }
        int direction=currentCross.getDirection(currentRoad,nextRoad);

        if(direction==0){
            System.out.println("该路口不连接到道路 ");
        }else if(direction==1){
            flagMove=turnStraight(currentCar,roadInfo[position[0]],position[1],currentRoad,nextRoad,nextLength,currentCross);
        }else if(direction==2){
            flagMove=turnLeft(currentCar,roadInfo[position[0]],position[1],currentRoad,nextRoad,nextLength,currentCross);
        }else if(direction==3){
            flagMove=turnRight(currentCar,roadInfo[position[0]],position[1],currentRoad,nextRoad,nextLength,currentCross);
        }
        if(flagMove){
            return true;
        }
        //由于冲突没有过路口.
        currentCar.flagChanged=true;
        return false;
    }

    private boolean reachEndCross(Car currentCar,Road currentRoad,Car[][] roadInfo,int[] position,Cross currentCross){
        //if不能进入车库的话返回false,否则进入车库并且返回true.
        if(!currentCar.getPriority()){
            Car leftRoadFirstCar=getFirstCarObj(currentCross.getLeftRoad(currentRoad),currentCross);
            Car rightRoadFirstCar=getFirstCarObj(currentCross.getRightRoad(currentRoad),currentCross);
            if(leftRoadFirstCar!=null){
                if(leftRoadFirstCar.getPriority()){
                    int dir;
                    if(leftRoadFirstCar.getPlanRoads().size()==0)
                        dir=1;
                    else
                        dir=currentCross.getDirection(currentCross.getLeftRoad(currentRoad),leftRoadFirstCar.getPlanRoads().getFirst());
                    if(dir==2) return false;
                    else ;
                }
            }
            if(rightRoadFirstCar!=null){
                if(rightRoadFirstCar.getPriority()){
                    int dir;
                    if(rightRoadFirstCar.getPlanRoads().size()==0)
                        dir=1;
                    else
                        dir=currentCross.getDirection(currentCross.getRightRoad(currentRoad),rightRoadFirstCar.getPlanRoads().getFirst());
                    if(dir==3) return false;
                    else ;
                }
            }
        }
        currentRoad.minLoad(roadInfo);
        waitingCars.remove(currentCar);
        notReachedCars.remove(currentCar);
        roadInfo[position[0]][position[1]]=null;
        updateEachChannels(roadInfo[position[0]],currentRoad.getSpeed());
        preSetCarStart(true,currentRoad,currentCross);
        //carStart(timeCount,true);
        if(currentCar.getPriority())
            priCarLastestReachTime=timeCount;
        return true;
    }

    public boolean turnStraight(Car currentCar,Car[] oldChannel,int oldPosition,Road currentRoad,Road nextRoad,int nextLength,Cross currentCross){
        //分该车辆是否是优先车辆进行讨论
        if(currentCar.getPriority()){
            //直行并且优先   直接行驶
            return moveToNextRoad(currentCar,oldChannel,oldPosition,currentRoad.getSpeed(),nextRoad,nextLength,currentCross);
        }else{
            //直行但不优先，判断其他道路上是否有优先车辆.
            Car leftRoadFirstCar=getFirstCarObj(currentCross.getLeftRoad(currentRoad),currentCross);
            Car rightRoadFirstCar=getFirstCarObj(currentCross.getRightRoad(currentRoad),currentCross);
            if(leftRoadFirstCar!=null) {
                if (leftRoadFirstCar.getPriority()){
                    //应该判断该优先级车辆是否会与他冲突
                    int dir;
                    if(leftRoadFirstCar.getPlanRoads().size()==0)
                        dir=1;
                    else
                        dir=currentCross.getDirection(currentCross.getLeftRoad(currentRoad),leftRoadFirstCar.getPlanRoads().getFirst());
                    if(dir==2)
                        return false;
                }
            }
            if(rightRoadFirstCar!=null){
                if(rightRoadFirstCar.getPriority()){
                    int dir;
                    if(rightRoadFirstCar.getPlanRoads().size()==0)
                        dir=1;
                    else
                        dir=currentCross.getDirection(currentCross.getRightRoad(currentRoad),rightRoadFirstCar.getPlanRoads().getFirst());
                    if(dir==3)
                        return false;
                }
            }
            return moveToNextRoad(currentCar,oldChannel,oldPosition,currentRoad.getSpeed(),nextRoad,nextLength,currentCross);
        }
    }

    public boolean turnLeft(Car currentCar,Car[] oldChannel,int oldPosition,Road currentRoad,Road nextRoad,int nextLength,Cross currentCross){
        //分该车辆是否是优先车辆进行讨论
        Car oppoRoadFirstCar=getFirstCarObj(currentCross.getOppoRoad(currentRoad),currentCross);
        Car rightRoadFirstCar=getFirstCarObj(currentCross.getRightRoad(currentRoad),currentCross);
        if(rightRoadFirstCar!=null){
            int dir;
            if(rightRoadFirstCar.getPlanRoads().size()==0)
                dir=1;
            else
                dir=currentCross.getDirection(currentCross.getRightRoad(currentRoad),rightRoadFirstCar.getPlanRoads().getFirst());
            if(dir==1) {
                if (rightRoadFirstCar.getPriority())
                    return false;
                else if(!currentCar.getPriority())
                    return false;
            }
        }
        if(oppoRoadFirstCar!=null) {
            if (oppoRoadFirstCar.getPriority()){
                //应该判断该优先级车辆是否会与他冲突
                int dir;
                if(oppoRoadFirstCar.getPlanRoads().size()==0)
                    dir=1;
                else
                    dir=currentCross.getDirection(currentCross.getOppoRoad(currentRoad),oppoRoadFirstCar.getPlanRoads().getFirst());
                if(dir==3) {
                    if (!currentCar.getPriority())
                        return false;
                }
            }
        }
        return moveToNextRoad(currentCar,oldChannel,oldPosition,currentRoad.getSpeed(),nextRoad,nextLength,currentCross);
    }

    public boolean turnRight(Car currentCar,Car[] oldChannel,int oldPosition,Road currentRoad,Road nextRoad,int nextLength,Cross currentCross){
        Car oppoRoadFirstCar=getFirstCarObj(currentCross.getOppoRoad(currentRoad),currentCross);
        Car leftRoadFirstCar=getFirstCarObj(currentCross.getLeftRoad(currentRoad),currentCross);
        if(leftRoadFirstCar!=null){
            int dir;
            if(leftRoadFirstCar.getPlanRoads().size()==0)
                dir=1;
            else
                dir=currentCross.getDirection(currentCross.getLeftRoad(currentRoad),leftRoadFirstCar.getPlanRoads().getFirst());
            if(dir==1){
                if(!currentCar.getPriority())
                    return false;
                else if(leftRoadFirstCar.getPriority())
                    return false;
            }
        }
        if(oppoRoadFirstCar!=null) {
            int dir;
            if(oppoRoadFirstCar.getPlanRoads().size()==0)
                dir=1;
            else
                dir=currentCross.getDirection(currentCross.getOppoRoad(currentRoad),oppoRoadFirstCar.getPlanRoads().getFirst());
            if(dir==2) {
                if(!currentCar.getPriority())
                    return false;
                else if(oppoRoadFirstCar.getPriority())
                    return false;
            }
        }
        return moveToNextRoad(currentCar,oldChannel,oldPosition,currentRoad.getSpeed(),nextRoad,nextLength,currentCross);
    }

    public boolean moveToNextRoad(Car currentCar,Car[] oldChannel,int oldPosition,int oldRoadSpeed,Road nextRoad,int nextLength,Cross currentCross){
        if(nextLength<=0){
            //该车由于下个道路的限速无法通过路口
            updateCarPosition(currentCar,oldChannel,oldPosition,oldRoadSpeed,oldChannel,oldChannel.length-1);
            preSetCarStart(true,currentCar.getPickedRoads().getLast(),currentCross);
            //carStart(timeCount,true);
            currentCar.flagChanged=true;
            return true;
        }

        Car[][] nextRoadInfo=nextRoad.getOutFromCross(currentCross);
        for (int i = 0; i < nextRoad.getChannel(); i++) {
            int j = 0;
            for (; j < nextLength; j++) {
                if(nextRoadInfo[i][j]!=null)
                    break;
            }
            if(j!=nextLength){
                if(j==0){
                    if(waitingCars.contains(nextRoadInfo[i][j])) {
                        currentCar.flagChanged=true;
                        return false;//当前车道最后一个位置有车辆在等待，无法移动返回false等待下一循环
                    }
                }else{
                    //在j的位置有车，且j>0
                    if(waitingCars.contains(nextRoadInfo[i][j])) {
                        currentCar.flagChanged=true;
                        return false;
                    }
                    updateCarPosition(currentCar,oldChannel,oldPosition,oldRoadSpeed,nextRoadInfo[i],j-1);
                    preSetCarStart(true,currentCar.getPickedRoads().getLast(),currentCross);
                    currentCar.updatePickedRoads();
                    //carStart(timeCount,true);
                    currentCar.flagChanged=false;
                    return true;
                }
            }else{
                //没有break出来，一直到nextLength的位置都没有遇到车
                //直接移动到nextLength-1然后return
                updateCarPosition(currentCar,oldChannel,oldPosition,oldRoadSpeed,nextRoadInfo[i],j-1);
                preSetCarStart(true,currentCar.getPickedRoads().getLast(),currentCross);
                currentCar.updatePickedRoads();
                //carStart(timeCount,true);
                currentCar.flagChanged=false;
                return true;
            }
            if(i==nextRoad.getChannel()-1){
                //一直到最后一车道全部都满了
                //当前车辆无法移动
                updateCarPosition(currentCar,oldChannel,oldPosition,oldRoadSpeed,oldChannel,oldChannel.length-1);
                preSetCarStart(true,currentCar.getPickedRoads().getLast(),currentCross);
                //carStart(timeCount,true);
                currentCar.flagChanged=true;
                return true;
            }
        }
        return false;
    }

    public void updateCarPosition(Car currentCar,Car[] oldChannel,int oldPosition,int oldRoadSpeed,Car[] newChannel,int newPosition){
        if(oldChannel!=newChannel){
            //车辆从当前道路移动到下一个道路
            Road currentRoad=currentCar.getPickedRoads().getLast();
            Road nextRoad=currentCar.getPlanRoads().getFirst();
            Cross currentCross=currentRoad.getCrossByNextRoad(nextRoad);

            nextRoad.addLoad(nextRoad.getOutFromCross(currentCross));
            currentRoad.minLoad(currentRoad.getIntoFromCross(currentCross));
        }
        oldChannel[oldPosition]=null;
        if( newChannel[newPosition]!=null)
            System.out.println("有车辆被覆盖");
        newChannel[newPosition]=currentCar;
        waitingCars.remove(currentCar);
        updateEachChannels(oldChannel,oldRoadSpeed);
    }

    public Car getFirstCarObj(Road currentroad,Cross currentCross){
        if(currentroad==null)
            return null;
        Car[][] roadInfo=currentroad.getIntoFromCross(currentCross);
        if(roadInfo==null)
            return null;
        int[] pos=getFirstRunningCar(roadInfo);
        if(pos[1]==-1)
            return null;
        else
            return roadInfo[pos[0]][pos[1]];
    }

    public int[] getFirstRunningCar(Car[][] roadInfo){
        int[] res=new int[2];
        res[0]=roadInfo.length;
        res[1]=-1;

        int lastCarPripority=0;

        for (int i = 0; i < roadInfo.length; i++) {
            for (int j = roadInfo[0].length-1; j >= 0; j--) {
                if(roadInfo[i][j]!=null){
                    //当前位置有车,判断是否优先度比已选中的高
                    Car currentCar=roadInfo[i][j];
                    if(!waitingCars.contains(currentCar))
                        break;
                    if(currentCar.getPriority()){
                        if(lastCarPripority==0){
                            lastCarPripority=1;
                            res[0]=i;
                            res[1]=j;
                            break;
                        }else{
                            if(j>res[1]){
                                res[0]=i;
                                res[1]=j;
                                break;
                            }
                        }
                    }else{
                        if(lastCarPripority==0){
                            if(j>res[1]){
                                res[0]=i;
                                res[1]=j;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        return res;
    }

    public void scheduleEachCross(Cross currentCross){
        //对每个路口进行调度
        LinkedList<Car[][]> scheduleRoads=currentCross.getAllToChannels();
            for (Car[][] roadInfo:
                    scheduleRoads) {
                //调度该道路
                for (; ; ) {
                    if (!scheduleEachRoads(roadInfo, currentCross)) {
                        break;
                    }
                }
            }
    }

    public void updateEachChannels(Car[] channel,int roadSpeed){
        int lastCarPosition=channel.length;
        for (int i = channel.length-1; i >=0; i--) {
            if(channel[i]!=null){
                //当前位置有车，判断前一辆车位置，使用lastCarPosition
                Car currentCar=channel[i];
                if(!waitingCars.contains(currentCar)) {
                    //当前车辆处于停止状态，
                    lastCarPosition = i;
                    continue;
                }
                int maxSpeed=Math.min(roadSpeed,currentCar.getSpeed());
                if(i+maxSpeed>=lastCarPosition){
                    //当前车辆行驶距离达到lastcarposition的位置
                    if(lastCarPosition==channel.length) {
                        //当前车辆是该车道第一辆车
                        lastCarPosition = i;
                    }
                    else{
                        if(waitingCars.contains(channel[lastCarPosition]))
                            //前车处于等待状态
                            //return;
                            lastCarPosition=i;
                        else{
                            //前车停止，更新到前车后一位
                            channel[i]=null;
                            channel[lastCarPosition - 1] = currentCar;
                            waitingCars.remove(currentCar);
                            lastCarPosition-=1;
                        }
                    }
                }else {
                    //只能移动到前一辆车的后面
                    lastCarPosition =i+maxSpeed;
                    channel[i] = null;
                    channel[lastCarPosition] = currentCar;
                    waitingCars.remove(currentCar);
                }
            }
        }
    }

    public void updateAllRoads(){
        for (Road currentRoad:
                roads.values()) {
            Car[][] fromTo=currentRoad.getFromTo();
            for (int i = 0; i < fromTo.length; i++)
                updateEachChannels(fromTo[i],currentRoad.getSpeed());
            if(currentRoad.getIsDuplex()==1) {
                Car[][] toFrom=currentRoad.getToFrom();
                for (int i = 0; i < toFrom.length; i++)
                    updateEachChannels(toFrom[i],currentRoad.getSpeed());
            }
        }
    }

    //静态负载，n为负载最高车辆数
    public boolean whetherStartStatic(Car currentCar,int n){
        LinkedList<Road> planRoads = currentCar.getPlanRoads();
        Cross currentCross = currentCar.getFrom();
        for(int i=0;i<planRoads.size();i++){
            if(i==5)//观察后面i条道路的负载情况
                break;
            Road currentRoad = planRoads.get(i);
            int nextRoadLoad = currentRoad.getLoadByCurrentCross(currentCross);//发车所上的路的负载
            if(nextRoadLoad>n*currentRoad.getLength()/30)
                return false;//过载，不能发车
            currentCross = currentRoad.getNextCross(currentCross);
        }
        return true;
    }

    //判断已选择的道路（planRoad所有的道路或者前16条）上的负载决定是否能够发车
    //动态负载n与比例参数有关
    public boolean whetherStart(Car currentCar,int n){
        LinkedList<Road> planRoads = currentCar.getPlanRoads();
        Cross currentCross = currentCar.getFrom();
        for(int i=0;i<planRoads.size();i++){
            if(i==3)//观察后面i条道路的负载情况
                break;
            Road currentRoad = planRoads.get(i);
            int nextRoadLoad = currentRoad.getLoadByCurrentCross(currentCar.getFrom());//发车所上的路的负载
            int parameter = n - i/2;//比例参数，随着道路往后，道路负载要求降低
            if(nextRoadLoad>currentRoad.getLength()/parameter)
                return false;//过载，不能发车
            if(nextRoadLoad<4)
                return true;//负载过低，直接发车
            currentCross = currentRoad.getNextCross(currentCross);
        }
        return true;
    }

    public void carStart(int timeCount,boolean priority){
        //非预置车辆的发车函数
        List<Car> waiting;
        if(priority)
            waiting=priWaitForStartCars;
        else
            waiting=norWaitForStartCars;
        int n;//预设负载参数
        int sp=0;//预设速度参数,固定时间段内只发速度大于等于sp的车辆
        //大于该路负载不要发车，负载为（车道数*车道长度）➗n（等同于负载最高为n段一辆车）

        if(timeCount<200)
            n=4;
        else if(timeCount<400)
            n=7;
        else if(timeCount<700)
            n=10;
        else if(timeCount<900)
            n=15;
        else if(timeCount<1000)
            n=30;
        else
            n=100;
        if(timeCount<130)
            sp=16;
        else if(timeCount<220)
            sp=14;
        else if(timeCount<250)
            sp=12;
        else if(timeCount<450)
            sp=10;
        else if(timeCount<600)
            sp=9;
        else if(timeCount<720)
            sp=6;
        else if(timeCount<800)
            sp=4;
        else
            sp=2;
        for (int c=0;c<waiting.size();c++){
            if(limitCount>=LIMIT_NUM_PER_TIME)
                return;
            Car currentCar=waiting.get(c);
            if(currentCar.getPlanTime()<=timeCount){
                //根据负载动态选择出发道路
                LinkedList<Road> newPath = shortestPath.get(currentCar.getFrom().getCrossId()).get(currentCar.getTo().getCrossId());
                currentCar.setPlanRoads(newPath);

                if(flagPre&&preCarCross.contains(currentCar.getPlanRoads().getFirst()))
                    continue;


                Road currentRoad=currentCar.getPlanRoads().getFirst();
                Car[][] roadInfo=currentRoad.getOutFromCross(currentCar.getFrom());
                //只对非预置车辆进行限制
                if(!currentCar.getPriority()) {//非优先车辆做速度限制
                    if((currentCar.getSpeed()<sp || !whetherStartStatic(currentCar,n))){
                        continue;
                    }
                }
                else{//优先车辆不做速度限制
                    if(!whetherStartStatic(currentCar,n+7)){
                        continue;
                    }
                }
                if(roadInfo==null){
                    System.out.println("nmdwsm");
                    return;
                }
                int currentSpeed=Math.min(currentCar.getSpeed(),currentRoad.getSpeed());
                for (int i = 0; i < currentRoad.getChannel(); i++) {
                    int j = 0;
                    for (; j < currentSpeed; j++)
                        if (roadInfo[i][j] != null)
                            break;
                    int targetPosition;
                    if(j==currentSpeed)
                        targetPosition=j-1;
                    else
                    if(j>0){
                        if(waitingCars.contains(roadInfo[i][j])) {
                            break;
                        }
                        else
                            targetPosition=j-1;
                    }
                    else {
                        if(waitingCars.contains(roadInfo[i][j])) {
                            break;
                        }
                        continue;
                    }

                    //把车发往targetposition位置
                    waitForStartCars.remove(currentCar);
                    waiting.remove(currentCar);
                    notReachedCars.add(currentCar);
                    currentCar.updatePickedRoads();
                    if(roadInfo[i][targetPosition]!=null)
                        System.out.println("有车辆被覆盖");
                    roadInfo[i][targetPosition]=currentCar;
                    currentRoad.addLoad(roadInfo);

                    if (currentCar.getPreset()==0)
                        currentCar.setPlanTime(timeCount);

                    c--;

                    limitCount++;

                    break;
                }
            }
        }
    }

    public void preSetCarStart(boolean priority,Road targetRoad,Cross toCross){
        List<Car> waiting=preWaitForStartCars;
        for (int c=0;c<preCount;c++) {
            Car currentCar=waiting.get(c);

            if(targetRoad!=null){
                if(currentCar.getPlanRoads().getFirst()!=targetRoad)
                    continue;
                else if(currentCar.getFrom()!=targetRoad.getNextCross(toCross))
                    continue;
            }

            if(currentCar.getPlanTime()<=timeCount&&currentCar.getPriority()==priority){
                Road currentRoad=currentCar.getPlanRoads().getFirst();
                Car[][] roadInfo=currentRoad.getOutFromCross(currentCar.getFrom());
                int currentSpeed=Math.min(currentCar.getSpeed(),currentRoad.getSpeed());
                for (int i = 0; i < currentRoad.getChannel(); i++) {
                    int j = 0;
                    for (; j < currentSpeed; j++) {
                        if (roadInfo[i][j] != null)
                            break;
                    }
                    int targetPosition;
                    if(j==currentSpeed)
                        targetPosition=j-1;
                    else
                    if(j>0) {
                        if (waitingCars.contains(roadInfo[i][j]))
                            break;
                        else
                            targetPosition = j - 1;
                    }else {
                        if(waitingCars.contains(roadInfo[i][j]))
                            break;
                        else
                            continue;
                    }
                    waitForStartCars.remove(currentCar);
                    preWaitForStartCars.remove(currentCar);
                    notReachedCars.add(currentCar);
                    currentCar.updatePickedRoads();

                    roadInfo[i][targetPosition]=currentCar;
                    currentRoad.addLoad(roadInfo);


                    preCount--;
                    c--;

                    break;
                }
            }
        }
    }
}

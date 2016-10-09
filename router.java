import java.io.*;
import java.util.ArrayList;
class RoutingTable
{
    int hops,next_lan,next_router,receiverTime;
    boolean isReceiver;
    int[][] childs;
    public RoutingTable(int h,int nl,int nr)
    {
        hops=h;
        next_lan=nl;
        next_router=nr;
        isReceiver=false;
        receiverTime=0;
        childs=new int[router.lans.length][3]; //1=lan number 2-if I'm the one to put the message on the lan 3- is there any child attached to the lan
        for(int y=0;y<childs.length;y++)
        {
            childs[y][0]=router.lans[y][0];
            childs[y][1]=0;
            childs[y][2]=0;
        }
    }
}
class lanChild  //childs attached to my lan for a particular source
{
    int lanInterface;
    int source;
    int childrouter;
    public lanChild(int a,int b,int c)
    {
        source=a;
        lanInterface=b;
        childrouter=c;
    }
}
class Sources   //sources for whom I've sent an NMR
{
    int source;
    int lastNMRsent;
    int arrived;
    public Sources(int value,int time)
    {
        source=value;
        arrived=time;
    }
}
public class router
{
    static int routerId;
    static int[][] lans;
    static ArrayList<NMR> nmr;
    static ArrayList<lanChild> chi;
    static RoutingTable[] rout_table_entry=new RoutingTable[10];
    static ArrayList<Sources> sources;
    static ArrayList<Integer> parent;
    public router(String[] args)									//parameterized constructor
    {
        routerId=Integer.parseInt(args[1]);
        int numberOfLans=args.length-2;
        chi=new ArrayList<lanChild>();
        lans=new int[numberOfLans][4];
        int j=2;
        sources=new ArrayList<Sources>();
        parent=new ArrayList<Integer>();
        nmr=new ArrayList<NMR>();
        for(int i=0;i<numberOfLans;i++,j++)							//lans and no of records read in the particualr lan file in lan[i][1]
        {
            lans[i][0]=Integer.parseInt(args[j]);
            lans[i][1]=0;   //how much I read from the file
            lans[i][2]=1;  //parent
            lans[i][3]=0; //leaf
        }
        try
        {
            BufferedWriter bf=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
            bf.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        for(int i=0;i<lans.length;i++)
        {
            try
            {
                BufferedWriter writer=new BufferedWriter(new FileWriter("lan"+lans[i][0]+".txt",true));
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
        initializeRoutingTable();
        
    }
    public void initializeRoutingTable()
    {
        for(int i=0;i<10;i++)										//setting up all the entries in the routing table to infinity '100'
        {
            rout_table_entry[i]=new RoutingTable(10,10,10);
        }
        for(int i=0;i<lans.length;i++)								//for router's attached to lan, changing their routing table entry and hop count
        {
            rout_table_entry[lans[i][0]].next_lan=lans[i][0];
            rout_table_entry[lans[i][0]].hops=0;
        }
    }
    public static void routerFunctions()
    {
        try
        {
            for(int i=0;i<=100;i++)
            {
                if(i%5==0)
                {
                    performDV(i);
                }
                readFromOtherLans(i);
                receiverTimeIncrement();
                sendNMR(i);
                NMRTimeIncrement();
                Thread.sleep(1000);
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public static void NMRTimeIncrement()
    {
        for(int l=0;l<nmr.size();l++)
        {
            nmr.get(l).time++;
            if(nmr.get(l).time==20)
            {
                nmr.remove(l);
            }
        }
    }
    public static void sendNMR(int now)
    {
        for(int d=0;d<sources.size();d++)               //sending nmr if I'm the leaf lan with respect to the source
        {
            int nextnmrat=sources.get(d).lastNMRsent;
            nextnmrat+=10;
            int sent=0;
            if(now==sources.get(d).arrived||nextnmrat==now)
            {
                int sourceLan=sources.get(d).source;
                int nextlan=rout_table_entry[sourceLan].next_lan;
                int nextrouter=rout_table_entry[sourceLan].next_router;
                for(int l=0;l<lans.length;l++)
                {
                    int noChild=0;
                    int totalChild=0;
                    boolean childPresent=false;
                    if(lans[l][0]!=nextlan)
                    {
                        if(rout_table_entry[lans[l][0]].isReceiver==false)
                        {
                            //if I'm the leaf lan I send nmr
                           
                            for(int y=0;y<rout_table_entry[sourceLan].childs.length;y++)
                            {
                                if(rout_table_entry[sourceLan].childs[y][2]==0)
                                {
                                    noChild+=1;
                                }
                            }
                            if(noChild==rout_table_entry[sourceLan].childs.length&&sent==0)
                            {
                                try
                                {
                                    BufferedWriter writer=new BufferedWriter(new FileWriter("lan"+nextlan+".txt",true));
                                    BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                    String towrite="NMR "+nextlan+" "+routerId+" "+sourceLan;
                                    writer.write(towrite);
                                    writer.write('\n');
                                    writer.close();
                                    we.write(towrite);
                                    we.write('\n');
                                    we.close();
                                    sent=1;
                                }
                                catch(Exception e )
                                {
                                    System.out.println(e);
                                }
                            }
                            else if(lans[l][0]!=sourceLan&&sent==0)
                            {
                                
                                int nmrtotal=0;
                                for(int s=0;s<nmr.size();s++)
                                {
                                    if(nmr.get(s).arrivedlan==lans[l][0]&&nmr.get(s).source==sourceLan)
                                        nmrtotal++;
                                }
                                for(int s=0;s<chi.size();s++)
                                {
                                    if(chi.get(s).source==sourceLan&&chi.get(s).lanInterface==lans[l][0])
                                        totalChild+=1;
                                }
                                if(nmrtotal==totalChild)
                                {
                                    if(rout_table_entry[sourceLan].next_lan!=sourceLan)
                                    {
                                        try
                                        {
                                            BufferedWriter writer=new BufferedWriter(new FileWriter("lan"+nextlan+".txt",true));
                                            BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                            String towrite="NMR "+nextlan+" "+routerId+" "+sourceLan;
                                            writer.write(towrite);
                                            writer.write('\n');
                                            writer.close();
                                            we.write(towrite);
                                            we.write('\n');
                                            we.close();
                                            sent=1;
                                        }
                                        catch(Exception e )
                                        {
                                            System.out.println(e);
                                        }
                                    }
                                }
                            }//end of else if
                        }//end of receiver=false
                       if(sent==0)
                        { // if I've a receiver attached to my outgoing lan but I'm not the one to send the data to the lan, send an NMR
                            if(rout_table_entry[lans[l][0]].isReceiver==true)
                            {
                                for(int s=0;s<chi.size();s++)
                                {
                                    if(chi.get(s).source==sourceLan&&chi.get(s).lanInterface==lans[l][0])
                                    {
                                        childPresent=true;
                                    }
                                }
                                for(int u=0;u<rout_table_entry[sourceLan].childs.length;u++)
                                {
                                if(childPresent==false&&rout_table_entry[sourceLan].childs[u][0]==lans[l][0]&&rout_table_entry[sourceLan].childs[u][1]==0&&lans[l][3]==1)
                                {
                                    try
                                    {
                                        BufferedWriter writer=new BufferedWriter(new FileWriter("lan"+nextlan+".txt",true));
                                        BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                        String towrite="NMR "+nextlan+" "+routerId+" "+sourceLan;
                                        writer.write(towrite);
                                        writer.write('\n');
                                        writer.close();
                                        we.write(towrite);
                                        we.write('\n');
                                        we.close();
                                        sent=1;
                                    }
                                    catch(Exception e )
                                    {
                                        System.out.println(e);
                                    }
                                }
                                }
                            }
                        }

                    }
                }
                if(sent==1)
                    sources.get(d).lastNMRsent=now;
                else
                    sources.get(d).lastNMRsent=now-9;
            }
        }
    }
    public static void receiverTimeIncrement()
    {
        for(int y=0;y<lans.length;y++)
        {
            if(rout_table_entry[y].isReceiver==true)
                rout_table_entry[y].receiverTime++;
            if(rout_table_entry[y].receiverTime==20)
            {
                rout_table_entry[y].receiverTime=0;
                rout_table_entry[y].isReceiver=false;
            }
        }
    }
    private static void readFromOtherLans(int time)
    {
        try
        {
            int[] flags=new int[10];
            int[] leafflag=new int[10];
            for(int i=0;i<lans.length;i++)
            {
                String file="lan"+lans[i][0]+".txt";
                BufferedReader reader=new BufferedReader(new FileReader(file));
                int new_pointer=0;
                String read;
                while((read=reader.readLine())!=null)
                {
                    if(new_pointer>=lans[i][1])
                    {
                        String[] tokens=read.split(" ");
                        if(tokens[0].equals("DV"))
                        {
                            
                            int tempLanID=Integer.parseInt(tokens[1]);
                            int tempRouterId=Integer.parseInt(tokens[2]);
                            if(Integer.parseInt(tokens[2])!=routerId)
                            {
                                lans[i][3]=1; //not a leaf lan
                                RoutingTable[] temp_table=new RoutingTable[10];
                                int t_number=3;
                                for(int j=0;j<10;j++)
                                {
                                    int temp_hops=Integer.parseInt(tokens[t_number]);
                                    t_number+=1;
                                    int temp_r=10;
                                    if(!tokens[t_number].equals("-"))
                                    {
                                        temp_r=Integer.parseInt(tokens[t_number]);
                                    }
                                    t_number+=1;
                                    temp_table[j]=new RoutingTable(temp_hops,10,temp_r);
                                }
                                if(routerId<tempRouterId)
                                {
                                    lans[i][2]=1; //Parent
                                }
                                else
                                {
                                    lans[i][2]=0;
                                }
                                for(int j=0;j<10;j++)
                                {
                                    if(temp_table[j].next_router==routerId)
                                    {
                                        for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                        {
                                            if(rout_table_entry[j].childs[y][0]==tempLanID)
                                            {
                                                rout_table_entry[j].childs[y][2]=1;
                                               
                                            }
                                            
                                        }
                                        for(int d=0;d<chi.size();d++)
                                        {
                                            if(chi.get(d).lanInterface==lans[i][0]&&chi.get(d).childrouter==tempRouterId&&chi.get(d).source==j)
                                            {
                                                chi.remove(d);
                                            }
                                        }
                                        chi.add(new lanChild(j,tempLanID,tempRouterId));
                                    }
                                    int hop=temp_table[j].hops;
                                    if((hop+1)<rout_table_entry[j].hops)
                                    {
                                        rout_table_entry[j].hops=hop+1;
                                        rout_table_entry[j].next_lan=tempLanID;
                                        rout_table_entry[j].next_router=tempRouterId;

                                    }
                                    else if((hop+1)==rout_table_entry[j].hops)
                                    {
                                        if(temp_table[j].next_router<rout_table_entry[j].next_router)
                                        {
                                            rout_table_entry[j].next_router=tempRouterId;
                                            rout_table_entry[j].next_lan=tempLanID;
                                        }
                                        else if(temp_table[j].next_router==10)
                                        {
                                            if(tempRouterId<rout_table_entry[j].next_router)
                                            {
                                                rout_table_entry[j].next_router=tempRouterId;
                                                rout_table_entry[j].next_lan=tempLanID;
                                            }
                                        }
                                    }
                                    else
                                    {

                                    }
                                    if(routerId==temp_table[j].next_router)
                                    {
                                        for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                        {
                                            if(rout_table_entry[j].childs[y][0]==tempLanID)
                                                rout_table_entry[j].childs[y][1]=1;
                                        }
                                    }
                                    else
                                    {
                                        if(rout_table_entry[j].hops==temp_table[j].hops)
                                        {
                                            if((routerId<tempRouterId)&&(temp_table[j].next_router!=10))
                                            {
                                                for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                                {
                                                    if((rout_table_entry[j].childs[y][0]==tempLanID)&&(flags[j]==0))
                                                        rout_table_entry[j].childs[y][1]=1;
                                                }
                                            }
                                            else
                                            {
                                                if((routerId>tempRouterId)&&(temp_table[j].next_router!=10))
                                                {
                                                    for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                                    {
                                                        if(rout_table_entry[j].childs[y][0]==tempLanID){
                                                            rout_table_entry[j].childs[y][1]=0;
                                                            flags[j]=1;
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    if(rout_table_entry[j].hops!=10)
                                                    {
                                                        for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                                        {
                                                            if((rout_table_entry[j].childs[y][0]!=tempLanID)&&flags[j]==0)
                                                                rout_table_entry[j].childs[y][1]=1;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if((rout_table_entry[j].hops<temp_table[j].hops)&&(temp_table[j].hops!=10))
                                            {
                                                for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                                {
                                                    if((rout_table_entry[j].childs[y][0]==tempLanID)&&(flags[j]==0))
                                                        rout_table_entry[j].childs[y][1]=1;
                                                }
                                            }
                                            else
                                            {
                                                if(rout_table_entry[j].hops>temp_table[j].hops&&rout_table_entry[j].hops!=10)
                                                    for(int y=0;y<rout_table_entry[j].childs.length;y++)
                                                    {
                                                        if(rout_table_entry[j].childs[y][0]==tempLanID)
                                                        {
                                                            rout_table_entry[j].childs[y][1]=0;
                                                            flags[j]=1;
                                                        }
                                                    }
                                                
                                            }
                                        }
                                    }
                                }
                                
                            }
                        }
                        else if(tokens[0].equals("receiver"))
                        {
                            rout_table_entry[lans[i][0]].isReceiver=true;
                            rout_table_entry[lans[i][0]].receiverTime=0;
                        }
                        else if(tokens[0].equals("data"))
                        {
                            int sourceLan=Integer.parseInt(tokens[2]);
                            int messageLan=Integer.parseInt(tokens[1]);
                            boolean sourceFound=false;
                            int got=0;
                            for(int d=0;d<sources.size();d++)
                            {
                                if(sources.get(d).source==sourceLan)
                                {
                                    sourceFound=true;
                                }
                            }
                            if(sourceFound==false)
                                sources.add(new Sources(sourceLan,time));
                            if(rout_table_entry[sourceLan].next_lan==messageLan)
                            {
                                BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                we.write(read);
                                we.write('\n');
                                got=1;
                                we.close();
                            }

                            if(rout_table_entry[sourceLan].next_lan==messageLan)
                            {

                                for(int l=0;l<lans.length;l++)
                                {
                                    int sl=0;
                                    if(lans[l][0]!=messageLan)
                                    {
                                        boolean childPresent=false;
                                        
                                        int totalChild=0;
                                        for(int s=0;s<chi.size();s++)
                                        {
                                            if(chi.get(s).source==sourceLan&&chi.get(s).lanInterface==lans[l][0])
                                            {
                                                totalChild+=1;
                                                childPresent=true;
                                            }
                                        }
                                        if(sl==0)
                                        {
                                            
                                            if(rout_table_entry[lans[l][0]].isReceiver==true&&lans[l][3]==0)
                                            {
                                                BufferedWriter writ=new BufferedWriter(new FileWriter("lan"+lans[l][0]+".txt",true));
                                                String toWrit="data"+" "+lans[l][0]+" "+sourceLan;
                                                writ.write(toWrit);
                                                //writ.write(" by router"+routerId);
                                                writ.write('\n');
                                                writ.close();
                                                sl=1;
                                            }
                                        }

                                        if(sl==0)
                                        {
                                            for(int y=0;y<rout_table_entry[sourceLan].childs.length;y++)
                                            {
                                                if((rout_table_entry[sourceLan].childs[y][0]==lans[l][0]&&rout_table_entry[sourceLan].childs[y][1]==1)&&(rout_table_entry[lans[i][0]].childs[y][2]==1||rout_table_entry[lans[l][0]].isReceiver==true)) //I'm the router who should put the message on the lan
                                                {
                                                    int nmrtotal=0;
                                                    for(int s=0;s<nmr.size();s++)
                                                    {
                                                        if(nmr.get(s).arrivedlan==lans[l][0]&&nmr.get(s).source==sourceLan)
                                                            nmrtotal++;
                                                    }
                                                    if(rout_table_entry[lans[l][0]].isReceiver==true||nmrtotal!=totalChild)
                                                    {
                                                        BufferedWriter writ=new BufferedWriter(new FileWriter("lan"+lans[l][0]+".txt",true));
                                                        String toWrit="data"+" "+lans[l][0]+" "+sourceLan;
                                                        writ.write(toWrit);
                                                        //writ.write(" by router"+routerId);
                                                        writ.write('\n');
                                                        writ.close();
                                                        sl=1;
                                                    }
                                                }
                                            }
                                        }
                                        if(sl==0)
                                        {
                                            if(rout_table_entry[lans[l][0]].isReceiver==true&&childPresent==false)
                                            {
                                                    if(lans[l][2]==1)
                                                    {
                                                        BufferedWriter writ=new BufferedWriter(new FileWriter("lan"+lans[l][0]+".txt",true));
                                                        String toWrit="data"+" "+lans[l][0]+" "+sourceLan;
                                                        writ.write(toWrit);
                                                        //writ.write(" by router"+routerId);
                                                        writ.write('\n');
                                                        writ.close();
                                                        sl=1;
                                                    }
                                            }
                                        }
                                        
                                    }
                                }
                            }
                        }
                        else if(tokens[0].equals("NMR"))
                        {
                            int flag=0;
                            int readlan=Integer.parseInt(tokens[1]);
                            int sentrouter=Integer.parseInt(tokens[2]);
                            int s_NMR=Integer.parseInt(tokens[3]);
                            boolean ischild=false;
                            for(int s=0;s<chi.size();s++)
                            {
                                if(chi.get(s).source==s_NMR&&chi.get(s).childrouter==sentrouter&&chi.get(s).lanInterface==readlan)
                                    ischild=true;
                            }
                            if(ischild==true)
                            {
                                for(int l=0;l<nmr.size();l++)
                                {
                                    if((nmr.get(l).source==s_NMR)&&(readlan==nmr.get(l).arrivedlan)&&(sentrouter==nmr.get(l).sentRouter))
                                    {
                                        nmr.get(l).time=0;
                                        flag=1;
                                        
                                        BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                        we.write(read);
                                        we.write('\n');
                                        we.close();
                                        break;
                                    }
                                }
                                if(flag==0)
                                {
                                    nmr.add(new NMR(readlan,sentrouter,s_NMR));
                                    BufferedWriter we=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
                                    we.write(read);
                                    we.write('\n');
                                    we.close();
                                }
                            }
                        }
                    }
                    new_pointer+=1;
                }
                lans[i][1]=new_pointer;
                reader.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void performDV(int q)
    {
        try
        {
            String dv=null;
            dv=String.valueOf(routerId);
            for(int i=0;i<rout_table_entry.length;i++)
            {
                if(rout_table_entry[i].hops==10)
                {
                    dv=dv+" "+rout_table_entry[i].hops+" -";
                }
                else
                    dv=dv+" "+rout_table_entry[i].hops+" "+rout_table_entry[i].next_router;
            }
            BufferedWriter bf=new BufferedWriter(new FileWriter("rout"+routerId+".txt",true));
            for(int i=0;i<lans.length;i++)
            {
                String toWrite=null;
                toWrite="DV "+lans[i][0]+" "+dv;
                bf.write(toWrite);
                bf.write('\n');
            }
            bf.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public static void main(String[] args)
    {
        router r=new router(args);
        routerFunctions();
    }
}

class NMR
{
    int arrivedlan;
    int sentRouter;
    int source;
    int time;
    public NMR(int a,int b,int c)
    {
        arrivedlan=a;
        sentRouter=b;
        source=c;
        time=0;
    }
}

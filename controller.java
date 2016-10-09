import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class controller {
    static int[][] hosts;								//contains [hostid][no_of_records read so far]
    static int[][] routers;								//contains [routerid][no_of_records read so far]
    static int[] lans;

    public controller(int[] host,int[] router,int[] lan) // parameterised constructor
    {
        hosts=new int[host.length][2];
        for(int i=0;i<host.length;i++)
        {
            hosts[i][0]=host[i];
            hosts[i][1]=-1;
            try
            {
                BufferedWriter writer=new BufferedWriter(new FileWriter("hout"+host[i]+".txt",true));
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
        routers=new int[router.length][2];
        for(int i=0;i<router.length;i++)
        {
            routers[i][0]=router[i];
            routers[i][1]=0;
            try
            {
                BufferedWriter writer=new BufferedWriter(new FileWriter("rout"+router[i]+".txt",true));
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
        lans=new int[lan.length];
        for(int i=0;i<lan.length;i++)
        {
            lans[i]=lan[i];
            try
            {
                BufferedWriter writer=new BufferedWriter(new FileWriter("lan"+lans[i]+".txt",true));
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }
    public static void constrcall(String[] args)				// to split the input line and find no of routers,hosts and lans
    {
        int in=Arrays.asList(args).indexOf("router");
        int[] temp_host=new int[in-2];
        for(int j=2;j<in;j++)
            temp_host[j-2]=Integer.parseInt(args[j]);
        
        int y=Arrays.asList(args).indexOf("lan");
        int[] temp_router=new int[y-in-1];
        for(int j=in+1,k=0;j<y;j++,k++)
            temp_router[k]=Integer.parseInt(args[j]);
        
        int[] temp_controller=new int[args.length-y-1];
        for(int j=y+1,k=0;j<args.length;j++,k++)
            temp_controller[k]=Integer.parseInt(args[j]);
        new controller(temp_host,temp_router,temp_controller);		//calls contructor for initialization
    }
    private static void readHostFile(int q)
    {
        try
        {
            for(int i=0;i<hosts.length;i++)
            {
                String file="hout"+hosts[i][0]+".txt";
                BufferedReader rf = new BufferedReader(new FileReader("hout"+hosts[i][0]+".txt"));
                //System.out.println(file);
                String line;
                int new_pointer=0;
                while((line=rf.readLine())!=null)
                {
                    if(new_pointer>=hosts[i][1])
                    {
                        String[] rl=line.split(" ");
                        file="lan"+rl[1]+".txt";
                       // System.out.println(file);
                        BufferedWriter writer=new BufferedWriter(new FileWriter(file,true));
                        writer.write(line);
                        writer.write('\n');
                        writer.close();
                    }
                    new_pointer+=1;
                }
                hosts[i][1]=new_pointer;
                rf.close();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    private static void readRouterFile()
    {
        try
        {
            for(int i=0;i<routers.length;i++)
            {
                String file="rout"+routers[i][0]+".txt";
                BufferedReader rf = new BufferedReader(new FileReader("rout"+routers[i][0]+".txt"));
                //System.out.println(file);
                String line;
                int new_pointer=0;
                while((line=rf.readLine())!=null)
                {
                    if(new_pointer>=routers[i][1])
                    {
                        String[] rl=line.split(" ");
                        file="lan"+rl[1]+".txt";
                        if(rl[0].equals("DV"))
                        {
                            BufferedWriter writer=new BufferedWriter(new FileWriter(file,true));
                            writer.write(line);
                            writer.write('\n');
                            writer.close();
                        }
                    }
                    new_pointer+=1;
                }
                routers[i][1]=new_pointer;
                rf.close();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public static void main(String[] args)
    {
        constrcall(args);						//initialize controller
        try
        {
            for(int i=1;i<=100;i++)				//every on sec
            {
                //System.out.println("call "+(i+1));
                readHostFile(i);					//read from host files and put in respective lan file
                readRouterFile();				//read from router files and put in respective lan file
                Thread.sleep(1000);
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
import java.io.*;

public class host {
    static int hostId;
    static int lanId;
    static String type;
    static int timeToStart='\0';
    static int period='\0';
    int count;
    int last_read=-1;
    public host(String args[])					//Initialization (parameterized constructor)
    {
        hostId=Integer.parseInt(args[1]);
        lanId=Integer.parseInt(args[2]);
        type=args[3];
        if(args.length>4)
        {
            timeToStart=Integer.parseInt(args[4]);
            period=Integer.parseInt(args[5]);
        }
        try
        {
            BufferedWriter bf=new BufferedWriter(new FileWriter("lan"+lanId+".txt",true));
            bf.close();
            bf=new BufferedWriter(new FileWriter("hout"+hostId+".txt",true));
            bf.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void writeFile()
    {
        String file="hout"+hostId+".txt";		//corresponding host file hout<host>.txt
        try
        {
            FileWriter f= new FileWriter(file,true);
            BufferedWriter bf=new BufferedWriter(f);
            if(type.equals("sender"))
            {
                bf.write("data");				//String "data"
                bf.write(" "+lanId);					//lanId where the message is to be forwarded
                bf.write(" "+lanId);				//lanId of the host sending the message
                bf.write('\n');
            }
            if(type.equals("receiver"))
            {
                bf.write("receiver");			//String "receiver"
                bf.write(" "+lanId);				//lanID of the host
                bf.write('\n');
            }
            bf.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void hostFunction(host h)
    {
        long sleeptime=timeToStart*1000;
        try
        {
            if(type.equals("sender"))				//Calling the write function every period secs from timeToStart as the host...
            {										//...is a sender
                Thread.sleep(sleeptime);			//sleep till To Start
                for(int i=timeToStart;i<100;i=i+period)
                {
                    h.writeFile();
                    sleeptime=period*1000;			//sleep for period secs
                    Thread.sleep(sleeptime);
                    //System.out.println("at"+i+"sec");
                   
                }
            }
            if(type.equals("receiver"))				//Calling the write function every 10 secs as the host is a receiver
            {
                for(int i=1;i<=100;i++)
                {
                    
                    h.readFile();
                    //System.out.println(i);
                    if(i%10==1)
                    {
                        h.writeFile();
                    }
                    Thread.sleep(1000);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    private void readFile() 						//read from lan file and write the message to the hin file
    {
        String readfile="lan"+lanId+".txt";
        String writefile="hin"+hostId+".txt";
        try
        {
            BufferedWriter writer=new BufferedWriter(new FileWriter(writefile,true));
            BufferedReader reader=new BufferedReader(new FileReader(readfile));
            String line;
            int new_pointer=0;
            while((line=reader.readLine())!=null)
            {
                if(new_pointer>=last_read)
                {
                    String[] t=line.split(" ");
                    if(t[0].equals("data"))
                    {
                        writer.write(line);
                        writer.write('\n');
                    }
                }
                new_pointer+=1;
            }
            last_read=new_pointer;
            writer.close();
            reader.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public static void main(String[] args)
    {
        host h=new host(args);
        hostFunction(h);	
    }
}
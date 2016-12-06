package softwareinclude.ro.portforwardandroid.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

import net.sbbi.upnp.messages.UPNPResponseException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import softwareinclude.ro.portforwardandroid.network.UPnPPortMapper;
import softwareinclude.ro.portforwardandroid.util.ApplicationConstants;

/**
 * Created by manolescusebastian on 8/9/14.
 */
public class AddPortAsync extends AsyncTask<Void, Void, Void> {


    private Context context;
    private UPnPPortMapper uPnPPortMapper;
	ProgressDialog progressDialog = null;
    private String externalIP;
    private String internalIP;
    private int externalPort;
    private int internalPort;
	public String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator;

    private ServerSocket serverSocket;
    public AddPortAsync(Context context, String externalIP, String internalIP,
                        int externalPort, int internalPort) {

        this.externalIP = externalIP;
        this.internalIP = internalIP;
        this.externalPort = externalPort;
        this.internalPort = internalPort;
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        uPnPPortMapper = new UPnPPortMapper();
    }

    @Override
    protected Void doInBackground(Void... params) {

            if(uPnPPortMapper != null){
                try {
                    uPnPPortMapper.openRouterPort(externalIP, externalPort,internalIP,internalPort, ApplicationConstants.ADD_PORT_DESCRIPTION);
                    try{
                        //建立serverSocket
        				serverSocket = new ServerSocket(externalPort);

                        //等待連線 有問題 他會持續接收嗎?
        				while (true) {
                            //接收連線
        					System.out.println("傾聽中....");
        					Socket clientSkt = serverSocket.accept();
        					System.out.println("連線成功....");
        					try {
        						BufferedInputStream inputStream = new BufferedInputStream(clientSkt.getInputStream());
                                // 取得檔案名稱
        		                String fileName = new DataInputStream(inputStream).readUTF();
        		 
        		                System.out.printf("接收檔案 %s ...", sdcardPath+fileName);
        		 
        		                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(sdcardPath+fileName));
        		 
        		                int readin; 
        		                while((readin = inputStream.read()) != -1) { 
        		                    outputStream.write(readin);
        		                    Thread.yield();
        		                } 
        		 
        		                outputStream.close();                
        		                inputStream.close(); 
        		 
        		                clientSkt.close(); 
        		 
        		                System.out.println("\n檔案接收完畢！");
        						 break;
        					} catch (Exception e) {
        						e.printStackTrace();
        					}
        					finally{
        						
        					}
        				}
          			}catch(IOException e){
          				e.printStackTrace();
          			}
                
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UPNPResponseException e) {
                    e.printStackTrace();
                }
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //Send broadcast for update in the main activity
        Intent i = new Intent(ApplicationConstants.APPLICATION_ENCODING_TEXT);
        context.sendBroadcast(i);

    }
}

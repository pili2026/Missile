package softwareinclude.ro.portforwardandroid.asyncTasks;

import android.os.AsyncTask;

import net.sbbi.upnp.messages.UPNPResponseException;

import java.io.IOException;

import softwareinclude.ro.portforwardandroid.network.UPnPPortMapper;

public class RemoviePortAsync extends AsyncTask<Void, Void, Void> {
    private UPnPPortMapper uPnPPortMapper;
    private String externalIP;
    private int externalPort;
    
    public RemoviePortAsync(String externalIP, int externalPort) {

    this.externalIP = externalIP;
	this.externalPort = externalPort;
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
            	uPnPPortMapper.removePort(externalIP, externalPort);
            	}
            catch (IOException e) {
                e.printStackTrace();
            } catch (UPNPResponseException e) {
                e.printStackTrace();
            }
        }
		return null;
	}

}

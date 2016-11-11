package upnp.service;

import android.app.Application;

import org.teleal.cling.model.meta.Device;

import java.util.ArrayList;


public class UpnpBrowserApp extends Application {
	
	private ArrayList<Device> devicelist = new ArrayList();
	
	
	public Device getDevice(int position) {
		  return devicelist.get(position);
		}
	

	
	public void addDevice(Device d){
		devicelist.add(d);
	}
	
	public void rmDevice(Device d){
		devicelist.remove(d);
	}
	

}

/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package upnp.service;

import org.teleal.cling.binding.annotations.UpnpAction;
import org.teleal.cling.binding.annotations.UpnpOutputArgument;
import org.teleal.cling.binding.annotations.UpnpService;
import org.teleal.cling.binding.annotations.UpnpServiceId;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.annotations.UpnpStateVariable;

import java.beans.PropertyChangeSupport;

@UpnpService(
        serviceId = @UpnpServiceId("MASP"),
        serviceType = @UpnpServiceType(value = "MASP", version = 1)
)
public class SwitchPower {
    //這邊指開啟upnp後他的功能
    private final PropertyChangeSupport propertyChangeSupport;

    public SwitchPower() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpStateVariable(defaultValue = "0")
    private boolean status = false;



    @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
    public boolean getStatus() {
        return status;
    }

}

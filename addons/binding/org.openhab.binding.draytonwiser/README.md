# Drayton Wiser Binding

This binding integrates the [Drayton Wiser Smart Heating System](https://wiser.draytoncontrols.co.uk/). The integration happens through the HeatHub, which acts as an IP gateway to the ZigBee devices (thermostats and TRVs).

## Supported Things

The Drayton Wiser binding supports the following things:
* Bridge - The network device in the controller that allows us to interact with the other devices in the system
* Controller - The _HeatHub_ attached to the boiler. This also acts as the hub device.
* Rooms - Virtual groups of _Room Thermostats_ and _TRVs_ that can have temperatures and schedules
* Room Thermostats - Wireless thermostats which monitor temperature and humidity, and call for heat
* Smart TRVs - Wireless TRVs that monitor temperature, alter the radiator valve state and call for heat
* Hot Water - Virtual thing to manage hot water states

## Discovery

The HeatHub can be discovered automatically via mDNS, however the `SECRET` cannot be determined automatically. Once the `SECRET` has been configured, all other devices can be discovered by triggering device discovery again.

## Binding Configuration

None required

## Thing Configuration

### HeatHub Configuration

Once discovered, the HeatHub `AUTHTOKEN` needs to be configured. There are a few ways to obtain this, assuming you have already configured the system using the Wiser App.

1. Temporarily install a packet sniffing tool on your mobile device. Every request made includes the `SECRET` in the header.
2. Enable setup mode on the HeatHub. Connect a machine temporarily to the `WiserHeat_XXXXX` network and browse to `http://192.168.8.1/secret` to obtain the `AUTHTOKEN`.

The `REFRESH` interval defines in seconds, how often the binding will poll the controller for updates.

The `AWAY MODE SET POINT` defines the temperature in degrees celcius that will be sent to the heathub when away mode is activated.

### Manual configuration with .things files

```
Bridge draytonwiser:heathub:HeatHub [ ADDR="192.168.1.X", REFRESH=60, AUTHTOKEN="authtoken from hub" ]
{
	controller controller
	room livingroom	[ roomName="Living Room" ]
	room bathroom	[ roomName="Bathroom" ]
	room bedroom	[ roomName="Bedroom" ]
	roomstat livingroomstat [ serialNumber="ABCDEF1234" ]
	itrv livingroomtrv [ serialNumber="ABCDEF1235" ]
	hotwater hotwater
}
```

The `roomName` corresponds to the room name configured in the Wiser App. It is not case sensitive. The `serialNumber` corresponds to the device serial number which can be found on a sticker inside the battery compartment of the Smart Valves/TRVs, and behind the wall mount of the Room Thermostats.

## Channels

### Readonly Channels

#### Controller

| Channel                   | Description                                              | Implemented |
|---------------------------|----------------------------------------------------------|-------------|
| `heatingOverride`         | State of the heating override button on the controller   | Yes         |
| `heatChannel1Demand`      | Current demand level of heating channel 1                | Yes         |
| `heatChannel1DemandState` | Is channel 1 calling the boiler for heat                 | Yes         |
| `heatChannel2Demand`      | Current demand level of heating channel 2                | Yes         |
| `heatChannel2DemandState` | Is channel 2 calling the boiler for heat                 | Yes         |
| `currentSignalRSSI`       | Relative Signal Strength Indicator                       | Yes         |
| `currentSignalStrength`   | Human readable signal strength                           | Yes         |

#### Hot Water

| Channel                   | Description                                              | Implemented |
|---------------------------|----------------------------------------------------------|-------------|
| `hotWaterOverride`        | State of the hot water override button on the controller | Yes         |
| `hotWaterDemandState`     | Is hot water calling the boiler for heat                 | Yes         |

#### Room

| Channel              | Description                                                                  | Implemented |
|----------------------|------------------------------------------------------------------------------|-------------|
| `currentTemperature` | Currently reported temperature                                               | Yes         |
| `currentHumidity`    | Currently reported humidity (if there is a room stat configured in this room | Yes         |
| `currentDemand`      | Current heat demand percentage of the room                                   | Yes         |
| `heatRequest`        | Is the room actively requesting heat from the controller                     | Yes         |

#### Room Stat

| Channel                 | Description                        | Implemented |
|-------------------------|------------------------------------|-------------|
| `currentTemperature`    | Currently reported temperature     | Yes         |
| `currentHumidity`       | Currently reported humidity        | Yes         |
| `currentSetPoint`       | Currently reported set point       | Yes         |
| `currentBatteryVoltage` | Currently reported battery voltage | Yes         |
| `currentBatteryLevel`   | Human readable battery level       | Yes         |
| `currentSignalRSSI`     | Relative Signal Strength Indicator | Yes         |
| `currentSignalLQI`      | Link Quality Indicator             | Yes         |
| `currentSignalStrength` | Human readable signal strength     | Yes         |
| `zigbeeConnected`       | Is the roomstat joined to network  | Yes         |

#### Smart TRV

| Channel                 | Description                               | Implemented |
|-------------------------|-------------------------------------------|-------------|
| `currentTemperature`    | Currently reported temperature            | Yes         |
| `currentDemand`         | Current heat demand percentage of the TRV | Yes         |
| `currentSetPoint`       | Currently reported set point              | Yes         |
| `currentBatteryVoltage` | Currently reported battery voltage        | Yes         |
| `currentBatteryLevel`   | Human readable battery level              | Yes         |
| `currentSignalRSSI`     | Relative Signal Strength Indicator        | Yes         |
| `currentSignalLQI`      | Link Quality Indicator                    | Yes         |
| `currentSignalStrength` | Human readable signal strength            | Yes         |
| `zigbeeConnected`       | Is the TRV joined to network              | Yes          |

### Writeable Channels

#### Controller

| Channel            | Description                              | Implemented |
|--------------------|------------------------------------------|-------------|
| `awayModeState`    | Has away mode been enabled               | Yes         |
| `ecoModeState`     | Has eco mode been enabled                | Yes         |

#### Hot Water

| Channel            | Description                                | Implemented |
|--------------------|--------------------------------------------|-------------|
| `manualModeState`  | Has manual mode been enabled               | Yes         |
| `hotWaterSetPoint`  | The current hot water setpoint (on or off) | Yes         |
| Hot Water Boosting | Boost the hot water for a set interval     | No          |
| Schedules          | The time and hot water state schedule      | No          |

#### Room

| Channel           | Description                                    | Implemented |
|-------------------|------------------------------------------------|-------------|
| `currentSetPoint` | The current set point temperature for the room | Yes         |
| `manualModeState` | Has manual mode been enabled                   | Yes         |
| Room Boosting     | Boost the room temperature for a set interval  | No          |
| Schedules         | The Time and Set Point schedule                | No          |

#### Known string responses for specific channels:

| Channel                 | Known responses                          |
|-------------------------|------------------------------------------|
| `currentSignalStrength` | `{ "VeryGood", "Good", "Medium", "Poor", "NoSignal" }` |
| `currentBatteryLevel`   | `{ "Full", "Normal", "TwoThirds", "OneThird", "Low", "Critical" }`              |

## Full Example

TODO

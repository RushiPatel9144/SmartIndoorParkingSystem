/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.utility;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingLocation;
import ca.tech.sense.it.smart.indoor.parking.system.model.parking.ParkingSlot;

import java.util.List;
import java.util.Map;

public class ParkingInterface {

    // Interfaces for callback
    public interface FetchLocationsCallback {
        void onFetchSuccess(Map<String, ParkingLocation> locations);
        void onFetchFailure(Exception exception);
    }

    public interface FetchLocationCallback {
        void onFetchSuccess(ParkingLocation location);
        void onFetchFailure(Exception exception);
    }

    public interface FetchSlotsCallback {
        void onFetchSuccess(Map<String, ParkingSlot> slots);
        void onFetchFailure(Exception exception);
    }

    public interface ParkingLocationFetchCallback {
        void onFetchSuccess(List<ParkingLocation> locations);

        void onFetchFailure(String errorMessage);
    }


}

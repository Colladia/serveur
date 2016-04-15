package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;

public class DiaElt {
    public Map<String, String> propertyMap = new HashMap<String, String>();
    public Map<String, DiaElt> subEltMap = new HashMap<String, DiaElt>();
    
    public DiaElt(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }
}

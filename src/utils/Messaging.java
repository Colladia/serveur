package utils;

public class Messaging {
    public static String DESCRIPTION = "description"; // field containing the properties and sub-element description
    public static String PROPERTIES = "properties"; // field containing the properties of a diagram/element
    public static String TYPE = "type"; // type of query (PUT, GET etc.)
    public static String PATH = "path"; // path to the element
    public static String STATUS = "status"; // status : OK or KO
    public static String OK = "OK";
    public static String KO = "KO";
    public static String ERROR = "error"; // field for error message
    public static String LIST = "diagram-list"; // field for diagram list
    public static String PROPERTIES_LIST = "properties-list"; // field for a list of properties name
    
    public static String TYPE_RESTORE = "restore"; // new type for query to diaAgt for restoring from a complete description
}

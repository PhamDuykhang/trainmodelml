package vn.edu.ctu.cit.thesis.TrainHU;

public class Hemorrhage {
    private String PatientID; //id benh nhan
    private String PatientName; // ten benh nhan
    private String PatientAge; // tuoi benh nhan
    private String PatientSex; // gioi tinh
    private String InstitutionName; // ten bv
    private String InstitutionAddress; // dic chi benh vien
    private String AccessionNumber; // ngoai tk
    private String Manufacturer; // philips
    private String Modality; // ct
    private float Area;
    private float CentroidX;
    private float CentroidY;
    private float Perimeter;
    private float DistanceWithSkull;
    private float EquivDiameter;
    private float AreaBoudingbox;
    private float Solidity;
    private float Extent;
    private float Eccentricity;
    private float MajorAxisLength;
    private float MinorAxisLength;
    private float Orientation;

    public Hemorrhage() {
    }

    public String getPatientID() {
        return PatientID;
    }

    public void setPatientID(String patientID) {
        PatientID = patientID;
    }

    public String getPatientName() {
        return PatientName;
    }

    public void setPatientName(String patientName) {
        PatientName = patientName;
    }

    public String getPatientAge() {
        return PatientAge;
    }

    public void setPatientAge(String patientAge) {
        PatientAge = patientAge;
    }

    public String getPatientSex() {
        return PatientSex;
    }

    public void setPatientSex(String patientSex) {
        PatientSex = patientSex;
    }

    public String getInstitutionName() {
        return InstitutionName;
    }

    public void setInstitutionName(String institutionName) {
        InstitutionName = institutionName;
    }

    public String getInstitutionAddress() {
        return InstitutionAddress;
    }

    public void setInstitutionAddress(String institutionAddress) {
        this.InstitutionAddress = institutionAddress;
    }

    public String getAccessionNumber() {
        return AccessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        AccessionNumber = accessionNumber;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getModality() {
        return Modality;
    }

    public void setModality(String modality) {
        Modality = modality;
    }

    public float getArea() {
        return Area;
    }

    public void setArea(float area) {
        Area = area;
    }

    public float getCentroidX() {
        return CentroidX;
    }

    public void setCentroidX(float centroidX) {
        CentroidX = centroidX;
    }

    public float getCentroidY() {
        return CentroidY;
    }

    public void setCentroidY(float centroidY) {
        CentroidY = centroidY;
    }

    public float getPerimeter() {
        return Perimeter;
    }

    public void setPerimeter(float perimeter) {
        Perimeter = perimeter;
    }

    public float getDistanceWithSkull() {
        return DistanceWithSkull;
    }

    public void setDistanceWithSkull(float distanceWithSkull) {
        DistanceWithSkull = distanceWithSkull;
    }

    public float getEquivDiameter() {
        return EquivDiameter;
    }

    public void setEquivDiameter(float equivDiameter) {
        EquivDiameter = equivDiameter;
    }

    public float getAreaBoudingbox() {
        return AreaBoudingbox;
    }

    public void setAreaBoudingbox(float areaBoudingbox) {
        AreaBoudingbox = areaBoudingbox;
    }

    public float getSolidity() {
        return Solidity;
    }

    public void setSolidity(float solidity) {
        Solidity = solidity;
    }

    public float getExtent() {
        return Extent;
    }

    public void setExtent(float extent) {
        Extent = extent;
    }

    public float getEccentricity() {
        return Eccentricity;
    }

    public void setEccentricity(float eccentricity) {
        Eccentricity = eccentricity;
    }

    public float getMajorAxisLength() {
        return MajorAxisLength;
    }

    public void setMajorAxisLength(float majorAxisLength) {
        MajorAxisLength = majorAxisLength;
    }

    public float getMinorAxisLength() {
        return MinorAxisLength;
    }

    public void setMinorAxisLength(float minorAxisLength) {
        MinorAxisLength = minorAxisLength;
    }

    public float getOrientation() {
        return Orientation;
    }

    public void setOrientation(float orientation) {
        Orientation = orientation;
    }
}

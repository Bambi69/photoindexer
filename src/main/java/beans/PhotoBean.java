package beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhotoBean implements Serializable {

    private String name;
    private String directory;
    private String dateTimeOriginal;
    private Integer yearTimeOriginal;
    private Integer monthTimeOriginal;
    private List<String> faces = new ArrayList<String>();
    private List<String> unknownKeywords= new ArrayList<String>();
    private LocationBean location;
    private String cameraModel;
    private Date dateIndexed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public void setDateTimeOriginal(String dateTimeOriginal) {
        this.dateTimeOriginal = dateTimeOriginal;
    }

    public Integer getYearTimeOriginal() {
        return yearTimeOriginal;
    }

    public void setYearTimeOriginal(Integer yearTimeOriginal) {
        this.yearTimeOriginal = yearTimeOriginal;
    }

    public Integer getMonthTimeOriginal() {
        return monthTimeOriginal;
    }

    public void setMonthTimeOriginal(Integer monthTimeOriginal) {
        this.monthTimeOriginal = monthTimeOriginal;
    }

    public List<String> getFaces() {
        return faces;
    }

    public void setFaces(List<String> faces) {
        this.faces = faces;
    }

    public List<String> getUnknownKeywords() {
        return unknownKeywords;
    }

    public void setUnknownKeywords(List<String> unknownKeywords) {
        this.unknownKeywords = unknownKeywords;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public Date getDateIndexed() {
        return dateIndexed;
    }

    public void setDateIndexed(Date dateIndexed) {
        this.dateIndexed = dateIndexed;
    }

    public LocationBean getLocation() {
        return location;
    }

    public void setLocation(LocationBean location) {
        this.location = location;
    }
}

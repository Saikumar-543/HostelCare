package model;

import java.sql.Timestamp;

/** An image attached to a complaint - either student-uploaded evidence ("BEFORE") or staff-uploaded proof of fix ("RESOLUTION"). */
public class ComplaintImage {
    public enum Kind { BEFORE, RESOLUTION }

    private int imageId;
    private String complaintId;
    private String imageName;
    private String imagePath;
    private String imageType;
    private long imageSize;
    private Kind kind;
    private Timestamp uploadedAt;

    public ComplaintImage() { }

    public ComplaintImage(String complaintId, String imageName, String imagePath,
                           String imageType, long imageSize, Kind kind) {
        this.complaintId = complaintId;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageType = imageType;
        this.imageSize = imageSize;
        this.kind = kind;
    }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public long getImageSize() { return imageSize; }
    public void setImageSize(long imageSize) { this.imageSize = imageSize; }

    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}

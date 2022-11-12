package nz.ac.canterbury.seng302.identityprovider.database;

import com.google.protobuf.ByteString;

import javax.persistence.*;

@Entity
public class PhotoModel {
    @Id
    private int id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] userPhoto;

    protected PhotoModel () {};

    public PhotoModel (int userId, byte[] rawImage) {
        this.id = userId;
        this.userPhoto = rawImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(byte[] userPhoto) {
        this.userPhoto = userPhoto;
    }
}

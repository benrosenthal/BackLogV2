package com.nychareport.backlog.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;
import com.nychareport.backlog.Constants;

import java.util.HashMap;

/**
 * Created by User_1_Benjamin_Rosenthal on 4/5/16.
 */
public class Problem implements Parcelable {

    private String problem;
    private String problemID;
    private String description;
    private String location;
    private String problemPic;
    private String postedBy;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;

    public Problem(){}

    public Problem(String problem, String problemDescription, String problemLocation, String problemPic,
                   String postedBy, HashMap<String, Object> timestampCreated) {
        this.problem = problem;
        description = problemDescription;
        location = problemLocation;
        this.problemPic = problemPic;
        this.postedBy = postedBy;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }


    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getProblemDescription() {
        return description;
    }

    public void setProblemDescription(String problemDescription) {
        description = problemDescription;
    }

    public String getProblemLocation() {
        return location;
    }

    public void setProblemLocation(String problemLocation) {
        location = problemLocation;
    }

    public String getProblemPic() {
        return problemPic;
    }

    public void setProblemPic(String problemPic) {
        this.problemPic = problemPic;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public String getProblemID() {
        return this.problemID;
    }
    public void setProblemID(String problemID) {
        this.problemID = problemID;
    }
    @JsonIgnore
    public long getTimestampCreatedLong() {

        return (long) ((timestampCreated != null) ? timestampCreated.get(Constants.FIREBASE_PROPERTY_TIMESTAMP) : 0L);
    }

    @JsonIgnore
    public long getTimestampLastChangedLong() {

        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public void setTimestampLastChangedToNow() {
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(problem);
        dest.writeString(description);
        dest.writeString(location);
        dest.writeString(problemPic);
    }

    private Problem(Parcel in){
        problem = in.readString();
        description = in.readString();
        location = in.readString();
        problemPic = in.readString();
    }

    public static final Creator<Problem> CREATOR = new Creator<Problem>() {
        @Override
        public Problem createFromParcel(Parcel source) {
            return new Problem(source);
        }

        @Override
        public Problem[] newArray(int size) {
            return new Problem[size];
        }
    };

    @Override
    public String toString() {
        return "Problem{" +
                "timestampCreated=" + timestampCreated +
                ", postedBy='" + postedBy + '\'' +
                ", problem='" + problem + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", problemPic='" + problemPic + '\'' +
                '}';
    }
}


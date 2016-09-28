package com.ctoddcook.android.personcrud;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.Date;


/**
 * This is a model of the Person to be stored, entered and displayed. This class is as simple as
 * it can be, containing only the fields required for storing and displaying data, get and set
 * accessors, and an internal SanityException class for communicating data input errors.
 * <p/>
 * Created by C. Todd Cook on 9/14/2016.<br>
 * ctodd@ctoddcook.com
 */

// WeakerAccess suppressed because it warns that the "public" access indicator for methods can be
// removed since all calls come from within the package; but following that suggestion interferes
// with Firebase and causes exceptions.
@SuppressWarnings("WeakerAccess")
class PersonModel {
    private static final String TAG = "PersonModel";

    private String mID;
    private Date mDateOfBirth;
    private String mFirstName;
    private String mLastName;
    private int mZipCode;
    private Date mDateUpdated;
    private String mUserUpdated;

    private static final long INITIAL_DATE = 1;
    private static final int INITIAL_ZIP_CODE = 1;
    private static final int LOWEST_US_ZIP_CODE = 501;
    private static final int HIGHEST_US_ZIP_CODE = 99950;


    //
    // CONSTRUCTORS
    //

    public PersonModel() {
        Log.d(TAG, "PersonModel: constructor");
        mFirstName = "";
        mLastName = "";
        mDateOfBirth = new Date(INITIAL_DATE);
        mZipCode = INITIAL_ZIP_CODE;
        mDateUpdated = null;
        mUserUpdated = "";
    }


    //
    // ACCESSORS
    //

    /**
     * Accessor to retrieve the database ID.
     *
     * @return The database ID of the person.
     */
    // We exclude this from Firebase, since the ID here does not need to be stored in the database.
    @Exclude
    public String getID() {
        return mID;
    }

    /**
     * Accessor to set the database ID. Should only be used when inserting an instance into the
     * database.
     *
     * @param id The database ID returned by the Firebase database.
     */
    public void setID(final String id) {
        Log.d(TAG, "setID: " + id);
        mID = id;
    }

    /**
     * Accessor to retrieve the date of birth.
     *
     * @return A references to the date of birth of this person.
     */
    public long getDateOfBirth() {
        return mDateOfBirth.getTime();
    }

    /**
     * Accessor to set the date of birth.
     *
     * @param dateOfBirth The new birth date for this person.
     */
    @Exclude    // Annotation tells Firebase to ignore this overload of the setter
    public void setDateOfBirth(final Date dateOfBirth) throws SanityException {
        Log.d(TAG, "setDateOfBirth(Date): " + dateOfBirth);
        if (dateOfBirth == null) {
            throw new SanityException(Field.DATE_OF_BIRTH, "Date of Birth has not been set");
        }

        if (dateOfBirth.after(Calendar.getInstance().getTime())) {
            throw new SanityException(Field.DATE_OF_BIRTH, "Date of Birth cannot be in the future");
        }

        mDateOfBirth = dateOfBirth;
    }

    /**
     * Accessor to set the date of birth.
     *
     * @param dateOfBirth The new birth date, in millis.
     */
    @SuppressWarnings("unused")     // Suppressed because Firebase uses this method
    public void setDateOfBirth(final long dateOfBirth) throws SanityException {
        Log.d(TAG, "setDateOfBirth(long): " + dateOfBirth);
        if (dateOfBirth == 0L) {
            throw new SanityException(Field.DATE_OF_BIRTH, "Date of Birth has not been set");
        }

        if (dateOfBirth > Calendar.getInstance().getTimeInMillis()) {
            throw new SanityException(Field.DATE_OF_BIRTH, "Date of Birth cannot be in the future");
        }

        mDateOfBirth = new Date(dateOfBirth);
    }

    /**
     * Accessor to retrieve the first name.
     *
     * @return A reference to the first name of this person.
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * Accessor to set the first name.
     *
     * @param firstName The new first name for this person.
     */
    public void setFirstName(final String firstName) throws SanityException {
        Log.d(TAG, "setFirstName: " + firstName);
        if (firstName == null || firstName.isEmpty()) {
            throw new SanityException(Field.FIRST_NAME, "First Name has not been set");
        }

        mFirstName = firstName;
    }

    /**
     * Accessor to retrieve the last name.
     *
     * @return A reference to the first name of this person.
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * Accessor to set the last name of this person.
     *
     * @param lastName The new last name for this person.
     */
    public void setLastName(final String lastName) throws SanityException {
        Log.d(TAG, "setLastName: " + lastName);
        if (lastName == null || lastName.isEmpty()) {
            throw new SanityException(Field.LAST_NAME, "Last Name has not been set");
        }

        mLastName = lastName;
    }

    /**
     * Accessor to retrieve the zip code.
     *
     * @return The zip code of this person.
     */
    public int getZipCode() {
        return mZipCode;
    }

    /**
     * Accessor to set the zip code.
     *
     * @param zipCode The new zip code for this person.
     */
    public void setZipCode(final int zipCode) throws SanityException {
        Log.d(TAG, "setZipCode: " + zipCode);
        if (zipCode == INITIAL_ZIP_CODE) {
            throw new SanityException(Field.ZIP_CODE, "Zip Code has not been set");
        }

        if (zipCode < LOWEST_US_ZIP_CODE) {
            throw new SanityException(Field.ZIP_CODE, "Zip Code is too low. The lowest US zip " +
                "code is " + Formatter.formatZipCode(LOWEST_US_ZIP_CODE));
        }

        if (zipCode > HIGHEST_US_ZIP_CODE) {
            throw new SanityException(Field.ZIP_CODE, "Zip Code is too high. The highest US zip " +
                "code is " + Formatter.formatZipCode(HIGHEST_US_ZIP_CODE));
        }

        mZipCode = zipCode;
    }

    /**
     * Used to update two fields which provide metadata about who and when this Person was last
     * updated.
     *
     * @param user The user who initiated the update to this Person
     */
    public void touch(@NonNull final String user) {
        mDateUpdated = Calendar.getInstance().getTime();
        mUserUpdated = user;
    }

    /**
     * Accessor to retrieve the date this Person was last updated.
     *
     * @return The Date this Person was last updated.
     */
    public long getDateUpdated() {
        return mDateUpdated.getTime();
    }

    /**
     * Accessor to set the metadata item indicating when this record was last updated. Should
     * only be called when retrieving from the database.
     *
     * @param dateUpdated The DateTime the record was last updated.
     */
    @SuppressWarnings("unused")     // Suppressed because Firebase uses the method
    public void setDateUpdated(final long dateUpdated) {
        Log.d(TAG, "setDateUpdated: " + dateUpdated);
        mDateUpdated = new Date(dateUpdated);
    }

    /**
     * Accessor to retrieve the User who last updated this Person
     *
     * @return The user who last updated this Person.
     */
    public String getUserUpdated() {
        return mUserUpdated;
    }

    /**
     * Accessor to set the metadata item indicating who last updated this record. Should only be
     * set when retrieving records from the database.
     *
     * @param userUpdated The user who last updated this Person.
     */
    @SuppressWarnings("unused")     // Suppressed because Firebase uses the method
    public void setUserUpdated(final String userUpdated) {
        Log.d(TAG, "setUserUpdated: " + userUpdated);
        mUserUpdated = userUpdated;
    }


    /*
     * This enum is used to indicate which member field contains a validation exception.
     */
    enum Field {
        FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, ZIP_CODE
    }

    /*
     * This internal class is used to communicate not just a message about what is found wrong in
     * validation checks, but also which field contains the problem.
     */
    class SanityException extends Exception {
        private final Field mInvalidField;

        SanityException(Field field, String message) {
            super(message);
            mInvalidField = field;
        }

        Field getInvalidField() {
            return mInvalidField;
        }
    }
}

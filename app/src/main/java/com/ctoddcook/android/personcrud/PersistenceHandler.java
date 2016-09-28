package com.ctoddcook.android.personcrud;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class handles inserting, updating and deleting Person records via Firebase. This
 * is a singleton class, so callers must use <code>getInstance()</code> to retrieve a reference
 * to the functioning instance.
 * <p>
 * Created by C. Todd Cook on 9/21/2016.<br>
 * ctodd@ctoddcook.com
 */
class PersistenceHandler {
    private final static PersistenceHandler singleInstance = new PersistenceHandler();
    private final static DatabaseReference mDBRef = FirebaseDatabase.getInstance()
        .getReference().child("data").child("persons");

    private final static ArrayList<PersonModel> mPersonList = new ArrayList<>();
    private final static HashMap<String, PersonModel> mPersonMap = new HashMap<>();


    /**
     * Used to get a reference to the single instance of this class.
     *
     * @return A reference to the one instance of PersistenceHandler.
     */
    synchronized static PersistenceHandler getInstance() {
        return singleInstance;
    }

    private PersistenceHandler() {
    }

    /**
     * Provides a common DatabaseReference for use by other classes which need one, so they don't
     * have to make their own.
     *
     * @return The Firebase DatabaseReference to our database of Person objects.
     */
    static DatabaseReference getDBRef() {
        return mDBRef;
    }

    /**
     * Inserts a new Person object into the Firebase object.
     *
     * @param person The new Person object to be saved into the database.
     */
    void insertPerson(final PersonModel person) {
        DatabaseReference postRef = mDBRef.push();
        postRef.setValue(person);
    }

    /**
     * Updates the fields in the database for a given Person. Any data already in the record will
     * be replaced.
     *
     * @param person The PersonModel object to be stored to the Firebase database.
     */
    void updatePerson(final PersonModel person) throws IllegalStateException {
        if (person.getID() == null) {
            throw new IllegalStateException("Cannot update a Person object with a null ID");
        }
        DatabaseReference childRef = mDBRef.child(person.getID());
        childRef.setValue(person);
    }

    /**
     * Deletes a Person from the Firebase database.
     *
     * @param person The Person/record to be deleted.
     */
    void deletePerson(final PersonModel person) {
        DatabaseReference childRef = mDBRef.child(person.getID());
        childRef.setValue(null);
    }

    /**
     * Retrieves a list of Persons from a DataSnapshot and returns it as an ArrayList. This can
     * be used by any code which receives an <code>ValueEventListener.onDataChange()</code>
     * notification, which occurs after a change is made to the Firebase data.
     *
     * @param snapshot The snapshot provided by the Firebase database when there's an update.
     * @return An ArrayList containing the Person objects from the snapshot.
     */
    ArrayList<PersonModel> getPersonListFromSnapshot(DataSnapshot snapshot) {
        PersonModel personInList;

        mPersonList.clear();
        mPersonMap.clear();

        ArrayList<PersonModel> list = new ArrayList<>((int) snapshot.getChildrenCount());

        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
            personInList = postSnapshot.getValue(PersonModel.class);
            personInList.setID(postSnapshot.getKey());
            list.add(personInList);
            mPersonList.add(personInList);
            mPersonMap.put(personInList.getID(), personInList);
        }

        return list;
    }

    /**
     * Returns the person for a given ID.
     *
     * @param personID The ID for the Person to be returned.
     * @return A Person for the given ID.
     */
    PersonModel fetchPerson(String personID) {
        return mPersonMap.get(personID);
    }

    /**
     * Returns a full list of Persons.
     *
     * @return ArrayList of all the Person objects from the database.
     */
    ArrayList<PersonModel> fetchPersonList() {
        return mPersonList;
    }
}

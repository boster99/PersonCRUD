package com.ctoddcook.android.personcrud;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.ctoddcook.android.personcrud.MainActivity.Sort.BY_BIRTH_DATE;
import static com.ctoddcook.android.personcrud.MainActivity.Sort.BY_FIRST_NAME;
import static com.ctoddcook.android.personcrud.MainActivity.Sort.BY_LAST_NAME;
import static com.ctoddcook.android.personcrud.MainActivity.Sort.BY_ZIP_CODE;
import static com.ctoddcook.android.personcrud.MainActivity.Sort.NONE;


/**
 * The main activity for the app. Displays rows of PersonModel records, and provides access to
 * add/edit, and view details activities.
 * <p>
 * Created by C. Todd Cook on 9/15/2016.<br>
 * ctodd@ctoddcook.com
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    static ArrayList<PersonModel> mPersonList;
    private ListView mPersonLV;

    enum Sort {
        NONE, BY_FIRST_NAME, BY_LAST_NAME, BY_BIRTH_DATE, BY_ZIP_CODE
    }

    private Sort mPersonSort = NONE;

    // Firebase-related instance variables
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private final DatabaseReference mDBRef = PersistenceHandler.getDBRef();
    private final PersistenceHandler mPersistenceHandler = PersistenceHandler.getInstance();


    /**
     * Perform initialization of the activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Note: Otherwise it is
     *                           null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPersonLV = (ListView) findViewById(R.id.PersonList);
        registerForContextMenu(mPersonLV);

        // Get a reference to the Firebase user. If the user is null, switch to the login activity.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // If the user is null, then s/he is not logged in, so switch to the login activity
            launchLogin();
        }

        setupFABListener();
        setupFirebaseListener();
        setupListClickListener();
        setupSortListeners();
    }

    /**
     * Sets up a listener for when data in the Firebase database is updated. When a PersonModel
     * record is added to the database, or deleted, or updated, this gets fired and the displayed
     * list is refreshed.
     */
    private void setupFirebaseListener() {
        mDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mPersonList = mPersistenceHandler.getPersonListFromSnapshot(snapshot);
                sortPeople();
                mPersonLV.setAdapter(new PersonRow(MainActivity.this, mPersonList));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    /**
     * Sets up a listener for when the user touches a row of data, and responds to a touch by
     * opening the ViewDetailActivity.
     */
    private void setupListClickListener() {
        mPersonLV.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent intent = new Intent(MainActivity.this, ViewDetailActivity.class);
                intent.putExtra(ViewDetailActivity.ARG_POSITION, pos);
                startActivity(intent);
            }
        });
    }

    /**
     * Sets up listeners for when the user touches one of the column headers over the rows of
     * data. When one of the headers is touched, the rows are sorted by that column.
     */
    private void setupSortListeners() {
        TextView firstNameTV = (TextView) findViewById(R.id.col_first_name);
        firstNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortPeople(BY_FIRST_NAME);
                mPersonLV.setAdapter(new PersonRow(MainActivity.this, mPersonList));
            }
        });

        TextView lastNameTV = (TextView) findViewById(R.id.col_last_name);
        lastNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortPeople(BY_LAST_NAME);
                mPersonLV.setAdapter(new PersonRow(MainActivity.this, mPersonList));
            }
        });

        TextView birthDateTV = (TextView) findViewById(R.id.col_birth_date);
        birthDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortPeople(BY_BIRTH_DATE);
                mPersonLV.setAdapter(new PersonRow(MainActivity.this, mPersonList));
            }
        });

        TextView zipCodeTV = (TextView) findViewById(R.id.col_zip_code);
        zipCodeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortPeople(BY_ZIP_CODE);
                mPersonLV.setAdapter(new PersonRow(MainActivity.this, mPersonList));
            }
        });
    }

    /*
     * Sets up a listener on the floating action button.
     */
    private void setupFABListener() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: inside method");
                Intent intent = new Intent(MainActivity.this, AddEditPersonActivity.class);

                // Tell the new activity whether we're in ADD mode or EDIT mode
                intent.putExtra(AddEditPersonActivity.KEY_ADD_EDIT_MODE, AddEditPersonActivity.MODE_ADD);

                // Tell the new activity who the user is
                intent.putExtra(AddEditPersonActivity.KEY_USER_ID, mFirebaseUser.getEmail());
                startActivity(intent);
            }
        });
    }

    /**
     * Called when the options menu is about to be shown. Unlike onCreateContextMenu(), this is
     * called only when the activity is created, rather than every time the user calls for the menu.
     *
     * @param menu The menu which is being built
     * @return Always true (otherwise the menu doesn't display)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Responds to the user touching a menu item.
     *
     * @param item The menu item which was touched.
     * @return True, or whatever super.onOptionItemSelected() returns.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                launchLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Launches the login activity, and closes this activity. Duh.
     */
    private void launchLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Called when a context menu for the {@code view} is about to be shown. Unlike
     * onCreateOptionsMenu(Menu), this will be called every time the context menu is about to be
     * shown and should be populated for the view .
     * <p>
     * Use {@link #onContextItemSelected(MenuItem)} to know when an item has been selected.
     *
     * @param menu     The menu which is being built
     * @param v        The view for which the menu is being built
     * @param menuInfo Additional information about the the item for which the context menu will be
     *                 shown. Since we only have one ListView shown, and only one context menu,
     *                 this can be ignored.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() != R.id.PersonList) return;  // Pop the menu ONLY for Person rows

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_popup, menu);
    }

    /**
     * Called when the user selects one of the options on the context menu. We use the provided
     * MenuItem value to determine which row was clicked, and from the row we get the Person
     * which is to be edited or deleted.
     *
     * @param item The menu item which was selected
     * @return True if we can identify which menu item was clicked and respond to it appropriately
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        PersonModel person = (PersonModel) mPersonLV.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.person_edit:
                addEditPerson(person);
                return true;
            case R.id.person_delete:
                deletePerson(person);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Opens the Add/Edit PersonModel activity, put it in EDIT mode, and give it the person id to
     * edit.
     *
     * @param person The Person to edit
     */
    private void addEditPerson(final PersonModel person) {
        Intent intent = new Intent(this, AddEditPersonActivity.class);

        // Tell the new activity whether we're in EDIT mode, and which Person is to be edited
        intent.putExtra(AddEditPersonActivity.KEY_ADD_EDIT_MODE, AddEditPersonActivity.MODE_EDIT);
        intent.putExtra(AddEditPersonActivity.KEY_ARRAY_INDEX, mPersonList.indexOf(person));
        intent.putExtra(AddEditPersonActivity.KEY_USER_ID, mFirebaseUser.getEmail());

        startActivity(intent);
    }


    /**
     * Get confirmation from the user that s/he really wants to delete the selected person, then
     * delete it (or cancel).
     *
     * @param person The Person to be deleted.
     */
    private void deletePerson(final PersonModel person) {
        if (person == null) return;

        // Setup the listeners which will respond to the user's response to the dialog, the code
        // for which is at the end of this method
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // If the user clicks "YES" then we delete the Person
                    case DialogInterface.BUTTON_POSITIVE:
                        mPersistenceHandler.deletePerson(person);
                        break;

                    // If the user clicks "NO" then we clean up and get out of here
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        // Set up and display the dialog to get the user's confirmation that the person should be
        // deleted.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_alert_title));
        builder.setMessage(getString(R.string.delete_alert_message))
            .setPositiveButton(getString(R.string.delete_alert_btn_yes), dialogClickListener)
            .setNegativeButton(getString(R.string.delete_alert_btn_no), dialogClickListener).show();
    }

    /**
     * Sorts the Person list according to the indicated sort order.
     *
     * @param sort The order by which the list should be sorted.
     */
    private void sortPeople(Sort sort) {
        mPersonSort = sort;

        TextView colFirstName = (TextView) findViewById(R.id.col_first_name);
        TextView colLastName = (TextView) findViewById(R.id.col_last_name);
        TextView colBirthDate = (TextView) findViewById(R.id.col_birth_date);
        TextView colZipCode = (TextView) findViewById(R.id.col_zip_code);

        // Return all column header typefaces to normal.
        colFirstName.setTypeface(null, Typeface.NORMAL);
        colLastName.setTypeface(null, Typeface.NORMAL);
        colBirthDate.setTypeface(null, Typeface.NORMAL);
        colZipCode.setTypeface(null, Typeface.NORMAL);

        if (mPersonList == null || mPersonList.isEmpty()) {
            return;
        }

        /*
         Depending on which column we're sorting by, run a sort and change the column header's
         text typeface to BOLD.
         */
        switch (mPersonSort) {
            case NONE:
                return;

            case BY_FIRST_NAME:
                Collections.sort(mPersonList, new Comparator<PersonModel>() {
                    @Override
                    public int compare(PersonModel p1, PersonModel p2) {
                        return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
                    }
                });
                colFirstName.setTypeface(null, Typeface.BOLD);
                break;

            case BY_LAST_NAME:
                Collections.sort(mPersonList, new Comparator<PersonModel>() {
                    @Override
                    public int compare(PersonModel p1, PersonModel p2) {
                        return p1.getLastName().compareToIgnoreCase(p2.getLastName());
                    }
                });
                colLastName.setTypeface(null, Typeface.BOLD);
                break;

            case BY_BIRTH_DATE:
                Collections.sort(mPersonList, new Comparator<PersonModel>() {
                    @Override
                    public int compare(PersonModel p1, PersonModel p2) {
                        long compare = p1.getDateOfBirth() - p2.getDateOfBirth();
                        return (int) (compare / 100000);
                    }
                });
                colBirthDate.setTypeface(null, Typeface.BOLD);
                break;

            case BY_ZIP_CODE:
                Collections.sort(mPersonList, new Comparator<PersonModel>() {
                    @Override
                    public int compare(PersonModel p1, PersonModel p2) {
                        return p1.getZipCode() - p2.getZipCode();
                    }
                });
                colZipCode.setTypeface(null, Typeface.BOLD);
                break;

            default:
                throw new IllegalArgumentException("Unknown value for parameter 'sort'");
        }
    }

    /**
     * Sorts the Person list based on the current default sort.
     */
    private void sortPeople() {
        sortPeople(mPersonSort);
    }
}

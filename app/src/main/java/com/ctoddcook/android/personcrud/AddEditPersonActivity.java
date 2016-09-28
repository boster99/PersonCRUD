package com.ctoddcook.android.personcrud;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.GregorianCalendar;


/**
 * This activity provides functionality for adding a new or editing an existing PersonModel record.
 * <p>
 * Created by C. Todd Cook on 9/15/2016.<br>
 * ctodd@ctoddcook.com
 */
public class AddEditPersonActivity extends AppCompatActivity
    implements DatePickerFragment.DatePickerCaller {

    private static final String TAG = "AddEditPersonActivity";

    private int mMode;
    public static final int MODE_ADD = 1;
    public static final int MODE_EDIT = 2;
    public static final String KEY_ADD_EDIT_MODE = "com.ctoddcook.personcrud.ADD_EDIT_MODE";
    public static final String KEY_ARRAY_INDEX = "com.ctoddcook.personcrud.ARRAY_INDEX";
    public static final String KEY_USER_ID = "com.ctoddcook.personcrud.USER_ID";

    private EditText mFirstNameET;
    private EditText mLastNameET;
    private TextView mBirthDateTV;
    private EditText mZipCodeET;
    private Date mBirthDate;

    private PersonModel mPerson;
    private String mUser;

    private final PersistenceHandler mPersistenceHandler = PersistenceHandler.getInstance();


    /**
     * When this activity is created, we establish references to some on-screen fields and the
     * Firebase user, and importantly we determine whether we're in ADD mode or EDIT mode.
     *
     * @param savedInstanceState Data for reincarnating an activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: inside method");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_person);

        mFirstNameET = (EditText) findViewById(R.id.edit_person_first_name);
        mLastNameET = (EditText) findViewById(R.id.edit_person_last_name);
        mBirthDateTV = (TextView) findViewById(R.id.edit_person_birth_date);
        mZipCodeET = (EditText) findViewById(R.id.edit_person_zip_code);


        mUser = getIntent().getStringExtra(KEY_USER_ID);
        if (mUser == null || mUser.isEmpty()) {
            throw new IllegalArgumentException("A user must be provided by the calling method");
        }

        mMode = getIntent().getIntExtra(KEY_ADD_EDIT_MODE, -1);

        switch (mMode) {
            case MODE_ADD:
                Log.d(TAG, "onCreate: mode add");
                mPerson = new PersonModel();
                break;
            case MODE_EDIT:
                int arrayIndex = getIntent().getIntExtra(KEY_ARRAY_INDEX, -1);
                if (arrayIndex == -1)
                    throw new IllegalArgumentException("In edit mode, an array index must be " +
                        "provided");

                mPerson = MainActivity.mPersonList.get(arrayIndex);

                mFirstNameET.setText(mPerson.getFirstName());
                mLastNameET.setText(mPerson.getLastName());
                mBirthDate = new Date(mPerson.getDateOfBirth());
                mBirthDateTV.setText(Formatter.formatBirthDate(mBirthDate));
                mZipCodeET.setText(Formatter.formatZipCode(mPerson.getZipCode()));

                break;
            default:
                throw new IllegalArgumentException("Calling process must specify add/edit mode");
        }

        setupBirthDateListeners();
        mFirstNameET.requestFocus();
    }

    /**
     * When the user touches the "Save" button, extract all of the entered details, and
     * then--depending on whether we're in ADD or EDIT mode, insert a new Person or update an
     * existing one in the database.
     *
     * @param v Unused, but required by the onClick attribute of the button
     */
    public void savePerson(View v) {
        try {
            // Reset any error messages
            mFirstNameET.setError(null);
            mLastNameET.setError(null);
            mBirthDateTV.setError(null);
            mZipCodeET.setError(null);

            // Transfer details from the on-screen fields to the PersonModel object
            mPerson.setFirstName(mFirstNameET.getText().toString().trim());
            mPerson.setLastName(mLastNameET.getText().toString().trim());
            mPerson.setDateOfBirth(mBirthDate);
            mPerson.setZipCode(Integer.parseInt(mZipCodeET.getText().toString().trim()));
            mPerson.touch(mUser);

            // If one of the set accessors throws a SanityException, show the error and set focus
            // to the problem field
        } catch (PersonModel.SanityException e) {
            TextView textView = null;

            switch (e.getInvalidField()) {
                case FIRST_NAME:
                    textView = mFirstNameET;
                    break;
                case LAST_NAME:
                    textView = mLastNameET;
                    break;
                case DATE_OF_BIRTH:
                    textView = mBirthDateTV;
                    break;
                case ZIP_CODE:
                    textView = mZipCodeET;
                    break;
            }

            if (textView == null) {
                throw new NullPointerException("Thrown SanityException did not indicate a known " +
                    "field.");
            } else {
                textView.requestFocus();
                textView.setError(e.getMessage());
                return;
            }
        }

        // If there are no problems with the input data, store the record
        switch (mMode) {
            case MODE_ADD:
                mPersistenceHandler.insertPerson(mPerson);
                break;
            case MODE_EDIT:
                mPersistenceHandler.updatePerson(mPerson);
                break;
            default:
                throw new IllegalArgumentException("Member field mode does not " +
                    "indicate either ADD or EDIT");
        }

        this.finish();
    }


    /**
     * User has touched "Cancel". Just get out of here.
     *
     * @param v The View which triggered this method call
     */
    public void cancelEdits(View v) {
        this.finish();
    }

    /**
     * Opens a Date Picker dialog for the user to change the date of fill. Pre-set the picker
     * with the current value of the mBirthDate instance variable (which may be today's date or
     * the Person's birth date, depending on whether we're in ADD mode or EDIT mode).
     */
    public void showDatePickerDialog() {
        /*
         * If mBirthDate is null, then we're in ADD mode, and we initialize the date picker with
         * today's date. Otherwise, we initialize it with the already existing date for the Person,
         * mBirthDate.
         */
        long date = mBirthDate != null ? mBirthDate.getTime() : 0;

        DialogFragment datePicker = DatePickerFragment.newInstance(date);
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Used by the Date Picker dialog to pass back the date selected by the user. The date is
     * saved in an instance variable so it can easily be set into the mPerson object, and
     * displayed on screen.
     *
     * @param year  the mYear selected by the user
     * @param month the month selected by the user
     * @param day   the day selected by the user
     */
    public void setSelectedDate(int year, int month, int day) {
        mBirthDate = new GregorianCalendar(year, month, day, 12, 0).getTime();
        mBirthDateTV.setText(Formatter.formatBirthDate(mBirthDate));
        mBirthDateTV.setError(null);
        mZipCodeET.requestFocus();
    }

    /*
     * Setup event listeners on the BirthDate TextView. The UI flow/model goes like this:
     *   1) If the BirthDate gains focus without a touch, and is empty, it automatically pops up
     *      the date picker dialog. Presumably, a date needs to be chosen.
     *   2) If the BirthDate gains focus without a touch, and is NOT empty, then it moves the
     *      focus on to the next field (Zip Code). We don't know whether it is the user's intention
     *      to change the date, and if the user DOES want to change the date s/he will likely just
     *      touch the BirthDate TextView.
     *   3) If the BirthDate is touched, then regardless of whether a date is already in place, we
     *      pop open the date picker.
     *
     * This is harder than it might otherwise seem. At first I just used an OnFocusChange
     * listener and an OnClick listener. This presented a problem, because when the user touched
     * the BirthDate TextView, if it didn't already have the focus, then it would gain the focus,
     * the OnFocusChange event would fire, and it would prevent the OnClick event from firing. If
     * the TextView was empty, that was okay, because as per the model above, the date picker
     * would be popped up. But if there was already a date in the TextView, then per the second
     * rule above the date picker would not be presented; and since the OnClick event was not
     * fired rule 3 was not activated.
     *
     * The result was that if there was a date in the TextView, and the user touched the TextView
     * in order to access the picker, the user would have to touch the TextView a second time
     * before the date picker would actually be shown.
     *
     * To solve this, we add the OnTouch listener. It includes information amounting to how the
     * TextView received focus ... i.e., was it actually touched. If it was actually touched,
     * the onTouch() method calls performClick() which will fire the OnClick event, and returns
     * FALSE which prevents the OnTouch event from firing.
     *
     * Complex, but makes the experience nicer for the user.
     */
    private void setupBirthDateListeners() {
        /*
         * Listen for the BirthDate TextView to be touched, and determine whether to allow the
         * OnFocusChange event to be fired, or to cause the OnClick event to be fired.
         */
        mBirthDateTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Was the mBirthDateTV touched? If so, cause the OnClick event to be fired and
                // prevent the OnFocusChange event from being fired.
                if (event.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                    view.performClick();
                    return false;
                } else {
                    return true;
                }
            }
        });

        /*
         * Listen for the BirthDate TextView to gain focus. (This will also be called when it
         * loses focus, but we don't care about that.)
         */
        mBirthDateTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // Only interested when we gain focus.
                if (hasFocus) {
                    // If we don't already have a date, pop open the date picker, otherwise move
                    // the focus on to the next field.
                    if (mBirthDate == null) {
                        showDatePickerDialog();
                    } else {
                        mZipCodeET.requestFocus();
                    }
                }
            }
        });

        /*
         * Anytime we hear the OnClick event being fired, we pop open the date picker.
         */
        mBirthDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }
}

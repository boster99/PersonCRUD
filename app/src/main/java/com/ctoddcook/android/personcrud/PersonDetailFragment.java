package com.ctoddcook.android.personcrud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * This populates the display with the details from a particular Person.
 * <p>
 * Created by C. Todd Cook on 5/24/2016.<br>
 * ctodd@ctoddcook.com
 */
public class PersonDetailFragment extends Fragment {
    private static final String PERSON_ID = "PersonID";
    private PersonModel mPerson;

    /**
     * Android does not want parameters passed through a constructor; they only want parameters
     * passed through a Bundle, so parameters can be provided again later if the Fragment needs to
     * be reconstituted.
     *
     * @param personID the id of the Person to display
     * @return a new PersonDetailFragment
     */
    public static PersonDetailFragment getInstance(String personID) {
        PersonDetailFragment fragment = new PersonDetailFragment();
        Bundle args = new Bundle();
        args.putString(PERSON_ID, personID);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * This simply gets (from arguments) the ID of the Person to be displayed, then retrieves that
     * Person.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String personID = (getArguments() != null ? getArguments().getString(PERSON_ID) : "");
        mPerson = PersistenceHandler.getInstance().fetchPerson(personID);
    }

    /**
     * Puts the details from the Person in question into the TextViews which are displayed on
     * screen.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_person_details, container, false);

        TextView tvFirstName = (TextView) layoutView.findViewById(R.id.view_person_first_name);
        tvFirstName.setText(mPerson.getFirstName());

        TextView tvLastName = (TextView) layoutView.findViewById(R.id.view_person_last_name);
        tvLastName.setText(mPerson.getLastName());

        TextView tvBirthDate = (TextView) layoutView.findViewById(R.id.view_person_birth_date);
        tvBirthDate.setText(Formatter.formatBirthDate(mPerson.getDateOfBirth()));

        TextView tvZipCode = (TextView) layoutView.findViewById(R.id.view_person_zip_code);
        tvZipCode.setText(Formatter.formatZipCode(mPerson.getZipCode()));

        TextView tvDTUpdated = (TextView) layoutView.findViewById(R.id.view_person_dt_updated);
        tvDTUpdated.setText(Formatter.formatMLDateTime(mPerson.getDateUpdated()));

        TextView tvUserUpdated = (TextView) layoutView.findViewById(R.id.view_person_updated_by);
        tvUserUpdated.setText(mPerson.getUserUpdated());

        return layoutView;
    }
}

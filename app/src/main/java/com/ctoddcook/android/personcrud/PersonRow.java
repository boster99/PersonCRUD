package com.ctoddcook.android.personcrud;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class is used for presenting the details of a Person instance in a scrollable list of
 * rows. The <code>getView()</code> method is the primary method which uses recycled rows of
 * TextViews, or creates new ones when needed.
 * <p>
 * Created by C. Todd Cook on 9/15/2016.<br>
 * ctodd@ctoddcook.com
 */
class PersonRow extends ArrayAdapter<PersonModel> {
    private final Context mContext;
    private final ArrayList<PersonModel> mPersonList;


    /**
     * Constructor.
     *
     * @param context    The current context.
     * @param personList The list of Person objects to be displayed.
     */
    public PersonRow(Context context, ArrayList<PersonModel> personList) {
        super(context, R.layout.row_person, personList);
        mContext = context;
        mPersonList = personList;
    }

    /**
     * Get a View that displays the data at the specified position. This takes a
     *
     * @param pos         The position of the item within the adapter's data set--the list position of the
     *                    object we want to display in a row.
     * @param convertView The old row to reuse, if possible, or null if there is not a row
     *                    available for reuse.
     * @param parent      The parent that this row will be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PersonModel person = mPersonList.get(pos);

        /*
        Reuse views. Only create a row from scratch if the call to this method did not give us
        an old (no longer visible) view we could reuse.
        */
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_person, parent, false);

            ViewHolder newHolder = new ViewHolder();
            newHolder.tvFirstName = (TextView) row.findViewById(R.id.col_first_name);
            newHolder.tvLastName = (TextView) row.findViewById(R.id.col_last_name);
            newHolder.tvBirthDate = (TextView) row.findViewById(R.id.col_birth_date);
            newHolder.tvZipCode = (TextView) row.findViewById(R.id.col_zip_code);
            row.setTag(newHolder);
        }

        ViewHolder holder = (ViewHolder) row.getTag();

        holder.setFirstName(person.getFirstName());
        holder.setLastName(person.getLastName());
        holder.setBirthDate(new Date(person.getDateOfBirth()));
        holder.setZipCode(person.getZipCode());

        return row;

    }

    /*
    This simple class holds references to the TextViews in a row. It is used in the getView()
    method for quickly setting the displayed data in new or recycled rows.
     */
    private static class ViewHolder {
        TextView tvFirstName;
        TextView tvLastName;
        TextView tvBirthDate;
        TextView tvZipCode;

        void setFirstName(String firstName) {
            tvFirstName.setText(firstName);
        }

        void setLastName(String lastName) {
            tvLastName.setText(lastName);
        }

        void setBirthDate(Date birthDate) {
            tvBirthDate.setText(Formatter.formatBirthDate(birthDate));
        }

        void setZipCode(int zipCode) {
            tvZipCode.setText(Formatter.formatZipCode(zipCode));
        }
    }
}
